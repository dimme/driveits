package radarp;

import java.io.FileInputStream;
import java.util.Properties;

import radarp.kinematics.Position;

public class Config {

	/**
	 * The file location of the config file
	 */
	public static final String fileLocation = "/boot/radarp.conf";

	/**
	 * Serial communications port name for the AVR
	 */
	public static String COM_PORT = "/dev/ttyAMA0";// "COM5";//
													// "/dev/cu.usbserial-A4001sy9";
	/**
	 * The baud rate selection is explained in the report
	 */
	public static int BAUD_RATE = 57600;

	public static int FRAME_LENGTH_WITHOUT_CRC = 21;

	/**
	 * Position of the radar.
	 */
	public static Position RADAR_POSITION = null;

	/**
	 * Facing direction of the radar in degrees. 0 degrees is north, advancing
	 * clockwise.
	 */
	public static float RADAR_BEARING = 0f;
	
	/**
	 * Whether the radar should simulate traffic or not.
	 */
	public static boolean RADAR_SIMULATION = false;
	
	/**
	 * Installation height of the radar in meters. Min 1m max 10m.
	 */
	public static int RADAR_INSTALLATION_HEIGHT = 5;

	/**
	 * Slope of the road in degrees. Positive is uphill and negative is
	 * downhill. 0 is flat.
	 */
	public static float ROAD_SLOPE = 0f;

	/**
	 * Sweden according to COMPLEMENT TO ITU-T RECOMMENDATION E.212 (11/98)
	 */
	public static short COUNTRY_CODE = 0;

	/**
	 * @return The current radar implementation
	 */
	public static Radar CURRENT_RADAR = new AVRRadar();

	/**
	 * @return The current modem implementation
	 */
	public static Modem CURRENT_MODEM = new LoggingModem();

	/**
	 * It reads the current configuration from a text file
	 * 
	 * @param fileName
	 *            The config file
	 */
	public static void loadConfigFromFile() {
		String fileName = Config.fileLocation;
		
		Properties configFile = new java.util.Properties();

		try {
			configFile.load(new FileInputStream(fileName));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not load config file: " + e.getLocalizedMessage());
			System.out.println("Is your config file placed in /boot/radarp.conf ?");
			System.exit(1);
		}

		Config.RADAR_POSITION = new Position(
				configFile.getProperty("radarLatitudeDirection","N").equals("N") ? Position.GeographicDirection.N : Position.GeographicDirection.S,
				configFile.getProperty("radarLongitudeDirection","E").equals("E") ? Position.GeographicDirection.E : Position.GeographicDirection.W,
				Double.valueOf(configFile.getProperty("radarLatitude", "0")),
				Double.valueOf(configFile.getProperty("radarLongitude", "0")),
				Double.valueOf(configFile.getProperty("radarAltitude", "0"))); // Altitude in meters from the sea level at ground point.
																			// (NOT on top of the pole)
		Config.RADAR_BEARING = Float.valueOf(configFile.getProperty("radarBearing", "0"));
		Config.RADAR_SIMULATION = Boolean.valueOf(configFile.getProperty("radarSimulation", "false"));
		Config.RADAR_INSTALLATION_HEIGHT = Integer.valueOf(configFile.getProperty("radarInstallationHeight", "5"));
		if (Config.RADAR_INSTALLATION_HEIGHT < 1 || Config.RADAR_INSTALLATION_HEIGHT > 10) // Limiting it between 1 and 10 meters
			Config.RADAR_INSTALLATION_HEIGHT = 5;
		Config.ROAD_SLOPE = Float.valueOf(configFile.getProperty("roadSlope","0"));
		Config.COUNTRY_CODE = Short.valueOf(configFile.getProperty("countryCode","240"));
		
		System.out.println("Config loaded successfully: " + fileName);
		System.out.println("Radar position: " + Config.RADAR_POSITION.latitudeDirection + Config.RADAR_POSITION.latitude + ", " + Config.RADAR_POSITION.longitudeDirection + Config.RADAR_POSITION.longitude);
		System.out.println("Radar simulate traffic: " + Config.RADAR_SIMULATION);
		System.out.println("Radar installation height: " + Config.RADAR_INSTALLATION_HEIGHT + " meters");
		System.out.println("Radar bearing: " + Config.RADAR_BEARING + "¡");
		System.out.println("Road slope: " + Config.ROAD_SLOPE + "¡");
		System.out.println("Country code: " + Config.COUNTRY_CODE);
	}
	
	/**
	 * Testing loading the config file
	 * @param args
	 */
	public static void main(String[] args) {
		Config.loadConfigFromFile();
	}
}
