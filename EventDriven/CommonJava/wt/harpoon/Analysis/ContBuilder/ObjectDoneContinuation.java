package harpoon.Analysis.ContBuilder;


// adapter class. to be eliminated when we switch to optimism
public class ObjectDoneContinuation extends ObjectContinuation implements VoidResultContinuation {

    Object result;
    Throwable throwable;
    boolean isResume;

	public ObjectDoneContinuation() {
		this.result = null;
		this.throwable = null;
		this.isResume = true;
	}

	public void setResult(Object result) {
		this.result = result;
		this.throwable = null;
		this.isResume = true;
	}

	public void setException(Throwable ex) {
		this.result = null;
		this.throwable = ex;
		this.isResume = false;
	}
	
    public ObjectDoneContinuation(Object result) {
	this.result= result;
	this.isResume=true;
	Scheduler.addReady(this);
    }

    public ObjectDoneContinuation(Throwable throwable) {
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

    static public ObjectContinuation pesimistic(ObjectContinuation c) {
	return (!c.done)? c : new ObjectDoneContinuation(c.result);
    }
    
    public void exception(Throwable t) {
    }
}


