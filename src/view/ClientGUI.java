package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import leaderelection.ChangRoberts;
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

    public ClientGUI() throws RemoteException {
        super("Chatroom");

        // Setup JFrame
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                removeClient();
            }

            @Override
            public void windowClosed(WindowEvent e) {
            }

            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
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
                try {
                    Message msg = new Message(" joined the chat", Message.BROADCAST, client.getUniqueID());
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
                removeClient();
            }
        });
        logoutBtn.setEnabled(false);
        loginPnl.add(logoutBtn);

        add(loginPnl, BorderLayout.SOUTH);

        mainPnl = new JPanel(new BorderLayout());

        clientsList = new JList();

        JPanel clientPnl = new JPanel(new BorderLayout());
        clientPnl.setPreferredSize(new Dimension(200, 400));
        clientPnl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Users Online"), BorderFactory.createEtchedBorder()));
        clientPnl.add(clientsList, BorderLayout.CENTER);
        add(clientPnl, BorderLayout.WEST);
        mainTextArea = new JTextArea();

        JScrollPane scroll = new JScrollPane(mainTextArea);
        scroll.setBorder(BorderFactory.createEtchedBorder());
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mainTextArea.setEnabled(false);
        mainTextArea.setLineWrap(true);
        mainPnl.add(scroll, BorderLayout.CENTER);
        mainPnl.setBorder(BorderFactory.createTitledBorder("Chatarea"));

        add(mainPnl, BorderLayout.CENTER);

        JLabel header = new JLabel("CHATROOM");
        header.setFont(new Font("Verdana", Font.BOLD, 60));
        header.setForeground(Color.DARK_GRAY);
        header.setHorizontalAlignment(SwingConstants.CENTER);

        add(header, BorderLayout.NORTH);
        
        setVisible(true);

        clientThread = new ClientThread(client, gui);
        new Thread(clientThread).start();
    }

    public void removeClient() {
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
