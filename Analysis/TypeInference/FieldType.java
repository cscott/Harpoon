// FieldType.java, created Fri Nov 20 21:19:26 1998 by marinov
package harpoon.Analysis.TypeInference;

import java.util.Enumeration;
import harpoon.Util.Set;
import harpoon.ClassFile.*;
/**
 * <code>FieldType</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: FieldType.java,v 1.1.2.1 1998-12-02 08:08:32 marinov Exp $
 */

public class FieldType  {
    SetHClass type = new SetHClass();
    Set callees = new Set();

    /** Creates a <code>FieldType</code>. */
    public FieldType() { }
   
    SetHClass getType () { return type.copy(); }
    boolean union(SetHClass s) { return type.union(s); }
    void addCallee(Object i) { callees.union(i); }
    Enumeration getCallees() { return callees.elements(); }
}
