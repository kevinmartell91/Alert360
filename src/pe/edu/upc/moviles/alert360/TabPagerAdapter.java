package pe.edu.upc.moviles.alert360;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabPagerAdapter extends FragmentStatePagerAdapter {
	public TabPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int i) {
		/*
		 * switch (i) { case 0: //Fragement for Android Tab return new
		 * Android(); case 1: //Fragment for Ios Tab return new Ios(); case 2:
		 * //Fragment for Windows Tab return new Windows(); } return null;
		 */
		switch (i) {
		case 0:
			return new GuiaCero();
		case 1:
			return new GuiaUno();
		case 2:
			return new GuiaDos();
		case 3:
			return new GuiaTres();
		case 4:
			return new GuiaCuatro();
		}
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 5; // No of Tabs OV 3
	}

}