import java.util.*;
import java.io.*;
import java.awt.*; /* abstract window toolkit */
import java.awt.event.*;
import javax.swing.*;
import java.net.Socket;
import java.net.ServerSocket;
import javax.swing.text.*;
public class ChatClient implements ActionListener, WindowListener{
    //private static JScrollPane clientPane;
    //private static JScrollPane messagePane;
    //private static JScrollPane typePane;
    private static JFrame frame;
    private static JPanel westPanel;
    private static JPanel centerPanel;
    private static JPanel southPanel;
    private static JButton sendButton;
    private static JTextField typeField;
    private static JTextArea clientList;
    private static JTextArea messages;

    private static Socket socket; //  The server
    private static PrintWriter writer;
    private static BufferedReader reader;
    private static String name;
    public static void main(String[] args) {
        new ChatClient();

        String ip = JOptionPane.showInputDialog(frame,
                "Enter the IP Address to connect to. Default is \"127.0.0.1\".",
                "IP Address",JOptionPane.QUESTION_MESSAGE);
        if(ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }

        JOptionPane.showMessageDialog(frame,"Client will connect to Port 50000.",
            "Port Number",JOptionPane.PLAIN_MESSAGE);
        clientList.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        messages.setFont(new Font("TimesRoman", Font.PLAIN, 16)); 
        typeField.setFont(new Font("TimesRoman", Font.PLAIN, 16));

        try {
            socket = new Socket(ip,50000);
            writer = new PrintWriter(socket.getOutputStream(),true);
            reader = new BufferedReader(new InputStreamReader
                (socket.getInputStream()));
            typeField.setEditable(true);
            new Thread(new ListenThread()).start();
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(frame,"The Server is not Hosting!",
                "Server Hosting",JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }
        //while(true) {}
    }

    public ChatClient() {
        frame = new JFrame("ChatClient");
        frame.addWindowListener(this);

        westPanel = new JPanel(); // For clientList
        centerPanel = new JPanel(); // For messages
        southPanel = new JPanel(); // For typeField

        westPanel.setPreferredSize(new Dimension(300,500));
        centerPanel.setPreferredSize(new Dimension(700,500));
        southPanel.setPreferredSize(new Dimension(1000,100));

        westPanel.setBackground(Color.WHITE);
        centerPanel.setBackground(Color.WHITE);
        southPanel.setBackground(Color.WHITE);

        westPanel.setLayout(new BorderLayout());
        centerPanel.setLayout(new BorderLayout());
        southPanel.setLayout(new BorderLayout());

        sendButton = new JButton("SEND");
        sendButton.setBackground(new Color(127,127,127));
        sendButton.setPreferredSize(new Dimension(100,100));
        sendButton.addActionListener(this);

        typeField = new JTextField("");
        typeField.setPreferredSize(new Dimension(900,100));
        typeField.addActionListener(this);

        clientList = new JTextArea();
        clientList.setPreferredSize(new Dimension(300,500));
        clientList.setEditable(false);

        messages = new JTextArea();
        messages.setPreferredSize(new Dimension(700,500));
        messages.setEditable(false);

        southPanel.add(sendButton, BorderLayout.EAST);
        southPanel.add(typeField, BorderLayout.CENTER);

        DefaultCaret caret = (DefaultCaret)clientList.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        westPanel.add(clientList);
        JScrollPane westScroll = new JScrollPane(clientList);
        westScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        westScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        westPanel.add(westScroll);
        
        messages.setLineWrap(true);
        messages.setWrapStyleWord(true);
        messages.setRows(500);
        DefaultCaret caret2 = (DefaultCaret)messages.getCaret();
        caret2.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        centerPanel.add(messages);
        JScrollPane centerScroll = new JScrollPane(messages);
        centerScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        centerScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        centerPanel.add(centerScroll);

        southPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        westPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        centerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        frame.setLayout(new BorderLayout());
        frame.add(westPanel, BorderLayout.WEST);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(southPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.setAlwaysOnTop(false);

    }

    public void actionPerformed(ActionEvent e) {
        String line = typeField.getText();
        if(!line.equals("")) {
            if(name == null) {
                name = line;
            }
            typeField.setText("");
            messages.append(name+": "+line+"\n");
            if(writer != null) {
                writer.println(line);
            }
        }
    }

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {
        System.exit(0);
    }

    public void windowClosed(WindowEvent e) {
    }

    private static class ListenThread implements Runnable {
        public void run() {
            try {
                while(true) {
                    String line = reader.readLine();
                    if(line.indexOf("message")==0) {
                        line = line.substring(9);
                        messages.append(line+"\n");
                    }
                    else if(line.indexOf("addClient")==0) {
                        clientList.append(line.substring(11)+"\n");
                    }
                    else if(line.indexOf("closeServer")==0) {
                        JOptionPane.showMessageDialog(frame,"The Server Disconnected!",
                            "Server Disconnected",JOptionPane.PLAIN_MESSAGE);
                        System.exit(0);
                    }
                    else if(line.indexOf("clearClients")==0) {
                        clientList.setText("");
                    }
                    else if(line.indexOf("notification")==0) {
                        line = "Notification: "+line.substring(14);
                        messages.append(line+"\n");
                    }
                }
            } catch(Exception e) {
                JOptionPane.showMessageDialog(frame,"The Server Disconnected!",
                    "Server Disconnected",JOptionPane.PLAIN_MESSAGE);
                System.exit(0);
            }
        }
    }
}