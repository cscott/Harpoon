// SerializableCodeFactory.java, created Fri Aug  6 23:09:35 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * A <code>SerializableCodeFactory</code> is an <code>HCodeFactory</code>
 * that implements <code>java.io.Serializable</code>.  This is a
 * convenience interface to make it possible to easily use anonymous inner
 * classes to define serializable code factories.  A code factory is
 * generally serializable if it has no internal fields or state (most
 * code factories are thus serializable). <code>CachingCodeFactory</code>
 * keeps state (the cache of converted methods), so it has to take
 * extra care before it can call itself <code>Serializable</code>.
 * Many simple method-at-a-time optimizing code factories will be
 * serializable, but complicated whole-program optimizations will
 * probably will not be unless special attention is paid.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SerializableCodeFactory.java,v 1.2 2002-02-25 21:03:04 cananian Exp $
 */
public interface SerializableCodeFactory
    extends HCodeFactory, java.io.Serializable {
    /** No new fields or methods. */
}
