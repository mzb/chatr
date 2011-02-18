package chatr.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import chatr.Connection;
import chatr.Message;
import chatr.events.*;

public class Application extends EventHandler {

	private static final Logger log = Logger.getLogger("chatr");

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
		log.info("Closing...");
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
	  gui.addMessage(String.format("%s: %s", msg.getSenderName(), msg.getBody()));
	}
	
	@Override
	public void handle(NicknameNotAvailable event) {
	  gui.addNotice(String.format("Nick `%s' jest już zajęty", 
	      event.getNickname()));
	}
	
	@Override
	public void handle(Messages event) {
	  Map<String, ArrayList<Message>> messagesByDate = new TreeMap<String, ArrayList<Message>>();
	  for (Message message : event.getMessages()) {
	    String sent = new SimpleDateFormat("dd.MM.yyyy").format(message.getSent());
	    ArrayList<Message> msgs = messagesByDate.get(sent);
	    if (msgs == null) {
	      msgs = new ArrayList<Message>();
	      messagesByDate.put(sent, msgs);
	    }
	    msgs.add(message);
	  }
	  gui.addToMessageLog("\n");
	  Iterator<Map.Entry<String, ArrayList<Message>>> i = messagesByDate.entrySet().iterator();
	  while (i.hasNext()) {
	    Map.Entry<String, ArrayList<Message>> e = i.next();
	    gui.addToMessageLog(String.format("====== %s ======\n", e.getKey()));
	    ArrayList<Message> messages = e.getValue();
	    for (Message message : messages) {
	      gui.addDatedMessage(String.format("%s: %s", 
	          message.getSenderName(), message.getBody()), message.getSent(), "HH:mm");
	    }
	  }
	  gui.addToMessageLog("\n");
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
    
    commands.put("msgs", new Command() {
      private final String SENT_FORMAT = "dd.MM.yyyy";
      public void run(String[] args) throws Exception {
        if (currentRoom == null) {
          gui.addError("Nie jesteś obecnie w żadnym pokoju");
          return;
        }
        if (client != null) {
          String sentArg = args.length > 0 ? args[0] : null;
          Date sent = null;
          if (sentArg != null) {
            SimpleDateFormat df = new SimpleDateFormat(SENT_FORMAT);
            try {
              sent = df.parse(sentArg);
            } catch (ParseException e) {
              gui.addNotice(String.format("msgs: Data powinna byc w formacie `%s'", SENT_FORMAT));
              return;
            }
          }
          client.showMessages(currentRoom.roomName, sent);
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
			log.info(String.format("Connecting to %s:%d...", connectionHost,
					connectionPort));
			client = new Client(this, connectionHost, connectionPort);
		} catch (Connection.Error e) {
		  gui.blockInput();
			log.warning("Unable to connect");
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
				log.info("Disconnecting...");
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
