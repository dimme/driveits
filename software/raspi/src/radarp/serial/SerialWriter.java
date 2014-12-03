package radarp.serial;

import java.io.IOException;
import java.io.OutputStream;

import radarp.Config;

public class SerialWriter implements Runnable {
	OutputStream out;

	public SerialWriter(OutputStream out) {
		this.out = out;
	}

	@Override
	public void run() {
		for (int i = 0; i < 10; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				// Send operation mode and installation height
				byte data = (byte) ((Config.RADAR_SIMULATION ? 1 : 0) << 4 | Config.RADAR_INSTALLATION_HEIGHT);
				out.write(data);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}