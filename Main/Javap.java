// RawP.java, created by cananian
// Copyright (C) 2002 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.Loader;
import harpoon.IR.RawClass.AccessFlags;
import harpoon.IR.RawClass.Attribute;
import harpoon.IR.RawClass.AttributeExceptions;
import harpoon.IR.RawClass.AttributeSignature;
import harpoon.IR.RawClass.AttributeSourceFile;
import harpoon.IR.RawClass.ClassFile;
import harpoon.IR.RawClass.ConstantClass;
import harpoon.IR.RawClass.FieldInfo;
import harpoon.IR.RawClass.MethodInfo;
import harpoon.Util.Util;

import java.io.InputStream;
/**
 * <code>Javap</code> is a low-level clone of javap that supports
 * GJ signatures.
 * 
 * @author  C. Scott Ananian <cananian@lesser-magoo.lcs.mit.edu>
 * @version $Id: Javap.java,v 1.3 2003-04-09 22:21:59 cananian Exp $
 */
public abstract class Javap /*extends harpoon.IR.Registration*/ {
    public static final void main(String args[]) {
	String classname = args[0];

	InputStream is = 
	    Loader.getResourceAsStream(Loader.classToResource(classname));
	if (is==null) throw new NoClassDefFoundError(classname);
	ClassFile raw = new ClassFile(is);

	// print "Compiled from"
	AttributeSourceFile asf = (AttributeSourceFile)
	    findAttribute(raw, "SourceFile");
	if (asf!=null)
	    System.out.println("Compiled from "+asf.sourcefile());
	// get generic signature.
	AttributeSignature asig = (AttributeSignature)
	    findAttribute(raw, "Signature");
	String gjsig = (asig==null) ? null : asig.signature();
	// print class modifiers.
	System.out.print(modString(raw.access_flags, true));
	// now print class name
	System.out.print(desc2name("L"+raw.this_class().name()+";"));
	// print formal type parameters.
	if (gjsig!=null && gjsig.charAt(0)=='<') {
	    OffsetAndString oas = munchParamPart(gjsig);
	    System.out.print(oas.string);
	    gjsig = gjsig.substring(oas.offset);
	}
	// supertype.
	if (raw.super_class()!=null) { // only null for java.lang.Object.
	    System.out.print(" extends ");
	    if (gjsig==null)
		System.out.print(desc2name("L"+raw.super_class().name()+";"));
	    else {
		OffsetAndString oas = munchClassTypeSig(gjsig);
		System.out.print(oas.string);
		gjsig = gjsig.substring(oas.offset);
	    }
	}
	// interfaces
	if (raw.interfaces_count() > 0) {
	    System.out.print(" implements ");
	    for (int i=0; i<raw.interfaces_count(); i++) {
		if (gjsig==null)
		    System.out.print
			(desc2name("L"+raw.interfaces(i).name()+";"));
		else {
		    OffsetAndString oas = munchClassTypeSig(gjsig);
		    System.out.print(oas.string);
		    gjsig = gjsig.substring(oas.offset);
		}
		if (i+1 < raw.interfaces_count())
		    System.out.print(", ");
	    }
	}
	System.out.println();
	System.out.println("{");
	// fields.
	for (int i=0; i<raw.fields_count(); i++) {
	    FieldInfo fi = raw.fields[i];
	    AttributeSignature fis = (AttributeSignature)
		findAttribute(fi.attributes, "Signature");
	    System.out.print("    "); // indent.
	    // access flags
	    System.out.print(modString(fi.access_flags, false));
	    // type/signature.
	    if (fis==null)
		System.out.print(desc2name(fi.descriptor()));
	    else
		System.out.print(munchTypeSig(fis.signature()).string);
	    System.out.print(" ");
	    // field name.
	    System.out.print(fi.name());
	    System.out.print(";");
	    System.out.println();
	}
	// methods.
	for (int i=0; i<raw.methods_count(); i++) {
	    MethodInfo mi = raw.methods[i];
	    AttributeSignature mis = (AttributeSignature)
		findAttribute(mi.attributes, "Signature");
	    // assign descriptor.
	    String md;
	    if (mis!=null) md = mis.signature();
	    else {
		md = mi.descriptor();
		// add throws clauses.
		AttributeExceptions ae = (AttributeExceptions)
		    findAttribute(mi.attributes, "Exceptions");
		for (int j=0; ae!=null && j<ae.number_of_exceptions(); j++) {
		    ConstantClass cc = ae.exception_index_table(j);
		    if (cc==null) continue;
		    md+="^L"+cc.name()+";";
		}
	    }
	    // indent.
	    System.out.print("    ");
	    // access flags
	    System.out.print(modString(mi.access_flags, false));
	    // type formal parameters
	    if (md.charAt(0)=='<') {
		OffsetAndString oas = munchParamPart(md);
		System.out.print(oas.string);
		System.out.print(" ");
		md = md.substring(oas.offset);
	    }
	    // return type (skip to the end...)
	    OffsetAndString ret_oas = munchTypeSig
		(md.substring(md.indexOf(')')+1));
	    System.out.print(ret_oas.string);
	    System.out.print(" ");
	    // method name!
	    System.out.print(mi.name());
	    // parameters.
	    System.out.print('(');
	    assert md.charAt(0)=='(';
	    for (int off=1; md.charAt(off)!=')'; ) {
		OffsetAndString param_oas = munchTypeSig(md.substring(off));
		if (off!=1) System.out.print(", ");
		System.out.print(param_oas.string);
		off += param_oas.offset;
	    }
	    System.out.print(')');
	    // 'throws' list.
	    md = md.substring(md.indexOf(')')+1+ret_oas.offset);
	    for (int off=0; off < md.length() && md.charAt(off)=='^'; ) {
		off++;
		OffsetAndString thr_oas = munchTypeSig(md.substring(off));
		if (off==1) System.out.print(" throws ");
		else System.out.print(", ");
		System.out.print(thr_oas.string);
		off += thr_oas.offset;
	    }
	    // done!
	    System.out.print(';');
	    System.out.println();
	}
	System.out.println("}");
    }

