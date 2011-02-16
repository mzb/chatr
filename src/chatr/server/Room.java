package chatr.server;

public class Room {

	private String name;
	private String password;

	public Room(String name, String password) {
		this.name = name;
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}
}
