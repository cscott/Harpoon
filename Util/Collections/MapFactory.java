// MapFactory.java, created Tue Oct 19 22:42:28 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Map;

/** <code>MapFactory</code> is a <code>Map</code> generator.
    Subclasses should implement constructions of specific types of
    <code>Map</code>s.
     <p>
    Note also that the current limitations on parametric types in
    Java mean that we can't easily type this class as
    <code>MapFactory&lt;M extends Map&lt;K,V&gt;,K,V&gt;</code>,
    as <code>MapFactory&lt;HashMap&lt;K,V&gt;,K,V&gt;</code> is not
    a subtype of <code>MapFactory&lt;Map&lt;K,V&gt;,K,V&gt;</code>,
    even though <code>HashMap</code> is a subtype of <code>Map</code>.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: MapFactory.java,v 1.2.2.1 2002-02-27 22:24:13 cananian Exp $
 */
public abstract class MapFactory<K,V> {
    
    /** Creates a <code>MapFactory</code>. */
    public MapFactory() {
        
    }

    /** Generates a new, mutable, empty <code>Map</code>. */
    public Map<K,V> makeMap() {
	return this.makeMap(harpoon.Util.Default.EMPTY_MAP);
    }

    /** Generates a new <code>Map</code>, using the entries of
	<code>map</code> as a template for its initial mappings. 
    */
    public abstract Map<K,V> makeMap(Map<K,V> map);

    
}
