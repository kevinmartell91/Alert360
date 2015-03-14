package pe.edu.upc.moviles.alert360;

import java.util.ArrayList;
import java.util.List;

import pe.edu.upc.moviles.alert360.bl.be.Receptor;
import pe.edu.upc.moviles.alert360.bl.constantes.VariablesGlobales;
import pe.edu.upc.moviles.alert360.dl.bd.MySQLiteDataSource;
import pe.edu.upc.moviles.alert360.utilitarios.FloatingActionButton;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListadoReceptoresActivity extends FragmentActivity {

	private MySQLiteDataSource dataSource;

	private ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listado_receptores);

		dataSource = new MySQLiteDataSource(this);
		dataSource.open();

		lv = (ListView) findViewById(R.id.lstReceptor);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String recuperado = (String) parent.getItemAtPosition(position);
				Intent intent = new Intent(getApplicationContext(),
						EditarReceptorActivity.class);
				VariablesGlobales.EXTRA_EDITAR_RECEPTOR = recuperado;
				intent.putExtra(VariablesGlobales.EXTRA_EDITAR_RECEPTOR,
						recuperado);
				startActivity(intent);
			}
		});
		FloatingActionButton fabButton = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.pluscuatro))
				.withButtonColor(Color.WHITE)
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();

		fabButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Toast.makeText(getApplicationContext(), "prieba",
				// Toast.LENGTH_SHORT).show();
				GoToRegistroReceptores(v);
			}
		});

		LlenarSpinner();
	}

	public void GoToRegistroReceptores(View v) {
		Intent i = new Intent(getApplicationContext(),
				RegistroReceptorActivity.class);
		startActivity(i);
	}

	private void LlenarSpinner() {
		// OFFLINE - si BD Embebida ( CONT de receptores) > BD Externa (saldria
		// de la preferencia que se actualiza al momento del login)
		try {
			List<Receptor> temporal = dataSource.ListarReceptores();
			if (temporal != null) {
				List<String> nombres = new ArrayList<String>();
				for (int i = 0; i < temporal.size(); i++) {
					nombres.add(temporal.get(i).getNombres());
				}
				// ArrayAdapter<Receptor> aa=new ArrayAdapter<Receptor>(this,
				// android.R.layout.simple_list_item_2,temporal);
				ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, nombres);

				lv.setAdapter(aa);
			}
		} catch (Exception e) {
			// String x=e.toString();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		LlenarSpinner();
		super.onResume();
	}

}
