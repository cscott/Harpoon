package MCC.IR;

import java.util.*;

public abstract class Quantifier {

    public abstract Set getRequiredDescriptors();

    public abstract void generate_open(CodeWriter writer);
}
