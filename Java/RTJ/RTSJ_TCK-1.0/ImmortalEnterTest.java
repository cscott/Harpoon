import javax.realtime.*;

public class ImmortalEnterTest extends RealtimeThread
{
    public static void main(String[] args)
    {
        ImmortalEnterTest im = new ImmortalEnterTest();
        im.start();
    }

    public void run()
    {
        System.out.println("In the run method\n");
        MemoryArea m = ImmortalMemory.instance();
        try
        {
            m.executeInArea(new Runnable() {
                public void run()
                {
                    System.out.println("Hey I'm inside!");
                }
            });
        }
        catch(Exception e) { e.printStackTrace(); }

    }
}
