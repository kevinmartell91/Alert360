package pe.edu.upc.moviles.alert360.dl.bd;

import java.util.ArrayList;
import java.util.List;

import pe.edu.upc.moviles.alert360.bl.be.Receptor;
import pe.edu.upc.moviles.alert360.bl.be.Usuario;
import pe.edu.upc.moviles.alert360.bl.constantes.VariablesGlobales;
import pe.edu.upc.moviles.alert360.dl.bd.MySQLiteOpenHelper.TablaReceptor;
import pe.edu.upc.moviles.alert360.dl.bd.MySQLiteOpenHelper.TablaUsuario;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MySQLiteDataSource {
	private SQLiteDatabase db;
	private MySQLiteOpenHelper dbhelper;
	private String[] columnas = { TablaUsuario.COLUMNA_IDUSUARIO,
			TablaUsuario.COLUMNA_NOMBRES, TablaUsuario.COLUMNA_APELLIDOS,
			TablaUsuario.COLUMNA_CONTRASENA, TablaUsuario.COLUMNA_ACTIVO };

	/*private String[] columnasReceptor = { TablaReceptor.COLUMNA_IDUSUARIO,
			TablaReceptor.COLUMNA_CORREO, TablaReceptor.COLUMNA_TELEFONOUNO,
			TablaReceptor.COLUMNA_TELEFONODOS,
			TablaReceptor.COLUMNA_TELEFONOTRES };*/

	public MySQLiteDataSource(Context context) {
		dbhelper = new MySQLiteOpenHelper(context);
	}

	public void open() {
		db = dbhelper.getWritableDatabase();
	}

	public void close() {
		dbhelper.close();
	}

	public int RegistrarUsuario(String nombres, String apellidos,
			String idusuario, String contrasena) {
		List<Usuario> lstUsuarios = ListarUsuarios();
		if (lstUsuarios.size() > 0) {
			for (int i = 0; i < lstUsuarios.size(); i++) {
				if (lstUsuarios.get(i).get_IDUsuario().equals(idusuario))
					return -1;
			}
		}
		ContentValues values = new ContentValues();
		values.put(TablaUsuario.COLUMNA_IDUSUARIO, idusuario);
		values.put(TablaUsuario.COLUMNA_NOMBRES, nombres);
		values.put(TablaUsuario.COLUMNA_APELLIDOS, apellidos);
		values.put(TablaUsuario.COLUMNA_CONTRASENA, contrasena);
		// por defecto el activo es 0 porque no ha iniciado sesion
		values.put(TablaUsuario.COLUMNA_ACTIVO, "0");
		db.insert(TablaUsuario.TABLA_USUARIO, null, values);
		return 1;
	}

	private List<Usuario> ListarUsuarios() {
		List<Usuario> lstUsuarios = new ArrayList<Usuario>();

		Cursor cursor = db.query(TablaUsuario.TABLA_USUARIO, columnas, null,
				null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Usuario nuevoUsuario = cursorToUsuario(cursor);
			lstUsuarios.add(nuevoUsuario);
			cursor.moveToNext();
		}

		cursor.close();
		return lstUsuarios;
	}

	public void BorrarUsuario(Usuario usuario) {
		String id = usuario.get_IDUsuario();
		db.delete(TablaUsuario.TABLA_USUARIO, TablaUsuario.COLUMNA_IDUSUARIO
				+ " = " + id, null);
	}

	public void DepurarTablas() {
		db.delete(TablaUsuario.TABLA_USUARIO, null, null);
		db.delete(TablaReceptor.TABLA_RECEPTOR, null, null);
	}

	private Usuario cursorToUsuario(Cursor cursor) {
		try {
			Usuario usuario = new Usuario();
			usuario.set_IDUsuario(cursor.getString(0));
			usuario.set_Nombres(cursor.getString(1));
			usuario.set_Apellidos(cursor.getString(2));
			usuario.set_Contrasena(cursor.getString(3));
			usuario.set_Activo(cursor.getInt(4));
			return usuario;
		} catch (Exception ex) {
			return null;
		}
	}

	private Receptor cursorToReceptor(Cursor cursor) {
		try {
			Receptor resp = new Receptor();
			resp.setIDUsuario(cursor.getString(0));
			resp.setNombres(cursor.getString(1));
			resp.setCorreo(cursor.getString(2));
			resp.setTel1(cursor.getString(3));
			resp.setTel2(cursor.getString(4));
			resp.setTel3(cursor.getString(5));
			return resp;
		} catch (Exception ex) {
			return null;
		}
	}

	private Usuario ObtenerUsuarioXID(String IDBuscar) {
		Cursor cursor = db.query(TablaUsuario.TABLA_USUARIO, columnas, null,
				null, null, null, null);
		if (cursor.getCount() <= 0)
			return null;
		if (cursor != null) {
			cursor.moveToFirst();
			Usuario resp = cursorToUsuario(cursor);
			if (resp.get_IDUsuario().equals(IDBuscar))
				return resp;
		}
		return null;
	}

	public void CambiarEstadoUsuario(Usuario aActualizar) {
		int aCambiar;
		if (aActualizar.get_Activo() == VariablesGlobales.USUARIO_ACTIVO)
			aCambiar = VariablesGlobales.USUARIO_INACTIVO;
		else
			aCambiar = VariablesGlobales.USUARIO_ACTIVO;

		ContentValues values = new ContentValues();
		values.put(TablaUsuario.COLUMNA_NOMBRES, aActualizar.get_Nombres());
		values.put(TablaUsuario.COLUMNA_APELLIDOS, aActualizar.get_Apellidos());
		values.put(TablaUsuario.COLUMNA_CONTRASENA,
				aActualizar.get_Contrasena());
		values.put(TablaUsuario.COLUMNA_ACTIVO, aCambiar);

		db.update(TablaUsuario.TABLA_USUARIO, values,
				TablaUsuario.COLUMNA_IDUSUARIO + " = ?",
				new String[] { aActualizar.get_IDUsuario() });
	}

	public void ActualizarUsuario(Usuario aActualizar) {
		ContentValues values = new ContentValues();
		values.put(TablaUsuario.COLUMNA_NOMBRES, aActualizar.get_Nombres());
		values.put(TablaUsuario.COLUMNA_APELLIDOS, aActualizar.get_Apellidos());
		values.put(TablaUsuario.COLUMNA_CONTRASENA,
				aActualizar.get_Contrasena());
		values.put(TablaUsuario.COLUMNA_ACTIVO, aActualizar.get_Activo());

		db.update(TablaUsuario.TABLA_USUARIO, values,
				TablaUsuario.COLUMNA_IDUSUARIO + " = ?",
				new String[] { aActualizar.get_IDUsuario() });
	}

	public int IniciarSesion(String IDLogin, String PassLogin) {
		Usuario recuperar = ObtenerUsuarioXID(IDLogin);
		if (recuperar == null)
			return VariablesGlobales.REGISTRO_NO_ENCONTRADO;
		if (!recuperar.get_Contrasena().equals(PassLogin))
			return VariablesGlobales.REGISTRO_NO_COINCIDE;
		CambiarEstadoUsuario(recuperar);
		return VariablesGlobales.REGISTRO_ENCONTRADO;
	}

	public Usuario ObternerSesionActiva() {
		Cursor cursor = db.query(TablaUsuario.TABLA_USUARIO, columnas, null,
				null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();
		else
			return null;
		Usuario resp = cursorToUsuario(cursor);
		/*
		 * if(resp.get_Activo()==VariablesGlobales.USUARIO_ACTIVO) return resp;
		 * return null;
		 */
		return resp;
	}

	// /////////////////RECEPTOR////////////////////
	public int RegistrarReceptor(String idusuario, String nombre,
			String correo, String tel1, String tel2, String tel3) {
		// TODO Validar que el nuevo receptro no exite en SQLite
		ContentValues values = new ContentValues();
		values.put(TablaReceptor.COLUMNA_IDUSUARIO, idusuario);
		values.put(TablaReceptor.COLUMNA_NOMBRE, nombre);
		values.put(TablaReceptor.COLUMNA_CORREO, correo);
		values.put(TablaReceptor.COLUMNA_TELEFONOUNO, tel1);
		values.put(TablaReceptor.COLUMNA_TELEFONODOS, tel2);
		values.put(TablaReceptor.COLUMNA_TELEFONOTRES, tel3);

		db.insert(TablaReceptor.TABLA_RECEPTOR, null, values);
		return 1;
	}

	public Cursor ListarRecPorCursor() {
		Cursor cursor;

		cursor = db.rawQuery("SELECT * FROM Receptor", null);
		cursor.moveToFirst();
		return cursor;
	}

	public List<Receptor> ListarReceptores() {
		List<Receptor> lista = new ArrayList<Receptor>();

		// Cursor cursor = db.query(TablaReceptor.TABLA_RECEPTOR,
		// columnasReceptor, null, null, null, null, null);
		Cursor cursor;

		cursor = db.rawQuery("SELECT * FROM Receptor", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Receptor nuevo = cursorToReceptor(cursor);
			lista.add(nuevo);
			cursor.moveToNext();
		}

		cursor.close();
		return lista;
	}

	public List<Receptor> ListarReceptores(String nombre) {
		List<Receptor> lista = new ArrayList<Receptor>();

		// Cursor cursor = db.query(TablaReceptor.TABLA_RECEPTOR,
		// columnasReceptor, null, null, null, null, null);
		Cursor cursor;

		cursor = db.rawQuery("SELECT * FROM Receptor where "
				+ TablaReceptor.COLUMNA_NOMBRE + " = ?",
				new String[] { nombre });
		// cursor=db.rawQuery("select * from todo where _id = ?", new String[] {
		// id });
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Receptor nuevo = cursorToReceptor(cursor);
			lista.add(nuevo);
			cursor.moveToNext();
		}

		cursor.close();
		return lista;
	}

	public void ActualizarReceptor(Receptor aActualizar) {

		ContentValues values = new ContentValues();
		values.put(TablaReceptor.COLUMNA_IDUSUARIO, aActualizar.getIDUsuario());
		values.put(TablaReceptor.COLUMNA_NOMBRE, aActualizar.getNombres());
		values.put(TablaReceptor.COLUMNA_CORREO, aActualizar.getCorreo());
		values.put(TablaReceptor.COLUMNA_TELEFONOUNO, aActualizar.getTel1());
		values.put(TablaReceptor.COLUMNA_TELEFONODOS, aActualizar.getTel2());
		values.put(TablaReceptor.COLUMNA_TELEFONOTRES, aActualizar.getTel3());

		db.update(TablaReceptor.TABLA_RECEPTOR, values,
				TablaReceptor.COLUMNA_NOMBRE + " = ?",
				new String[] { aActualizar.getNombres() });

	}

	public void BorrarReceptor(Receptor receptor) {

		try {
			String nombre = receptor.getNombres();
			db.delete(TablaReceptor.TABLA_RECEPTOR,
					TablaReceptor.COLUMNA_NOMBRE + " = ?",
					new String[] { nombre });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
