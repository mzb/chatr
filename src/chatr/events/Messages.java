package chatr.events;

import java.util.List;

import chatr.Message;

public class Messages implements Event {
  
  private List<Message> messages;
  
  public Messages(List<Message> messages) {
    this.messages = messages;
  }
  
  public List<Message> getMessages() {
    return messages;
  }

  @Override
  public void handle(EventHandler handler) {
    handler.handle(this);
  }

}
