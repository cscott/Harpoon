package harpoon.Analysis.ContBuilder;


// adapter class. to be eliminated when we switch to optimism
public class IntDoneContinuation extends IntContinuation implements VoidResultContinuation {

    int result;
    Throwable throwable;
    boolean isResume;

	public IntDoneContinuation() {
		this.result = -1;
		this.isResume = true;
	}

	public void setResult(int result) {
		this.result = result;
		this.isResume = true;
	}

	public void setException(Throwable ex) {
		this.throwable = ex;
		this.isResume = false;
	}
	
    public IntDoneContinuation(int result) {
	this.result= result;
	this.isResume=true;
	Scheduler.addReady(this);
    }

    public IntDoneContinuation(Throwable throwable) {
	this.throwable=throwable;
	this.isResume=false;
	Scheduler.addReady(this);
    }

    public void resume() {
	if (isResume)
	    next.resume(result);
	else
	    next.exception(throwable);
    }


    // input:  optimistic continuation (can be null)
    // output: pesimistic continuation (can't be null)
    // depressing, huh
    static public IntContinuation pesimistic(IntContinuation c)
    {
	return (!c.done)? c : new IntDoneContinuation(c.result);
    }


    public void exception(Throwable t) {
    }
}
