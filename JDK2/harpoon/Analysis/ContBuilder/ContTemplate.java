// ContTemplate.java, created Wed Nov  3 20:17:08 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

import harpoon.Analysis.EnvBuilder.Environment;

/**
 * <code>ContTemplate</code> is a template for continuations.
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: ContTemplate.java,v 1.1 2000-03-15 17:56:03 bdemsky Exp $
 */
public class ContTemplate {
    protected Environment e;
    
    /** Creates a <code>ContTemplate</code>. */
    public ContTemplate(Environment e) {
	this.e = e;
    }
    
    public void resume() {
    }

    public void exception(Throwable t) {
    }

    public Continuation link;

    public void setLink(Continuation newLink) { 
	link= newLink;
    }

    public Continuation getLink() { 
	return link;
    }
}
