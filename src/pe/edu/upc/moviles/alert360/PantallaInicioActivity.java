package pe.edu.upc.moviles.alert360;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import pe.edu.upc.moviles.alert360.bgservices.AcelerometroService;
import pe.edu.upc.moviles.alert360.bl.be.Receptor;
import pe.edu.upc.moviles.alert360.bl.be.ReceptorBackEnd;
import pe.edu.upc.moviles.alert360.bl.be.UsuarioBE;
import pe.edu.upc.moviles.alert360.bl.constantes.VariablesGlobales;
import pe.edu.upc.moviles.alert360.dl.bd.MySQLiteDataSource;
import android.app.Activity;
import android.app.ProgressDialog;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class PantallaInicioActivity extends Activity {

	private SharedPreferences prefs, acelpref, cargarDatapref;
	private String sesionActiva;
	private UsuarioBE sesion;
	private Switch swAcel;
	private MySQLiteDataSource dataSource;

	private void CargarPreferencias()
	{
		prefs = getSharedPreferences(VariablesGlobales.PREF_NAME,
				Context.MODE_PRIVATE);

		acelpref = getSharedPreferences(
				VariablesGlobales.PREF_INICIAR_ACELEROMETRO,
				Context.MODE_PRIVATE);
		
		cargarDatapref=getSharedPreferences(
				VariablesGlobales.PREF_RUTA_PRIMERA_CARGA, 
				Context.MODE_PRIVATE);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pantalla_inicio);

		if(dataSource==null)
		{
			dataSource = new MySQLiteDataSource(this);
		}
		dataSource.open();
		
		CargarPreferencias();
		
		boolean onSwitch = acelpref.getBoolean(
				VariablesGlobales.PREF_INICIAR_ACELEROMETRO,
				VariablesGlobales.PREF_DEFAULT_INICIAR_ACELEROMENTRO);

		swAcel = (Switch) findViewById(R.id.switchAcelerometro);
		swAcel.setChecked(onSwitch);

		// Iniciar acelerometro si el switch esta activo
		if (onSwitch) {
			startService(new Intent(this, AcelerometroService.class));
		}

		//ImageButton ibEnviarAlerta = (ImageButton) findViewById(R.id.imgBtnEnviarAlerta);
		/*ibEnviarAlerta.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EnviarAlerta(v);
			}
		});*/

		swAcel.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				CambiarEstadoSwitch();
			}
		});

		

		sesionActiva = prefs.getString(VariablesGlobales.PREF_SESION_ACTIVA,
				VariablesGlobales.PREF_DEFAULT_SESION_ACTIVA);
		ObjectMapper mapper = new ObjectMapper();

		try {
			sesion = mapper.readValue(sesionActiva, UsuarioBE.class);
			if (sesion != null) {
				TextView txtBienvenida = (TextView) findViewById(R.id.lbl_bienvenida_nombre);
				txtBienvenida.setText(
						getResources().getString(R.string.msg_usuario_login_existoso) 
						+ " " + sesion.getNombres()
						+ " " + sesion.getApellidos());
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.msg_error_carga_sesion), 
						Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this, InicioSesionActivity.class));
				finish();
			}

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
		
		VerificarReceptores();
	}

	private void VerificarReceptores()
	{
		boolean yaCargoDatos= cargarDatapref.getBoolean(
				VariablesGlobales.PREF_RUTA_PRIMERA_CARGA,
				VariablesGlobales.PREF_PRIMERA_CARGA_DATOS);
		
		if (!sesion.getReceptors().equals("0")
				&& yaCargoDatos) {
			cargarReceptoresServer();
			cargarDatapref.edit().putBoolean(VariablesGlobales.PREF_RUTA_PRIMERA_CARGA, false).apply();
			cargarDatapref.edit().commit();
		}
	}
	private void CambiarEstadoSwitch() {
		if (swAcel.isChecked()) {
			startService(new Intent(this, AcelerometroService.class));
			acelpref.edit()
					.putBoolean(VariablesGlobales.PREF_INICIAR_ACELEROMETRO,
							true).apply();
			acelpref.edit().commit();
			Toast.makeText(this, 
					getResources().getString(R.string.acel_iniciado), 
					Toast.LENGTH_SHORT).show();
		} else {
			stopService(new Intent(this, AcelerometroService.class));
			acelpref.edit()
					.putBoolean(
							VariablesGlobales.PREF_INICIAR_ACELEROMETRO,
							VariablesGlobales.PREF_DEFAULT_INICIAR_ACELEROMENTRO)
					.apply();
			acelpref.edit().commit();
			Toast.makeText(this, 
					getResources().getString(R.string.acel_terminado), Toast.LENGTH_SHORT).show();
		}
	}

	private void cargarReceptoresServer() {
		ServiceCaller caller = new ServiceCaller();
		String URL = VariablesGlobales.URL_BASE_SERVICIOS
				+ "/api/receptores?CodUsuario=" + sesion.getCodUsuario();
		caller.setUrl(URL);
		new CallServiceObtenerReceptores(caller).execute();
	}

	public void EnviarAlerta(View v) {

		List<Receptor> contactos = dataSource.ListarReceptores();
		if(contactos.size()>0)
		{
			Intent i = new Intent(getApplicationContext(),
					ConfirmacionAlertaActivity.class);
			startActivity(i);
		}
		else
		{
			Toast.makeText(getApplicationContext(), 
					getResources().getString(R.string.msg_contactos_vacio), 
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.pantalla_inicio, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.lbl_guia_inicio:
			i = new Intent(this, GuiaInicioActivity.class);
			startActivity(i);
			break;
		case R.id.lbl_acerca_de:
			i = new Intent(this, AcercaDeActivity.class);
			startActivity(i);
			break;

		case R.id.lbl_cerrar_sesion:
			CerrarSesionService();
			return true;

		case R.id.lbl_info_usuario:
			i = new Intent(this, EditarUsuarioActivity.class);
			startActivity(i);
			break;

		case R.id.lbl_destinatarios_admin:
			i = new Intent(this, ListadoReceptoresActivity.class);
			startActivity(i);
			break;

		case R.id.lbl_custom_mensaje:
			i = new Intent(this, EditarMensajeActivity.class);
			startActivity(i);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	private void CerrarSesionService() {
		
		String URL = VariablesGlobales.URL_BASE_SERVICIOS + "/api/usuarios";
		ServiceCaller caller = new ServiceCaller();
		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			caller.setUrl(URL);

			parameters.add(new BasicNameValuePair("Nombres", "signout"));
			parameters.add(new BasicNameValuePair("IDUsuario", sesion
					.getIDUsuario().toString()));

			caller.setParametersList(parameters);
			new CallServiceSignout(caller).execute();

		} catch (Exception e1) {
			System.out.println(e1);
			e1.printStackTrace();
		}
	}

	private void CerrarSesionPreferencias() {
		stopService(new Intent(this, AcelerometroService.class));
		prefs.edit().putString(
				VariablesGlobales.PREF_SESION_ACTIVA,
				VariablesGlobales.PREF_DEFAULT_SESION_ACTIVA).apply();
		prefs.edit().commit();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		dataSource.close();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.onCreate(null);
	}

	public class CallServiceSignout extends AsyncTask<String, Void, String> {

		ServiceCaller caller;
		ProgressDialog dialog;

		public CallServiceSignout(ServiceCaller caller) {
			super();
			this.caller = caller;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = ProgressDialog.show(PantallaInicioActivity.this,
					getResources().getString(R.string.msg_cerrando_sesion),
					getResources().getString(R.string.msg_general_espera));
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String response = "";
			response = RESTClient.connectAndReturnResponsePut(caller.getUrl(),
					caller.getParametersList());
			return response;
		}

		@Override
		protected void onPostExecute(String result) {

			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			try {
				int ii = new Gson().fromJson(result, int.class);
				if (ii == 1) {

					CerrarSesionPreferencias();
					LimpiarSQLite();
					dataSource.close();
					cargarDatapref.edit().putBoolean(
							VariablesGlobales.PREF_RUTA_PRIMERA_CARGA, 
							VariablesGlobales.PREF_PRIMERA_CARGA_DATOS).apply();
					cargarDatapref.edit().commit();
					
					Intent i = new Intent(PantallaInicioActivity.this,
							InicioSesionActivity.class);
					i.putExtra("login", result);
					startActivity(i);
					finish();
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.msg_excepcion, Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {

				Toast.makeText(
						getApplicationContext(),
						getResources().getString(
								R.string.msg_falla_comunicacion),
						Toast.LENGTH_SHORT).show();
			}

			super.onPostExecute(result);
		}

		private void LimpiarSQLite() {
			dataSource.DepurarTablas();

		}
	}

	public class CallServiceObtenerReceptores extends
			AsyncTask<String, Void, String> {

		ServiceCaller caller;
		ProgressDialog dialog;

		public CallServiceObtenerReceptores(ServiceCaller caller) {
			super();
			this.caller = caller;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = ProgressDialog.show(PantallaInicioActivity.this,
					getResources().getString(R.string.msg_cargando_data),
					getResources().getString(R.string.msg_general_espera));

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String response = "";
			response = RESTClient.connectAndReturnResponseGet(caller.getUrl());
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			try {
				if (result.length() > 1) {
					UsuarioGuardarReceptoresSQLite(result.toString());
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.msg_excepcion, Toast.LENGTH_SHORT).show();
				}

			} catch (Exception e) {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.msg_falla_comunicacion),
						Toast.LENGTH_SHORT).show();
			}

			super.onPostExecute(result);
		}

		private void UsuarioGuardarReceptoresSQLite(String result) {

			try {
				java.lang.reflect.Type listType = (java.lang.reflect.Type) new TypeToken<ArrayList<ReceptorBackEnd>>() {
				}.getType();
				ArrayList<ReceptorBackEnd> listaReceptoresServer = new Gson()
						.fromJson((String) result,
								(java.lang.reflect.Type) listType);

				for (ReceptorBackEnd receptor : listaReceptoresServer) {

					dataSource.RegistrarReceptor(sesion.getIDUsuario()
							.toString(), receptor.getNombre(), receptor
							.getCorreoElectronico(), receptor.getTelefonoUno(),
							receptor.getTelefonoDos(), receptor.getTelefonoTres());
				}
				//dataSource.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
