// IIR_PackageDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_PackageDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PackageDeclaration.java,v 1.3 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PackageDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_PACKAGE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_PackageDeclaration() { }
    //METHODS:  
    public void set_package_body( IIR_PackageBodyDeclaration package_body)
    { _package_body = package_body; }
    public IIR_PackageBodyDeclaration get_package_body()
    { return _package_body; }
 
    //MEMBERS:  
    public IIR_DeclarationList package_declarative_part;

// PROTECTED:
    IIR_PackageBodyDeclaration _package_body;
} // END class

