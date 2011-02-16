package chatr.events;

public abstract class RoomEvent implements Event {

	protected String roomName;

	public RoomEvent(String roomName) {
		this.roomName = roomName;
	}

	public String getRoomName() {
		return roomName;
	}

	public abstract void handle(EventHandler handler);
}
