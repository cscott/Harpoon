import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.ImmortalMemory;
import javax.realtime.VTMemory;
import javax.realtime.LTMemory;
import javax.realtime.MemoryArea;

class Worker extends NoHeapRealtimeThread {
  Worker(MemoryArea ma) { super(ma); }
  public void run() {
    ImmortalMemory im = ImmortalMemory.instance();
    try {
      HelloWorld.results = (String[])im.newArray(String.class, new int[] { 1 });
      HelloWorld.results[0] = (String)im.newInstance(String.class, 
						     new Class[] { String.class },
						     new Object[] { "Hello World!" });
    } catch (Exception e) {
      System.exit(-1);
    }
  }
}
public class HelloWorld {
  public static String[] results = null;
  public static void main(String args[]) {
    LTMemory lt = new LTMemory(1000);
    lt.enter(new Runnable() {
      public void run() {
        Worker w = new Worker(new VTMemory());
        w.start();
        try {
          w.join();
	} catch (Exception e) {
	  System.out.println(e);
	}
      }
    });
    System.out.println(results[0]);
  }
}
