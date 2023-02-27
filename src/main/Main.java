package main;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import utils.Utils;

public class Main {

	public static void main(String[] args) {

		final String URL = "https://api.tutiempo.net/xml/?lan=es&apid=zwDX4azaz4X4Xqs&lid=3768";
		ArrayList<Dia> dias = new ArrayList<>();

		Connection connection;
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "root");
			Statement statement = connection.createStatement();
			statement.execute("CREATE DATABASE IF NOT EXISTS TIEMPO");
			statement.executeUpdate("DROP TABLE IF EXISTS TIEMPO.HORA, TIEMPO.DIA");
			statement.execute("CREATE TABLE IF NOT EXISTS TIEMPO.DIA (\r\n" + "DIA DATE NOT NULL,\r\n"
					+ "TEMPMAX INT NOT NULL,\r\n" + "TEMPMIN INT NOT NULL,\r\n"
					+ "DESCRIPCION VARCHAR(100) NOT NULL,\r\n" + "HUMEDAD INT NOT NULL,\r\n"
					+ "VIENTO INT NOT NULL,\r\n" + "DIRECCION VARCHAR(100) NOT NULL,\r\n" + "PRIMARY KEY(DIA)\r\n"
					+ ")");

			statement.execute("CREATE TABLE IF NOT EXISTS TIEMPO.HORA (\r\n" + "HORA TIME NOT NULL,\r\n"
					+ "DIA DATE NOT NULL,\r\n" + "TEMP INT NOT NULL,\r\n" + "DESCRIPCION VARCHAR(100) NOT NULL,\r\n"
					+ "PRESION INT NOT NULL,\r\n" + "HUMEDAD INT NOT NULL,\r\n" + "VIENTO INT NOT NULL,\r\n"
					+ "DIRECCION VARCHAR(100) NOT NULL,\r\n" + "PRIMARY KEY(HORA,DIA)\r\n" + ")");

			statement.execute("ALTER TABLE TIEMPO.HORA ADD CONSTRAINT FOREIGN KEY(DIA) REFERENCES\r\n"
					+ "TIEMPO.DIA(DIA) ON UPDATE CASCADE ON DELETE CASCADE");

			System.out.println("DATABASE CREATED");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		SimpleDateFormat dateFormatter1 = new SimpleDateFormat("yyyy-mm-dd");
		SimpleDateFormat dateFormatter2 = new SimpleDateFormat("dd/mm/yyyy");
		SimpleDateFormat hourFormatter = new SimpleDateFormat("HH:mm");

