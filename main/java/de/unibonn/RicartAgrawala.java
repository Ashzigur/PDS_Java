package de.unibonn;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by fmardini on 11/22/14.
 */
public class RicartAgrawala implements MutualExclusionAlgorithm {
  private Node node;
  private final LamportClock clock;
  private long sentTimeStamp;
  private boolean acquiring = false;
  private boolean isLocked = false;
  private CountDownLatch latch;
  private final HashSet<String> replied = new HashSet<>();
  private final Queue<String> pendingReplies = new LinkedList<>();


  public RicartAgrawala(Node n) {
    this.node = n;
    clock = new LamportClock(n.address);
  }

  public RicartAgrawala(Node n, boolean initiallyLocked) {
    this.node = n;
    clock = new LamportClock(n.address);
    isLocked = initiallyLocked;
    if (initiallyLocked) {
      latch = new CountDownLatch(0);
    }
  }

  @Override
  public void acquire() {
    if (isLocked) return;
    assert !acquiring;
    acquiring = true;
    clock.incCounter();

    // Should receive a response from every other node in the network
    latch = new CountDownLatch(node.network.size() - 1);
    replied.clear();

    sentTimeStamp = clock.getCounter();
    for (String ip : node.network) {
      if (!ip.equals(node.address)) {
        node.getClient(ip).ra_request(node.address, clock);
      }
    }

    try {
      // When all other nodes reply this will move forward
      while (!latch.await(3, TimeUnit.SECONDS)) {
        System.out.println("Received replies from: " + replied);
        System.out.println("Current count: " + latch.getCount());
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    acquiring = false;
    isLocked = true;
    latch = null;
  }


  public void messageReceived(String ip, long k) {
    clock.receivedCounter(k);

    // If not interested in critical section reply with 'OK'
    if (!node.dc.wantsCritical.get()) {
      node.getClient(ip).ra_reply(node.address);
    // Wants critical section but doesn't have the lock yet
    } else if (acquiring) {
      // I win (the result of this comparison should be the same on the sender as well
      if (LamportClock.compare(sentTimeStamp, node.address, k, ip) < 0) {
        pendingReplies.add(ip);
      // I lose
      } else {
        node.getClient(ip).ra_reply(node.address);
      }
    // In the resource
    } else {
      pendingReplies.add(ip);
    }
  }

  public void okReceived(String ip) {
    assert acquiring;
    clock.incCounter();
    replied.add(ip);

    if (latch != null && latch.getCount() > 0) {
      latch.countDown();
    } else {
      System.out.println("Error");
    }
  }

  @Override
  public void release() {
    // send all pending messages
    isLocked = false;
    while (!pendingReplies.isEmpty()) {
      String ip = pendingReplies.remove();
      node.getClient(ip).ra_reply(node.address);
    }
  }

  @Override
  public void setNode(Node n) {
    this.node = n;
  }

  @Override
  public void done() {
    System.out.println("Done with computation...");
  }

  @Override
  public boolean hasLock() {
    return isLocked;
  }
}
