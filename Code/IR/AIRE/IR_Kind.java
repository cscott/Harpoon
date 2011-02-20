// IR_Kind.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

import java.util.Hashtable;
/**
 * <code>IR_Kind</code>:
 * An enumerated type must be provided, called <code>IR_Kind</code>,
 * which uniquely identifies the IIR class associated with a particular
 * object created from an IIR class.  The enumeration may be implemented
 * as a true enumerated type or (preferred) as an integer and constant
 * set.  In either case, the type must include the following labels
 * prior to any labels associated with completely new, instantiable IIR
 * extension classes:
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IR_Kind.java,v 1.8 2002-02-25 21:03:59 cananian Exp $
 */
public final class IR_Kind {
    public final static IR_Kind IR_DESIGN_FILE = _(1);
    public final static IR_Kind IR_COMMENT = _(2);
    public final static IR_Kind IR_IDENTIFIER = _(3);
    public final static IR_Kind IR_INTEGER_LITERAL = _(4);
    public final static IR_Kind IR_INTEGER_LITERAL32 = _(5);
    public final static IR_Kind IR_INTEGER_LITERAL64 = _(6);
    public final static IR_Kind IR_FLOATING_POINT_LITERAL = _(7);
    public final static IR_Kind IR_FLOATING_POINT_LITERAL32 = _(8);
    public final static IR_Kind IR_FLOATING_POINT_LITERAL64 = _(9);
    public final static IR_Kind IR_CHARACTER_LITERAL = _(10);
    public final static IR_Kind IR_BIT_STRING_LITERAL = _(11);
    public final static IR_Kind IR_STRING_LITERAL = _(12);
    public final static IR_Kind IR_ASSOCIATION_ELEMENT = _(13);
    public final static IR_Kind IR_ASSOCIATION_ELEMENT_BY_EXPRESSION = _(14);
    public final static IR_Kind IR_ASSOCIATION_ELEMENT_OPEN = _(15);
    public final static IR_Kind IR_BREAK_ELEMENT = _(16);
    public final static IR_Kind IR_CASE_STATEMENT_ALTERNATIVE_BY_EXPRESSION = _(17);
    public final static IR_Kind IR_CASE_STATEMENT_ALTERNATIVE_BY_CHOICES = _(18);
    public final static IR_Kind IR_CASE_STATEMENT_ALTERNATIVE_BY_OPEN = _(19);
    public final static IR_Kind IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS = _(19); // alt
    public final static IR_Kind IR_CHOICE = _(20);
    public final static IR_Kind IR_CONDITIONAL_WAVEFORM = _(21);
    public final static IR_Kind IR_BLOCK_CONFIGURATION = _(22);
    public final static IR_Kind IR_COMPONENT_CONFIGURATION = _(23);
    public final static IR_Kind IR_DESIGNATOR_EXPLICIT = _(24);
    public final static IR_Kind IR_DESIGNATOR_BY_OTHERS = _(25);
    public final static IR_Kind IR_DESIGNATOR_BY_ALL = _(26);
    public final static IR_Kind IR_ENTITY_CLASS_ENTRY = _(27);
    public final static IR_Kind IR_ELSIF = _(28);
    public final static IR_Kind IR_GROUP_CONSTITUENT = _(29); // FIR only
    public final static IR_Kind IR_SELECTED_WAVEFORM = _(30);
    public final static IR_Kind IR_SIMULTANEOUS_ALTERNATIVE_BY_EXPRESSION = _(31);
    public final static IR_Kind IR_SIMULTANEOUS_ALTERNATIVE_BY_CHOICES = _(32);
    public final static IR_Kind IR_SIMULTANEOUS_ALTERNATIVE_BY_OTHERS = _(33);
    public final static IR_Kind IR_SIMULTANEOUS_ELSIF = _(34);
    public final static IR_Kind IR_WAVEFORM_ELEMENT = _(35);
    public final static IR_Kind IR_ASSOCIATION_LIST = _(36);
    public final static IR_Kind IR_ATTRIBUTE_SPECIFICATION_LIST = _(37);
    public final static IR_Kind IR_BREAK_LIST = _(38);
    public final static IR_Kind IR_CASE_ALTERNATIVE_LIST = _(39);
    public final static IR_Kind IR_CASE_STATEMENT_ALTERNATIVE_LIST = _(39); // alt
    public final static IR_Kind IR_CHOICE_LIST = _(40);
    public final static IR_Kind IR_COMMENT_LIST = _(41);
    public final static IR_Kind IR_CONCURRENT_STATEMENT_LIST = _(42);
    public final static IR_Kind IR_CONDITIONAL_WAVEFORM_LIST = _(43);
    public final static IR_Kind IR_CONFIGURATION_ITEM_LIST = _(44);
    public final static IR_Kind IR_DECLARATION_LIST = _(45);
    public final static IR_Kind IR_DESIGN_FILE_LIST = _(46);
    public final static IR_Kind IR_DESIGNATOR_LIST = _(47);
    public final static IR_Kind IR_ELEMENT_DECLARATION_LIST = _(48);
    public final static IR_Kind IR_NATURE_ELEMENT_DECLARATION_LIST = _(1000); // IIR ONLY!
    public final static IR_Kind IR_ENTITY_CLASS_ENTRY_LIST = _(49);
    public final static IR_Kind IR_ENUMERATION_LITERAL_LIST = _(50);
    public final static IR_Kind IR_GENERIC_LIST = _(51);
    public final static IR_Kind IR_INTERFACE_LIST = _(52);
    public final static IR_Kind IR_LIBRARY_UNIT_LIST = _(53);
    public final static IR_Kind IR_PORT_LIST = _(54);
    public final static IR_Kind IR_SELECTED_WAVEFORM_LIST = _(55);
    public final static IR_Kind IR_SEQUENTIAL_STATEMENT_LIST = _(56);
    public final static IR_Kind IR_SIMULTANEOUS_ALTERNATIVE_LIST = _(57);
    public final static IR_Kind IR_SIMULTANEOUS_STATEMENT_LIST = _(58);
    public final static IR_Kind IR_STATEMENT_LIST = _(999); // IR ONLY!
    public final static IR_Kind IR_UNIT_LIST = _(59);
    public final static IR_Kind IR_WAVEFORM_LIST = _(60);
    public final static IR_Kind IR_ENUMERATION_TYPE_DEFINITION = _(61);
    public final static IR_Kind IR_ENUMERATION_SUBTYPE_DEFINITION = _(62);
    public final static IR_Kind IR_INTEGER_TYPE_DEFINITION = _(63);
    public final static IR_Kind IR_INTEGER_SUBTYPE_DEFINITION = _(64);
    public final static IR_Kind IR_FLOATING_TYPE_DEFINITION = _(65);
    public final static IR_Kind IR_FLOATING_SUBTYPE_DEFINITION = _(66);
    public final static IR_Kind IR_PHYSICAL_TYPE_DEFINITION = _(67);
    public final static IR_Kind IR_PHYSICAL_SUBTYPE_DEFINITION = _(68);
    public final static IR_Kind IR_RANGE_TYPE_DEFINITION = _(69);
    public final static IR_Kind IR_SCALAR_NATURE_DEFINITION = _(998); // IIR ONLY!
    public final static IR_Kind IR_SCALAR_SUBNATURE_DEFINITION = _(997); // IIR ONLY!
    public final static IR_Kind IR_ARRAY_TYPE_DEFINITION = _(70);
    public final static IR_Kind IR_ARRAY_SUBTYPE_DEFINITION = _(71);
    public final static IR_Kind IR_ARRAY_NATURE_DEFINITION = _(996); // IIR ONLY!
    public final static IR_Kind IR_ARRAY_SUBNATURE_DEFINITION = _(995); // IIR ONLY!
    public final static IR_Kind IR_RECORD_TYPE_DEFINITION = _(72);
    public final static IR_Kind IR_RECORD_SUBTYPE_DEFINITION = _(994); // IIR ONLY!
    public final static IR_Kind IR_RECORD_NATURE_DEFINITION = _(993); // IIR ONLY!
    public final static IR_Kind IR_RECORD_SUBNATURE_DEFINITION = _(992); // IIR ONLY!
    public final static IR_Kind IR_ACCESS_TYPE_DEFINITION = _(73);
    public final static IR_Kind IR_ACCESS_SUBTYPE_DEFINITION = _(74);
    public final static IR_Kind IR_FILE_TYPE_DEFINITION = _(75);
    public final static IR_Kind IR_SIGNATURE = _(76);
    public final static IR_Kind IR_FUNCTION_DECLARATION = _(77);
    public final static IR_Kind IR_PROCEDURE_DECLARATION = _(78);
    public final static IR_Kind IR_ELEMENT_DECLARATION = _(79);
    public final static IR_Kind IR_NATURE_ELEMENT_DECLARATION = _(991); // IIR ONLY!
    public final static IR_Kind IR_ENUMERATION_LITERAL = _(990); // IIR ONLY!
    public final static IR_Kind IR_TYPE_DECLARATION = _(80);
    public final static IR_Kind IR_SUBTYPE_DECLARATION = _(81);
    public final static IR_Kind IR_NATURE_DECLARATION = _(989); // IIR ONLY!
    public final static IR_Kind IR_SUBNATURE_DECLARATION = _(988); // IIR ONLY!
    public final static IR_Kind IR_CONSTANT_DECLARATION = _(82);
    public final static IR_Kind IR_FILE_DECLARATION = _(83);
    public final static IR_Kind IR_SIGNAL_DECLARATION = _(84);
    public final static IR_Kind IR_SHARED_VARIABLE_DECLARATION = _(85);
    public final static IR_Kind IR_VARIABLE_DECLARATION = _(86);
    public final static IR_Kind IR_TERMINAL_DECLARATION = _(87);
    public final static IR_Kind IR_FREE_QUANTITY_DECLARATION = _(88);
    public final static IR_Kind IR_ACROSS_QUANTITY_DECLARATION = _(987); // IIR ONLY!
    public final static IR_Kind IR_THROUGH_QUANTITY_DECLARATION = _(986); // IIR ONLY!
    public final static IR_Kind IR_SPECTRUM_SOURCE_QUANTITY_DECLARATION = _(985); // IIR ONLY!
    public final static IR_Kind IR_NOISE_SOURCE_QUANTITY_DECLARATION = _(984); // IIR ONLY!
    public final static IR_Kind IR_BRANCH_QUANTITY_DECLARATION = _(89); // fir only
    public final static IR_Kind IR_CONSTANT_INTERFACE_DECLARATION = _(90);
    public final static IR_Kind IR_FILE_INTERFACE_DECLARATION = _(91);
    public final static IR_Kind IR_SIGNAL_INTERFACE_DECLARATION = _(92);
    public final static IR_Kind IR_VARIABLE_INTERFACE_DECLARATION = _(93);
    public final static IR_Kind IR_TERMINAL_INTERFACE_DECLARATION = _(94);
    public final static IR_Kind IR_QUANTITY_INTERFACE_DECLARATION = _(95);
    public final static IR_Kind IR_ALIAS_DECLARATION = _(96);
    public final static IR_Kind IR_ATTRIBUTE_DECLARATION = _(97);
    public final static IR_Kind IR_COMPONENT_DECLARATION = _(98);
    public final static IR_Kind IR_GROUP_DECLARATION = _(99);
    public final static IR_Kind IR_GROUP_TEMPLATE_DECLARATION = _(100);
    public final static IR_Kind IR_LIBRARY_DECLARATION = _(101);
    public final static IR_Kind IR_ENTITY_DECLARATION = _(102);
    public final static IR_Kind IR_ARCHITECTURE_DECLARATION = _(103);
    public final static IR_Kind IR_PACKAGE_DECLARATION = _(104);
    public final static IR_Kind IR_PACKAGE_BODY_DECLARATION = _(105);
    public final static IR_Kind IR_CONFIGURATION_DECLARATION = _(106);
    public final static IR_Kind IR_PHYSICAL_UNIT = _(107);
    public final static IR_Kind IR_ATTRIBUTE_SPECIFICATION = _(108);
    public final static IR_Kind IR_CONFIGURATION_SPECIFICATION = _(109);
    public final static IR_Kind IR_DISCONNECTION_SPECIFICATION = _(110);
    public final static IR_Kind IR_LABEL = _(111);
    public final static IR_Kind IR_LIBRARY_CLAUSE = _(112);
    public final static IR_Kind IR_USE_CLAUSE = _(113);
    public final static IR_Kind IR_SIMPLE_NAME = _(983); // IIR ONLY!
    public final static IR_Kind IR_SELECTED_NAME = _(114);
    public final static IR_Kind IR_SELECTED_NAME_BY_ALL = _(115);
    public final static IR_Kind IR_INDEXED_NAME = _(116);
    public final static IR_Kind IR_SLICE_NAME = _(117);
    public final static IR_Kind IR_USER_ATTRIBUTE = _(118);
    public final static IR_Kind IR_BASE_ATTRIBUTE = _(119);
    public final static IR_Kind IR_LEFT_ATTRIBUTE = _(120);
    public final static IR_Kind IR_RIGHT_ATTRIBUTE = _(121);
    public final static IR_Kind IR_LOW_ATTRIBUTE = _(122);
    public final static IR_Kind IR_HIGH_ATTRIBUTE = _(123);
    public final static IR_Kind IR_ASCENDING_ATTRIBUTE = _(124);
    public final static IR_Kind IR_IMAGE_ATTRIBUTE = _(125);
    public final static IR_Kind IR_VALUE_ATTRIBUTE = _(126);
    public final static IR_Kind IR_POS_ATTRIBUTE = _(127);
    public final static IR_Kind IR_VAL_ATTRIBUTE = _(128);
    public final static IR_Kind IR_SUCC_ATTRIBUTE = _(129);
    public final static IR_Kind IR_PRED_ATTRIBUTE = _(130);
    public final static IR_Kind IR_LEFT_OF_ATTRIBUTE = _(131);
    public final static IR_Kind IR_RIGHT_OF_ATTRIBUTE = _(132);
    public final static IR_Kind IR_RANGE_ATTRIBUTE = _(133);
    public final static IR_Kind IR_REVERSE_RANGE_ATTRIBUTE = _(134);
    public final static IR_Kind IR_LENGTH_ATTRIBUTE = _(135);
    public final static IR_Kind IR_DELAYED_ATTRIBUTE = _(136);
    public final static IR_Kind IR_STABLE_ATTRIBUTE = _(137);
    public final static IR_Kind IR_QUIET_ATTRIBUTE = _(138);
    public final static IR_Kind IR_TRANSACTION_ATTRIBUTE = _(139);
     //public final static IR_Kind IR_ASCENDING_ATTRIBUTE = _(140); // DUPLICATE! fir only
    public final static IR_Kind IR_EVENT_ATTRIBUTE = _(141);
    public final static IR_Kind IR_ACTIVE_ATTRIBUTE = _(142);
    public final static IR_Kind IR_LAST_EVENT_ATTRIBUTE = _(143);
    public final static IR_Kind IR_LAST_ACTIVE_ATTRIBUTE = _(144);
    public final static IR_Kind IR_LAST_VALUE_ATTRIBUTE = _(145);
    public final static IR_Kind IR_BEHAVIOR_ATTRIBUTE = _(982); // IIR ONLY!
    public final static IR_Kind IR_STRUCTURE_ATTRIBUTE = _(981); // IIR ONLY!
    public final static IR_Kind IR_DRIVING_ATTRIBUTE = _(146);
    public final static IR_Kind IR_DRIVING_VALUE_ATTRIBUTE = _(147);
    public final static IR_Kind IR_SIMPLE_NAME_ATTRIBUTE = _(148);
    public final static IR_Kind IR_INSTANCE_NAME_ATTRIBUTE = _(149);
    public final static IR_Kind IR_PATH_NAME_ATTRIBUTE = _(150);
    public final static IR_Kind IR_ACROSS_ATTRIBUTE = _(151);
    public final static IR_Kind IR_THROUGH_ATTRIBUTE = _(152);
    public final static IR_Kind IR_REFERENCE_ATTRIBUTE = _(153);
    public final static IR_Kind IR_CONTRIBUTION_ATTRIBUTE = _(154);
    public final static IR_Kind IR_TOLERANCE_ATTRIBUTE = _(980); // IIR ONLY!
    public final static IR_Kind IR_DOT_ATTRIBUTE = _(155);
    public final static IR_Kind IR_INTEG_ATTRIBUTE = _(156);
    public final static IR_Kind IR_ABOVE_ATTRIBUTE = _(157);
    public final static IR_Kind IR_ZOH_ATTRIBUTE = _(979); // IIR ONLY!
    public final static IR_Kind IR_LTF_ATTRIBUTE = _(978); // IIR ONLY!
    public final static IR_Kind IR_ZTF_ATTRIBUTE = _(977); // IIR ONLY!
    public final static IR_Kind IR_RAMP_ATTRIBUTE = _(976); // IIR ONLY!
    public final static IR_Kind IR_SLEW_ATTRIBUTE = _(975); // IIR ONLY!
    public final static IR_Kind IR_IDENTITY_OPERATOR = _(158);
    public final static IR_Kind IR_NEGATION_OPERATOR = _(159);
    public final static IR_Kind IR_ABSOLUTE_OPERATOR = _(160);
    public final static IR_Kind IR_NOT_OPERATOR = _(161);
    public final static IR_Kind IR_AND_OPERATOR = _(162);
    public final static IR_Kind IR_OR_OPERATOR = _(163);
    public final static IR_Kind IR_NAND_OPERATOR = _(164);
    public final static IR_Kind IR_NOR_OPERATOR = _(165);
    public final static IR_Kind IR_XOR_OPERATOR = _(166);
    public final static IR_Kind IR_XNOR_OPERATOR = _(167);
    public final static IR_Kind IR_EQUALITY_OPERATOR = _(168);
    public final static IR_Kind IR_INEQUALITY_OPERATOR = _(169);
    public final static IR_Kind IR_LESS_THAN_OPERATOR = _(170);
    public final static IR_Kind IR_LESS_THAN_OR_EQUAL_OPERATOR = _(171);
    public final static IR_Kind IR_GREATER_THAN_OPERATOR = _(172);
    public final static IR_Kind IR_GREATER_THAN_OR_EQUAL_OPERATOR = _(173);
    public final static IR_Kind IR_SLL_OPERATOR = _(174);
    public final static IR_Kind IR_SRL_OPERATOR = _(175);
    public final static IR_Kind IR_SLA_OPERATOR = _(176);
    public final static IR_Kind IR_SRA_OPERATOR = _(177);
    public final static IR_Kind IR_ROL_OPERATOR = _(178);
    public final static IR_Kind IR_ROR_OPERATOR = _(179);
    public final static IR_Kind IR_ADDITION_OPERATOR = _(180);
    public final static IR_Kind IR_SUBTRACTION_OPERATOR = _(181);
    public final static IR_Kind IR_CONCATENATION_OPERATOR = _(182);
    public final static IR_Kind IR_MULTIPLICATION_OPERATOR = _(183);
    public final static IR_Kind IR_DIVISION_OPERATOR = _(184);
    public final static IR_Kind IR_MODULUS_OPERATOR = _(185);
    public final static IR_Kind IR_REMAINDER_OPERATOR = _(186);
    public final static IR_Kind IR_EXPONENTIATION_OPERATOR = _(187);
    public final static IR_Kind IR_FUNCTION_CALL = _(188);
    public final static IR_Kind IR_PHYSICAL_LITERAL = _(189);
    public final static IR_Kind IR_AGGREGATE = _(190);
    public final static IR_Kind IR_OTHERS_INITIALIZATION = _(191);
    public final static IR_Kind IR_QUALIFIED_EXPRESSION = _(192);
    public final static IR_Kind IR_TYPE_CONVERSION = _(193);
    public final static IR_Kind IR_ALLOCATOR = _(194);
    public final static IR_Kind IR_WAIT_STATEMENT = _(195);
    public final static IR_Kind IR_ASSERTION_STATEMENT = _(196);
    public final static IR_Kind IR_REPORT_STATEMENT = _(197);
    public final static IR_Kind IR_SIGNAL_ASSIGNMENT_STATEMENT = _(198);
    public final static IR_Kind IR_VARIABLE_ASSIGNMENT_STATEMENT = _(199);
    public final static IR_Kind IR_PROCEDURE_CALL_STATEMENT = _(200);
    public final static IR_Kind IR_IF_STATEMENT = _(201);
    public final static IR_Kind IR_CASE_STATEMENT = _(202);
    public final static IR_Kind IR_FOR_LOOP_STATEMENT = _(203);
    public final static IR_Kind IR_WHILE_LOOP_STATEMENT = _(204);
    public final static IR_Kind IR_NEXT_STATEMENT = _(205);
    public final static IR_Kind IR_EXIT_STATEMENT = _(206);
    public final static IR_Kind IR_RETURN_STATEMENT = _(207);
    public final static IR_Kind IR_NULL_STATEMENT = _(208);
    public final static IR_Kind IR_BREAK_STATEMENT = _(974); // IIR ONLY!
    public final static IR_Kind IR_BLOCK_STATEMENT = _(209);
    public final static IR_Kind IR_PROCESS_STATEMENT = _(210);
    public final static IR_Kind IR_SENSITIZED_PROCESS_STATEMENT = _(211);
    public final static IR_Kind IR_CONCURRENT_PROCEDURE_CALL_STATEMENT = _(212);
    public final static IR_Kind IR_CONCURRENT_ASSERTION_STATEMENT = _(213);
    public final static IR_Kind IR_CONCURRENT_CONDITIONAL_SIGNAL_ASSIGNMENT = _(214);
    public final static IR_Kind IR_CONCURRENT_SELECTED_SIGNAL_ASSIGNMENT = _(215);
    public final static IR_Kind IR_CONCURRENT_INSTANTIATION_STATEMENT = _(216);
    public final static IR_Kind IR_COMPONENT_INSTANTIATION_STATEMENT = _(216); //alt
    public final static IR_Kind IR_CONCURRENT_GENERATE_FOR_STATEMENT = _(217);
    public final static IR_Kind IR_CONCURRENT_GENERATE_IF_STATEMENT = _(218);
    public final static IR_Kind IR_SIMPLE_SIMULTANEOUS_STATEMENT = _(219);
    public final static IR_Kind IR_CONCURRENT_BREAK_STATEMENT = _(973); // IIR ONLY!
    public final static IR_Kind IR_SIMULTANEOUS_IF_STATEMENT = _(220);
    public final static IR_Kind IR_SIMULTANEOUS_CASE_STATEMENT = _(221);
    public final static IR_Kind IR_SIMULTANEOUS_PROCEDURAL_STATEMENT = _(222);
    public final static IR_Kind IR_SIMULTANEOUS_NULL_STATEMENT = _(223);
    public final static IR_Kind FIR_PROXY_REF = _(224); // fir only
    public final static IR_Kind FIR_PROXY_INDICATOR = _(225); // fir only

