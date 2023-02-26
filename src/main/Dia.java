package main;
// Generated Feb 27, 2023, 12:38:42 AM by Hibernate Tools 6.1.5.Final

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Dia generated by hbm2java
 */
public class Dia implements java.io.Serializable {

	private Date dia;
	private int tempmax;
	private int tempmin;
	private String descripcion;
	private int humedad;
	private int viento;
	private String direccion;
	private Set horas = new HashSet(0);

	public Dia() {
	}

	public Dia(Date dia, int tempmax, int tempmin, String descripcion, int humedad, int viento, String direccion) {
		this.dia = dia;
		this.tempmax = tempmax;
		this.tempmin = tempmin;
		this.descripcion = descripcion;
		this.humedad = humedad;
		this.viento = viento;
		this.direccion = direccion;
	}

	public Dia(Date dia, int tempmax, int tempmin, String descripcion, int humedad, int viento, String direccion,
			Set horas) {
		this.dia = dia;
		this.tempmax = tempmax;
		this.tempmin = tempmin;
		this.descripcion = descripcion;
		this.humedad = humedad;
		this.viento = viento;
		this.direccion = direccion;
		this.horas = horas;
	}

	public Date getDia() {
		return this.dia;
	}

	public void setDia(Date dia) {
		this.dia = dia;
	}

	public int getTempmax() {
		return this.tempmax;
	}

	public void setTempmax(int tempmax) {
		this.tempmax = tempmax;
	}

	public int getTempmin() {
		return this.tempmin;
	}

	public void setTempmin(int tempmin) {
		this.tempmin = tempmin;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
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

	public Set getHoras() {
		return this.horas;
	}

	public void setHoras(Set horas) {
		this.horas = horas;
	}

}
