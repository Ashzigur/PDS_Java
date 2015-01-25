package de.unibonn;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fmardini on 11/8/14.
 */
public class Node {
  String address;
  private WebServer webServer;
  private XmlRpcServer xmlRpcServer;
  private PropertyHandlerMapping phm;
  private final HashMap<String, Client> clients = new HashMap<>();

  public MutualExclusionAlgorithm getAlg() {
    return alg;
  }

  public void setAlg(MutualExclusionAlgorithm alg) {
    alg.setNode(this);
    this.alg = alg;
  }

  private MutualExclusionAlgorithm alg;


  final List<String> network = new ArrayList<String>();
  static Node currentInstance = null;
  DistributedCalculation dc;

  static final int port = 3105;

  // Cache clients
  public Client getClient(String ip) {
    if (clients.containsKey(ip)) {
      return clients.get(ip);
    }
    clients.put(ip, new Client(ip));
    return clients.get(ip);
  }


  public Node(String ip) {
    try {
      address = ip;
      this.addToNetwork(ip);
      this.webServer = new WebServer(port, Inet4Address.getByName(ip));
      this.xmlRpcServer = this.webServer.getXmlRpcServer();

      this.phm = new PropertyHandlerMapping();
      this.phm.addHandler("", RPCOperations.class);
      this.xmlRpcServer.setHandlerMapping(phm);
    } catch (UnknownHostException e) {
      System.out.println("Invalid IP Address" + ip);
      System.exit(1);
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
    // Silly Singleton, basically needed so that RPC server can have a reference to the local node
    currentInstance = this;
  }

  public void start() {
    XmlRpcServerConfigImpl serverConfig = (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
    serverConfig.setEnabledForExtensions(true);
    serverConfig.setContentLengthOptional(false);
    try {
      this.webServer.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void stop() {
    this.webServer.shutdown();
  }

  public String nextNode() {
    int idx = network.indexOf(this.address);
    assert idx != -1;
    return network.get((idx + 1) % network.size());
  }

  public void join(String ip) {
    List<String> nodes = getClient(ip).join(address);
    this.addToNetwork(nodes.toArray(new String[nodes.size()]));
  }

  // Nodes should have an ordering in the ring
  // They are ordered by their IP addresses
  public void addToNetwork(String... ips) {
    for (String ip : ips) {
      this.network.add(ip);
    }
    Collections.sort(network, new IPComparator());
  }

  // This is called from a terminal command
  public void startCalculation() {
    // Initialize DC object
    this.dc = new DistributedCalculation(this);
    System.out.println("Started distributed calculation with initial value " + this.dc.getVal());
    // Starting a new calculation, assumes no token in the network
    if (this.getAlg() == null) {
      System.out.println("Starting with the default TokenRing Algorithm");
      this.setAlg(new TokenRing(true));
    } else {
      assert this.getAlg().hasLock();
      System.out.println("Starting with " + this.getAlg().getClass().getSimpleName() + " Algorithm");
    }
    // Inform other nodes that we are about to start the calculation
    System.out.println("Telling other nodes to start");
    for (String ip : network) {
      if (!ip.equals(this.address)) {
        // On receiving this message each node will initialize a DC object with initial value
        // And they will all block until they receive the token to perform an operation
        getClient(ip).startCalculation(dc);
      }
    }
    dc.start();
  }

  // This method is called from the RPC server
  public void startCalculation(int val, String alg) {
    assert !this.getAlg().hasLock();
    System.out.println("Starting distributed calculation with val " + val + " using " + alg + " Algorithm");
    if (alg.equals("TokenRing")) {
      this.setAlg(new TokenRing());
    } else {
      this.setAlg(new RicartAgrawala(this));
    }
    this.dc = new DistributedCalculation(this, val);

    dc.start();
  }


  public void removeFromNetwork(String ip) {
    network.remove(ip);
  }

  public void leave() {
    System.out.println("Leaving Network");
    for (String ip : network) {
      if (!ip.equals(this.address)) {
        getClient(ip).leave(this.address);
      }
    }
  }
}
