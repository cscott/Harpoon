import javax.realtime.RealtimeThread;
import javax.realtime.CTMemory;
class main { 
    public static void main(String args[]) {
	int i = Integer.parseInt(args[0]);
	Fib f = new Fib(i);
	CTMemory scope = new CTMemory(1000000);
	try {
	    scope.enter(f);
	} catch (javax.realtime.ScopedCycleException e) {
	    System.out.println(e);
	    System.exit(1);
	}
    }
}
class Fib implements Runnable {
    int source;
    
    Fib(int i) { source = i; }

    public void run() {
	Task t = new Task(new Integer(source));
	t.start();
	try {
	    t.join();
	} catch (Exception e) { System.out.println(e); }
	System.out.println(t.target().toString());
    }
}
class Task extends RealtimeThread { 
  Integer source;
  Integer target;

  Task(Integer s) {
    source = s;
  }
  Integer target() { 
    return target;
  }
  public void run() {
    int v = source.intValue();
    if (v <= 1) { 
	target = source;
    } else { 
	Task t1 = new Task(new Integer(v-1));
	Task t2 = new Task(new Integer(v-2));
	t1.start();
	t2.start();
	try { 
	    t1.join();
	    t2.join();
	} catch (Exception e) { System.out.println(e); }
	int x = t1.target().intValue();
	int y = t2.target().intValue();
	target = new Integer(x + y);
    }
  }
}
