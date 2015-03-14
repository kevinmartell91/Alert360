package pe.edu.upc.moviles.alert360;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GuiaCero extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View guiaCero = inflater.inflate(R.layout.guia_cero_frag, container,
				false);
		return guiaCero;
	}
}
