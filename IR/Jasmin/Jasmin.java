// Jasmin.java, created Mon Aug  2 13:55:50 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Jasmin;

import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.ClassFile.*;
import harpoon.Analysis.UseDef;
import harpoon.Util.WorkSet;
import harpoon.Analysis.Maps.TypeMap;

import java.io.*;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * <code>Jasmin</code> converts from QuadWithTry to bytecode
 * formatted for the Jasmin assembler.  
 * Note:  Requires patch on 1.06 to do sane things with
 * fields.
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: Jasmin.java,v 1.1.2.14 1999-09-09 21:42:58 cananian Exp $
 */
public class Jasmin {
    HCode[] hc;
    HMethod[] hm;
    HClass hclass;
    String classname;

    /**Creates a <code>Jasmin</code> object.*/
    public Jasmin(HCode[] hc, HMethod[] hm, HClass hclass) {
	this.hc = hc;
	this.hm = hm;
	this.hclass=hclass;
	this.classname=hclass.getName();
    }

    /**Takes in a <code>PrintStream</code> 'out' and dumps
     * Jasmin formatted assembly code to it.*/
    public void outputClass(PrintStream out) {
	if (hclass.isInterface()) 
	    out.println(".interface "+Modifier.toString(hclass.getModifiers())+" "+hclass.getName().replace('.','/'));
	    else
	out.println(".class "+Modifier.toString(hclass.getModifiers())+" "+hclass.getName().replace('.','/'));
	out.println(".super "+hclass.getSuperclass().getName().replace('.','/'));

	//List the interfaces
	HClass[] interfaces=hclass.getInterfaces();
	for (int i=0;i<interfaces.length;i++)
	    out.println(".implements "+interfaces[i].getName().replace('.','/'));
	HField[] hfields=hclass.getDeclaredFields();

	//List the fields
	for (int i=0;i<hfields.length;i++) {
	    String value="";
	    if (hfields[i].isConstant())
		if (hfields[i].getType()==HClass.forName("java.lang.String"))
		    value=" = "+'"'+hfields[i].getConstant().toString()+'"';
		else
		value=" = "+hfields[i].getConstant().toString();
	    out.println(".field "+Modifier.toString(hfields[i].getModifiers())+" "+hfields[i].getName() +" "+hfields[i].getDescriptor()+value);
	}

	//List the methods
	for(int i=0;i<hc.length;i++) {
	    outputMethod(out, i);
	}
    }

   
    private void outputMethod(PrintStream out, int i) {
	out.println(".method "+Modifier.toString(hm[i].getModifiers())+" "+hm[i].getName().replace('.','/')
		    +hm[i].getDescriptor().replace('.','/'));
	//Get modifiers
	int modifiers=hm[i].getModifiers();
	//Only print method quads if it isn't Abstract
	if (!Modifier.isAbstract(modifiers))
	    outputQuads(out, hm[i], hc[i]);

	out.println(".end method");
    }

    private void outputQuads(PrintStream out,HMethod hm, HCode hc) {
	//Output assembly from the quads

        TypeInfo tp=new TypeInfo(hc);
	//Build mapping from temps to localvars/stack
	Object[] tuple=buildmap(hc,tp);
	//Store the map
	Map map=(Map) tuple[0];
	//Print out the number of local variables
	out.println("    .limit locals "+((Integer)tuple[1]).toString());

	//FIX ME
	//This is wrong....
	out.println("    .limit stack "+(((Integer)tuple[1]).intValue()+2));
	WorkSet done=new WorkSet();
	Visitor visitor=new Visitor(out, map,hc,tp);
	Quad start=(Quad)hc.getRootElement();
	METHOD m=(METHOD)start.next(1);
	//Loop through the handlers
	//We do this to label the quad ranges protected by the handler
	for (int i=1;i<m.nextLength();i++) {
	    HANDLER h=(HANDLER)m.next(i);
	    visitor.labeler(h.next(0));
	    Enumeration e=h.protectedQuads();
	    while(e.hasMoreElements()) {
		Quad q=(Quad)e.nextElement();
		String tmp=visitor.labeler(q);
		tmp=visitor.labeler(q.next(0));
	    }
	}
	//Visit all the quads
	visitAll(out,visitor,start.next(1), done);
    }
		 
