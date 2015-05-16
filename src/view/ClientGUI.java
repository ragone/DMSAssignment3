package view;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import model.Client;

public class ClientGUI extends JFrame {
    private Client client;
    private ClientGUI gui = this;
    private JButton joinBtn, logoutBtn, sendMsgBtn, getMsgBtn;
    private JTextField usernameTextField, messageTextField;
    private JPanel mainPnl, loginPnl;
    private final TextArea mainTextArea;
    private final JList clientsList;
    private Thread t;
    
    public ClientGUI() throws RemoteException {
        super("SnapHack");
        
        // Setup JFrame
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Setup buttons and textfield
        loginPnl = new JPanel(new GridLayout(2, 3));
        
        messageTextField = new JTextField();
        messageTextField.setEnabled(false);
        loginPnl.add(messageTextField);
        
        sendMsgBtn = new JButton("Send Message");
        sendMsgBtn.setEnabled(false);
        sendMsgBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // client.getServer().sendMessage(null, null);
            }
        });
        loginPnl.add(sendMsgBtn);
        
        getMsgBtn = new JButton("Get messages");
        getMsgBtn.setEnabled(false);
        loginPnl.add(getMsgBtn);
        
        usernameTextField = new JTextField();
        loginPnl.add(usernameTextField);
        
        // Login button
        joinBtn = new JButton("Join");
        joinBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameTextField.getText();
                try {
                    client = new Client();
                } catch (RemoteException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                client.setUsername(username);
                logoutBtn.setEnabled(true);
                joinBtn.setEnabled(false);
                sendMsgBtn.setEnabled(true);
                messageTextField.setEnabled(true);
                getMsgBtn.setEnabled(true);
                usernameTextField.setEnabled(false);
                getMainTextArea().setEnabled(true);
                getMainTextArea().append(username + " joined the chat\n");
                
                ClientThread clientThread = new ClientThread(client, gui);
                t = new Thread(clientThread);
                t.start();
            }
        });
        loginPnl.add(joinBtn);
        
        // Logout button
        logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if(client.isServer()) {
                    // startLeaderElection();
                }
                logoutBtn.setEnabled(false);
                joinBtn.setEnabled(true);
                sendMsgBtn.setEnabled(false);
                messageTextField.setEnabled(false);
                getMsgBtn.setEnabled(false);
                usernameTextField.setEnabled(true);
                getMainTextArea().setEnabled(false);
                getMainTextArea().append(client.getUsername() + " left the chat\n");
                t.interrupt();
                clientsList.setListData(new Object[0]);
                try {
                    client.getServer().removeClient(client);
                } catch (RemoteException ex) {
                    Logger.getLogger(ClientGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        logoutBtn.setEnabled(false);
        loginPnl.add(logoutBtn);
        
        add(loginPnl, BorderLayout.SOUTH);
        
        mainPnl = new JPanel(new GridLayout(1, 2, 10, 10));
        
        clientsList = new JList();
        mainPnl.add(clientsList);
        mainTextArea = new TextArea();
        mainTextArea.setEnabled(false);
        mainPnl.add(mainTextArea);
        
        add(mainPnl, BorderLayout.CENTER);
        
        setVisible(true);
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
    public TextArea getMainTextArea() {
        return mainTextArea;
    }
}
