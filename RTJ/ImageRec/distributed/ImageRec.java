package imagerec;

import javax.realtime.*;

public class ImageRec {
    public static RobertsCross robertsCross = null;
    public static SearchThreshold searchThreshold = null;
    public static Hysteresis hysteresis = null;
    public static Thinning thinning = null;
    public static long[] time;

    public ImageRec() {}

    public static void process(String infile, int frame, String outfile) {
	long begin = System.currentTimeMillis();
	ImageData id = ImageDataManip.readPPM(infile+"."+frame);
	time[0]+=System.currentTimeMillis()-begin;
	
	begin = System.currentTimeMillis();
	id = robertsCross.transform(id);
	time[1]+=System.currentTimeMillis()-begin;
	
	begin = System.currentTimeMillis();
	id = searchThreshold.transform(id);
	time[2]+=System.currentTimeMillis()-begin;
	
	begin = System.currentTimeMillis();
	id = hysteresis.transform(id);
	time[3]+=System.currentTimeMillis()-begin;
	
	begin = System.currentTimeMillis();
	id = thinning.transform(id);
	time[4]+=System.currentTimeMillis()-begin;
	
	begin = System.currentTimeMillis();
	ImageDataManip.writePPM(id, outfile+"."+frame);
	time[5]+=System.currentTimeMillis()-begin;
    }

    public static void main(String args[]) {
	boolean RTJ = false;
	boolean stats = false;
	boolean noheap = false;
	MemoryArea ma = null;
	MemoryArea mb = null;

	try {
	    if (RTJ=!args[3].equalsIgnoreCase("noRTJ")) {
		noheap = (args.length >= 7) && args[6].equalsIgnoreCase("noheap");
		if (args[3].equalsIgnoreCase("CT")) {
		    if (noheap) {
			ImmortalMemory im = ImmortalMemory.instance();
			Class[] params = new Class[] { long.class };
			Object[] vals = new Object[] { new Long(args[4]) };
			Class cls = CTMemory.class;
			ma = (MemoryArea)im.newInstance(cls, params, vals);
			mb = (MemoryArea)im.newInstance(cls, params, vals);
			    
		    } else {
			ma = new CTMemory(Long.parseLong(args[4]));
			mb = new CTMemory(Long.parseLong(args[4]));
		    }
		} else if (args[3].equalsIgnoreCase("VT")) {
		    if (noheap) {
			ImmortalMemory im = ImmortalMemory.instance();
			Class cls = VTMemory.class;
			ma = (MemoryArea)im.newInstance(cls);
			mb = (MemoryArea)im.newInstance(cls);
		    } else {
			ma = new VTMemory();
			mb = new VTMemory();
		    }
		} else {
		    throw new Exception();
		}
	    }
	    stats = (args.length >= 6) && args[5].equalsIgnoreCase("stats");
	} catch (Exception e) {
	    System.out.print("jaco ImageRec <infile> <num> <outfile> <noRTJ | CT | VT> ");
	    System.out.print("[ctsize] [stats | nostats] [heap | noheap]");
	    System.exit(-1);
	}
	
	final String[] finalArgs = args;
	if (noheap) {
	    ImmortalMemory.instance().enter(new Runnable() {
		public void run() {
		    ImageRec.robertsCross = new RobertsCross(finalArgs);
		    ImageRec.searchThreshold = new SearchThreshold(finalArgs);
		    ImageRec.hysteresis = new Hysteresis(finalArgs);
		    ImageRec.thinning = new Thinning(finalArgs);
		    ImageRec.time = new long[6];
		}
	    });
	} else {
	    robertsCross = new RobertsCross(args);
	    searchThreshold = new SearchThreshold(args);
	    hysteresis = new Hysteresis(args);
	    thinning = new Thinning(args);
	    time = new long[6];
	}
	
	final String infile = args[0];
	final String outfile = args[2];
	final boolean finalNoheap = noheap;
	final boolean finalRTJ = RTJ;
	final MemoryArea finalMa = ma;

	for (int i=0; i<Integer.parseInt(args[1]); i++) {
	    final int finalI = i;
	    Runnable r = new Runnable() {
		public void run() {
		    RealtimeThread rt;
		    if (finalNoheap) {
			rt = new NoHeapRealtimeThread(finalMa) {
			    public void run() {
				ImageRec.process(infile, finalI, outfile);
			    }
			};
		    } else if (finalRTJ) {
			rt = new RealtimeThread(finalMa) {
			    public void run() {
				ImageRec.process(infile, finalI, outfile);
			    }
			};
		    } else {
			rt = new RealtimeThread() {
			    public void run() {
				ImageRec.process(infile, finalI, outfile);
			    }
			};
		    }
		    rt.start();
		    try {
			rt.join();
		    } catch (InterruptedException e) {
			System.out.println(e);
			System.exit(-1);
		    }
		}
	    };
	    if (RTJ) {
		mb.enter(r);
	    } else {
		r.run();
	    }
	}

	for (int i=0; i<6; i++) {
	    System.out.println(i+") "+
	      (((double)time[i])/((double)Integer.parseInt(args[1]))));
	}

	if (stats) Stats.print();
    }
}
