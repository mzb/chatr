package chatr.requests;

import java.io.Serializable;

public abstract class Request implements Serializable {

	protected String roomName;

	public Request(String roomName) {
		this.roomName = roomName;
	}

	public String getRoomName() {
		return roomName;
	}

	public abstract void handle(RequestHandler handler) throws Exception;

	public String toString() {
		return getClass().getSimpleName();
	}
}
