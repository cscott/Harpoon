package harpoon.Type;

/** 
 * A simple interface for all objects possessing a static type.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TypedObject.java,v 1.1 1998-07-29 00:56:47 cananian Exp $
 */
public interface TypedObject {
  /** Returns the <code>Type</code> of this object. */
  abstract public Type type();
}
