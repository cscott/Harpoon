// IIR_PackageDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_PackageDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PackageDeclaration.java,v 1.1 1998-10-10 07:53:39 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PackageDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_PACKAGE_DECLARATION
    //CONSTRUCTOR:
    public IIR_PackageDeclaration() { }
    //METHODS:  
    public IIR_PackageBodyDeclaration get_package_body()
    { return _package_body; }
 
    //MEMBERS:  
    IIR_DeclarationList package_declarative_part;

// PROTECTED:
} // END class

