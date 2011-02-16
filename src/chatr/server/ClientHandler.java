package chatr.server;

import chatr.Connection;
import chatr.events.MessageReceived;
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
import chatr.server.RoomRepository.RoomNameNotAvailableException;

public class ClientHandler extends RequestHandler implements Runnable {

	private Connection connection;
	private boolean running;
	private ActiveRooms activeRooms;
	private RoomRepository roomRepository;

	public ClientHandler(Connection connection, ActiveRooms activeRooms,
			RoomRepository roomRepository) {
		this.connection = connection;
		this.activeRooms = activeRooms;
		this.roomRepository = roomRepository;
	}

	@Override
	public void handle(JoinRoom request) throws RoomRepository.Error,
			Connection.Error {
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
	public void handle(SendMessage request) {
		activeRooms.broadcast(new MessageReceived(request.getRoomName(), 
		    request.getMessage()));
	}

	@Override
	public void handle(CreateRoom request) throws Connection.Error,
			RoomRepository.Error {
		log(request + ": name=" + request.getRoomName() + ", password="
				+ request.getPassword());
		try {
			roomRepository.addRoom(request.getRoomName(), request.getPassword());
			log(request + ": room added");
			connection.put(new RoomCreated(request.getRoomName()));
		} catch (RoomNameNotAvailableException e) {
			connection.put(new RoomNameNotAvailable(request.getRoomName()));
		}
	}

	@Override
	public void run() {
		Request request = null;
		try {
			while (running) {
				request = (Request) connection.get();
				request.handle(this);
			}
		} catch (Exception e) {
			if (e instanceof Connection.Error) {
				log("Connection lost!");
				stop();
			} else {
				log("ServerError: " + e.getMessage());
				try {
					connection.put(new ServerError(e.getMessage()));
				} catch (Connection.Error e1) {
					e1.printStackTrace();
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
			log("connection closed.");
		} catch (Connection.Error e) {
			e.printStackTrace();
		}
	}

	private void log(String msg) {
		System.out.println("[ClientHandler#" + connection + "] " + msg);
	}
}