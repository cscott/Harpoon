package realtime;

public class RealtimeThread extends java.lang.Thread {
  MemoryArea mem;
  
  public RealtimeThread() {  // All of the same constructors as Thread...
    super();
    mem = HeapMemory.instance();  // All RealtimeThreads default to pointing to the heap upon creation.
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

  public void run() { // When the run method is called, this RealtimeThread points to the current scope.
    mem = currentRealtimeThread().mem;
    super.run();
  }
  
  public MemoryArea getMemoryArea() {
    return mem;
  }
  
  
}
