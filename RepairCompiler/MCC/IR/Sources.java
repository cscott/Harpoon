package MCC.IR;

import MCC.State;
import MCC.Compiler;
import java.util.Iterator;

public class Sources {
    State state;

    public Sources(State s) {
	this.state=s;
    }

    public boolean setSource(SetDescriptor sd) {
	SetDescriptor usedblock=(SetDescriptor)state.stSets.get("UsedBlock");
	SetDescriptor usedinode=(SetDescriptor)state.stSets.get("UsedInode");

	if (usedblock!=null&&usedblock.isSubset(sd))
	    return true;
	if (usedinode!=null&&usedinode.isSubset(sd))
	    return true;

	return false;
    }
    public boolean allocSource(SetDescriptor sd) {
	return !setSource(sd);
    }
    public SetDescriptor getSourceSet(SetDescriptor sd) {
	SetDescriptor usedblock=(SetDescriptor)state.stSets.get("UsedBlock");
	SetDescriptor usedinode=(SetDescriptor)state.stSets.get("UsedInode");

	if (usedblock!=null&&usedblock.isSubset(sd))
	    return (SetDescriptor)state.stSets.get("FreeBlock");
	if (usedinode!=null&&usedinode.isSubset(sd))
	    return (SetDescriptor)state.stSets.get("FreeInode");

	return null;
    }

    public void generateSourceAlloc(CodeWriter cr,VarDescriptor vd, SetDescriptor sd) {
	TypeDescriptor td=sd.getType();
	Expr e=td.getSizeExpr();
	VarDescriptor size=VarDescriptor.makeNew("size");
	cr.pushSymbolTable(state.stGlobals);
	e.generate(cr, size);
	cr.popSymbolTable();
	cr.outputline(td.getGenerateType().getSafeSymbol()+" "+vd.getSafeSymbol()+"=("+td.getGenerateType().getSafeSymbol()+") calloc(1,"+size.getSafeSymbol()+");");
	cr.outputline("alloc("+vd.getSafeSymbol()+","+size.getSafeSymbol()+");");
	if (Compiler.ALLOCATECPLUSPLUS) {
	    if (td instanceof StructureTypeDescriptor) {
		if (((StructureTypeDescriptor)td).size()>0) {
		    FieldDescriptor fd=((StructureTypeDescriptor)td).get(0);
		    if (fd.getSymbol().startsWith("_vptr_")) {
			String vtable="_ZTV";
			vtable+=td.getSymbol().length();
			vtable+=td.getSymbol();
			cr.outputline("((int**) "+vd.getSafeSymbol()+")[0] = (int *) & "+vtable+"+2;");
		    }
		}
	    }
	}
    }

    public boolean relsetSource(RelationDescriptor rd, boolean domain) {
	if (domain)
	    return setSource(rd.getDomain());
	else return setSource(rd.getRange());
    }
    public boolean relallocSource(RelationDescriptor rd, boolean domain) {
	if (domain)
	    return allocSource(rd.getDomain());
	else return allocSource(rd.getRange());
    }
    
    public SetDescriptor relgetSourceSet(RelationDescriptor rd, boolean domain) {
	if (domain)
	    return getSourceSet(rd.getDomain());
	else return getSourceSet(rd.getRange());
    }
    public void relgenerateSourceAlloc(CodeWriter cr,VarDescriptor vd, RelationDescriptor rd, boolean domain) {
	SetDescriptor sd=null;
	if (domain)
	    sd=rd.getDomain();
	else
	    sd=rd.getRange();
	
	generateSourceAlloc(cr, vd, sd);
    }
}
