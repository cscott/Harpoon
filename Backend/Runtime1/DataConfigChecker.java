// DataConfigChecker.java, created Thu Oct 25 22:49:32 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.*;
import harpoon.IR.Tree.*;
import harpoon.Temp.*;
import harpoon.Util.*;

import java.util.*;
/**
 * <code>DataConfigChecker</code> outputs some (never used) references
 * which will be unresolved (resulting in linker errors at build-time)
 * unless the runtime configuration matches the flex configuration.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataConfigChecker.java,v 1.1.2.1 2001-10-29 16:42:37 cananian Exp $
 */
public class DataConfigChecker extends Data {
    
    /** Creates a <code>DataConfigChecker</code>. */
    public DataConfigChecker(Frame f, HClass hc) {
	super("config-checker", hc, f);
	// only build one of these; wait until hc is java.lang.Object.
	this.root = (hc==linker.forName("java.lang.Object")) ?
	    build() : null;
    }
    private HDataElement build() {
	List stmlist = new ArrayList(4);
	stmlist.add(new SEGMENT(tf, null, SEGMENT.TEXT));
	stmlist.add(new ALIGN(tf, null, 4)); // word align.
	// output label for 'object padding' using frame information.
	int object_padding = frame.getRuntime().getTreeBuilder().objectSize
	    (linker.forName("java.lang.Object"));
	stmlist.add(_DATUM(new Label
	    ("check_object_padding_should_be_"+object_padding)));
	// label for '--with-pointer-size', using frame info.
	stmlist.add(_DATUM(new Label
	    ("check_with_pointer_size_should_be_"+
	     (frame.pointersAreLong()?8:4))));
	// XXX: missing checks for the following Runtime options:
	// --with-thread-model, --with-gc, --with-clustered_heaps,

	// now emit labels for things in the Runtime.configurationSet.
	for (Iterator it=frame.getRuntime().configurationSet.iterator();
	     it.hasNext(); )
	    stmlist.add(_DATUM(new Label((String)it.next())));

	// done!  fold statement list into nested SEQs and return.
	return (HDataElement) Stm.toStm(stmlist);
    }
}
