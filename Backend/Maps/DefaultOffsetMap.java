// DefaultOffsetMap.java, created Sat Jan 16 21:04:18 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

/**
 * <code>DefaultOffsetMap</code> gives offset mappings given a set of
 * field/method numberings.  Right now this is just an abstract skeleton.
 * Someone should fill it in at some point.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefaultOffsetMap.java,v 1.1.2.3 1999-08-04 05:52:27 cananian Exp $
 */
public abstract class DefaultOffsetMap  {
    /** Creates a <code>DefaultOffsetMap</code>. */
    public DefaultOffsetMap(MethodMap class_method_map,
			    MethodMap interface_method_map,
			    ClassDepthMap display_map,
			    FieldMap class_field_map,
			    InlineMap inline_map) {
        // do something intelligent with all these maps.
    }
    
}
