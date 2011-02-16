package chatr.server;

import java.io.IOException;
import java.net.ServerSocket;

import chatr.Connection;

public class Server {

	private ActiveRooms activeRooms = new ActiveRooms();
	private RoomRepository roomRepository = new RoomRepository();
	
	public Server(int port) throws IOException, ClassNotFoundException {
		ServerSocket socket = new ServerSocket(port);
		log("listening at localhost:" + port);

		boolean listening = true;
		while (listening) {
			Connection connection = new Connection(socket.accept());
			log("new connection from " + connection);
			new ClientHandler(connection, activeRooms, roomRepository).start();
		}

		socket.close();
	}
	
	private void log(String msg) {
		System.out.println("[Server] " + msg);
	}
}
