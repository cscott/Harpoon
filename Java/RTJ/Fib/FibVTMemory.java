import javax.realtime.RealtimeThread;
import javax.realtime.VTMemory;

class FibVTMemory { 
  public static void main(String args[]) {
    int i = Integer.parseInt(args[0]);
    System.out.println("fib("+i+")"); 

    Compute2VTMemory c2 = new Compute2VTMemory();
    c2.i = new Integer(i);

    VTMemory scope = new VTMemory(1000000, 1000000);
    scope.enter(c2);
  }
}

class Compute2VTMemory implements Runnable {
  Integer i;

  public void run() {
    RealtimeThread foo = new RealtimeThread();
    ComputeVTMemory c = new ComputeVTMemory(i);
    c.start();
    try {
      c.join();
    } catch (Exception e) { System.out.println(e); }
    System.out.println("fib("+i.toString()+")="+c.target.toString());
  }
}

class ComputeVTMemory extends RealtimeThread { 
  Integer source;
  Integer target;

  ComputeVTMemory(Integer s) {
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
      ComputeVTMemory c1 = new ComputeVTMemory(new Integer(v-1));
      ComputeVTMemory c2 = new ComputeVTMemory(new Integer(v-2));
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
