package view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Client;

public class ClientThread implements Runnable {

    private ClientGUI gui;
    private Client client;
    private boolean isRunning;
    final int port = 44827;

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
        new Thread(new Runnable() {
            Socket clientSock;
            ServerSocket serverSock;
            BufferedReader br;
            
            @Override
            public void run() {
                try {
                    serverSock = new ServerSocket(client.getPort());
                } catch (IOException ex) {
                    Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                while (true) {
                    try {
                        clientSock = serverSock.accept();
                        br = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
                        gui.getMainTextArea().append(br.readLine() + "\n");
//
//                        br.close();
//                        serverSock.close();
//                        clientSock.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        while (true) {
            if (client.getUsername() != null) {
                try {
                    int[] i = gui.getClientsList().getSelectedIndices();
                    String[] clients = client.getServer().getClients();
                    gui.getClientsList().setListData(clients);
                    gui.getClientsList().setSelectedIndices(i);
//
//                    Message message = client.getServer().getLastMessage(client.getUniqueID());
//                    if (message != null) {
//                        RemoteObject sender = client.getServer().getClientByID(message.getSender());
//                        gui.getMainTextArea().append(message.getTime() + sender.getUsername() + ": " + message.getContent() + "\n");
//                    }
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
