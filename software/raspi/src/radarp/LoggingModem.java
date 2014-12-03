package radarp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import radarp.Modem;
import radarp.MovingObject;

public class LoggingModem implements Modem {

	private File logFile;

	public LoggingModem() {

	}

	@Override
	public void initiate() {
		logFile = new File("logfile.txt");
		System.out.println("Logging of coordinates and time initiated to "
				+ logFile.getAbsolutePath());
		System.out
				.println("timeStamp;latitude;longitude;xPosition;yPosition;xSpeed;ySpeed;speedMetersPerSecond;directionDegrees");
	}

	@Override
	public void transmitMovingObject(MovingObject obj) {

		String lineToSave = obj.timeStamp + ";"
				+ obj.position.latitudeDirection.toString()
				+ obj.position.latitude + ";"
				+ obj.position.longitudeDirection.toString()
				+ obj.position.longitude + ";" + obj.position.xPosition + ";"
				+ obj.position.yPosition + ";" + obj.velocity.speedX + ";"
				+ obj.velocity.speedY + ";"
				+ obj.velocity.getSpeedMetersPerSecond() + ";"
				+ obj.velocity.getDirectionDegrees() + "\n";

		try {

			if (!logFile.exists()) {
				logFile.createNewFile();
			}

			BufferedWriter output = new BufferedWriter(new FileWriter(logFile.getAbsoluteFile(), true));

			output.write(lineToSave);
			output.close();
		} catch (IOException e) {
			System.out.println("Could not save logfile.txt");
			e.printStackTrace();
			System.exit(0);
		}
	}

	// Int to bytes
	byte[] intToBytes(int i) {
		byte[] result = new byte[4];

		result[0] = (byte) (i >> 24);
		result[1] = (byte) (i >> 16);
		result[2] = (byte) (i >> 8);
		result[3] = (byte) (i);

		return result;
	}
}
