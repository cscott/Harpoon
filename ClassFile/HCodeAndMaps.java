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
 * @version $Id: HCodeAndMaps.java,v 1.2 2002-02-25 21:03:03 cananian Exp $
 */
public final class HCodeAndMaps {
    private final HCode hcode, ancestorHCode;
    private final Map elementMap, ancestorElementMap;
    private final TempMap tempMap, ancestorTempMap;
    /** constructor. */
    public HCodeAndMaps(HCode hcode, Map elementMap, TempMap tempMap,
			HCode ancestorHCode, Map ancestorElementMap,
			TempMap ancestorTempMap) {
	this.hcode=hcode;
	this.elementMap=elementMap;
	this.tempMap=tempMap;
	this.ancestorHCode=ancestorHCode;
	this.ancestorElementMap=ancestorElementMap;
	this.ancestorTempMap=ancestorTempMap;
    }

    /** Returns the newly-cloned <code>HCode</code>. */
    public HCode hcode() { return hcode; }
    /** An immutable mapping from ancestor <code>HCodeElement</code>s
     *  to newly-cloned <code>HCodeElement</code>s. */
    public Map elementMap() { return elementMap; }
    /** An immutable mapping from ancestor <code>Temp</code>s to
     *  newly-cloned <code>Temp</code>s. */
    public TempMap tempMap() { return tempMap; }

    /** Returns the original <code>HCode</code> that the clone returned
     *  by the <code>hcode()</code> method was copied from. */
    public HCode ancestorHCode() { return ancestorHCode; }
    /** An immutable mapping from newly-cloned <code>HCodeElement</code>s
     *  to ancestor <code>HCodeElement</code>s. */
    public Map ancestorElementMap() { return ancestorElementMap; }
    /** An immutable mapping from newly-cloned <code>Temp</code>s to
     *  ancestor <code>Temp</code>s. */
    public TempMap ancestorTempMap() { return ancestorTempMap; }
}
