import javax.realtime.RealtimeThread;
import javax.realtime.CTMemory;

class FibCTMemory { 
  public static void main(String args[]) {
    int i = Integer.parseInt(args[0]);
    System.out.println("fib("+i+")"); 

    Compute2CTMemory c2 = new Compute2CTMemory();
    c2.i = new Integer(i);

    CTMemory scope = new CTMemory(1000000);
    scope.enter(c2);
  }
}

class Compute2CTMemory implements Runnable {
  Integer i;

  public void run() {
    RealtimeThread foo = new RealtimeThread();
    ComputeCTMemory c = new ComputeCTMemory(i);
    c.start();
    try {
      c.join();
    } catch (Exception e) { System.out.println(e); }
    System.out.println("fib("+i.toString()+")="+c.target.toString());
  }
}

class ComputeCTMemory extends RealtimeThread { 
  Integer source;
  Integer target;

  ComputeCTMemory(Integer s) {
    source = s;
  }
  Integer target() { 
    return target;
  }
  public void run() {
    int v = source.intValue();
    if (v <= 1) { 
      target = new Integer(v);
    } else { 
      ComputeCTMemory c1 = new ComputeCTMemory(new Integer(v-1));
      ComputeCTMemory c2 = new ComputeCTMemory(new Integer(v-2));
      c1.start();
      c2.start();
      try { 
        c1.join();
        c2.join();
      } catch (Exception e) { System.out.println(e); }
      target = new Integer(c1.target().intValue() + c2.target().intValue());
    }
  }
}
