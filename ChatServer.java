import java.util.*;
import java.io.*;
import java.awt.*; /* abstract window toolkit */
import java.awt.event.*;
import javax.swing.*;
import java.net.Socket;
import java.net.ServerSocket;
import javax.swing.text.*;
public class ChatServer implements ActionListener, WindowListener {
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

    private static ServerSocket server; // me - The server
    private static Map<String, Socket> clients; // you - The client
    private static Map<String,PrintWriter> writers;
    private static Map<String,BufferedReader> readers;
    private static String servername;
    public static void main(String[] args) {
        new ChatServer();
        JOptionPane.showMessageDialog(frame,"Port 50000 will be used to host the server.",
            "Port Number",JOptionPane.PLAIN_MESSAGE);
        try {
            server = new ServerSocket(50000);
            clients = new HashMap<String, Socket>();
            writers = new HashMap<String,PrintWriter>();
            readers = new HashMap<String,BufferedReader>();
        } catch(Exception e) {
            e.printStackTrace();
        }
        servername = JOptionPane.showInputDialog(frame,"Enter your username. Default is \"Server\".",
            "Username",JOptionPane.QUESTION_MESSAGE);
        if(servername == null || servername.equals("")) {
            servername = "Server";
        }
        clientList.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        messages.setFont(new Font("TimesRoman", Font.PLAIN, 16)); 
        typeField.setFont(new Font("TimesRoman", Font.PLAIN, 16));
        clientList.setText("Connected Users:\n"+servername+"\n");
        messages.setText("Waiting for Clients to Connect\n");
        new Thread(new AcceptThread()).start();
        //while(true) {}
    }

    public ChatServer() {
        frame = new JFrame("ChatServer");
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
            typeField.setText("");
            broadcast(servername,"message",line);
            messages.append(servername+": "+line+"\n");
        }
    }

    public static void updateClientList() {
        Iterator<String> names = clients.keySet().iterator();
        String list = "Connected Users:\n"+servername+"\n";
        broadcast(servername,"clearClients","");
        broadcast(servername,"addClient","Connected Users:");
        broadcast(servername,"addClient",servername);
        while(names.hasNext()) {
            String temp = names.next();
            broadcast(servername,"addClient",temp);
            list += temp+"\n";
        }
        clientList.setText(list);
    }
    // Incomplete
    public static void broadcast(String inputname,String messagetype, String message) {
        /* Messagetypes - message(message is the message to send), notification(message is notification), 
        clearClients, addClient(message is client), closeServer*/
        Iterator<String> names = clients.keySet().iterator();
        while(names.hasNext()) {
            String outputname = names.next();
            if(!outputname.equals(inputname)) {
                try {
                    PrintWriter outputWriter = writers.get(outputname);
                    outputWriter.println(messagetype+": "+(messagetype.equals("message")?inputname+": ":"")+message);
                }
                catch(Exception err) {
                    err.printStackTrace();
                }
            }
        }
    }

    public void windowActivated(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {
        if(server != null && !server.isClosed()) {
            try {
                broadcast(servername,"closeServer","");
                server.close();
            } catch(IOException err) {
                err.printStackTrace();
            }
        }
    }

    public void windowClosed(WindowEvent e) {
    }
    private static class AcceptThread implements Runnable {
        public void run() {
            try {
                while(true) {
                    Socket socket = server.accept();
                    // WAITING FOR NAME BEFORE NEXT ACCEPT
                    new Thread(new ListenThread(socket)).start();
                }
            }
            catch(Exception err) {
                //err.printStackTrace();
            }
        }
    }
    // Incomplete
    private static class ListenThread implements Runnable {
        private String name;
        private Socket socket;
        public ListenThread(Socket socket) {
            this.name = name;
            this.socket = socket;
        }

        public void run() {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);
                writer.println("notification: Welcome to the Server!");
                writer.println("notification: What would you like your username to be?");
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                name = reader.readLine();
                // CHECK IF NAME IS UNIQUE
                clients.put(name,socket);
                writers.put(name,writer);
                readers.put(name,reader);
                broadcast(servername,"notification",name+" connected!");
                messages.append("Notification: "+name+" connected!\n");
                updateClientList();
                while(true) {
                    String line = reader.readLine();
                    broadcast(name,"message",line);
                    messages.append(name+": "+line+"\n");
                }
            } catch(Exception e) {
                messages.append("Notification: "+name+" disconnected! Exiting the server!\n");
                clients.remove(name);
                writers.remove(name);
                readers.remove(name);
                updateClientList();
                broadcast(servername,"notification",name+" disconnected! Exiting the server!");
                Thread.currentThread().interrupt();
                return;
                //System.exit(0);
            }
        }

    }
}