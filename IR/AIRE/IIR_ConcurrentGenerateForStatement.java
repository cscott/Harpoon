// IIR_ConcurrentGenerateForStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcurrentGenerateForStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentGenerateForStatement.java,v 1.2 1998-10-10 11:05:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentGenerateForStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CONCURRENT_GENERATE_FOR_STATEMENT
    //CONSTRUCTOR:
    public IIR_ConcurrentGenerateForStatement() { }
    //METHODS:  
    public void set_generate_parameter_specification(IIR_ConstantDeclaration generate_parameter_specification)
    { _generate_parameter_specification = generate_parameter_specification; }
 
    public IIR_ConstantDeclaration get_generate_parameter_specification()
    { return _generate_parameter_specification; }
 
    //MEMBERS:  
    public IIR_DeclarationList block_declarative_part;
    public IIR_ConcurrentStatementList concurrent_statement_part;

// PROTECTED:
    IIR_ConstantDeclaration _generate_parameter_specification;
} // END class

