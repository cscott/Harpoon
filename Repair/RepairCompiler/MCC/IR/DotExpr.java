package MCC.IR;

import java.util.*;
import MCC.Compiler;

public class DotExpr extends Expr {

    Expr left;
    String field;
    Expr index;

    static boolean DOMEMCHECKS=false;
    static boolean DOTYPECHECKS=false;
    static boolean DONULL=false;


    public DotExpr(Expr left, String field, Expr index) {
        this.left = left;
        this.field = field;
        this.index = index;
    }

    public boolean isInvariant(Set vars) {
	if (!left.isInvariant(vars))
	    return false;
	if (intindex!=null)
	    return intindex.isInvariant(vars);
	else
	    return true;
    }

    public Set findInvariants(Set vars) {
	if (isInvariant(vars)) {
	    Set s=new HashSet();
	    s.add(this);
	    return s;
	} else {
	    Set ls=left.findInvariants(vars);
	    if (intindex!=null) {
		ls.addAll(intindex.findInvariants(vars));
		Expr indexbound=((ArrayDescriptor)this.fd).getIndexBound();
		ls.addAll(indexbound.findInvariants(vars));
		if ((!(intindex instanceof IntegerLiteralExpr))||
		    ((IntegerLiteralExpr) intindex).getValue() != 0) {
		    FieldDescriptor fd=this.fd;
		    if (fd instanceof ArrayDescriptor)
			fd=((ArrayDescriptor)fd).getField();
		    Expr basesize = fd.getBaseSizeExpr();
		    ls.addAll(basesize.findInvariants(vars));
		}
	    }
	    return ls;
	}
    }


    public boolean isSafe() {
	if (!left.isSafe())
	    return false;

	FieldDescriptor tmpfd=fd;

	if (tmpfd.getPtr()) // Pointers cound be invalid
	    return false;

	if (tmpfd instanceof ArrayDescriptor) {
            Expr arrayindex=((ArrayDescriptor)tmpfd).getIndexBound();
            if (index instanceof IntegerLiteralExpr&&arrayindex instanceof IntegerLiteralExpr) {
                int indexvalue=((IntegerLiteralExpr)index).getValue();
                int arrayindexvalue=((IntegerLiteralExpr)arrayindex).getValue();
                if (indexvalue>=0&&indexvalue<arrayindexvalue)
                    return true;
            }
	    return false; // Otherwise, arrays could be out of bounds
        }
        return true;
    }

    public Set freeVars() {
	Set lset=left.freeVars();
	Set iset=null;
	if (intindex!=null)
	    iset=intindex.freeVars();
	if (lset==null)
	    return iset;
	if (iset!=null)
	    lset.addAll(iset);
	return lset;
    }

    /*
    static int memoryindents = 0;

    public static void generate_memory_endblocks(CodeWriter cr) {
        while (memoryindents > 0) {
            memoryindents --;
            cr.endblock();
        }
        memoryindents = 0;
    }
    */

    FieldDescriptor fd;
    TypeDescriptor fieldtype;
    Expr intindex;

    public String name() {
	String name=left.name()+"."+field;
	if (index!=null)
	    name+="["+index.name()+"]";
	return name;
    }

    public void findmatch(Descriptor d, Set s) {
	if (d==fd)
	    s.add(this);
	left.findmatch(d,s);
	if (intindex!=null)
	    intindex.findmatch(d,s);
    }

    public Set useDescriptor(Descriptor d) {
	HashSet newset=new HashSet();
	if (d==fd)
	    newset.add(this);
	newset.addAll(left.useDescriptor(d));
	if (intindex!=null)
	    newset.addAll(intindex.useDescriptor(d));
	return newset;
    }

