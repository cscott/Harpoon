import javax.realtime.CTMemory;
import javax.realtime.RealtimeThread;

class ToyTree extends RealtimeThread {
    public static void main(String [] arg) {
	int asize=0;
	int repeats=0;
	try {
	    asize=Integer.parseInt(arg[0]);
	    repeats=Integer.parseInt(arg[1]);
	} catch (Exception e) {
	    System.out.println("Toy Tree <tree depth> <repeats>");
	    System.exit(-1);
	}
	
	ToyTree ta=new ToyTree(asize,repeats);
	ToyTree tb=new ToyTree(asize,repeats);
	long start=System.currentTimeMillis();
	ta.start();
      	tb.start();
	try {
	    ta.join();
	    tb.join();
	} catch (Exception e) {System.out.println(e);}

	long end=System.currentTimeMillis();
	System.out.println("Elapsed time(mS): "+(end-start));


    }

    int size;
    int repeat;
    public ToyTree(int size, int repeat) {
	super(new CTMemory(1000000));
	this.size=size;
	this.repeat=repeat;
    }

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
