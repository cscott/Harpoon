// IR_Kind.java, created by cananian
package harpoon.IR.AIRE;

import java.util.Hashtable;
/**
 * <code>IR_Kind</code>:
 * An enumerated type must be provided, called <code>IR_Kind</code>,
 * which uniquely identifies the IIR class associated with a particular
 * object created from an IIR class.  The enumeration may be implemented
 * as a true enumerated type or (preferred) as an integer and constant
 * set.  In either case, the type must include the following labels
 * prior to any labels associated with completely new, instatiable IIR
 * extension classes:
 */
public class IR_Kind {
    public static int IR_DESIGN_FILE = 1;
    public static int IR_COMMENT = 2;
    public static int IR_IDENTIFIER = 3;
    public static int IR_INTEGER_LITERAL = 4;
    public static int IR_INTEGER_LITERAL32 = 5;
    public static int IR_INTEGER_LITERAL64 = 6;
    public static int IR_FLOATING_POINT_LITERAL = 7;
    public static int IR_FLOATING_POINT_LITERAL32 = 8;
    public static int IR_FLOATING_POINT_LITERAL64 = 9;
    public static int IR_CHARACTER_LITERAL = 10;
    public static int IR_BIT_STRING_LITERAL = 11;
    public static int IR_STRING_LITERAL = 12;
    public static int IR_ASSOCIATION_ELEMENT = 13;
    public static int IR_ASSOCIATION_ELEMENT_BY_EXPRESSION = 14;
    public static int IR_ASSOCIATION_ELEMENT_OPEN = 15;
    public static int IR_BREAK_ELEMENT = 16;
    public static int IR_CASE_STATEMENT_ALTERNATIVE_BY_EXPRESSION = 17;
    public static int IR_CASE_STATEMENT_ALTERNATIVE_BY_CHOICES = 18;
    public static int IR_CASE_STATEMENT_ALTERNATIVE_BY_OPEN = 19;
    public static int IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS = 19; // alt
    public static int IR_CHOICE = 20;
    public static int IR_CONDITIONAL_WAVEFORM = 21;
    public static int IR_BLOCK_CONFIGURATION = 22;
    public static int IR_COMPONENT_CONFIGURATION = 23;
    public static int IR_DESIGNATOR_EXPLICIT = 24;
    public static int IR_DESIGNATOR_BY_OTHERS = 25;
    public static int IR_DESIGNATOR_BY_ALL = 26;
    public static int IR_ENTITY_CLASS_ENTRY = 27;
    public static int IR_ELSIF = 28;
    public static int IR_GROUP_CONSTITUENT = 29; // FIR only
    public static int IR_SELECTED_WAVEFORM = 30;
    public static int IR_SIMULTANEOUS_ALTERNATIVE_BY_EXPRESSION = 31;
    public static int IR_SIMULTANEOUS_ALTERNATIVE_BY_CHOICES = 32;
    public static int IR_SIMULTANEOUS_ALTERNATIVE_BY_OTHERS = 33;
    public static int IR_SIMULTANEOUS_ELSIF = 34;
    public static int IR_WAVEFORM_ELEMENT = 35;
    public static int IR_ASSOCIATION_LIST = 36;
    public static int IR_ATTRIBUTE_SPECIFICATION_LIST = 37;
    public static int IR_BREAK_LIST = 38;
    public static int IR_CASE_ALTERNATIVE_LIST = 39;
    public static int IR_CHOICE_LIST = 40;
    public static int IR_COMMENT_LIST = 41;
    public static int IR_CONCURRENT_STATEMENT_LIST = 42;
    public static int IR_CONDITIONAL_WAVEFORM_LIST = 43;
    public static int IR_CONFIGURATION_ITEM_LIST = 44;
    public static int IR_DECLARATION_LIST = 45;
    public static int IR_DESIGN_FILE_LIST = 46;
    public static int IR_DESIGNATOR_LIST = 47;
    public static int IR_ELEMENT_DECLARATION_LIST = 48;
    public static int IR_ENTITY_CLASS_ENTRY_LIST = 49;
    public static int IR_ENUMERATION_LITERAL_LIST = 50;
    public static int IR_GENERIC_LIST = 51;
    public static int IR_INTERFACE_LIST = 52;
    public static int IR_LIBRARY_UNIT_LIST = 53;
    public static int IR_PORT_LIST = 54;
    public static int IR_SELECTED_WAVEFORM_LIST = 55;
    public static int IR_SEQUENTIAL_STATEMENT_LIST = 56;
    public static int IR_SIMULTANEOUS_ALTERNATIVE_LIST = 57;
    public static int IR_SIMULTANEOUS_STATEMENT_LIST = 58;
    public static int IR_STATEMENT_LIST = 999; // IR ONLY!
    public static int IR_UNIT_LIST = 59;
    public static int IR_WAVEFORM_LIST = 60;
    public static int IR_ENUMERATION_TYPE_DEFINITION = 61;
    public static int IR_ENUMERATION_SUBTYPE_DEFINITION = 62;
    public static int IR_INTEGER_TYPE_DEFINITION = 63;
    public static int IR_INTEGER_SUBTYPE_DEFINITION = 64;
    public static int IR_FLOATING_TYPE_DEFINITION = 65;
    public static int IR_FLOATING_SUBTYPE_DEFINITION = 66;
    public static int IR_PHYSICAL_TYPE_DEFINITION = 67;
    public static int IR_PHYSICAL_SUBTYPE_DEFINITION = 68;
    public static int IR_RANGE_TYPE_DEFINITION = 69;
    public static int IR_SCALAR_NATURE_DEFINITION = 998; // IIR ONLY!
    public static int IR_SCALAR_SUBNATURE_DEFINITION = 997; // IIR ONLY!
    public static int IR_ARRAY_TYPE_DEFINITION = 70;
    public static int IR_ARRAY_SUBTYPE_DEFINITION = 71;
    public static int IR_ARRAY_NATURE_DEFINITION = 996; // IIR ONLY!
    public static int IR_ARRAY_SUBNATURE_DEFINITION = 995; // IIR ONLY!
    public static int IR_RECORD_TYPE_DEFINITION = 72;
    public static int IR_RECORD_SUBTYPE_DEFINITION = 994; // IIR ONLY!
    public static int IR_RECORD_NATURE_DEFINITION = 993; // IIR ONLY!
    public static int IR_RECORD_SUBNATURE_DEFINITION = 992; // IIR ONLY!
    public static int IR_ACCESS_TYPE_DEFINITION = 73;
    public static int IR_ACCESS_SUBTYPE_DEFINITION = 74;
    public static int IR_FILE_TYPE_DEFINITION = 75;
    public static int IR_SIGNATURE = 76;
    public static int IR_FUNCTION_DECLARATION = 77;
    public static int IR_PROCEDURE_DECLARATION = 78;
    public static int IR_ELEMENT_DECLARATION = 79;
    public static int IR_NATURE_ELEMENT_DECLARATION = 991; // IIR ONLY!
    public static int IR_ENUMERATION_LITERAL = 990; // IIR ONLY!
    public static int IR_TYPE_DECLARATION = 80;
    public static int IR_SUBTYPE_DECLARATION = 81;
    public static int IR_NATURE_DECLARATION = 989; // IIR ONLY!
    public static int IR_SUBNATURE_DECLARATION = 988; // IIR ONLY!
    public static int IR_CONSTANT_DECLARATION = 82;
    public static int IR_FILE_DECLARATION = 83;
    public static int IR_SIGNAL_DECLARATION = 84;
    public static int IR_SHARED_VARIABLE_DECLARATION = 85;
    public static int IR_VARIABLE_DECLARATION = 86;
    public static int IR_TERMINAL_DECLARATION = 87;
    public static int IR_FREE_QUANTITY_DECLARATION = 88;
    public static int IR_ACROSS_QUANTITY_DECLARATION = 987; // IIR ONLY!
    public static int IR_THROUGH_QUANTITY_DECLARATION = 986; // IIR ONLY!
    public static int IR_SPECTRUM_SOURCE_QUANTITY_DECLARATION = 985; // IIR ONLY!
    public static int IR_NOISE_SOURCE_QUANTITY_DECLARATION = 984; // IIR ONLY!
    public static int IR_BRANCH_QUANTITY_DECLARATION = 89; // fir only
    public static int IR_CONSTANT_INTERFACE_DECLARATION = 90;
    public static int IR_FILE_INTERFACE_DECLARATION = 91;
    public static int IR_SIGNAL_INTERFACE_DECLARATION = 92;
    public static int IR_VARIABLE_INTERFACE_DECLARATION = 93;
    public static int IR_TERMINAL_INTERFACE_DECLARATION = 94;
    public static int IR_QUANTITY_INTERFACE_DECLARATION = 95;
    public static int IR_ALIAS_DECLARATION = 96;
    public static int IR_ATTRIBUTE_DECLARATION = 97;
    public static int IR_COMPONENT_DECLARATION = 98;
    public static int IR_GROUP_DECLARATION = 99;
    public static int IR_GROUP_TEMPLATE_DECLARATION = 100;
    public static int IR_LIBRARY_DECLARATION = 101;
    public static int IR_ENTITY_DECLARATION = 102;
    public static int IR_ARCHITECTURE_DECLARATION = 103;
    public static int IR_PACKAGE_DECLARATION = 104;
    public static int IR_PACKAGE_BODY_DECLARATION = 105;
    public static int IR_CONFIGURATION_DECLARATION = 106;
    public static int IR_PHYSICAL_UNIT = 107;
    public static int IR_ATTRIBUTE_SPECIFICATION = 108;
    public static int IR_CONFIGURATION_SPECIFICATION = 109;
    public static int IR_DISCONNECTION_SPECIFICATION = 110;
    public static int IR_LABEL = 111;
    public static int IR_LIBRARY_CLAUSE = 112;
    public static int IR_USE_CLAUSE = 113;
    public static int IR_SIMPLE_NAME = 983; // IIR ONLY!
    public static int IR_SELECTED_NAME = 114;
    public static int IR_SELECTED_NAME_BY_ALL = 115;
    public static int IR_INDEXED_NAME = 116;
    public static int IR_SLICE_NAME = 117;
    public static int IR_USER_ATTRIBUTE = 118;
    public static int IR_BASE_ATTRIBUTE = 119;
    public static int IR_LEFT_ATTRIBUTE = 120;
    public static int IR_RIGHT_ATTRIBUTE = 121;
    public static int IR_LOW_ATTRIBUTE = 122;
    public static int IR_HIGH_ATTRIBUTE = 123;
    public static int IR_ASCENDING_ATTRIBUTE = 124;
    public static int IR_IMAGE_ATTRIBUTE = 125;
    public static int IR_VALUE_ATTRIBUTE = 126;
    public static int IR_POS_ATTRIBUTE = 127;
    public static int IR_VAL_ATTRIBUTE = 128;
    public static int IR_SUCC_ATTRIBUTE = 129;
    public static int IR_PRED_ATTRIBUTE = 130;
    public static int IR_LEFT_OF_ATTRIBUTE = 131;
    public static int IR_RIGHT_OF_ATTRIBUTE = 132;
    public static int IR_RANGE_ATTRIBUTE = 133;
    public static int IR_REVERSE_RANGE_ATTRIBUTE = 134;
    public static int IR_LENGTH_ATTRIBUTE = 135;
    public static int IR_DELAYED_ATTRIBUTE = 136;
    public static int IR_STABLE_ATTRIBUTE = 137;
    public static int IR_QUIET_ATTRIBUTE = 138;
    public static int IR_TRANSACTION_ATTRIBUTE = 139;
     //public static int IR_ASCENDING_ATTRIBUTE = 140; // DUPLICATE! fir only
    public static int IR_EVENT_ATTRIBUTE = 141;
    public static int IR_ACTIVE_ATTRIBUTE = 142;
    public static int IR_LAST_EVENT_ATTRIBUTE = 143;
    public static int IR_LAST_ACTIVE_ATTRIBUTE = 144;
    public static int IR_LAST_VALUE_ATTRIBUTE = 145;
    public static int IR_BEHAVIOR_ATTRIBUTE = 982; // IIR ONLY!
    public static int IR_STRUCTURE_ATTRIBUTE = 981; // IIR ONLY!
    public static int IR_DRIVING_ATTRIBUTE = 146;
    public static int IR_DRIVING_VALUE_ATTRIBUTE = 147;
    public static int IR_SIMPLE_NAME_ATTRIBUTE = 148;
    public static int IR_INSTANCE_NAME_ATTRIBUTE = 149;
    public static int IR_PATH_NAME_ATTRIBUTE = 150;
    public static int IR_ACROSS_ATTRIBUTE = 151;
    public static int IR_THROUGH_ATTRIBUTE = 152;
    public static int IR_REFERENCE_ATTRIBUTE = 153;
    public static int IR_CONTRIBUTION_ATTRIBUTE = 154;
    public static int IR_TOLERANCE_ATTRIBUTE = 980; // IIR ONLY!
    public static int IR_DOT_ATTRIBUTE = 155;
    public static int IR_INTEG_ATTRIBUTE = 156;
    public static int IR_ABOVE_ATTRIBUTE = 157;
    public static int IR_ZOH_ATTRIBUTE = 979; // IIR ONLY!
    public static int IR_LTF_ATTRIBUTE = 978; // IIR ONLY!
    public static int IR_ZTF_ATTRIBUTE = 977; // IIR ONLY!
    public static int IR_RAMP_ATTRIBUTE = 976; // IIR ONLY!
    public static int IR_SLEW_ATTRIBUTE = 975; // IIR ONLY!
    public static int IR_IDENTITY_OPERATOR = 158;
    public static int IR_NEGATION_OPERATOR = 159;
    public static int IR_ABSOLUTE_OPERATOR = 160;
    public static int IR_NOT_OPERATOR = 161;
    public static int IR_AND_OPERATOR = 162;
    public static int IR_OR_OPERATOR = 163;
    public static int IR_NAND_OPERATOR = 164;
    public static int IR_NOR_OPERATOR = 165;
    public static int IR_XOR_OPERATOR = 166;
    public static int IR_XNOR_OPERATOR = 167;
    public static int IR_EQUALITY_OPERATOR = 168;
    public static int IR_INEQUALITY_OPERATOR = 169;
    public static int IR_LESS_THAN_OPERATOR = 170;
    public static int IR_LESS_THAN_OR_EQUAL_OPERATOR = 171;
    public static int IR_GREATER_THAN_OPERATOR = 172;
    public static int IR_GREATER_THAN_OR_EQUAL_OPERATOR = 173;
    public static int IR_SLL_OPERATOR = 174;
    public static int IR_SRL_OPERATOR = 175;
    public static int IR_SLA_OPERATOR = 176;
    public static int IR_SRA_OPERATOR = 177;
    public static int IR_ROL_OPERATOR = 178;
    public static int IR_ROR_OPERATOR = 179;
    public static int IR_ADDITION_OPERATOR = 180;
    public static int IR_SUBTRACTION_OPERATOR = 181;
    public static int IR_CONCATENATION_OPERATOR = 182;
    public static int IR_MULTIPLICATION_OPERATOR = 183;
    public static int IR_DIVISION_OPERATOR = 184;
    public static int IR_MODULUS_OPERATOR = 185;
    public static int IR_REMAINDER_OPERATOR = 186;
    public static int IR_EXPONENTIATION_OPERATOR = 187;
    public static int IR_FUNCTION_CALL = 188;
    public static int IR_PHYSICAL_LITERAL = 189;
    public static int IR_AGGREGATE = 190;
    public static int IR_OTHERS_INITIALIZATION = 191;
    public static int IR_QUALIFIED_EXPRESSION = 192;
    public static int IR_TYPE_CONVERSION = 193;
    public static int IR_ALLOCATOR = 194;
    public static int IR_WAIT_STATEMENT = 195;
    public static int IR_ASSERTION_STATEMENT = 196;
    public static int IR_REPORT_STATEMENT = 197;
    public static int IR_SIGNAL_ASSIGNMENT_STATEMENT = 198;
    public static int IR_VARIABLE_ASSIGNMENT_STATEMENT = 199;
    public static int IR_PROCEDURE_CALL_STATEMENT = 200;
    public static int IR_IF_STATEMENT = 201;
    public static int IR_CASE_STATEMENT = 202;
    public static int IR_FOR_LOOP_STATEMENT = 203;
    public static int IR_WHILE_LOOP_STATEMENT = 204;
    public static int IR_NEXT_STATEMENT = 205;
    public static int IR_EXIT_STATEMENT = 206;
    public static int IR_RETURN_STATEMENT = 207;
    public static int IR_NULL_STATEMENT = 208;
    public static int IR_BREAK_STATEMENT = 974; // IIR ONLY!
    public static int IR_BLOCK_STATEMENT = 209;
    public static int IR_PROCESS_STATEMENT = 210;
    public static int IR_SENSITIZED_PROCESS_STATEMENT = 211;
    public static int IR_CONCURRENT_PROCEDURE_CALL_STATEMENT = 212;
    public static int IR_CONCURRENT_ASSERTION_STATEMENT = 213;
    public static int IR_CONCURRENT_CONDITIONAL_SIGNAL_ASSIGNMENT = 214;
    public static int IR_CONCURRENT_SELECTED_SIGNAL_ASSIGNMENT = 215;
    public static int IR_CONCURRENT_INSTANTIATION_STATEMENT = 216;
    public static int IR_CONCURRENT_GENERATE_FOR_STATEMENT = 217;
    public static int IR_CONCURRENT_GENERATE_IF_STATEMENT = 218;
    public static int IR_SIMPLE_SIMULTANEOUS_STATEMENT = 219;
    public static int IR_CONCURRENT_BREAK_STATEMENT = 973; // IIR ONLY!
    public static int IR_SIMULTANEOUS_IF_STATEMENT = 220;
    public static int IR_SIMULTANEOUS_CASE_STATEMENT = 221;
    public static int IR_SIMULTANEOUS_PROCEDURAL_STATEMENT = 222;
    public static int IR_SIMULTANEOUS_NULL_STATEMENT = 223;
    public static int FIR_PROXY_REF = 224; // fir only
    public static int FIR_PROXY_INDICATOR = 225; // fir only

