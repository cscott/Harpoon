// SingularOracle.java, created Sat May  3 19:27:35 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Companions;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Temp;
import java.util.Collection;
import java.util.Set;
/**
 * A <code>SingularOracle</code> provides information about
 * singularity and mutual singularity of static values.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SingularOracle.java,v 1.1 2003-05-07 22:52:07 cananian Exp $
 */
public interface SingularOracle<HCE extends HCodeElement> {
    /** Returns a set of parameters the given static value is
     *  conditionally singular dependent on, or <code>null</code>
     *  if there is no such set (the static value is not singular).
     */
    Set<Temp> conditionallySingular(HMethod m, StaticValue<HCE> sv);
    /** Returns a set of parameters the pair of static values are
     *  conditionally singular dependent on, or <code>null</code>
     *  if there is no such set (the values are not pairwise singular).
     */
    Set<Temp> pairwiseSingular(HMethod m,
			       StaticValue<HCE> sv1, StaticValue<HCE> sv2);
    /** Returns a set of parameters the given static values are
     *  conditionally singular dependent on, or <code>null</code>
     *  if there is no such set (the values are not mutually singular).
     */
    Set<Temp> mutuallySingular(HMethod m, Collection<StaticValue<HCE>> svs);
}
