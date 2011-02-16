package chatr.events;

public class NicknameNotAvailable extends RoomEvent {

	private String nickname;

	public NicknameNotAvailable(String roomName, String nickname) {
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
