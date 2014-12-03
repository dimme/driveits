package radarp;

public class RadarP {
	private static RadarThread radar;
	private static ModemThread modem;
	
	public static void main(String[] args) {
		Config.loadConfigFromFile();
		
		MessageBuffer<MovingObject> buffer = new MessageBuffer<MovingObject>();
		
		radar = new RadarThread(buffer);
		modem = new ModemThread(buffer);
		
		radar.start();
		modem.start();
	}
}
