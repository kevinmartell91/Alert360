package pe.edu.upc.moviles.alert360.dl.bd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "AlertDB";
	private static final int DATABASE_VERSION = 2;

	public static class TablaUsuario {
		public static String TABLA_USUARIO = "Usuario";
		public static String COLUMNA_IDUSUARIO = "_idusuario";
		public static String COLUMNA_NOMBRES = "_nombres";
		public static String COLUMNA_APELLIDOS = "_apellidos";
		public static String COLUMNA_CONTRASENA = "_contrasena";
		public static String COLUMNA_ACTIVO = "_activo";
	}

	public static class TablaReceptor {
		public static String TABLA_RECEPTOR = "Receptor";
		public static String COLUMNA_IDUSUARIO = "_idusuario";
		public static String COLUMNA_NOMBRE = "_nombre";
		public static String COLUMNA_CORREO = "_correo";
		public static String COLUMNA_TELEFONOUNO = "_teluno";
		public static String COLUMNA_TELEFONODOS = "_teldos";
		public static String COLUMNA_TELEFONOTRES = "_teltres";
	}

	/*
	 * private static final String DATABASE_CREATE = "create table " +
	 * TablaUsuario.TABLA_USUARIO + "(" + TablaUsuario.COLUMNA_ID +
	 * " integer primary key autoincrement, " + TablaUsuario.COLUMNA_NOMBRES +
	 * " text not null, " + TablaUsuario.COLUMNA_APELLIDOS + " text not null, "
	 * + TablaUsuario.COLUMNA_IDUSUARIO + " text not null, " +
	 * TablaUsuario.COLUMNA_CONTRASENA + " text not null, " +
	 * TablaUsuario.COLUMNA_ACTIVO + " integer not null" + ");";
	 */

	private static final String DATABASE_CREATE = "create table "
			+ TablaUsuario.TABLA_USUARIO + "(" + TablaUsuario.COLUMNA_IDUSUARIO
			+ " text primary key, " + TablaUsuario.COLUMNA_NOMBRES
			+ " text not null, " + TablaUsuario.COLUMNA_APELLIDOS
			+ " text not null, " + TablaUsuario.COLUMNA_CONTRASENA
			+ " text not null, " + TablaUsuario.COLUMNA_ACTIVO
			+ " integer not null" + ");";
	private static final String DATABASE_CREATE_RECEPTOR = "create table "
			+ TablaReceptor.TABLA_RECEPTOR + "("
			+ TablaReceptor.COLUMNA_IDUSUARIO + " text not null, "
			+ TablaReceptor.COLUMNA_NOMBRE + " text not null, "
			+ TablaReceptor.COLUMNA_CORREO + " text not null, "
			+ TablaReceptor.COLUMNA_TELEFONOUNO + " text not null, "
			+ TablaReceptor.COLUMNA_TELEFONODOS + " text not null, "
			+ TablaReceptor.COLUMNA_TELEFONOTRES + " text not null" + ");";

	public MySQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(DATABASE_CREATE);
		db.execSQL(DATABASE_CREATE_RECEPTOR);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("delete table if exists " + TablaUsuario.TABLA_USUARIO);
		db.execSQL("delete table if exists " + TablaReceptor.TABLA_RECEPTOR);
		onCreate(db);
	}
}
