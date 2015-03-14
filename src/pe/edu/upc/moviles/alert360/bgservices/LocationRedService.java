package pe.edu.upc.moviles.alert360.bgservices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import pe.edu.upc.moviles.alert360.R;
import pe.edu.upc.moviles.alert360.RESTClient;
import pe.edu.upc.moviles.alert360.ServiceCaller;

import pe.edu.upc.moviles.alert360.bl.be.UsuarioBE;
import pe.edu.upc.moviles.alert360.bl.constantes.VariablesGlobales;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

public class LocationRedService extends Service implements LocationListener {

	TextView tvLatitud, tvLongitud;
	LocationManager locationManager;
	String provider;
	private SharedPreferences prefs;
	String sesionActiva;
	UsuarioBE sesion;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);

		if (provider != null && !provider.equals("")) {

			// Get the location from the given provider
			Location location = locationManager.getLastKnownLocation(provider);

			locationManager.requestLocationUpdates(provider, 20000, 1, this);

			if (location != null) {
				onLocationChanged(location);
				SendAlerta(location);
			}

			else {
				Toast.makeText(getBaseContext(),
						"No se pudo obtener localizacion", Toast.LENGTH_SHORT)
						.show();
				// SOLO PARA TEST
				SendAlerta(location);
			}

		} else {
			Toast.makeText(getBaseContext(), "No Provider Found",
					Toast.LENGTH_SHORT).show();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		tvLatitud.setText("Latitud: " + location.getLatitude());
		tvLongitud.setText("Longitud: " + location.getLongitude());
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void SendAlerta(Location location) {
		String URL = VariablesGlobales.URL_BASE_SERVICIOS + "/api/alertas";
		ServiceCaller caller = new ServiceCaller();
		/*String FechaAlerta = java.text.DateFormat.getDateTimeInstance().format(
				Calendar.getInstance().getTime());*/
		// Feb 27, 2012 5:41:23 PM

		/*
		 * Calendar calendar = new GregorianCalendar(); TimeZone timeZone =
		 * calendar.getTimeZone(); calendar.get TimeZone timeZone =
		 * TimeZone.getDefault(); TimeZone timeZone =
		 * TimeZone.getTimeZone("America/Bogota");
		 */

		// Obtenemos de la preferencia el Cod del usuario para el INSERT de
		// alerta
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
			/*
			 * parameters.add(new BasicNameValuePair("CodUsuario",
			 * sesion.get_CodUsuario().toString())); parameters.add(new
			 * BasicNameValuePair("Latitud",
			 * Double.toString(location.getLatitude()))); parameters.add(new
			 * BasicNameValuePair("Longitud",
			 * Double.toString(location.getLongitude())));
			 * 
			 * parameters.add(new BasicNameValuePair("Fecha",FechaAlerta));
			 * parameters.add(new BasicNameValuePair("HusoHorario", "-5"));
			 * parameters.add(new BasicNameValuePair("Tipo", "0"));
			 */

			parameters.add(new BasicNameValuePair("CodUsuario", sesion
					.getCodUsuario().toString()));
			parameters.add(new BasicNameValuePair("Latitud", "1111"));
			parameters.add(new BasicNameValuePair("Longitud", "9999"));
			parameters.add(new BasicNameValuePair("Fecha", " hoy "));
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
			// TODO Auto-generated method stub
			super.onPreExecute();
			// dialog =
			// ProgressDialog.show(LocationRedService.this,"Enviando alerta",
			// "Por favor espere...");

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
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			// Deserealize JSON

			try {
				int ii = new Gson().fromJson(result, int.class);
				if (ii >= 1) {

					// Toast.makeText(LocationRedService.this,"Se envió con éxito su alerta "
					// ,Toast.LENGTH_LONG).show();
					// Intent i = new
					// Intent(LocationRedService.this,ConfirmacionAlertaActivity.class);
					// i.putExtra("login",result);
					// startActivity(i);
					// PantallaInicioActivity.this.finish();

					// TODO: INSERT DE ALERTA BASE DE BD NTERNA
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.msg_excepcion, Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(getApplicationContext(),
						"No se pudo comunicar con el servidor, lo sentimos.",
						Toast.LENGTH_SHORT).show();
			}

			super.onPostExecute(result);
		}
	}

}
