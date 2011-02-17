package chatr.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import chatr.Connection;
import chatr.server.Repository.Error;

public class Server {
  
  private static final Logger log = Logger.getLogger("Server");

	private ActiveRooms activeRooms = new ActiveRooms();
	private RoomRepository roomRepository;
	private MessageRepository messageRepository;
	
	public Server(int port) {
		ServerSocket socket = null;
    try {
      socket = new ServerSocket(port);
    } catch (IOException e) {
      abort(e);
    }
		log.info("listening at localhost:" + port);
		
		try {
      roomRepository = new RoomRepository();
      messageRepository = new MessageRepository();
    } catch (Repository.Error e) {
      abort(e);
    }

    try {
  		boolean listening = true;
  		while (listening) {
  			Connection connection = new Connection(socket.accept());
  			log.info("new connection from " + connection);
  			ClientHandler handler = new ClientHandler(connection, 
  			    activeRooms, roomRepository, messageRepository); 
  			handler.start();
  		}
  
  		socket.close();
    } catch (IOException e) {
      abort(e);
    }
	}
	
	private void abort(Exception e) {
	  log.log(Level.SEVERE, "ServerError", e);
    System.exit(1);
	}
}
