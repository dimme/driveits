package radarp.its;

public class BTP {
	public final short destinationPort, sourcePort;
	public static final int size = 4;

	public BTP(short destinationPort, short sourcePort) {
		this.destinationPort = destinationPort;
		this.sourcePort = sourcePort;
	}

	public byte[] getBytes() {
		byte[] data = new byte[size];
		
		data[0] = (byte) (destinationPort >> 8);
		data[1] = (byte) destinationPort;
		data[2] = (byte) (sourcePort >> 8);
		data[3] = (byte) sourcePort;
		
		return data;
	}

	/**
	 * Testing the BTP class
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		BTP btp = new BTP((short) 2001, (short) 2000);

		String result = "";
		for (byte b : btp.getBytes())
			result += Integer.toString(b & 0xFF, 16).toUpperCase() + " ";
		
		if (result.equals("7 D1 7 D0 "))
			System.out.println("Test passed: " + result);
		else
			System.out.println("Test failed: " + result);
	}
}
