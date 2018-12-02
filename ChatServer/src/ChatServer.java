import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.StyledEditorKit;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChatServer extends JFrame {
	private JPanel contentPane;
    BufferedReader reader;
    Socket sock;
    PrintWriter client;
    
    ArrayList<String> rooms;
    String[] users;
    HashMap<PrintWriter, String[]> map;
    
    private JEditorPane textPane;
    private JScrollPane scrollPane;
    private JPanel panel;
    private JList list;
    private int port = 2222;
    
    public class ClientHandler implements Runnable	
    {
        public ClientHandler(Socket clientSocket, PrintWriter user) 
        {
             client = user;
             try 
             {
                 sock = clientSocket;
                 InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                 reader = new BufferedReader(isReader);
             }
             catch (Exception ex) 
             {
                 addText("Unexpected error... \n");
             }

        }

        @Override
        public void run() 
        {
             String message = "", connect = "Connect", disconnect = "Disconnect", chat = "Chat" ;
             String[] data;

             try 
             {
                 while ((message = reader.readLine()) != null) 
                 {
		            
                     data = message.split(":");

                     if (data[2].equals(connect)) 
                     {
                         userAdd(message);
                         tellEveryone((data[0] + ":" + data[1] + ":" + chat));
                     } 
                     else if (data[2].equals(disconnect)) 
                     {
                         userRemove(data[0]);
                         tellEveryone((data[0] + ":has disconnected." + ":" + chat));
                     } 
                     else if (data[2].equals(chat)) 
                     {
                         tellEveryone(message);
                     } 
                     else 
                     {
                    	 addText("No Conditions were met. \n");
                     }
                 } 
              } 
              catch (Exception ex) 
              {
            	 addText("Lost a connection. \n");
                 ex.printStackTrace();
                 map.remove(client);
              } 
 	} 
     }
    
    public class ServerStart implements Runnable 
    {
        @Override
        public void run() 
        {

        	map = new HashMap<>();
        	rooms = new ArrayList<>();
        	
        	try 
        	{
        		ServerSocket serverSock = new ServerSocket(port, 100);

        		while (true) 
        		{
        			Socket clientSock = serverSock.accept();
        			PrintWriter writer = new PrintWriter(clientSock.getOutputStream());

        			map.put(writer, new String[2]);
        			
        			Thread listener = new Thread(new ClientHandler(clientSock, writer));
        			listener.start();
        			addText("Got a connection. \n");
        		}
        	}
        	catch (Exception ex)
        	{
        		addText("Error making a connection. \n");
        	}
        }
    }
    
    public static void main(String args[]) 
    {
        java.awt.EventQueue.invokeLater(new Runnable() 
        {
            @Override
            public void run() 
            {
                new ChatServer().setVisible(true);
            }
        });
    }
    
    public String findRoom(String message) {
    	String[] data = message.split(":");

    	String usrName = data[0];

		for (PrintWriter client : map.keySet()) {
			if(map.get(client)[0].equals(usrName)) {
				return map.get(client)[1];
			}
		}
    	
		return "";
    }

    public void systemCall(String message) {
    	String[] data = message.split(":");

		for (PrintWriter client : map.keySet()) {
    		try 
    		{
    			client.println(message);
    			client.flush();


    			if(!message.contains("Server")) {
    				addText(data[0] + ":" + data[1] + "\n");
    			}

    		}


    		catch (Exception ex) 
    		{
    			addText("Error telling System. \n");
    		}

    	} 
    }
    
    public void roomCall(String room, String message) {
			for (PrintWriter client : map.keySet()) {

    			if(map.get(client)[1].equals(room)) {
    				PrintWriter writer = client;
    				writer.println(message);
    				writer.flush();
    			}

			}
    }

    public void tellEveryone(String message) 
    {
    	String[] data = message.split(":");
		
		if(data[2].equals("Chat")) {
        	String room = findRoom(message);
        	roomCall(room, message);
        	
    	} else {
    		systemCall(message);
    	}
    	  	
    }

    public void userAdd (String message) 
    {
    	String[] data = message.split(":");

    	System.out.println(data[1]);
    	String usrRoom = data[1].substring(6, data[1].lastIndexOf('>'));

    	boolean roomExists = false;

    	for(String room : rooms) {
    		if (room.equals(usrRoom)) {
    			roomExists = true;
    			break;
    		}
    	}

    	map.get(client)[1] = usrRoom;

    	if(!roomExists) {
    		rooms.add(usrRoom);
    	}

    	list.setModel(new AbstractListModel() {
    		//String[] values = (String[]) servers.toArray();
    		public int getSize() {
    			return rooms.size();
    		}
    		public Object getElementAt(int index) {
    			return rooms.get(index);
    		}
    	});

    	map.get(client)[0] = data[0];
    }
    
    public void userRemove (String data) 
    {
    	String[] userTemp;
    	
        for(PrintWriter client : map.keySet()) {
        	userTemp = map.get(client);
        	
        	if(userTemp[0].equals(data)) {
        		map.remove(client);
        		break;
        	}
        }

    }
    
	
	/**
	 * Create the frame.
	 */
	public ChatServer() {
		setTitle("Servidor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 539, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setResizable(false);
        
        scrollPane = new JScrollPane();
        scrollPane.setBounds(161, 11, 362, 197);
        contentPane.add(scrollPane);
        
        textPane = new JEditorPane();
        scrollPane.setViewportView(textPane);
        textPane.setEditable(false);
        textPane.setContentType("text/html");
        textPane.setEditorKit(new StyledEditorKit());
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        textPane.setFont(new Font("Trebuchet MS", Font.BOLD, 20));
        
        panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Salas", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panel.setBounds(0, 0, 151, 246);
        contentPane.add(panel);
        
        list = new JList();
        list.setBounds(10, 21, 131, 214);
        panel.add(list);
        
        JButton btnIniciar = new JButton("Iniciar");
        btnIniciar.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
                Thread starter = new Thread(new ServerStart());
                starter.start();
                
                addText("Servidor iniciado!\n");
        	}
        });

        btnIniciar.setBounds(231, 219, 89, 23);
        contentPane.add(btnIniciar);
        
        JButton btnNewButton_1 = new JButton("Parar");
        btnNewButton_1.setBounds(374, 219, 89, 23);
        contentPane.add(btnNewButton_1);

	}
	
    public void addText(String str) {
        textPane.setText(textPane.getText() + str);
        textPane.select(textPane.getText().length(), textPane.getText().length());
    }
}
