package de.unibonn;

import java.util.Comparator;

/**
* Created by fmardini on 11/20/14.
*/
class IPComparator implements Comparator<String> {
  @Override
  public int compare(String o1, String o2) {
    String[] ip1 = o1.split("\\.");
    String[] ip2 = o2.split("\\.");
    for (int i = 0; i < ip1.length; i++) {
      int x = Integer.parseInt(ip1[i]), y = Integer.parseInt(ip2[i]);
      if (x != y) return Integer.compare(x, y);
    }
    return 0;
  }
}
