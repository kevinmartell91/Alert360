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

import pe.edu.upc.moviles.alert360.bl.be.Receptor;
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

public class EditarReceptorActivity extends Activity {

	EditText txtNombre, txtCorreo, txtTel1, txtTel2, txtTel3;
	Button btnActualizar, btnEliminar;

	private MySQLiteDataSource datasource;

	private String receptorNombre = "";
	private SharedPreferences prefs;
	private UsuarioBE sesion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editar_receptor);

		prefs = getSharedPreferences(VariablesGlobales.PREF_NAME,
				Context.MODE_PRIVATE);

		Intent i = getIntent();
		Bundle b = i.getExtras();
		if (b != null) {
			// TextView
			// tv=(TextView)findViewById(R.id.lbl_receptor_editar_titulo);
			receptorNombre = b
					.getString(VariablesGlobales.EXTRA_EDITAR_RECEPTOR);
		}

		txtNombre = (EditText) findViewById(R.id.txtEditarNombre);
		txtCorreo = (EditText) findViewById(R.id.txtEditarCorreo);
		txtTel1 = (EditText) findViewById(R.id.txtEditarTelefono1);
		txtTel2 = (EditText) findViewById(R.id.txtEditarTelefono2);
		txtTel3 = (EditText) findViewById(R.id.txtEditarTelefono3);
		btnActualizar = (Button) findViewById(R.id.btnActualizarReceptor);
		btnEliminar = (Button) findViewById(R.id.btnEliminarReceptor);

		btnActualizar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ActualizarReceptor();
			}

		});

		btnEliminar.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EliminarReceptor();
			}
		});

		CargarDatosCampos();
		CargarSesionPreferencias();
	}

	private void CargarSesionPreferencias() {
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
	}

	private void CargarDatosCampos() {
		// TODO Auto-generated method stub
		try {

			datasource = new MySQLiteDataSource(this);
			datasource.open();

			List<Receptor> receptores = datasource
					.ListarReceptores(receptorNombre);
			txtNombre.setText(receptores.get(0).getNombres());
			txtCorreo.setText(receptores.get(0).getCorreo());
			txtTel1.setText(receptores.get(0).getTel1());
			txtTel2.setText(receptores.get(0).getTel2());
			txtTel3.setText(receptores.get(0).getTel3());
		} catch (Exception e) {
			// TODO: handle exception
			// String ex = e.toString();
		}
	}

	protected void ActualizarReceptor() {
		// TODO Auto-generated method stub

		Receptor receporActilizar = CargarReceptor();
		datasource.ActualizarReceptor(receporActilizar);
		// datasource.close();
		// llamamos al servicio
		ReceptorService(receporActilizar, "actualizar");
	}

	protected void EliminarReceptor() {
		// TODO Auto-generated method stub

		Receptor receporEliminar = CargarReceptor();
		datasource.BorrarReceptor(receporEliminar);
		// datasource.close();
		// llamamos al servicio
		ReceptorService(receporEliminar, "eliminar");
	}

	private Receptor CargarReceptor() {
		// TODO Auto-generated method stub
		Receptor r = new Receptor();
		r.setIDUsuario(sesion.getIDUsuario().toString());
		r.setNombres(txtNombre.getText().toString());
		r.setCorreo(txtCorreo.getText().toString());
		r.setTel1(txtTel1.getText().toString());
		r.setTel2(txtTel2.getText().toString());
		r.setTel3(txtTel3.getText().toString());

		return r;
	}

	private void ReceptorService(Receptor receporActilizar, String opcion) {

		ServiceCaller caller = new ServiceCaller();
		String URL = "";

		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			// caller.setUrl(URL);

			parameters.add(new BasicNameValuePair("CodReceptor", "999")); // no
																			// se
																			// usa
																			// por
																			// el
																			// momento
			parameters.add(new BasicNameValuePair("CodUsuario", sesion
					.getCodUsuario().toString())); // no se usa por el momento
			parameters.add(new BasicNameValuePair("Nombre",
					VariablesGlobales.EXTRA_EDITAR_RECEPTOR));
			parameters.add(new BasicNameValuePair("CorreoElectronico",
					txtCorreo.getText().toString()));
			parameters.add(new BasicNameValuePair("TelefonoUno", txtTel1
					.getText().toString()));
			parameters.add(new BasicNameValuePair("TelefonoDos", txtTel2
					.getText().toString()));
			parameters.add(new BasicNameValuePair("TelefonoTres", txtTel3
					.getText().toString()));

			caller.setParametersList(parameters);
			if (opcion.equals("actualizar")) {
				URL = VariablesGlobales.URL_BASE_SERVICIOS + "/api/receptores";
				caller.setUrl(URL);
				new CallServiceActualizarReceptor(caller).execute();
			}
			if (opcion.equals("eliminar")) {
				URL = VariablesGlobales.URL_BASE_SERVICIOS
						+ "/api/receptores?nombre="
						+ VariablesGlobales.EXTRA_EDITAR_RECEPTOR;
				caller.setUrl(URL);
				new CallServiceEliminarReceptor(caller).execute();
			}

		} catch (Exception e1) {

			System.out.println(e1);
			e1.printStackTrace();
		}

	}

	public class CallServiceActualizarReceptor extends
			AsyncTask<String, Void, String> {

		ServiceCaller caller;
		ProgressDialog dialog;

		public CallServiceActualizarReceptor(ServiceCaller caller) {
			super();
			this.caller = caller;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = ProgressDialog.show(EditarReceptorActivity.this,
					getResources()
							.getString(R.string.msg_actualizando_receptor),
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

				if (ii == 1) // msg_receptor_actualizacion_datos_existosa
				{

					Toast.makeText(EditarReceptorActivity.this,
							R.string.msg_receptores_actualizacion_existosa,
							Toast.LENGTH_LONG).show();
					/*Intent i = new Intent(EditarReceptorActivity.this,
							ListadoReceptoresActivity.class);
					i.putExtra("login", result);
					startActivity(i);*/
					EditarReceptorActivity.this.finish();
				}

				else // me parece que nunca entraria
				{
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

	public class CallServiceEliminarReceptor extends
			AsyncTask<String, Void, String> {

		ServiceCaller caller;
		ProgressDialog dialog;

		public CallServiceEliminarReceptor(ServiceCaller caller) {
			super();
			this.caller = caller;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = ProgressDialog.show(EditarReceptorActivity.this,
					getResources().getString(R.string.msg_eliminando_receptor),
					getResources().getString(R.string.msg_general_espera));
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String response = "";
			response = RESTClient.connectAndReturnResponseDelete(
					caller.getUrl(), caller.getParametersList());
			return response;
		}

		@Override
		protected void onPostExecute(String result) {

			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			try {
				int ii = new Gson().fromJson(result, int.class);

				if (ii == 1) // msg_receptor_elminado_datos_existosa
				{

					Toast.makeText(EditarReceptorActivity.this,
							R.string.msg_receptores_eliminacion_existosa,
							Toast.LENGTH_LONG).show();
					/*Intent i = new Intent(EditarReceptorActivity.this,
							ListadoReceptoresActivity.class);
					i.putExtra("login", result);
					startActivity(i);*/
					EditarReceptorActivity.this.finish();
				}

				else // me parece que nunca entraria
				{
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
}
