// IIR_AssociationElementByExpression.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined class <code>IIR_AssociationElementByExpression</code> 
 * represents wither an association between a formal and an expicit actual
 * expression, or an association between elements of a composite type
 * and their values within an aggregate.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AssociationElementByExpression.java,v 1.2 1998-10-11 00:32:16 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AssociationElementByExpression extends IIR_AssociationElement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ASSOCIATION_ELEMENT_BY_EXPRESSION; }

    /** The constructor initializes an association by expression object
     *  with undefined source location, undefined formal, undefined
     *  actual, undefined next value. */
    public IIR_AssociationElementByExpression() { }

    //METHODS:  
    public void set_actual( IIR actual) {
	_actual = actual;
    }
 
    public IIR get_actual() {
	return _actual;
    }
 
    //MEMBERS:  

// PROTECTED:
    IIR _actual;
} // END class

