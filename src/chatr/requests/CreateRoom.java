package chatr.requests;

public class CreateRoom extends Request {

	private String password;

	public CreateRoom(String roomName, String password) {
		super(roomName);
		this.password = password;
	}

	@Override
	public void handle(RequestHandler handler) throws Exception {
		handler.handle(this);
	}

	public String getPassword() {
		return password;
	}
}
