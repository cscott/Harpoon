public interface remoteODS extends java.rmi.Remote {
    //void initialize (int numMachines, int machineNum) throws java.rmi.RemoteException;
    void remoteInitialize (int machineNum, remoteODS newODS) throws java.rmi.RemoteException;
    public remoteODS getMachine (int index) throws java.rmi.RemoteException;
}

