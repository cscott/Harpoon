// IIR_LibraryClause.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LibraryClause</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LibraryClause.java,v 1.2 1998-10-11 00:32:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LibraryClause extends IIR_Declaration
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_LIBRARY_CLAUSE; }
    //CONSTRUCTOR:
    public IIR_LibraryClause() { }
    //METHODS:  
    public void set_logical_name(IIR_LibraryDeclaration logical_name)
    { _logical_name = logical_name; }
 
    public IIR_LibraryDeclaration get_logical_name()
    { return _logical_name; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_LibraryDeclaration _logical_name;
} // END class

