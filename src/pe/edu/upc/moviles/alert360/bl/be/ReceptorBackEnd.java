package pe.edu.upc.moviles.alert360.bl.be;

public class ReceptorBackEnd {

	/*
	 * {"CodReceptor":"7", "CodUsuario":"185", "Nombre":"Andree",
	 * "CorreoElectronico":"atorres@upc.com", "TelefonoUno":"988576205",
	 * "TelefonoDos":"988576205", "TelefonoTres":"988576205"}]
	 */

	private String CodReceptor;
	private String CodUsuario;
	private String Nombre;
	private String CorreoElectronico;
	private String TelefonoUno;
	private String TelefonoDos;
	private String TelefonoTres;

	public String getCodReceptor() {
		return CodReceptor;

	}

	public void setCodReceptor(String codReceptor) {
		CodReceptor = codReceptor;
	}

	public String getCodUsuario() {
		return CodUsuario;
	}

	public void setCodUsuario(String codUsuario) {
		CodUsuario = codUsuario;
	}

	public String getNombre() {
		return Nombre;
	}

	public void setNombre(String nombre) {
		Nombre = nombre;
	}

	public String getCorreoElectronico() {
		return CorreoElectronico;
	}

	public void setCorreoElectronico(String correoElectronico) {
		CorreoElectronico = correoElectronico;
	}

	public String getTelefonoUno() {
		return TelefonoUno;
	}

	public void setTelefonoUno(String telefonoUno) {
		TelefonoUno = telefonoUno;
	}

	public String getTelefonoDos() {
		return TelefonoDos;
	}

	public void setTelefonoDos(String telefonoDos) {
		TelefonoDos = telefonoDos;
	}

	public String getTelefonoTres() {
		return TelefonoTres;
	}

	public void setTelefonoTres(String telefonoTres) {
		TelefonoTres = telefonoTres;
	}

}
