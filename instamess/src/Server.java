

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server extends JFrame{
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket connection;
    
    
    public Server()
    {
        super("Instant Messenger");
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(e.getActionCommand());
                userText.setText("");
            }
        }
        
        );
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(500, 350);
        setVisible(true);
    }
    
    public void startRunning()
    {
        try {
            server = new ServerSocket(6789, 100);
            while(true)
            {
                try{
                    waitForConnection();
                    setupStreams();
                    whileChatting();
                }catch(EOFException eof)
                {
                    showMessage("\nServer ended the connection");
                    
                }finally{
                    closeCrap();
                }
                
                
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }

    private void waitForConnection() throws IOException{
        showMessage("Waiting to be connected....\n");
        connection = server.accept();
        showMessage("Now connected to " + connection.getInetAddress().getHostAddress());
        
    }

    private void setupStreams() throws IOException{
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input =  new ObjectInputStream(connection.getInputStream());
        showMessage("\nStreams are now setup! \n");
        
    }

    private void whileChatting() throws IOException{
        String message = "You are noe connected!";
        sendMessage(message);
        ableToType(true);
        do{
            try{
                message = (String)input.readObject();
                showMessage("\n" + message);
            }catch(ClassNotFoundException ex){
                showMessage("\nidk wtf that user send!");
            }
        }while(!message.equals("CLIENT - END"));
    }

    private void showMessage(final String text) {
        SwingUtilities.invokeLater(
                new Runnable() {

            @Override
            public void run() {
                chatWindow.append(text);
            }
        }
        );
    }

    private void closeCrap() {
        showMessage("\nClosing connections...\n");
        ableToType(false);
        try{
            output.close();
            input.close();
            connection.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    
    }

    private void sendMessage(String message) {
        try{
            output.writeObject("SERVER - " + message);
            output.flush();
            showMessage("\nSERVER - "+ message);
        }catch(IOException ex)
        {
            chatWindow.append("\nERROR: DUDDE I CANT SEND THAT MESSAGE");
        }
    
    }

    private void ableToType(final boolean b) {
        SwingUtilities.invokeLater(
                new Runnable() {

            @Override
            public void run() {
                userText.setEditable(b);
            }
        }
        );
    }
    
    
}