    static Attribute findAttribute(ClassFile cf, String name) {
	return findAttribute(cf.attributes, name);
    }
    static Attribute findAttribute(Attribute[] attributes, String name) {
	for (int i=0; i<attributes.length; i++)
	    if (attributes[i].attribute_name().equals(name))
		return attributes[i];
	return null;
    }

    static String modString(AccessFlags af, boolean isClass) {
	StringBuffer sb = new StringBuffer();
	if (af.isPrivate()) sb.append("private ");
	if (af.isProtected()) sb.append("protected ");
	if (af.isPublic()) sb.append("public ");
	if (af.isAbstract() && !af.isInterface())
	    sb.append("abstract ");
	if (af.isStatic()) sb.append("static ");
	if (af.isFinal()) sb.append("final ");
	if (af.isTransient()) sb.append("transient ");
	if (af.isVolatile()) sb.append("volatile ");
	if (af.isSynchronized() && !isClass) sb.append("synchronized ");
	if (af.isNative()) sb.append("native ");
	if (af.isStrict()) sb.append("strict ");
	if (af.isInterface()) sb.append("interface ");
	else if (isClass) sb.append("class ");
	return sb.toString();
    }
    static String desc2name(String descriptor) {
	switch(descriptor.charAt(0)) {
	case '[': // arrays
	    return desc2name(descriptor.substring(1))+"[]";
	 case 'L': // object type.
	     return descriptor
		 .substring(1, descriptor.indexOf(';'))
		 .replace('/','.');
	    // primitive types
	case 'B': return "byte";
	case 'C': return "char";
	case 'D': return "double";
	case 'F': return "float";
	case 'I': return "int";
	case 'J': return "long";
	case 'S': return "short";
	case 'Z': return "boolean";
	case 'V': return "void";
	default:
	    assert false : "bad descriptor: "+descriptor;
	    return "<unknown>";
	}
    }
    // more sophisticated parser, for gj sigs.
    static class OffsetAndString {
	final String string;
	final int offset;
	OffsetAndString(String string, int offset) {
	    this.string = string; this.offset = offset;
	}
    }
    static OffsetAndString munchParamPart(String psig) {
	assert psig.charAt(0)=='<' && psig.indexOf('>')>0;
	StringBuffer sb = new StringBuffer("<");
	int off = 1;
	boolean first = true;
	while (psig.charAt(off)!='>') {
	    int colon = psig.indexOf(':', off);
	    String name = psig.substring(off, colon);
	    off = colon;
	    // make sb.
	    if (first) first=false;
	    else sb.append(", ");
	    sb.append(name);
	    // back to parsing.
	    boolean firstbound=true;
	    // note that bounds of
	    //    ':Ljava/lang/Object/Object;:Ljava/lang/Comparable;'
	    // is different from (has a different erasure than)
	    //    '::Ljava/lang/Comparable;'
	    // [The first is declared as 'extends Object & Comparable'
	    //  while the second is declared as 'extends Comparable' ]
	    while (psig.charAt(off)==':') {
		off++;
		if (psig.charAt(off)==':') {
		    // no class type specified.
		    continue;
		}
		OffsetAndString oas = munchTypeSig(psig.substring(off));
		off += oas.offset;
		if (firstbound) { sb.append(" extends "); firstbound=false; }
		else sb.append(" & ");
		sb.append(oas.string);
	    }
	}
	off++;
	sb.append(">");
	return new OffsetAndString(sb.toString(), off);
    }
    static OffsetAndString munchClassTypeSig(String descriptor) {
	assert descriptor.charAt(0)=='L';
	StringBuffer sb = new StringBuffer();
	int off;
	int semi = descriptor.indexOf(';');
	int brack= descriptor.indexOf('<');
	if (brack!=-1 && brack < semi) {
	    // ooh, ooh, type parameters!
	    sb.append
		(descriptor.substring(1, brack).replace('/','.'));
	    off = brack;
	    OffsetAndString oas = munchTypeArguments
		(descriptor.substring(off));
	    sb.append(oas.string);
	    off+=oas.offset;
	    assert descriptor.charAt(off)==';';
	    off++;
	} else {
	    sb.append(descriptor
		      .substring(1, descriptor.indexOf(';'))
		      .replace('/','.'));
	    off = descriptor.indexOf(';')+1;
	}
	// optional '.ID<type args>'
	while (off < descriptor.length() && descriptor.charAt(off)=='.') {
	    int semi2 = descriptor.indexOf(';', off+1);
	    String id = descriptor.substring(off+1, semi2);
	    off=semi2+1;
	    sb.append('.'); sb.append(id);
	    // optional type arguments.
	    if (off < descriptor.length() && descriptor.charAt(off)=='<') {
		OffsetAndString oas = munchTypeArguments
		    (descriptor.substring(off));
		sb.append(oas.string);
		off+=oas.offset;
	    }
	}
	// okay, finally done.
	return new OffsetAndString(sb.toString(), off);
    }
    static OffsetAndString munchTypeArguments(String descriptor) {
	assert descriptor.charAt(0)=='<';
	assert descriptor.indexOf('>')!=-1;
	StringBuffer sb = new StringBuffer();
	int off = 1;
	sb.append('<');
	boolean first=true;
	while (descriptor.charAt(off)!='>') {
	    OffsetAndString oas =
		munchTypeSig(descriptor.substring(off));
	    if (first) first=false;
	    else sb.append(", ");
	    sb.append(oas.string);
	    off+=oas.offset;
	}
	sb.append('>');
	off++;
	return new OffsetAndString(sb.toString(), off);
    }
    static OffsetAndString munchMethodTypeSig(String descriptor) {
	assert descriptor.charAt(0)=='<' || descriptor.charAt(0)=='(';
	StringBuffer sb = new StringBuffer();
	int off = 0;
	if (descriptor.charAt(off)=='<') {
	    OffsetAndString oas = munchParamPart(descriptor);
	    sb.append(oas.string);
	    off+=oas.offset;
	}
	assert descriptor.charAt(off)=='(';
	off++;
	while (descriptor.charAt(off)!=')') {
	    // method parameters
	    OffsetAndString oas = munchTypeSig(descriptor.substring(off));
	    off+=oas.offset;
	}
	off++;
	// return value type.
	{
	    OffsetAndString oas = munchTypeSig(descriptor.substring(off));
	    off+=oas.offset;
	}
	// optional throws signatures.
	while (off < descriptor.length() && descriptor.charAt(off)=='^') {
	    off++;
	    OffsetAndString oas = munchTypeSig(descriptor.substring(off));
	    off+=oas.offset;
	}
	// done.
	return new OffsetAndString(sb.toString(), off);
    }
    static OffsetAndString munchTypeSig(String descriptor) {
	switch(descriptor.charAt(0)) {
	case '[': { // arrays
	    int arrcnt=1;
	    while (descriptor.charAt(arrcnt)=='[')
		arrcnt++;
	    OffsetAndString oas = munchTypeSig(descriptor.substring(arrcnt));
	    return new OffsetAndString
		(oas.string+Util.repeatString("[]",arrcnt), oas.offset+arrcnt);
	}
	 case 'L': // object type.
	     return munchClassTypeSig(descriptor);
	case 'T': // type variable signature
	    return new OffsetAndString
		(descriptor.substring(1, descriptor.indexOf(';')),
		 descriptor.indexOf(';')+1);
	case '<':
	case '(': // method type signature
	    return munchMethodTypeSig(descriptor);
	    // primitive types
	case 'B': return new OffsetAndString("byte", 1);
	case 'C': return new OffsetAndString("char", 1);
	case 'D': return new OffsetAndString("double", 1);
	case 'F': return new OffsetAndString("float", 1);
	case 'I': return new OffsetAndString("int", 1);
	case 'J': return new OffsetAndString("long", 1);
	case 'S': return new OffsetAndString("short", 1);
	case 'Z': return new OffsetAndString("boolean", 1);
	case 'V': return new OffsetAndString("void", 1);
	default:
	    assert false : "bad descriptor: "+descriptor;
	    return new OffsetAndString("<unknown>", 1);
	}
    }
}
