package chatr.client;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import chatr.Connection;
import chatr.Message;
import chatr.events.EventHandler;
import chatr.events.MessageReceived;
import chatr.events.NicknameNotAvailable;
import chatr.events.RoomCreated;
import chatr.events.RoomJoined;
import chatr.events.RoomLeft;
import chatr.events.RoomNameNotAvailable;
import chatr.events.RoomNotFound;
import chatr.events.ServerError;

public class Application extends EventHandler {

	private static final Logger logger = Logger.getLogger("chatr");

	private String connectionHost = "localhost";
	private int connectionPort = 3000;
	
	private final Map<String, Application.Command> commands = 
	    new HashMap<String, Application.Command>();
	
	private Client client;
	private GUI gui;
	
	private CurrentRoom currentRoom;

	public Application(String[] args) {
		defineCommands();
	}

	public void run() {
		try {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					disconnect();
				}
			});

			gui = new GUI(this);
			connect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		logger.info("Closing...");
		disconnect();
		System.exit(0);
	}
	
	public void handleInput(String input) {
    if (input.startsWith("/")) {
      handleCommand(input.substring(1));
    } else {
      sendMessage(input);
    }
  }
	
	@Override
	public void handle(RoomCreated event) {
	  gui.addNotice(String.format("Pokój `%s' został utworzony", 
	      event.getRoomName()));
	}
	
	@Override
	public void handle(RoomNameNotAvailable event) {
	  gui.addNotice(String.format("Nazwa `%s' jest już zajęta", 
	      event.getRoomName()));
	}
	
	@Override
	public void handle(RoomJoined event) {
	  gui.addNotice(String.format("%s dołączył(a) do pokoju %s", 
	      event.getNickname(), event.getRoomName()));
	  if (currentRoom != null &&
	      event.getNickname().equals(currentRoom.nickname) &&
	      event.getRoomName().equals(currentRoom.roomName)) {
	    currentRoom.joined = true;
	    gui.setStatus(String.format("%s@%s", 
	        event.getNickname(), event.getRoomName()));
	  }
	}
	
	@Override
  public void handle(RoomLeft event) {
    gui.addNotice(String.format("%s opuścił(a) pokój %s", 
        event.getNickname(), event.getRoomName()));
  }
	
	@Override
	public void handle(RoomNotFound event) {
	  gui.addError("Nieprawidłowa nazwa pokoju lub hasło");
	}
	
	@Override
	public void handle(MessageReceived event) {
	  Message msg = event.getMessage();
	  gui.addMessage(String.format("%s: %s", 
	      msg.getSenderName(), msg.getBody()));
	}
	
	@Override
	public void handle(NicknameNotAvailable event) {
	  gui.addNotice(String.format("Nick `%s' jest już zajęty", 
	      event.getNickname()));
	}
	
	@Override
  public void handle(ServerError error) {
    gui.addError(String.format("Błąd serwera: %s", error.getMessage()));
  }
	
	protected void defineCommands() {
    commands.put("quit", new Command() { 
      public void run(String[] args) throws Exception {
        close();
      }
    });
    
    commands.put("create", new Command() { 
      public void run(String[] args) throws Exception {
        if (args.length < 2) {
          gui.addNotice("create <pokoj> <haslo>");
          return;
        }
        String roomName = args[0];
        String password = args[1];
        if (client != null) client.createRoom(roomName, password);
      }
    });
    
    commands.put("join", new Command() { 
      public void run(String[] args) throws Exception { 
        if (args.length < 3) {
          gui.addNotice("join <nick> <pokoj> <haslo>");
          return;
        }
        String nickname = args[0];
        String roomName = args[1];
        String password = args[2];
        if (client != null) {
          if (currentRoom != null) {
            handleCommand("leave");
          }
          client.joinRoom(roomName, password, nickname);
          currentRoom = new CurrentRoom(roomName, nickname);
        }
      }
    });
    
    commands.put("leave", new Command() { 
      public void run(String[] args) throws Exception {
        if (currentRoom == null) {
          gui.addError("Nie jesteś obecnie w żadnym pokoju");
          return;
        }
        if (client != null) {
          String currentRoomName = currentRoom.roomName;
          String currentRoomNickname = currentRoom.nickname;
          client.leaveRoom(currentRoom.roomName, currentRoom.nickname);
          currentRoom = null;
          gui.addNotice(String.format("%s opuścił(a) pokój %s", 
              currentRoomNickname, currentRoomName));
          gui.setStatus("");
        }
      }
    });
  }
	
	protected void handleCommand(String commandLine) {
	  String[] commandParts = commandLine.split("\\s+");
	  Command command = commands.get(commandParts[0]);
	  if (command != null) {
	    String[] args = new String[commandParts.length - 1]; 
	    System.arraycopy(commandParts, 1, args, 0, commandParts.length - 1);
	    try {
	      command.run(args);
	    } catch (Exception e) {
	      gui.addError(e.toString());
	    }
	  } else {
	    gui.addNotice(String.format("Nieznane polecenie `%s'", commandLine));
	  }
	}
	
	protected void sendMessage(String msg) {
	  if (currentRoom == null) {
      gui.addError("Nie jesteś obecnie w żadnym pokoju");
      return;
    }
	  if (client != null) {
	    try {
  	    client.sendMessage(currentRoom.roomName, 
  	        new Message(currentRoom.nickname, msg));
	    } catch (Exception e) {
	      gui.addError(e.toString());
	    }
	  }
	}

	protected void connect() {
		try {
			logger.info(String.format("Connecting to %s:%d...", connectionHost,
					connectionPort));
			client = new Client(this, connectionHost, connectionPort);
		} catch (Connection.Error e) {
		  gui.blockInput();
			logger.warning("Unable to connect");
			gui.addError(String.format("Nie można połączyć się z serwerem (%s:%d)",
					connectionHost, connectionPort));
		}
	}

	protected void disconnect() {
		if (client != null) {
			try {
			  if (currentRoom != null) {
			    handleCommand("leave");
			  }
				logger.info("Disconnecting...");
				client.disconnect();
			} catch (Connection.Error e) {
			}
		}
	}
	

	public static void main(String[] args) {
		new Application(args).run();
	}
	
	
	private static abstract class Command {
    public abstract void run(String[] args) throws Exception;
	}
	
	private static class CurrentRoom {
	  String roomName;
	  String nickname;
	  boolean joined = false;
	  public CurrentRoom(String roomName, String nickname) {
      this.roomName = roomName;
      this.nickname = nickname;
    }
	}
}
