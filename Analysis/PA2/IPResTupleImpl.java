// IPResTupleImpl.java, created Thu Jul 14 09:58:31 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;

/**
 * <code>IPResTupleImpl</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: IPResTupleImpl.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $
 */
class IPResTupleImpl extends InterProcAnalysisResult {
    public IPResTupleImpl(PAEdgeSet eomI,
			  PAEdgeSet eomO,
			  Set<PANode> eomDirGblEsc,
			  Set<PANode> eomAllGblEsc,
			  Set<PANode> ret, Set<PANode> ex) {
	this.eomI = eomI;
	this.eomO = eomO;
	this.eomDirGblEsc = eomDirGblEsc;
	this.eomAllGblEsc = eomAllGblEsc;
	this.ret  = ret;
	this.ex   = ex;
    }
    private final PAEdgeSet eomI;
    private final PAEdgeSet eomO;
    private final Set<PANode> eomDirGblEsc;
    private final Set<PANode> eomAllGblEsc;
    private final Set<PANode> ret;
    private final Set<PANode> ex;
    
    public PAEdgeSet eomI()              { return eomI; }
    public PAEdgeSet eomO()              { return eomO; }
    public Set<PANode> eomDirGblEsc()    { return eomDirGblEsc; }
    public Set<PANode> eomAllGblEsc()    { return eomAllGblEsc; }
    public Set<PANode> ret()             { return ret; }
    public Set<PANode> ex()              { return ex; }
}
