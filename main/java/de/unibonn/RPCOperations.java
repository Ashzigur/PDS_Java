package de.unibonn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fmardini on 11/9/14.
 */
public class RPCOperations {
  public int rand_op(int i) {
    return i + 1;
  }

  // A node sends this message to join the current network
  // Sends its IP address and gets back a List of the IPs
  // *WITHOUT* its own
  public List<String> join(String ip) {
    List<String> res = new ArrayList<String>(Node.currentInstance.network);
    // Add requesting node to this network
    Node.currentInstance.addToNetwork(ip);

    return res;
  }

  // DC starting with initial value
  public int start_calculation(int val, String alg) {
    Node.currentInstance.startCalculation(val, alg);
    return 0;
  }

  // Inform node that it has the token
  public int take_token() {
    assert Node.currentInstance.getAlg() instanceof TokenRing;
    ((TokenRing) Node.currentInstance.getAlg()).acquireToken();
    return 0;
  }

  public int propagate_state(int op, int arg) {
    System.out.println("Receiving update " + DistributedCalculation.MathOp.values()[op] + " " + arg);
    Node.currentInstance.dc.update(DistributedCalculation.MathOp.values()[op], arg);
    return 0;
  }

  // Entry to critical section request received
  public int ra_request(String ip, long clock) {
    assert Node.currentInstance.getAlg() instanceof RicartAgrawala;
    ((RicartAgrawala) Node.currentInstance.getAlg()).messageReceived(ip, clock);
    return 0;
  }

  public int sign_off(String ip) {
    Node.currentInstance.removeFromNetwork(ip);
    return 0;
  }

  // Received OK from other node
  public int ra_reply(String ip) {
    assert Node.currentInstance.getAlg() instanceof RicartAgrawala;
    ((RicartAgrawala) Node.currentInstance.getAlg()).okReceived(ip);
    return 0;
  }
}
