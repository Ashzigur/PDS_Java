package de.unibonn;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by fmardini on 11/14/14.
 */
public class DistributedCalculation implements Runnable {
  private int currentValue;
  private static final Random random = new Random();
  private Date startTime;
  private boolean finished = false;
  private Node node;
  private boolean token = false;
  public final AtomicBoolean wantsCritical = new AtomicBoolean(false);

  public static final int DURATION = 20;
  public boolean shouldStop() {
    Date t = new Date();
    return t.getTime() - startTime.getTime() > DURATION * 1000;
  }

  @Override
  public void run() {
    while (!shouldStop()) {
      wantsCritical.set(true);
      node.getAlg().acquire();

      MathOp op = MathOp.randOp(random);
      int arg = random.nextInt(100) + 1; // never divide by zero
      update(op, arg);
      propagateState(op, arg);

      wantsCritical.set(false);
      node.getAlg().release();

      int sleepInterval = 500 + random.nextInt(500);
      try {
        System.out.println("Sleeping for " + sleepInterval);
        Thread.sleep(sleepInterval);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    finished = true;
    this.node.getAlg().done();
  }

  public void update(MathOp op, int arg) {
    System.out.println("Performing Operation: " + op + "(" + currentValue + ", " + arg + ")");
    currentValue = op.p(currentValue, arg);
    System.out.println("CurrentValue: " + currentValue);
  }

  public int getVal() {
    return currentValue;
  }

  public enum MathOp {
    ADD, SUB, MUL, DIV;

    int p(int a, int b) {
      switch (this.ordinal()) {
        case 0: return a + b;
        case 1: return a - b;
        case 2: return a * b;
        case 3: return a / b;
      }
      return Integer.MAX_VALUE;
    }
    public static MathOp randOp(Random rnd) {
      return MathOp.values()[rnd.nextInt(4)];
    }
  }

  public DistributedCalculation(Node n, int currentValue) {
    node = n;
    this.currentValue = currentValue;
  }

  public void start() {
    if (startTime != null) return;
    startTime = new Date();
    Thread t = new Thread(this);
    t.start();
  }

  public DistributedCalculation(Node n) {
    this(n, random.nextInt(100));
  }

  public void propagateState(DistributedCalculation.MathOp op, int arg) {
    System.out.println("Sending " + op + " " + arg + " to network");
    for (String ip : node.network) {
      if (!ip.equals(node.address)) {
        node.getClient(ip).propagateState(op.ordinal(), arg);
      }
    }
  }

  public String getNodeAlg() {
    return this.node.getAlg().getClass().getSimpleName();
  }
}
