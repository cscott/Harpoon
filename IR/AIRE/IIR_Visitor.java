// IIR_Visitor.java, created Sun Oct 11 06:00:15 EDT 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The <code>IIR_Visitor</code> class is part of the implementation of
 * the "visitor" design pattern.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Visitor.java,v 1.3 1998-10-11 10:04:37 cananian Exp $
 */
public abstract class IIR_Visitor {
    public abstract void visit(IIR i);

    public void visit(IIR_Comment ic) {
        visit( (IIR) ic);
    }
    public void visit(IIR_Declaration id) {
        visit( (IIR) id);
    }
    public void visit(IIR_AliasDeclaration iad) {
        visit( (IIR_Declaration) iad);
    }
    public void visit(IIR_AttributeDeclaration iad) {
        visit( (IIR_Declaration) iad);
    }
    public void visit(IIR_AttributeSpecification ias) {
        visit( (IIR_Declaration) ias);
    }
    public void visit(IIR_ComponentDeclaration icd) {
        visit( (IIR_Declaration) icd);
    }
    public void visit(IIR_ConfigurationSpecification ics) {
        visit( (IIR_Declaration) ics);
    }
    public void visit(IIR_DisconnectionSpecification ids) {
        visit( (IIR_Declaration) ids);
    }
    public void visit(IIR_ElementDeclaration ied) {
        visit( (IIR_Declaration) ied);
    }
    public void visit(IIR_EnumerationLiteral iel) {
        visit( (IIR_Declaration) iel);
    }
    public void visit(IIR_GroupDeclaration igd) {
        visit( (IIR_Declaration) igd);
    }
    public void visit(IIR_GroupTemplateDeclaration igtd) {
        visit( (IIR_Declaration) igtd);
    }
    public void visit(IIR_InterfaceDeclaration iid) {
        visit( (IIR_Declaration) iid);
    }
    public void visit(IIR_ConstantInterfaceDeclaration icid) {
        visit( (IIR_InterfaceDeclaration) icid);
    }
    public void visit(IIR_FileInterfaceDeclaration ifid) {
        visit( (IIR_InterfaceDeclaration) ifid);
    }
    public void visit(IIR_QuantityInterfaceDeclaration iqid) {
        visit( (IIR_InterfaceDeclaration) iqid);
    }
    public void visit(IIR_SignalInterfaceDeclaration isid) {
        visit( (IIR_InterfaceDeclaration) isid);
    }
    public void visit(IIR_TerminalInterfaceDeclaration itid) {
        visit( (IIR_InterfaceDeclaration) itid);
    }
    public void visit(IIR_VariableInterfaceDeclaration ivid) {
        visit( (IIR_InterfaceDeclaration) ivid);
    }
    public void visit(IIR_Label il) {
        visit( (IIR_Declaration) il);
    }
    public void visit(IIR_LibraryClause ilc) {
        visit( (IIR_Declaration) ilc);
    }
    public void visit(IIR_LibraryDeclaration ild) {
        visit( (IIR_Declaration) ild);
    }
    public void visit(IIR_LibraryUnit ilu) {
        visit( (IIR_Declaration) ilu);
    }
    public void visit(IIR_ArchitectureDeclaration iad) {
        visit( (IIR_LibraryUnit) iad);
    }
    public void visit(IIR_ConfigurationDeclaration icd) {
        visit( (IIR_LibraryUnit) icd);
    }
    public void visit(IIR_EntityDeclaration ied) {
        visit( (IIR_LibraryUnit) ied);
    }
    public void visit(IIR_PackageBodyDeclaration ipbd) {
        visit( (IIR_LibraryUnit) ipbd);
    }
    public void visit(IIR_PackageDeclaration ipd) {
        visit( (IIR_LibraryUnit) ipd);
    }
    public void visit(IIR_NatureDeclaration ind) {
        visit( (IIR_Declaration) ind);
    }
    public void visit(IIR_NatureElementDeclaration ined) {
        visit( (IIR_Declaration) ined);
    }
    public void visit(IIR_ObjectDeclaration iod) {
        visit( (IIR_Declaration) iod);
    }
    public void visit(IIR_ConstantDeclaration icd) {
        visit( (IIR_ObjectDeclaration) icd);
    }
    public void visit(IIR_FileDeclaration ifd) {
        visit( (IIR_ObjectDeclaration) ifd);
    }
    public void visit(IIR_QuantityDeclaration iqd) {
        visit( (IIR_ObjectDeclaration) iqd);
    }
    public void visit(IIR_AcrossQuantityDeclaration iaqd) {
        visit( (IIR_QuantityDeclaration) iaqd);
    }
    public void visit(IIR_FreeQuantityDeclaration ifqd) {
        visit( (IIR_QuantityDeclaration) ifqd);
    }
    public void visit(IIR_NoiseSourceQuantityDeclaration insqd) {
        visit( (IIR_QuantityDeclaration) insqd);
    }
    public void visit(IIR_SpectrumSourceQuantityDeclaration issqd) {
        visit( (IIR_QuantityDeclaration) issqd);
    }
    public void visit(IIR_ThroughQuantityDeclaration itqd) {
        visit( (IIR_QuantityDeclaration) itqd);
    }
    public void visit(IIR_SharedVariableDeclaration isvd) {
        visit( (IIR_ObjectDeclaration) isvd);
    }
    public void visit(IIR_SignalDeclaration isd) {
        visit( (IIR_ObjectDeclaration) isd);
    }
    public void visit(IIR_TerminalDeclaration itd) {
        visit( (IIR_ObjectDeclaration) itd);
    }
    public void visit(IIR_VariableDeclaration ivd) {
        visit( (IIR_ObjectDeclaration) ivd);
    }
    public void visit(IIR_PhysicalUnit ipu) {
        visit( (IIR_Declaration) ipu);
    }
    public void visit(IIR_SubnatureDeclaration isd) {
        visit( (IIR_Declaration) isd);
    }
    public void visit(IIR_SubprogramDeclaration isd) {
        visit( (IIR_Declaration) isd);
    }
    public void visit(IIR_FunctionDeclaration ifd) {
        visit( (IIR_SubprogramDeclaration) ifd);
    }
    public void visit(IIR_ProcedureDeclaration ipd) {
        visit( (IIR_SubprogramDeclaration) ipd);
    }
    public void visit(IIR_SubtypeDeclaration isd) {
        visit( (IIR_Declaration) isd);
    }
    public void visit(IIR_TypeDeclaration itd) {
        visit( (IIR_Declaration) itd);
    }
    public void visit(IIR_UseClause iuc) {
        visit( (IIR_Declaration) iuc);
    }
    public void visit(IIR_DesignFile idf) {
        visit( (IIR) idf);
    }
    public void visit(IIR_Expression ie) {
        visit( (IIR) ie);
    }
    public void visit(IIR_Aggregate ia) {
        visit( (IIR_Expression) ia);
    }
    public void visit(IIR_Allocator ia) {
        visit( (IIR_Expression) ia);
    }
    public void visit(IIR_DyadicOperator ido) {
        visit( (IIR_Expression) ido);
    }
    public void visit(IIR_AdditionOperator iao) {
        visit( (IIR_DyadicOperator) iao);
    }
    public void visit(IIR_AndOperator iao) {
        visit( (IIR_DyadicOperator) iao);
    }
    public void visit(IIR_ConcatenationOperator ico) {
        visit( (IIR_DyadicOperator) ico);
    }
    public void visit(IIR_DivisionOperator ido) {
        visit( (IIR_DyadicOperator) ido);
    }
    public void visit(IIR_EqualityOperator ieo) {
        visit( (IIR_DyadicOperator) ieo);
    }
    public void visit(IIR_ExponentiationOperator ieo) {
        visit( (IIR_DyadicOperator) ieo);
    }
    public void visit(IIR_GreaterThanOperator igto) {
        visit( (IIR_DyadicOperator) igto);
    }
    public void visit(IIR_GreaterThanOrEqualOperator igtoeo) {
        visit( (IIR_DyadicOperator) igtoeo);
    }
    public void visit(IIR_InequalityOperator iio) {
        visit( (IIR_DyadicOperator) iio);
    }
    public void visit(IIR_LessThanOperator ilto) {
        visit( (IIR_DyadicOperator) ilto);
    }
    public void visit(IIR_LessThanOrEqualOperator iltoeo) {
        visit( (IIR_DyadicOperator) iltoeo);
    }
    public void visit(IIR_ModulusOperator imo) {
        visit( (IIR_DyadicOperator) imo);
    }
    public void visit(IIR_MultiplicationOperator imo) {
        visit( (IIR_DyadicOperator) imo);
    }
    public void visit(IIR_NandOperator ino) {
        visit( (IIR_DyadicOperator) ino);
    }
    public void visit(IIR_NorOperator ino) {
        visit( (IIR_DyadicOperator) ino);
    }
    public void visit(IIR_OrOperator ioo) {
        visit( (IIR_DyadicOperator) ioo);
    }
    public void visit(IIR_ROLOperator irolo) {
        visit( (IIR_DyadicOperator) irolo);
    }
    public void visit(IIR_ROROperator iroro) {
        visit( (IIR_DyadicOperator) iroro);
    }
    public void visit(IIR_RemainderOperator iro) {
        visit( (IIR_DyadicOperator) iro);
    }
    public void visit(IIR_SLAOperator islao) {
        visit( (IIR_DyadicOperator) islao);
    }
    public void visit(IIR_SLLOperator isllo) {
        visit( (IIR_DyadicOperator) isllo);
    }
    public void visit(IIR_SRAOperator israo) {
        visit( (IIR_DyadicOperator) israo);
    }
    public void visit(IIR_SRLOperator isrlo) {
        visit( (IIR_DyadicOperator) isrlo);
    }
    public void visit(IIR_SubtractionOperator iso) {
        visit( (IIR_DyadicOperator) iso);
    }
    public void visit(IIR_XnorOperator ixo) {
        visit( (IIR_DyadicOperator) ixo);
    }
    public void visit(IIR_XorOperator ixo) {
        visit( (IIR_DyadicOperator) ixo);
    }
    public void visit(IIR_FunctionCall ifc) {
        visit( (IIR_Expression) ifc);
    }
    public void visit(IIR_MonadicOperator imo) {
        visit( (IIR_Expression) imo);
    }
    public void visit(IIR_AbsoluteOperator iao) {
        visit( (IIR_MonadicOperator) iao);
    }
    public void visit(IIR_IdentityOperator iio) {
        visit( (IIR_MonadicOperator) iio);
    }
    public void visit(IIR_NegationOperator ino) {
        visit( (IIR_MonadicOperator) ino);
    }
    public void visit(IIR_NotOperator ino) {
        visit( (IIR_MonadicOperator) ino);
    }
    public void visit(IIR_OthersInitialization ioi) {
        visit( (IIR_Expression) ioi);
    }
    public void visit(IIR_PhysicalLiteral ipl) {
        visit( (IIR_Expression) ipl);
    }
    public void visit(IIR_QualifiedExpression iqe) {
        visit( (IIR_Expression) iqe);
    }
    public void visit(IIR_TypeConversion itc) {
        visit( (IIR_Expression) itc);
    }
    public void visit(IIR_List il) {
        visit( (IIR) il);
    }
    public void visit(IIR_AssociationList ial) {
        visit( (IIR_List) ial);
    }
    public void visit(IIR_AttributeSpecificationList iasl) {
        visit( (IIR_List) iasl);
    }
    public void visit(IIR_BreakList ibl) {
        visit( (IIR_List) ibl);
    }
    public void visit(IIR_CaseStatementAlternativeList icsal) {
        visit( (IIR_List) icsal);
    }
    public void visit(IIR_ChoiceList icl) {
        visit( (IIR_List) icl);
    }
    public void visit(IIR_CommentList icl) {
        visit( (IIR_List) icl);
    }
    public void visit(IIR_ConcurrentStatementList icsl) {
        visit( (IIR_List) icsl);
    }
    public void visit(IIR_ConditionalWaveformList icwl) {
        visit( (IIR_List) icwl);
    }
    public void visit(IIR_ConfigurationItemList icil) {
        visit( (IIR_List) icil);
    }
    public void visit(IIR_DeclarationList idl) {
        visit( (IIR_List) idl);
    }
    public void visit(IIR_DesignFileList idfl) {
        visit( (IIR_List) idfl);
    }
    public void visit(IIR_DesignatorList idl) {
        visit( (IIR_List) idl);
    }
    public void visit(IIR_ElementDeclarationList iedl) {
        visit( (IIR_List) iedl);
    }
    public void visit(IIR_EntityClassEntryList iecel) {
        visit( (IIR_List) iecel);
    }
    public void visit(IIR_EnumerationLiteralList iell) {
        visit( (IIR_List) iell);
    }
    public void visit(IIR_GenericList igl) {
        visit( (IIR_List) igl);
    }
    public void visit(IIR_InterfaceList iil) {
        visit( (IIR_List) iil);
    }
    public void visit(IIR_LibraryUnitList ilul) {
        visit( (IIR_List) ilul);
    }
    public void visit(IIR_NatureElementDeclarationList inedl) {
        visit( (IIR_List) inedl);
    }
    public void visit(IIR_PortList ipl) {
        visit( (IIR_List) ipl);
    }
    public void visit(IIR_SelectedWaveformList iswl) {
        visit( (IIR_List) iswl);
    }
    public void visit(IIR_SequentialStatementList issl) {
        visit( (IIR_List) issl);
    }
    public void visit(IIR_SimultaneousAlternativeList isal) {
        visit( (IIR_List) isal);
    }
    public void visit(IIR_SimultaneousStatementList issl) {
        visit( (IIR_List) issl);
    }
    public void visit(IIR_StatementList isl) {
        visit( (IIR_List) isl);
    }
    public void visit(IIR_UnitList iul) {
        visit( (IIR_List) iul);
    }
    public void visit(IIR_WaveformList iwl) {
        visit( (IIR_List) iwl);
    }
    public void visit(IIR_Literal il) {
        visit( (IIR) il);
    }
    public void visit(IIR_FloatingPointLiteral ifpl) {
        visit( (IIR_Literal) ifpl);
    }
    public void visit(IIR_FloatingPointLiteral32 ifpl32) {
        visit( (IIR_Literal) ifpl32);
    }
    public void visit(IIR_FloatingPointLiteral64 ifpl64) {
        visit( (IIR_Literal) ifpl64);
    }
    public void visit(IIR_IntegerLiteral iil) {
        visit( (IIR_Literal) iil);
    }
    public void visit(IIR_IntegerLiteral32 iil32) {
        visit( (IIR_Literal) iil32);
    }
    public void visit(IIR_IntegerLiteral64 iil64) {
        visit( (IIR_Literal) iil64);
    }
    public void visit(IIR_TextLiteral itl) {
        visit( (IIR_Literal) itl);
    }
    public void visit(IIR_BitStringLiteral ibsl) {
        visit( (IIR_TextLiteral) ibsl);
    }
    public void visit(IIR_CharacterLiteral icl) {
        visit( (IIR_TextLiteral) icl);
    }
    public void visit(IIR_Identifier ii) {
        visit( (IIR_TextLiteral) ii);
    }
    public void visit(IIR_StringLiteral isl) {
        visit( (IIR_TextLiteral) isl);
    }
    public void visit(IIR_Name in) {
        visit( (IIR) in);
    }
    public void visit(IIR_Attribute ia) {
        visit( (IIR_Name) ia);
    }
    public void visit(IIR_AboveAttribute iaa) {
        visit( (IIR_Attribute) iaa);
    }
    public void visit(IIR_AcrossAttribute iaa) {
        visit( (IIR_Attribute) iaa);
    }
    public void visit(IIR_ActiveAttribute iaa) {
        visit( (IIR_Attribute) iaa);
    }
    public void visit(IIR_AscendingAttribute iaa) {
        visit( (IIR_Attribute) iaa);
    }
    public void visit(IIR_BaseAttribute iba) {
        visit( (IIR_Attribute) iba);
    }
    public void visit(IIR_ContributionAttribute ica) {
        visit( (IIR_Attribute) ica);
    }
    public void visit(IIR_DelayedAttribute ida) {
        visit( (IIR_Attribute) ida);
    }
    public void visit(IIR_DotAttribute ida) {
        visit( (IIR_Attribute) ida);
    }
    public void visit(IIR_DrivingAttribute ida) {
        visit( (IIR_Attribute) ida);
    }
    public void visit(IIR_EventAttribute iea) {
        visit( (IIR_Attribute) iea);
    }
    public void visit(IIR_HighAttribute iha) {
        visit( (IIR_Attribute) iha);
    }
    public void visit(IIR_ImageAttribute iia) {
        visit( (IIR_Attribute) iia);
    }
    public void visit(IIR_IntegAttribute iia) {
        visit( (IIR_Attribute) iia);
    }
    public void visit(IIR_LTFAttribute iltfa) {
        visit( (IIR_Attribute) iltfa);
    }
    public void visit(IIR_LastActiveAttribute ilaa) {
        visit( (IIR_Attribute) ilaa);
    }
    public void visit(IIR_LastEventAttribute ilea) {
        visit( (IIR_Attribute) ilea);
    }
    public void visit(IIR_LastValueAttribute ilva) {
        visit( (IIR_Attribute) ilva);
    }
    public void visit(IIR_LeftAttribute ila) {
        visit( (IIR_Attribute) ila);
    }
    public void visit(IIR_LeftOfAttribute iloa) {
        visit( (IIR_Attribute) iloa);
    }
    public void visit(IIR_LengthAttribute ila) {
        visit( (IIR_Attribute) ila);
    }
    public void visit(IIR_LowAttribute ila) {
        visit( (IIR_Attribute) ila);
    }
    public void visit(IIR_PathNameAttribute ipna) {
        visit( (IIR_Attribute) ipna);
    }
    public void visit(IIR_PosAttribute ipa) {
        visit( (IIR_Attribute) ipa);
    }
    public void visit(IIR_PredAttribute ipa) {
        visit( (IIR_Attribute) ipa);
    }
    public void visit(IIR_QuietAttribute iqa) {
        visit( (IIR_Attribute) iqa);
    }
    public void visit(IIR_RangeAttribute ira) {
        visit( (IIR_Attribute) ira);
    }
    public void visit(IIR_ReferenceAttribute ira) {
        visit( (IIR_Attribute) ira);
    }
    public void visit(IIR_ReverseRangeAttribute irra) {
        visit( (IIR_Attribute) irra);
    }
    public void visit(IIR_RightAttribute ira) {
        visit( (IIR_Attribute) ira);
    }
    public void visit(IIR_RightOfAttribute iroa) {
        visit( (IIR_Attribute) iroa);
    }
    public void visit(IIR_SimpleNameAttribute isna) {
        visit( (IIR_Attribute) isna);
    }
    public void visit(IIR_StableAttribute isa) {
        visit( (IIR_Attribute) isa);
    }
    public void visit(IIR_SuccAttribute isa) {
        visit( (IIR_Attribute) isa);
    }
    public void visit(IIR_ThroughAttribute ita) {
        visit( (IIR_Attribute) ita);
    }
    public void visit(IIR_ToleranceAttribute ita) {
        visit( (IIR_Attribute) ita);
    }
    public void visit(IIR_TransactionAttribute ita) {
        visit( (IIR_Attribute) ita);
    }
    public void visit(IIR_UserAttribute iua) {
        visit( (IIR_Attribute) iua);
    }
    public void visit(IIR_ValAttribute iva) {
        visit( (IIR_Attribute) iva);
    }
    public void visit(IIR_ValueAttribute iva) {
        visit( (IIR_Attribute) iva);
    }
    public void visit(IIR_ZOHAttribute izoha) {
        visit( (IIR_Attribute) izoha);
    }
    public void visit(IIR_ZTFAttribute iztfa) {
        visit( (IIR_Attribute) iztfa);
    }
    public void visit(IIR_IndexedName iin) {
        visit( (IIR_Name) iin);
    }
    public void visit(IIR_SelectedName isn) {
        visit( (IIR_Name) isn);
    }
    public void visit(IIR_SelectedNameByAll isnba) {
        visit( (IIR_Name) isnba);
    }
    public void visit(IIR_SimpleName isn) {
        visit( (IIR_Name) isn);
    }
    public void visit(IIR_SliceName isn) {
        visit( (IIR_Name) isn);
    }
    public void visit(IIR_Statement is) {
        visit( (IIR) is);
    }
    public void visit(IIR_ConcurrentStatement ics) {
        visit( (IIR_Statement) ics);
    }
    public void visit(IIR_BlockStatement ibs) {
        visit( (IIR_ConcurrentStatement) ibs);
    }
    public void visit(IIR_ComponentInstantiationStatement icis) {
        visit( (IIR_ConcurrentStatement) icis);
    }
    public void visit(IIR_ConcurrentAssertionStatement icas) {
        visit( (IIR_ConcurrentStatement) icas);
    }
    public void visit(IIR_ConcurrentConditionalSignalAssignment iccsa) {
        visit( (IIR_ConcurrentStatement) iccsa);
    }
    public void visit(IIR_ConcurrentGenerateForStatement icgfs) {
        visit( (IIR_ConcurrentStatement) icgfs);
    }
    public void visit(IIR_ConcurrentGenerateIfStatement icgis) {
        visit( (IIR_ConcurrentStatement) icgis);
    }
    public void visit(IIR_ConcurrentProcedureCallStatement icpcs) {
        visit( (IIR_ConcurrentStatement) icpcs);
    }
    public void visit(IIR_ConcurrentSelectedSignalAssignment icssa) {
        visit( (IIR_ConcurrentStatement) icssa);
    }
    public void visit(IIR_ProcessStatement ips) {
        visit( (IIR_ConcurrentStatement) ips);
    }
    public void visit(IIR_SensitizedProcessStatement isps) {
        visit( (IIR_ProcessStatement) isps);
    }
    public void visit(IIR_SequentialStatement iss) {
        visit( (IIR_Statement) iss);
    }
    public void visit(IIR_AssertionStatement ias) {
        visit( (IIR_SequentialStatement) ias);
    }
    public void visit(IIR_BreakStatement ibs) {
        visit( (IIR_SequentialStatement) ibs);
    }
    public void visit(IIR_CaseStatement ics) {
        visit( (IIR_SequentialStatement) ics);
    }
    public void visit(IIR_ExitStatement ies) {
        visit( (IIR_SequentialStatement) ies);
    }
    public void visit(IIR_ForLoopStatement ifls) {
        visit( (IIR_SequentialStatement) ifls);
    }
    public void visit(IIR_IfStatement iis) {
        visit( (IIR_SequentialStatement) iis);
    }
    public void visit(IIR_NextStatement ins) {
        visit( (IIR_SequentialStatement) ins);
    }
    public void visit(IIR_NullStatement ins) {
        visit( (IIR_SequentialStatement) ins);
    }
    public void visit(IIR_ProcedureCallStatement ipcs) {
        visit( (IIR_SequentialStatement) ipcs);
    }
    public void visit(IIR_ReportStatement irs) {
        visit( (IIR_SequentialStatement) irs);
    }
    public void visit(IIR_ReturnStatement irs) {
        visit( (IIR_SequentialStatement) irs);
    }
    public void visit(IIR_SignalAssignmentStatement isas) {
        visit( (IIR_SequentialStatement) isas);
    }
    public void visit(IIR_VariableAssignmentStatement ivas) {
        visit( (IIR_SequentialStatement) ivas);
    }
    public void visit(IIR_WaitStatement iws) {
        visit( (IIR_SequentialStatement) iws);
    }
    public void visit(IIR_WhileLoopStatement iwls) {
        visit( (IIR_SequentialStatement) iwls);
    }
    public void visit(IIR_SimultaneousStatement iss) {
        visit( (IIR_Statement) iss);
    }
    public void visit(IIR_ConcurrentBreakStatement icbs) {
        visit( (IIR_SimultaneousStatement) icbs);
    }
    public void visit(IIR_SimpleSimultaneousStatement isss) {
        visit( (IIR_SimultaneousStatement) isss);
    }
    public void visit(IIR_SimultaneousCaseStatement iscs) {
        visit( (IIR_SimultaneousStatement) iscs);
    }
    public void visit(IIR_SimultaneousIfStatement isis) {
        visit( (IIR_SimultaneousStatement) isis);
    }
    public void visit(IIR_SimultaneousProceduralStatement isps) {
        visit( (IIR_SimultaneousStatement) isps);
    }
    public void visit(IIR_Tuple it) {
        visit( (IIR) it);
    }
    public void visit(IIR_AssociationElement iae) {
        visit( (IIR_Tuple) iae);
    }
    public void visit(IIR_AssociationElementByExpression iaebe) {
        visit( (IIR_AssociationElement) iaebe);
    }
    public void visit(IIR_AssociationElementOpen iaeo) {
        visit( (IIR_AssociationElement) iaeo);
    }
    public void visit(IIR_BreakElement ibe) {
        visit( (IIR_Tuple) ibe);
    }
    public void visit(IIR_CaseStatementAlternative icsa) {
        visit( (IIR_Tuple) icsa);
    }
    public void visit(IIR_CaseStatementAlternativeByChoices icsabc) {
        visit( (IIR_CaseStatementAlternative) icsabc);
    }
    public void visit(IIR_CaseStatementAlternativeByExpression icsabe) {
        visit( (IIR_CaseStatementAlternative) icsabe);
    }
    public void visit(IIR_CaseStatementAlternativeByOthers icsabo) {
        visit( (IIR_CaseStatementAlternative) icsabo);
    }
    public void visit(IIR_Choice ic) {
        visit( (IIR_Tuple) ic);
    }
    public void visit(IIR_ConditionalWaveform icw) {
        visit( (IIR_Tuple) icw);
    }
    public void visit(IIR_ConfigurationItem ici) {
        visit( (IIR_Tuple) ici);
    }
    public void visit(IIR_BlockConfiguration ibc) {
        visit( (IIR_ConfigurationItem) ibc);
    }
    public void visit(IIR_ComponentConfiguration icc) {
        visit( (IIR_ConfigurationItem) icc);
    }
    public void visit(IIR_Designator id) {
        visit( (IIR_Tuple) id);
    }
    public void visit(IIR_DesignatorByAll idba) {
        visit( (IIR_Designator) idba);
    }
    public void visit(IIR_DesignatorByOthers idbo) {
        visit( (IIR_Designator) idbo);
    }
    public void visit(IIR_DesignatorExplicit ide) {
        visit( (IIR_Designator) ide);
    }
    public void visit(IIR_Elsif ie) {
        visit( (IIR_Tuple) ie);
    }
    public void visit(IIR_EntityClassEntry iece) {
        visit( (IIR_Tuple) iece);
    }
    public void visit(IIR_SelectedWaveform isw) {
        visit( (IIR_Tuple) isw);
    }
    public void visit(IIR_SimultaneousAlternative isa) {
        visit( (IIR_Tuple) isa);
    }
    public void visit(IIR_SimultaneousAlternativeByChoices isabc) {
        visit( (IIR_SimultaneousAlternative) isabc);
    }
    public void visit(IIR_SimultaneousAlternativeByExpression isabe) {
        visit( (IIR_SimultaneousAlternative) isabe);
    }
    public void visit(IIR_SimultaneousAlternativeByOthers isabo) {
        visit( (IIR_SimultaneousAlternative) isabo);
    }
    public void visit(IIR_SimultaneousElsif ise) {
        visit( (IIR_Tuple) ise);
    }
    public void visit(IIR_WaveformElement iwe) {
        visit( (IIR_Tuple) iwe);
    }
    public void visit(IIR_TypeDefinition itd) {
        visit( (IIR) itd);
    }
    public void visit(IIR_AccessTypeDefinition iatd) {
        visit( (IIR_TypeDefinition) iatd);
    }
    public void visit(IIR_AccessSubtypeDefinition iasd) {
        visit( (IIR_AccessTypeDefinition) iasd);
    }
    public void visit(IIR_ArrayTypeDefinition iatd) {
        visit( (IIR_TypeDefinition) iatd);
    }
    public void visit(IIR_ArraySubtypeDefinition iasd) {
        visit( (IIR_ArrayTypeDefinition) iasd);
    }
    public void visit(IIR_FileTypeDefinition iftd) {
        visit( (IIR_TypeDefinition) iftd);
    }
    public void visit(IIR_NatureDefinition ind) {
        visit( (IIR_TypeDefinition) ind);
    }
    public void visit(IIR_CompositeNatureDefinition icnd) {
        visit( (IIR_NatureDefinition) icnd);
    }
    public void visit(IIR_ArrayNatureDefinition iand) {
        visit( (IIR_CompositeNatureDefinition) iand);
    }
    public void visit(IIR_ArraySubnatureDefinition iasd) {
        visit( (IIR_ArrayNatureDefinition) iasd);
    }
    public void visit(IIR_RecordNatureDefinition irnd) {
        visit( (IIR_CompositeNatureDefinition) irnd);
    }
    public void visit(IIR_RecordSubnatureDefinition irsd) {
        visit( (IIR_RecordNatureDefinition) irsd);
    }
    public void visit(IIR_ScalarNatureDefinition isnd) {
        visit( (IIR_NatureDefinition) isnd);
    }
    public void visit(IIR_ScalarSubnatureDefinition issd) {
        visit( (IIR_ScalarNatureDefinition) issd);
    }
    public void visit(IIR_RecordTypeDefinition irtd) {
        visit( (IIR_TypeDefinition) irtd);
    }
    public void visit(IIR_RecordSubtypeDefinition irsd) {
        visit( (IIR_RecordTypeDefinition) irsd);
    }
    public void visit(IIR_ScalarTypeDefinition istd) {
        visit( (IIR_TypeDefinition) istd);
    }
    public void visit(IIR_EnumerationTypeDefinition ietd) {
        visit( (IIR_ScalarTypeDefinition) ietd);
    }
    public void visit(IIR_EnumerationSubtypeDefinition iesd) {
        visit( (IIR_EnumerationTypeDefinition) iesd);
    }
    public void visit(IIR_FloatingTypeDefinition iftd) {
        visit( (IIR_ScalarTypeDefinition) iftd);
    }
    public void visit(IIR_FloatingSubtypeDefinition ifsd) {
        visit( (IIR_FloatingTypeDefinition) ifsd);
    }
    public void visit(IIR_IntegerTypeDefinition iitd) {
        visit( (IIR_ScalarTypeDefinition) iitd);
    }
    public void visit(IIR_IntegerSubtypeDefinition iisd) {
        visit( (IIR_IntegerTypeDefinition) iisd);
    }
    public void visit(IIR_PhysicalTypeDefinition iptd) {
        visit( (IIR_ScalarTypeDefinition) iptd);
    }
    public void visit(IIR_PhysicalSubtypeDefinition ipsd) {
        visit( (IIR_PhysicalTypeDefinition) ipsd);
    }
    public void visit(IIR_RangeTypeDefinition irtd) {
        visit( (IIR_ScalarTypeDefinition) irtd);
    }
    public void visit(IIR_Signature is) {
        visit( (IIR_TypeDefinition) is);
    }
}
