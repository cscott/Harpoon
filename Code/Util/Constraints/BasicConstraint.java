// BasicConstraint.java, created Mon Apr  8 10:59:19 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Constraints;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;

import harpoon.Util.Util;

/** <code>BasicConstraint</code> models the simplest possible
    inclusion constraint:
    &quot;set of atoms <code>atoms</code> \subseteq of <code>v</code>.
    
    @author  Alexandru SALCIANU <salcianu@MIT.EDU>
    @version $Id: BasicConstraint.java,v 1.1 2002-04-11 04:25:19 salcianu Exp $ */
public class BasicConstraint extends Constraint {

    private Set atoms;
    private Var v;
    
    /** Creates a <code>BasicConstraint</code>. */
    public BasicConstraint(Collection atoms, Var v) {
        this.atoms = new HashSet(atoms);
	this.v = v;
	in_dep_array  = new Var[0];
	out_dep_array = new Var[]{v};
    }

    /** Uses the partial solution accesser to propagate the set
	<code>atoms</vcode> to the variable <code>v</code>. */
    public void action(PSolAccesser sacc) {
	sacc.updateSet(v, atoms);
    }

    public Constraint convert(Map m) {
	return new BasicConstraint(atoms, (Var) Util.convert(v, m));
    }

    public String toString() {
	return " BC: " + atoms + " \\subseteq " + v;
    }
    
}
