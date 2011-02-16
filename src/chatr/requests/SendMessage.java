package chatr.requests;

import chatr.Message;

public class SendMessage extends Request {

	private Message message;

	public SendMessage(String roomName, Message message) {
		super(roomName);
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}

	@Override
	public void handle(RequestHandler handler) throws Exception {
		handler.handle(this);
	}
}
