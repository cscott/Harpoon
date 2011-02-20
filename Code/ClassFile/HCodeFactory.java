// HCodeFactory.java, created Sat Sep 12 18:13:40 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * An <code>HCodeFactory</code> makes an <code>HCode</code> from an
 * <code>HMethod</code>. For example, an 
 * <code>HCodeFactory</code> may make an <code>harpoon.IR.Quads.QuadSSI</code>
 * from a <code>harpoon.IR.Bytecode.Code</code>, calling
 * <code>HMethod.getCode("bytecode")</code> to get the source representation
 * for the conversion.  The <code>HCodeFactory</code> should call 
 * <code>HMethod.putCode(this.getCodeName())</code> after conversion
 * to cache the result.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HCodeFactory.java,v 1.5 2002-02-25 21:03:03 cananian Exp $
 */

public interface HCodeFactory  {
    /** Make an <code>HCode</code> from an <code>HMethod</code>.
     *  <p>
     *  <code>convert</code> is allowed to return null if the requested
     *  conversion is impossible; typically this is because it's attempt
     *  to convert a source representation failed -- for
     *  example, because <code>m</code> is a native method.
     */
    public HCode convert(HMethod m);
    /** Returns a string naming the type of the <code>HCode</code>
     *  that this factory produces.  <p>
     *  <code>this.getCodeName()</code> should equal
     *  <code>this.convert(m).getName()</code> for every 
     *  <code>HMethod m</code>. */
    public String getCodeName();
    /**
     * Removes representation of method <code>m</code> from all caches
     * in this factory and its parents.
     */
    public void clear(HMethod m);
}