    private static final void visitAll(PrintStream out, Visitor visitor, Quad start, Set qm) {
	//Visit the node passed to us
	start.accept(visitor);
	//If we have a node after us, but we've already printed it
	//we need to do a goto
	if (start.next().length!=0)
	    if (qm.contains(start.next(0)))
		out.println("    goto "+visitor.labeler(start.next(0)));
	//add this node to the done list
	qm.add(start);
	//visit the kids
        Quad[] ql = start.next();
        for (int i=0; i<ql.length; i++) {
            if (qm.contains(ql[i])) continue; // skip if already done.
            visitAll(out, visitor, ql[i],qm);
        }
    }

    /** This Visitor prints out opcodes for each quad visited.*/

    class Visitor extends QuadVisitor {
	PrintStream out;
	int label;
	Map tempmap;
	HashMap labelmap;
	HCode hc;
	TypeMap tm;

	Visitor(PrintStream out, Map tempmap, HCode hc, TypeMap tm) {
	    this.out=out;
	    this.label=0;
	    this.tempmap=tempmap;
	    this.hc=hc;
	    labelmap=new HashMap();
	    this.tm=tm;
	}

	public void visit(Quad q) {
	    System.out.println("**********Unhandled quad"+q.toString());
	}

	public void visit(AGET q) {
	    System.out.println(q.toString());
	    String operand="***AGET error";

	    HClass ty = tm.typeMap(q, q.objectref());
	    HClass tp=ty.getComponentType();
	    if (tp==HClass.Boolean)
		operand="baload";
	    else if (tp==HClass.Byte)
		operand="baload";
	    else if(tp==HClass.Char)
		operand="caload";
	    else if (tp==HClass.Short)
		operand="saload";
	    else if (tp==HClass.Int)
		operand="iaload";
	    else if (tp==HClass.Double)
		operand="daload";
	    else if (tp==HClass.Float)
		operand="faload";
	    else if (tp==HClass.Long)
		operand="laload";
	    else
		operand="aaload";

	    out.println(iflabel(q));
	    load(q.objectref());
	    load(q.index());
	    out.println("    "+operand);
	    store(q.dst());
	}

	public void visit(ALENGTH q) {
	    out.println(iflabel(q));
	    load(q.objectref());
	    out.println("    arraylength");
	    store(q.dst());
	}

	public void visit(ANEW q) {
	    out.println(iflabel(q));
	    for (int i=0;i<q.dimsLength();i++)
		load(q.dims(i));
	    out.println("    multianewarray "+q.hclass().getName().replace('.','/') +" "+q.dimsLength());
            store(q.dst());
	}

