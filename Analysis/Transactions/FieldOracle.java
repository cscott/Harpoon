// FieldOracle.java, created Wed Nov 15 15:57:40 2000 by cananian
// Copyright (C) 2000  <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.HField;

/**
 * A <code>FieldOracle</code> helps the <code>SyncTransformer</code>
 * decide which field references it can skip checks for.
 * 
 * @author   <cananian@alumni.princeton.edu>
 * @version $Id: FieldOracle.java,v 1.1.2.1 2001-01-11 20:26:40 cananian Exp $
 */
abstract class FieldOracle {
    /** Returns <code>true</code> if this field can be read within a
        synchronized context. */
    public abstract boolean isSyncRead(HField hf);
    /** Returns <code>true</code> if this field can be written within a
        synchronized context. */
    public abstract boolean isSyncWrite(HField hf);
    /** Returns <code>true</code> if this field can be read outside a
        synchronized context. */
    public abstract boolean isUnsyncRead(HField hf);
    /** Returns <code>true</code> if this field can be written outside a
        synchronized context. */
    public abstract boolean isUnsyncWrite(HField hf);
}
