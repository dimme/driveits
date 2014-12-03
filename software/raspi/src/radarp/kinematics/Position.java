package radarp.kinematics;

import radarp.Config;


public class Position {
	public enum GeographicDirection {
		E, W, N, S
	};
	
	public final float xPosition, yPosition;
	public final GeographicDirection latitudeDirection, longitudeDirection;
	public final double latitude, longitude, altitude;
	
	public Position(GeographicDirection latitudeDirection, GeographicDirection longitudeDirection,
			double latitude, double longitude, double altitude) {
		this.latitudeDirection = latitudeDirection;
		this.longitudeDirection = longitudeDirection;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		
		xPosition = 0; 
		yPosition = 0;
	}
	
	/**
	 * Creates an Position object for a scanned object from the Radar
	 * @param radarX
	 * @param radarY
	 */
	public Position(float radarX, float radarY) {
		xPosition = radarX;
		yPosition = radarY;
		
		// Inspiration taken from: http://www.movable-type.co.uk/scripts/latlong.html
		// Section: "Destination point given distance and bearing from start point"
		double objDistanceMeters = Math.hypot(radarX, radarY);
		double objDistance = objDistanceMeters / 6367500; /* Mean earth radius */
		double objBearing = Math.atan2(radarY, radarX) + Math.toRadians(Config.RADAR_BEARING);

		// Convert the Radar coordinates to radians
		double latitudeRadar = Math.toRadians(Config.RADAR_POSITION.latitude);
		double longitudeRadar = Math.toRadians(Config.RADAR_POSITION.longitude);
		
		// Calculate the object coordinates
		double latitudeObject = Math.asin(Math.sin(latitudeRadar)*Math.cos(objDistance) + 
                Math.cos(latitudeRadar)*Math.sin(objDistance)*Math.cos(objBearing));
		double longitudeObject = longitudeRadar + Math.atan2(Math.sin(objBearing)*Math.sin(objDistance)*Math.cos(latitudeRadar), 
                Math.cos(objDistance)-Math.sin(latitudeRadar)*Math.sin(latitudeObject));
		
		// Normalize to -180 ... +180 degrees
		longitudeObject = (longitudeObject+3*Math.PI) % (2*Math.PI) - Math.PI;  
		
		// Set the object coordinates in degrees
		this.latitude = Math.toDegrees(latitudeObject);
		this.longitude = Math.toDegrees(longitudeObject);
		
		// Calculate the object altitude
		this.altitude = (Config.RADAR_POSITION.altitude + Math.sin(Math.toRadians(Config.ROAD_SLOPE)) * objDistanceMeters);
		
		// Set the geographic directions
		this.latitudeDirection = Config.RADAR_POSITION.latitudeDirection;
		this.longitudeDirection = Config.RADAR_POSITION.longitudeDirection;
	}
	
	/**
	 * Testing the position class
	 * @param args
	 */
	public static void main(String[] args) {
		Position pos = new Position(128, 0);
		
		System.out.println("Moving object position: " + pos.latitudeDirection.toString() + pos.latitude + ", " + pos.longitudeDirection.toString() + pos.longitude);
		System.out.println("https://maps.google.com/maps?q=" + pos.latitudeDirection.toString() + pos.latitude + "," + pos.longitudeDirection.toString() + pos.longitude + "%20(Moving%20Object)");
		System.out.println("https://maps.google.com/maps?saddr=" + Config.RADAR_POSITION.latitudeDirection.toString() + Config.RADAR_POSITION.latitude + "," + Config.RADAR_POSITION.longitudeDirection.toString() + Config.RADAR_POSITION.longitude + "%20(Radar)&daddr=" + pos.latitudeDirection.toString() + pos.latitude + "," + pos.longitudeDirection.toString() + pos.longitude + "%20(Moving%20Object)");
	}	
}