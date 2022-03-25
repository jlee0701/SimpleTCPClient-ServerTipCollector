package TCServer;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.json.JSONObject;

public abstract class Node implements Runnable {
  private int _port;

  public Node(int port) {
    _port = port;
  }

  public abstract JSONObject vote_commit(JSONObject object);

  public abstract JSONObject update(JSONObject object);

  public abstract JSONObject currentList(JSONObject object);

  public abstract JSONObject error(String error);

  @Override
  public void run() {
    // separated so the finally can clean up the connection
    ServerSocket socket = null;
    try {
      // create the listening socket
      socket = new ServerSocket(_port);
      while (true) { // handle connections indefinitely
        Socket conn = null;
        try {
          // listen for connection
          conn = socket.accept();

          // read in a message
          JSONObject root = NetworkUtils.read(conn);

          JSONObject ret = error("");
          if (root.has("request")) {
            switch (root.getString("request")) {
              case ("get_vote"):
                ret = vote_commit(new JSONObject());
                break;
              case ("update_status"):
                ret = update(root);
                break;
              case ("current_list"):
                ret = currentList(root);
                break;
            }
          }

          NetworkUtils.respond(conn, ret);

          // cleanup
          conn.close();
        } catch (SocketException | EOFException e) {
          // expected on timeout
        } catch (IOException ex) {
          ex.printStackTrace();
        } finally {
          // cleanup, just in case
          if (conn != null)
            try {
              conn.close();
            } catch (IOException ex) {
              ex.printStackTrace();
            }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      // cleanup, just in case
      if (socket != null)
        try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
  }
}
