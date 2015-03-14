package pe.edu.upc.moviles.alert360;

import pe.edu.upc.moviles.alert360.bl.constantes.VariablesGlobales;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EditarMensajeActivity extends Activity {
	private EditText txtMiMensaje;
	private SharedPreferences msgPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editar_mensaje);

		msgPref = getSharedPreferences(VariablesGlobales.PREF_MI_MENSAJE,
				Context.MODE_PRIVATE);
		txtMiMensaje = (EditText) findViewById(R.id.txt_mensaje_editar);
		txtMiMensaje.setText(msgPref.getString(
				VariablesGlobales.PREF_MI_MENSAJE,
				VariablesGlobales.PREF_DEFAULT_MI_MENSAJE));
	}

	public void GuardarNuevoMensaje(View v) {
		if (txtMiMensaje.length() > 0) {
			String nuevoMensaje = txtMiMensaje.getText().toString();
			msgPref.edit()
					.putString(VariablesGlobales.PREF_MI_MENSAJE, nuevoMensaje)
					.apply();
			msgPref.edit().commit();
			Toast.makeText(
					getApplicationContext(),
					getResources().getString(
							R.string.msg_mensaje_confirmacion_guardado),
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.msg_campos_vacios),
					Toast.LENGTH_SHORT).show();
		}
	}
}
