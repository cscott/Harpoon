class ToyArray {
    public static void main(String[] args) {
	int asize=0;
	int repeats=0;
	boolean RTJ = false;
	boolean stats = false;
	boolean noheap = false;
	javax.realtime.MemoryArea ma = null;
	javax.realtime.MemoryArea mb = null;
	try {
	    asize=Integer.parseInt(args[0]);
	    repeats=Integer.parseInt(args[1]);
	    if (RTJ=!args[2].equalsIgnoreCase("noRTJ")) {
		noheap = (args.length >= 6) && args[5].equalsIgnoreCase("noheap");
		if (args[2].equalsIgnoreCase("CT")) {
		    if (noheap) {
			javax.realtime.ImmortalMemory im = 
			    javax.realtime.ImmortalMemory.instance();
			Class[] params = new Class[] { long.class };
			Object[] vals = new Object[] { new Long(args[3]) };
			Class cls = javax.realtime.CTMemory.class;
			ma = (javax.realtime.MemoryArea)
			    im.newInstance(cls, params, vals);
			mb = (javax.realtime.MemoryArea)
			    im.newInstance(cls, params, vals);
		    } else {
			ma = new javax.realtime.CTMemory(Long.parseLong(args[3]));
			mb = new javax.realtime.CTMemory(Long.parseLong(args[3]));
		    }
		} else if (args[2].equalsIgnoreCase("VT")) {
		    if (noheap) {
			javax.realtime.ImmortalMemory im =
			    javax.realtime.ImmortalMemory.instance();
			ma = (javax.realtime.MemoryArea)
			    im.newInstance(javax.realtime.VTMemory.class);
			mb = (javax.realtime.MemoryArea)
			    im.newInstance(javax.realtime.VTMemory.class);
		    } else {
			ma = new javax.realtime.VTMemory();
			mb = new javax.realtime.VTMemory();
		    }
		} else {
		    throw new Exception();
		}
		stats = (args.length >= 5) && args[4].equalsIgnoreCase("stats");
	    }
	} catch (Exception e) {
	    System.out.println("Toy Array <array size> <repeats> <noRTJ | CT | VT> "+
			       "[ctsize] [stats | nostats] [heap | noheap]");
	    System.exit(-1);
	}
	
	long start;
	if (RTJ) {
	    if (noheap) {
		javax.realtime.ImmortalMemory im = 
		    javax.realtime.ImmortalMemory.instance();
		Class[] params = new Class[] { javax.realtime.MemoryArea.class, 
					       int.class, int.class};
		Object[] vals = new Object[] { ma, new Integer(asize), 
					       new Integer(repeats) };
		Class cls = ToyArrayNoHeap.class;
		/* To make sure the correct roots are generated! */
		ToyArrayNoHeap ta = (params == null)?
		    new ToyArrayNoHeap(ma, asize, repeats):null;
		ToyArrayNoHeap tb = null;
		try {
		    ta = (ToyArrayNoHeap)im.newInstance(cls, params, vals);
		    tb = (ToyArrayNoHeap)im.newInstance(cls, params, vals);
		} catch (Exception e) {
		    System.out.println(e);
		    System.exit(-1);
		}
		start=System.currentTimeMillis();
		ta.start();
		tb.start();
		try {
		    ta.join();
		    tb.join();
		} catch (Exception e) {System.out.println(e);}
	    } else {
		ToyArrayRTJ ta=new ToyArrayRTJ(ma, asize, repeats);
		ToyArrayRTJ tb=new ToyArrayRTJ(mb, asize, repeats);
		start=System.currentTimeMillis();
		ta.start();
		tb.start();
		try {
		    ta.join();
		    tb.join();
		} catch (Exception e) {System.out.println(e);}
	    }
	} else {
	    ToyArrayNoRTJ ta=new ToyArrayNoRTJ(asize, repeats);
	    ToyArrayNoRTJ tb=new ToyArrayNoRTJ(asize, repeats);
	    start=System.currentTimeMillis();
	    ta.start();
	    tb.start();
	    try {
		ta.join();
		tb.join();
	    } catch (Exception e) {System.out.println(e);}
	}

	long end=System.currentTimeMillis();
	System.out.println("Elapsed time(mS): "+(end-start));
	if (stats) {
	    javax.realtime.Stats.print();
	}
    }
}

class ToyArrayRTJ extends javax.realtime.RealtimeThread {
    public ToyArrayRTJ(javax.realtime.MemoryArea ma, int size, int repeat) {
	super(ma);
	this.size=size;
	this.repeat=repeat;
    }

    int size;
    int repeat;

    public void run() {
	Object[] a=new Object[size];
	Object[] b=new Object[size];
	Object ao=new Object();
	Object bo=new Object();
	for (int i=0;i<size;i++) {
	    a[i]=ao;
	    b[i]=bo;
	}
	for (int j=0;j<repeat;j++)
	    for (int i=0;i<size;i++) {
		Object t=a[i];
		a[i]=b[i];
		b[i]=t;
	    }
    }
}

class ToyArrayNoHeap extends javax.realtime.NoHeapRealtimeThread {
    public ToyArrayNoHeap(javax.realtime.MemoryArea ma, int size, int repeat) {
	super(ma);
	this.size=size;
	this.repeat=repeat;
    }

    int size;
    int repeat;

    public void run() {
	Object[] a=new Object[size];
	Object[] b=new Object[size];
	Object ao=new Object();
	Object bo=new Object();
	for (int i=0;i<size;i++) {
	    a[i]=ao;
	    b[i]=bo;
	}
	for (int j=0;j<repeat;j++)
	    for (int i=0;i<size;i++) {
		Object t=a[i];
		a[i]=b[i];
		b[i]=t;
	    }
    }
}


class ToyArrayNoRTJ extends Thread {
    public ToyArrayNoRTJ(int size, int repeat) {
	super();
	this.size=size;
	this.repeat=repeat;
    }

    int size;
    int repeat;

    public void run() {
	Object[] a=new Object[size];
	Object[] b=new Object[size];
	Object ao=new Object();
	Object bo=new Object();
	for (int i=0;i<size;i++) {
	    a[i]=ao;
	    b[i]=bo;
	}
	for (int j=0;j<repeat;j++)
	    for (int i=0;i<size;i++) {
		Object t=a[i];
		a[i]=b[i];
		b[i]=t;
	    }
    }
}
