// IIR_AssociationElementOpen.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_AssociationElementOpen</code> represents
 * either an association between a formal and an implicit actual
 * expression or between the elements of a composite type and the value
 * associated with the specified elements within the aggregate.  The implicit
 * actual value is derived from (1) a delayed binding, (2) an initializer
 * associated with the formal interface declaration, or (3) the
 * (sub)type of the declaration itself.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AssociationElementOpen.java,v 1.2 1998-10-11 00:32:16 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AssociationElementOpen extends IIR_AssociationElement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ASSOCIATION_ELEMENT_OPEN; }

    /** The constructor initializes an association element by open object
     * with an undefined source location, an undefined formal, an undefined
     * actual, and undefined next value. */
    public IIR_AssociationElementOpen() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

