package radarp.its;

import radarp.kinematics.Position;
import radarp.kinematics.Position.GeographicDirection;

public class CAM {
	
	public final CamPdu camPdu;
	public static final int size = 25;
	
	public CAM(byte messageID, long timeStamp, long stationID, Position referencePosition) {
		this.camPdu = new CamPdu(messageID, timeStamp, stationID, referencePosition);
	}
	
	public byte[] getBytes() {
		byte[] data = new byte[size];
		
		int lon = (int) (camPdu.cam.referencePosition.longitude*1000000);
		int lat = (int) (camPdu.cam.referencePosition.latitude*1000000);
		int altWithOffset = (int) Math.round(camPdu.cam.referencePosition.altitude) + 10000;
		
		data[0] = camPdu.header.protocolVersion;
		data[1] = camPdu.header.messageID;
		
		for (byte i = 1; i <= 6; i++)
			data[1 + i] = (byte) (camPdu.header.generationTime >> (48 - i * 8));
		
		for (byte i = 1; i <= 4; i++) {
			data[7 + i] = (byte) (camPdu.cam.stationID >> (34 - i * 8));
		}
		data[12] = (byte) (camPdu.cam.stationID << 6 | camPdu.cam.stationCharacteristics);
		
		
		data[13] = (byte) (camPdu.cam.referencePosition.longitudeDirection == GeographicDirection.W ? 1 << 4 : 0
				
				| lon >> 27
				
				);
		
		data[14] = (byte) (lon >> 19);
		data[15] = (byte) (lon >> 11);
		data[16] = (byte) (lon >> 3);
		data[17] = (byte) (lon << 5 |
				(byte)(camPdu.cam.referencePosition.latitudeDirection == GeographicDirection.S ? 1 << 4 : 0) |
				lat >> 26
				);
		data[18] = (byte) (lat >> 18);
		data[19] = (byte) (lat >> 10);
		data[20] = (byte) (lat >> 2);
		data[21] = (byte) (lat << 6 | altWithOffset >> 18);
		data[22] = (byte) (altWithOffset >> 10);
		data[23] = (byte) (altWithOffset >> 2);
		data[24] = (byte) (altWithOffset << 6);
		
		return data;
	}
	
	public class CamPdu {
		public final Header header;
		public final Cam cam;
		
		public CamPdu(byte messageID, long timeStamp, long stationID, Position referencePosition) {
			this.header = new Header(messageID, timeStamp);
			this.cam = new Cam(stationID, referencePosition);
		}
		
		public class Header {
			public final byte protocolVersion;
			public final byte messageID;
			public final long generationTime;
			
			public Header(byte messageID, long timeStamp) {
				this.protocolVersion = 0;
				this.messageID = messageID;
				this.generationTime = timeStamp; //System.currentTimeMillis();
			}
		}
		
		public class Cam {
			public final long stationID;
			public final byte stationCharacteristics;
			public final Position referencePosition;
			
			public Cam(long stationID, Position referencePosition) {
				this.stationID = stationID;
				this.stationCharacteristics = 0x1C; // mobile, private and physicalRelevant (basicVehicle profile)
				this.referencePosition = referencePosition;
			}
		}
	}
	
	/**
	 * Testing the CAM class
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// This test is obsolete, the CAM messages work
		
		CAM cam = new CAM((byte) 0, System.currentTimeMillis(), 2130706433L, new Position(GeographicDirection.N, GeographicDirection.E, 48.215126f, 16.434540f, 1560f));

		String result = "";
		for (byte b : cam.getBytes())
			result += Integer.toString(b & 0xFF, 16).toUpperCase() + " ";
		
		if (result.contains("0 1F 58 AD 80 B7 ED 15 80 B 4A 0"))
			System.out.println("Test passed: " + result);
		else
			System.out.println("Test failed: " + result);
	}

}
