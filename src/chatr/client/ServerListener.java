package chatr.client;

import chatr.Connection;
import chatr.events.Event;
import chatr.events.EventHandler;

public class ServerListener implements Runnable {

	private Connection connection;
	private EventHandler eventHandler;
	private boolean running;

	public ServerListener(Connection connection, EventHandler eventHandler) {
		this.connection = connection;
		this.eventHandler = eventHandler;
	}

	@Override
	public void run() {
		Event response = null;
		try {
			while (running) {
				response = (Event) connection.get();
				response.handle(eventHandler);
			}
		} catch (Exception e) {
			// FIXME: Throws java.io.EOFException when connection closes.
		}
	}

	public void stop() {
		running = false;
	}

	public void start() {
		running = true;
		new Thread(this).start();
	}
}
