package javax.realtime;

public abstract class AsyncEventHandler extends RealtimeThread {
	int fireCount = 0;

	public AsyncEventHandler(SchedulingParameters sp, ReleaseParameters rp) {
		super(sp, rp);
	}

	public int getAndClearPendingFireCount() {
		int x = fireCount;
		fireCount = 0;
		return x;
	}

	public int getAndDecrementPendingFireCount() {
		return fireCount--;
	}

	public int getAndIncrementPendingFireCount() {
		return fireCount++;
	}

	public abstract void handleAsyncEvent();

	public final void run() {
		while (fireCount > 0)
			handleAsyncEvent();
	}

}
