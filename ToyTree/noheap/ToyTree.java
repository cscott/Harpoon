class ToyTree {
    public static void main(String [] args) {
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
	    System.out.println("Toy Tree <tree depth> <repeats> <noRTJ | CT | VT> [ctsize] [stats | nostats] [noheap]");
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
		Class cls = ToyTreeNoHeap.class;
		/* To make sure the correct roots are generated! */
		ToyTreeNoHeap ta = (params == null)?
		    new ToyTreeNoHeap(ma, asize, repeats):null;
		ToyTreeNoHeap tb = null;
		try {
		    ta = (ToyTreeNoHeap)im.newInstance(cls, params, vals);
		    tb = (ToyTreeNoHeap)im.newInstance(cls, params, vals);
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
		ToyTreeRTJ ta=new ToyTreeRTJ(ma,asize,repeats);
		ToyTreeRTJ tb=new ToyTreeRTJ(mb,asize,repeats);
		start=System.currentTimeMillis();
		ta.start();
		tb.start();
		try {
		    ta.join();
		    tb.join();
		} catch (Exception e) {System.out.println(e);}
	    }
	} else {
	    ToyTreeNoRTJ ta=new ToyTreeNoRTJ(asize,repeats);
	    ToyTreeNoRTJ tb=new ToyTreeNoRTJ(asize,repeats);
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

class ToyTreeNoHeap extends javax.realtime.NoHeapRealtimeThread {
    public ToyTreeNoHeap(javax.realtime.MemoryArea ma, int size, int repeat) {
	super(ma);
	this.size=size;
	this.repeat=repeat;
    }

    int size;
    int repeat;

    static class TreeEle {
	public TreeEle(TreeEle l, TreeEle r) {
	    left=l;
	    right=r;
	}
	TreeEle left;
	TreeEle right;
    }
    
    TreeEle buildtree(int size) {
	if (size==0)
	    return null;
	return new TreeEle(buildtree(size-1),buildtree(size-1));
    }

    void fliptree(TreeEle root) {
	if (root==null)
	    return;
	TreeEle left=root.left;
	TreeEle right=root.right;
	if (left!=null) {
	    TreeEle temp=left.left;
	    left.left=left.right;
	    left.right=temp;
	    fliptree(left.left);
	    fliptree(left.right);

	}
	if (right!=null) {
	    TreeEle temp=right.left;
	    right.left=right.right;
	    right.right=temp;
	    fliptree(right.left);
	    fliptree(right.right);
	}
	root.left=right;
	root.right=left;
    }

    public void run() {
	TreeEle root=buildtree(size);
	for(int i=0;i<repeat;i++)
	    fliptree(root);
    }
}

class ToyTreeRTJ extends javax.realtime.RealtimeThread {
    public ToyTreeRTJ(javax.realtime.MemoryArea ma, int size, int repeat) {
	super(ma);
	this.size=size;
	this.repeat=repeat;
    }

    int size;
    int repeat;

    static class TreeEle {
	public TreeEle(TreeEle l, TreeEle r) {
	    left=l;
	    right=r;
	}
	TreeEle left;
	TreeEle right;
    }
    
    TreeEle buildtree(int size) {
	if (size==0)
	    return null;
	return new TreeEle(buildtree(size-1),buildtree(size-1));
    }

    void fliptree(TreeEle root) {
	if (root==null)
	    return;
	TreeEle left=root.left;
	TreeEle right=root.right;
	if (left!=null) {
	    TreeEle temp=left.left;
	    left.left=left.right;
	    left.right=temp;
	    fliptree(left.left);
	    fliptree(left.right);

	}
	if (right!=null) {
	    TreeEle temp=right.left;
	    right.left=right.right;
	    right.right=temp;
	    fliptree(right.left);
	    fliptree(right.right);
	}
	root.left=right;
	root.right=left;
    }

    public void run() {
	TreeEle root=buildtree(size);
	for(int i=0;i<repeat;i++)
	    fliptree(root);
    }
}

class ToyTreeNoRTJ extends Thread {
    public ToyTreeNoRTJ(int size, int repeat) {
	super();
	this.size=size;
	this.repeat=repeat;
    }

    int size;
    int repeat;

    static class TreeEle {
	public TreeEle(TreeEle l, TreeEle r) {
	    left=l;
	    right=r;
	}
	TreeEle left;
	TreeEle right;
    }

    TreeEle buildtree(int size) {
	if (size==0)
	    return null;
	return new TreeEle(buildtree(size-1),buildtree(size-1));
    }

    void fliptree(TreeEle root) {
	if (root==null)
	    return;
	TreeEle left=root.left;
	TreeEle right=root.right;
	if (left!=null) {
	    TreeEle temp=left.left;
	    left.left=left.right;
	    left.right=temp;
	    fliptree(left.left);
	    fliptree(left.right);

	}
	if (right!=null) {
	    TreeEle temp=right.left;
	    right.left=right.right;
	    right.right=temp;
	    fliptree(right.left);
	    fliptree(right.right);
	}
	root.left=right;
	root.right=left;
    }

    public void run() {
	TreeEle root=buildtree(size);
	for(int i=0;i<repeat;i++)
	    fliptree(root);
    }
}
