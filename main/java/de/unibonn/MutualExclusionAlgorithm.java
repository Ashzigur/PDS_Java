package de.unibonn;

/**
 * Created by fmardini on 11/20/14.
 */
public interface MutualExclusionAlgorithm {
  public void acquire();
  public void release();
  public void setNode(Node n);
  // Called when calculation is complete
  public void done();
  public boolean hasLock();
}
