package chatr.requests;

import java.util.Date;

public class ShowMessages extends Request {
  
  private Date sent;

  public ShowMessages(String roomName, Date sent) {
    super(roomName);
    this.sent = sent;
  }
  
  public Date getSent() {
    return sent;
  }
  
  @Override
  public void handle(RequestHandler handler) throws Exception {
    handler.handle(this);
  }

}
