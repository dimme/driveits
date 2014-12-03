package radarp;

import java.util.LinkedList;

/**
 * A synchronized message buffer (FIFO) for communication between the reader and transmitter threads.
 * @author Dimitrios Vlastaras
 *
 */
public class MessageBuffer<T> {
	private LinkedList<T> buffer;
	private int maxSize;

	public MessageBuffer() {
		maxSize = 5;
		buffer = new LinkedList<T>();
	}

	/**
	 * Method to put objects in the buffer.
	 * @param The object to add first in the buffer
	 */
	public synchronized void post(T object) {
		while (buffer.size() >= maxSize) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("MessageBuffer.post(): " + e.getLocalizedMessage());
			}
		}
		
		if (buffer.isEmpty()) {
			notifyAll();
		}
		
		buffer.addFirst(object);
	}

	/**
	 * Method to get objects from the buffer.
	 * @return And removes the last object from the buffer
	 */
	public synchronized T fetch() {
		while (buffer.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("MessageBuffer.fetch(): " + e.getLocalizedMessage());
			}
		}
		
		if (buffer.size() >= maxSize) {
			notifyAll();
		}
		
		return buffer.removeLast();
	}

}
