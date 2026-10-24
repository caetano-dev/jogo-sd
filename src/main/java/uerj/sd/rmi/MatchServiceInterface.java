package uerj.sd.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MatchServiceInterface extends Remote {
    String getOponent(String jsonIn) throws RemoteException;
}
