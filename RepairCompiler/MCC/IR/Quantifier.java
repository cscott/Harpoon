package MCC.IR;

import java.util.*;

public abstract class Quantifier {

    public abstract Set getRequiredDescriptors();

    public abstract void generate_open(CodeWriter writer);

    public abstract int generate_worklistload(CodeWriter writer, int offset);
    public abstract int generate_workliststore(CodeWriter writer, int offset);

}
