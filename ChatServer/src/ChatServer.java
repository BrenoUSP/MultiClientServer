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
    PrintWriter client;
    ArrayList<String> rooms = new ArrayList<>();

    HashMap<PrintWriter, String[]> map;
    
    private JEditorPane textPane;
    private JScrollPane scrollPane;
    private JPanel panel;
    private JList list;
    private int port = 6789;
    
    public class ClientHandler implements Runnable	
    {
        BufferedReader reader;
        Socket sock;

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

             }

        }

        @Override
        public void run() 
        {
             String message;
             String[] data;

             try 
             {
                 while ((message = reader.readLine()) != null) 
                 {
                     data = message.split(":");

                     if (data[2].equals("Connect")) 
                     {
                         userAdd(data[0]);
                         tellEveryone((data[0] + ":" + data[1] + ":Chat"));
                     } 
                     else if (data[2].equals("Disconnect")) 
                     {
                         userRemove(data[0]);
                     } 
                     else if (data[2].equals("Chat")) 
                     {
                    	 tellEveryone(message);
                     } 
                 } 
              } 
              catch (Exception ex) 
              {
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

            try 
            {
                ServerSocket serverSock = new ServerSocket(port);

                while (true) 
                {
				Socket clientSock = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
				map.put(writer, new String[] {"", ""});

				Thread listener = new Thread(new ClientHandler(clientSock, writer));
				listener.start();
				addText("Got a connection. \n");
                }
            }
            catch (Exception ex)
            {

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
 
    public void userAdd (String data) 
    {
    	map.get(client)[0] = data;
    }
    
    public void userRemove (String data) 
    {
    	for (PrintWriter user : map.keySet()) {
    		if(map.get(user)[0].equals(data)) {
    			map.remove(user);
    			break;
    		}
    	}
    }
    
    public void tellEveryone(String message) 
    {
    	String [] data = message.split(":");
    	boolean send = true;
    	String usrRoom = "";

    	if(data[2].equals("Chat")) {
    		for(PrintWriter user : map.keySet()) {
    			if(map.get(user)[0].equals(data[0])){ 
    				if(map.get(user)[1].equals("")){ // USUARIO NÃO TEM SALA. COMANDOS PARA LER JOIN, NICKNAME E LIST
    					send = false;

    					if(data[1].startsWith("list")) {

    						if(!rooms.isEmpty()) {
    							user.println(":Available rooms - :Chat");
    							user.flush();

    							for(String room: rooms) {
    								user.println(":"+ room +":Chat");
    								user.flush();
    							}
    						} else {
    							user.println(":No rooms available:Chat");
    							user.flush();
    						}

    					} else if(data[1].startsWith("join <") && data[1].endsWith(">") && data[1].length() > 7) {
    						boolean limitRoom = false;
    						
    						if(rooms.size() >= 10) {
    							boolean roomEmpty;
    							String roomToDelete = "";
    							
    							for(String room : rooms) {
    								roomEmpty = true;
    								
    								for(PrintWriter userInRoom : map.keySet()) {
    									if(map.get(userInRoom)[1].equals(room)) {
    										roomEmpty = false;
    										break;
    									}
    								}
    								
    								if(roomEmpty) {
    									roomToDelete = room;
    									break;
    								}
    							}

    							if(roomToDelete.equals("")) {
    								limitRoom = true;
    								user.println(":Server already have 10 rooms. All of them contains at least one user.:Chat");
    								user.flush();
    							} else {
    								rooms.remove(roomToDelete);

    							}

    						} 

    						if(!limitRoom) {
    							usrRoom = data[1].substring(6, data[1].lastIndexOf('>'));
    							map.get(user)[1] = usrRoom;

    							boolean roomExists = false;

    							for(String room : rooms) {
    								if (room.equals(usrRoom)) {
    									roomExists = true;
    									break;
    								}
    							}

    							if(!roomExists) {
    								rooms.add(usrRoom);
    							}

    							list.setModel(new AbstractListModel() {
    								public int getSize() {
    									return rooms.size();
    								}
    								public Object getElementAt(int index) {
    									return rooms.get(index);
    								}
    							});

    							user.println(":You entered the room - " + usrRoom + ":Chat");
    							user.flush();

    							for(PrintWriter writer : map.keySet()){

    								if(map.get(writer)[1].equals(usrRoom) && !user.equals(writer)) {
    									writer.println(":" + data[0] + " enters room " + usrRoom + ".:Chat");
    									writer.flush();
    								}

    							} 

    							send = false;
    							break;

    						}

    					} else if(data[1].startsWith("nickname <") && data[1].endsWith(">") && data[1].length() > 11) {
    						String usrName = data[1].substring(10, data[1].indexOf(">"));
    						
    						for(PrintWriter writer : map.keySet()){

    							if(map.get(writer)[1].equals(usrRoom)) {
    								writer.println(":Your new name is " + usrName + ".:Chat");
    								writer.flush();

    								map.get(writer)[0] = usrName;
    								
    								writer.println(":" + usrName + ":Nickname");
    								writer.flush();
    								break;
    							}

    						} 
    						
    					} else {
    						
    						user.println(":You must enter in a room!:Chat");
    						user.flush();
    					}

    				break;
    			} else { // USUARIO JA ESTA EM UMA SALA. COMANDOS PARA LER: \
    				usrRoom = map.get(user)[1]; 

    				if(data[1].startsWith("\\")) {
    					send = false;
    					
    					for(PrintWriter writer : map.keySet()){

    						if(map.get(writer)[1].equals(usrRoom) && !user.equals(writer)) {
    							writer.println(":" + data[0] + " leaves room " + usrRoom + ".:Chat");
    							writer.flush();
    						}

    					} 
    					
    					map.get(user)[1] = "";
    					user.println(":You left the room.:Chat");
    					user.flush();

    					break;
    				}
    				


    			}
    		}
    	}



    	if (send) {
    		for(PrintWriter writer : map.keySet()){

    			if(map.get(writer)[1].equals(usrRoom)) {
    				writer.println(message);
    				writer.flush();
    			}

    		} 

    	} 
    } else {
    	for(PrintWriter writer : map.keySet()){
    		writer.println(message);
    		writer.flush();
    	} 
    }

}

public ChatServer() {
	setTitle("Servidor");
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(100, 100, 539, 300);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	setContentPane(contentPane);
	contentPane.setLayout(null);
	setResizable(false);
	setLocationRelativeTo(null);

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
	panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Rooms", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
	panel.setBounds(0, 0, 151, 246);
	contentPane.add(panel);

	list = new JList();
	list.setBounds(10, 21, 131, 214);
	panel.add(list);

	JButton btnIniciar = new JButton("Start");
	btnIniciar.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			Thread starter = new Thread(new ServerStart());
			starter.start();

			addText("Server started!\n");
		}
	});

	btnIniciar.setBounds(231, 219, 89, 23);
	contentPane.add(btnIniciar);

	JButton btnNewButton_1 = new JButton("Stop");
	btnNewButton_1.setBounds(374, 219, 89, 23);
	contentPane.add(btnNewButton_1);

}

public void addText(String str) {
	textPane.setText(textPane.getText() + str);
	textPane.select(textPane.getText().length(), textPane.getText().length()); // Desce o Scroll da área de texto
}
}
