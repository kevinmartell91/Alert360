package pe.edu.upc.moviles.alert360.bl.be;

public class UsuarioBackEnd {

	private String Nombres;
	private String Apellidos;
	private String IDUsuario;
	private String Contrasena;
	private String Activo;

	private String CodUsuario;
	private String Alertas;
	private String Receptors;

	/*
	 * private int Activo; private int CodUsuario; private int Alertas; private
	 * int Receptors;
	 */
	// {"CodUsuario":"174","Nombres":"bnm","Apellidos":"bnm","IDUsuario":"bnm","Contrasena":null,"Activo":"1","Alertas":"0","Receptors":"0"}

	public UsuarioBackEnd() {

	}

	public String getNombres() {
		return Nombres;
	}

	public void setNombres(String nombres) {
		Nombres = nombres;
	}

	public String getApellidos() {
		return Apellidos;
	}

	public void setApellidos(String apellidos) {
		Apellidos = apellidos;
	}

	public String getIDUsuario() {
		return IDUsuario;
	}

	public void setIDUsuario(String iDUsuario) {
		IDUsuario = iDUsuario;
	}

	public String getContrasena() {
		return Contrasena;
	}

	public void setContrasena(String contrasena) {
		Contrasena = contrasena;
	}

	/*
	 * public int getActivo() { return Activo; } public void setActivo(int
	 * activo) { Activo = activo; } public int getAlertas() { return Alertas; }
	 * public void setAlertas(int alertas) { Alertas = alertas; } public int
	 * getReceptors() { return Receptors; } public void setReceptors(int
	 * receptors) { Receptors = receptors; }
	 * 
	 * public int getCodUsuario() { return CodUsuario; } public void
	 * setCodUsuario(int codUsuario) { CodUsuario = codUsuario; }
	 */

	public String getActivo() {
		return Activo;
	}

	public void setActivo(String activo) {
		Activo = activo;
	}

	public String getCodUsuario() {
		return CodUsuario;
	}

	public void setCodUsuario(String codUsuario) {
		CodUsuario = codUsuario;
	}

	public String getAlertas() {
		return Alertas;
	}

	public void setAlertas(String alertas) {
		Alertas = alertas;
	}

	public String getReceptors() {
		return Receptors;
	}

	public void setReceptors(String receptors) {
		Receptors = receptors;
	}

}