    public static int IR_NO_KIND = 0;

    // int->string mapping table --------------------------------------
    public static String toString(int kind) 
    { return (String) names.get(new Integer(kind)); }

    private static Hashtable names = new Hashtable();
    static {
	names.put(new Integer(IR_DESIGN_FILE),
		  "IR_DESIGN_FILE");
	names.put(new Integer(IR_COMMENT),
		  "IR_COMMENT");
	names.put(new Integer(IR_IDENTIFIER),
		  "IR_IDENTIFIER");
	names.put(new Integer(IR_INTEGER_LITERAL),
		  "IR_INTEGER_LITERAL");
	names.put(new Integer(IR_FLOATING_POINT_LITERAL),
		  "IR_FLOATING_POINT_LITERAL");
	names.put(new Integer(IR_CHARACTER_LITERAL),
		  "IR_CHARACTER_LITERAL");
	names.put(new Integer(IR_BIT_STRING_LITERAL),
		  "IR_BIT_STRING_LITERAL");
	names.put(new Integer(IR_STRING_LITERAL),
		  "IR_STRING_LITERAL");
	names.put(new Integer(IR_ASSOCIATION_ELEMENT),
		  "IR_ASSOCIATION_ELEMENT");
	names.put(new Integer(IR_ASSOCIATION_ELEMENT_BY_EXPRESSION),
		  "IR_ASSOCIATION_ELEMENT_BY_EXPRESSION");
	names.put(new Integer(IR_ASSOCIATION_ELEMENT_OPEN),
		  "IR_ASSOCIATION_ELEMENT_OPEN");
	names.put(new Integer(IR_BREAK_ELEMENT),
		  "IR_BREAK_ELEMENT");
	names.put(new Integer(IR_CASE_STATEMENT_ALTERNATIVE_BY_EXPRESSION),
		  "IR_CASE_STATEMENT_ALTERNATIVE_BY_EXPRESSION");
	names.put(new Integer(IR_CASE_STATEMENT_ALTERNATIVE_BY_CHOICES),
		  "IR_CASE_STATEMENT_ALTERNATIVE_BY_CHOICES");
	names.put(new Integer(IR_CASE_STATEMENT_ALTERNATIVE_BY_OPEN),
		  "IR_CASE_STATEMENT_ALTERNATIVE_BY_OPEN");
	names.put(new Integer(IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS),
		  "IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS");
	names.put(new Integer(IR_CHOICE),
		  "IR_CHOICE");
	names.put(new Integer(IR_CONDITIONAL_WAVEFORM),
		  "IR_CONDITIONAL_WAVEFORM");
	names.put(new Integer(IR_BLOCK_CONFIGURATION),
		  "IR_BLOCK_CONFIGURATION");
	names.put(new Integer(IR_COMPONENT_CONFIGURATION),
		  "IR_COMPONENT_CONFIGURATION");
	names.put(new Integer(IR_DESIGNATOR_EXPLICIT),
		  "IR_DESIGNATOR_EXPLICIT");
	names.put(new Integer(IR_DESIGNATOR_BY_OTHERS),
		  "IR_DESIGNATOR_BY_OTHERS");
	names.put(new Integer(IR_DESIGNATOR_BY_ALL),
		  "IR_DESIGNATOR_BY_ALL");
	names.put(new Integer(IR_ENTITY_CLASS_ENTRY),
		  "IR_ENTITY_CLASS_ENTRY");
	names.put(new Integer(IR_ELSIF),
		  "IR_ELSIF");
	names.put(new Integer(IR_GROUP_CONSTITUENT),
		  "IR_GROUP_CONSTITUENT");
	names.put(new Integer(IR_SELECTED_WAVEFORM),
		  "IR_SELECTED_WAVEFORM");
	names.put(new Integer(IR_SIMULTANEOUS_ALTERNATIVE_BY_EXPRESSION),
		  "IR_SIMULTANEOUS_ALTERNATIVE_BY_EXPRESSION");
	names.put(new Integer(IR_SIMULTANEOUS_ALTERNATIVE_BY_CHOICES),
		  "IR_SIMULTANEOUS_ALTERNATIVE_BY_CHOICES");
	names.put(new Integer(IR_SIMULTANEOUS_ALTERNATIVE_BY_OTHERS),
		  "IR_SIMULTANEOUS_ALTERNATIVE_BY_OTHERS");
	names.put(new Integer(IR_SIMULTANEOUS_ELSIF),
		  "IR_SIMULTANEOUS_ELSIF");
	names.put(new Integer(IR_WAVEFORM_ELEMENT),
		  "IR_WAVEFORM_ELEMENT");
	names.put(new Integer(IR_ASSOCIATION_LIST),
		  "IR_ASSOCIATION_LIST");
	names.put(new Integer(IR_ATTRIBUTE_SPECIFICATION_LIST),
		  "IR_ATTRIBUTE_SPECIFICATION_LIST");
	names.put(new Integer(IR_BREAK_LIST),
		  "IR_BREAK_LIST");
	names.put(new Integer(IR_CASE_ALTERNATIVE_LIST),
		  "IR_CASE_ALTERNATIVE_LIST");
	names.put(new Integer(IR_CHOICE_LIST),
		  "IR_CHOICE_LIST");
	names.put(new Integer(IR_COMMENT_LIST),
		  "IR_COMMENT_LIST");
	names.put(new Integer(IR_CONCURRENT_STATEMENT_LIST),
		  "IR_CONCURRENT_STATEMENT_LIST");
	names.put(new Integer(IR_CONDITIONAL_WAVEFORM_LIST),
		  "IR_CONDITIONAL_WAVEFORM_LIST");
	names.put(new Integer(IR_CONFIGURATION_ITEM_LIST),
		  "IR_CONFIGURATION_ITEM_LIST");
	names.put(new Integer(IR_DECLARATION_LIST),
		  "IR_DECLARATION_LIST");
	names.put(new Integer(IR_DESIGN_FILE_LIST),
		  "IR_DESIGN_FILE_LIST");
	names.put(new Integer(IR_DESIGNATOR_LIST),
		  "IR_DESIGNATOR_LIST");
	names.put(new Integer(IR_ELEMENT_DECLARATION_LIST),
		  "IR_ELEMENT_DECLARATION_LIST");
	names.put(new Integer(IR_ENTITY_CLASS_ENTRY_LIST),
		  "IR_ENTITY_CLASS_ENTRY_LIST");
	names.put(new Integer(IR_ENUMERATION_LITERAL_LIST),
		  "IR_ENUMERATION_LITERAL_LIST");
	names.put(new Integer(IR_GENERIC_LIST),
		  "IR_GENERIC_LIST");
	names.put(new Integer(IR_INTERFACE_LIST),
		  "IR_INTERFACE_LIST");
	names.put(new Integer(IR_LIBRARY_UNIT_LIST),
		  "IR_LIBRARY_UNIT_LIST");
	names.put(new Integer(IR_PORT_LIST),
		  "IR_PORT_LIST");
	names.put(new Integer(IR_SELECTED_WAVEFORM_LIST),
		  "IR_SELECTED_WAVEFORM_LIST");
	names.put(new Integer(IR_SEQUENTIAL_STATEMENT_LIST),
		  "IR_SEQUENTIAL_STATEMENT_LIST");
	names.put(new Integer(IR_SIMULTANEOUS_ALTERNATIVE_LIST),
		  "IR_SIMULTANEOUS_ALTERNATIVE_LIST");
	names.put(new Integer(IR_SIMULTANEOUS_STATEMENT_LIST),
		  "IR_SIMULTANEOUS_STATEMENT_LIST");
	names.put(new Integer(IR_STATEMENT_LIST),
		  "IR_STATEMENT_LIST");
	names.put(new Integer(IR_UNIT_LIST),
		  "IR_UNIT_LIST");
	names.put(new Integer(IR_WAVEFORM_LIST),
		  "IR_WAVEFORM_LIST");
	names.put(new Integer(IR_ENUMERATION_TYPE_DEFINITION),
		  "IR_ENUMERATION_TYPE_DEFINITION");
	names.put(new Integer(IR_ENUMERATION_SUBTYPE_DEFINITION),
		  "IR_ENUMERATION_SUBTYPE_DEFINITION");
	names.put(new Integer(IR_INTEGER_TYPE_DEFINITION),
		  "IR_INTEGER_TYPE_DEFINITION");
	names.put(new Integer(IR_INTEGER_SUBTYPE_DEFINITION),
		  "IR_INTEGER_SUBTYPE_DEFINITION");
	names.put(new Integer(IR_FLOATING_TYPE_DEFINITION),
		  "IR_FLOATING_TYPE_DEFINITION");
	names.put(new Integer(IR_FLOATING_SUBTYPE_DEFINITION),
		  "IR_FLOATING_SUBTYPE_DEFINITION");
	names.put(new Integer(IR_PHYSICAL_TYPE_DEFINITION),
		  "IR_PHYSICAL_TYPE_DEFINITION");
	names.put(new Integer(IR_PHYSICAL_SUBTYPE_DEFINITION),
		  "IR_PHYSICAL_SUBTYPE_DEFINITION");
	names.put(new Integer(IR_RANGE_TYPE_DEFINITION),
		  "IR_RANGE_TYPE_DEFINITION");
	names.put(new Integer(IR_SCALAR_NATURE_DEFINITION),
		  "IR_SCALAR_NATURE_DEFINITION");
	names.put(new Integer(IR_SCALAR_SUBNATURE_DEFINITION),
		  "IR_SCALAR_SUBNATURE_DEFINITION");
	names.put(new Integer(IR_ARRAY_TYPE_DEFINITION),
		  "IR_ARRAY_TYPE_DEFINITION");
	names.put(new Integer(IR_ARRAY_SUBTYPE_DEFINITION),
		  "IR_ARRAY_SUBTYPE_DEFINITION");
	names.put(new Integer(IR_ARRAY_NATURE_DEFINITION),
		  "IR_ARRAY_NATURE_DEFINITION");
	names.put(new Integer(IR_ARRAY_SUBNATURE_DEFINITION),
		  "IR_ARRAY_SUBNATURE_DEFINITION");
	names.put(new Integer(IR_RECORD_TYPE_DEFINITION),
		  "IR_RECORD_TYPE_DEFINITION");
	names.put(new Integer(IR_RECORD_SUBTYPE_DEFINITION),
		  "IR_RECORD_SUBTYPE_DEFINITION");
	names.put(new Integer(IR_RECORD_NATURE_DEFINITION),
		  "IR_RECORD_NATURE_DEFINITION");
	names.put(new Integer(IR_RECORD_SUBNATURE_DEFINITION),
		  "IR_RECORD_SUBNATURE_DEFINITION");
	names.put(new Integer(IR_ACCESS_TYPE_DEFINITION),
		  "IR_ACCESS_TYPE_DEFINITION");
	names.put(new Integer(IR_ACCESS_SUBTYPE_DEFINITION),
		  "IR_ACCESS_SUBTYPE_DEFINITION");
	names.put(new Integer(IR_FILE_TYPE_DEFINITION),
		  "IR_FILE_TYPE_DEFINITION");
	names.put(new Integer(IR_SIGNATURE),
		  "IR_SIGNATURE");
	names.put(new Integer(IR_FUNCTION_DECLARATION),
		  "IR_FUNCTION_DECLARATION");
	names.put(new Integer(IR_PROCEDURE_DECLARATION),
		  "IR_PROCEDURE_DECLARATION");
	names.put(new Integer(IR_ELEMENT_DECLARATION),
		  "IR_ELEMENT_DECLARATION");
	names.put(new Integer(IR_NATURE_ELEMENT_DECLARATION),
		  "IR_NATURE_ELEMENT_DECLARATION");
	names.put(new Integer(IR_ENUMERATION_LITERAL),
		  "IR_ENUMERATION_LITERAL");
	names.put(new Integer(IR_TYPE_DECLARATION),
		  "IR_TYPE_DECLARATION");
	names.put(new Integer(IR_SUBTYPE_DECLARATION),
		  "IR_SUBTYPE_DECLARATION");
	names.put(new Integer(IR_NATURE_DECLARATION),
		  "IR_NATURE_DECLARATION");
	names.put(new Integer(IR_SUBNATURE_DECLARATION),
		  "IR_SUBNATURE_DECLARATION");
	names.put(new Integer(IR_CONSTANT_DECLARATION),
		  "IR_CONSTANT_DECLARATION");
	names.put(new Integer(IR_FILE_DECLARATION),
		  "IR_FILE_DECLARATION");
	names.put(new Integer(IR_SIGNAL_DECLARATION),
		  "IR_SIGNAL_DECLARATION");
	names.put(new Integer(IR_SHARED_VARIABLE_DECLARATION),
		  "IR_SHARED_VARIABLE_DECLARATION");
	names.put(new Integer(IR_VARIABLE_DECLARATION),
		  "IR_VARIABLE_DECLARATION");
	names.put(new Integer(IR_TERMINAL_DECLARATION),
		  "IR_TERMINAL_DECLARATION");
	names.put(new Integer(IR_FREE_QUANTITY_DECLARATION),
		  "IR_FREE_QUANTITY_DECLARATION");
	names.put(new Integer(IR_ACROSS_QUANTITY_DECLARATION),
		  "IR_ACROSS_QUANTITY_DECLARATION");
	names.put(new Integer(IR_THROUGH_QUANTITY_DECLARATION),
		  "IR_THROUGH_QUANTITY_DECLARATION");
	names.put(new Integer(IR_SPECTRUM_SOURCE_QUANTITY_DECLARATION),
		  "IR_SPECTRUM_SOURCE_QUANTITY_DECLARATION");
	names.put(new Integer(IR_NOISE_SOURCE_QUANTITY_DECLARATION),
		  "IR_NOISE_SOURCE_QUANTITY_DECLARATION");
	names.put(new Integer(IR_BRANCH_QUANTITY_DECLARATION),
		  "IR_BRANCH_QUANTITY_DECLARATION");
	names.put(new Integer(IR_CONSTANT_INTERFACE_DECLARATION),
		  "IR_CONSTANT_INTERFACE_DECLARATION");
	names.put(new Integer(IR_FILE_INTERFACE_DECLARATION),
		  "IR_FILE_INTERFACE_DECLARATION");
	names.put(new Integer(IR_SIGNAL_INTERFACE_DECLARATION),
		  "IR_SIGNAL_INTERFACE_DECLARATION");
	names.put(new Integer(IR_VARIABLE_INTERFACE_DECLARATION),
		  "IR_VARIABLE_INTERFACE_DECLARATION");
	names.put(new Integer(IR_TERMINAL_INTERFACE_DECLARATION),
		  "IR_TERMINAL_INTERFACE_DECLARATION");
	names.put(new Integer(IR_QUANTITY_INTERFACE_DECLARATION),
		  "IR_QUANTITY_INTERFACE_DECLARATION");
	names.put(new Integer(IR_ALIAS_DECLARATION),
		  "IR_ALIAS_DECLARATION");
	names.put(new Integer(IR_ATTRIBUTE_DECLARATION),
		  "IR_ATTRIBUTE_DECLARATION");
	names.put(new Integer(IR_COMPONENT_DECLARATION),
		  "IR_COMPONENT_DECLARATION");
	names.put(new Integer(IR_GROUP_DECLARATION),
		  "IR_GROUP_DECLARATION");
	names.put(new Integer(IR_GROUP_TEMPLATE_DECLARATION),
		  "IR_GROUP_TEMPLATE_DECLARATION");
	names.put(new Integer(IR_LIBRARY_DECLARATION),
		  "IR_LIBRARY_DECLARATION");
	names.put(new Integer(IR_ENTITY_DECLARATION),
		  "IR_ENTITY_DECLARATION");
	names.put(new Integer(IR_ARCHITECTURE_DECLARATION),
		  "IR_ARCHITECTURE_DECLARATION");
	names.put(new Integer(IR_PACKAGE_DECLARATION),
		  "IR_PACKAGE_DECLARATION");
	names.put(new Integer(IR_PACKAGE_BODY_DECLARATION),
		  "IR_PACKAGE_BODY_DECLARATION");
	names.put(new Integer(IR_CONFIGURATION_DECLARATION),
		  "IR_CONFIGURATION_DECLARATION");
	names.put(new Integer(IR_PHYSICAL_UNIT),
		  "IR_PHYSICAL_UNIT");
	names.put(new Integer(IR_ATTRIBUTE_SPECIFICATION),
		  "IR_ATTRIBUTE_SPECIFICATION");
	names.put(new Integer(IR_CONFIGURATION_SPECIFICATION),
		  "IR_CONFIGURATION_SPECIFICATION");
	names.put(new Integer(IR_DISCONNECTION_SPECIFICATION),
		  "IR_DISCONNECTION_SPECIFICATION");
	names.put(new Integer(IR_LABEL),
		  "IR_LABEL");
	names.put(new Integer(IR_LIBRARY_CLAUSE),
		  "IR_LIBRARY_CLAUSE");
	names.put(new Integer(IR_USE_CLAUSE),
		  "IR_USE_CLAUSE");
	names.put(new Integer(IR_SIMPLE_NAME),
		  "IR_SIMPLE_NAME");
	names.put(new Integer(IR_SELECTED_NAME),
		  "IR_SELECTED_NAME");
	names.put(new Integer(IR_SELECTED_NAME_BY_ALL),
		  "IR_SELECTED_NAME_BY_ALL");
	names.put(new Integer(IR_INDEXED_NAME),
		  "IR_INDEXED_NAME");
	names.put(new Integer(IR_SLICE_NAME),
		  "IR_SLICE_NAME");
	names.put(new Integer(IR_USER_ATTRIBUTE),
		  "IR_USER_ATTRIBUTE");
	names.put(new Integer(IR_BASE_ATTRIBUTE),
		  "IR_BASE_ATTRIBUTE");
	names.put(new Integer(IR_LEFT_ATTRIBUTE),
		  "IR_LEFT_ATTRIBUTE");
	names.put(new Integer(IR_RIGHT_ATTRIBUTE),
		  "IR_RIGHT_ATTRIBUTE");
	names.put(new Integer(IR_LOW_ATTRIBUTE),
		  "IR_LOW_ATTRIBUTE");
	names.put(new Integer(IR_HIGH_ATTRIBUTE),
		  "IR_HIGH_ATTRIBUTE");
	names.put(new Integer(IR_ASCENDING_ATTRIBUTE),
		  "IR_ASCENDING_ATTRIBUTE");
	names.put(new Integer(IR_IMAGE_ATTRIBUTE),
		  "IR_IMAGE_ATTRIBUTE");
	names.put(new Integer(IR_VALUE_ATTRIBUTE),
		  "IR_VALUE_ATTRIBUTE");
	names.put(new Integer(IR_POS_ATTRIBUTE),
		  "IR_POS_ATTRIBUTE");
	names.put(new Integer(IR_VAL_ATTRIBUTE),
		  "IR_VAL_ATTRIBUTE");
	names.put(new Integer(IR_SUCC_ATTRIBUTE),
		  "IR_SUCC_ATTRIBUTE");
	names.put(new Integer(IR_PRED_ATTRIBUTE),
		  "IR_PRED_ATTRIBUTE");
	names.put(new Integer(IR_LEFT_OF_ATTRIBUTE),
		  "IR_LEFT_OF_ATTRIBUTE");
	names.put(new Integer(IR_RIGHT_OF_ATTRIBUTE),
		  "IR_RIGHT_OF_ATTRIBUTE");
	names.put(new Integer(IR_RANGE_ATTRIBUTE),
		  "IR_RANGE_ATTRIBUTE");
	names.put(new Integer(IR_REVERSE_RANGE_ATTRIBUTE),
		  "IR_REVERSE_RANGE_ATTRIBUTE");
	names.put(new Integer(IR_LENGTH_ATTRIBUTE),
		  "IR_LENGTH_ATTRIBUTE");
	names.put(new Integer(IR_DELAYED_ATTRIBUTE),
		  "IR_DELAYED_ATTRIBUTE");
	names.put(new Integer(IR_STABLE_ATTRIBUTE),
		  "IR_STABLE_ATTRIBUTE");
	names.put(new Integer(IR_QUIET_ATTRIBUTE),
		  "IR_QUIET_ATTRIBUTE");
	names.put(new Integer(IR_TRANSACTION_ATTRIBUTE),
		  "IR_TRANSACTION_ATTRIBUTE");
	names.put(new Integer(IR_ASCENDING_ATTRIBUTE),
		  "IR_ASCENDING_ATTRIBUTE");
	names.put(new Integer(IR_EVENT_ATTRIBUTE),
		  "IR_EVENT_ATTRIBUTE");
	names.put(new Integer(IR_ACTIVE_ATTRIBUTE),
		  "IR_ACTIVE_ATTRIBUTE");
	names.put(new Integer(IR_LAST_EVENT_ATTRIBUTE),
		  "IR_LAST_EVENT_ATTRIBUTE");
	names.put(new Integer(IR_LAST_ACTIVE_ATTRIBUTE),
		  "IR_LAST_ACTIVE_ATTRIBUTE");
	names.put(new Integer(IR_LAST_VALUE_ATTRIBUTE),
		  "IR_LAST_VALUE_ATTRIBUTE");
	names.put(new Integer(IR_BEHAVIOR_ATTRIBUTE),
		  "IR_BEHAVIOR_ATTRIBUTE");
	names.put(new Integer(IR_STRUCTURE_ATTRIBUTE),
		  "IR_STRUCTURE_ATTRIBUTE");
	names.put(new Integer(IR_DRIVING_ATTRIBUTE),
		  "IR_DRIVING_ATTRIBUTE");
	names.put(new Integer(IR_DRIVING_VALUE_ATTRIBUTE),
		  "IR_DRIVING_VALUE_ATTRIBUTE");
	names.put(new Integer(IR_SIMPLE_NAME_ATTRIBUTE),
		  "IR_SIMPLE_NAME_ATTRIBUTE");
	names.put(new Integer(IR_INSTANCE_NAME_ATTRIBUTE),
		  "IR_INSTANCE_NAME_ATTRIBUTE");
	names.put(new Integer(IR_PATH_NAME_ATTRIBUTE),
		  "IR_PATH_NAME_ATTRIBUTE");
	names.put(new Integer(IR_ACROSS_ATTRIBUTE),
		  "IR_ACROSS_ATTRIBUTE");
	names.put(new Integer(IR_THROUGH_ATTRIBUTE),
		  "IR_THROUGH_ATTRIBUTE");
	names.put(new Integer(IR_REFERENCE_ATTRIBUTE),
		  "IR_REFERENCE_ATTRIBUTE");
	names.put(new Integer(IR_CONTRIBUTION_ATTRIBUTE),
		  "IR_CONTRIBUTION_ATTRIBUTE");
	names.put(new Integer(IR_TOLERANCE_ATTRIBUTE),
		  "IR_TOLERANCE_ATTRIBUTE");
	names.put(new Integer(IR_DOT_ATTRIBUTE),
		  "IR_DOT_ATTRIBUTE");
	names.put(new Integer(IR_INTEG_ATTRIBUTE),
		  "IR_INTEG_ATTRIBUTE");
	names.put(new Integer(IR_ABOVE_ATTRIBUTE),
		  "IR_ABOVE_ATTRIBUTE");
	names.put(new Integer(IR_ZOH_ATTRIBUTE),
		  "IR_ZOH_ATTRIBUTE");
	names.put(new Integer(IR_LTF_ATTRIBUTE),
		  "IR_LTF_ATTRIBUTE");
	names.put(new Integer(IR_ZTF_ATTRIBUTE),
		  "IR_ZTF_ATTRIBUTE");
	names.put(new Integer(IR_RAMP_ATTRIBUTE),
		  "IR_RAMP_ATTRIBUTE");
	names.put(new Integer(IR_SLEW_ATTRIBUTE),
		  "IR_SLEW_ATTRIBUTE");
	names.put(new Integer(IR_IDENTITY_OPERATOR),
		  "IR_IDENTITY_OPERATOR");
	names.put(new Integer(IR_NEGATION_OPERATOR),
		  "IR_NEGATION_OPERATOR");
	names.put(new Integer(IR_ABSOLUTE_OPERATOR),
		  "IR_ABSOLUTE_OPERATOR");
	names.put(new Integer(IR_NOT_OPERATOR),
		  "IR_NOT_OPERATOR");
	names.put(new Integer(IR_AND_OPERATOR),
		  "IR_AND_OPERATOR");
	names.put(new Integer(IR_OR_OPERATOR),
		  "IR_OR_OPERATOR");
	names.put(new Integer(IR_NAND_OPERATOR),
		  "IR_NAND_OPERATOR");
	names.put(new Integer(IR_NOR_OPERATOR),
		  "IR_NOR_OPERATOR");
	names.put(new Integer(IR_XOR_OPERATOR),
		  "IR_XOR_OPERATOR");
	names.put(new Integer(IR_XNOR_OPERATOR),
		  "IR_XNOR_OPERATOR");
	names.put(new Integer(IR_EQUALITY_OPERATOR),
		  "IR_EQUALITY_OPERATOR");
	names.put(new Integer(IR_INEQUALITY_OPERATOR),
		  "IR_INEQUALITY_OPERATOR");
	names.put(new Integer(IR_LESS_THAN_OPERATOR),
		  "IR_LESS_THAN_OPERATOR");
	names.put(new Integer(IR_LESS_THAN_OR_EQUAL_OPERATOR),
		  "IR_LESS_THAN_OR_EQUAL_OPERATOR");
	names.put(new Integer(IR_GREATER_THAN_OPERATOR),
		  "IR_GREATER_THAN_OPERATOR");
	names.put(new Integer(IR_GREATER_THAN_OR_EQUAL_OPERATOR),
		  "IR_GREATER_THAN_OR_EQUAL_OPERATOR");
	names.put(new Integer(IR_SLL_OPERATOR),
		  "IR_SLL_OPERATOR");
	names.put(new Integer(IR_SRL_OPERATOR),
		  "IR_SRL_OPERATOR");
	names.put(new Integer(IR_SLA_OPERATOR),
		  "IR_SLA_OPERATOR");
	names.put(new Integer(IR_SRA_OPERATOR),
		  "IR_SRA_OPERATOR");
	names.put(new Integer(IR_ROL_OPERATOR),
		  "IR_ROL_OPERATOR");
	names.put(new Integer(IR_ROR_OPERATOR),
		  "IR_ROR_OPERATOR");
	names.put(new Integer(IR_ADDITION_OPERATOR),
		  "IR_ADDITION_OPERATOR");
	names.put(new Integer(IR_SUBTRACTION_OPERATOR),
		  "IR_SUBTRACTION_OPERATOR");
	names.put(new Integer(IR_CONCATENATION_OPERATOR),
		  "IR_CONCATENATION_OPERATOR");
	names.put(new Integer(IR_MULTIPLICATION_OPERATOR),
		  "IR_MULTIPLICATION_OPERATOR");
	names.put(new Integer(IR_DIVISION_OPERATOR),
		  "IR_DIVISION_OPERATOR");
	names.put(new Integer(IR_MODULUS_OPERATOR),
		  "IR_MODULUS_OPERATOR");
	names.put(new Integer(IR_REMAINDER_OPERATOR),
		  "IR_REMAINDER_OPERATOR");
	names.put(new Integer(IR_EXPONENTIATION_OPERATOR),
		  "IR_EXPONENTIATION_OPERATOR");
	names.put(new Integer(IR_FUNCTION_CALL),
		  "IR_FUNCTION_CALL");
	names.put(new Integer(IR_PHYSICAL_LITERAL),
		  "IR_PHYSICAL_LITERAL");
	names.put(new Integer(IR_AGGREGATE),
		  "IR_AGGREGATE");
	names.put(new Integer(IR_OTHERS_INITIALIZATION),
		  "IR_OTHERS_INITIALIZATION");
	names.put(new Integer(IR_QUALIFIED_EXPRESSION),
		  "IR_QUALIFIED_EXPRESSION");
	names.put(new Integer(IR_TYPE_CONVERSION),
		  "IR_TYPE_CONVERSION");
	names.put(new Integer(IR_ALLOCATOR),
		  "IR_ALLOCATOR");
	names.put(new Integer(IR_WAIT_STATEMENT),
		  "IR_WAIT_STATEMENT");
	names.put(new Integer(IR_ASSERTION_STATEMENT),
		  "IR_ASSERTION_STATEMENT");
	names.put(new Integer(IR_REPORT_STATEMENT),
		  "IR_REPORT_STATEMENT");
	names.put(new Integer(IR_SIGNAL_ASSIGNMENT_STATEMENT),
		  "IR_SIGNAL_ASSIGNMENT_STATEMENT");
	names.put(new Integer(IR_VARIABLE_ASSIGNMENT_STATEMENT),
		  "IR_VARIABLE_ASSIGNMENT_STATEMENT");
	names.put(new Integer(IR_PROCEDURE_CALL_STATEMENT),
		  "IR_PROCEDURE_CALL_STATEMENT");
	names.put(new Integer(IR_IF_STATEMENT),
		  "IR_IF_STATEMENT");
	names.put(new Integer(IR_CASE_STATEMENT),
		  "IR_CASE_STATEMENT");
	names.put(new Integer(IR_FOR_LOOP_STATEMENT),
		  "IR_FOR_LOOP_STATEMENT");
	names.put(new Integer(IR_WHILE_LOOP_STATEMENT),
		  "IR_WHILE_LOOP_STATEMENT");
	names.put(new Integer(IR_NEXT_STATEMENT),
		  "IR_NEXT_STATEMENT");
	names.put(new Integer(IR_EXIT_STATEMENT),
		  "IR_EXIT_STATEMENT");
	names.put(new Integer(IR_RETURN_STATEMENT),
		  "IR_RETURN_STATEMENT");
	names.put(new Integer(IR_NULL_STATEMENT),
		  "IR_NULL_STATEMENT");
	names.put(new Integer(IR_BREAK_STATEMENT),
		  "IR_BREAK_STATEMENT");
	names.put(new Integer(IR_BLOCK_STATEMENT),
		  "IR_BLOCK_STATEMENT");
	names.put(new Integer(IR_PROCESS_STATEMENT),
		  "IR_PROCESS_STATEMENT");
	names.put(new Integer(IR_SENSITIZED_PROCESS_STATEMENT),
		  "IR_SENSITIZED_PROCESS_STATEMENT");
	names.put(new Integer(IR_CONCURRENT_PROCEDURE_CALL_STATEMENT),
		  "IR_CONCURRENT_PROCEDURE_CALL_STATEMENT");
	names.put(new Integer(IR_CONCURRENT_ASSERTION_STATEMENT),
		  "IR_CONCURRENT_ASSERTION_STATEMENT");
	names.put(new Integer(IR_CONCURRENT_CONDITIONAL_SIGNAL_ASSIGNMENT),
		  "IR_CONCURRENT_CONDITIONAL_SIGNAL_ASSIGNMENT");
	names.put(new Integer(IR_CONCURRENT_SELECTED_SIGNAL_ASSIGNMENT),
		  "IR_CONCURRENT_SELECTED_SIGNAL_ASSIGNMENT");
	names.put(new Integer(IR_CONCURRENT_INSTANTIATION_STATEMENT),
		  "IR_CONCURRENT_INSTANTIATION_STATEMENT");
	names.put(new Integer(IR_CONCURRENT_GENERATE_FOR_STATEMENT),
		  "IR_CONCURRENT_GENERATE_FOR_STATEMENT");
	names.put(new Integer(IR_CONCURRENT_GENERATE_IF_STATEMENT),
		  "IR_CONCURRENT_GENERATE_IF_STATEMENT");
	names.put(new Integer(IR_SIMPLE_SIMULTANEOUS_STATEMENT),
		  "IR_SIMPLE_SIMULTANEOUS_STATEMENT");
	names.put(new Integer(IR_CONCURRENT_BREAK_STATEMENT),
		  "IR_CONCURRENT_BREAK_STATEMENT");
	names.put(new Integer(IR_SIMULTANEOUS_IF_STATEMENT),
		  "IR_SIMULTANEOUS_IF_STATEMENT");
	names.put(new Integer(IR_SIMULTANEOUS_CASE_STATEMENT),
		  "IR_SIMULTANEOUS_CASE_STATEMENT");
	names.put(new Integer(IR_SIMULTANEOUS_PROCEDURAL_STATEMENT),
		  "IR_SIMULTANEOUS_PROCEDURAL_STATEMENT");
	names.put(new Integer(IR_SIMULTANEOUS_NULL_STATEMENT),
		  "IR_SIMULTANEOUS_NULL_STATEMENT");
	names.put(new Integer(FIR_PROXY_REF),
		  "FIR_PROXY_REF");
	names.put(new Integer(FIR_PROXY_INDICATOR),
		  "FIR_PROXY_INDICATOR");
	names.put(new Integer(IR_NO_KIND),
		  "IR_NO_KIND");

    }
}
