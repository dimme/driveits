package radarp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import radarp.Modem;
import radarp.MovingObject;

import radarp.its.BTP;
import radarp.its.CAM;
import radarp.its.GN;

public class KapschModem implements Modem {

	// Public constants about the modem
	public final static short RESPONSE_HEADER_SIZE = 12,
			REQUEST_HEADER_SIZE = 10;

	// Private global variables
	private int sequenceNumber;
	//private long packetsSent;
	private InetAddress modemIPAddress;
	private int modemPort;
	private byte[] localModemMACAddress, remoteModemMACAddress;
	private DatagramSocket sendingSocket, receiveSocket;
	
	public KapschModem() {
		sequenceNumber = 0;
		//packetsSent = 0;
		remoteModemMACAddress = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };

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

	@Override
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

	@Override
	public void transmitMovingObject(MovingObject obj) {
		//System.out.println("Transmitting " + obj.kind + " with speed: " + obj.velocity.getSpeedMetersPerSecond() + "m/s and position: " +
		//	obj.position.latitudeDirection.toString() + obj.position.latitude + ", " + obj.position.longitudeDirection.toString() + obj.position.longitude);
		
		byte[] data = new byte[GN.size + BTP.size + CAM.size];
		
		GN gn = new GN(obj.kind, localModemMACAddress, obj.timeStamp, obj.position, obj.velocity);
		BTP btp = new BTP((short) 2001, (short) 2001); // As in the Volvo frames
		CAM cam = new CAM((byte) 0, obj.timeStamp, (long) obj.id, obj.position); // MessageID is 0 in all examples for some reason
		
		// Add the GN packet to the datagram
		byte[] gnData = gn.getBytes();
		for (int i = 0; i < GN.size; i++)
			data[i] = gnData[i];
		
		// Add the BTP packet to the datagram
		byte[] btpData = btp.getBytes();
		for (int i = 0; i < BTP.size; i++)
			data[GN.size + i] = btpData[i];
		
		// Add the CAM packet to the datagram
		byte[] camData = cam.getBytes();
		for (int i = 0; i < CAM.size; i++)
			data[GN.size + BTP.size + i] = camData[i];
		
		sendPacket(data);
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
		receiveSocket.close();

		// If the modem is found, return true. Return false in all other cases
		return (payloadSize == 15 && payload.equals("TS3306 is here!"));
	}

	private boolean connect() {
		sendHandshake();
		System.out.println("Sent handshake to modem");
		return waitForHandshakeResponse();
	}

	private int sendPacket(byte[] payload) {
		
		if (sequenceNumber == 65535)
			sequenceNumber = 0;

		sequenceNumber++;
		//packetsSent++;
		
		//if (packetsSent % 100 == 0)
		//	System.out.println("Packets sent so far: " + packetsSent);

		byte[] message = new byte[REQUEST_HEADER_SIZE + payload.length];
		
		// Add the MAC in the header
		for (byte i = 0; i < 6; i++)
			message[i] = remoteModemMACAddress[i];

		// Add the sequence number in the header
		byte[] seqNum = intToBytes(sequenceNumber);
		for (byte i = 0; i < 2; i++)
			message[i + 6] = seqNum[i+2];

		// Add the payload length in the header
		byte[] payloadLen = intToBytes(payload.length);
		for (byte i = 0; i < 2; i++)
			message[i + 8] = payloadLen[i+2];

		// Finally add the payload to the message
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
					.println("Could not send the payload, terminating the program.");
			System.exit(0);
		}

		return sequenceNumber;
	}
	
	// Converts an array of HEX values to their visual representation in a
	// string.
	private static String getHexString(byte[] b) {
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
