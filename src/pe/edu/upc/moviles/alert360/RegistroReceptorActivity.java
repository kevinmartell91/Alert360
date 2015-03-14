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
import pe.edu.upc.moviles.alert360.dl.bd.MySQLiteDataSource;
import pe.edu.upc.moviles.alert360.bl.be.UsuarioBE;
import pe.edu.upc.moviles.alert360.bl.constantes.VariablesGlobales;
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

public class RegistroReceptorActivity extends Activity {

	private SharedPreferences prefs;
	private String sesionActiva;
	private EditText txtNombre, txtCorreo, txtTel1, txtTel2, txtTel3;
	private MySQLiteDataSource dataSource;
	private UsuarioBE sesion;
	//
	private String nombre;
	private String correo;
	private String tel1;
	private String tel2;
	private String tel3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registro_receptor);

		prefs = getSharedPreferences(VariablesGlobales.PREF_NAME,
				Context.MODE_PRIVATE);
		sesionActiva = prefs.getString(VariablesGlobales.PREF_SESION_ACTIVA,
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

		txtNombre = (EditText) findViewById(R.id.txtRegistroReceptorNombre);
		txtCorreo = (EditText) findViewById(R.id.txtRegistroReceptorCorreo);
		txtTel1 = (EditText) findViewById(R.id.txtRegistroReceptorTelUno);
		txtTel2 = (EditText) findViewById(R.id.txtRegistroReceptorTelDos);
		txtTel3 = (EditText) findViewById(R.id.txtRegistroReceptorTelTres);

		dataSource = new MySQLiteDataSource(this);
		dataSource.open();
	}

	public void RegistrarReceptor(View v) {
		nombre = txtNombre.getText().toString();
		correo = txtCorreo.getText().toString();
		tel1 = txtTel1.getText().toString();
		tel2 = txtTel2.getText().toString();
		tel3 = txtTel3.getText().toString();
		// guardar en BD Embebida
		if (nombre.length() > 0 && correo.length() > 0 && tel1.length() > 0
				&& tel2.length() > 0 && tel3.length() > 0) {
			// String URL =
			// "http://alert360upc-001-site1.smarterasp.net/api/receptores";
			String URL = VariablesGlobales.URL_BASE_SERVICIOS
					+ "/api/receptores";
			ServiceCaller caller = new ServiceCaller();
			try {
				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
				caller.setUrl(URL);
				parameters.add(new BasicNameValuePair("CodUsuario", sesion
						.getCodUsuario()));
				parameters.add(new BasicNameValuePair("Nombre", nombre));
				parameters.add(new BasicNameValuePair("CorreoElectronico",
						correo));
				parameters.add(new BasicNameValuePair("TelefonoUno", tel1));
				parameters.add(new BasicNameValuePair("TelefonoDos", tel2));
				parameters.add(new BasicNameValuePair("TelefonoTres", tel3));
				caller.setParametersList(parameters);
				new CallServiceRegistroReceptor(caller).execute();

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

	public class CallServiceRegistroReceptor extends
			AsyncTask<String, Void, String> {

		ServiceCaller caller;
		ProgressDialog dialog;

		public CallServiceRegistroReceptor(ServiceCaller caller) {
			super();
			this.caller = caller;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = ProgressDialog
					.show(RegistroReceptorActivity.this,
							getResources().getString(
									R.string.msg_registrando_receptor),
							getResources().getString(
									R.string.msg_general_espera));

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

				if (ii == -1) // si ya existe el Receptor
				{
					Toast.makeText(RegistroReceptorActivity.this,
							R.string.msg_receptores_registro_ya_existe,
							Toast.LENGTH_LONG).show();
				}
				if (ii != 0) {
					// int
					// resp=dataSource.RegistrarReceptor(sesion.getIDUsuario(),
					// nombre, correo, tel1, tel2, tel3);
					dataSource.RegistrarReceptor(sesion.getIDUsuario(), nombre,
							correo, tel1, tel2, tel3);
					Toast.makeText(RegistroReceptorActivity.this,
							R.string.msg_receptores_registro_existoso,
							Toast.LENGTH_LONG).show();
					Intent i = new Intent(RegistroReceptorActivity.this,
							PantallaInicioActivity.class);
					i.putExtra("login", result);

					// TODO: Guardar en BD Embebida el codigo del receptor
					// generado

					startActivity(i);
					finish();
				} else {
					Toast.makeText(
							RegistroReceptorActivity.this,
							R.string.msg_receptores_usuario_actualizar_no_exite,
							Toast.LENGTH_LONG).show();
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
