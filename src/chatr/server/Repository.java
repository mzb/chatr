package chatr.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public abstract class Repository {
  
  protected static final String CONNECTION_URL = "jdbc:sqlite:db/chatr.db";
  protected Connection connection;

  public Repository() throws Repository.Error {
    try {
      Class.forName("org.sqlite.JDBC");
      connection = DriverManager.getConnection(CONNECTION_URL);
      initialize();
    } catch (SQLException e) {
      throw new Repository.Error(e);
    } catch (ClassNotFoundException e) {
      throw new Repository.Error(e);
    }
  }
  
  protected abstract String schema() throws Repository.Error;
  
  public abstract String name();
  
  protected QueryResults query(String sql, Object... bindings) throws Repository.Error {
    PreparedStatement stmt = preparedStatement(sql, bindings);
    try {
      return new QueryResults(stmt.executeQuery());
    } catch (SQLException e) {
      throw new Repository.Error(e);
    }
  }
  
  protected void update(String sql, Object... bindings) throws Repository.Error {
    PreparedStatement stmt = preparedStatement(sql, bindings);
    try {
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new Repository.Error(e);
    }
  }
  
  protected void initialize() throws Repository.Error {
    String schema = schema();
    update(schema);
  }
  
  protected PreparedStatement preparedStatement(String sql, Object... bindings) 
      throws Repository.Error {
    try {
      PreparedStatement stmt = connection.prepareStatement(sql);
      int i = 1;
      for (Object b : bindings) {
        stmt.setObject(i, b);
        i += 1;
      }
      return stmt;
    } catch (SQLException e) {
      throw new Repository.Error(e);
    }
  }
  
  protected String read(String fileName) throws Repository.Error {
    StringBuilder sb = new StringBuilder();
    try {
      BufferedReader file = new BufferedReader(
          new FileReader(new File(fileName).getAbsoluteFile()));
      try {
        String line = null;
        while ((line = file.readLine()) != null) {
          sb.append(line).append(System.getProperty("line.separator"));
        }
      } finally {
        file.close();
      }
    } catch (IOException e) {
      throw new Repository.Error(e);
    }
    return sb.toString();
  }
  
  
  protected static class QueryResults {
    private ResultSet rs;
    public QueryResults(ResultSet rs) { this.rs = rs; }
    public boolean next() throws Repository.Error { 
      try {
        return rs.next();
      } catch (SQLException e) {
        throw new Repository.Error(e);
      } 
    }
    public boolean empty() throws Repository.Error { return !next(); }
    public Object get(String column) throws Repository.Error {
      try {
        return rs.getObject(column);
      } catch (SQLException e) {
        throw new Repository.Error(e);
      }
    }
    public void close() throws Repository.Error { 
      try {
        rs.close();
      } catch (SQLException e) {
        throw new Repository.Error(e);
      } 
    }
  }
  
  
  public static class Error extends Exception {
    public Error(String msg) { super(msg); }
    public Error(Exception e) { super(e); }
  }
}
