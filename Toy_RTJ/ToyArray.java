class ToyArray {
    public static void main(String[] args) {
	int asize=0;
	int repeats=0;
	boolean RTJ = false;
	boolean stats = false;
	javax.realtime.MemoryArea ma = null;
	javax.realtime.MemoryArea mb = null;
	try {
	    asize=Integer.parseInt(args[0]);
	    repeats=Integer.parseInt(args[1]);
	    if (RTJ=!args[2].equalsIgnoreCase("noRTJ")) {
		if (args[2].equalsIgnoreCase("CT")) {
		    ma = new javax.realtime.CTMemory(Long.parseLong(args[4]));
		    mb = new javax.realtime.CTMemory(Long.parseLong(args[4]));
		} else if (args[2].equalsIgnoreCase("VT")) {
		    ma = new javax.realtime.VTMemory(1000, 1000);
		    mb = new javax.realtime.VTMemory(1000, 1000);
		} else {
		    throw new Exception();
		}
		stats = args[3].equalsIgnoreCase("stats");
	    }
	} catch (Exception e) {
	    System.out.println("Toy Array <array size> <repeats> <noRTJ | CT | VT> [stats | nostats] [ctsize]");
	    System.exit(-1);
	}
	
	long start;
	if (RTJ) {
	    ToyArrayRTJ ta=new ToyArrayRTJ(ma, asize, repeats);
	    ToyArrayRTJ tb=new ToyArrayRTJ(mb, asize, repeats);
	    start=System.currentTimeMillis();
	    ta.start();
	    tb.start();
	    try {
		ta.join();
		tb.join();
	    } catch (Exception e) {System.out.println(e);}
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
