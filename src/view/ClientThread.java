package view;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import model.Client;
import model.Message;

public class ClientThread implements Runnable {

    private ClientGUI gui;
    private Client client;
    private boolean isRunning;

    public ClientThread(Client model, ClientGUI gui) {
        this.gui = gui;
        this.client = model;
        isRunning = false;
    }

    public void setRunning(boolean b) {
        isRunning = b;
    }

    @Override
    public void run() {
        while (true) {
            if (client.getUsername() != null) {
                try {
                    int[] i = gui.getClientsList().getSelectedIndices();
                    gui.getClientsList().setListData(client.getServer().getClients());
                    gui.getClientsList().setSelectedIndices(i);
                    
                    Message message = client.getServer().getLastMessage(client.getUniqueID());
                    if (message != null) {
                        gui.getMainTextArea().append(message.getTime() + message.getContent() + "\n");
                    }
                } catch (RemoteException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
