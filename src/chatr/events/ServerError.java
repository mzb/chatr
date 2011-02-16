package chatr.events;

public class ServerError implements Event {

	private String message;

	public ServerError(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public void handle(EventHandler handler) {
		handler.handle(this);
	}

}
