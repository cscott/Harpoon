// PASync.java, created Wed Mar 29 12:38:44 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.CALL;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Util.DataStructs.Relation;

/**
 * <code>PASync</code> models a <code>sync</code> action.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PASync.java,v 1.1.2.5 2001-02-27 22:11:12 salcianu Exp $
 */
public class PASync implements java.io.Serializable {
    /** The node on which the <code>sync</code> is performed on. */
    public PANode n;
    /** The thread which performs the <code>sync</code>. */
    public PANode nt;
    /** The instruction which performs the <code>sync</code>. */
    public HCodeElement hce;

    /** The depth of the call-path specialization chain; if 0, the
	<code>sync</code> took place in the current method. */
    public int depth;
    /** The call chain that specializes this action. Its size is equal
	to <code>depth</code>. To cope with the recursive methods, this
	is just the last part of the call-path, containing up to
	<code>PointerAnalysis.MAX_SPEC_DEPTH</code> elements: every chain
	shorter than that limit is exact, the rest are just approximations. */
    public ListCell call_path;

    /** If <code>this</code> action is the thread specialization of another
	action, this is the run method of the thread which does this action. */
    public MetaMethod wtspec_run;

    /** Creates a <code>PASync</code>. */
    public PASync(PANode n, PANode nt, HCodeElement hce) {
	this(n, nt, hce, 0, null, null);
    }

    /** Private constructor. */
    private PASync(PANode n, PANode nt, HCodeElement hce, int depth,
		   ListCell call_path, MetaMethod wtspec_run){
	this.n     = n;
	this.nt    = nt;
	this.hce   = hce;
	this.depth = depth;
	this.call_path  = call_path;
	this.wtspec_run = wtspec_run;
    }

    /** Checks whether <code>node</code> can still be specialized. 
	(is an INSIDE node that is not bottom) */
    private boolean ableToBeSpecialized(PANode node){
	return
	    (node != ActionRepository.THIS_THREAD) &&
	    (node.type == PANode.INSIDE) && !node.isBottom();
    }

    /** Specializes this action for a specific call site. The argument is
	supposed to be a <code>CALL</code>. It is added at the end of the 
	call chain. The new, specialized action is returned.<br>
	<b>Note:</b> If the depth of this action is too big, no specialization
	takes place; instead, <code>this</code> object is returned. */
    public PASync csSpecialize(final Map map, final CALL call){
	if(depth >= PointerAnalysis.MAX_SPEC_DEPTH){
	    if(!(ableToBeSpecialized(n) || ableToBeSpecialized(nt)))
		return this;
	}

	///// System.out.println("DEPTH: " + (depth + 1) + " < sync , " + n +
	/////		   " , " + nt + " >");

	return 
	    new PASync(PANode.translate(n, map),
		       PANode.translate(nt, map),
		       hce,
		       depth + 1,
		       new ListCell(call, call_path),
		       null);
    }

    /** Returns the image of <code>this</code> <code>sync</code> action
	projected through the relation <code>mu</code>. */
    public Set project(Relation mu){
	Set set_n  = mu.getValues(n);
	Set set_nt = mu.getValues(nt);
	if(set_n.isEmpty() || set_nt.isEmpty())
	    return Collections.EMPTY_SET;
	
	// save some space by avoid to allocate the same stuff once again
	if((set_n.size() == 1) && (set_nt.size() == 1) &&
	   set_n.contains(n) && set_nt.contains(nt))
	    return Collections.singleton(this);

	Set retval = new HashSet();

	for(Iterator it_n = set_n.iterator(); it_n.hasNext(); ){
	    PANode new_n = (PANode) it_n.next();
	    for(Iterator it_nt = set_nt.iterator(); it_nt.hasNext(); ){
		PANode new_nt = (PANode) it_nt.next();
		retval.add(new PASync(new_n, new_nt, hce, depth,
				      call_path, wtspec_run));
	    }
	}

	return retval;
    }

    /** Checks whether this action is a calling context specialization of
	some other one. */
    public boolean isCSSpec(){
	return depth != 0;
    }

    /** Does the thread-specialization of <code>this</code> action. */
    public PASync tSpecialize(final Map map, final MetaMethod run){
        return
	    new PASync(PANode.translate(n, map),
		       PANode.translate(nt, map),
		       hce,
		       depth,
		       call_path,
		       run);
    }

    /** Checks whether this action is a thread specialization of some
	other one. */
    public boolean isTSpec(){
	return wtspec_run != null;
    }

    /** Checks the equality of this object with another one. */
    public boolean equals(Object obj){
	if(obj == this) return true;
	PASync s2 = (PASync) obj;
	return
	    n.equals(s2.n) &&
	    nt.equals(s2.nt) &&
	    (depth == s2.depth) &&
	    hce.equals(s2.hce) &&
	    MetaMethod.identical(wtspec_run, s2.wtspec_run) &&
	    ListCell.identical(call_path, s2.call_path);
    }

    /** Cache for the hash code. */
    private int hash = -1;
    public int hashCode(){
	if(hash == -1)
	    // this should be variate enough
	    hash = n.hashCode() + nt.hashCode() + depth + hce.hashCode();
	return hash;
    }

    /** String representation for debug purposes. */
    public String toString(){
	StringBuffer buffer = new StringBuffer();
	buffer.append("< sync, " + n + 
		      (nt != ActionRepository.THIS_THREAD?(", " + nt):"") +
		      " > ");
	if(isTSpec())
	    buffer.append(" <-TS-");
	for(ListCell lc = call_path; lc != null; lc = lc.next)
	    buffer.append(" <- " + (CALL) lc.info + " - ");

	buffer.append(" {" + hce.getSourceFile() + ":" + hce.getLineNumber() +
		      " " + hce + "}");

	return buffer.toString();
    }
}
