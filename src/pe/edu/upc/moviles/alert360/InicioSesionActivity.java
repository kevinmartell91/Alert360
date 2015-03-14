package pe.edu.upc.moviles.alert360;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import pe.edu.upc.moviles.alert360.bgservices.AcelerometroService;
import pe.edu.upc.moviles.alert360.bl.be.UsuarioBE;
import pe.edu.upc.moviles.alert360.bl.be.UsuarioBackEnd;
import pe.edu.upc.moviles.alert360.bl.constantes.VariablesGlobales;
import pe.edu.upc.moviles.alert360.dl.bd.MySQLiteDataSource;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class InicioSesionActivity extends Activity {

	private SharedPreferences prefs;
	private String sesionActiva;
	private MySQLiteDataSource dataSource;
	private EditText txtUsuario;
	private EditText txtContrasena;
	@SuppressWarnings("unused")
	private boolean errorLogin;
	@SuppressWarnings("unused")
	private UsuarioBE sesion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_inicio_sesion);

		prefs = getSharedPreferences(VariablesGlobales.PREF_NAME,
				Context.MODE_PRIVATE);

		dataSource = new MySQLiteDataSource(this);
		dataSource.open();

		txtUsuario = (EditText) findViewById(R.id.txtUsuarioLogin);
		txtContrasena = (EditText) findViewById(R.id.txtContrasenaLogin);

		errorLogin = false;
		ComprobarSesionActiva();

	}

	private void IniciarAcelerometroYGoToInicio() {
		Intent i = new Intent(InicioSesionActivity.this,
				PantallaInicioActivity.class);
		// i.putExtra("NombreLogin",usuarioLogin);
		startActivity(i);
		finish();
	}

	private void ComprobarSesionActiva() {
		sesionActiva = prefs.getString(VariablesGlobales.PREF_SESION_ACTIVA,
				VariablesGlobales.PREF_DEFAULT_SESION_ACTIVA);

		if (!sesionActiva.equals(VariablesGlobales.PREF_DEFAULT_SESION_ACTIVA)) {
			startService(new Intent(this, AcelerometroService.class));
			Intent i = new Intent(this, PantallaInicioActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
		}
	}

	public void clickIniciarSesion(View v) {
		String IDLogin = txtUsuario.getText().toString();
		String PassLogin = txtContrasena.getText().toString();

		String URL = VariablesGlobales.URL_BASE_SERVICIOS + "/api/usuarios";

		if (IDLogin.length() > 0 && PassLogin.length() > 0) {
			ServiceCaller caller = new ServiceCaller();

			try {

				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				caller.setUrl(URL);
				parameters.add(new BasicNameValuePair("Nombres", "login"));
				
				parameters.add(new BasicNameValuePair("IDUsuario", IDLogin));
				parameters.add(new BasicNameValuePair("Contrasena", PassLogin));
				caller.setParametersList(parameters);
				new CallServiceLogin(caller).execute();

			} catch (Exception e1) {

				System.out.println(e1);
				e1.printStackTrace();

			}
		} else {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.msg_campos_vacios),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void IrARegistro(View v) {
		Intent i = new Intent(this, RegistroUsuarioActivity.class);
		startActivity(i);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		dataSource.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		dataSource.open();
		super.onResume();
	}

	public class CallServiceLogin extends AsyncTask<String, Void, String> {

		ServiceCaller caller;
		ProgressDialog dialog;

		public CallServiceLogin(ServiceCaller caller) {
			super();
			this.caller = caller;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = ProgressDialog.show(InicioSesionActivity.this,
					"Iniciando Sesión", "Por favor espere...");
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

			if (result.length() <= 3)
			{
				errorLogin = true;
				try {
					int ii = new Gson().fromJson(result, int.class);

					switch (ii) {
					case 0:
						Toast.makeText(InicioSesionActivity.this,
								R.string.msg_usuario_contrasena_incorrecta,
								Toast.LENGTH_LONG).show();
						break;
					case -1:
						Toast.makeText(InicioSesionActivity.this,
								R.string.msg_usuario_ya_loguado,
								Toast.LENGTH_LONG).show();
						break;
					case -3:
						Toast.makeText(InicioSesionActivity.this,
								R.string.msg_usuario_no_existe,
								Toast.LENGTH_LONG).show();
						break;
					}
				} catch (Exception e) {
					// TODO: handle exception
					errorLogin = false;

					Toast.makeText(
							getApplicationContext(),
							"No se pudo comunicar con el servidor, lo sentimos.",
							Toast.LENGTH_SHORT).show();
				}
			} else
			{
				UsuarioGuardarEnPreferencia(result.toString());
			}
			super.onPostExecute(result);
		}

		private void UsuarioGuardarEnPreferencia(String result) {
			String aGuardar = "";
			ObjectMapper mapper = new ObjectMapper();
			UsuarioBackEnd resultUsuario;

			UsuarioBE usuarioToPref = new UsuarioBE();

			try {

				resultUsuario = new Gson().fromJson((String) result,
						UsuarioBackEnd.class);

				usuarioToPref.setCodUsuario(resultUsuario.getCodUsuario());
				usuarioToPref.setNombres(resultUsuario.getNombres());
				usuarioToPref.setApellidos(resultUsuario.getApellidos());
				usuarioToPref.setIDUsuario(resultUsuario.getIDUsuario());
				usuarioToPref.setContrasena(resultUsuario.getContrasena());
				usuarioToPref.setActivo(resultUsuario.getActivo());
				usuarioToPref.setAlertas(resultUsuario.getAlertas());
				usuarioToPref.setReceptors(resultUsuario.getReceptors());

				usuarioToPref.setMensaje(VariablesGlobales.DEFAULT_MI_MENSAJE);
				usuarioToPref
						.setAlarmaInmediata(VariablesGlobales.DEFAULT_ALARMA_INMEDIATA);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				aGuardar = mapper.writeValueAsString(usuarioToPref); 
				IniciarAcelerometroYGoToInicio();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			prefs.edit()
					.putString(VariablesGlobales.PREF_SESION_ACTIVA, aGuardar)
					.apply();
			prefs.edit().commit();
		}
	}
}
