package radarp;

import radarp.kinematics.Position;
import radarp.kinematics.Velocity;

public class MovingObject {
	public enum Kind {
		PRE_TRACK, PEDESTRIAN, BICYCLE, RESULT_NOT_CLEAR, CAR, TRUCK
	}
	
	public final int id;
	public final float length;
	public final Position position;
	public final Velocity velocity;
	public final Kind kind;
	public final long timeStamp;

	public MovingObject(int id, float length, float xPosition, float yPosition,
			float xVelocity, float yVelocity, long timeStamp) {
		this.id = id;
		this.length = length;
		this.position = new Position(xPosition, yPosition);
		this.velocity = new Velocity(xVelocity, yVelocity);
		this.timeStamp = timeStamp;
		
		if (length == 1.0f)
			this.kind = Kind.PRE_TRACK;
		else if (length == 1.2f)
			this.kind = Kind.PEDESTRIAN;
		else if (length == 2.0f)
			this.kind = Kind.BICYCLE;
		else if (length == 3.2f)
			this.kind = Kind.RESULT_NOT_CLEAR;
		else if (length >= 4.4f && length <= 8.4f)
			this.kind = Kind.CAR;
		else if (length > 8.4f)
			this.kind = Kind.TRUCK;
		else
			this.kind = Kind.RESULT_NOT_CLEAR;
	}
}
