package chatr;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {

	private String senderName;
	private String body;
	private Date sent;

	public Message(String senderName, String body, Date sent) {
    this.senderName = senderName;
    this.body = body;
    this.sent = sent;
  }
	
	public Message(String senderName, String body) {
	  this(senderName, body, new Date());
  }

	public String getSenderName() {
		return senderName;
	}

	public String getBody() {
		return body;
	}
	
	public Date getSent() {
	  return sent;
	}

	@Override
	public String toString() {
		return "#<Message @sender=" + senderName + ", @body=" + body + "@sent=" + sent + ">";
	}
}
