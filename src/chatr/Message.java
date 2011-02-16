package chatr;

import java.io.Serializable;

public class Message implements Serializable {

	private String senderName;
	private String body;

	public Message(String senderName, String body) {
		this.senderName = senderName;
		this.body = body;
	}

	public String getSenderName() {
		return senderName;
	}

	public String getBody() {
		return body;
	}

	@Override
	public String toString() {
		return "#<Message @sender=" + senderName + ", @body=" + body + ">";
	}
}
