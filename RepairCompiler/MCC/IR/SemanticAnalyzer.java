package MCC.IR;

import java.util.*;

public interface SemanticAnalyzer {
    
    IRErrorReporter getErrorReporter();

    SymbolTable getSymbolTable();

}
