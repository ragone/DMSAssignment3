package view;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import model.Client;

public class ClientGUI extends JFrame {
    private Client client;
    private JPanel loginPnl;
    private JButton joinBtn;
    private JButton logoutBtn;
    private JTextField usernameTextField;
    private JPanel mainPnl;
    private final TextArea mainTextArea;
    
    public ClientGUI() throws RemoteException {
        super("SnapHack");
        client = new Client();
        
        // Setup JFrame
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Setup buttons and textfield
        loginPnl = new JPanel(new GridLayout(1, 3));
        
        usernameTextField = new JTextField();
        loginPnl.add(usernameTextField);
        
        // Login button
        joinBtn = new JButton("Join");
        joinBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameTextField.getText();
                client.setUsername(username);
                logoutBtn.setEnabled(true);
                joinBtn.setEnabled(false);
                usernameTextField.setEnabled(false);
                mainTextArea.setEnabled(true);
                mainTextArea.append(username + " joined the chat\n");
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
                usernameTextField.setEnabled(true);
                mainTextArea.setEnabled(false);
                mainTextArea.append(client.getUsername() + " left the chat\n");
            }
        });
        logoutBtn.setEnabled(false);
        loginPnl.add(logoutBtn);
        
        add(loginPnl, BorderLayout.SOUTH);
        
        mainPnl = new JPanel(new GridLayout(1, 2));
        
        mainPnl.add(new JLabel(""));
        mainTextArea = new TextArea();
        mainTextArea.setEnabled(false);
        mainPnl.add(mainTextArea);
        
        add(mainPnl, BorderLayout.CENTER);
        
        setVisible(true);
    }
    
    public static void main(String[] args) throws RemoteException {
        new ClientGUI();
    }
}
