package main;
// Generated Feb 27, 2023, 12:38:42 AM by Hibernate Tools 6.1.5.Final

/**
 * Hora generated by hbm2java
 */
public class Hora implements java.io.Serializable {

	private HoraId id;
	private Dia dia;
	private int temp;
	private String descripcion;
	private int presion;
	private int humedad;
	private int viento;
	private String direccion;

	public Hora() {
	}

	public Hora(HoraId id, Dia dia, int temp, String descripcion, int presion, int humedad, int viento,
			String direccion) {
		this.id = id;
		this.dia = dia;
		this.temp = temp;
		this.descripcion = descripcion;
		this.presion = presion;
		this.humedad = humedad;
		this.viento = viento;
		this.direccion = direccion;
	}

	public HoraId getId() {
		return this.id;
	}

	public void setId(HoraId id) {
		this.id = id;
	}

	public Dia getDia() {
		return this.dia;
	}

	public void setDia(Dia dia) {
		this.dia = dia;
	}

	public int getTemp() {
		return this.temp;
	}

	public void setTemp(int temp) {
		this.temp = temp;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public int getPresion() {
		return this.presion;
	}

	public void setPresion(int presion) {
		this.presion = presion;
	}

	public int getHumedad() {
		return this.humedad;
	}

	public void setHumedad(int humedad) {
		this.humedad = humedad;
	}

	public int getViento() {
		return this.viento;
	}

	public void setViento(int viento) {
		this.viento = viento;
	}

	public String getDireccion() {
		return this.direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

}
