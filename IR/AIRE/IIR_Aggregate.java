// IIR_Aggregate.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Aggregate</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Aggregate.java,v 1.2 1998-10-11 00:32:15 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Aggregate extends IIR_Expression
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_AGGREGATE; }
    //CONSTRUCTOR:
    public IIR_Aggregate() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

