import javax.realtime.RealtimeThread;
import javax.realtime.MemoryArea;
import javax.realtime.HeapMemory;

// This is a testing driver for the Realtime Java support.

public class Tester {
  
  
  private static void testing(String description, boolean pass) {
    System.out.print("  Testing "+description+" ... ");
    if (pass) {
      System.out.println("passed.");
    } else {
      System.out.println("FAILED!!!");
      System.exit(1);
    }
  }
  
  private static void section(String name) {
    System.out.println("---------- Testing "+name+" -------------");
  }

  public static void main(String argv[]) {
    section("RealtimeThread");
    testing("Thread.currentThread() instanceof RealtimeThread",
	    Thread.currentThread() instanceof RealtimeThread);
    testing("RealtimeThread.currentRealtimeThread().getMemoryArea()!=null",
	    RealtimeThread.currentRealtimeThread().getMemoryArea() != null);
    testing("RealtimeThread.currentRealtimeThread().getMemoryArea() instanceof HeapMemory",
	    RealtimeThread.currentRealtimeThread().getMemoryArea() 
	    instanceof HeapMemory);


    section("ScopedMemory");
    

  }
}

