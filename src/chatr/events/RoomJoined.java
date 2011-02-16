package chatr.events;

public class RoomJoined extends RoomEvent {

	private String nickname;

	public RoomJoined(String roomName, String nickname) {
		super(roomName);
		this.nickname = nickname;
	}

	public String getNickname() {
		return nickname;
	}

	@Override
	public void handle(EventHandler handler) {
		handler.handle(this);
	}

}
