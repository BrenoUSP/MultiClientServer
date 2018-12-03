import java.awt.EventQueue;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
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
import java.util.Random;
import java.util.stream.Collectors;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ChatClient extends JFrame {
	JTextArea textArea;
    BufferedReader reader;
    Socket sock;
    PrintWriter client;
    PrintWriter writer;
	private JPanel contentPane;
	String usrName;
	String message = "";
    ArrayList<String> users = new ArrayList();
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private Socket connection;
	private String serverIP = "localhost";
	private int port = 6789;
	JEditorPane textPane;
	boolean inRoom = false;
    Thread starter;
    String strState, txt;
    boolean isConnected = false;
    
	/**
	 * Launch the application.
	 */
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
    
	  
    public void ListenThread() 
    {
         Thread IncomingReader = new Thread(new IncomingReader());
         IncomingReader.start();
    }
    
 
	
    public class IncomingReader implements Runnable
    {
        @Override
        public void run() 
        {
            String[] data;
            String stream, done = "Done", connect = "Connect", disconnect = "Disconnect", chat = "Chat";

            try 
            {
                while ((stream = reader.readLine()) != null) 
                {
                     data = stream.split(":");

                     if (data[2].equals(chat)) 
                     {
                        addText(data[0] + ": " + data[1] + "\n");
                        //ta_chat.setCaretPosition(ta_chat.getDocument().getLength());
                     } else if (data[2].equals(connect)) {
                    	 //addText(data[0] + " enters the room."+ "\n");
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
				try {

					if (inRoom) {

						sock.close();

						String strState = "Disconnect";
						inRoom = false;


						writer.println(usrName + ":" + " :" + strState);
						writer.flush(); // flushes the buffer

						textArea.setText("");
					}
					
					File file = new File("M:/users.txt");

					List<String> lines = FileUtils.readLines(file);
					List<String> updatedLines = lines.stream().filter(s -> !s.contains(usrName)).collect(Collectors.toList());
					FileUtils.writeLines(file, updatedLines, false);

				} catch (IOException et) {

				}
			}
		});

		JButton btnSend = new JButton("Enviar");
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
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Comandos", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
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
		textArea.setEnabled(false);
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
		scrollPane.setBounds(10, 11, 614, 219);
		contentPane.add(scrollPane);
		
		textPane = new JEditorPane();
		scrollPane.setViewportView(textPane);
        textPane.setEditable(false);
        textPane.setContentType("text/html");
        textPane.setEditorKit(new StyledEditorKit());
        
        JButton btnNewButton = new JButton("Conectar");
        btnNewButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if(!isConnected) {
        	        Thread starter = new Thread(new ServerConnect());
        	        starter.start();
        	        addText("You entered the Server! You must enter in a room! \n");
        	        isConnected = true;
        	        textArea.setEnabled(true);
        		} else {
        	        addText("You are already connected to the server!\n");
        		}
        	}
        });
        btnNewButton.setBounds(526, 255, 98, 54);
        contentPane.add(btnNewButton);
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
                sock = new Socket(serverIP, port);
                InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(streamreader);
                writer = new PrintWriter(sock.getOutputStream());
                writer.println(usrName + ":has connected.:Connect");
                writer.flush(); 

            } 
            catch (Exception ex) 
            {

            }
            
            ListenThread();
        }
    }

	public void addText(String str) {
		textPane.setText(textPane.getText() + str);
        textPane.select(textPane.getText().length(), textPane.getText().length());
	}
}
