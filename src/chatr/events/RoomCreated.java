package chatr.events;

public class RoomCreated extends RoomEvent {

	public RoomCreated(String roomName) {
		super(roomName);
	}

	@Override
	public void handle(EventHandler handler) {
		handler.handle(this);
	}
}
