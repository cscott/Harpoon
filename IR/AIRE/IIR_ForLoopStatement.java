// IIR_ForLoopStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ForLoopStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ForLoopStatement.java,v 1.2 1998-10-10 09:21:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ForLoopStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_FOOR_LOOP_STATEMENT
    //CONSTRUCTOR:
    public IIR_ForLoopStatement() { }
    //METHODS:  
    public void set_iteration_scheme(IIR_ConstantDeclaration iterator)
    { _iteration_scheme = iterator; }
 
    public IIR_ConstantDeclaration get_iteration_scheme()
    { return _iteration_scheme; }
 
    //MEMBERS:  
    public IIR_SequentialStatementList sequence_of_statements;
    public IIR_DeclarationList loop_declarations;

// PROTECTED:
    IIR_ConstantDeclaration _iteration_scheme;
} // END class

