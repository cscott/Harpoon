package harpoon.Analysis.ContBuilder;


// adapter class. to be eliminated when we switch to optimism
public class BooleanDoneContinuation extends BooleanContinuation implements VoidResultContinuation {

    boolean result;
    Throwable throwable;
    boolean isResume;

    public BooleanDoneContinuation(boolean result) {
	this.result= result;
	this.isResume=true;
	Scheduler.addReady(this);
    }

    public BooleanDoneContinuation(Throwable throwable) {
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

    public void exception(Throwable t) {
    }

}
