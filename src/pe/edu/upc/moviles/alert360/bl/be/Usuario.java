package pe.edu.upc.moviles.alert360.bl.be;

public class Usuario {
	private String _Nombres;
	private String _Apellidos;
	private String _IDUsuario;
	private String _Contrasena;
	private int _Activo;

	public String get_Nombres() {
		return _Nombres;
	}

	public void set_Nombres(String _Nombres) {
		this._Nombres = _Nombres;
	}

	public String get_Apellidos() {
		return _Apellidos;
	}

	public void set_Apellidos(String _Apellidos) {
		this._Apellidos = _Apellidos;
	}

	public String get_IDUsuario() {
		return _IDUsuario;
	}

	public void set_IDUsuario(String _IDUsuario) {
		this._IDUsuario = _IDUsuario;
	}

	public String get_Contrasena() {
		return _Contrasena;
	}

	public void set_Contrasena(String _Contrasena) {
		this._Contrasena = _Contrasena;
	}

	public int get_Activo() {
		return _Activo;
	}

	public void set_Activo(int _Activo) {
		this._Activo = _Activo;
	}
}
