package harpoon.Type.Basic;

/**
 * Parent class for all datatype reprsentations.
 * The Harpoon system treats any subclass of
 * <code>harpoon.Type.Basic.Datatype</code> as a `natural' datatypes.
 * <code>harpoon.Type.Basic.Int</code>, <code>harpoon.Type.Basic.Long</code>, 
 * & etc are the subtypes of <code>harpoon.Type.Basic.Datatype</code> that
 * the Harpoon system uses to represent the JVM `primitive' 
 * datatypes.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Datatype.java,v 1.2 1998-07-31 13:44:47 cananian Exp $
 * @see harpoon.Type.Type
 * @see harpoon.Type.Basic.Int
 * @see harpoon.Type.Basic.Long
 * @see harpoon.Type.Basic.Float
 * @see harpoon.Type.Basic.Double
 */
abstract public class Datatype {
  /**
   * This method is required if you wish to index arrays with
   * this datatype.  You can leave it unimplemented, and it
   * will throw an <code>UnimplementedException</code> if invoked.
   * @exception Exception
   *            if intValue() is not implemented by the sub-class.
   */
  public int intValue() throws Exception {
    throw new UnimplementedException("intValue() not supported.");
  }
}
