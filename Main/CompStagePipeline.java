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
 * <code>CompStagePipeline</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: CompStagePipeline.java,v 1.1 2003-04-19 01:05:39 salcianu Exp $
 */
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
    

    public List/*<Option>*/ getOptions() {
	List opts = new LinkedList();
	for(Iterator/*<CompilerStage>*/ it=stages.iterator(); it.hasNext(); ) {
	    CompilerStage stage = (CompilerStage) it.next();
	    opts.addAll(stage.getOptions());
	}
	return opts;
    }


    public CompilerState action(CompilerState cs) {
	for(Iterator/*<CompilerStage>*/ it=stages.iterator(); it.hasNext(); ) {
	    CompilerStage stage = (CompilerStage) it.next();
	    cs = stage.action(cs);
	}
	return cs;
    }

}
