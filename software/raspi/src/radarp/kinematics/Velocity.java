package radarp.kinematics;

import radarp.Config;

public class Velocity {
	public final float speedX, speedY;

	public Velocity(float speedX, float speedY) {
		this.speedX = speedX;
		this.speedY = speedY;
	}

	public float getSpeedMetersPerSecond() {
		return (float) Math.sqrt(speedX * speedX + speedY * speedY);
	}

	public float getSpeedKilometersPerHour() {
		return getSpeedMetersPerSecond() * 3.6f;
	}
	
	public float getDirectionDegrees() {
		float speed = this.getSpeedMetersPerSecond();
		
		if (speed == 0f)
			return 0f;
		
		return (float) (Math.toDegrees(Math.acos(speedX / speed)) + Config.RADAR_BEARING + 180.0f) % 360;
	}
}
