import java.util.*;
import java.io.*;

class EAccumulator { 
  Integer value = null;
  synchronized void add(int i) { 
    int v;
    System.out.print("add " + i + "\n");
    if (value == null) v = 0;
    else v = value.intValue();
    value = new Integer(v+i);
  }
}

class ETask extends Thread { 
  Vector      work;
  EAccumulator dest;
  ETask(Vector w, EAccumulator d) { 
    work = w;
    dest = d;
  }
  public void run() { 
    int sum = 0;
    Enumeration e = work.elements();
    while (e.hasMoreElements()) { 
      sum += ((Integer) e.nextElement()).intValue();
    }
    dest.add(sum);
  }
}

class ESum { 
  void generateTask(int l, int u, EAccumulator a) { 
    Vector v = new Vector();
    for (int j = l; j < u; j++) { 
      v.addElement(new Integer(j));
    }
    ETask t = new ETask(v,a);
    t.start();
  }
  void generate(int n, int m, EAccumulator a) { 
    for (int i = 0; i < n; i ++) { 
      generateTask(i*m, (i+1)*m, a);
    }
  }
}

class ETruc { 
  public static void main(String args[]) { 
    ESum s = new ESum();
    EAccumulator a = new EAccumulator();
    s.generate(100, 10, a);
  }
}
