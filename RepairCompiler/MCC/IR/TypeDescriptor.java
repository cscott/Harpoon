package MCC.IR;

/**
 * TypeDescriptor 
 *
 * represents types in the language (bit, short, etc. as well as structures)
 */

public abstract class TypeDescriptor extends Descriptor {
    static int counter=0;
    int idnum;
    boolean subclass;


    public int getId() {
	return idnum;
    }

    public TypeDescriptor(String name) {
        super(name);
	if (!(this instanceof MissingTypeDescriptor))
	    idnum=counter++;
	subclass=false;
    }

    public boolean isSubtypeOf(TypeDescriptor td) {
        return false;
    }

    public TypeDescriptor getGenerateType() {
        return this;
    }

    public abstract Expr getSizeExpr();

}
