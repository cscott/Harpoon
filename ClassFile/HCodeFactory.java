// HCodeFactory.java, created Sat Sep 12 18:13:40 1998 by cananian
package harpoon.ClassFile;

import harpoon.ClassFile.*;
/**
 * An <code>HCodeFactory</code> makes an <code>HCode</code> from an
 * <code>HMethod</code>. For example, an 
 * <code>HCodeFactory</code> may make an <code>harpoon.IR.QuadSSA.Code</code>
 * from a <code>harpoon.ClassFile.Bytecode.Code</code>, calling
 * <code>HMethod.getCode("bytecode")</code> to get the source representation
 * for the conversion.  The <code>HCodeFactory</code> should call 
 * <code>HMethod.putCode(this.getCodeName())</code> after conversion
 * to cache the result.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCodeFactory.java,v 1.1 1998-09-13 23:57:15 cananian Exp $
 */

public interface HCodeFactory  {
    /** Make an <code>HCode</code> from an <code>HMethod</code>.
     *  This method should call the <code>getCode</code> method of
     *  <code>m</code> to get the source representation for the
     *  conversion.  <code>HMethod.getCode()</code> will take
     *  care of properly caching the value <code>convert</code>
     *  returns. */
    public HCode convert(HMethod m);
    /** Returns a string naming the type of the <code>HCode</code>
     *  that this factory produces.  <p>
     *  <code>this.getCodeName()</code> should equal
     *  <code>this.convert(m).getName()</code> for every 
     *  <code>HMethod m</code>. */
    public String getCodeName();
}
