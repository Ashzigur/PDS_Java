package de.unibonn;

import java.util.concurrent.Semaphore;

/**
 * Created by fmardini on 11/20/14.
 */
public class TokenRing implements MutualExclusionAlgorithm {
  private Node n;
  private boolean hasToken = false;
  public final Semaphore sem = new Semaphore(0);

  @Override
  public void setNode(Node n) {
    this.n = n;
  }

  public TokenRing() {
  }
  public TokenRing(boolean tok) {
    hasToken = tok;
  }

  @Override
  public void acquire() {
    // Need the token before entering the critical section
    if (!hasToken) {
      try {
        // Semaphore will be signaled when we get the token
        sem.acquire();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void release() {
    // Done with critical section, release token
    releaseToken();
  }

  // Called from RPC server when token reaches this node
  public void acquireToken() {
    assert !hasToken;
    hasToken = true;
    // Check if want to go into the critical section
    if (n.dc.wantsCritical.get()) {
      // Waiting DC will perform op and then call `release`
      sem.release();
    } else {
      releaseToken();
    }
  }

  public void releaseToken() {
    assert hasToken;
    hasToken = false;
    // To stop token going around at high speed when all processes are idle
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // Send token to next client in the ring
    n.getClient(n.nextNode()).takeToken();
  }

  @Override
  public void done() {
    System.out.println("DONE!!!");
    hasToken = false;
  }

  @Override
  public boolean hasLock() {
    return hasToken;
  }
}
