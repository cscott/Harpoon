class ImageData
{
    public int number0;
    public int number1;
    public int number2;

    public Object lock = new Object();
    public RealtimeThread dummyThread = new RealtimeThread()
        {
	    public void run()
	    {
		sychronized(lock)
		{
		    lock.wait();
		}
	    }
	};
}
