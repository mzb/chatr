package chatr.events;

import chatr.Message;

public class MessageReceived extends RoomEvent {

	private Message message;

	public MessageReceived(String roomName, Message message) {
		super(roomName);
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}

	@Override
	public void handle(EventHandler handler) {
		handler.handle(this);
	}
}