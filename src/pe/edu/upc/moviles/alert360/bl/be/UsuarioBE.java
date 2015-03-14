package pe.edu.upc.moviles.alert360.bl.be;

public class UsuarioBE {
	// Result = {"CodUsuario":"167",
	// "Nombres":"mm",
	// "Apellidos":"mm",
	// "IDUsuario":"mm",
	// "Contrasena":null,
	// "Activo":"1",
	// "Alertas":"0",
	// "Receptors":"0"}

	private String CodUsuario;
	private String Nombres;
	private String Apellidos;
	private String IDUsuario;
	private String Contrasena;
	private String Activo;
	private String Alertas;
	private String Receptors;
	private String Mensaje;
	private boolean AlarmaInmediata;

	public String getCodUsuario() {
		return CodUsuario;
	}

	public void setCodUsuario(String codUsuario) {
		CodUsuario = codUsuario;
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

	public String getActivo() {
		return Activo;
	}

	public void setActivo(String activo) {
		Activo = activo;
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

	public String getMensaje() {
		return Mensaje;
	}

	public void setMensaje(String mensaje) {
		Mensaje = mensaje;
	}

	public boolean isAlarmaInmediata() {
		return AlarmaInmediata;
	}

	public void setAlarmaInmediata(boolean alarmaInmediata) {
		AlarmaInmediata = alarmaInmediata;
	}

}
