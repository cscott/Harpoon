package MCC.IR;

public interface IRErrorReporter {
    public void report(ParseNode v, String s);
    public void warn(ParseNode v, String s);
    public void setFilename(String filename);
}
