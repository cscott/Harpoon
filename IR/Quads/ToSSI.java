// ToSSI.java, created Wed Mar 31 18:23:34 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.SESE;
import harpoon.ClassFile.HCode;
/**
 * The <code>ToSSI</code> class places phi and sigma functions and
 * renames variables to convert to SSI form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ToSSI.java,v 1.1.2.1 1999-04-03 18:04:43 cananian Exp $
 */
public class ToSSI  {
    
    /** Creates a <code>ToSSI</code>. */
    public ToSSI() {
    }

    private void place(HCode hc) {
	SESE sese = new SESE(hc, true);
    }
    private void rename() {
    }
    private void prune() {
    }
}
