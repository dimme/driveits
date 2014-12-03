package radarp.its;

import radarp.Config;
import radarp.MovingObject;
import radarp.kinematics.Position;
import radarp.kinematics.Velocity;

public class GN {
	public final CommonHeader commonHeader;
	public static final int size = 36;

	public GN(MovingObject.Kind kind, byte[] macAddress, long realTime, Position position, Velocity velocity) {
		this.commonHeader = new CommonHeader(kind, macAddress, realTime, position, velocity);
	}

	public byte[] getBytes() {
		byte[] data = new byte[size];
		
		data[0] = commonHeader.versionAndnextHeader;
		data[1] = commonHeader.headerTypeAndSubtype;
		data[2] = commonHeader.reserved;
		data[3] = commonHeader.flags;
		data[4] = (byte) (commonHeader.payloadLength >> 8);
		data[5] = (byte) commonHeader.payloadLength;
		data[6] = commonHeader.trafficClass;
		data[7] = commonHeader.hopLimit;
		data[8] = (byte) (commonHeader.senderPositionVector.gnAddress.assignementAndStationTypeAndTypeDetailsAndSubtypeAndCountrycode >> 8);
		data[9] = (byte) commonHeader.senderPositionVector.gnAddress.assignementAndStationTypeAndTypeDetailsAndSubtypeAndCountrycode;
		
		for (byte i = 0; i < 6; i++)
			data[10 + i] = commonHeader.senderPositionVector.gnAddress.linkLayerAddress[i];
		
		for (byte i = 1; i <= 4; i++)
			data[15 + i] = (byte) (commonHeader.senderPositionVector.timeStamp >> (32 - i * 8));
			
		int latitude = (int) (commonHeader.senderPositionVector.position.latitude * 10000000);
		for (byte i = 1; i <= 4; i++)
			data[19 + i] = (byte) (latitude >> (32 - i * 8));
		
		int longitude = (int) (commonHeader.senderPositionVector.position.longitude * 10000000);
		for (byte i = 1; i <= 4; i++)
			data[23 + i] = (byte) (longitude >> (32 - i * 8));
		
		short speed = (short) (commonHeader.senderPositionVector.velocity.getSpeedMetersPerSecond() * 100);
		data[28] = (byte) (speed >> 8);
		data[29] = (byte) speed;
		
		short heading = (short) (commonHeader.senderPositionVector.velocity.getDirectionDegrees() * 10);
		data[30] = (byte) (heading >> 8);
		data[31] = (byte) heading;
		
		short altitude = (short) commonHeader.senderPositionVector.position.altitude;
		data[32] = (byte) (altitude >> 8);
		data[33] = (byte) altitude;
		data[34] = commonHeader.senderPositionVector.timestampAndpositionAccuracy;
		data[35] = commonHeader.senderPositionVector.speedAndHeadingAndAltitudeAccuracy;
		
		return data;
	}

	public class CommonHeader {
		public final byte versionAndnextHeader;
		public final byte headerTypeAndSubtype;
		public final byte reserved;
		public final byte flags;
		public final short payloadLength;
		public final byte trafficClass;
		public final byte hopLimit;
		public final SenderPositionVector senderPositionVector;

		public CommonHeader(MovingObject.Kind kind, byte[] linkLayerAddress, long realTime, Position position, Velocity velocity) {
			this.versionAndnextHeader = 0x01; // 0 = Version and 1 = BTP-A
			this.headerTypeAndSubtype = 0x50; // 5 = TSB and 0 = Single Hop
			this.reserved = 0x00;
			this.flags = 0x00;
			this.payloadLength = 29;
			this.trafficClass = 0x3A;
			this.hopLimit = 0x01;
			this.senderPositionVector = new SenderPositionVector(kind, linkLayerAddress, realTime, position, velocity);
		}

		public class SenderPositionVector {
			public final GNAddress gnAddress;
			public final int timeStamp;
			public final Position position;
			public final Velocity velocity;
			public final byte timestampAndpositionAccuracy;
			public final byte speedAndHeadingAndAltitudeAccuracy;

			public SenderPositionVector(MovingObject.Kind kind, byte[] linkLayerAddress, long realTime, Position position, Velocity velocity) {
				this.gnAddress = new GNAddress(kind, linkLayerAddress);
				this.timeStamp = (int) (realTime % 4294967296L); // Expresses the time in milliseconds at which the latitude and longitude of the ITS 
																				    // station were acquired by the GeoAdhoc router. The time is encoded as: TST=TST(UET)mod2^32
																				    // where TST(UET) is the number of milliseconds since the Unix Epoch 1970-01-01T00:00.
				this.position = position;
				this.velocity = velocity;
				this.timestampAndpositionAccuracy = 0x00; // TODO: Check the accuracies and set the correct ones
				this.speedAndHeadingAndAltitudeAccuracy = 0x00; // TODO: Here too
			}

			public class GNAddress {
				public final short assignementAndStationTypeAndTypeDetailsAndSubtypeAndCountrycode;
				public final byte[] linkLayerAddress; // 6 bytes MAC address

				public GNAddress(MovingObject.Kind kind, byte[] linkLayerAddress) {
					byte stationTypeDetails;
					
					switch (kind) {
					case BICYCLE:
						stationTypeDetails = 1;
						break;
					case CAR:
						stationTypeDetails = 2;
						break;
					case TRUCK:
						stationTypeDetails = 3;
						break;
					default:
						stationTypeDetails = 0;
						break;
					}
					
					this.assignementAndStationTypeAndTypeDetailsAndSubtypeAndCountrycode = 
							(short) (0x0400 | stationTypeDetails << 11 | Config.COUNTRY_CODE); // 0x0400 = Automatic / Vehicle ITS Station / Private
					this.linkLayerAddress = linkLayerAddress;
				}
			}
		}
	}

	/**
	 * Testing the GN class
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		GN gn = new GN(MovingObject.Kind.CAR, 
				new byte[]{ (byte) 0x00, (byte) 0xE0, (byte) 0x6A, (byte) 0x00, (byte) 0xBB, (byte) 0x15},
				System.currentTimeMillis(),
				new Position(Position.GeographicDirection.N, Position.GeographicDirection.E, 0f, 0f, 0f), 
				new Velocity(0, 0));

		String result = "";
		for (byte b : gn.getBytes())
			result += Integer.toString(b & 0xFF, 16).toUpperCase() + " ";

		if (result.contains("1 50 0 0 0 1D 3A 1 14 F0 0 E0 6A 0 BB 15"))
			System.out.println("Test passed: " + result);
		else
			System.out.println("Test failed: " + result);
	}
}
