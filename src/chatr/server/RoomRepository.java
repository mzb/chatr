package chatr.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RoomRepository {

	private static final String REPOSITORY_NAME = "rooms.json";
	private Map<String, Room> rooms;

	public synchronized Room find(String roomName, String password)
			throws RoomNotFoundException, RoomRepository.Error {
		initialize();

		Room room = rooms.get(roomName);
		if (room == null || !room.getPassword().equals(password)) {
			throw new RoomNotFoundException();
		} else {
			return room;
		}
	}

	public synchronized void addRoom(String roomName, String password)
			throws RoomNameNotAvailableException, RoomRepository.Error {
		initialize();

		if (rooms.get(roomName) != null) {
			throw new RoomNameNotAvailableException();
		} else {
			rooms.put(roomName, new Room(roomName, password));
			save();
		}
	}

	private void initialize() throws RoomRepository.Error {
		if (rooms == null) {
			rooms = new HashMap<String, Room>();
		}
		deserialize(read());
	}

	private void deserialize(String data) throws RoomRepository.Error {
		try {
			JSONArray jsonRooms = new JSONArray(data);
			for (int i = 0; i < jsonRooms.length(); ++i) {
				JSONObject jsonRoom = jsonRooms.getJSONObject(i);
				Room room = new Room(jsonRoom.getString("name"),
						jsonRoom.getString("password"));
				rooms.put(room.getName(), room);
			}
		} catch (JSONException e) {
			throw new RoomRepository.Error(e.getMessage());
		}
	}

	private String serialize() throws RoomRepository.Error {
		JSONArray json = new JSONArray();
		for (Room room : rooms.values()) {
			try {
				json.put(new JSONObject().put("name", room.getName()).put("password",
						room.getPassword()));
			} catch (JSONException e) {
				throw new RoomRepository.Error(e.getMessage());
			}
		}
		return json.toString();
	}

	private String read() throws RoomRepository.Error {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader file = new BufferedReader(new FileReader(new File(
					REPOSITORY_NAME).getAbsoluteFile()));
			try {
				String line = null;
				while ((line = file.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				file.close();
			}
		} catch (IOException e) {
			throw new RoomRepository.Error(e.getMessage());
		}
		return sb.toString();
	}

	private void save() throws RoomRepository.Error {
		try {
			PrintWriter file = new PrintWriter(
					new File(REPOSITORY_NAME).getAbsoluteFile());
			try {
				file.print(serialize());
			} finally {
				file.close();
			}
		} catch (FileNotFoundException e) {
			throw new RoomRepository.Error(e.getMessage());
		}
	}

	public static class RoomNotFoundException extends Exception {
	}

	public static class RoomNameNotAvailableException extends Exception {
	}

	public static class Error extends Exception {
		public Error(String msg) {
			super(msg);
		}
	}
}
