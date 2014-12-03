package radarp.serial;

import java.io.IOException;
import java.io.InputStream;

import radarp.Config;
import radarp.MessageBuffer;

public class SerialReader implements Runnable {
	InputStream in;
	MessageBuffer<byte[]> localBuffer;

	public SerialReader(InputStream in, MessageBuffer<byte[]> localBuffer) {
		this.in = in;
		this.localBuffer = localBuffer;
	}

	@Override
	public void run() {
		byte[] serialFrame = new byte[22];

		try {
			// Find the header
			while (readBytesInto(in, serialFrame, 0, 1) > -1) {

				if (serialFrame[0] == 'G') {
					readBytesInto(in, serialFrame, 1, 21);

					if (checksum(serialFrame) == serialFrame[21]) {
						localBuffer.post(serialFrame);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private char checksum(byte[] frame) {
		short sum = 0;
		for (short i = 0; i < Config.FRAME_LENGTH_WITHOUT_CRC; i++) {
			sum += (short) (frame[i] & 0xFF);
		}
		return (char) ('H' + sum % 16);
	}

	private int readBytesInto(InputStream in, byte[] array, int offset, int size)
			throws IOException {
		int bytesRead = 0;

		while (bytesRead < size && bytesRead > -1) {
			bytesRead += this.in.read(array, offset + bytesRead, size
					- bytesRead);
		}

		return bytesRead;
	}
}