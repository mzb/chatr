package chatr.events;

import java.io.Serializable;

public interface Event extends Serializable {

	public void handle(EventHandler handler);
}
