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
	private int port = 2222;
	JEditorPane textPane;
	boolean inRoom = false;
    Thread starter;
    String strState, txt;
    
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
		btnSend.setBounds(526, 255, 95, 115);
		contentPane.add(btnSend);


		textArea = new JTextArea();
		textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				txt = textArea.getText();

				if(e.getKeyCode() != KeyEvent.VK_BACK_SPACE){
					e.consume();
				}

				if(!txt.isEmpty()){
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						
						strState = "Chat";
						
						/*
						 * TRABALHAR POSSIVELMENTE COM SALAS EM TEXTO
						 */
						
						if(!inRoom) {
							if(txt.startsWith("join <")) {
								String usrText = txt.substring(6, txt.lastIndexOf('>'));
									
								textPane.setText(textPane.getText() + "Você entrou na sala: " + usrText + "\n");

						        strState = "Connect";
								
						        Thread starter = new Thread(new ServerConnect());
						        starter.start();

						        inRoom = true;
							} /*else if (txt.startsWith("list")) {
								try {
									File file = new File("M:/servers.txt");

									FileWriter fw = new FileWriter(file, true);
									BufferedWriter bw = new BufferedWriter(fw);
									PrintWriter out = new PrintWriter(bw);

									BufferedReader in = new BufferedReader(new FileReader(file));
									String line;

									textPane.setText(textPane.getText() + "\nTemos as seguintes salas disponíveis: \n");

									while((line = in.readLine()) != null)
									{
										textPane.setText(textPane.getText() + line + "\n");
									}

									in.close();
									out.close();
									fw.close();
									bw.close();
									
								} catch (IOException et) {

								}
							}*/ else if (txt.startsWith("nickname <") && txt.endsWith(">")){
								String data =  txt.substring(10, txt.lastIndexOf('>'));
								usrName = data;
							} 
						} else if (txt.startsWith("\\") && inRoom) {
							// SAIR DO SERVIDOR E SALA
							try {
								sock.close();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							strState = "Disconnect";
							inRoom = false;		
						}

						if(strState != "Connect") {
			            try {
			                writer.println(usrName + ":" + txt + ":" + strState);
			                writer.flush(); // flushes the buffer
			             } catch (Exception ex) {
			                //addText("Entre em uma sala! \n");
			             }
						}
						textArea.setText("");
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
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setResizable(false);
        /*
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


			in.close();
			out.close();
			fw.close();
			bw.close();
		} catch (IOException e) {

		}
		*/
		setVisible(true);
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
                writer.println(usrName + ":" + txt + ":Connect");
                writer.flush(); 
            } 
            catch (Exception ex) 
            {
                addText("Cannot Connect! Try Again. \n");
                //tf_username.setEditable(true);
            }
            
            ListenThread();
        }
    }

	public void addText(String str) {
		textPane.setText(textPane.getText() + str);
        textPane.select(textPane.getText().length(), textPane.getText().length());
	}


}