		@SuppressWarnings("unused")
		org.jboss.logging.Logger logger = org.jboss.logging.Logger.getLogger("org.hibernate");
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.OFF);

		Configuration config = new Configuration();
		config.configure("./hibernate.cfg.xml");

		SessionFactory sessionFactory = config.buildSessionFactory();
		Session session = sessionFactory.openSession();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);

		DocumentBuilder db;

		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(new URL(URL).openStream());

			int hourDayIndex = 1;

			for (int i = 1; i <= 7; i++) {
				NodeList dayNodes = doc.getElementsByTagName("day" + i);
				Element dia = (Element) dayNodes.item(0);

				String date = getTagText(dia, "date");
				Date sqlDate = new Date(dateFormatter1.parse(date).getTime());

				int temperature_max = getTagNumber(dia, "temperature_max");
				int temperature_min = getTagNumber(dia, "temperature_min");
				String text = getTagText(dia, "text");

				int humidity = getTagNumber(dia, "humidity");
				int wind = getTagNumber(dia, "wind");
				String wind_direction = getTagText(dia, "wind_direction");

				Dia day = new Dia(sqlDate, temperature_max, temperature_min, text, humidity, wind, wind_direction);

				Set<Hora> horas = new HashSet<Hora>();

				for (int j = hourDayIndex; j < 25; j++) {
					NodeList hourNodes = doc.getElementsByTagName("hour" + j);
					Element hourElement = (Element) hourNodes.item(0);
					String hourDate = getTagText(hourElement, "date");

					if (hourDate.equals(date)) {

						String hour = getTagText(hourElement, "hour_data");
						Time sqlHour = new Time(hourFormatter.parse(hour).getTime());

						int hourTemperature = getTagNumber(hourElement, "temperature");
						String hourText = getTagText(hourElement, "text");
						int hourPressure = getTagNumber(hourElement, "pressure");
						int hourHumidity = getTagNumber(hourElement, "humidity");
						int hourWind = getTagNumber(hourElement, "wind");
						String hourWindDirection = getTagText(hourElement, "wind_direction");

						Hora hourSchema = new Hora(new HoraId(sqlDate, sqlHour), day, hourTemperature, hourText,
								hourPressure, hourHumidity, hourWind, hourWindDirection);

						horas.add(hourSchema);

						hourDayIndex++;
					}
				}

				day.setHoras(horas);

				dias.add(day);

				session.beginTransaction();
				session.save(day);
				session.getTransaction().commit();

				for (Hora hora : horas) {
					session.beginTransaction();
					session.persist(hora);
					session.getTransaction().commit();
				}

			}

			System.out.println("INSERTION COMPLETED");

			// Crear XML
			Document domTree = Utils.generararbolDOMvacio();

			Element tiempoElement = domTree.createElement("tiempo");
			domTree.appendChild(tiempoElement);

			int horaIndex = 1;
			String html = "<html><style>table tr, th, td {border: 1px solid black} td{padding: 5px 10px} h2 {margin: 0}</style><table style='border: 1px solid black'>";

			for (Dia dia : dias) {
				Element diaElement = insertElementWithAttribute(domTree, "dia", tiempoElement, "id",
						dateFormatter2.format(dia.getDia()));

				insertElementWithText(domTree, "tempmax", diaElement, dia.getTempmax() + "");
				insertElementWithText(domTree, "tempmin", diaElement, dia.getTempmin() + "");
				insertElementWithText(domTree, "descripcion", diaElement, dia.getDescripcion());
				insertElementWithText(domTree, "humedad", diaElement, dia.getHumedad() + "");
				insertElementWithText(domTree, "viento", diaElement, dia.getViento() + "");
				insertElementWithText(domTree, "direccion", diaElement, dia.getDireccion());

				Element horasElement = insertElement(domTree, "horas", diaElement);

				String tableHeadings = "<tr><th>Temperatura maxima</th><th>Temperatura minima</th><th>Descripcion</th><th>Humedad</th><th>Viento</th><th>Direccion</th></tr>";

				// Html
				html += "<tr>" + "<th colspan='6' style='text-align:center'><h2>" + dateFormatter2.format(dia.getDia())
						+ "</h2>" + "<td rowspan='3'>" + "<img src='./src/images/"
						+ convertDescription(dia.getDescripcion()) + ".png' />" + "</td>" + "</tr>" + tableHeadings
						+ "<tr><td>" + dia.getTempmax() + "</td>" + "<td>" + dia.getTempmin() + "</td>" + "<td>"
						+ dia.getDescripcion() + "</td>" + "<td>" + dia.getHumedad() + "</td>" + "<td>"
						+ dia.getViento() + "</td>" + "<td>" + dia.getDireccion() + "</td>" + "</tr>";

				// Horas set
				Set<Hora> horas = dia.getHoras();

				for (Hora hora : horas) {
					Element horaElement = insertElementWithAttribute(domTree, "hora", horasElement, "id",
							horaIndex + "");

					insertElementWithText(domTree, "dato", horaElement, hourFormatter.format(hora.getId().getHora()));
					insertElementWithText(domTree, "temp", horaElement, hora.getTemp() + "");
					insertElementWithText(domTree, "descripcion", horaElement, hora.getDescripcion());
					insertElementWithText(domTree, "presion", horaElement, hora.getPresion() + "");
					insertElementWithText(domTree, "humedad", horaElement, hora.getHumedad() + "");
					insertElementWithText(domTree, "viento", horaElement, hora.getViento() + "");
					insertElementWithText(domTree, "direccion", horaElement, hora.getDireccion());

					horaIndex++;
				}
			}

			// Generar XML
			// Transformaremos el árbol DOM en un String y lo guardamos en un fichero
			// TransformerFactory nos permitirá crear el transformador
			TransformerFactory transFact = TransformerFactory.newInstance();
			transFact.setAttribute("indent-number", 4);
			// Sangría de 4 espacios para cada nivel de anidamiento
			Transformer trans = transFact.newTransformer();
			/*
			 * Transformará el árbol DOM dado como un objeto DOMSource(1) en un String por
			 * medio de un objeto StreamResult(2)
			 */
			trans.setOutputProperty(OutputKeys.INDENT, "yes"); // Habilitados las sangrías
			/*
			 * 1. Preparamos el árbol DOM y generamos el objeto DOMSource.
			 */
			domTree.normalize();
			domTree.setXmlStandalone(true);
			// Indicamos que el documento XML no depende de otros.
			DOMSource domSource = new DOMSource(domTree); // Creamos el objeto DOMSource
			// 2. Creamos el objeto StringWriter (String modificable) donde escribirá el
			// objeto StreamResult
			StringWriter salidaStrings = new StringWriter();
			StreamResult sr = new StreamResult(salidaStrings);
			trans.transform(domSource, sr); // Generamos la transformación a String
			System.out.println(salidaStrings.toString());

			Path rutaFicheroSalida = Paths.get("tiempo.xml");
			Charset cs = Charset.forName("utf-8");
			BufferedWriter ficheroSalida = Files.newBufferedWriter(rutaFicheroSalida, cs);

			ficheroSalida.write(salidaStrings.toString());
			ficheroSalida.close();
			System.out.println("tiempo.xml GENERATED");

			// Generar HTML
			Path rutaFicheroSalidaHtml = Paths.get("tiempo.html");
			BufferedWriter ficheroSalidaHtml = Files.newBufferedWriter(rutaFicheroSalidaHtml, cs);

			html += "</table></html>";

			ficheroSalidaHtml.write(html);
			ficheroSalidaHtml.close();
			System.out.println("tiempo.html GENERATED");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Element insertElementWithText(Document domTree, String tagName, Element parentElement,
			String textContent) {
		Element element = domTree.createElement(tagName);
		element.setTextContent(textContent);
		parentElement.appendChild(element);
		return element;
	}

	private static Element insertElementWithAttribute(Document domTree, String tagName, Element parentElement,
			String attributeName, String attributeValue) {
		Element element = domTree.createElement(tagName);
		element.setAttribute(attributeName, attributeValue);
		parentElement.appendChild(element);
		return element;
	}

	private static Element insertElement(Document domTree, String tagName, Element parentElement) {
		Element element = domTree.createElement(tagName);
		parentElement.appendChild(element);
		return element;
	}

	private static int getTagNumber(Element element, String tagName) {
		return Integer.parseInt(element.getElementsByTagName(tagName).item(0).getTextContent());
	}

	private static String getTagText(Element element, String tagName) {
		return element.getElementsByTagName(tagName).item(0).getTextContent();
	}

	private static String convertDescription(String original) {
		return original.replaceAll(" ", "_");
	}
}
