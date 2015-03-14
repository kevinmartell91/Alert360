package pe.edu.upc.moviles.alert360;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditarUsuarioActivity extends Activity {

	private SharedPreferences prefs;
	private UsuarioBE sesion;
	Button btnActualizar;
	EditText txtNombre, txtApellido, txtUsuario, txtContrasena;

	private MySQLiteDataSource dataSource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_editar_usuario);

		prefs = getSharedPreferences(VariablesGlobales.PREF_NAME,
				Context.MODE_PRIVATE);

		btnActualizar = (Button) findViewById(R.id.btnActualizarUsuario);
		txtNombre = (EditText) findViewById(R.id.txtEditarNombres);
		txtApellido = (EditText) findViewById(R.id.txtEditarApellidos);
		txtUsuario = (EditText) findViewById(R.id.txtEditarUsuario);
		txtContrasena = (EditText) findViewById(R.id.txtEditarContrasena);
		btnActualizar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ActualizarDatos();
			}
		});

		dataSource = new MySQLiteDataSource(this);
		dataSource.open();

		String sesionActiva = prefs.getString(
				VariablesGlobales.PREF_SESION_ACTIVA,
				VariablesGlobales.PREF_DEFAULT_SESION_ACTIVA);
		ObjectMapper mapper = new ObjectMapper();

		try {
			sesion = mapper.readValue(sesionActiva, UsuarioBE.class);

			txtNombre.setText(sesion.getNombres());
			txtApellido.setText(sesion.getApellidos());
			txtUsuario.setText(sesion.getIDUsuario());
			txtContrasena.setText(sesion.getContrasena());

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
	}

	private void ActualizarDatos() {
		if (txtNombre.getText().length() > 0
				&& txtApellido.getText().length() > 0
				&& txtContrasena.getText().length() > 0) {
			// Actualizacion de preferencias
			// TODO: FALTARIA MODO OFFLINE
			sesion.setNombres(txtNombre.getText().toString());
			sesion.setApellidos(txtApellido.getText().toString());
			sesion.setContrasena(txtContrasena.getText().toString());

			// Actulizacion en Base de datos externa
			// String URL =
			// "http://alert360upc-001-site1.smarterasp.net/api/usuarios";
			String URL = VariablesGlobales.URL_BASE_SERVICIOS + "/api/usuarios";

			ServiceCaller caller = new ServiceCaller();

			try {

				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				caller.setUrl(URL);

				parameters.add(new BasicNameValuePair("Nombres", txtNombre
						.getText().toString()));
				parameters.add(new BasicNameValuePair("IDUsuario", sesion
						.getIDUsuario().toString()));
				parameters.add(new BasicNameValuePair("Apellidos", txtApellido
						.getText().toString()));
				parameters.add(new BasicNameValuePair("Contrasena",
						txtContrasena.getText().toString()));

				caller.setParametersList(parameters);
				new CallServiceActualizarUsuario(caller).execute();

			} catch (Exception e1) {

				System.out.println(e1);
				e1.printStackTrace();
			}

		} else {
			Toast.makeText(getApplicationContext(), R.string.msg_campos_vacios,
					Toast.LENGTH_SHORT).show();
		}
	}

	public class CallServiceActualizarUsuario extends
			AsyncTask<String, Void, String> {

		ServiceCaller caller;
		ProgressDialog dialog;

		public CallServiceActualizarUsuario(ServiceCaller caller) {
			super();
			this.caller = caller;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = ProgressDialog.show(
					EditarUsuarioActivity.this,
					getResources().getString(
							R.string.label_actualizando_usuario),
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
			// TODO Auto-generated method stub
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			// Deserealize JSON

			try {
				int ii = new Gson().fromJson(result, int.class);

				if (ii == 2) // msg_usuario_actualizacion_datos_existosa
				{
					// Actualizacion de la preferncia

					sesion.setIDUsuario(txtUsuario.getText().toString());
					sesion.setActivo(sesion.getActivo());
					sesion.setAlertas(sesion.getAlertas());
					sesion.setReceptors(sesion.getReceptors());
					sesion.setMensaje(VariablesGlobales.DEFAULT_MI_MENSAJE);
					sesion.setAlarmaInmediata(VariablesGlobales.DEFAULT_ALARMA_INMEDIATA);

					ObjectMapper mapper = new ObjectMapper();
					String aGuardar = "";

					try {
						aGuardar = mapper.writeValueAsString(sesion);
					} catch (JsonProcessingException e) {

						e.printStackTrace();
					}
					prefs.edit()
							.putString(VariablesGlobales.PREF_SESION_ACTIVA,
									aGuardar).apply();
					prefs.edit().commit();

					Toast.makeText(EditarUsuarioActivity.this,
							R.string.msg_usuario_actualizacion_datos_existosa,
							Toast.LENGTH_LONG).show();
					Intent i = new Intent(EditarUsuarioActivity.this,
							PantallaInicioActivity.class);
					i.putExtra("login", result);
					startActivity(i);
					EditarUsuarioActivity.this.finish();
				}
				/*
				 * else // me parece que nunca entraria {
				 * Toast.makeText(getApplicationContext
				 * (),R.string.msg_excepcion, Toast.LENGTH_SHORT).show(); }
				 */

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
