// IIR_Aggregate.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Aggregate</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Aggregate.java,v 1.3 1998-10-11 01:24:53 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Aggregate extends IIR_Expression
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_AGGREGATE).
     * @return <code>IR_Kind.IR_AGGREGATE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_AGGREGATE; }
    //CONSTRUCTOR:
    public IIR_Aggregate() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

