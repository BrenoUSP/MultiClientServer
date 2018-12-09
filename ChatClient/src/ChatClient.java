import java.awt.EventQueue;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.text.StyledEditorKit;
import org.apache.commons.io.FileUtils;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JOptionPane;

public class ChatClient extends JFrame {
	private JTextArea textArea;
    private BufferedReader reader;
    private Socket socket;
    private PrintWriter writer;
	private JPanel contentPane;
	private String serverIP, usrName, txt;
	private int port;
	private JEditorPane textPane;
	boolean isConnected = false;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatClient frame = new ChatClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
     
    public void Listen() 
    {
         Thread IncomingReader = new Thread(new Reader());
         IncomingReader.start();
    }
    
 
	
    public class Reader implements Runnable
    {
        @Override
        public void run() 
        {
            String stream;
            String[] data;

            try 
            {
                while ((stream = reader.readLine()) != null) 
                {
                     data = stream.split(":");

                     if (data[2].equals("Chat")) 
                     {
                    	if(data[0].length() > 0) {
                            addText(data[0] + ": " + data[1] + "\n");
                    	} else {
                    		addText(data[1] + "\n");
                    	}

                     } else if (data[2].equals("Nickname")) {
                    	 usrName = data[1];
                     } 
                }
           }catch(Exception ex) { }
        }
    }
	/**
	 * Create the frame.
	 */
	public ChatClient() {
		setTitle("Chat");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 420);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		usrName = "username" + Math.abs(new Random().nextInt());
		
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if(isConnected) {
					writer.println(usrName + ":\\:Chat");
					writer.flush(); 

					writer.println("::Disconnect");
					writer.flush();

					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		JButton btnSend = new JButton("Send");
		btnSend.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				txt = textArea.getText();

				if(!txt.isEmpty()){
					sendText();

				}
			}
		});

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Commands", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.setBounds(4, 239, 250, 141);
		contentPane.add(panel_1);
		panel_1.setLayout(null);

		JTextPane txtpnCommandsListList = new JTextPane();
		txtpnCommandsListList.setBounds(6, 16, 238, 115);
		panel_1.add(txtpnCommandsListList);
		txtpnCommandsListList.setEditable(false);
		txtpnCommandsListList.setText("  list              List the names of all rooms.\r\n  nickname <name>   Gives a name for the user.\r\n  join <room name>  Joins a room.\r\n  \\                 Leaves a room (only command\r\n                    possible inside a room).");
		btnSend.setBounds(526, 316, 98, 54);
		contentPane.add(btnSend);


		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				txt = textArea.getText();

				if(e.getKeyCode() != KeyEvent.VK_BACK_SPACE){
					e.consume();
				}

				if(!txt.isEmpty()){
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						sendText();
					}
				}
			}

		});

		textArea.setBounds(258, 255, 258, 115);
		contentPane.add(textArea);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 614, 172);
		contentPane.add(scrollPane);
		
		textPane = new JEditorPane();
		scrollPane.setViewportView(textPane);
        textPane.setEditable(false);
        textPane.setContentType("text/html");
        textPane.setEditorKit(new StyledEditorKit());
        
        JButton btnConnect = new JButton("Connect");
        btnConnect.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if(!isConnected) {
        			serverIP = JOptionPane.showInputDialog(null, "Server IP", "Connect", 1);
        			port = Integer.parseInt(JOptionPane.showInputDialog(null, "Port", "Connect", 1));
        			
        	        Thread starter = new Thread(new ServerConnect());
        	        starter.start();

        		} else {
        	        addText("You are already connected to the server!\n");
        		}
        	}
        });
        btnConnect.setBounds(526, 194, 98, 54);
        contentPane.add(btnConnect);
        
        JButton buttonDisconnect = new JButton("Disconnect");
        buttonDisconnect.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		if(isConnected) {
					writer.println(usrName + ":\\:Chat");
					writer.flush(); 

					writer.println(usrName + "::Disconnect");
					writer.flush();
					
					try {
						socket.close();
						isConnected = false;
						textArea.setEditable(false);
	        	        addText("You disconnected from the server!\n");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
        	}
        });


        buttonDisconnect.setBounds(526, 255, 98, 54);
        contentPane.add(buttonDisconnect);
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setResizable(false);

		setVisible(true);
		
	}

	public void sendText() {

        try {
            writer.println(usrName + ":" + txt + ":" + "Chat");
            writer.flush(); // 
         } catch (Exception ex) {
         }
		textArea.setText("");

	}
	
    public class ServerConnect implements Runnable 
    {
        @Override
        public void run() 
        {

            try 
            {
                socket = new Socket(serverIP, port);
                InputStreamReader streamreader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(streamreader);
                writer = new PrintWriter(socket.getOutputStream());
    	        addText("You entered the Server!\n");
    	        isConnected = true;
    	        textArea.setEditable(true);
                writer.println(usrName + "::Connect");
                writer.flush(); 

            } 
            catch (Exception ex) 
            {
            	JOptionPane.showMessageDialog(null, "Invalid Server!", "Connect", 0);
            }
            
            Listen();
        }
    }

	public void addText(String str) {
		textPane.setText(textPane.getText() + str);
        textPane.select(textPane.getText().length(), textPane.getText().length()); // Desce o Scroll da área de texto
	}
}
