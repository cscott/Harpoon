package MCC.IR;

import MCC.State;

public class Sources {
    State state;

    public Sources(State s) {
	this.state=s;
    }

    public boolean setSource(SetDescriptor sd) {
	if (sd.getSymbol().equals("InodeBitmapBlock"))
	    return true;

	return false;
    }
    public boolean allocSource(SetDescriptor sd) {
	return !setSource(sd);
    }
    public SetDescriptor getSourceSet(SetDescriptor sd) {
	if (sd.getSymbol().equals("InodeBitmapBlock"))
	    return (SetDescriptor)state.stSets.get("FreeBlock");
	return null;
    }
    public void generateSourceAlloc(CodeWriter cr,VarDescriptor vd, SetDescriptor sd) {
	TypeDescriptor td=sd.getType();
	Expr e=td.getSizeExpr();
	VarDescriptor size=VarDescriptor.makeNew("size");
	cr.pushSymbolTable(state.stGlobals);
	e.generate(cr, size);
	cr.popSymbolTable();
	cr.outputline(td.getGenerateType().getSafeSymbol()+" "+vd.getSafeSymbol()+"=("+td.getGenerateType().getSafeSymbol()+") malloc("+size.getSafeSymbol()+");");
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
	TypeDescriptor td=sd.getType();
	Expr e=td.getSizeExpr();
	VarDescriptor size=VarDescriptor.makeNew("size");
	cr.pushSymbolTable(state.stGlobals);
	e.generate(cr, size);
	cr.popSymbolTable();
	cr.outputline(td.getGenerateType().getSafeSymbol()+" "+vd.getSafeSymbol()+"=("+td.getGenerateType().getSafeSymbol()+") malloc("+size.getSafeSymbol()+");");
    }

}
