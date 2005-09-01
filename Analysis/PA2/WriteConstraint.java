// WriteConstraint.java, created Tue Aug 30 11:10:34 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;

import jpaul.Constraints.Var;
import jpaul.Constraints.SolAccessor;
import jpaul.Constraints.Constraint;

import jpaul.DataStructs.Pair;
import jpaul.DataStructs.DSUtil;
import jpaul.Misc.Function;

import harpoon.ClassFile.HField;

/**
 * <code>WriteConstraint</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: WriteConstraint.java,v 1.1 2005-09-01 22:45:21 salcianu Exp $
 */
public class WriteConstraint extends Constraint {

    public WriteConstraint(LVar vd, HField hf, WVar w) {
	this.vd = vd;
	this.hf = hf;
	this.w  = w;
	in  = (vd == null) ? Collections.<Var>emptySet() : Collections.<Var>singleton(vd);
	out = Collections.<Var>singleton(w);
    }

    private final LVar vd;
    private final HField hf;
    private final WVar w;

    private final Collection<Var> in;
    private final Collection<Var> out;
    
    public Collection<Var> in()  { return in; }
    public Collection<Var> out() { return out; }

    public void action(SolAccessor sa) {
	if(vd == null) {
	    Pair<PANode,HField> abstractStaticField = new Pair<PANode,HField>(null, hf);
	    sa.join(w, Collections.singleton(abstractStaticField));
	    return;
	}
	
	Set<PANode> nodes = (Set<PANode>) sa.get(vd);
	if(nodes == null) return;

	Set<Pair<PANode,HField>> abstractFields = DSFactories.abstractFieldSetFactory.create();
	for(PANode node : nodes) {
	    if(PAUtil.isOldAndMutable(node))
		abstractFields.add(new Pair<PANode,HField>(node, hf));
	}

	sa.join(w, abstractFields);
    }

    public String toString() {
	return "writeCons: " + vd + " x {" + hf + "} <= " + w;
    }

}
