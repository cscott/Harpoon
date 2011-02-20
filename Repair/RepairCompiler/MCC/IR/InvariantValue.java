package MCC.IR;
import java.util.*;

public class InvariantValue {
    Hashtable maybevalue;
    Hashtable value;

    public InvariantValue() {
	maybevalue=new Hashtable();
	value=new Hashtable();
    }

    void assignPair(Expr e, VarDescriptor val, VarDescriptor maybe) {
	value.put(e,val);
	maybevalue.put(e,maybe);
    }

    VarDescriptor getValue(Expr e) {
	if (value.containsKey(e))
	    return (VarDescriptor)value.get(e);
	throw new Error("No Value");
    }
    
    VarDescriptor getMaybe(Expr e) {
	if (maybevalue.containsKey(e))
	    return (VarDescriptor)maybevalue.get(e);
	throw new Error("No Value");
    }

    boolean isInvariant(Expr e) {
	return maybevalue.containsKey(e);
    }
}
