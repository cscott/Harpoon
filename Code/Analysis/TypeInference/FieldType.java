// FieldType.java, created Fri Nov 20 21:19:26 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.TypeInference;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
/**
 * <code>FieldType</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: FieldType.java,v 1.2 2002-02-25 21:00:36 cananian Exp $
 */

public class FieldType  {
    SetHClass type = new SetHClass();
    Set callees = new HashSet();

    /** Creates a <code>FieldType</code>. */
    public FieldType() { }
   
    SetHClass getType () { return type.copy(); }
    boolean union(SetHClass s) { return type.union(s); }
    void addCallee(Object i) { callees.add(i); }
    Enumeration getCallees() { return Collections.enumeration(callees); }
}
