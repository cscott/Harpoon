package MCC;
// base package
// this class stores all the information needed for between passes

import MCC.IR.*;
import java.util.*;

public class State {
    
    public static State currentState = null;

    public static boolean failed = false;
    public static boolean debug = false;
    public static int verbose = 0;

    public static String infile;
    public static String outfile;

    public static final int VERBOSE_TOKENS = 1;

    public ParseNode ptStructures;
    public ParseNode ptModel;
    public ParseNode ptConstraints;
    public ParseNode ptSpace;

    public SymbolTable stSets;
    public SymbolTable stRelations;
    public SymbolTable stTypes;
    public SymbolTable stGlobals;

    public Vector vConstraints;
    public Vector vRules;

    public Hashtable rulenodes;
    public Hashtable constraintnodes;    

    State() {
        vConstraints = null;
        vRules = null;

        stTypes = null;
        stSets = null;
        stRelations = null;
        stGlobals = null;

        ptStructures = null;
        ptModel = null;
        ptConstraints = null;
        ptSpace = null;
    }

}
