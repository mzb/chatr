package chatr.client;

import java.util.Date;

import chatr.Connection;
import chatr.Message;
import chatr.events.EventHandler;
import chatr.requests.CreateRoom;
import chatr.requests.JoinRoom;
import chatr.requests.LeaveRoom;
import chatr.requests.SendMessage;
import chatr.requests.ShowMessages;

public class Client {

	private Connection connection;
	private ServerListener serverListener;
	private EventHandler eventHandler;

	public Client(EventHandler eventHandler, String host, int port)
			throws Connection.Error {
		this.eventHandler = eventHandler;
		connect(host, port);
	}

	public void createRoom(String name, String password) throws Connection.Error {
		connection.put(new CreateRoom(name, password));
	}

	public void joinRoom(String roomName, String password, String nickname)
			throws Connection.Error {
		connection.put(new JoinRoom(roomName, password, nickname));
	}

	public void leaveRoom(String roomName, String nickname)
			throws Connection.Error {
		connection.put(new LeaveRoom(roomName, nickname));
	}

	public void sendMessage(String roomName, Message message)
			throws Connection.Error {
		connection.put(new SendMessage(roomName, message));
	}
	
	public void showMessages(String roomName, Date sent) throws Connection.Error {
	  connection.put(new ShowMessages(roomName, sent));
	}

	public void disconnect() throws Connection.Error {
		serverListener.stop();
		connection.close();
	}

	protected void connect(String host, int port) throws Connection.Error {
		connection = new Connection(host, port);
		serverListener = new ServerListener(connection, eventHandler);
		serverListener.start();
	}
}
