package chatr.server;

import java.util.HashMap;
import java.util.Map;

import chatr.Connection;
import chatr.events.RoomEvent;

public class ActiveRooms {

	private Map<String, RoomEntry> rooms = new HashMap<String, RoomEntry>();

	public synchronized void addRoom(Room room) {
		rooms.put(room.getName(), new RoomEntry(room));
	}

	public synchronized void addMember(Connection connection, String roomName,
			String nickname) throws NicknameNotAvailableException {
		RoomEntry room = rooms.get(roomName);
		if (room.hasMemberWith(nickname)) {
			throw new NicknameNotAvailableException();
		} else {
			room.addMember(connection, nickname);
		}
	}

	public synchronized void deleteMember(String roomName, String nickname) {
		RoomEntry room = rooms.get(roomName);
		if (room == null) {
			return;
		} else {
			room.deleteMember(nickname);
			if (!room.isEmpty()) {
				rooms.remove(roomName);
			}
		}
	}

	public synchronized Room find(String roomName, String password) {
		RoomEntry room = rooms.get(roomName);
		if (room == null) {
			return null;
		} else {
			return room.getRoom(roomName, password);
		}
	}

	public synchronized void broadcast(RoomEvent event) {
		RoomEntry room = rooms.get(event.getRoomName());
		if (room != null) {
			room.broadcast(event);
		}
	}

	public static class NicknameNotAvailableException extends Exception {
	}

	private static class RoomEntry {

		private Room room;
		private Map<String, MemberEntry> members = new HashMap<String, MemberEntry>();

		public RoomEntry(Room room) {
			this.room = room;
		}

		public boolean isEmpty() {
			return !members.isEmpty();
		}

		public void deleteMember(String nickname) {
			members.remove(nickname);
		}

		public void broadcast(RoomEvent event) {
			for (MemberEntry member : members.values()) {
				try {
					member.send(event);
				} catch (Connection.Error e) {
					e.printStackTrace();
				}
			}
		}

		public boolean hasMemberWith(String nickname) {
			return members.containsKey(nickname);
		}

		public Room getRoom(String name, String password) {
			if (room != null && room.getName().equals(name)
					&& room.getPassword().equals(password)) {
				return room;
			} else {
				return null;
			}
		}

		public void addMember(Connection connection, String nickname) {
			members.put(nickname, new MemberEntry(connection, nickname));
		}
	}

	private static class MemberEntry {

		private String nickname;
		private Connection connection;

		public MemberEntry(Connection connection, String nickname) {
			this.nickname = nickname;
			this.connection = connection;
		}

		public void send(RoomEvent event) throws Connection.Error {
			connection.put(event);
		}
	}
}
