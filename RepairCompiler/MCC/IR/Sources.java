package MCC.IR;

import MCC.State;

public class Sources {
    State state;

    public Sources(State s) {
	this.state=s;
    }

    public boolean setSource(SetDescriptor sd) {
	return false;
    }
    public boolean allocSource(SetDescriptor sd) {
	return true;
    }
    public SetDescriptor getSourceSet(SetDescriptor sd) {
	return null;
    }
    public void generateSourceAlloc(CodeWriter cr,VarDescriptor vd, SetDescriptor sd) {
	TypeDescriptor td=sd.getType();
	Expr e=td.getSizeExpr();
	VarDescriptor size=VarDescriptor.makeNew("size");
	e.generate(cr, size);
	cr.outputline(td.getGenerateType().getSafeSymbol()+" "+vd.getSafeSymbol()+"=("+td.getGenerateType().getSafeSymbol()+") malloc("+size.getSafeSymbol()+");");
    }

    public boolean relsetSource(RelationDescriptor rd, boolean domain) {
	return false;
    }
    public boolean relallocSource(RelationDescriptor rd, boolean domain) {
	return true;
    }
    public SetDescriptor relgetSourceSet(RelationDescriptor rd, boolean domain) {
	return null;
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
	e.generate(cr, size);
	cr.outputline(td.getGenerateType().getSafeSymbol()+" "+vd.getSafeSymbol()+"=("+td.getGenerateType().getSafeSymbol()+") malloc("+size.getSafeSymbol()+");");
    }

}
