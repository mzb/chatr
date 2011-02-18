package chatr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connection {

  private Socket socket;
  private ObjectOutputStream output;
  private ObjectInputStream input;

  public Connection(String host, int port) throws Connection.Error {
    try {
      socket = new Socket(host, port);
    } catch (UnknownHostException e) {
      throw new Connection.Error(e.getMessage());
    } catch (IOException e) {
      throw new Connection.Error(e.getMessage());
    }
  }

  public Connection(Socket socket) {
    this.socket = socket;
  }

  public void close() throws Connection.Error {
    try {
      if (input != null) {
        input.close();
      }
      if (output != null) {
        output.close();
      }
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      throw new Connection.Error(e.getMessage());
    }
  }

  public void put(Serializable data) throws Connection.Error {
    try {
      if (output == null) {
        output = new ObjectOutputStream(socket.getOutputStream());
      }
      output.writeObject(data);
      output.flush();
      output.reset();
    } catch (IOException e) {
      throw new Connection.Error(e.getMessage());
    }
  }

  public Object get() throws Connection.Error {
    try {
      if (input == null) {
        input = new ObjectInputStream(socket.getInputStream());
      }
      return input.readObject();
    } catch (IOException e) {
      throw new Connection.Error(e.getMessage());
    } catch (ClassNotFoundException e) {
      throw new Connection.Error(e.getMessage());
    }
  }

  @Override
  public String toString() {
    return socket.toString();
  }

  @Override
  public boolean equals(Object other) {
    return socket.equals(other);
  }

  @Override
  public int hashCode() {
    return socket.hashCode();
  }

  public static class Error extends Exception {

    public Error(String message) {
      super(message);
    }
  }
}
