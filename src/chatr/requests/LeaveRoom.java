package chatr.requests;

public class LeaveRoom extends Request {

	private String nickname;

	public LeaveRoom(String roomName, String nickname) {
		super(roomName);
		this.nickname = nickname;
	}

	@Override
	public void handle(RequestHandler handler) throws Exception {
		handler.handle(this);
	}

	public String getNickname() {
		return nickname;
	}
}
