package chatr.events;

public class RoomLeft extends RoomEvent {

	private String nickname;

	public RoomLeft(String roomName, String nickname) {
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
