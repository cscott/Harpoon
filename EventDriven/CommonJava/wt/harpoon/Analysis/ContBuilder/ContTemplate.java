// ContTemplate.java, created Wed Nov  3 20:17:08 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.ContBuilder;

import harpoon.Analysis.EnvBuilder.Environment;

/**
 * <code>ContTemplate</code> is a template for continuations.
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: ContTemplate.java,v 1.2 2000-03-28 03:43:09 bdemsky Exp $
 */
public class ContTemplate {
    
    /** Creates a <code>ContTemplate</code>. */
    public void resume() {
    }

    public void exception(Throwable t) {
    }

}
