// CompStagePipeline.java, created Fri Apr 18 15:05:45 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.Util.Options.Option;

import harpoon.Util.CombineIterator;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Arrays;

/**
 * <code>CompStagePipeline</code> is a special
 * <code>CompilerStage</code> that is the sequential composition of a
 * list of <code>CompilerStage</code>s.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: CompStagePipeline.java,v 1.3 2003-04-30 20:04:40 salcianu Exp $ */
public class CompStagePipeline extends CompilerStage {
    
    /** Creates a <code>CompStagePipeline</code>. */
    public CompStagePipeline(List/*<CompilerStage*/ stages, String name) {
	super(name);
        this.stages = stages;
    }

    public CompStagePipeline(List/*<CompilerStage*/ stages) {
	this(stages, build_name(stages));
    }

    public CompStagePipeline(CompilerStage s1, CompilerStage s2, String name) {
	this(Arrays.asList(new CompilerStage[]{s1, s2}), name);
    }

    public CompStagePipeline(CompilerStage s1, CompilerStage s2) {
	this(Arrays.asList(new CompilerStage[]{s1, s2}));
    }


    private static String build_name(List/*<CompilerStage>*/ stages) {
	StringBuffer name = new StringBuffer("(");
	boolean first = true;
	for(Iterator/*<CompilerStage>*/ it=stages.iterator(); it.hasNext(); ) {
	    CompilerStage stage = (CompilerStage) it.next();
	    if(!first) name.append(",");
	    first = false;
	    name.append(stage.name());
	}
	name.append(")");
	return name.toString();
    }


    private final List/*<CompilerStage*/ stages;

    /** @return <code>count</code>th CompilerStage from this pipeline. */
    protected CompilerStage getStage(int count) { 
	return (CompilerStage) stages.get(count);
    }

    public List/*<Option>*/ getOptions() {
	List opts = new LinkedList();
	for(Iterator/*<CompilerStage>*/ it=stages.iterator(); it.hasNext(); ) {
	    CompilerStage stage = (CompilerStage) it.next();
	    opts.addAll(stage.getOptions());
	}
	return opts;
    }

    public boolean enabled() { return true; }

    public final CompilerState action(CompilerState cs) {
	for(Iterator/*<CompilerStage>*/ it=stages.iterator(); it.hasNext(); ) {
	    CompilerStage stage = (CompilerStage) it.next();
	    if(stage.enabled())
		cs = stage.action(cs);
	}
	return cs;
    }

}
