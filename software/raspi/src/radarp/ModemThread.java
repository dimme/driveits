package radarp;

public class ModemThread extends Thread {

	private MessageBuffer<MovingObject> buffer;
	private Modem modem;
	private long[] movingObjectLastTransmitted;

	public ModemThread(MessageBuffer<MovingObject> buffer) {
		this.buffer = buffer;
		this.modem = Config.CURRENT_MODEM;
		
		// There are maximum 64 objects tracked and the id is modulo 64. Page 51 of radar manual.
		this.movingObjectLastTransmitted = new long[64];
	}

	@Override
	public void run() {
		super.run();
		System.out.println("Modem thread started!");

		modem.initiate();

		MovingObject obj;
		while ((obj = buffer.fetch()) != null) {
			
			// Get the current time
			long currTime = System.currentTimeMillis();
			
			// Check if at least 100 ms have passed since that object was transmitted
			if (currTime > movingObjectLastTransmitted[obj.id] + 100) {
				
				// If yes transmit it and set the new time
				modem.transmitMovingObject(obj);
				movingObjectLastTransmitted[obj.id] = currTime;
			}
		}
		
		System.out.println("Radar thread died!");
	}
}
