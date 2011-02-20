// IR_SourceLanguage.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IR_SourceLanguage</code>:
 * An enumerated type must be provided, called <code>IR_SourceLanguage</code>,
 * which specifies the original source language in which an 
 * AIRE/CE fragment originated.
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IR_SourceLanguage.java,v 1.5 2002-02-25 21:03:59 cananian Exp $
 */
public class IR_SourceLanguage {
    public final static IR_SourceLanguage IR_VHDL87_SOURCE = _(0);
    public final static IR_SourceLanguage IR_VHDL93_SOURCE = _(1);
    public final static IR_SourceLanguage IR_VHDL98_SOURCE = _(2);
    public final static IR_SourceLanguage IR_VHDLAMS98_SOURCE = _(3);
    public final static IR_SourceLanguage IR_VERILOG95_SOURCE = _(4);
    public final static IR_SourceLanguage IR_VERILOG98_SOURCE = _(5);
    public final static IR_SourceLanguage IR_JAVA_SOURCE = _(6);

    public String toString() {
	if (this==IR_VHDL87_SOURCE) return "IR_VHDL87_SOURCE";
	if (this==IR_VHDL93_SOURCE) return "IR_VHDL93_SOURCE";
	if (this==IR_VHDL98_SOURCE) return "IR_VHDL98_SOURCE";
	if (this==IR_VHDLAMS98_SOURCE) return "IR_VHDLAMS98_SOURCE";
	if (this==IR_VERILOG95_SOURCE) return "IR_VERILOG95_SOURCE";
	if (this==IR_VERILOG98_SOURCE) return "IR_VERILOG98_SOURCE";
	if (this==IR_JAVA_SOURCE) return "IR_JAVA_SOURCE";
	throw new Error("Unknown IR_SourceLanguage: "+_source);
    }
    
    // private implementation.
    private final int _source;
    private IR_SourceLanguage(int source) { _source = source; }
    private static IR_SourceLanguage _(int source)
    { return new IR_SourceLanguage(source); }
}