    public final static IR_Kind IR_NO_KIND = _(0);

    // private class implementation
    private /*final*/ int _kind; // javac bug doesn't let this be final.
    private IR_Kind(int kind) { _kind = kind; }
    private static IR_Kind _(int k) { return new IR_Kind(k); }

    // int->string mapping table --------------------------------------
    public String toString()
    { return (String) names.get(this); }
    public int hashCode()
    { return _kind; }

    private static Hashtable names = new Hashtable();
    static {
	names.put(IR_DESIGN_FILE,
		  "IR_DESIGN_FILE");
	names.put(IR_COMMENT,
		  "IR_COMMENT");
	names.put(IR_IDENTIFIER,
		  "IR_IDENTIFIER");
	names.put(IR_INTEGER_LITERAL,
		  "IR_INTEGER_LITERAL");
	names.put(IR_FLOATING_POINT_LITERAL,
		  "IR_FLOATING_POINT_LITERAL");
	names.put(IR_CHARACTER_LITERAL,
		  "IR_CHARACTER_LITERAL");
	names.put(IR_BIT_STRING_LITERAL,
		  "IR_BIT_STRING_LITERAL");
	names.put(IR_STRING_LITERAL,
		  "IR_STRING_LITERAL");
	names.put(IR_ASSOCIATION_ELEMENT,
		  "IR_ASSOCIATION_ELEMENT");
	names.put(IR_ASSOCIATION_ELEMENT_BY_EXPRESSION,
		  "IR_ASSOCIATION_ELEMENT_BY_EXPRESSION");
	names.put(IR_ASSOCIATION_ELEMENT_OPEN,
		  "IR_ASSOCIATION_ELEMENT_OPEN");
	names.put(IR_BREAK_ELEMENT,
		  "IR_BREAK_ELEMENT");
	names.put(IR_CASE_STATEMENT_ALTERNATIVE_BY_EXPRESSION,
		  "IR_CASE_STATEMENT_ALTERNATIVE_BY_EXPRESSION");
	names.put(IR_CASE_STATEMENT_ALTERNATIVE_BY_CHOICES,
		  "IR_CASE_STATEMENT_ALTERNATIVE_BY_CHOICES");
	names.put(IR_CASE_STATEMENT_ALTERNATIVE_BY_OPEN,
		  "IR_CASE_STATEMENT_ALTERNATIVE_BY_OPEN");
	names.put(IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS,
		  "IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS");
	names.put(IR_CHOICE,
		  "IR_CHOICE");
	names.put(IR_CONDITIONAL_WAVEFORM,
		  "IR_CONDITIONAL_WAVEFORM");
	names.put(IR_BLOCK_CONFIGURATION,
		  "IR_BLOCK_CONFIGURATION");
	names.put(IR_COMPONENT_CONFIGURATION,
		  "IR_COMPONENT_CONFIGURATION");
	names.put(IR_DESIGNATOR_EXPLICIT,
		  "IR_DESIGNATOR_EXPLICIT");
	names.put(IR_DESIGNATOR_BY_OTHERS,
		  "IR_DESIGNATOR_BY_OTHERS");
	names.put(IR_DESIGNATOR_BY_ALL,
		  "IR_DESIGNATOR_BY_ALL");
	names.put(IR_ENTITY_CLASS_ENTRY,
		  "IR_ENTITY_CLASS_ENTRY");
	names.put(IR_ELSIF,
		  "IR_ELSIF");
	names.put(IR_GROUP_CONSTITUENT,
		  "IR_GROUP_CONSTITUENT");
	names.put(IR_SELECTED_WAVEFORM,
		  "IR_SELECTED_WAVEFORM");
	names.put(IR_SIMULTANEOUS_ALTERNATIVE_BY_EXPRESSION,
		  "IR_SIMULTANEOUS_ALTERNATIVE_BY_EXPRESSION");
	names.put(IR_SIMULTANEOUS_ALTERNATIVE_BY_CHOICES,
		  "IR_SIMULTANEOUS_ALTERNATIVE_BY_CHOICES");
	names.put(IR_SIMULTANEOUS_ALTERNATIVE_BY_OTHERS,
		  "IR_SIMULTANEOUS_ALTERNATIVE_BY_OTHERS");
	names.put(IR_SIMULTANEOUS_ELSIF,
		  "IR_SIMULTANEOUS_ELSIF");
	names.put(IR_WAVEFORM_ELEMENT,
		  "IR_WAVEFORM_ELEMENT");
	names.put(IR_ASSOCIATION_LIST,
		  "IR_ASSOCIATION_LIST");
	names.put(IR_ATTRIBUTE_SPECIFICATION_LIST,
		  "IR_ATTRIBUTE_SPECIFICATION_LIST");
	names.put(IR_BREAK_LIST,
		  "IR_BREAK_LIST");
	names.put(IR_CASE_ALTERNATIVE_LIST,
		  "IR_CASE_ALTERNATIVE_LIST");
	names.put(IR_CASE_STATEMENT_ALTERNATIVE_LIST,
		  "IR_CASE_STATEMENT_ALTERNATIVE_LIST");
	names.put(IR_CHOICE_LIST,
		  "IR_CHOICE_LIST");
	names.put(IR_COMMENT_LIST,
		  "IR_COMMENT_LIST");
	names.put(IR_CONCURRENT_STATEMENT_LIST,
		  "IR_CONCURRENT_STATEMENT_LIST");
	names.put(IR_CONDITIONAL_WAVEFORM_LIST,
		  "IR_CONDITIONAL_WAVEFORM_LIST");
	names.put(IR_CONFIGURATION_ITEM_LIST,
		  "IR_CONFIGURATION_ITEM_LIST");
	names.put(IR_DECLARATION_LIST,
		  "IR_DECLARATION_LIST");
	names.put(IR_DESIGN_FILE_LIST,
		  "IR_DESIGN_FILE_LIST");
	names.put(IR_DESIGNATOR_LIST,
		  "IR_DESIGNATOR_LIST");
	names.put(IR_ELEMENT_DECLARATION_LIST,
		  "IR_ELEMENT_DECLARATION_LIST");
	names.put(IR_ENTITY_CLASS_ENTRY_LIST,
		  "IR_ENTITY_CLASS_ENTRY_LIST");
	names.put(IR_ENUMERATION_LITERAL_LIST,
		  "IR_ENUMERATION_LITERAL_LIST");
	names.put(IR_GENERIC_LIST,
		  "IR_GENERIC_LIST");
	names.put(IR_INTERFACE_LIST,
		  "IR_INTERFACE_LIST");
	names.put(IR_LIBRARY_UNIT_LIST,
		  "IR_LIBRARY_UNIT_LIST");
	names.put(IR_PORT_LIST,
		  "IR_PORT_LIST");
	names.put(IR_SELECTED_WAVEFORM_LIST,
		  "IR_SELECTED_WAVEFORM_LIST");
	names.put(IR_SEQUENTIAL_STATEMENT_LIST,
		  "IR_SEQUENTIAL_STATEMENT_LIST");
	names.put(IR_SIMULTANEOUS_ALTERNATIVE_LIST,
		  "IR_SIMULTANEOUS_ALTERNATIVE_LIST");
	names.put(IR_SIMULTANEOUS_STATEMENT_LIST,
		  "IR_SIMULTANEOUS_STATEMENT_LIST");
	names.put(IR_STATEMENT_LIST,
		  "IR_STATEMENT_LIST");
	names.put(IR_UNIT_LIST,
		  "IR_UNIT_LIST");
	names.put(IR_WAVEFORM_LIST,
		  "IR_WAVEFORM_LIST");
	names.put(IR_ENUMERATION_TYPE_DEFINITION,
		  "IR_ENUMERATION_TYPE_DEFINITION");
	names.put(IR_ENUMERATION_SUBTYPE_DEFINITION,
		  "IR_ENUMERATION_SUBTYPE_DEFINITION");
	names.put(IR_INTEGER_TYPE_DEFINITION,
		  "IR_INTEGER_TYPE_DEFINITION");
	names.put(IR_INTEGER_SUBTYPE_DEFINITION,
		  "IR_INTEGER_SUBTYPE_DEFINITION");
	names.put(IR_FLOATING_TYPE_DEFINITION,
		  "IR_FLOATING_TYPE_DEFINITION");
	names.put(IR_FLOATING_SUBTYPE_DEFINITION,
		  "IR_FLOATING_SUBTYPE_DEFINITION");
	names.put(IR_PHYSICAL_TYPE_DEFINITION,
		  "IR_PHYSICAL_TYPE_DEFINITION");
	names.put(IR_PHYSICAL_SUBTYPE_DEFINITION,
		  "IR_PHYSICAL_SUBTYPE_DEFINITION");
	names.put(IR_RANGE_TYPE_DEFINITION,
		  "IR_RANGE_TYPE_DEFINITION");
	names.put(IR_SCALAR_NATURE_DEFINITION,
		  "IR_SCALAR_NATURE_DEFINITION");
	names.put(IR_SCALAR_SUBNATURE_DEFINITION,
		  "IR_SCALAR_SUBNATURE_DEFINITION");
	names.put(IR_ARRAY_TYPE_DEFINITION,
		  "IR_ARRAY_TYPE_DEFINITION");
	names.put(IR_ARRAY_SUBTYPE_DEFINITION,
		  "IR_ARRAY_SUBTYPE_DEFINITION");
	names.put(IR_ARRAY_NATURE_DEFINITION,
		  "IR_ARRAY_NATURE_DEFINITION");
	names.put(IR_ARRAY_SUBNATURE_DEFINITION,
		  "IR_ARRAY_SUBNATURE_DEFINITION");
	names.put(IR_RECORD_TYPE_DEFINITION,
		  "IR_RECORD_TYPE_DEFINITION");
	names.put(IR_RECORD_SUBTYPE_DEFINITION,
		  "IR_RECORD_SUBTYPE_DEFINITION");
	names.put(IR_RECORD_NATURE_DEFINITION,
		  "IR_RECORD_NATURE_DEFINITION");
	names.put(IR_RECORD_SUBNATURE_DEFINITION,
		  "IR_RECORD_SUBNATURE_DEFINITION");
	names.put(IR_ACCESS_TYPE_DEFINITION,
		  "IR_ACCESS_TYPE_DEFINITION");
	names.put(IR_ACCESS_SUBTYPE_DEFINITION,
		  "IR_ACCESS_SUBTYPE_DEFINITION");
	names.put(IR_FILE_TYPE_DEFINITION,
		  "IR_FILE_TYPE_DEFINITION");
	names.put(IR_SIGNATURE,
		  "IR_SIGNATURE");
	names.put(IR_FUNCTION_DECLARATION,
		  "IR_FUNCTION_DECLARATION");
	names.put(IR_PROCEDURE_DECLARATION,
		  "IR_PROCEDURE_DECLARATION");
	names.put(IR_ELEMENT_DECLARATION,
		  "IR_ELEMENT_DECLARATION");
	names.put(IR_NATURE_ELEMENT_DECLARATION,
		  "IR_NATURE_ELEMENT_DECLARATION");
	names.put(IR_ENUMERATION_LITERAL,
		  "IR_ENUMERATION_LITERAL");
	names.put(IR_TYPE_DECLARATION,
		  "IR_TYPE_DECLARATION");
	names.put(IR_SUBTYPE_DECLARATION,
		  "IR_SUBTYPE_DECLARATION");
	names.put(IR_NATURE_DECLARATION,
		  "IR_NATURE_DECLARATION");
	names.put(IR_SUBNATURE_DECLARATION,
		  "IR_SUBNATURE_DECLARATION");
	names.put(IR_CONSTANT_DECLARATION,
		  "IR_CONSTANT_DECLARATION");
	names.put(IR_FILE_DECLARATION,
		  "IR_FILE_DECLARATION");
	names.put(IR_SIGNAL_DECLARATION,
		  "IR_SIGNAL_DECLARATION");
	names.put(IR_SHARED_VARIABLE_DECLARATION,
		  "IR_SHARED_VARIABLE_DECLARATION");
	names.put(IR_VARIABLE_DECLARATION,
		  "IR_VARIABLE_DECLARATION");
	names.put(IR_TERMINAL_DECLARATION,
		  "IR_TERMINAL_DECLARATION");
	names.put(IR_FREE_QUANTITY_DECLARATION,
		  "IR_FREE_QUANTITY_DECLARATION");
	names.put(IR_ACROSS_QUANTITY_DECLARATION,
		  "IR_ACROSS_QUANTITY_DECLARATION");
	names.put(IR_THROUGH_QUANTITY_DECLARATION,
		  "IR_THROUGH_QUANTITY_DECLARATION");
	names.put(IR_SPECTRUM_SOURCE_QUANTITY_DECLARATION,
		  "IR_SPECTRUM_SOURCE_QUANTITY_DECLARATION");
	names.put(IR_NOISE_SOURCE_QUANTITY_DECLARATION,
		  "IR_NOISE_SOURCE_QUANTITY_DECLARATION");
	names.put(IR_BRANCH_QUANTITY_DECLARATION,
		  "IR_BRANCH_QUANTITY_DECLARATION");
	names.put(IR_CONSTANT_INTERFACE_DECLARATION,
		  "IR_CONSTANT_INTERFACE_DECLARATION");
	names.put(IR_FILE_INTERFACE_DECLARATION,
		  "IR_FILE_INTERFACE_DECLARATION");
	names.put(IR_SIGNAL_INTERFACE_DECLARATION,
		  "IR_SIGNAL_INTERFACE_DECLARATION");
	names.put(IR_VARIABLE_INTERFACE_DECLARATION,
		  "IR_VARIABLE_INTERFACE_DECLARATION");
	names.put(IR_TERMINAL_INTERFACE_DECLARATION,
		  "IR_TERMINAL_INTERFACE_DECLARATION");
	names.put(IR_QUANTITY_INTERFACE_DECLARATION,
		  "IR_QUANTITY_INTERFACE_DECLARATION");
	names.put(IR_ALIAS_DECLARATION,
		  "IR_ALIAS_DECLARATION");
	names.put(IR_ATTRIBUTE_DECLARATION,
		  "IR_ATTRIBUTE_DECLARATION");
	names.put(IR_COMPONENT_DECLARATION,
		  "IR_COMPONENT_DECLARATION");
	names.put(IR_GROUP_DECLARATION,
		  "IR_GROUP_DECLARATION");
	names.put(IR_GROUP_TEMPLATE_DECLARATION,
		  "IR_GROUP_TEMPLATE_DECLARATION");
	names.put(IR_LIBRARY_DECLARATION,
		  "IR_LIBRARY_DECLARATION");
	names.put(IR_ENTITY_DECLARATION,
		  "IR_ENTITY_DECLARATION");
	names.put(IR_ARCHITECTURE_DECLARATION,
		  "IR_ARCHITECTURE_DECLARATION");
	names.put(IR_PACKAGE_DECLARATION,
		  "IR_PACKAGE_DECLARATION");
	names.put(IR_PACKAGE_BODY_DECLARATION,
		  "IR_PACKAGE_BODY_DECLARATION");
	names.put(IR_CONFIGURATION_DECLARATION,
		  "IR_CONFIGURATION_DECLARATION");
	names.put(IR_PHYSICAL_UNIT,
		  "IR_PHYSICAL_UNIT");
	names.put(IR_ATTRIBUTE_SPECIFICATION,
		  "IR_ATTRIBUTE_SPECIFICATION");
	names.put(IR_CONFIGURATION_SPECIFICATION,
		  "IR_CONFIGURATION_SPECIFICATION");
	names.put(IR_DISCONNECTION_SPECIFICATION,
		  "IR_DISCONNECTION_SPECIFICATION");
	names.put(IR_LABEL,
		  "IR_LABEL");
	names.put(IR_LIBRARY_CLAUSE,
		  "IR_LIBRARY_CLAUSE");
	names.put(IR_USE_CLAUSE,
		  "IR_USE_CLAUSE");
	names.put(IR_SIMPLE_NAME,
		  "IR_SIMPLE_NAME");
	names.put(IR_SELECTED_NAME,
		  "IR_SELECTED_NAME");
	names.put(IR_SELECTED_NAME_BY_ALL,
		  "IR_SELECTED_NAME_BY_ALL");
	names.put(IR_INDEXED_NAME,
		  "IR_INDEXED_NAME");
	names.put(IR_SLICE_NAME,
		  "IR_SLICE_NAME");
	names.put(IR_USER_ATTRIBUTE,
		  "IR_USER_ATTRIBUTE");
	names.put(IR_BASE_ATTRIBUTE,
		  "IR_BASE_ATTRIBUTE");
	names.put(IR_LEFT_ATTRIBUTE,
		  "IR_LEFT_ATTRIBUTE");
	names.put(IR_RIGHT_ATTRIBUTE,
		  "IR_RIGHT_ATTRIBUTE");
	names.put(IR_LOW_ATTRIBUTE,
		  "IR_LOW_ATTRIBUTE");
	names.put(IR_HIGH_ATTRIBUTE,
		  "IR_HIGH_ATTRIBUTE");
	names.put(IR_ASCENDING_ATTRIBUTE,
		  "IR_ASCENDING_ATTRIBUTE");
	names.put(IR_IMAGE_ATTRIBUTE,
		  "IR_IMAGE_ATTRIBUTE");
	names.put(IR_VALUE_ATTRIBUTE,
		  "IR_VALUE_ATTRIBUTE");
	names.put(IR_POS_ATTRIBUTE,
		  "IR_POS_ATTRIBUTE");
	names.put(IR_VAL_ATTRIBUTE,
		  "IR_VAL_ATTRIBUTE");
	names.put(IR_SUCC_ATTRIBUTE,
		  "IR_SUCC_ATTRIBUTE");
	names.put(IR_PRED_ATTRIBUTE,
		  "IR_PRED_ATTRIBUTE");
	names.put(IR_LEFT_OF_ATTRIBUTE,
		  "IR_LEFT_OF_ATTRIBUTE");
	names.put(IR_RIGHT_OF_ATTRIBUTE,
		  "IR_RIGHT_OF_ATTRIBUTE");
	names.put(IR_RANGE_ATTRIBUTE,
		  "IR_RANGE_ATTRIBUTE");
	names.put(IR_REVERSE_RANGE_ATTRIBUTE,
		  "IR_REVERSE_RANGE_ATTRIBUTE");
	names.put(IR_LENGTH_ATTRIBUTE,
		  "IR_LENGTH_ATTRIBUTE");
	names.put(IR_DELAYED_ATTRIBUTE,
		  "IR_DELAYED_ATTRIBUTE");
	names.put(IR_STABLE_ATTRIBUTE,
		  "IR_STABLE_ATTRIBUTE");
	names.put(IR_QUIET_ATTRIBUTE,
		  "IR_QUIET_ATTRIBUTE");
	names.put(IR_TRANSACTION_ATTRIBUTE,
		  "IR_TRANSACTION_ATTRIBUTE");
	names.put(IR_ASCENDING_ATTRIBUTE,
		  "IR_ASCENDING_ATTRIBUTE");
	names.put(IR_EVENT_ATTRIBUTE,
		  "IR_EVENT_ATTRIBUTE");
	names.put(IR_ACTIVE_ATTRIBUTE,
		  "IR_ACTIVE_ATTRIBUTE");
	names.put(IR_LAST_EVENT_ATTRIBUTE,
		  "IR_LAST_EVENT_ATTRIBUTE");
	names.put(IR_LAST_ACTIVE_ATTRIBUTE,
		  "IR_LAST_ACTIVE_ATTRIBUTE");
	names.put(IR_LAST_VALUE_ATTRIBUTE,
		  "IR_LAST_VALUE_ATTRIBUTE");
	names.put(IR_BEHAVIOR_ATTRIBUTE,
		  "IR_BEHAVIOR_ATTRIBUTE");
	names.put(IR_STRUCTURE_ATTRIBUTE,
		  "IR_STRUCTURE_ATTRIBUTE");
	names.put(IR_DRIVING_ATTRIBUTE,
		  "IR_DRIVING_ATTRIBUTE");
	names.put(IR_DRIVING_VALUE_ATTRIBUTE,
		  "IR_DRIVING_VALUE_ATTRIBUTE");
	names.put(IR_SIMPLE_NAME_ATTRIBUTE,
		  "IR_SIMPLE_NAME_ATTRIBUTE");
	names.put(IR_INSTANCE_NAME_ATTRIBUTE,
		  "IR_INSTANCE_NAME_ATTRIBUTE");
	names.put(IR_PATH_NAME_ATTRIBUTE,
		  "IR_PATH_NAME_ATTRIBUTE");
	names.put(IR_ACROSS_ATTRIBUTE,
		  "IR_ACROSS_ATTRIBUTE");
	names.put(IR_THROUGH_ATTRIBUTE,
		  "IR_THROUGH_ATTRIBUTE");
	names.put(IR_REFERENCE_ATTRIBUTE,
		  "IR_REFERENCE_ATTRIBUTE");
	names.put(IR_CONTRIBUTION_ATTRIBUTE,
		  "IR_CONTRIBUTION_ATTRIBUTE");
	names.put(IR_TOLERANCE_ATTRIBUTE,
		  "IR_TOLERANCE_ATTRIBUTE");
	names.put(IR_DOT_ATTRIBUTE,
		  "IR_DOT_ATTRIBUTE");
	names.put(IR_INTEG_ATTRIBUTE,
		  "IR_INTEG_ATTRIBUTE");
	names.put(IR_ABOVE_ATTRIBUTE,
		  "IR_ABOVE_ATTRIBUTE");
	names.put(IR_ZOH_ATTRIBUTE,
		  "IR_ZOH_ATTRIBUTE");
	names.put(IR_LTF_ATTRIBUTE,
		  "IR_LTF_ATTRIBUTE");
	names.put(IR_ZTF_ATTRIBUTE,
		  "IR_ZTF_ATTRIBUTE");
	names.put(IR_RAMP_ATTRIBUTE,
		  "IR_RAMP_ATTRIBUTE");
	names.put(IR_SLEW_ATTRIBUTE,
		  "IR_SLEW_ATTRIBUTE");
	names.put(IR_IDENTITY_OPERATOR,
		  "IR_IDENTITY_OPERATOR");
	names.put(IR_NEGATION_OPERATOR,
		  "IR_NEGATION_OPERATOR");
	names.put(IR_ABSOLUTE_OPERATOR,
		  "IR_ABSOLUTE_OPERATOR");
	names.put(IR_NOT_OPERATOR,
		  "IR_NOT_OPERATOR");
	names.put(IR_AND_OPERATOR,
		  "IR_AND_OPERATOR");
	names.put(IR_OR_OPERATOR,
		  "IR_OR_OPERATOR");
	names.put(IR_NAND_OPERATOR,
		  "IR_NAND_OPERATOR");
	names.put(IR_NOR_OPERATOR,
		  "IR_NOR_OPERATOR");
	names.put(IR_XOR_OPERATOR,
		  "IR_XOR_OPERATOR");
	names.put(IR_XNOR_OPERATOR,
		  "IR_XNOR_OPERATOR");
	names.put(IR_EQUALITY_OPERATOR,
		  "IR_EQUALITY_OPERATOR");
	names.put(IR_INEQUALITY_OPERATOR,
		  "IR_INEQUALITY_OPERATOR");
	names.put(IR_LESS_THAN_OPERATOR,
		  "IR_LESS_THAN_OPERATOR");
	names.put(IR_LESS_THAN_OR_EQUAL_OPERATOR,
		  "IR_LESS_THAN_OR_EQUAL_OPERATOR");
	names.put(IR_GREATER_THAN_OPERATOR,
		  "IR_GREATER_THAN_OPERATOR");
	names.put(IR_GREATER_THAN_OR_EQUAL_OPERATOR,
		  "IR_GREATER_THAN_OR_EQUAL_OPERATOR");
	names.put(IR_SLL_OPERATOR,
		  "IR_SLL_OPERATOR");
	names.put(IR_SRL_OPERATOR,
		  "IR_SRL_OPERATOR");
	names.put(IR_SLA_OPERATOR,
		  "IR_SLA_OPERATOR");
	names.put(IR_SRA_OPERATOR,
		  "IR_SRA_OPERATOR");
	names.put(IR_ROL_OPERATOR,
		  "IR_ROL_OPERATOR");
	names.put(IR_ROR_OPERATOR,
		  "IR_ROR_OPERATOR");
	names.put(IR_ADDITION_OPERATOR,
		  "IR_ADDITION_OPERATOR");
	names.put(IR_SUBTRACTION_OPERATOR,
		  "IR_SUBTRACTION_OPERATOR");
	names.put(IR_CONCATENATION_OPERATOR,
		  "IR_CONCATENATION_OPERATOR");
	names.put(IR_MULTIPLICATION_OPERATOR,
		  "IR_MULTIPLICATION_OPERATOR");
	names.put(IR_DIVISION_OPERATOR,
		  "IR_DIVISION_OPERATOR");
	names.put(IR_MODULUS_OPERATOR,
		  "IR_MODULUS_OPERATOR");
	names.put(IR_REMAINDER_OPERATOR,
		  "IR_REMAINDER_OPERATOR");
	names.put(IR_EXPONENTIATION_OPERATOR,
		  "IR_EXPONENTIATION_OPERATOR");
	names.put(IR_FUNCTION_CALL,
		  "IR_FUNCTION_CALL");
	names.put(IR_PHYSICAL_LITERAL,
		  "IR_PHYSICAL_LITERAL");
	names.put(IR_AGGREGATE,
		  "IR_AGGREGATE");
	names.put(IR_OTHERS_INITIALIZATION,
		  "IR_OTHERS_INITIALIZATION");
	names.put(IR_QUALIFIED_EXPRESSION,
		  "IR_QUALIFIED_EXPRESSION");
	names.put(IR_TYPE_CONVERSION,
		  "IR_TYPE_CONVERSION");
	names.put(IR_ALLOCATOR,
		  "IR_ALLOCATOR");
	names.put(IR_WAIT_STATEMENT,
		  "IR_WAIT_STATEMENT");
	names.put(IR_ASSERTION_STATEMENT,
		  "IR_ASSERTION_STATEMENT");
	names.put(IR_REPORT_STATEMENT,
		  "IR_REPORT_STATEMENT");
	names.put(IR_SIGNAL_ASSIGNMENT_STATEMENT,
		  "IR_SIGNAL_ASSIGNMENT_STATEMENT");
	names.put(IR_VARIABLE_ASSIGNMENT_STATEMENT,
		  "IR_VARIABLE_ASSIGNMENT_STATEMENT");
	names.put(IR_PROCEDURE_CALL_STATEMENT,
		  "IR_PROCEDURE_CALL_STATEMENT");
	names.put(IR_IF_STATEMENT,
		  "IR_IF_STATEMENT");
	names.put(IR_CASE_STATEMENT,
		  "IR_CASE_STATEMENT");
	names.put(IR_FOR_LOOP_STATEMENT,
		  "IR_FOR_LOOP_STATEMENT");
	names.put(IR_WHILE_LOOP_STATEMENT,
		  "IR_WHILE_LOOP_STATEMENT");
	names.put(IR_NEXT_STATEMENT,
		  "IR_NEXT_STATEMENT");
	names.put(IR_EXIT_STATEMENT,
		  "IR_EXIT_STATEMENT");
	names.put(IR_RETURN_STATEMENT,
		  "IR_RETURN_STATEMENT");
	names.put(IR_NULL_STATEMENT,
		  "IR_NULL_STATEMENT");
	names.put(IR_BREAK_STATEMENT,
		  "IR_BREAK_STATEMENT");
	names.put(IR_BLOCK_STATEMENT,
		  "IR_BLOCK_STATEMENT");
	names.put(IR_PROCESS_STATEMENT,
		  "IR_PROCESS_STATEMENT");
	names.put(IR_SENSITIZED_PROCESS_STATEMENT,
		  "IR_SENSITIZED_PROCESS_STATEMENT");
	names.put(IR_CONCURRENT_PROCEDURE_CALL_STATEMENT,
		  "IR_CONCURRENT_PROCEDURE_CALL_STATEMENT");
	names.put(IR_CONCURRENT_ASSERTION_STATEMENT,
		  "IR_CONCURRENT_ASSERTION_STATEMENT");
	names.put(IR_CONCURRENT_CONDITIONAL_SIGNAL_ASSIGNMENT,
		  "IR_CONCURRENT_CONDITIONAL_SIGNAL_ASSIGNMENT");
	names.put(IR_CONCURRENT_SELECTED_SIGNAL_ASSIGNMENT,
		  "IR_CONCURRENT_SELECTED_SIGNAL_ASSIGNMENT");
	names.put(IR_CONCURRENT_INSTANTIATION_STATEMENT,
		  "IR_CONCURRENT_INSTANTIATION_STATEMENT");
	names.put(IR_COMPONENT_INSTANTIATION_STATEMENT, // alt
		  "IR_COMPONENT_INSTANTIATION_STATEMENT");
	names.put(IR_CONCURRENT_GENERATE_FOR_STATEMENT,
		  "IR_CONCURRENT_GENERATE_FOR_STATEMENT");
	names.put(IR_CONCURRENT_GENERATE_IF_STATEMENT,
		  "IR_CONCURRENT_GENERATE_IF_STATEMENT");
	names.put(IR_SIMPLE_SIMULTANEOUS_STATEMENT,
		  "IR_SIMPLE_SIMULTANEOUS_STATEMENT");
	names.put(IR_CONCURRENT_BREAK_STATEMENT,
		  "IR_CONCURRENT_BREAK_STATEMENT");
	names.put(IR_SIMULTANEOUS_IF_STATEMENT,
		  "IR_SIMULTANEOUS_IF_STATEMENT");
	names.put(IR_SIMULTANEOUS_CASE_STATEMENT,
		  "IR_SIMULTANEOUS_CASE_STATEMENT");
	names.put(IR_SIMULTANEOUS_PROCEDURAL_STATEMENT,
		  "IR_SIMULTANEOUS_PROCEDURAL_STATEMENT");
	names.put(IR_SIMULTANEOUS_NULL_STATEMENT,
		  "IR_SIMULTANEOUS_NULL_STATEMENT");
	names.put(FIR_PROXY_REF,
		  "FIR_PROXY_REF");
	names.put(FIR_PROXY_INDICATOR,
		  "FIR_PROXY_INDICATOR");
	names.put(IR_NO_KIND,
		  "IR_NO_KIND");

    }
}
