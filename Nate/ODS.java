import java.rmi.server.UnicastRemoteObject;
import java.rmi.*;

class ODS extends UnicastRemoteObject {
  
  static remoteODS ods;
  static int machineIndex;
  static remoteODS machines[];
  
  ODS () throws java.rmi.RemoteException{	
  }
  
  /*static public void initialize (int numMachines, int machineNum){

    try{
    ods = (remoteODS) new odsModel();
    } catch (Exception e){
    e.printStackTrace();
    }
    
    machines = new remoteODS[numMachines];
    
    System.setSecurityManager (new RMISecurityManager());
    for (int i = 0; i < numMachines; i++){
    try{
    remoteODS obj = 
    (remoteODS) Naming.lookup ("//lm.lcs.mit.edu/server" + i);
    machines[i] = obj;
    obj.remoteInitialize (machineIndex, ods);
    } catch (NotBoundException e){
    //just means that this server isn't up yet, so do nothing.
    } catch (Exception e){
    e.printStackTrace();
    }
    }
    try {
    Naming.rebind ("//lm.lcs.mit.edu/server" + machineIndex, ods);
    } catch (Exception e){
    e.printStackTrace();
    }
    System.out.println ("I just initialized I think...");
    
    }*/

  public remoteODS getMachine (int index){
    System.out.println ("Getting the machine for index: " + index);
   
    remoteODS mach = machines[index];
    if (mach == null){
      System.out.println ("Looks like mach is null for some reason");
    }
    return machines[index];
  }

  public void remoteInitialize (int machineNum, remoteODS newODS){
    System.out.println ("Hey look... remoteInitialize is acctually being called");
    machines[machineNum] = newODS;
  }
}


// set emacs indentation style.
// Local Variables:
// c-basic-offset:2
// End:




