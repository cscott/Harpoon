package harpoon.Analysis.ContBuilder;


public interface IOContinuation extends VoidResultContinuation
{
	public void resume(); 
	
	public java.io.FileDescriptor getFD();
}
