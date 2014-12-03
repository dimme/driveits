import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

public class KapschModem {

	// Public constants about the modem
	public final static short RESPONSE_HEADER_SIZE = 12,
			REQUEST_HEADER_SIZE = 10;

	// Private global variables
	private InetAddress modemIPAddress;
	private int modemPort;
	private byte[] localModemMACAddress;
	private DatagramSocket sendingSocket, receiveSocket;

	public KapschModem() {
		// Socket for sending datagrams to the modem
		try {
			sendingSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(6886);
			modemIPAddress = InetAddress.getByName("192.168.11.2");
			modemPort = 8668;
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("Exiting because of critical error.");
			System.exit(0);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("Exiting because of critical error.");
			System.exit(0);
		}
	}

	public void initiate() {

		// Try to see if there is a modem connected
		if (connect()) {
			System.out
					.println("Received handshake response. Modem MAC address: "
							+ getHexString(localModemMACAddress));

		} else {
			System.out.println("Modem not found, exiting the program.");
			System.exit(0);
		}
	}

	private void sendHandshake() {
		byte[] payload = "Hi there!".getBytes();
		byte[] message = new byte[REQUEST_HEADER_SIZE + payload.length];

		// Destination MAC for handshake (all zeros), a short zero for sequense
		// number and the first half of the short for payload size
		for (byte i = 0; i < 9; i++)
			message[i] = 0;

		// The second half of the payload size, we only need that since our
		// payload is short
		message[9] = (byte) payload.length;

		// Put the header and the payload together
		for (int i = 0; i < payload.length; i++)
			message[i + REQUEST_HEADER_SIZE] = payload[i];

		// Create a packet for the handshake
		DatagramPacket handshakePacket = new DatagramPacket(message,
				message.length, modemIPAddress, modemPort);

		// Try to send the packet
		try {
			sendingSocket.send(handshakePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Could not send the packet for handshake, terminating the program.");
			System.exit(0);
		}
	}

	private boolean waitForHandshakeResponse() {

		// Variable for receiving incoming data
		byte[] data = new byte[1024];

		// Packet for receiving data
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		// Try to receive the data
		try {
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Could not receive the handshake response, terminating the program.");
			System.exit(0);
		}

		// Create a ByteBuffer and a ShortBuffer to read the short values from
		// the data
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, 6, 12));
		ShortBuffer sb = bb.asShortBuffer();

		// Read all the values from data
		localModemMACAddress = Arrays.copyOfRange(data, 0, 6);
		@SuppressWarnings("unused")
		short sequence = sb.get();
		@SuppressWarnings("unused")
		short rssi = sb.get();
		short payloadSize = sb.get();
		String payload = new String(Arrays.copyOfRange(data,
				RESPONSE_HEADER_SIZE, RESPONSE_HEADER_SIZE + payloadSize));

		// // Print all values to see if they make sense
		// System.out.println("Modem MAC: " + getHexString(modemMACAddress));
		// System.out.println("Sequence number: " + sequence);
		// System.out.println("RSSI value: " + rssi);
		// System.out.println("Payload size: " + payloadSize);
		// System.out.println("Payload: " + payload);

		// Close the receiving socket
		// receiveSocket.close();

		// If the modem is found, return true. Return false in all other cases
		return (payloadSize == 15 && payload.equals("TS3306 is here!"));
	}

	private boolean connect() {
		sendHandshake();
		System.out.println("Sent handshake to modem");
		return waitForHandshakeResponse();
	}

	public byte[] receivePacket() {

		// Variable for receiving incoming data
		byte[] data = new byte[1024];

		// Packet for receiving data
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);

		// Try to receive the data
		try {
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.out
					.println("Could not receive packet, terminating the program.");
			System.exit(0);
		}

		// Create a ByteBuffer and a ShortBuffer to read the short values from
		// the data
		ByteBuffer bb = ByteBuffer.wrap(Arrays.copyOfRange(data, 6, 12));
		ShortBuffer sb = bb.asShortBuffer();

		// Read all the values from data
		localModemMACAddress = Arrays.copyOfRange(data, 0, 6);
		@SuppressWarnings("unused")
		short sequence = sb.get();
		@SuppressWarnings("unused")
		short rssi = sb.get();
		short payloadSize = sb.get();
		byte[] payload = Arrays.copyOfRange(data, RESPONSE_HEADER_SIZE, RESPONSE_HEADER_SIZE + payloadSize);

		return payload;
	}

	// Converts an array of HEX values to their visual representation in a
	// string.
	public static String getHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1)
					+ ((i == b.length - 1) ? "" : ":");
		}
		return result;
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
