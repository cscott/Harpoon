// BitFieldNumbering.java, created Sun Mar  4 20:21:45 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.Linker;
import harpoon.Util.Util;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <code>BitFieldNumbering</code> finds a bit-position and a field to
 * embed boolean flags describing object fields.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: BitFieldNumbering.java,v 1.4.2.2 2003-07-21 20:51:04 cananian Exp $
 */
public class BitFieldNumbering {
    // note: for 64-bit archs, might be worthwhile to make fields 64 bits.
    public static final HClass FIELD_TYPE = HClass.Int;
    public static final int BITS_IN_FIELD = 32;

    // unique suffix for the fields created by this BitFieldNumbering.
    private final String suffix;
    // cache HClass for java.lang.Object
    private final HClass HCobject;
    // set of all referenced 'bitfield' fields: mutable version.
    private final Set<HField> _bitfields = new HashSet<HField>();
    /** Set of all fields returned as part of a <code>BitFieldTuple</code>
     *  by <code>bfLoc</code> or <code>arrayBitField</code>. */
    public final Set<HField> bitfields =
	Collections.unmodifiableSet(_bitfields);

    /** Creates a <code>BitFieldNumbering</code>. */
    public BitFieldNumbering(Linker l) { this(l, ""); }
    public BitFieldNumbering(Linker l, String suffix) {
	this.suffix=suffix;
	this.HCobject = l.forName("java.lang.Object");
    }

    public static class BitFieldTuple {
	public final HField field;
	public final int bit;
	BitFieldTuple(HField field, int bit) {this.field=field; this.bit=bit;}
	public String toString() { return "Bit "+bit+" of "+field; }
    }
    public BitFieldTuple bfLoc(HField hf) {
	int n = fieldNumber(hf);
	// which class would the check field belong to?
	// answer: same class as contains the definition of field #(marker)
	int marker = BITS_IN_FIELD * (n/BITS_IN_FIELD);
	HClass hc = hf.getDeclaringClass();
	if (marker!=0) {
	    while (classNumber(hc.getSuperclass()) > marker)
		hc = hc.getSuperclass();
	} else {
	    /* special case: zero'th field goes in java.lang.Object.
	     * this is to make the array case more regular. */
	    hc = HCobject;
	}
	// okay, fetch this field, creating if necessary.
	HField bff = getOrMake(hc, n/BITS_IN_FIELD);
	// done.
	return new BitFieldTuple(bff, n % BITS_IN_FIELD);
    }
    public HField arrayBitField(HClass hc) {
	assert hc.isArray();
	/* okay, first 'field info' field is used for arrays. */
	return getOrMake(HCobject, 0);
    }

    // fetch a bitfield, creating if necessary.
    private HField getOrMake(HClass where, int which) {
	/* for safety: always call classNumber(where) to cache the field
	 * numbering for 'where' *before* we screw it up by adding fields. */
	classNumber(where);
	/* okay, now fetch/make the bitfield field. */
	String fieldname="$$bitfield"+which+suffix;
	try {
	    return where.getDeclaredField(fieldname);
	} catch (NoSuchFieldError nsfe) {
	    HField hf =
		where.getMutator().addDeclaredField(fieldname, FIELD_TYPE);
	    _bitfields.add(hf);
	    return hf;
	}
    }
    // field numbering.
    final Map<HField,Integer> fieldNumbers = new HashMap<HField,Integer>();
    final Map<HClass,Integer> classNumbers = new HashMap<HClass,Integer>();
    private int fieldNumber(HField hf) {
	assert !hf.isStatic();
	assert !hf.getDeclaringClass().isInterface();
	if (!fieldNumbers.containsKey(hf))
	    classNumber(hf.getDeclaringClass());
	assert fieldNumbers.containsKey(hf) : hf + " / "+fieldNumbers;
	return fieldNumbers.get(hf).intValue();
    }
    /* all fields in 'hc' are numbered *strictly less than* classNumber(hc) */
    private int classNumber(HClass hc) {
	assert !hc.isArray();
	assert !hc.isInterface();
	if (!classNumbers.containsKey(hc)) {
	    HClass sc = hc.getSuperclass();
	    int start = (sc==null) ? 0 : classNumber(sc);
	    HField[] hfa = hc.getDeclaredFields();
	    for (int i=0; i<hfa.length; i++)
		if (!hfa[i].isStatic())
		    fieldNumbers.put(hfa[i], new Integer(start++));
	    classNumbers.put(hc, new Integer(start));
	}
	return classNumbers.get(hc).intValue();
    }
}
