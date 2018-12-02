import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.AbstractListModel;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledEditorKit;

import org.apache.commons.io.FileUtils;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
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
import java.nio.file.Path;
import java.nio.file.Paths;
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
	private String serverIP = "127.0.0.1";
	JEditorPane textPane;
	boolean inRoom = false;
    Thread starter;
    
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
    
    //--------------------------//
    
    public void userAdd(String data) 
    {
         users.add(data);
    }
    
    //--------------------------//
    
    public void userRemove(String data) 
    {
         addText(data + " is now offline.\n");
    }
    
    //--------------------------//
    
    public void writeUsers() 
    {
         String[] tempList = new String[(users.size())];
         users.toArray(tempList);
         for (String token:tempList) 
         {
             //users.append(token + "\n");
         }
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
                     else if (data[2].equals(connect))
                     {
                        userAdd(data[0]);
                     } 
                     else if (data[2].equals(disconnect)) 
                     {
                        userRemove(data[0]);
                     } 
                     else if (data[2].equals(done)) 
                     {
                        writeUsers();
                        users.clear();
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
		setBounds(100, 100, 600, 420);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				try {
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

		try {

			// Cria arquivos com os usuários

			File file = new File("M:/users.txt");
			boolean roomExists = false;

			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);

			usrName = "username" + Math.abs(new Random().nextInt());

			out.println(usrName);

			BufferedReader in = new BufferedReader(new FileReader(file));

			out.close();

		} catch (IOException el) {

		}

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Comandos", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.setBounds(4, 239, 250, 138);
		contentPane.add(panel_1);
		panel_1.setLayout(null);

		JTextPane txtpnCommandsListList = new JTextPane();
		txtpnCommandsListList.setBounds(6, 16, 238, 115);
		panel_1.add(txtpnCommandsListList);
		txtpnCommandsListList.setEditable(false);
		txtpnCommandsListList.setText("  list              List the names of all rooms.\r\n  nickname <name>   Gives a name for the user.\r\n  join <room name>  Joins a room.\r\n  \\                 Leaves a room (only command\r\n                    possible inside a room).");
		btnSend.setBounds(498, 255, 76, 115);
		contentPane.add(btnSend);


		textArea = new JTextArea();
		textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				String txt = textArea.getText();

				if(e.getKeyCode() != KeyEvent.VK_BACK_SPACE){
					e.consume();
				}

				if(!txt.isEmpty()){
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						
						boolean userRoom = false;
						String strState = "Chat";
						
						if(!userRoom) {
							if(txt.startsWith("join <")) {
								
								try {
									File file = new File("M:/servers.txt");
									boolean roomExists = false;

									FileWriter fw = new FileWriter(file, true);
									BufferedWriter bw = new BufferedWriter(fw);
									PrintWriter out = new PrintWriter(bw);

									String usrText = txt.substring(6, txt.lastIndexOf('>'));

									BufferedReader in = new BufferedReader(new FileReader(file));
									String line;
									ArrayList<String> servers = new ArrayList<>();

									while((line = in.readLine()) != null)
									{
										if(line.equals(usrText)) {
											roomExists = true;
										}

										servers.add(line);
									}
									
									textPane.setText(textPane.getText() + "Você entrou na sala: " + usrText + "\n");
									
							        inRoom = true;
									
									if(!roomExists) {
										out.println(usrText);
										servers.add(usrText);
									} 
									
									in.close();
									out.close();
									
									/*
									 * ALOCAR USUÁRIO A UM SERVIDOR
									 */
									
									File file1 = new File("M:/users.txt");

									FileWriter fw1 = new FileWriter(file1, true);
									BufferedWriter bw1 = new BufferedWriter(fw1);
									PrintWriter out1 = new PrintWriter(bw1);

									BufferedReader in1 = new BufferedReader(new FileReader(file1));
									String line1;
									int lineCounter1 = 0;

									while((line1 = in1.readLine()) != null)
									{
										lineCounter1++;
										if(usrName.equals(line1)) {
											break;
										}
									}

									List<String> lines = Files.readAllLines(file1.toPath(), StandardCharsets.UTF_8);
									lines.set(lineCounter1 - 1, usrName + " " + usrText);
									Files.write(file1.toPath(), lines, StandardCharsets.UTF_8);

									out1.close();
									in1.close();

								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

						        Thread starter = new Thread(new ServerConnect());
						        starter.start();

							} else if (txt.startsWith("list") && !inRoom) {
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

								} catch (IOException et) {

								}
							} else if (txt.startsWith("nickname <") && !inRoom){
								try {
									File file = new File("M:/users.txt");

									FileWriter fw = new FileWriter(file, true);
									BufferedWriter bw = new BufferedWriter(fw);
									PrintWriter out = new PrintWriter(bw);

									BufferedReader in = new BufferedReader(new FileReader(file));
									String line;
									int lineCounter = 0;

									while((line = in.readLine()) != null)
									{
										lineCounter++;
										if(usrName.equals(line)) {
											break;
										}
									}

									String data =  txt.substring(10, txt.lastIndexOf('>'));

									List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
									lines.set(lineCounter - 1, data);
									Files.write(file.toPath(), lines, StandardCharsets.UTF_8);

									usrName = data;

									out.close();

								} catch (IOException et) {

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
						}

			            try {
			                writer.println(usrName + ":" + txt + ":" + strState);
			                writer.flush(); // flushes the buffer
			             } catch (Exception ex) {
			                addText("Entre em uma sala! \n");
			             }

						textArea.setText("");
					}
				}
			}

		});

		textArea.setBounds(258, 255, 230, 115);
		contentPane.add(textArea);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 564, 217);
		contentPane.add(scrollPane);
		
		textPane = new JEditorPane();
		scrollPane.setViewportView(textPane);
        textPane.setEditable(false);
        textPane.setContentType("text/html");
        textPane.setEditorKit(new StyledEditorKit());
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setResizable(false);
        
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
		} catch (IOException e) {

		}
		
		setVisible(true);
	}

    public class ServerConnect implements Runnable 
    {
        @Override
        public void run() 
        {
            try 
            {
                sock = new Socket(InetAddress.getByName(serverIP), 6789);
                InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(streamreader);
                writer = new PrintWriter(sock.getOutputStream());
                writer.println(usrName + ":has connected.:Connect");
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
