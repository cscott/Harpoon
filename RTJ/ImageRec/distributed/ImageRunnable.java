package imagerec;

import javax.realtime.*;

public class ImageRunnable implements Runnable {
    final String infile;
    final String outfile;
    final MemoryArea ma;
    final boolean noheap;
    final boolean RTJ;
    
    public ImageRunnable(String infile, String outfile, MemoryArea ma, boolean noheap, boolean RTJ) {
	this.infile = infile;
	this.outfile = outfile;
	this.ma = ma;
	this.noheap = noheap;
	this.RTJ = RTJ;
    }

    public void run() {
	RealtimeThread rt;
	if (noheap) {
	    rt = new NoHeapRealtimeThread(ma) {
		public void run() {
		    (new Runnable() {
			public ImageData id = null;
			
			public void run() {
			    RealtimeThread rt = new RealtimeThread() {
				public void run() {
				    id = ImageRec.load(infile);
				}
			    };
			    rt.start();
			    try { 
				rt.join();
			    } catch (InterruptedException e) {
				NoHeapRealtimeThread.print(e.toString());
				System.exit(-1);
			    }
			    id = ImageRec.process(id);
			    RealtimeThread rt2 = new RealtimeThread() {
				public void run() {
				    ImageRec.save(outfile, id);
				}
			    };
			    rt2.start();
			    try {
				rt2.join();
			    } catch (InterruptedException e) {
				NoHeapRealtimeThread.print(e.toString());
				System.exit(-1);
			    }
			}
		    }).run();
		}
	    };
	} else if (RTJ) {
	    rt = new RealtimeThread(ma) {
		public void run() {
		    (new Runnable() {
			public ImageData id = null;
			
			public void run() {
			    id = ImageRec.load(infile);
			    id = ImageRec.process(id);
			    ImageRec.save(outfile, id);
			}
		    }).run();
		}
	    };
	} else {
	    rt = new RealtimeThread() {
		public void run() {
		    (new Runnable() {
			public ImageData id = null;

			public void run() {
			    id = ImageRec.load(infile);
			    id = ImageRec.process(id);
			    ImageRec.save(outfile, id);
			}
		    }).run();
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
