// ImageRec.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.rtj;

import imagerec.graph.*;
import javax.realtime.*;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/**
 *
 *
 */

public class ImageRec {
    public static RobertsCross robertsCross = null;
    public static SearchThreshold searchThreshold = null;
    public static Hysteresis hysteresis = null;
    public static Thinning thinning = null;
    public static long[] time;

    public ImageRec() {}

    public static ImageData load(String infile) {
	long begin = System.currentTimeMillis();
	ImageData id = ImageDataManip.readPPM(infile);
	time[0]+=System.currentTimeMillis()-begin;
	return id;
    }

    public static void save(String outfile, ImageData id) {
	long begin = System.currentTimeMillis();
	ImageDataManip.writePPM(id, outfile);
	time[5]+=System.currentTimeMillis()-begin;
    }

    public static ImageData process(ImageData id) {
	long begin = System.currentTimeMillis();
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
	return id;
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
	    System.out.println("[ctsize] [stats | nostats] [heap | noheap]");
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
	
	try {
	    for (int i=0; i<Integer.parseInt(args[1]); i++) {
		String infile = noheap?(String)ImmortalMemory.instance()
		    .newInstance(String.class,
				 new Class[] { String.class },
				 new Object[] { args[0]+"."+i }):new String(args[0]+"."+i);
		String outfile = noheap?(String)ImmortalMemory.instance()
		    .newInstance(String.class,
				 new Class[] { String.class },
				 new Object[] { args[2]+"."+i }):new String(args[2]+"."+i);	    
		Runnable r = noheap?(ImageRunnable)ImmortalMemory.instance()
		    .newInstance(ImageRunnable.class, 
				 new Class[] { String.class, String.class, MemoryArea.class,
					       MemoryArea.class, boolean.class, boolean.class },
				 new Object[] { infile, outfile, mb, ma,
						new Boolean(noheap), new Boolean(RTJ)}):
		    new ImageRunnable(infile, outfile, mb, ma, noheap, RTJ);
			    
	        if (RTJ) {
		    mb.enter(r);
		} else {
		    r.run();
		}
	    }
	} catch (IllegalAccessException e) {
	    System.out.println(e.toString());
	    System.exit(-1);
	} catch (InstantiationException e) {
	    System.out.println(e.toString());
	    System.exit(-1);
	}
	for (int i=0; i<6; i++) {
	    System.out.println(i+") "+
			       (((double)time[i])/((double)Integer.parseInt(args[1]))));
	}
	
	if (stats) Stats.print();
    }
}
