package realtime;

public abstract class ScopedMemory extends MemoryArea {
  protected java.lang.Object portal;

  public ScopedMemory(long size) {
    super(size);
    portal = null;
    scoped = true;
    parent = RealtimeThread.currentRealtimeThread().getMemoryArea();
  }

  public void enter(java.lang.Runnable logic) {
    super.enter(logic);    
  }
  
  public long getMaximumSize() { // For fixed size scopes...
    return size;
  }
  
  public MemoryArea getOuterScope() {
    return parent;
  }
  
  public java.lang.Object getPortal() {
    return portal;
  }
  
  public void setPortal(java.lang.Object object) {
    portal = object;
  }

  public synchronized void checkAccess(java.lang.Object obj) throws IllegalAccessException { 
    Stats.addCheck();
    if ((obj!=null)&&(obj.memoryArea!=null)&&obj.memoryArea.scoped) {
      ScopedMemory target = (ScopedMemory)(obj.memoryArea);
      MemoryArea p = this;
      while (p != null) {
        if (p == target) {
          return;
        }
        p = p.parent;
      }
      throw new IllegalAccessException();
    } 
  }
}
