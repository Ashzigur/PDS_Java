package de.unibonn;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fmardini on 11/11/14.
 */
public class Client {
  private XmlRpcClient client;
  private String host;
  private int port;

  public Client(String host) {
    this(host, Node.port);
  }

  public Client(String host, int port) {

    try {
      XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
      config.setServerURL(new URL("http://" + host + ":" + port + "/"));
      config.setEnabledForExtensions(true);
      client = new XmlRpcClient();
      // client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
      // client.setTransportFactory(new XmlRpcSun15HttpTransportFactory(client));
      client.setConfig(config);
      this.host = host;
      this.port = port;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  public int rand_op(int x) {
    try {
      Object[] params = new Object[] {x};
      Integer res = (Integer) client.execute("pdc.rand_op", params);
      return res.intValue();
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
    return Integer.MAX_VALUE;
  }

  public List<String> join(String ip) {
    try {
      Object[] params = new Object[] {ip};
      Object[] res = (Object[]) client.execute("pdc.join", params);
      ArrayList<String> s = new ArrayList<String>();
      for (Object o : res) {
        s.add((String) o);
      }
      return s;
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void takeToken() {
    try {
      Object[] params = new Object[] {};
      client.execute("pdc.take_token", params);
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
  }

  public void startCalculation(DistributedCalculation dc) {
    try {
      Object[] params = new Object[] {dc.getVal(), dc.getNodeAlg()};
      client.execute("pdc.start_calculation", params);
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
  }

  public void propagateState(int op, int arg) {
    try {
      Object[] params = new Object[] {op, arg};
      client.execute("pdc.propagate_state", params);
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
  }

  public void ra_request(String myIP, LamportClock clock) {
    try {
      Object[] params = new Object[] {myIP, clock.getCounter()};
      client.execute("pdc.ra_request", params);
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
  }

  public void ra_reply(String ip) {
    try {
      Object[] params = new Object[] {ip};
      client.execute("pdc.ra_reply", params);
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
  }

  public void leave(String ip) {
    try {
      Object[] params = new Object[] {ip};
      client.execute("pdc.sign_off", params);
    } catch (XmlRpcException e) {
      e.printStackTrace();
    }
  }
}
