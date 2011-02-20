package MCC.IR;

/**
 * ReservedFieldDescriptor
 *
 * represents an unreferencable region of a structure. usually used
 * for padding structures or for allocating generic memory space
 */

public class ReservedFieldDescriptor extends FieldDescriptor {

    static int number = 0;

    public ReservedFieldDescriptor() {
        super("#RESERVED-" + ReservedFieldDescriptor.number++ + "#");
    }

}
