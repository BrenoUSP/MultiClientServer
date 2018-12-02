import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.StyledEditorKit;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChatServer extends JFrame {
	private JPanel contentPane;
    BufferedReader reader;
    Socket sock;
    PrintWriter client;
    ArrayList<PrintWriter> clientOutputStreams;
    ArrayList<String> users;
    private JEditorPane textPane;
    private JScrollPane scrollPane;
    private JPanel panel;
    private JList list;
    
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
             String message, connect = "Connect", disconnect = "Disconnect", chat = "Chat" ;
             String[] data;

             try 
             {
                 while ((message = reader.readLine()) != null) 
                 {
                	 //addText("Received: " + message + "\n");
                     data = message.split(":");

                     if (data[2].equals(connect)) 
                     {
                         tellEveryone((data[0] + ":" + data[1] + ":" + chat));
                         userAdd(data[0]);
                     } 
                     else if (data[2].equals(disconnect)) 
                     {
                         tellEveryone((data[0] + ":has disconnected." + ":" + chat));
                         userRemove(data[0]);
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
                 clientOutputStreams.remove(client);
              } 
 	} 
     }
    
    public class ServerStart implements Runnable 
    {
        @Override
        public void run() 
        {
            clientOutputStreams = new ArrayList();
            users = new ArrayList();  

            try 
            {
                ServerSocket serverSock = new ServerSocket(6789, 100);

                while (true) 
                {
				Socket clientSock = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
				clientOutputStreams.add(writer);

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
    
    public String findServer(String message) {
    	String[] data = message.split(":");

    	String usrName = data[0];

    	try {

    		File file = new File("M:/users.txt");

    		String exp = "";
    		
    		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
    		    for(String line; (line = br.readLine()) != null; ) {
    		        if (line.contains(usrName)) {
    		        	exp = line;
    		        	break;
    		        }
    		    }
    		}
    				
    		String[] lineComp;	
    		
    		//if (!exp.contains(" ")) {
    		//	return "";
    		//} else {
        	lineComp = exp.split(" ");


    		return lineComp[1];
    	} catch (IOException e) {
        	return "";
    	}
    	
    }

    public void systemCall(String message) {
    	Iterator it = clientOutputStreams.iterator();
    	String[] data = message.split(":");

    	while (it.hasNext()) 
    	{
    		try 
    		{
    			PrintWriter writer = (PrintWriter) it.next();

    			if(!message.contains("join <")) {
    				writer.println(message);
    			} else {
    				File file = new File("M:/servers.txt");

    				FileWriter fw = new FileWriter(file, true);
    				BufferedWriter bw = new BufferedWriter(fw);
    				PrintWriter out = new PrintWriter(bw);

    				BufferedReader in = new BufferedReader(new FileReader(file));
    				String line;
    				ArrayList<String> servers = new ArrayList<>();

    				while((line = in.readLine()) != null)
    				{
    					servers.add(line);
    				}

    				list.setModel(new AbstractListModel() {
    					//String[] values = (String[]) servers.toArray();
    					public int getSize() {
    						return servers.size();
    					}
    					public Object getElementAt(int index) {
    						return servers.get(index);
    					}
    				});

    				in.close();

    				out.close();
    			}

    			if(!message.contains("Server")) {
    				addText(data[0] + ":" + data[1] + "\n");
    			}
    			writer.flush();
    		}


    		catch (Exception ex) 
    		{
    			addText("Error telling System. \n");
    		}

    	} 
    }
    
    /*
     * ARRUMAR A ORDEM QUE É MANDADA AS MENSAGENS PARA FUNCIONAR
     */
    
    public void serverCall(String server, String message) {
    	//Iterator it = clientOutputStreams.iterator();
    	String line1 = "";

		//System.out.println(message + server);

    	try 
    	{
    		File file1 = new File("M:/users.txt");

    		FileWriter fw1 = new FileWriter(file1, true);
    		BufferedWriter bw1 = new BufferedWriter(fw1);
    		PrintWriter out1 = new PrintWriter(bw1);

    		BufferedReader in1 = new BufferedReader(new FileReader(file1));
			line1 = in1.readLine();
			System.out.println(line1);
			
			for (PrintWriter client : clientOutputStreams) {
    	
    			String[] lineComp = line1.split(" ");

    			/*
    			 * SE O SERVIDOR É IGUAL
    			 */

    			if(server.equals(lineComp[1])) {
    				PrintWriter writer = client;
    				writer.println(message);
    				writer.flush();
    			}


    			//if(!message.contains("Server")) {
    			//	addText(data[0] + ":" + data[1] + "\n");
    			//}
    			
    			line1 = in1.readLine();
    		}
			
			out1.close();
			in1.close();
			
    	} 

    	catch (Exception ex) 
    	{
    		addText("Error telling Server. \n");
    	}
    }

    public void tellEveryone(String message) 
    {
    	String[] data = message.split(":");
		String server = "";
		
    	if(data[2].equals("Chat")) {
        	server = findServer(message);
        	
        	serverCall(server, message);
        	
    	} else {
    		systemCall(message);
    	}
    	

    	int counter = 0;

    	
    }

    public void userAdd (String data) 
    {
        String message, add = ": :Connect", done = "Server: :Done", name = data;
        //addText("Before " + name + " added. \n");
        users.add(name);
        //addText("After " + name + " added. \n");
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);

        for (String token:tempList) 
        {
            message = (token + add);
            tellEveryone(message);
        }
        tellEveryone(done);
    }
    
    public void userRemove (String data) 
    {
        String message, add = ": :Connect", done = "Server: :Done", name = data;
        users.remove(name);
        String[] tempList = new String[(users.size())];
        users.toArray(tempList);

        for (String token:tempList) 
        {
            message = (token + add);
            tellEveryone(message);
        }
        tellEveryone(done);
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
		try {

			File file = new File("M:/servers.txt");

			//Create the file
			//if (file.createNewFile()){
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);


			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			ArrayList<String> servers = new ArrayList<>();

			while((line = in.readLine()) != null)
			{
				servers.add(line);
			}

			list.setModel(new AbstractListModel() {
				//String[] values = (String[]) servers.toArray();
				public int getSize() {
					return servers.size();
				}
				public Object getElementAt(int index) {
					return servers.get(index);
				}
			});

			in.close();

			out.close();
		} catch (IOException e) {

		}

	}
	
    public void addText(String str) {
        textPane.setText(textPane.getText() + str);
        textPane.select(textPane.getText().length(), textPane.getText().length());
    }
}
