// Regulator.java, created by benster
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

import imagerec.util.CommonMemory;

/**
 * A {@link Regulator} node may be used to halt a thread until receiving a signal
 * through a {@link CommonMemory} variable. The <code>process()</code> method
 * of this class may be instructed to either call <code>super.process()</code>
 * before or after blocking.<br><br>
 *
 * By default, the <code>process()</code> method will block after calling
 * <code>super.process()</code>.
 *
 * @see CommonMemory
 *
 * @author Reuben Sterling <<a href="mailto:benster@mit.edu">benster@mit.edu</a>>
 */
public class Regulator extends Node {
    /**
     * Used to indicate that the <code>process()</code> method
     * should block before calling <code>super.process()</code>.
     */
    public static final int BEFORE = 0;
    /**
     * Used to indicate that the <code>process()</code> method
     * should block after calling <code>super.process()</code>.
     */
    public static final int AFTER = 1;

    /**
     *  Name of the {@link CommonMemory} variable that will be used
     *  to signal when this {@link Regulator} node may cease blocking.
     *
     * @see CommonMemory
     */
    private String goAheadSignal;

    /**
     * Specifies whether the <code>process()</code> method should block
     * before or after calling <code>super.process()</code>.
     */
    private int wait;

    /**
     * Construct a {@link Regulator} node that will wait for signals
     * from the specified {@link CommonMemory} variable.<br><br>
     *
     * By default, the <code>process()</code> method will block after calling
     * <code>super.process()</code>.
     *
     * @param goAheadSignal The name of the {@link CommonMemory} variable
     * that regulates when this node will block.
     *
     * @see CommonMemory
     */
    public Regulator(String goAheadSignal) {
	init(goAheadSignal, AFTER);
    }
    
    /**
     * Construct a {@link Regulator} node that will wait for signals
     * from the specified {@link CommonMemory} variable.<br><br>
     *
     * The the <code>process()</code> method will block before or after calling
     * <code>super.process()</code>, depending on the value of '<code>wait</code>'.
     *
     * @param goAheadSignal The name of the {@link CommonMemory} variable
     * that regulates when this node will block.
     * @param wait Specifies whether the <code>process()</code> method will
     * block before or after calling
     * <code>super.process()</code>.
     *
     * @see CommonMemory
     */
    public Regulator(String goAheadSignal, int wait) {
	init(goAheadSignal, wait);
    }


    /** 
     *  Method should be called by all constructors to initialize object fields.
     */
    private void init(String goAheadSignal, int wait) {
	this.goAheadSignal = goAheadSignal;
	this.wait = wait;
	CommonMemory.setValue(goAheadSignal, new Boolean(false));
    }
    
    
    /**
     * Either blocks before or after calling <code>super.process(id)</code>
     * (depending on what the user specified) and waits for a boolean variable
     * in {@link CommonMemory} to be set to <code>true</code>. After receiving
     * the signal, sets the {@link CommonMemory} variable
     * back to <code>false</code> again.
     *
     * @param id The {@link ImageData} which will be passed on.
     *
     * @see CommonMemory
     */
    public void process(ImageData id) {
	//if (wait == AFTER) {
	//    System.out.println("Regulator #"+getUniqueID()+" processing before.");
	//}
	//else {
	//    System.out.println("Regulator #"+getUniqueID()+" processing after.");	    
	//}

	if (wait == AFTER) {
	    super.process(id);
	    //System.out.println("Regulator #"+getUniqueID()+" done processing");
	}
	
	//System.out.println("Regulator #"+getUniqueID()+" waiting.");
	while (!((Boolean)CommonMemory.getValue(goAheadSignal)).booleanValue()) {
	    try {
		Thread.currentThread().sleep(1);
	    }
	    catch (InterruptedException e) {
	    }
	}
	//System.out.println("Regulator #"+getUniqueID()+" done waiting.");

	CommonMemory.setValue(goAheadSignal, new Boolean(false));
	       
	if (wait == BEFORE) {
	    super.process(id);
	    //System.out.println("Regulator #"+getUniqueID()+" done processing");
	}
    }
}
