package chatr.server;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import chatr.Connection;
import chatr.Message;
import chatr.events.MessageReceived;
import chatr.events.Messages;
import chatr.events.NicknameNotAvailable;
import chatr.events.RoomCreated;
import chatr.events.RoomJoined;
import chatr.events.RoomLeft;
import chatr.events.RoomNameNotAvailable;
import chatr.events.RoomNotFound;
import chatr.events.ServerError;
import chatr.requests.CreateRoom;
import chatr.requests.JoinRoom;
import chatr.requests.LeaveRoom;
import chatr.requests.Request;
import chatr.requests.RequestHandler;
import chatr.requests.SendMessage;
import chatr.requests.ShowMessages;
import chatr.server.RoomRepository.RoomNameNotAvailableException;

public class ClientHandler extends RequestHandler implements Runnable {
  
  private static final Logger log = Logger.getLogger("ClientHandler");

	private Connection connection;
	private boolean running;
	private ActiveRooms activeRooms;
	private RoomRepository roomRepository;
	private MessageRepository messageRepository;

	public ClientHandler(Connection connection, ActiveRooms activeRooms,
			RoomRepository roomRepository, MessageRepository messageRepository) {
		this.connection = connection;
		this.activeRooms = activeRooms;
		this.roomRepository = roomRepository;
		this.messageRepository = messageRepository;
	}

	@Override
	public void handle(JoinRoom request) throws Repository.Error, Connection.Error {
		Room room = activeRooms.find(request.getRoomName(), request.getPassword());
		try {
			if (room == null) {
				room = roomRepository.find(request.getRoomName(), request.getPassword());
				activeRooms.addRoom(room);
			}
			activeRooms.addMember(connection, room.getName(), request.getNickname());
			activeRooms.broadcast(new RoomJoined(request.getRoomName(), 
			    request.getNickname()));
		} catch (RoomRepository.RoomNotFoundException e) {
			connection.put(new RoomNotFound(request.getRoomName()));
		} catch (ActiveRooms.NicknameNotAvailableException e) {
			connection.put(new NicknameNotAvailable(request.getRoomName(), 
			    request.getNickname()));
		}
	}

	@Override
	public void handle(LeaveRoom request) {
		activeRooms.deleteMember(request.getRoomName(), request.getNickname());
		activeRooms.broadcast(new RoomLeft(request.getRoomName(), 
		    request.getNickname()));
	}

	@Override
	public void handle(SendMessage request) throws Repository.Error {
		activeRooms.broadcast(new MessageReceived(request.getRoomName(), 
		    request.getMessage()));
		messageRepository.save(request.getRoomName(), request.getMessage());
	}

	@Override
	public void handle(CreateRoom request) throws Connection.Error, Repository.Error {
		try {
			roomRepository.addRoom(request.getRoomName(), request.getPassword());
			connection.put(new RoomCreated(request.getRoomName()));
		} catch (RoomNameNotAvailableException e) {
			connection.put(new RoomNameNotAvailable(request.getRoomName()));
		}
	}
	
	@Override
	public void handle(ShowMessages request) throws Repository.Error, Connection.Error {
	  List<Message> messages = messageRepository.find(request.getRoomName(), request.getSent());
	  log.info(messages.toString());
	  connection.put(new Messages(messages));
	}

  @Override public void run() {
    Request request = null;
    while (running) {
      try {
        request = (Request) connection.get();
        log.info("Received request " + request);
        request.handle(this);
      } catch (Exception e) {
        if (e instanceof Connection.Error) {
          log.log(Level.WARNING, "Connection lost", e);
          stop();
        } else {
          log.log(Level.SEVERE, "ServerError", e);
          try {
            connection.put(new ServerError(e.getMessage()));
          } catch (Connection.Error ee) {
            log.log(Level.SEVERE, "Cannot send ServerError to client", ee);
            stop();
          }
        }
      }
    }
  }

	public void start() {
		running = true;
		new Thread(this).start();
	}

	public void stop() {
		running = false;
		try {
			connection.close();
			log.info("connection closed.");
		} catch (Connection.Error e) {
			e.printStackTrace();
		}
	}
}