import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;

class odsModel extends ODS implements remoteODS {

    static {
	int i;
	i = 2;
    }

    public static void main(String args[]) {
    }

    static public void initialize (int numMachines, int machineNum){
	machineIndex = machineNum;

	try{
	    ods = (remoteODS) new odsModel();
	} catch (Exception e){
	    e.printStackTrace();
	}
	
	machines = new remoteODS[numMachines];
	
	System.setSecurityManager (new RMISecurityManager());
	for (int i = 0; i < numMachines; i++){
	    try{
		System.out.println ("Looking for: HelloServer" + i);
		remoteODS obj = 
		    (remoteODS) Naming.lookup ("//lm.lcs.mit.edu/HelloServer" + i);
		machines[i] = obj;
		System.out.println ("Actually finding another machien on the network");
		obj.remoteInitialize (machineIndex, ods);
	    } catch (NotBoundException e){
		//just means that this server isn't up yet, so do nothing.
	    } catch (Exception e){
		e.printStackTrace();
	    }
	}
	try {
	    Naming.rebind ("//lm.lcs.mit.edu/HelloServer" + machineNum, ods);
	    System.out.println ("Bound myself as: HelloServer" + machineNum);
	} catch (Exception e){
	    e.printStackTrace();
	    }
	System.out.println ("I just initialized I think...");
	
	
    }
    
    odsModel() throws java.rmi.RemoteException {
	//this.machineIndex = machineIndex;
    }
    
}

