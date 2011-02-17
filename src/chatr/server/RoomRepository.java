package chatr.server;

public class RoomRepository extends Repository {

  public RoomRepository() throws Repository.Error {
    super();
  }
  
  @Override protected String schema() throws Repository.Error {
    return read("db/rooms.sql");
  }
  
  @Override public String name() { return "rooms"; }

	public Room find(String roomName, String password)
			throws RoomNotFoundException, Repository.Error {
	  QueryResults results = query(
	      "select * from " + name() + " where (name = ? and password = ?) limit 1", 
        roomName, password);
	  if (results.empty()) {
	    results.close();
	    throw new RoomNotFoundException();
	  }
	  Room room = new Room((String)results.get("name"), (String)results.get("password"));
	  results.close();
	  return room;
	}

	public void addRoom(String roomName, String password)
			throws RoomNameNotAvailableException, RoomRepository.Error {
	  QueryResults results = query(
        "select name, password from " + name() + " where (name = ? and password = ?) limit 1", 
        roomName, password);
	  if (!results.empty()) {
	    results.close();
	    throw new RoomNameNotAvailableException();
	  }
	  results.close();
	  update("insert into " + name() + " values (?, ?)", roomName, password);
	}

	public static class RoomNotFoundException extends Exception {
	}

	public static class RoomNameNotAvailableException extends Exception {
	}
}
