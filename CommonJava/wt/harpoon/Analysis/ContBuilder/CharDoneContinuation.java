package harpoon.Analysis.ContBuilder;


// adapter class. to be eliminated when we switch to optimism
public class CharDoneContinuation extends CharContinuation implements VoidResultContinuation {

    char result;
    Throwable throwable;
    boolean isResume;

    public CharDoneContinuation(char result) {
	this.result= result;
	this.isResume=true;
	Scheduler.addReady(this);
    }

    public CharDoneContinuation(Throwable throwable) {
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
