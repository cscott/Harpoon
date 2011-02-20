class FibNoRealtime { 
  public static void main(String args[]) {
    int i = Integer.parseInt(args[0]);
    System.out.println("fib("+i+")"); 

    Compute2 c2 = new Compute2();
    c2.i = new Integer(i);
    c2.run();
  }
}

class Compute2 implements Runnable {
  Integer i;

  public void run() {
    Thread foo = new Thread();
    Compute c = new Compute(i);
    c.start();
    try {
      c.join();
    } catch (Exception e) { System.out.println(e); }
    System.out.println("fib("+i.toString()+")="+c.target.toString());
  }
}

class Compute extends Thread { 
  Integer source;
  Integer target;

  Compute(Integer s) {
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
      Compute c1 = new Compute(new Integer(v-1));
      Compute c2 = new Compute(new Integer(v-2));
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
