package TCServer;

import java.util.ArrayList;
import java.util.Random;

import org.json.JSONObject;

class Node1 extends Node {
  private ArrayList<String> list = new ArrayList<String>();
  
  public Node1(int port) {
    super (port);
  }


  public synchronized JSONObject vote_commit(JSONObject object) {
    object.put("response", "node1_vote");
    Random rand = new Random();
    object.put("value", rand.nextInt(4));//value=0/2/3 -> allow, 1 -> not allow
    return object;
  }

  public synchronized JSONObject update(JSONObject object) {
    JSONObject response = new JSONObject();
    if(object.get("status").equals("commit")) {
      list.add(object.getString("data"));
      response.put("response", "list_updated");
    } else {
      response.put("response", "list_not_updated");
    }
    response.put("value", list.toString());
    return response;
  }

  public synchronized JSONObject currentList(JSONObject object) {
    object.put("response", "current list");
    object.put("value", list.toString());
    return object;
  }

  public JSONObject error(String error) {
    JSONObject ret = new JSONObject();
    ret.put("error", error);
    return ret;
  }

  public static void main(String[] args) {
    new Node1(Integer.valueOf(args[0])).run();
  }
}