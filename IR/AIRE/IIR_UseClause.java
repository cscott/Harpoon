// IIR_UseClause.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_UseClause</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_UseClause.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_UseClause extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_USE_CLAUSE
    //CONSTRUCTOR:
    public IIR_UseClause() { }
    
    //METHODS:  
    public void set_selected_name(IIR_Name selected_name)
    { _selected_name = selected_name; }
 
    public IIR_Name get_selected_name()
    { return _selected_name; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_Name _selected_name;
} // END class

