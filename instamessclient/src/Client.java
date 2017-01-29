
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class Client extends JFrame{
    
    
    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP;
    private Socket connection;
    
    public Client(String host)
    {
        super("Client side!");
        serverIP = host;
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sendData(e.getActionCommand());
                userText.setText("");
            }
        }
        );
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(500, 350);
        setVisible(true);
    }
    
    public void startRunning()
    {
        try{
            connectToServer();
            setupStreams();
            whileChatting();
            
        }catch(EOFException e)
        {
            showMessage("\nClient terminated connection");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }finally{
            closeCrap();
        }
    }

    private void showMessage(final String m) {
        SwingUtilities.invokeLater(
                new Runnable() {

            @Override
            public void run() {
                chatWindow.append(m);
            }
        }
        );
    }

    private void closeCrap() {
        showMessage("\nClosing crap down...");
        ableToType(false);
        try{
            output.close();
            input.close();
            connection.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void connectToServer() throws IOException{
        showMessage("Attempting connection...\n");
        connection = new Socket(InetAddress.getByName(serverIP), 6789);
        showMessage("Connected to: " + connection.getInetAddress().getHostAddress());
        
    }

    private void setupStreams() throws IOException {
        output  = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\nDude your streams are now good to go! \n");
    }

    private void whileChatting() throws IOException{
        ableToType(true);
        do{
            try{
                message = (String)input.readObject();
                showMessage("\n" + message);
            }catch(ClassNotFoundException e)
            {
                showMessage("\nI dont know thatg object type!");
            }
        }while(!message.equals("SERVER - END"));
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
    private void sendData(String message)
    {
        try{
            output.writeObject("CLIENT - " + message);
            output.flush();
            showMessage("\nCLIENT - " + message);
        }catch(IOException e)
        {
            chatWindow.append("\nsomething messed up sending message hoss!");
        }
    }
}
