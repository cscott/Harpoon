package MCC.IR;

/**
 * If an exception condition occurs while building an IR, an IRException
 * object is thrown.
 */

public class IRException extends java.lang.RuntimeException
{
    /**
     * @param reason    reason for exception 
     */
    public IRException(String reason)
    {
	super(reason);
    }

    public IRException()
    {
	super("IR ERROR");
    }
}

