package pe.edu.upc.moviles.alert360.bgservices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.gson.Gson;

import pe.edu.upc.moviles.alert360.R;
import pe.edu.upc.moviles.alert360.RESTClient;
import pe.edu.upc.moviles.alert360.ServiceCaller;
import pe.edu.upc.moviles.alert360.bl.be.Receptor;
import pe.edu.upc.moviles.alert360.bl.be.UsuarioBE;
import pe.edu.upc.moviles.alert360.bl.constantes.VariablesGlobales;
import pe.edu.upc.moviles.alert360.dl.bd.MySQLiteDataSource;
import pe.edu.upc.moviles.alert360.dl.correo.GMailSender;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

public class AcelerometroService extends Service implements
		SensorEventListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	private long lastUpdate = 0;
	@SuppressWarnings("unused")
	private float last_x, last_y, last_z;

	private MySQLiteDataSource dataSource;
	private SharedPreferences msgPref;

	private static final int VELOCIDAD_MINIMA = 1000;
	private static final int VELOCIDAD_MAXIMA = 1500;// 20

	private float DISTANCIA_MININA = (float) 10.0;// 15cm
	private float DISTANCIA_MAXIMA = (float) 30.0;// cm

	private boolean Estado = true;

	private LocationClient mLocationClient;

	private SharedPreferences prefs;
	String sesionActiva;
	UsuarioBE sesion;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		senAccelerometer = senSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		senSensorManager.registerListener(this, senAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

		mLocationClient = new LocationClient(getApplicationContext(), this,
				this);

		lastUpdate = System.currentTimeMillis();
		return super.onStartCommand(intent, flags, startId);
	}

	private void ReiniciarValores() {
		last_x = 0;
		last_y = 0;
		last_z = 0;

		lastUpdate = System.currentTimeMillis();

		Estado = true;
	}

	private void AvisarReceptorXSMS(Location ubicacion) {

		dataSource = new MySQLiteDataSource(this);
		dataSource.open();
		List<Receptor> contactos = dataSource.ListarReceptores();
		msgPref = getSharedPreferences(VariablesGlobales.PREF_MI_MENSAJE,
				Context.MODE_PRIVATE);
		String mimensaje = msgPref.getString(VariablesGlobales.PREF_MI_MENSAJE,
				VariablesGlobales.PREF_DEFAULT_MI_MENSAJE);

		if (contactos != null) {
			if (contactos.size() > 0) {
				for (int i = 0; i < contactos.size(); i++) {
					SmsManager sms = SmsManager.getDefault();
					// Intent llamar=new Intent(Intent.ACTION_CALL);
					sms.sendTextMessage(
							contactos.get(i).getTel1(),
							null,
							mimensaje + ".   MAPA: "
									+ " https://www.google.com.pe/maps?q=loc:"
									+ ubicacion.getLatitude() + ","
									+ ubicacion.getLongitude(), null, null);
					// AGREGADO
					if (contactos.get(i).getTel2().length() == 9) {
						sms.sendTextMessage(
								contactos.get(i).getTel2(),
								null,
								mimensaje
										+ ".   MAPA: "
										+ " https://www.google.com.pe/maps?q=loc:"
										+ ubicacion.getLatitude() + ","
										+ ubicacion.getLongitude(), null, null);
					}
					if (contactos.get(i).getTel3().length() == 9) {
						sms.sendTextMessage(
								contactos.get(i).getTel3(),
								null,
								mimensaje
										+ ".   MAPA: "
										+ " https://www.google.com.pe/maps?q=loc:"
										+ ubicacion.getLatitude() + ","
										+ ubicacion.getLongitude(), null, null);
					}
					// TERMINA AGREGADO
				}
			}
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		Sensor mySensor = event.sensor;
		if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			float speed;
			long curTime = System.currentTimeMillis();

			if ((curTime - lastUpdate) > 100) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				if (Estado) {
					if (Math.abs(y - last_y) * 100 < DISTANCIA_MININA
							&& Math.abs(y - last_y) * 100 > DISTANCIA_MAXIMA) {
						ReiniciarValores();
						return;
					}
					speed = Math.abs(y - last_y) / diffTime * 10000;
					if (speed > VELOCIDAD_MINIMA && speed < VELOCIDAD_MAXIMA) {
						Estado = false;
					}
				} else {
					if (Math.abs(z - last_z) * 100 < DISTANCIA_MININA
							&& Math.abs(z - last_z) * 100 > DISTANCIA_MAXIMA) {
						ReiniciarValores();
						return;
					}
					speed = Math.abs(z - last_z) / diffTime * 10000;
					if (speed > VELOCIDAD_MINIMA && speed < VELOCIDAD_MAXIMA) {
						Estado = true;
						ReiniciarValores();
						displayCurrentLocation();
					}
				}
				last_x = x;
				last_y = y;
				last_z = z;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		mLocationClient.connect();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Todavía no implementado");
	}

	public class CallServiceAlerta extends AsyncTask<String, Void, String> {

		ServiceCaller caller;
		ProgressDialog dialog;

		public CallServiceAlerta(ServiceCaller caller) {
			super();
			this.caller = caller;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String response = "";
			response = RESTClient.connectAndReturnResponsePost(caller.getUrl(),
					caller.getParametersList());
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub

			try {
				int ii = new Gson().fromJson(result, int.class);
				if (ii >= 1) {
					Toast.makeText(
							AcelerometroService.this,
							getResources().getString(
									R.string.msg_alerta_enviada),
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.msg_excepcion, Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.msg_falla_comunicacion),
						Toast.LENGTH_SHORT).show();
			}

			super.onPostExecute(result);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
	}

	private void CorreoNotificar(Location ubicacion) {
		dataSource = new MySQLiteDataSource(getApplicationContext());
		dataSource.open();
		final List<Receptor> contactos = dataSource.ListarReceptores();
		final String mimensaje = msgPref.getString(
				VariablesGlobales.PREF_MI_MENSAJE,
				VariablesGlobales.PREF_DEFAULT_MI_MENSAJE);
		final GMailSender sender = new GMailSender("alert360.upc@gmail.com",
				"porel20upc");
		final double latitud = ubicacion.getLatitude();
		final double longitud = ubicacion.getLongitude();
		new Thread(new Runnable() {
			public void run() {
				try {
					for (int i = 0; i < contactos.size(); i++) {
						sender.sendMail(
								getResources().getString(
										R.string.msg_correo_asunto),
								mimensaje
										+ " https://www.google.com.pe/maps?q=loc:"
										+ latitud + "," + longitud,
								"alert360.upc@gmail.com", contactos.get(i)
										.getCorreo());
					}

				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.msg_falla_comunicacion),
							Toast.LENGTH_LONG).show();
				}
			}
		}).start();
	}

	public void displayCurrentLocation() {
		try {
			Location currentLocation = mLocationClient.getLastLocation();

			AvisarReceptorXSMS(currentLocation);
			CorreoNotificar(currentLocation);
			SendAlerta(currentLocation.getLatitude(),
					currentLocation.getLongitude());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	/*
	 * @Override protected void onDestroy() { // TODO Auto-generated method stub
	 * mLocationClient.disconnect(); super.onDestroy(); }
	 * 
	 * @Override protected void onPause() { // TODO Auto-generated method stub
	 * mLocationClient.disconnect(); super.onPause(); }
	 * 
	 * @Override protected void onResume() { // TODO Auto-generated method stub
	 * mLocationClient.connect(); super.onResume(); }
	 * 
	 * @Override protected void onStop() { // TODO Auto-generated method stub
	 * mLocationClient.disconnect(); super.onStop(); }
	 * 
	 * @Override protected void onStart() { // TODO Auto-generated method stub
	 * mLocationClient.connect(); super.onStart(); }
	 */
	public void SendAlerta(Double Latitud, Double Longitud) {
		String URL = VariablesGlobales.URL_BASE_SERVICIOS + "/api/alertas";
		ServiceCaller caller = new ServiceCaller();
		String FechaAlerta = (String) java.text.DateFormat
				.getDateTimeInstance().format(Calendar.getInstance().getTime());

		prefs = getSharedPreferences(VariablesGlobales.PREF_NAME,
				Context.MODE_PRIVATE);
		String sesionActiva = prefs.getString(
				VariablesGlobales.PREF_SESION_ACTIVA,
				VariablesGlobales.PREF_DEFAULT_SESION_ACTIVA);
		ObjectMapper mapper = new ObjectMapper();
		try {
			sesion = mapper.readValue(sesionActiva, UsuarioBE.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			caller.setUrl(URL);

			parameters.add(new BasicNameValuePair("CodUsuario", sesion
					.getCodUsuario().toString()));
			parameters.add(new BasicNameValuePair("Latitud", Double
					.toString(Latitud)));
			parameters.add(new BasicNameValuePair("Longitud", Double
					.toString(Longitud)));
			parameters.add(new BasicNameValuePair("Fecha", FechaAlerta));
			parameters.add(new BasicNameValuePair("HusoHorario", "-5"));
			parameters.add(new BasicNameValuePair("Tipo", "1"));

			caller.setParametersList(parameters);
			new CallServiceAlerta(caller).execute();

		} catch (Exception e1) {

			System.out.println(e1);
			e1.printStackTrace();
		}
	}
}
