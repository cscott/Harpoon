package MCC.IR;

import java.util.*;

public class SumExpr extends Expr {

    SetDescriptor sd;
    RelationDescriptor rd;


    public SumExpr(SetDescriptor sd, RelationDescriptor rd) {
        if (sd == null||rd==null) {
            throw new NullPointerException();
        }
        this.sd=sd;
        this.rd=rd;
    }

    public String name() {
	return "sum("+sd.toString()+"."+rd.toString()+")";
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null||!(e instanceof SumExpr))
	    return false;
	SumExpr se=(SumExpr)e;
	return (se.sd==sd)&&(se.rd==rd);
    }

    public boolean usesDescriptor(Descriptor d) {
        return (sd==d)||(rd==d);
    }

    public Set useDescriptor(Descriptor d) {
        HashSet newset=new HashSet();
        if ((d==sd)||(d==rd))
            newset.add(this);
        return newset;
    }

    public Descriptor getDescriptor() {
        throw new Error("Sum shouldn't appear on left hand side!");
    }

    public boolean inverted() {
	return false;
    }

    public Set getRequiredDescriptors() {
        HashSet v=new HashSet();
        v.add(sd);
        v.add(rd);
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        writer.addDeclaration("int",dest.getSafeSymbol());
        writer.outputline(dest.getSafeSymbol()+"=0;");


        VarDescriptor itvd=VarDescriptor.makeNew("iterator");
        writer.addDeclaration("struct SimpleIterator",itvd.getSafeSymbol());
        writer.outputline("SimpleHashiterator("+sd.getSafeSymbol()+"_hash , &"+itvd.getSafeSymbol()+");");
        writer.outputline("while (hasNext(&"+itvd.getSafeSymbol()+"))");
        writer.startblock();
        {
            VarDescriptor keyvd=VarDescriptor.makeNew("key");
            writer.addDeclaration("int",keyvd.getSafeSymbol());
            writer.outputline(keyvd.getSafeSymbol()+"=next(&"+itvd.getSafeSymbol()+");");
            VarDescriptor tmpvar=VarDescriptor.makeNew("tmp");
            writer.addDeclaration("int",tmpvar.getSafeSymbol());

            VarDescriptor newset=VarDescriptor.makeNew("newset");
            writer.addDeclaration("struct SimpleHash *",newset.getSafeSymbol());
            writer.outputline(newset.getSafeSymbol()+"=SimpleHashimageSet("+rd.getSafeSymbol()+"_hash, "+keyvd.getSafeSymbol()+");");

            VarDescriptor itvd2=VarDescriptor.makeNew("iterator");
            writer.addDeclaration("struct SimpleIterator",itvd2.getSafeSymbol());
            writer.outputline("SimpleHashiterator("+newset.getSafeSymbol()+", &"+itvd2.getSafeSymbol()+");");

            writer.outputline("while (hasNext(&"+itvd2.getSafeSymbol()+"))");
            writer.startblock();
            {
                VarDescriptor keyvd2=VarDescriptor.makeNew("keyinner");
                writer.outputline(dest.getSafeSymbol()+"+=next(&"+itvd2.getSafeSymbol()+");");
                writer.endblock();
            }
            writer.outputline("freeSimpleHash("+newset.getSafeSymbol()+");");
            writer.endblock();
        }
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("sum(");
        pp.output(sd.toString());
        pp.output(".");
        pp.output(rd.toString());
        pp.output(")");
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        this.td = ReservedTypeDescriptor.INT;
        return this.td;
    }

    public Set getInversedRelations() {
        return new HashSet();
    }

}
