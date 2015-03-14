package pe.edu.upc.moviles.alert360;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import pe.edu.upc.moviles.alert360.bl.be.UsuarioBE;
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

public class RegistroUsuarioActivity extends Activity {

	private SharedPreferences prefs;
	// private String sesionActiva;
	private MySQLiteDataSource dataSource;

	private EditText txtNombres;
	private EditText txtApellidos;
	private EditText txtUsuario;
	private EditText txtContrasena;

	private UsuarioBE nuevoUsuario;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registro_usuario);

		prefs = getSharedPreferences(VariablesGlobales.PREF_NAME,
				Context.MODE_PRIVATE);
		/*
		 * sesionActiva = prefs.getString(VariablesGlobales.PREF_SESION_ACTIVA,
		 * VariablesGlobales.PREF_DEFAULT_SESION_ACTIVA);
		 */

		dataSource = new MySQLiteDataSource(this);
		dataSource.open();

		txtNombres = (EditText) findViewById(R.id.txtRegistroNombres);
		txtApellidos = (EditText) findViewById(R.id.txtRegistroApellidos);
		txtUsuario = (EditText) findViewById(R.id.txtUsuario);
		txtContrasena = (EditText) findViewById(R.id.txtContrasena);
	}

	public void RegistrarUsuario(View v) {
		String nombres = txtNombres.getText().toString();
		String apellidos = txtApellidos.getText().toString();
		String idusuario = txtUsuario.getText().toString();
		String contrasena = txtContrasena.getText().toString();

		if (nombres.length() > 0 && apellidos.length() > 0
				&& idusuario.length() > 0 && contrasena.length() > 0) {
			// String URL =
			// "http://alert360upc-001-site1.smarterasp.net/api/usuarios";
			String URL = VariablesGlobales.URL_BASE_SERVICIOS + "/api/usuarios";
			ServiceCaller caller = new ServiceCaller();

			try {
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				caller.setUrl(URL);

				parameters.add(new BasicNameValuePair("Nombres", nombres));
				parameters.add(new BasicNameValuePair("Apellidos", apellidos));
				parameters.add(new BasicNameValuePair("IDUsuario", idusuario));
				parameters
						.add(new BasicNameValuePair("Contrasena", contrasena));
				parameters.add(new BasicNameValuePair("Activo", "0"));

				caller.setParametersList(parameters);
				new CallServiceRegistroUsuario(caller).execute();
			} catch (Exception e1) {

				System.out.println(e1);
				e1.printStackTrace();

			}

			nuevoUsuario = new UsuarioBE();
			nuevoUsuario.setNombres(txtNombres.getText().toString());
			nuevoUsuario.setApellidos(txtApellidos.getText().toString());
			nuevoUsuario.setIDUsuario(txtUsuario.getText().toString());
			nuevoUsuario.setContrasena(txtContrasena.getText().toString());
			nuevoUsuario.setActivo("1");
			nuevoUsuario.setAlertas("0");
			nuevoUsuario.setReceptors("0");
			nuevoUsuario.setMensaje(VariablesGlobales.DEFAULT_MI_MENSAJE);
			nuevoUsuario
					.setAlarmaInmediata(VariablesGlobales.DEFAULT_ALARMA_INMEDIATA);
		} else {
			Toast.makeText(getApplicationContext(),
					R.string.msg_solicitar_campos_completos, Toast.LENGTH_SHORT)
					.show();
		}
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

	public class CallServiceRegistroUsuario extends
			AsyncTask<String, Void, String> {

		ServiceCaller caller;
		ProgressDialog dialog;

		public CallServiceRegistroUsuario(ServiceCaller caller) {
			super();
			this.caller = caller;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			/*
			 * dialog = ProgressDialog.show(RegistroUsuarioActivity.this,
			 * "Creando su usuario", "Por favor espere...");
			 */
			dialog = ProgressDialog.show(RegistroUsuarioActivity.this,
					getResources().getString(R.string.msg_usuario_registrando),
					getResources().getString(R.string.msg_general_espera));
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

				if (ii == -2) // msg_usuario_ya_existe
				{
					Toast.makeText(RegistroUsuarioActivity.this,
							R.string.msg_usuario_ya_existe, Toast.LENGTH_LONG)
							.show();
				} else {
					if (ii >= 1)// msg_usuario_registro_existoso
					{
						// Alcenamos en Preferencias al usuario registrado
						nuevoUsuario.setCodUsuario(Integer.toString(ii));

						ObjectMapper mapper = new ObjectMapper();
						String aGuardar = "";

						try {
							aGuardar = mapper.writeValueAsString(nuevoUsuario);

						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}

						prefs.edit()
								.putString(
										VariablesGlobales.PREF_SESION_ACTIVA,
										aGuardar).apply();
						prefs.edit().commit();
						// String
						// resp=prefs.getString(VariablesGlobales.PREF_SESION_ACTIVA,
						// VariablesGlobales.PREF_DEFAULT_SESION_ACTIVA);

						// Toast.makeText(RegistroUsuarioActivity.this,R.string.msg_usuario_registro_existoso
						// ,Toast.LENGTH_LONG).show();
						Intent i = new Intent(RegistroUsuarioActivity.this,
								ConfirmacionRegistroActivity.class);
						i.putExtra("login", result);
						startActivity(i);
						finish();
					}
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
}
