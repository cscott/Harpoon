package imagerec;

import javax.realtime.*;

public class ImageRunnable implements Runnable {
    final String infile;
    final String outfile;
    final MemoryArea ma;
    final MemoryArea mb;
    final boolean noheap;
    final boolean RTJ;
    
    public ImageRunnable(String infile, String outfile, MemoryArea ma, MemoryArea mb, boolean noheap, boolean RTJ) {
	this.infile = infile;
	this.outfile = outfile;
	this.ma = ma;
	this.mb = mb;
	this.noheap = noheap;
	this.RTJ = RTJ;
    }

    public Runnable process() {
	return new Runnable() {
	    public ImageData id = null;
	    
	    public void run() {
		try {
		    RealtimeThread rt = new RealtimeThread() {
			public void run() {
			    id = ImageRec.load(infile);
			}
		    };
		    rt.start();
		    rt.join();
		    
		    id = ImageRec.process(id);
		    RealtimeThread rt2;
		    if (RTJ) {
			rt2 = new RealtimeThread(mb) {
			    public void run() {
				ImageRec.save(outfile, id);
			    }
			};
		    } else {
			rt2 = new RealtimeThread() {
			    public void run() {
				ImageRec.save(outfile, id);
			    }
			};
		    }
		    rt2.start();
		    rt2.join();
		} catch (InterruptedException e) {
		    NoHeapRealtimeThread.print(e.toString());
		    System.exit(-1);
		}
	    }
	};
    }

    public void run() {
	RealtimeThread rt;
	if (noheap) {
	    rt = new NoHeapRealtimeThread(ma) {
		public void run() {
		    process().run();
		}
	    };
	} else if (RTJ) {
	    rt = new RealtimeThread(ma) {
		public void run() {
		    process().run();
		}
	    };
	} else {
	    rt = new RealtimeThread() {
		public void run() {
		    process().run();
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
