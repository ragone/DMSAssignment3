package view;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Client;
import model.Message;

public class ClientThread implements Runnable {
    private ClientGUI gui;
    private Client model;
    
    public ClientThread(Client model, ClientGUI gui) {
        this.gui = gui;
        this.model = model;
    }
    
    
    @Override
    public void run() {
        while(true) {
            try {
                gui.getClientsList().setListData(model.getServer().getClients());
                Message message = model.getServer().getLastMessage(model.getUniqueID());
                if(message != null) {
                    gui.getMainTextArea().append(message.getTime() + message.getContent());
                }
            } catch (RemoteException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
