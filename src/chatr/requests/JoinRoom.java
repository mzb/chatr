package chatr.requests;

public class JoinRoom extends Request {

	private String password;
	private String nickname;

	public JoinRoom(String roomName, String password, String nickname) {
		super(roomName);
		this.password = password;
		this.nickname = nickname;
	}

	@Override
	public void handle(RequestHandler handler) throws Exception {
		handler.handle(this);
	}

	public String getPassword() {
		return password;
	}

	public String getNickname() {
		return nickname;
	}
}
