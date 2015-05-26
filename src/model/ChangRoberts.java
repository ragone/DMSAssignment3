package model;

import java.rmi.RemoteException;

public class ChangRoberts {

    public static final int ELECTION_IN_PROGRESS = 1;
    public static final int LEADER_FOUND = 2;

    public RemoteObject leader;
    public int status;

    public ChangRoberts(RemoteObject client) throws RemoteException {
        leader = client;
        status = ELECTION_IN_PROGRESS;
        runAlgo(client);
    }

    public void runAlgo(RemoteObject client) throws RemoteException {
        String clientID = client.getUniqueID();

        if (status == ELECTION_IN_PROGRESS) {
            if (clientID.compareTo(leader.getUniqueID()) < 0) {
                if (!client.isElectionParticipant()) {
                    leader = client;
                    client.setElectionParticipant(true);
                    runAlgo(client.getNeighbour());
                    System.out.println(client.getUsername() + " is now the leader");
                }
            } else if (clientID.compareTo(leader.getUniqueID()) > 0) {
                // forward message
                client.setElectionParticipant(true);
                runAlgo(client.getNeighbour());
                System.out.println(client.getUsername() + " passes it on");
            } else if (clientID.compareTo(leader.getUniqueID()) == 0) {
                // leader found
                client.setNewServer();
                status = LEADER_FOUND;
                client.setElectionParticipant(false);
                runAlgo(client.getNeighbour());
                System.out.println(client.getUsername() + " is now server");
            }
        } else {
            if(!client.getUniqueID().equals(leader.getUniqueID()))
                client.setElectionParticipant(false);
            System.out.println(client.getUsername() + " agrees");
        }
    }

}
