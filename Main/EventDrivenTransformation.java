// EventDrivenTransformation.java, created Sat Apr 12 17:18:22 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.Analysis.Quads.QuadClassHierarchy;

import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;

import harpoon.Analysis.EventDriven.EventDriven;

import harpoon.Util.Options.Option;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

/**
 * <code>EventDrivenTransformation</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: EventDrivenTransformation.java,v 1.1 2003-04-17 00:19:29 salcianu Exp $
 */
public abstract class EventDrivenTransformation {
    
    static boolean EVENTDRIVEN = false;
    private static MetaCallGraph mcg = null;
    private static boolean recycle = false;
    private static boolean optimistic = false;

    private static boolean _enabled() { return EVENTDRIVEN; }

    private static List/*<Option>*/ getOptions() {
	List/*<Option>*/ opts = new LinkedList/*<Option>*/();
	opts.add(new Option("E", "Event Driven transformation") {
	    public void action() { EVENTDRIVEN = true; }
	});
	opts.add(new Option("p", "Optimistic option for EventDriven") {
	    public void action() { optimistic = true; }
	});
	opts.add(new Option("f",
			    "Recycle option for EventDriven. " + 
			    "Environmentally (f)riendly") {
	    public void action() { recycle = true; }
	});
	return opts;
    }

    public static class QuadPass1 extends CompilerStageEZ {
	public QuadPass1() { super("event-driven-quad-pass-1"); }
	protected boolean enabled() { return _enabled(); }

	// Pass 1 returns the options for the entire transformation
	public List/*<Option>*/ getOptions() {
	    return EventDrivenTransformation.getOptions();
	}

	protected void real_action() {
	    hcf = harpoon.IR.Quads.QuadNoSSA.codeFactory(hcf);
	    Set mroots = 
		extract_method_roots
		(frame.getRuntime().runtimeCallableMethods());
	    mroots.add(mainM);
	    mcg = new MetaCallGraphImpl
		(new CachingCodeFactory(hcf), linker, classHierarchy, mroots);
	}

	// extract the method roots from the set of all the roots
	// (methods and classes)
	private Set extract_method_roots(Collection roots){
	    Set mroots = new HashSet();
	    for(Iterator it = roots.iterator(); it.hasNext(); ){
		Object obj = it.next();
		if(obj instanceof HMethod)
		    mroots.add(obj);
	    }
	    return mroots;
	}
    };

    
    public static class QuadPass2 extends CompilerStageEZ {
	public QuadPass2() { super("event-driven-quad-pass-2"); }
	protected boolean enabled() { return _enabled(); }

	protected void real_action() {
	    if (!SAMain.OPTIMIZE) {
		hcf = harpoon.IR.Quads.QuadSSI.codeFactory(hcf); 
	    }
	    hcf = new CachingCodeFactory(hcf, true);
	    HCode hc = hcf.convert(mainM);
	    EventDriven ed = 
		new EventDriven((CachingCodeFactory) hcf, hc,
				classHierarchy, linker, optimistic, recycle);
	    mainM=ed.convert(mcg);
	    mcg=null; /*Memory management*/
	    hcf = new CachingCodeFactory(hcf);
	    Set eroots = new java.util.HashSet
		(frame.getRuntime().runtimeCallableMethods());
	    // and our main method is a root, too...
	    eroots.add(mainM);
	    hcf = new CachingCodeFactory(hcf);
	    classHierarchy = new QuadClassHierarchy(linker, eroots, hcf);
	}
    };
    
}
