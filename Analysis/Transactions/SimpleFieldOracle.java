// SimpleFieldOracle.java, created Wed Nov 15 16:01:25 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.HField;

/**
 * A <code>SimpleFieldOracle</code> provides a valid but extremely
 * simple-minded implementation of <code>FieldOracle</code>.
 * 
 * @author   C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SimpleFieldOracle.java,v 1.1.2.3 2001-06-17 22:31:54 cananian Exp $
 */
class SimpleFieldOracle extends FieldOracle {
    public boolean isSyncRead(HField hf) { return true; }
    public boolean isSyncWrite(HField hf) { return true; }
    public boolean isUnsyncRead(HField hf) { return true; }
    public boolean isUnsyncWrite(HField hf) { return true; }
}
