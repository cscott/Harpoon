package MCC.IR;

import java.util.*;

public abstract class Inclusion {

    protected Inclusion() {}

    public abstract Set getTargetDescriptors();

    public abstract Set getRequiredDescriptors();

    public abstract void generate(CodeWriter writer);
    
    public abstract boolean typecheck(SemanticAnalyzer sa);
         
}

