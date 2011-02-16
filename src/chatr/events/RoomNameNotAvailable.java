package chatr.events;

public class RoomNameNotAvailable extends RoomEvent {

	public RoomNameNotAvailable(String roomName) {
		super(roomName);
	}

	@Override
	public void handle(EventHandler handler) {
		handler.handle(this);
	}

}
