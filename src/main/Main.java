package main;

import java.net.URL;
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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Main {

	public static void main(String[] args) {

		final String URL = "https://api.tutiempo.net/xml/?lan=es&apid=zwDX4azaz4X4Xqs&lid=3768";
		ArrayList<Dia> dias = new ArrayList<>();

		Connection connection;
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/tiempo", "root", "root");
			Statement statement = connection.createStatement();
			statement.executeUpdate("DROP TABLE IF EXISTS HORA, DIA");
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

			System.out.println(dias);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static int getTagNumber(Element element, String tagName) {
		return Integer.parseInt(element.getElementsByTagName(tagName).item(0).getTextContent());
	}

	private static String getTagText(Element element, String tagName) {
		return element.getElementsByTagName(tagName).item(0).getTextContent();
	}

}
