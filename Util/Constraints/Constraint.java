// Constraint.java, created Sun Apr  7 14:51:59 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Constraints;

import java.util.Map;

/**
 * <code>Constraint</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: Constraint.java,v 1.1 2002-04-11 04:25:19 salcianu Exp $
 */
public abstract class Constraint {
    
    protected Var[] in_dep_array;
    protected Var[] out_dep_array;

    /** Returns an array containing all the in-dependencies, i.e., all
        variables that are read by <code>this</code> constraint. */
    public Var[] in_dep() {
	return in_dep_array;
    }

    /** Returns an array containing all the out-dependencies, i.e., all
        variables that are modified by <code>this</code> constraint. */
    public Var[] out_dep() {
	return out_dep_array;
    }

    /** "Apply" <code>this</code> constraint. In translation: the
	existent partial solution is read and updated through the
	<code>PSolAccesser</code> argument <code>sacc</code>,
	according to the semantics of <code>this</code> constraint. */
    public abstract void action(PSolAccesser sacc);

    /** TODOCUMENT */
    public abstract Constraint convert(Map v);
}
