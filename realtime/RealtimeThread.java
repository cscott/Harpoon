package realtime;

public class RealtimeThread extends java.lang.Thread {
  MemoryArea mem;
  
  public RealtimeThread() {  // All of the same constructors as Thread...
    super();
  }

  public RealtimeThread(Runnable target) {
    super(target);
  }

  public RealtimeThread(Runnable target, String name) {
    super(target, name);
  }

  public RealtimeThread(String name) {
    super(name);
  }

  public RealtimeThread(ThreadGroup group, Runnable target) {
    super(group, target);
  }

  public RealtimeThread(ThreadGroup group, Runnable target, String name) {
    super(group, target, name);
  }

  public RealtimeThread(ThreadGroup group, String name) {
    super(group, name);
  }
  
  public static RealtimeThread currentRealtimeThread() {
    return (RealtimeThread)Thread.currentThread();
  }
  
  public MemoryArea getMemoryArea() {
    return mem;
  }
  
  
}
