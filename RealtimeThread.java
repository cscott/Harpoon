package realtime; 

// This copy of RealtimeThread is to supply the HCode...

public class RealtimeThread extends java.lang.Thread {
  MemoryArea mem;

  public RealtimeThread() {  // All of the same constructors as java.lang.Thread...
    super();                 // This code should be automatically generated...
    mem = HeapMemory.instance();  // All RealtimeThreads default to pointing to the heap.
  }

  public RealtimeThread(MemoryArea memory) {
    super();
    mem = memory;
  }

  public RealtimeThread(Runnable target) {
    super(target);
    mem = HeapMemory.instance();
  }

  public RealtimeThread(Runnable target, String name) {
    super(target, name);
    mem = HeapMemory.instance();
  }

  public RealtimeThread(String name) {
    super(name);
    mem = HeapMemory.instance();
  }

  public RealtimeThread(ThreadGroup group, Runnable target) {
    super(group, target);
    mem = HeapMemory.instance();
  }

  public RealtimeThread(ThreadGroup group, Runnable target, String name) {
    super(group, target, name);
    mem = HeapMemory.instance();
  }

  public RealtimeThread(ThreadGroup group, String name) {
    super(group, name);
    mem = HeapMemory.instance();
  }

  // Need to copy all static methods/fields (from java.lang.Thread) - This should
  // be automatically generated...

  public static final int MIN_PRIORITY = java.lang.Thread.MIN_PRIORITY;
  public static final int NORM_PRIORITY = java.lang.Thread.NORM_PRIORITY;
  public static final int MAX_PRIORITY = java.lang.Thread.MAX_PRIORITY;

  public static int activeCount() {
    return java.lang.Thread.activeCount();
  }

  public static java.lang.Thread currentThread() {
    return java.lang.Thread.currentThread();
  }

  public static int enumerate(java.lang.Thread tarray[]) {
    return java.lang.Thread.enumerate(tarray);
  }

  public static boolean interrupted() {
    return java.lang.Thread.interrupted();
  }

  public static void yield() {
    java.lang.Thread.yield();
  }

  public static void sleep(long millis) throws InterruptedException {
    java.lang.Thread.sleep(millis);
  }

  public static void sleep(long millis, int nanos) throws InterruptedException {
    java.lang.Thread.sleep(millis, nanos);
  }

  public static void dumpStack() {
    java.lang.Thread.dumpStack();
  }

  // The following methods are part of the RTJ spec.

  public static RealtimeThread currentRealtimeThread() {
    return (RealtimeThread)java.lang.Thread.currentThread();
  }

  public void run() { // When the run method is called, this Realtimerealtime.Thread points to the current scope.
    if ((java.lang.Thread.currentThread() != null) &&
        (java.lang.Thread.currentThread() instanceof RealtimeThread) &&
        (currentRealtimeThread().mem != null)) {
      mem = currentRealtimeThread().mem;
    } else if (mem == null) {
      mem = HeapMemory.instance();
    }
    super.run();
  }
  
  public MemoryArea getMemoryArea() {
    if (mem == null) { // Bypass static initializer chicken-and-egg problem.
      mem = HeapMemory.instance();
    }
    return mem;
  }  
}