	public void visit(ARRAYINIT q) {
	    out.println(iflabel(q));
	    if (q.value().length>0)
		load(q.objectref());
	    for (int i=0;i<q.value().length;i++) {
		if (q.type()==HClass.Boolean) {
		    Boolean b=(Boolean)q.value()[i];
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    if (b.booleanValue())
			out.println("    iconst_1");
		    else
			out.println("    iconst_0");
		    out.println("    bastore");
		}
		else if (q.type()==HClass.Int) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    ldc "+q.value()[i].toString());
		    out.println("    iastore");
		}
		else if (q.type()==HClass.Byte) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    bipush "+q.value()[i].toString());
		    out.println("    bastore");
		}
		else if (q.type()==HClass.Char) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    sipush "+q.value()[i].toString());
		    out.println("    castore");
		}
		else if (q.type()==HClass.Short) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    sipush "+q.value()[i].toString());
		    out.println("    sastore");
		}
		else if (q.type()==HClass.Long) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    ldc2_w "+q.value()[i].toString());
		    out.println("    lastore");
		}
		else if (q.type()==HClass.Double) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    ldc2_w "+q.value()[i].toString());
		    out.println("    dastore");
		}
		else if (q.type()==HClass.Float) {
		    dup(i,q.value().length);
		    out.println("    bipush "+(i+q.offset()));
		    out.println("    ldc "+q.value()[i].toString());
		    out.println("    fastore");
		}
	    }
	}
       
	private void dup(int i, int last) {
	    if ((i+1)!=last)
		out.println("    dup");
	}

	public void visit(ASET q) {
	    out.println(iflabel(q));
	    String operand="***ASET error";
	    HClass ty = tm.typeMap(q, q.objectref());
	    HClass tp=ty.getComponentType();
	    if (tp==HClass.Boolean)
		operand="bastore";
	    else if (tp==HClass.Byte)
		operand="bastore";
	    else if(tp==HClass.Char)
		operand="castore";
	    else if (tp==HClass.Short)
		operand="sastore";
	    else if (tp==HClass.Int)
		operand="iastore";
	    else if (tp==HClass.Double)
		operand="dastore";
	    else if (tp==HClass.Float)
		operand="fastore";
	    else if (tp==HClass.Long)
		operand="lastore";
	    else
		operand="aastore";

	    load(q.objectref());
	    load(q.index());
	    load(q.src());
	    out.println("    "+operand); 
	}

	public void visit(COMPONENTOF q) {
	    //kludge
	    //fix me
	    out.println(iflabel(q));
	    TempInfo dest=(TempInfo)tempmap.get(q.objectref());	    
	    if (dest.stack)
		out.println("    pop");
	    dest=(TempInfo)tempmap.get(q.arrayref());
	    if (dest.stack)
		out.println("    pop");
	    out.println("    bipush 1");
	    store(q.dst());
	}

	public void visit(METHOD q) {
	}

	public void visit(HEADER q) {
	}

	public void visit(PHI q) {
	    out.println(labeler(q)+":");
	}

	public void visit(FOOTER q) {
	    out.println(labeler(q)+":");
	    out.println("    return");
	}

	public void visit(HANDLER q) {
	    Enumeration e=q.protectedQuads();
	    String h=labeler(q);
	    out.println(h+":");
	    store(q.exceptionTemp());
	    out.println("    goto "+labeler(q.next(0)));
	    while(e.hasMoreElements()) {
		Quad qd=(Quad)e.nextElement();
		String start=labeler(qd);
		String stop=labeler(qd.next(0));
		String handler=labeler(q);
		if (q.caughtException()!=null)
		    out.println(".catch "+q.caughtException().getName().replace('.','/')+" from "+start+" to "+stop+" using "+handler);
		else
		    out.println(".catch all from "+start+" to "+stop+" using "+handler);
	    }
	}

	public void visit(INSTANCEOF q) {
	    out.println(iflabel(q));
	    load(q.src());
	    out.println("    instanceof "+q.hclass().getName().replace('.','/'));
	    store(q.dst());
	}
	
	public void visit(CALL q) {
	    if (q.isVirtual()&&!q.isInterfaceMethod()) {
		//virtual method
		out.println(iflabel(q));
		for(int i=0;i<q.params().length;i++) {
		    load(q.params(i)); 
		}
		out.println("    invokevirtual "+
			    q.method().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.method().getName().replace('.','/')
			    +q.method().getDescriptor().replace('.','/'));
		if (q.retval()!=null)
		    store(q.retval());
	    } 
	    else {
		if (q.isInterfaceMethod()) {
		    out.println(iflabel(q));
		    for(int i=0;i<q.params().length;i++) {
			load(q.params(i)); 
		    }
		    out.println("    invokeinterface "+
				q.method().getDeclaringClass().getName().replace('.','/')
				+"/"+q.method().getName().replace('.','/')
				+q.method().getDescriptor().replace('.','/')
				+" "+q.params().length);
		    if (q.retval()!=null)
			store(q.retval());
		}
		else
		    if(q.isStatic()) {
			//static method
			out.println(iflabel(q));
			for(int i=0;i<q.params().length;i++) {
			    load(q.params(i)); 
			}
			out.println("    invokestatic "+
			    q.method().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.method().getName().replace('.','/')
			    +q.method().getDescriptor().replace('.','/'));
			if (q.retval()!=null)
			    store(q.retval());
		    }
		    else {
					//non-virtual method
			out.println(iflabel(q));
			for(int i=0;i<q.params().length;i++) {
			    load(q.params(i)); 
			}
			out.println("    invokespecial "+
			    q.method().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.method().getName().replace('.','/')
			    +q.method().getDescriptor().replace('.','/'));
			if (q.retval()!=null)
			    store(q.retval());
		    }
	    }
	}

	public void visit(GET q) {
	    if (q.objectref()==null) {
		//Static
		out.println(iflabel(q));
		out.println("    getstatic "
			    +q.field().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.field().getName().replace('.','/')
			    +" "+q.field().getDescriptor().replace('.','/'));
	    }
	    else {
		out.println(iflabel(q));
		load(q.objectref());
		out.println("    getfield "
			    +q.field().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.field().getName().replace('.','/')
			    +" "+q.field().getDescriptor().replace('.','/'));
	    }
	    store(q.dst());
	}

	public void visit(CJMP q) {
	    String target=labeler(q);
	    out.println(iflabel(q));
	    load(q.test());
	    out.println("    ifne "+labeler(q.next(1)));
	}

	public void visit(THROW q) {
	    out.println(iflabel(q));
	    load(q.throwable());
	    out.println("    athrow");
	}

	public void visit(MONITORENTER q) {
	    out.println(iflabel(q));
	    load(q.lock());
	    out.println("    monitorenter");
	}

	public void visit(MONITOREXIT q) {
	    out.println(iflabel(q));
	    load(q.lock());
	    out.println("    monitorexit");
	}

	public void visit(MOVE q) {
	    out.println(iflabel(q));
	    load(q.src());
	    store(q.dst());
	}

	public void visit(NEW q) {
	    out.println(iflabel(q));
	    out.println("    new "+q.hclass().getName().replace('.','/'));
	    store(q.dst());
	}

	public void visit(LABEL q) {
	    out.println(iflabel(q));
	    out.println(";"+q.toString());
	}

	public void visit(NOP q) {
	    out.println(iflabel(q));
	    out.println("    nop");
	}

	public void visit(CONST q) {
	    out.println(iflabel(q));
	    if (q.value()!=null) {
		HClass hclass=q.type();
		if (hclass==HClass.forName("java.lang.String"))
		    out.println("    ldc "+'"'+q.value().toString()+'"');
		else
		    if ((hclass==HClass.Double)||(hclass==HClass.Long))
			out.println("    ldc2_w "+q.value().toString());
		    else
			out.println("    ldc "+q.value().toString());
	    }
	    else out.println("    aconst_null");
	    store(q.dst());

	    //HClass.Void
	}
	
	public void visit(RETURN q) {
	    out.println(iflabel(q));
	    if (q.retval()!=null) {
		String operand="Error";
		HClass tp=tm.typeMap(q,q.retval());
		if ((tp==HClass.Boolean)||
		(tp==HClass.Byte)||
		(tp==HClass.Char)||
		(tp==HClass.Short)||
		(tp==HClass.Int))
		operand="    ireturn";
	    else
		if (tp==HClass.Double)
		    operand="    dreturn";
		else
		    if (tp==HClass.Float)
			operand="    freturn";
		    else
			if (tp==HClass.Long)
			    operand="    lreturn";
			else
			    operand="    areturn";
		load(q.retval());
		out.println(operand);
	    }
	    else
		out.println("    return");
	}

	public void visit(SET q) {
	    if (q.objectref()==null) {
		//Static
		out.println(iflabel(q));
		load(q.src());
		out.println("    putstatic "
			    +q.field().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.field().getName().replace('.','/')
			    +" "+q.field().getDescriptor().replace('.','/'));
	    }
	    else {
		out.println(iflabel(q));
		load(q.objectref());
		load(q.src());
		out.println("    putfield "
			    +q.field().getDeclaringClass().getName().replace('.','/')
			    +"/"+q.field().getName().replace('.','/')
			    +" "+q.field().getDescriptor().replace('.','/'));
	    }
	}

	public void visit(SWITCH q) {
            out.println(iflabel(q));
	    load(q.index());
	    out.println("    lookupswitch");
	    for(int i=0;i<q.keysLength();i++)
		out.println("    "+q.keys(i)+" : "+labeler(q.next(i)));
	    out.println("    default : "+labeler(q.next(q.keysLength())));
	}

	public void visit(TYPECAST q) {
	    out.println(iflabel(q));
	    load(q.objectref());
	    out.println("    checkcast "+q.hclass().getName().replace('.','/'));
	    TempInfo dest=(TempInfo)tempmap.get(q.objectref());
	    if (!dest.stack)
		store(q.objectref());
	    else
		out.println("    pop");
	}

	//This method handles operands
	//really 3 cases:
	//normal, compares that are done with compares,
	//compares done with conditional branches

	public void visit(OPER q) {
	    out.println(iflabel(q));
	    switch (q.opcode()) {
	    case Qop.LCMPEQ:
		String l1=label(),l2=label();
		for (int i=0;i<q.operandsLength();i++)
		    load(q.operands(i));
		out.println("    lcmp");
		out.println("    ifeq "+l1);
		out.println("    bipush 0");
		out.println("    goto "+l2);
		out.println(l1+":");
		out.println("    bipush 1");
		out.println(l2+":");
		store(q.dst());
		break;
	    case Qop.LCMPGT:
		l1=label();l2=label();
		for (int i=0;i<q.operandsLength();i++)
		    load(q.operands(i));
		out.println("    lcmp");
		out.println("    ifgt "+l1);
		out.println("    bipush 0");
		out.println("    goto "+l2);
		out.println(l1+":");
		out.println("    bipush 1");
		out.println(l2+":");
		store(q.dst());
		break;
	    case Qop.DCMPEQ:
	    case Qop.DCMPGE:
	    case Qop.DCMPGT:
		l1=label();l2=label();
		for (int i=0;i<q.operandsLength();i++)
		    load(q.operands(i));
		out.println("    dcmpl");
		if (q.opcode()==Qop.DCMPEQ)
		    out.println("    ifeq "+l1);
		if (q.opcode()==Qop.DCMPGE)
		    out.println("    ifge "+l1);
		if (q.opcode()==Qop.DCMPGT)
		    out.println("    ifgt "+l1);
		out.println("    bipush 0");
		out.println("    goto "+l2);
		out.println(l1+":");
		out.println("    bipush 1");
		out.println(l2+":");
		store(q.dst());
		break;

	    case Qop.FCMPEQ:
	    case Qop.FCMPGE:
	    case Qop.FCMPGT:
		l1=label();l2=label();
		for (int i=0;i<q.operandsLength();i++)
		    load(q.operands(i));
		out.println("    fcmpl");
		if (q.opcode()==Qop.FCMPEQ)
		    out.println("    ifeq "+l1);
		if (q.opcode()==Qop.FCMPGE)
		    out.println("    ifge "+l1);
		if (q.opcode()==Qop.FCMPGT)
		    out.println("    ifgt "+l1);
		out.println("    bipush 0");
		out.println("    goto "+l2);
		out.println(l1+":");
		out.println("    bipush 1");
		out.println(l2+":");
		store(q.dst());
		break;

	    case Qop.ACMPEQ:
	    case Qop.ICMPEQ:
	    case Qop.ICMPGT:
		String base=Qop.toString(q.opcode());
		l1=label();
		l2=label();
		for (int i=0;i<q.operandsLength();i++) {
		    load(q.operands(i));
		}
		out.println("    if_"+base+" "+l1);
		out.println("    bipush 0");
		out.println("    goto "+l2);
		out.println(l1+":");
		out.println("    bipush 1");
		out.println(l2+":");
		//Need code to do jump/etc...
		//To store 0/1
		store(q.dst());
		break;
	    case Qop.D2F:
	    case Qop.D2I:
	    case Qop.D2L:
	    case Qop.DADD:
	    case Qop.DDIV:
	    case Qop.DMUL:
	    case Qop.DNEG:
	    case Qop.DREM:
	    case Qop.F2D:
	    case Qop.F2I:
	    case Qop.F2L:
	    case Qop.FADD:
	    case Qop.FDIV:
	    case Qop.FMUL:
	    case Qop.FNEG:
	    case Qop.FREM:
	    case Qop.I2B:
	    case Qop.I2C:
	    case Qop.I2D:
	    case Qop.I2F:
	    case Qop.I2L:
	    case Qop.I2S:
	    case Qop.IADD:
	    case Qop.IAND:
	    case Qop.IDIV:
	    case Qop.IMUL:
	    case Qop.INEG:
	    case Qop.IOR:
	    case Qop.IREM:
	    case Qop.ISHL:
	    case Qop.ISHR:
	    case Qop.IUSHR:
	    case Qop.IXOR:
	    case Qop.L2D:
	    case Qop.L2I:
	    case Qop.L2F:
	    case Qop.LADD:
	    case Qop.LAND:
	    case Qop.LDIV:
	    case Qop.LMUL:
	    case Qop.LNEG:
	    case Qop.LOR:
	    case Qop.LREM:
	    case Qop.LSHL:
	    case Qop.LSHR:
	    case Qop.LUSHR:
	    case Qop.LXOR:
		String cbase=Qop.toString(q.opcode());
		for (int i=0;i<q.operandsLength();i++) {
		    load(q.operands(i));
		}
		out.println("    "+cbase);
		store(q.dst());
		break;
	    default:
		out.println(q.toString()+" unimplemented");
	    }
	}

	private void store(Temp t) {
	    // Should be ok to pass null for HCodeElement param
	    HClass tp=tm.typeMap(null,t); 
	    String operand="***store error";
	    if ((tp==HClass.Boolean)||
		(tp==HClass.Byte)||
		(tp==HClass.Char)||
		(tp==HClass.Short)||
		(tp==HClass.Int))
		operand="istore ";
	    else
		if (tp==HClass.Double)
		    operand="dstore ";
		else
		    if (tp==HClass.Float)
			operand="fstore ";
		    else
			if (tp==HClass.Long)
			    operand="lstore ";
			else
			    operand="astore ";
	    TempInfo dest=(TempInfo)tempmap.get(t);
	    if (!dest.stack)
		out.println("    "+operand+(new Integer(dest.localvar)).toString());
	}

	private void load(Temp t) {
	    String operand="***load error";
	    // Should be ok to pass null for HCodeElement param
	    HClass tp=tm.typeMap(null,t);
	    if ((tp==HClass.Boolean)||
		(tp==HClass.Byte)||
		(tp==HClass.Char)||
		(tp==HClass.Short)||
		(tp==HClass.Int))
		operand="iload ";
	    else
		if (tp==HClass.Double)
		    operand="dload ";
		else
		    if (tp==HClass.Float)
			operand="fload ";
		    else
			if (tp==HClass.Long)
			    operand="lload ";
			else
			    operand="aload ";
	    TempInfo dest=(TempInfo)tempmap.get(t);
	    if (!dest.stack)
		out.println("    "+operand+(new Integer(dest.localvar)).toString());
	}

	private void astore(TempInfo t) {
	    if (!t.stack)
		out.println("    astore "+(new Integer(t.localvar)).toString());
	}

	private void aload(TempInfo t) {
	    if (!t.stack)
		out.println("    aload "+(new Integer(t.localvar)).toString());
	}
	private void dstore(TempInfo t) {
	    if (!t.stack)
		out.println("    dstore "+(new Integer(t.localvar)).toString());
	}

	private void lstore(TempInfo t) {
	    if (!t.stack)
		out.println("    lstore "+(new Integer(t.localvar)).toString());
	}

	private void fstore(TempInfo t) {
	    if (!t.stack)
		out.println("    fstore "+(new Integer(t.localvar)).toString());
	}

	private void istore(TempInfo t) {
	    if (!t.stack)
		out.println("    istore "+(new Integer(t.localvar)).toString());
	}
	
	private void iload(TempInfo t) {
	    if (!t.stack)
		out.println("    iload "+(new Integer(t.localvar)).toString());
	}

	//Give unique labels to quads
	private String labeler(Quad q) {
	    if (labelmap.containsKey(q))
		return (String)labelmap.get(q);
	    else {
		String label=label();
		labelmap.put(q,label);
		return label;
	    }
	}

	//Returns string with label in it
	private String iflabel(Quad q) {
	    if (labelmap.containsKey(q))
		return (String)labelmap.get(q)+(new String(":"));
	    else return new String("");
	}

	//Assigns label numbers
	private String label() {
	    return "L"+(new Integer(label++)).toString();
	}
    }

    /** <code>buildmap</code> takes in a <code>HCode</code> and a <code>TypeMap</code>
     *  and returns a Map and number of local variables used.
     *  Buildmap maps each temp either to the stack or to a localvariable.*/

    public final Object[] buildmap(final HCode code, TypeMap tp) {
	Quad q=(Quad)code.getRootElement();
	METHOD m=(METHOD) q.next(1);
	UseDef ud=new UseDef();
	Temp[] alltemp=ud.allTemps(code);
	HashMap stacktemps=new HashMap();
	WorkSet badquads=new WorkSet();
	int j=0;

	//Cycle through the formal parameters...Assign them to their
	//appropriate local variables
	for(int i=0;i<m.params().length;i++) {
	    stacktemps.put(m.params(i),new TempInfo(j++));
	    HClass hclass=tp.typeMap(null,m.params(i));
	    if ((hclass==HClass.Double)||(hclass==HClass.Long))
		j++;
	}


	//Make a set containing all of the quads protected
	//by handler quads...
	for (int i=1;i<m.nextLength();i++) {
	    HANDLER h=(HANDLER)m.next(i);
	    Enumeration e=h.protectedQuads();
	    while(e.hasMoreElements())
		badquads.add((Quad)e.nextElement());
	}

	//Cycled through all temps defined and used once
	//Consider them for placement on stack

	for (int i=0;i<alltemp.length;i++) {
	    Temp next=alltemp[i];
	    HCodeElement[] defs=ud.defMap(code,next);
	    if (!stacktemps.containsKey(next)) {
		if (defs.length==1) {
		    HCodeElement[] uses=ud.useMap(code,next);
		    if (uses.length==1) {
			checkPair(stacktemps, (Quad)  uses[0], (Quad) defs[0], next, badquads);
		    }
		}
	    }
	}

	//Iterate through the temps we've placed so far

	Iterator iterate=stacktemps.keySet().iterator();
	while (iterate.hasNext()) {
	    Temp next=(Temp)iterate.next();

	    //Only worry about the ones on the stack
	    if (((TempInfo)stacktemps.get(next)).stack) {
		HCodeElement[] huses=ud.useMap(code,next);
		HCodeElement[] hdefs=ud.defMap(code,next);
		Quad use=(Quad)huses[0];
		Quad def=(Quad)hdefs[0];
		Temp[] uses=use.use();
		Temp[] defs=def.def();
		boolean flag=true;
		boolean broken=false;

		//go through the using quad
		for (int i=0;i<uses.length;i++) {
		    //If we have an temp before 'next'
		    //that isn't on the stack, we can't put it
		    //under 'next'

		    if (!stacktemps.containsKey(uses[i]))
			flag=false;
		    else
			if (!((TempInfo)stacktemps.get(uses[i])).stack)
			    flag=false;
		    if ((flag==false)&&(uses[i]==next)) {
			broken=true;
			break;
		    }
		}
		flag=true;

		//if we need a temp under 'next'
		//we can't put 'next' on the stack
		for (int i=0;i<defs.length;i++) {
		    if (!stacktemps.containsKey(defs[i]))
			flag=false;
		    else if (!((TempInfo)stacktemps.get(defs[i])).stack)
			flag=false;
		    if ((flag==false)&&(uses[i]==next)) {
			broken=true;
			break;
		    }
		}

		if (broken) {
		    // If this stack allocation failed,
		    // allocate it to a local variable.
		    // Should be ok to pass null as HCodeElement param
		    HClass hclass=tp.typeMap(null,next);
		    stacktemps.put(next,new TempInfo(j++));
		    if ((hclass==HClass.Double)||(hclass==HClass.Long))
			j++;
		}
	    }
	}

	//loop through the rest of the temps
	//assign them to local variables...
	for (int i=0;i<alltemp.length;i++) {
	    if (!stacktemps.containsKey(alltemp[i])) {
		// Should be ok to pass null as HCodeElement param
		HClass hclass=tp.typeMap(null,alltemp[i]);
		stacktemps.put(alltemp[i],new TempInfo(j++));
		if ((hclass==HClass.Double)||(hclass==HClass.Long))
		    j++;
	    }
	}
	return new Object[] {stacktemps, new Integer(j)};
    }

    void checkPair(Map stacktemps, final Quad use, final Quad def, Temp t, Set badquads) {
	Quad ptr=def;
	boolean flag=true;
	WorkSet track=new WorkSet();
	//make sure that the definition quad
	//isn't covered by a handler and that it only defines one temp
	if ((ptr.def().length==1)&&(!badquads.contains(ptr))) {
	    //while we haven't found the use
	    while (ptr!=use) {
		//make sure we aren't at a sigma quad
		//don't want to stack allocate across them
		if (ptr.next().length!=1) {
		    flag=false;break;}
		
		//Look at the temp's defined by this quad
		Temp[] defs=ptr.def();
		//if any of them are on the stack
		//add them to our stack tracking list
		for (int i=0;i<defs.length;i++)
		    if (stacktemps.containsKey(defs[i]))
			track.push(defs[i]);

		//go to the next quad
       		ptr=ptr.next(0);

		//make sure that this isn't a phi...
		//don't want the possibility of the analysis
		//following a loop...
		//also make sure that this quad isn't covered by a handler
		if ((ptr.prev().length!=1)||(badquads.contains(ptr))) {
		    flag=false;break;}
		//check the uses of this quad....
		//if it is our temp, we are done...
		//if it is another temp, check to see
		//if it is stack allocated...
		//if it is in our track list remove it, if it is
		//not bail on this temp
		Temp[] uses=ptr.use();
		for (int i=uses.length-1;i>=0;i--) {
		    if (uses[i]==t) break;
		    if (stacktemps.containsKey(uses[i])) {
			if (track.contains(uses[i]))
			    track.remove(uses[i]);
			else
			    {flag=false; break;}
		    }
		}
	    }
	    //if we made it here, and our tracking list is empty
	    //we add it to our list of possibilities
	    if ((flag)&&(track.isEmpty())) {
		stacktemps.put(t, new TempInfo(true));
	    } 
	}
    }
}