    public boolean usesDescriptor(Descriptor d) {
	if (d==fd)
	    return true;
	return left.usesDescriptor(d)||((intindex!=null)&&intindex.usesDescriptor(d));
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null||!(e instanceof DotExpr))
	    return false;
	DotExpr de=(DotExpr)e;
	if (!de.field.equals(field))
	    return false;
	if (index==null) {
	    if (de.index!=null)
		return false;
	} else if (!index.equals(remap,de.index))
	    return false;
	if (!left.equals(remap,de.left))
	    return false;
	return true;
    }


    public Set getRequiredDescriptors() {
        Set v = left.getRequiredDescriptors();

        if (intindex != null) {
            v.addAll(intindex.getRequiredDescriptors());
        }
        v.add(fd);
        return v;
    }

    public Expr getExpr() {
        return left;
    }

    public FieldDescriptor getField() {
	return fd;
    }

    public Expr getIndex() {
	return intindex;
    }

    private boolean exactalloc(TypeDescriptor td) {
        if (!(td instanceof StructureTypeDescriptor))
            return false;
        StructureTypeDescriptor std=(StructureTypeDescriptor)td;
        if (std.size()!=1) /* Just looking for arrays */
            return false;
        FieldDescriptor tmpfd=std.get(0);
        if (!(tmpfd instanceof ArrayDescriptor))
            return false;
        ArrayDescriptor afd=(ArrayDescriptor)tmpfd;
        TypeDescriptor elementdescriptor=afd.getType();
        Expr sizeexpr=elementdescriptor.getSizeExpr();
        if (!OpExpr.isInt(sizeexpr))
            return false;
        Expr indexbound=afd.getIndexBound();
        if (indexbound instanceof DotExpr)
            return true;
        if ((indexbound instanceof OpExpr)&&
            (((OpExpr)indexbound).getOpcode()==Opcode.MULT)&&
            (((OpExpr)indexbound).getLeftExpr() instanceof DotExpr)&&
            (((OpExpr)indexbound).getRightExpr() instanceof DotExpr))
            return true;
        return false;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        VarDescriptor leftd = VarDescriptor.makeNew("left");

	if (writer.getInvariantValue()!=null&&
	    writer.getInvariantValue().isInvariant(this)) {
	    writer.addDeclaration(getType().getGenerateType().getSafeSymbol().toString(), dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol()+"="+writer.getInvariantValue().getValue(this).getSafeSymbol()+";");
	    writer.outputline("maybe="+writer.getInvariantValue().getMaybe(this).getSafeSymbol()+";");
	    return;
	}

        writer.output("/* " +  leftd.getSafeSymbol() + " <-- ");
        left.prettyPrint(writer);
        writer.outputline("*/");

        left.generate(writer, leftd);

        writer.output("/* " +  leftd.getSafeSymbol() + " = ");
        left.prettyPrint(writer);
        writer.outputline("*/");

        StructureTypeDescriptor struct = (StructureTypeDescriptor) left.getType();
        Expr offsetbits;

        // #ATTN#: getOffsetExpr needs to be called with the fielddescriptor object that is in the vector list
        // this means that if the field is an arraydescriptor you have to call getOffsetExpr with the array
        // descriptor not the underlying field descriptor

        /* we calculate the offset in bits */

        offsetbits = struct.getOffsetExpr(fd);

	FieldDescriptor fd=this.fd;
	if (fd instanceof ArrayDescriptor)
	    fd=((ArrayDescriptor)fd).getField();
	boolean doboundscheck=true;
	boolean performedboundscheck=false;

	writer.addDeclaration(getType().getGenerateType().toString(),dest.getSafeSymbol());
	writer.outputline(dest.getSafeSymbol()+"=0;");

        if (intindex != null) {
            if (intindex instanceof IntegerLiteralExpr && ((IntegerLiteralExpr) intindex).getValue() == 0) {
                /* short circuit for constant 0 */
            } else {
                Expr basesize = fd.getBaseSizeExpr();
		if (doboundscheck) {
		    VarDescriptor indexvd=VarDescriptor.makeNew("index");
		    indexvd.setType(ReservedTypeDescriptor.INT);
		    writer.getSymbolTable().add(indexvd);

		    writer.output("/* " + indexvd.getSafeSymbol() + " <-- ");

		    intindex.prettyPrint(writer);
		    writer.outputline("*/");
		    intindex.generate(writer, indexvd);
		    writer.output("/* " + indexvd.getSafeSymbol() + " = ");
		    intindex.prettyPrint(writer);
		    writer.outputline("*/");
		    Expr indexbound=((ArrayDescriptor)this.fd).getIndexBound();
		    VarDescriptor indexboundvd=VarDescriptor.makeNew("indexbound");

		    indexbound.generate(writer,indexboundvd);

		    writer.outputline("if ("+indexvd.getSafeSymbol()+">=0 &&"+indexvd.getSafeSymbol()+"<"+indexboundvd.getSafeSymbol()+")");
		    writer.startblock();
		    VarExpr indexve=new VarExpr(indexvd);
		    offsetbits = new OpExpr(Opcode.ADD, offsetbits, new OpExpr(Opcode.MULT, basesize, indexve));

		    performedboundscheck=true;
		} else
		    offsetbits = new OpExpr(Opcode.ADD, offsetbits, new OpExpr(Opcode.MULT, basesize, intindex));
            }
        }

	final SymbolTable st = writer.getSymbolTable();
        TypeDescriptor td2 = offsetbits.typecheck(new SemanticAnalyzer() {
		public IRErrorReporter getErrorReporter() { throw new IRException("badness"); }
		public SymbolTable getSymbolTable() { return st; }
	    });

        if (td2 == null) {
	    throw new IRException();
        } else if (td2 != ReservedTypeDescriptor.INT) {
	    throw new IRException();
	}

        boolean dotypecheck = false;

	VarDescriptor ob = VarDescriptor.makeNew("offsetinbits");
	writer.output("/* " + ob.getSafeSymbol() + " <-- ");
	offsetbits.prettyPrint(writer);
	writer.outputline("*/");
	offsetbits.generate(writer, ob);
	writer.output("/* " + ob.getSafeSymbol() + " = ");
	offsetbits.prettyPrint(writer);
	writer.outputline("*/");

	/* derive offset in bytes */
	VarDescriptor offset = VarDescriptor.makeNew("offset");
	writer.addDeclaration("int", offset.getSafeSymbol());
	writer.outputline(offset.getSafeSymbol() + " = " + ob.getSafeSymbol() + " >> 3;");

	if (fd.getType() instanceof ReservedTypeDescriptor && !fd.getPtr()) {
	    VarDescriptor shift = VarDescriptor.makeNew("shift");
	    writer.addDeclaration("int", shift.getSafeSymbol());
	    writer.outputline(shift.getSafeSymbol() + " = " + ob.getSafeSymbol() +
			      " - (" + offset.getSafeSymbol() + " << 3);");
	    int mask = bitmask(((IntegerLiteralExpr)fd.getType().getSizeExpr()).getValue());

	    /* type var = ((*(int *) (base + offset)) >> shift) & mask */
	    writer.outputline("if ("+leftd.getSafeSymbol()+")");
	    writer.outputline(dest.getSafeSymbol() + " = ((*(int *)" +
			      "(" + leftd.getSafeSymbol() + " + " + offset.getSafeSymbol() + ")) " +
			      " >> " + shift.getSafeSymbol() + ") & 0x" + Integer.toHexString(mask) + ";");
	    writer.outputline("else maybe=1;");
	} else { /* a structure address or a ptr */
	    String ptr = fd.getPtr() ? "*(int *)" : "";
	    /* type var = [*(int *)] (base + offset) */
	    writer.outputline("if ("+leftd.getSafeSymbol()+")");
	    writer.startblock();
	    writer.outputline(dest.getSafeSymbol() +
			      " = " + ptr + "(" + leftd.getSafeSymbol() + " + " + offset.getSafeSymbol() + ");");
	    if (fd.getPtr()) {
		writer.outputline("if ("+dest.getSafeSymbol()+")");
		writer.startblock();


		if (DOTYPECHECKS||DOMEMCHECKS) {
                    /* NEED TO CHECK IF THERE ARE VARIABLES TO PLAY WITH IN THE STRUCT!!!! */
                    if (Compiler.EXACTALLOCATION&&exactalloc(td)) {
                        writer.outputline("if (!assertexactmemory("+dest.getSafeSymbol()+", "+this.td.getId()+"))");
                        {
                            writer.startblock();
                            /* Okay, we've failed to fit it in here */

                            VarDescriptor highptr=VarDescriptor.makeNew("highptr");
                            writer.addDeclaration("int", highptr.getSafeSymbol());
                            writer.outputline(highptr.getSafeSymbol()+"=getendofblock("+dest.getSafeSymbol()+");");
                            VarDescriptor size=VarDescriptor.makeNew("size");
                            writer.addDeclaration("int", size.getSafeSymbol());
                            writer.outputline(size.getSafeSymbol()+"="+highptr.getSafeSymbol()+"-"+dest.getSafeSymbol()+";");

                            StructureTypeDescriptor std=(StructureTypeDescriptor)this.td;
                            ArrayDescriptor afd=(ArrayDescriptor)std.get(0);
                            TypeDescriptor elementdescriptor=afd.getType();
                            Expr sizeexpr=elementdescriptor.getSizeExpr();
                            int elementsize=OpExpr.getInt(sizeexpr);
                            //convert size to bytes
                            if (elementsize%8==0)
                                elementsize=elementsize/8;
                            else
                                elementsize=(elementsize/8)+1;
                            /* Basic sanity check */
                            writer.outputline("if ("+size.getSafeSymbol()+"%"+
                                              elementsize+"==0)");
                            {
                                writer.startblock();
                                VarDescriptor numElements=VarDescriptor.makeNew("numberofelements");
                                writer.addDeclaration("int", numElements.getSafeSymbol());
                                writer.outputline(numElements.getSafeSymbol()+"="+size.getSafeSymbol()+"/"+elementsize+";");
                                Expr indexbound=afd.getIndexBound();
                                if  (indexbound instanceof DotExpr) {
                                /* NEED TO IMPLEMENT */

                                    VarExpr ve=new VarExpr(numElements);
                                    numElements.setType(ReservedTypeDescriptor.INT);
                                    ve.td=ReservedTypeDescriptor.INT;
                                    Updates u=new Updates(indexbound,ve);
                                    UpdateNode un=new UpdateNode(null);
                                    un.addUpdate(u);
                                    un.generate(writer,false,false,null,null,null,null);
				    writer.outputline("free"+RepairGenerator.name+"("+RepairGenerator.newmodel.getSafeSymbol()+");");
				    writer.outputline("computesizes(thisvar);");
				    writer.outputline(RepairGenerator.name+"_staterecomputesizes(thisvar);");
				    writer.outputline("goto rebuild;");

				    //                                    writer.outputline("break;");
				    
                                } else if ((indexbound instanceof OpExpr)&&
                                           (((OpExpr)indexbound).getOpcode()==Opcode.MULT)&&
                                           (((OpExpr)indexbound).getLeftExpr() instanceof DotExpr)&&
                                           (((OpExpr)indexbound).getRightExpr() instanceof DotExpr)) {

                                    DotExpr leftexpr=(DotExpr)(((OpExpr)indexbound).getLeftExpr());
                                    VarDescriptor leftside=VarDescriptor.makeNew("leftvalue");
                                    writer.addDeclaration("int", leftside.getSafeSymbol());
                                    leftexpr.generate(writer,leftside);
                                    DotExpr rightexpr=(DotExpr)(((OpExpr)indexbound).getRightExpr());
                                    VarDescriptor rightside=VarDescriptor.makeNew("rightvalue");
                                    writer.addDeclaration("int", rightside.getSafeSymbol());
                                    rightexpr.generate(writer,rightside);
                                    writer.outputline("if (("+leftside.getSafeSymbol()+"!=0) &&("+numElements.getSafeSymbol()+"%"+leftside.getSafeSymbol()+"==0))");
                                    {
                                        writer.startblock();
                                        VarDescriptor newvalue=VarDescriptor.makeNew("newvalue");
                                        writer.addDeclaration("int", newvalue.getSafeSymbol());
                                        writer.outputline(newvalue.getSafeSymbol()+"="+numElements.getSafeSymbol()+"/"+leftside.getSafeSymbol()+";");
                                        VarExpr ve=new VarExpr(newvalue);
                                        newvalue.setType(ReservedTypeDescriptor.INT);
                                        ve.td=ReservedTypeDescriptor.INT;
                                        Updates u=new Updates(rightexpr,ve);
                                        UpdateNode un=new UpdateNode(null);
                                        un.addUpdate(u);
                                        un.generate(writer,false,false,null,null,null,null);
					writer.outputline("free"+RepairGenerator.name+"("+RepairGenerator.newmodel.getSafeSymbol()+");");
					writer.outputline("computesizes(thisvar);");
					writer.outputline(RepairGenerator.name+"_staterecomputesizes(thisvar);");
					writer.outputline("goto rebuild;");
					//                                        writer.outputline("break;");
                                        writer.endblock();
                                    }
                                    writer.outputline("else if (("+rightside.getSafeSymbol()+"!=0)&&("+numElements.getSafeSymbol()+"%"+rightside.getSafeSymbol()+"==0))");
                                    {
                                        writer.startblock();
                                        VarDescriptor newvalue=VarDescriptor.makeNew("newvalue");
                                        writer.addDeclaration("int", newvalue.getSafeSymbol());
                                        writer.outputline(newvalue.getSafeSymbol()+"="+numElements.getSafeSymbol()+"/"+rightside.getSafeSymbol()+";");
                                        VarExpr ve=new VarExpr(newvalue);
                                        newvalue.setType(ReservedTypeDescriptor.INT);
                                        ve.td=ReservedTypeDescriptor.INT;
                                        Updates u=new Updates(leftexpr,ve);
                                        UpdateNode un=new UpdateNode(null);
                                        un.addUpdate(u);
                                        un.generate(writer,false,false,null,null,null,null);
					writer.outputline("free"+RepairGenerator.name+"("+RepairGenerator.newmodel.getSafeSymbol()+");");
					writer.outputline("computesizes(thisvar);");
					writer.outputline(RepairGenerator.name+"_staterecomputesizes(thisvar);");
					writer.outputline("goto rebuild;");
					//                                        writer.outputline("break;");
                                        writer.endblock();
                                    }


                                } else throw new Error("Should be here");

                                writer.endblock();
                            }

                            writer.endblock();
                        }
                            /*

                              if (indexbound instanceof DotExpr)
                              return true;
                              if ((indexbound instanceof OpExpr)&&
                              (((OpExpr)indexbound).getOpcode()==Opcode.MULT)&&
                              (((OpExpr)indexbound).getLeftExpr() instanceof DotExpr)&&
                              (((OpExpr)indexbound).getRightExpr() instanceof DotExpr))
                              return true;
                              return false;
                            */


                            /* Give up and null out bad pointer */
                    }
                    VarDescriptor typevar=VarDescriptor.makeNew("typechecks");
                    if (DOMEMCHECKS&&(!DOTYPECHECKS)) {
                        writer.addDeclaration("bool", typevar.getSafeSymbol());
                        writer.outputline(typevar.getSafeSymbol()+"=assertvalidmemory(" + dest.getSafeSymbol() + ", " + this.td.getId() + ");");
                        dotypecheck = true;
                    } else if (DOTYPECHECKS) {
                        writer.addDeclaration("bool", typevar.getSafeSymbol());
                        writer.outputline(typevar.getSafeSymbol()+"=assertvalidtype(" + dest.getSafeSymbol() + ", " + this.td.getId() + ");");
                    }
                    if (DOMEMCHECKS||DOTYPECHECKS) {
                        writer.outputline("if (!"+typevar.getSafeSymbol()+")");
                        writer.startblock();
                        writer.outputline(dest.getSafeSymbol()+"=0;");
                        if (DONULL)
                            writer.outputline(ptr + "(" + leftd.getSafeSymbol() + " + " + offset.getSafeSymbol() + ")=0;");
                        writer.endblock();
                    }
                }

		writer.endblock();
	    }
	    writer.endblock();
	    writer.outputline("else maybe=1;");
        }
	if (performedboundscheck) {
	    writer.endblock();
	    writer.outputline(" else ");
	    writer.startblock();
	    writer.outputline(dest.getSafeSymbol()+"=0;");
	    writer.outputline("maybe=1;");
	    if (!Compiler.REPAIR)
		writer.outputline("printf(\"Array Index Out of Bounds\");");
	    writer.endblock();
	}
    }

    private int bitmask(int bits) {
        int mask = 0;

        for (int i = 0; i < bits; i++) {
            mask <<= 1;
            mask += 1;
        }

        return mask;
    }

    public void prettyPrint(PrettyPrinter pp) {
        left.prettyPrint(pp);
        pp.output("." + field);
        if (index != null) {
            pp.output("[");
            index.prettyPrint(pp);
            pp.output("]");
        }
    }

    public boolean isValue(TypeDescriptor td) {
	FieldDescriptor tmpfd=fd;
	if (tmpfd instanceof ArrayDescriptor)
	    tmpfd=((ArrayDescriptor)tmpfd).getField();
	return (tmpfd.getPtr()||(tmpfd.getType() instanceof ReservedTypeDescriptor));
    }

    public boolean isPtr() {
	FieldDescriptor tmpfd=fd;
	if (tmpfd instanceof ArrayDescriptor)
	    tmpfd=((ArrayDescriptor)tmpfd).getField();
	return tmpfd.getPtr();
    }

    boolean typechecked=false;
    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
	if (typechecked)
	    return this.td;
	else typechecked=true;
        TypeDescriptor lefttype = left.typecheck(sa);
        TypeDescriptor indextype = index == null ? null : index.typecheck(sa);

	{
	    /* finished typechecking...so we can fill the fields in */
	    StructureTypeDescriptor struct = (StructureTypeDescriptor) left.getType();
	    FieldDescriptor fd = struct.getField(field);
	    LabelDescriptor ld = struct.getLabel(field);
	    if (ld != null) { /* label */
		assert fd == null;
		fieldtype = ld.getType(); // d.s ==> Superblock, while,  d.b ==> Block
		fd = ld.getField();
		assert fd != null;
		assert intindex == null;
		intindex = ld.getIndex();
	    } else {
                if (fd==null) {
                    throw new Error("Null fd for: "+field);
                }
		fieldtype = fd.getType();
		intindex=index;
	    }
	    this.fd=fd;
	    if (fieldtype instanceof MissingTypeDescriptor)
		throw new Error(fieldtype.getSymbol()+" type undefined!");
	}

        if ((lefttype == null) || (index != null && indextype == null)) {
            return null;
        }

        if (indextype != null) {
            if (indextype != ReservedTypeDescriptor.INT) {
                sa.getErrorReporter().report(null, "Index must be of type 'int' not '" + indextype.getSymbol() + "'");
                return null;
            }
        }

        if (lefttype instanceof StructureTypeDescriptor) {
            StructureTypeDescriptor struct = (StructureTypeDescriptor) lefttype;
            FieldDescriptor fd = struct.getField(field);
            LabelDescriptor ld = struct.getLabel(field);

            if (fd != null) { /* field */
                assert ld == null;

                if (indextype == null && fd instanceof ArrayDescriptor) {
                    sa.getErrorReporter().report(null, "Must specify an index what accessing array field '" + struct.getSymbol() + "." + fd.getSymbol() + "'");
                    return null;
                } else if (indextype != null && !(fd instanceof ArrayDescriptor)) {
                    sa.getErrorReporter().report(null, "Cannot specify an index when accessing non-array field '" + struct.getSymbol() + "." + fd.getSymbol() + "'");
                    return null;
                }

                this.td = fd.getType();
            } else if (ld != null) { /* label */
                assert fd == null;

                if (index != null) {
                    sa.getErrorReporter().report(null, "A label cannot be accessed as an array");
                    return null;
                }

                this.td = ld.getType();
            } else {
                sa.getErrorReporter().report(null, "No such field or label '" + field + "' in structure '" + struct.getSymbol() + "'");
                return null;
            }

            /* we promote bit, byte and short to integer types */
            if (this.td == ReservedTypeDescriptor.BIT ||
                this.td == ReservedTypeDescriptor.BYTE ||
                this.td == ReservedTypeDescriptor.SHORT) {
                this.td = ReservedTypeDescriptor.INT;
            }

            return this.td;
        } else {
            sa.getErrorReporter().report(null, "Left hand side of . expression must be a structure type, not '" + lefttype.getSymbol() + "'");
            return null;
        }
    }
}
