package radarp;

public class RadarThread extends Thread {

	private MessageBuffer<MovingObject> buffer;
	private Radar radar;

	public RadarThread(MessageBuffer<MovingObject> buffer) {
		this.buffer = buffer;
		this.radar = Config.CURRENT_RADAR;
	}

	@Override
	public void run() {
		super.run();
		System.out.println("Radar thread started!");

		radar.initiate();

		MovingObject obj;
		while ((obj = radar.getMovingObject()) != null) {
			buffer.post(obj);
		}

		// buffer.post(new MovingObject(10, 22, 3f, 4f, 50f, 10f));
		// buffer.post(new MovingObject(11, 22, 3f, 4f, 50f, 10f));
		// buffer.post(new MovingObject(12, 22, 3f, 4f, 50f, 10f));
		// buffer.post(new MovingObject(13, 22, 3f, 4f, 50f, 10f));
		// buffer.post(new MovingObject(14, 22, 3f, 4f, 50f, 10f));
		// buffer.post(new MovingObject(15, 22, 3f, 4f, 50f, 10f));

		System.out.println("Radar thread died!");
	}

}
