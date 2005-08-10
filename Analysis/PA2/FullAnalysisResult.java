// FullAnalysisResult.java, created Tue Jul  5 13:57:06 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;

import harpoon.IR.Quads.Quad;
import harpoon.Temp.Temp;

/**
 * <code>FullAnalysisResult</code> models the full information that
 * pointer analysis was able to compute about an analyzed method.  It
 * extends <code>InterProcAnalysisResult</code> with information about
 * the program points inside the method.
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: FullAnalysisResult.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $ */
public abstract class FullAnalysisResult extends InterProcAnalysisResult {

    /** Inside edges right before <code>q</code> */
    public abstract PAEdgeSet preI(Quad q);

    /** Inside edges right after <code>q</code> */
    public abstract PAEdgeSet postI(Quad q);


    /** Nodes that escape right before <code>q</code> */
    public abstract Set<PANode> preEsc(Quad q);

    /** Nodes that escape right after <code>q</code> */
    public abstract Set<PANode> postEsc(Quad q);


    /** Nodes pointed to by variabke <code>t</code>.  We assume SSA is
        used, so this information is flow-sensitive */
    public abstract Set<PANode> lv(Temp t);

}
