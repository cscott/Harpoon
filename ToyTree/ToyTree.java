class ToyTree {
    public static void main(String [] args) {
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
	    System.out.println("Toy Tree <tree depth> <repeats> <noRTJ | CT | VT> [stats | nostats] [ctsize]\n");
	    System.exit(-1);
	}
	
	long start=System.currentTimeMillis();
	if (RTJ) {
	    ToyTreeRTJ ta=new ToyTreeRTJ(ma,asize,repeats);
	    ToyTreeRTJ tb=new ToyTreeRTJ(mb,asize,repeats);
	    ta.start();
	    tb.start();
	    try {
		ta.join();
		tb.join();
	    } catch (Exception e) {System.out.println(e);}
	} else {
	    ToyTreeNoRTJ ta=new ToyTreeNoRTJ(asize,repeats);
	    ToyTreeNoRTJ tb=new ToyTreeNoRTJ(asize,repeats);
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
