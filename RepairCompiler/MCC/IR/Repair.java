package MCC.IR;

import MCC.State;
import java.util.*;

public class Repair {
    public static RepairGenerator repairgenerator=null;

    public static void generate_dispatch(CodeWriter cr, RelationDescriptor rd, String leftvar, String rightvar) {
	repairgenerator.generate_dispatch(cr,rd,leftvar,rightvar);
    }


    public static void generate_dispatch(CodeWriter cr, SetDescriptor sd, String setvar) {
	repairgenerator.generate_dispatch(cr,sd,setvar);
    }
}
