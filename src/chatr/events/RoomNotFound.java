package chatr.events;

public class RoomNotFound extends RoomEvent {

	public RoomNotFound(String roomName) {
		super(roomName);
	}

	@Override
	public void handle(EventHandler handler) {
		handler.handle(this);
	}

}
