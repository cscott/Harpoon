package MCC.IR;

/**
 * ReservedSetDescriptor
 *
 * handles reserved sets: int and Token
 */

public class ReservedSetDescriptor extends SetDescriptor {

    public ReservedSetDescriptor(String name, TypeDescriptor td) {
        super(name);
        setType(td);
    }

}
