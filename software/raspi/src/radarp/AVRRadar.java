package radarp;

import java.io.InputStream;
import java.io.OutputStream;

import radarp.serial.SerialReader;
import radarp.serial.SerialWriter;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class AVRRadar implements Radar {
	private MessageBuffer<byte[]> localBuffer;

	public AVRRadar() {
		// This property must be set for the Raspberry Pi to find the serial port
		System.setProperty("gnu.io.rxtx.SerialPorts", Config.COM_PORT);
		localBuffer = new MessageBuffer<byte[]>();
	}

	@Override
	public void initiate() {
		try {
			// Try to connect to the serial communications port
			connect(Config.COM_PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public MovingObject getMovingObject() {

		String frame = new String(localBuffer.fetch());

		int id = Integer.parseInt(frame.substring(1, 3), 16);
		
		// We multiply the rest with their resolutions
		float length = Integer.parseInt(frame.substring(3, 5), 16) * 0.2f;
		float xPosition = (short) Integer.parseInt(frame.substring(5, 9), 16) * 0.032f;
		float yPosition = (short) Integer.parseInt(frame.substring(9, 13), 16) * 0.032f;
		float xVelocity = (short) Integer.parseInt(frame.substring(13, 17), 16) * 0.1f;
		float yVelocity = (short) Integer.parseInt(frame.substring(17, 21), 16) * 0.1f;
		long timeStamp = System.currentTimeMillis();

		return new MovingObject(id, length, xPosition, yPosition, xVelocity,
				yVelocity, timeStamp);
	}

	void connect(String portName) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),
					2000);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(Config.BAUD_RATE,
						SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

				InputStream in = serialPort.getInputStream();
				OutputStream out = serialPort.getOutputStream();

				(new Thread(new SerialReader(in, localBuffer))).start();
				(new Thread(new SerialWriter(out))).start();

			} else {
				System.err
						.println("Error: Only serial ports are handled by this code.");
				System.exit(1);
			}
		}
	}

}
