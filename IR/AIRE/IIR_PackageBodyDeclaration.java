// IIR_PackageBodyDeclaration.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_PackageBodyDeclaration</code> class represents
 * the optional implementation part of a package declaration.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_PackageBodyDeclaration.java,v 1.2 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_PackageBodyDeclaration extends IIR_LibraryUnit
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_PACKAGE_BODY_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_PackageBodyDeclaration() { }
    
    //METHODS:  
    /** The associate method pairs a named package body declaration up
     *  with the corresponding package declaration. */
    public void associate() {
	throw new Error("unimplemented"); // FIXME!
    }
 
    //MEMBERS:  

// PROTECTED:
} // END class

