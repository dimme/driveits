import java.nio.ByteBuffer;
import java.util.Arrays;
import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

public class DriveITSClient extends SimpleApplication {
	Geometry[] cars;
	long[] lastSeen;
	KapschModem modem;

	public static void main(String[] args) {
		DriveITSClient app = new DriveITSClient();
		app.start();
	}

	@Override
	public void simpleInitApp() {
		modem = new KapschModem();
		modem.initiate();

		cars = new Geometry[64];
		lastSeen = new long[64];

		for (int i = 0; i < 64; i++) {
			Node carNode = (Node) assetManager
					.loadModel("Models/Ferrari/Car.scene");
			carNode.setShadowMode(ShadowMode.Cast);

			cars[i] = findGeom(carNode, "Car");
			Material mat = cars[i].getMaterial();

			ColorRGBA randColor = ColorRGBA.randomColor();
			mat.setColor("Specular", randColor);
			mat.setColor("Diffuse", randColor);

			cars[i].rotate(0, 3.6f, 0);
			cars[i].setLocalTranslation(1000, 1000, 1000);

			rootNode.attachChild(cars[i]);
		}

		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(3, -5, -10).normalizeLocal());
		sun.setColor(ColorRGBA.White);
		rootNode.addLight(sun);
		rootNode.addLight(sun);

		flyCam.setMoveSpeed(30f);
		cam.setLocation(new Vector3f(-5, 5, 15));
	}

	private Geometry findGeom(Spatial spatial, String name) {
		if (spatial instanceof Node) {
			Node node = (Node) spatial;
			for (int i = 0; i < node.getQuantity(); i++) {
				Spatial child = node.getChild(i);
				Geometry result = findGeom(child, name);
				if (result != null) {
					return result;
				}
			}
		} else if (spatial instanceof Geometry) {
			if (spatial.getName().startsWith(name)) {
				return (Geometry) spatial;
			}
		}
		return null;
	}

	/* This is the update loop */
	@Override
	public void simpleUpdate(float t) {

		byte[] packet = modem.receivePacket();

		int lat = bytesToInt(Arrays.copyOfRange(packet, 20, 24));
		int lon = bytesToInt(Arrays.copyOfRange(packet, 24, 28));
		// float speed = bytesToShort(Arrays.copyOfRange(packet, 28, 30)) /
		// 100f;
		// float heading = bytesToShort(Arrays.copyOfRange(packet, 30, 32)) /
		// 10f;
		byte[] objIdData = new byte[2];
		objIdData[0] = 0;
		objIdData[1] = (byte) ((packet[51] & 0xFF) << 2 | (packet[52] & 0xFF) >> 6);
		short objId = (short) bytesToShort(objIdData);

		float relLat = -Math.abs((577353700 - lat) / 1000) * 3.5f;
		float relLon = -Math.abs((118615300 - lon) / 1000) * 3.5f;

		cars[objId].setLocalTranslation(relLat, 0, relLon);

		// System.out.println(lat + " " + lon);
		// System.out.println(relLat + " " + relLon);

		long currTime = System.currentTimeMillis();
		for (int i = 0; i < 64; i++)
			if (currTime > lastSeen[i] + 3000) {
				cars[i].setLocalTranslation(1000, 1000, 1000);
			}

		lastSeen[objId] = currTime;
	}

	public int bytesToShort(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.put(bytes);
		buffer.flip();// need flip
		return buffer.getShort();
	}

	public int bytesToInt(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.put(bytes);
		buffer.flip();// need flip
		return buffer.getInt();
	}
}