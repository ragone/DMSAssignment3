package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ListUI;
import model.Client;
import model.Message;
import model.RemoteObject;

public class ClientGUI extends JFrame {

    private Client client;
    private ClientGUI gui = this;
    private JButton joinBtn, logoutBtn, sendMsgBtn;
    private JTextField usernameTextField, messageTextField;
    private JPanel mainPnl, loginPnl;
    private final JTextArea mainTextArea;
    private final JList clientsList;
    private Thread t;
    private ClientThread clientThread;
    private String selection;

    public ClientGUI() throws RemoteException {
        super("Chatroom");

        // Setup JFrame
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            client = new Client();
        } catch (RemoteException ex) {
            Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Setup buttons and textfield
        loginPnl = new JPanel(new GridLayout(2, 3));
        loginPnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        messageTextField = new JTextField();
        messageTextField.setEnabled(false);
        loginPnl.add(messageTextField);

        sendMsgBtn = new JButton("", new ImageIcon(getClass().getResource("/res/send.png")));
        sendMsgBtn.setBorder(BorderFactory.createEmptyBorder());
        sendMsgBtn.setEnabled(false);
        sendMsgBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!client.waitingForToken()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                client.setWantToken(true);
                                client.setWaitingForToken(true);
                                while (!client.hasToken()) {
                                    Thread.sleep(100);
                                }
                                client.getServer().sendMessage(new Message(messageTextField.getText(), clientsList.getSelectedValuesList(), client.getUniqueID()));
                                client.setWantToken(false);
                                client.setWaitingForToken(false);
                            } catch (RemoteException ex) {
                                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }).start();
                }
            }
        });
        loginPnl.add(sendMsgBtn);

        loginPnl.add(new JLabel(""));

        usernameTextField = new JTextField();
        loginPnl.add(usernameTextField);

        // Login button
        joinBtn = new JButton("", new ImageIcon(getClass().getResource("/res/join.png")));
        joinBtn.setBorder(BorderFactory.createEmptyBorder());
        joinBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.getServer().addClient(client);
                } catch (RemoteException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                String username = usernameTextField.getText();
                client.setUsername(username);
                logoutBtn.setEnabled(true);
                joinBtn.setEnabled(false);
                sendMsgBtn.setEnabled(true);
                messageTextField.setEnabled(true);
                usernameTextField.setEnabled(false);
                getMainTextArea().setEnabled(true);
//                Message msg = new Message(client.getUniqueID(), client.getUniqueID(), // Added dummy receiverID
//                        username + " joined the chat\n", Message.BROADCAST);
                Message msg = new Message(" joined the chat", Message.BROADCAST, client.getUniqueID());
                try {
                    client.getServer().sendMessage(msg);
                } catch (RemoteException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        loginPnl.add(joinBtn);

        // Logout button
        logoutBtn = new JButton("", new ImageIcon(getClass().getResource("/res/logout.png")));
        logoutBtn.setBorder(BorderFactory.createEmptyBorder());
        logoutBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logoutBtn.setEnabled(false);
                joinBtn.setEnabled(true);
                sendMsgBtn.setEnabled(false);
                messageTextField.setEnabled(false);
                usernameTextField.setEnabled(true);
                getMainTextArea().setEnabled(false);
                Message msg = new Message(" left the chat", Message.BROADCAST, client.getUniqueID());
                try {
                    client.getServer().sendMessage(msg);
                } catch (RemoteException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    client.getServer().removeClient(client);
                } catch (RemoteException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (client.isServer()) {
                    try {
                        if (!client.getConnectedClients().isEmpty()) {
                            for (RemoteObject client : client.getConnectedClients()) {
                                client.setNewServer();
                            }
                        }
                        // startLeaderElection();
                    } catch (RemoteException ex) {
                        Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        logoutBtn.setEnabled(false);
        loginPnl.add(logoutBtn);

        add(loginPnl, BorderLayout.SOUTH);

        mainPnl = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPnl.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(10, 10, 0, 10), BorderFactory.createBevelBorder(1)));

        clientsList = new JList();

        mainPnl.add(clientsList);
        mainTextArea = new JTextArea();

        JScrollPane scroll = new JScrollPane(mainTextArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mainTextArea.setEnabled(false);
        mainTextArea.setLineWrap(true);
        mainPnl.add(scroll);

        add(mainPnl, BorderLayout.CENTER);

        setVisible(true);

        clientThread = new ClientThread(client, gui);
        new Thread(clientThread).start();
    }

    public static void main(String[] args) throws RemoteException {
        new ClientGUI();
    }

    /**
     * @return the clientsList
     */
    public JList getClientsList() {
        return clientsList;
    }

    /**
     * @return the mainTextArea
     */
    public JTextArea getMainTextArea() {
        return mainTextArea;
    }
}
