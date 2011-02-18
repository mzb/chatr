package chatr.client;

import java.util.logging.Logger;

import chatr.Connection;
import chatr.events.Event;
import chatr.events.EventHandler;

public class ServerListener implements Runnable {
  
  private static final Logger log = Logger.getLogger("ServerListener");

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
				log.info("Received event " + response.getClass().getSimpleName());
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
