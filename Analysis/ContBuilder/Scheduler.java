package harpoon.Analysis.ContBuilder;

//import java.io.NativeIO;

public final class Scheduler
{
    // two lists of IOContinuations, those that are not ready to proceed, as of the last select()
    static Continuation readConts;
    static int nReadConts;
    static Continuation writeConts;
    static int nWriteConts;
	
    // a linked list of Continuations, those that are ready to proceed
    static Continuation ready;
    	
    public static void addRead(IOContinuation c)
    {
	c.setLink(readConts);
	readConts= c;
	nReadConts++;
    }
	
    public static void addWrite(IOContinuation c)
    {
	c.setLink(writeConts);
	writeConts= c;
	nWriteConts++;
    }

    public static void addReady(VoidResultContinuation c)
    {
	c.setLink(ready);
	ready= c;
    }
	
    public static void loop()
    {
	while(true) {
	    // anything ready?
		if (ready != null) {
		    VoidResultContinuation c= (VoidResultContinuation)ready;
		    ready= c.getLink();
		    c.setLink(null);
		    c.resume();
		}
		else
		    // hmm... could it be?
		    if (readConts == null && writeConts == null)
			return;  // I'm done!!!
		    else {
				// pack everything up and do a select
				// upon doing that, move some stuff from r/wConts to ready
				
			doSelect();			
		    }
	}	
    }
	
    private static int[] toFDArray(Continuation list, int n)
    {
	int i, result[]= new int[n];
	IOContinuation c= (IOContinuation)list; 
		
	for (i= 0; i<n; i++) {
	    result[i]= c.getFD().fd;
	    c= (IOContinuation)c.getLink();
	}
		
	return result;
    }


    private static void doSelect()
    {
	// selectJNI returns an array of 1's and 0's of length nReadConts+nWriteConts
	boolean available[]= new boolean[0];//NativeIO.selectJNI( toFDArray(readConts, nReadConts), toFDArray(writeConts, nWriteConts) );
	int i, j;
	Continuation c, prev, tmp;

		// filter the read continuations
	for (i=0, j=0, c= readConts, prev= null; i<nReadConts; i++)
	    if (available[i]) {
		// delete c
		tmp= c.getLink();
		if (prev!= null) prev.setLink(tmp);
		else readConts= tmp;
				
		// insert to readylist				
		addReady((IOContinuation)c);
		j++;
				
		// c is next, prev is unchanged
		c= tmp;
	    } else { prev= c; c= c.getLink(); }
			
	nReadConts-= j;

		// filter the write cotinuations					
	for (j=0, c= writeConts, prev= null; i<nWriteConts; i++)
	    if (available[i]) {

		// delete c
		tmp= c.getLink();
		if (prev!= null) prev.setLink(tmp);
		else writeConts= tmp;
				
		// insert to readylist				
		addReady((IOContinuation)c);
		j++;
				
		c= tmp;
	    } else { prev= c; c= c.getLink(); }
			
	nWriteConts-= j;
    }
}























