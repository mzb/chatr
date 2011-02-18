package chatr.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import chatr.Message;

public class MessageRepository extends Repository {

  public MessageRepository() throws Repository.Error {
    super();
  }
  
  @Override protected String schema() throws Repository.Error {
    return read("db/messages.sql");
  }
  
  @Override public String name() { return "messages"; }
  
  public List<Message> find(String roomName, Date sent) throws Repository.Error {
    String conditions = "room = ?";
    Object[] bindings;
    if (sent != null) {
      conditions += " and date(timestamp, 'unixepoch') = ?";
      bindings = new Object[]{ roomName, new SimpleDateFormat("yyyy-MM-dd").format(sent) };
    } else {
      bindings = new Object[]{ roomName };
    }
    QueryResults results = query(
        "select * from " + name() + " where (" + conditions + ") order by timestamp asc", 
        bindings);
    
    List<Message> messages = new ArrayList<Message>();
    while (results.next()) {
      String senderName = (String) results.get("sender");
      String body = (String) results.get("body");
      long timestamp = results.getLong("timestamp") * 1000;
      messages.add(new Message(senderName, body, new Date(timestamp)));
    }
    results.close();
    
    return messages;
  }
  
  public void save(String roomName, Message message) throws Repository.Error {
    update("insert into " + name() + " values (?, ?, ?, ?)", 
        message.getBody(), roomName, message.getSenderName(), message.getSent().getTime()/1000);
  }

  

}
