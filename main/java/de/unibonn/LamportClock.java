package de.unibonn;

/**
 * Created by fmardini on 11/20/14.
 */
/*
 * Should increment the counter before processing received messages
 * We break ties by comparing IP address of requesting hosts
 * Reference: http://krzyzanowski.org/rutgers/notes/pdf/06-clocks.pdf
 */
public class LamportClock {
  private final String ip;
  private long counter = 0;

  public LamportClock(String ip) {
    this.ip = ip;
  }
  public long incCounter() {
    return ++counter;
  }
  public long receivedCounter(long k) {
    if (k > counter) {
      // timestamp of the received event and all further timestamps will be greater than
      // that of the timestamp of sending the message as well as all previous messages
      counter = k + 1;
    }
    return counter;
  }
  // This would allow us to have a total ordering of events
  public int compare(long k, String rIp) {
    return compare(counter, ip, k, rIp);
  }

  public String getIp() {
    return ip;
  }

  public long getCounter() {
    return counter;
  }

  @Override
  public String toString() {
    return ip + ":" + counter;
  }

  public static int compare(long k1, String ip1, long k2, String ip2) {
    if (k1 == k2) return new IPComparator().compare(ip1, ip2);
    return Long.compare(k1, k2);
  }
}
