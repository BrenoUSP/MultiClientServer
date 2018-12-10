import java.awt.EventQueue;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.Utilities;
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
import java.net.SocketException;
import java.net.URL;
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
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Essa classe consiste na construção de um Client que fará a conexão por Sockets com um servidor.
 * Com o endereço IP e porta de um servidor na rede local, o usuário irá se conectar.
 * 
 * @author  Breno Lívio Silva de Almeida, Daniel Eiji Martins Chiyo, Gabriel Couto Tabak e Lucas Albano de Oliveira
 * @version 1.0
 * @since   09-12-2018
 */


public class ChatClient extends JFrame {
	private JTextArea textArea;
	private BufferedReader reader;
	private Socket socket;
	private PrintWriter writer;
	private JPanel contentPane;
	private String serverIP, usrName, txt;
	private int port;
	private JEditorPane textPane;
	private boolean isConnected = false;
	private JList list;
	private ImageIcon smile, sad, surprised, sunglasses, sorry, blink, happy, tongue;
	private JLabel jLabelType;

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

	public void Listen() // Comando usado para receber mensagens do servidor
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
			String[] data = new String[3];

			try 
			{
				while ((stream = reader.readLine()) != null) 
				{

					// Fica rodando para receber mensagens do servidor

					data[0] = stream.substring(0, stream.indexOf(":"));
					data[1] = stream.substring(stream.indexOf(":") + 1, stream.lastIndexOf(":"));
					data[2] = stream.substring(stream.lastIndexOf(":") + 1, stream.length());

					/* Da mesma forma que o servidor, será necessário tratar mensagens com marcadores.
					 * O servidor poderá redirecioanar aos Clients diferentes tipos de mensagens, que
					 * serão tratadas em seguidas.
					 */

					if (data[2].equals("Chat")) 
					{
						if(data[0].length() > 0) {
							addText(data[0] + ": " + data[1] + "\n");
						} else {
							addText(data[1] + "\n");
						}

					} else if (data[2].equals("Nickname")) {
						usrName = data[1];
					} else if (data[2].equals("Typing")) {
						typing(data[1]);
					} else if (data[2].equals("User")) {
						listUsers(data[1]);
					}
				}
			} catch(SocketException sx) { 
				sx.printStackTrace();
			} catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}

	public void listUsers(String message){ // Lista os usuários em uma sala 
		String users = message.substring(message.lastIndexOf("-") + 1, message.length());

		if(message.contains("left")) {
			String[] usersList = new String[0];

			list.setModel(new AbstractListModel() {
				public int getSize() {
					return usersList.length;
				}
				public Object getElementAt(int index) {
					return usersList[index];
				}
			});
		} else {
			String[] usersList = users.split("/");

			list.setModel(new AbstractListModel() {
				public int getSize() {
					return usersList.length;
				}
				public Object getElementAt(int index) {
					return usersList[index];
				}
			});
		}

		String toSend = message.substring(0, message.lastIndexOf("-"));
		if(!toSend.equals("")) {
			addText(toSend + "\n");	
		}
	}

	public void typing(String usrName) { // Indica no label se existe alguém digitando alguma mensagem
		jLabelType.setText(usrName + " is typing...");
		new java.util.Timer().schedule(
				new java.util.TimerTask() {
					@Override
					public void run() {
						jLabelType.setText("");
					}
				},
				(new Random()).nextInt((3800 - 3500) + 1) + 350
				);
	}

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
		panel_1.setBounds(10, 229, 250, 141);
		contentPane.add(panel_1);
		panel_1.setLayout(null);

		JTextPane txtpnCommandsListList = new JTextPane();
		txtpnCommandsListList.setBounds(6, 16, 238, 115);
		panel_1.add(txtpnCommandsListList);
		txtpnCommandsListList.setEditable(false);
		txtpnCommandsListList.setText("  list              List the names of all rooms.\r\n  nickname <name>   Gives a name for the user.\r\n  join <room name>  Joins a room.\r\n  \\                 Leaves a room (only command\r\n                    possible inside a room).");
		btnSend.setBounds(526, 244, 98, 115);
		contentPane.add(btnSend);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(266, 263, 250, 96);
		contentPane.add(scrollPane_1);

		textArea = new JTextArea();
		scrollPane_1.setViewportView(textArea);
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
					} else {
						writer.println(usrName + "::Typing");
						writer.flush();
					}
				}
			}

		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 453, 166);
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
					serverIP = JOptionPane.showInputDialog(null, "Server IP. e.g. localhost", "Connect", 1);
					port = Integer.parseInt(JOptionPane.showInputDialog(null, "Port", "Connect", 1));

					Thread starter = new Thread(new ServerConnect());
					starter.start();

				} else {
					addText("You are already connected to the server!\n");
				}
			}
		});
		btnConnect.setBounds(418, 188, 98, 42);
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
						usrName = "username" + Math.abs(new Random().nextInt());
						addText("You disconnected from the server!\n");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					addText("You are already disconnected to the server!\n");
				}
			}
		});

		buttonDisconnect.setBounds(526, 188, 98, 42);
		contentPane.add(buttonDisconnect);

		JButton jButton1 = new JButton("");
		jButton1.setBounds(10, 188, 34, 33);
		contentPane.add(jButton1);

		JButton jButton2 = new JButton("");
		jButton2.setBounds(54, 188, 34, 33);
		contentPane.add(jButton2);

		JButton jButton3 = new JButton("");
		jButton3.setBounds(98, 188, 34, 33);
		contentPane.add(jButton3);

		JButton jButton4 = new JButton("");
		jButton4.setBounds(142, 188, 34, 33);
		contentPane.add(jButton4);

		JButton jButton5 = new JButton("");
		jButton5.setBounds(185, 188, 34, 33);
		contentPane.add(jButton5);

		JButton jButton6 = new JButton("");
		jButton6.setBounds(229, 188, 34, 33);
		contentPane.add(jButton6);

		JButton jButton7 = new JButton("");
		jButton7.setBounds(273, 188, 34, 33);
		contentPane.add(jButton7);

		JButton jButton8 = new JButton("");
		jButton8.setBounds(317, 188, 34, 33);
		contentPane.add(jButton8);
		textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		setResizable(false);

		try {
			URL imageUrlF = this.getClass().getResource("/Emojis/smile.png");
			smile = new ImageIcon(imageUrlF);
			URL imageUrlSur = this.getClass().getResource("/Emojis/surprised.png");
			surprised = new ImageIcon(imageUrlSur);
			URL imageUrlSad = this.getClass().getResource("/Emojis/sad.png");
			sad = new ImageIcon(imageUrlSad);
			URL imageUrlSun = this.getClass().getResource("/Emojis/sunglasses.png");
			sunglasses = new ImageIcon(imageUrlSun);
			URL imageUrlSor = this.getClass().getResource("/Emojis/sorry.png");
			sorry = new ImageIcon(imageUrlSor);
			URL imageUrlBli = this.getClass().getResource("/Emojis/blink.png");
			blink = new ImageIcon(imageUrlBli);
			URL imageUrlHap = this.getClass().getResource("/Emojis/grin.png");
			happy = new ImageIcon(imageUrlHap);
			URL imageUrlTon = this.getClass().getResource("/Emojis/tongue.png");
			tongue = new ImageIcon(imageUrlTon);
		} catch (Exception e) {
			e.printStackTrace();
		}

		jButton1.setIcon(smile);
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(isConnected) {
					textArea.setText(textArea.getText() + ":)");
				}
			}
		});

		jButton2.setIcon(sad);
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(isConnected) {
					textArea.setText(textArea.getText() + ":(");
				}
			}
		});

		jButton3.setIcon(blink);
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(isConnected) {
					textArea.setText(textArea.getText() + ";)");
				}

			}
		});

		jButton4.setIcon(sorry);
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(isConnected) {
					textArea.setText(textArea.getText() + ":/");
				}

			}
		});

		jButton5.setIcon(happy);
		jButton5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(isConnected) {
					textArea.setText(textArea.getText() + "=D");
				}

			}
		});

		jButton6.setIcon(sunglasses);
		jButton6.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(isConnected) {
					textArea.setText(textArea.getText() + "8)");
				}

			}
		});

		jButton7.setIcon(surprised);
		jButton7.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(isConnected) {
					textArea.setText(textArea.getText() + ":o");
				}

			}
		});

		jButton8.setIcon(tongue);

		jLabelType = new JLabel("");
		jLabelType.setBounds(283, 238, 233, 14);
		contentPane.add(jLabelType);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Users in room", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(467, 11, 157, 173);
		contentPane.add(panel);
		panel.setLayout(null);

		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(6, 16, 141, 146);
		panel.add(scrollPane_2);

		list = new JList();
		scrollPane_2.setViewportView(list);
		jButton8.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if(isConnected) {
					textArea.setText(textArea.getText() + ":p");
				}

			}
		});

		setVisible(true);

	}

	private void initListener() { 
		/* Esse método é exclusivamente usado para detectar o uso de caractéres que indicam emojis,
    	para dessa forma substituir eles por imagens.*/

		textPane.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent event) {
				final DocumentEvent e = event;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (e.getDocument() instanceof StyledDocument) {
							try {
								StyledDocument doc = (StyledDocument) e.getDocument();
								int start = Utilities.getRowStart(textPane, Math.max(0, e.getOffset() - 1));
								int end = Utilities.getWordStart(textPane, e.getOffset() + e.getLength());
								String text = doc.getText(start, end - start);

								int i = text.indexOf(":)");
				while (i >= 0) {
					final SimpleAttributeSet attrs = new SimpleAttributeSet(
							doc.getCharacterElement(start + i).getAttributes());
					if (StyleConstants.getIcon(attrs) == null) {
						StyleConstants.setIcon(attrs, smile);
						doc.remove(start + i, 2);
						doc.insertString(start + i, ":)", attrs);
					}
					i = text.indexOf(":)", i + 2);
				}

				int o = text.indexOf(":o");
				while (o >= 0) {
					final SimpleAttributeSet attrs = new SimpleAttributeSet(
							doc.getCharacterElement(start + o).getAttributes());
					if (StyleConstants.getIcon(attrs) == null) {
						StyleConstants.setIcon(attrs, surprised);
						doc.remove(start + o, 2);
						doc.insertString(start + o, ":o", attrs);
					}
					o = text.indexOf(":o", o + 2);
				}

				int t = text.indexOf(":(");
				while (t >= 0) {
					final SimpleAttributeSet attrs = new SimpleAttributeSet(
							doc.getCharacterElement(start + t).getAttributes());
					if (StyleConstants.getIcon(attrs) == null) {
						StyleConstants.setIcon(attrs, sad);
						doc.remove(start + t, 2);
						doc.insertString(start + t, ":(", attrs);
					}
					t = text.indexOf(":(", t + 2);
				}

				int c = text.indexOf("8)");
				while (c >= 0) {
					final SimpleAttributeSet attrs = new SimpleAttributeSet(
							doc.getCharacterElement(start + c).getAttributes());
					if (StyleConstants.getIcon(attrs) == null) {
						StyleConstants.setIcon(attrs, sunglasses);
						doc.remove(start + c, 2);
						doc.insertString(start + c, "8)", attrs);
					}
					c = text.indexOf("8)", c + 2);
				}

				int y = text.indexOf(":/");
				while (y >= 0) {
					final SimpleAttributeSet attrs = new SimpleAttributeSet(
							doc.getCharacterElement(start + y).getAttributes());
					if (StyleConstants.getIcon(attrs) == null) {
						StyleConstants.setIcon(attrs, sorry);
						doc.remove(start + y, 2);
						doc.insertString(start + y, ":/", attrs);
					}
					y = text.indexOf(":/", y + 2);
				}

				int p = text.indexOf(";)");
				while (p >= 0) {
					final SimpleAttributeSet attrs = new SimpleAttributeSet(
							doc.getCharacterElement(start + p).getAttributes());
					if (StyleConstants.getIcon(attrs) == null) {
						StyleConstants.setIcon(attrs, blink);
						doc.remove(start + p, 2);
						doc.insertString(start + p, ";)", attrs);
					}
					p = text.indexOf(";)", p + 2);
				}

				int g = text.indexOf("=D");
				while (g >= 0) {
					final SimpleAttributeSet attrs = new SimpleAttributeSet(
							doc.getCharacterElement(start + g).getAttributes());
					if (StyleConstants.getIcon(attrs) == null) {
						StyleConstants.setIcon(attrs, happy);
						doc.remove(start + g, 2);
						doc.insertString(start + g, "=D", attrs);
					}
					g = text.indexOf("=D", g + 2);
				}

				int v = text.indexOf(":p");
				while (v >= 0) {
					final SimpleAttributeSet attrs = new SimpleAttributeSet(
							doc.getCharacterElement(start + v).getAttributes());
					if (StyleConstants.getIcon(attrs) == null) {
						StyleConstants.setIcon(attrs, tongue);
						doc.remove(start + v, 2);
						doc.insertString(start + v, ":p", attrs);
					}
					v = text.indexOf(":p", v + 2);
				}
							} catch (BadLocationException e1) {
								e1.printStackTrace();
							}
						}
					}
				});
			}

			public void removeUpdate(DocumentEvent e) {
			}

			public void changedUpdate(DocumentEvent e) {
			}
		});
	}

	public void sendText() { // Envia mensagem para o servidor

		try {
			writer.println(usrName + ":" + txt + ":" + "Chat");
			writer.flush();
			initListener();
		} catch (Exception ex) {
			ex.printStackTrace();
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
