import javax.realtime.RealtimeThread;
import javax.realtime.HeapMemory;
import javax.realtime.MemoryArea;

public class Hello2 {
  public static void main(String argv[]) {
    int baz = 3;
    int bar = 2;
    int[] foo = new int[] { 1, baz, 2, bar};
    System.out.println(foo.length);
    if (MemoryArea.getMemoryArea(foo) instanceof HeapMemory) {
      System.out.println("Hooray!");
      if ((foo[0]==1)&&(foo[1]==3)&&(foo[2]==2)&&(foo[3]==2)) {
	System.out.println("cool!");
      } else {
	System.out.println("Not so cool");
      }
    } else {
      System.out.println("whoops!");
    }

    System.out.println(MemoryArea.getMemoryArea(foo).toString());
    Thread t = new Thread();
    Hello2 hello2 = new Hello2();
  }
    
  public Hello2() {
    System.out.println("Hello world");
    System.out.println(RealtimeThread.currentRealtimeThread().toString());
    if (Thread.currentThread() instanceof RealtimeThread) {
      System.out.println("RealtimeThread");
    } else {
      System.out.println("Thread");
    }
  }
}


