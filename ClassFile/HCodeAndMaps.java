// HCodeAndMaps.java, created Fri Oct  6 12:01:54 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Temp.TempMap;
import java.util.Map;
/**
 * <code>HCodeAndMaps</code> is a strongly-typed tuple representing
 * all the derivation information for a cloned <code>HCode</code>.
 * This includes the mappings from old <code>HCodeElement</code>s
 * and <code>Temp</code>s to new <code>HCodeElement</code>s and
 * <code>Temp</code>s as well as the identity of both the old
 * <code>HCode</code> and newly cloned <code>HCode</code>.
 * It is intended to make 'clone-then-mutate' operations more
 * straight-forward to write.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCodeAndMaps.java,v 1.1.2.1 2000-10-06 21:20:00 cananian Exp $
 */
public abstract class HCodeAndMaps {
    /** Returns the newly-cloned <code>HCode</code>. */
    public abstract HCode hcode();
    /** An immutable mapping from ancestor <code>HCodeElement</code>s
     *  to newly-cloned <code>HCodeElement</code>s. */
    public abstract Map elementMap();
    /** An immutable mapping from ancestor <code>Temp</code>s to
     *  newly-cloned <code>Temp</code>s. */
    public abstract TempMap tempMap();

    /** Returns the original <code>HCode</code> that the clone returned
     *  by the <code>hcode()</code> method was copied from. */
    public abstract HCode ancestorHCode();
    /** An immutable mapping from newly-cloned <code>HCodeElement</code>s
     *  to ancestor <code>HCodeElement</code>s. */
    public abstract Map ancestorElementMap();
    /** An immutable mapping from newly-cloned <code>Temp</code>s to
     *  ancestor <code>Temp</code>s. */
    public abstract TempMap ancestorTempMap();
}
