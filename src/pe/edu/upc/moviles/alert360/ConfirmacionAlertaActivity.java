package pe.edu.upc.moviles.alert360;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import pe.edu.upc.moviles.alert360.bl.be.Receptor;
import pe.edu.upc.moviles.alert360.bl.be.UsuarioBE;
import pe.edu.upc.moviles.alert360.bl.constantes.VariablesGlobales;
import pe.edu.upc.moviles.alert360.dl.bd.MySQLiteDataSource;
import pe.edu.upc.moviles.alert360.dl.correo.GMailSender;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

public class ConfirmacionAlertaActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	LocationClient mLocationClient;
	private MySQLiteDataSource dataSource;
	private SharedPreferences prefs, msgPref;
	String sesionActiva;
	UsuarioBE sesion;

	private boolean yaLanzoAlerta;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_confirmacion_alerta);

		yaLanzoAlerta=false;
		mLocationClient = new LocationClient(this, this, this);
		msgPref = getSharedPreferences(VariablesGlobales.PREF_MI_MENSAJE,
				Context.MODE_PRIVATE);

		dataSource = new MySQLiteDataSource(this);
		dataSource.open();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		// arg0.getErrorCode();
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		if(!yaLanzoAlerta)
		{
			displayCurrentLocation();
			yaLanzoAlerta=true;
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	private void AvisarReceptorXSMS(Location ubicacion) {
		List<Receptor> contactos = dataSource.ListarReceptores();
		String mimensaje = msgPref.getString(VariablesGlobales.PREF_MI_MENSAJE,
				VariablesGlobales.PREF_DEFAULT_MI_MENSAJE);

		if (contactos != null) {
			for (int i = 0; i < contactos.size(); i++) {
				SmsManager sms = SmsManager.getDefault();
				// Intent llamar=new Intent(Intent.ACTION_CALL);
				sms.sendTextMessage(
						contactos.get(i).getTel1(),
						null,
						mimensaje
								+ ".   MAPA: "
								+ " https://www.google.com.pe/maps?q=loc:"
								+ ubicacion.getLatitude() + ","
								+ ubicacion.getLongitude(),
						null, null);
				//AGREGADO
				if(contactos.get(i).getTel2().length()==9)
				{
					sms.sendTextMessage(
							contactos.get(i).getTel2(),
							null,
							mimensaje
									+ ".   MAPA: "
									+ " https://www.google.com.pe/maps?q=loc:"
									+ ubicacion.getLatitude() + ","
									+ ubicacion.getLongitude(),
							null, null);
				}
				if(contactos.get(i).getTel3().length()==9)
				{
					sms.sendTextMessage(
							contactos.get(i).getTel3(),
							null,
							mimensaje
									+ ".   MAPA: "
									+ " https://www.google.com.pe/maps?q=loc:"
									+ ubicacion.getLatitude() + ","
									+ ubicacion.getLongitude(),
							null, null);
				}
				//TERMINA AGREGADO
			}
		}

	}

	private void CorreoNotificar(Location ubicacion)
	{
		final List<Receptor> contactos = dataSource.ListarReceptores();
		final String mimensaje = msgPref.getString(VariablesGlobales.PREF_MI_MENSAJE,
				VariablesGlobales.PREF_DEFAULT_MI_MENSAJE);
		final GMailSender sender = new GMailSender("alert360.upc@gmail.com", "porel20upc");
		final double latitud=ubicacion.getLatitude();
		final double longitud=ubicacion.getLongitude();
		new Thread(new Runnable() {
			public void run() {
				try {
					for(int i=0;i<contactos.size();i++)
					{
						sender.sendMail(getResources().getString(R.string.msg_correo_asunto),
								mimensaje
								+ " https://www.google.com.pe/maps?q=loc:"
								+ latitud + ","
								+ longitud,
								"alert360.upc@gmail.com",
								contactos.get(i).getCorreo());
					}
					
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "Error",
							Toast.LENGTH_LONG).show();
				}
			}
		}).start();
	}
	public void displayCurrentLocation() {
		try {

			Location currentLocation = mLocationClient.getLastLocation();

			AvisarReceptorXSMS(currentLocation);
			SendAlerta(currentLocation.getLatitude(),
					currentLocation.getLongitude());
			CorreoNotificar(currentLocation);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mLocationClient.disconnect();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mLocationClient.disconnect();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mLocationClient.connect();
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		mLocationClient.connect();
		super.onStart();
	}

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
			parameters.add(new BasicNameValuePair("Tipo", "0"));

			caller.setParametersList(parameters);
			new CallServiceAlerta(caller).execute();

		} catch (Exception e1) {

			System.out.println(e1);
			e1.printStackTrace();
		}
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
					Toast.makeText(ConfirmacionAlertaActivity.this,
							getResources().getString(R.string.msg_alerta_registro_existoso),
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(ConfirmacionAlertaActivity.this,
							R.string.msg_excepcion, Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(ConfirmacionAlertaActivity.this,
						getResources().getString(R.string.msg_falla_comunicacion),
						Toast.LENGTH_SHORT).show();
			}
			// super.onPostExecute(result);
		}
	}
}
