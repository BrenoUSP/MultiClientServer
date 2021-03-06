import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
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
import javax.swing.JOptionPane;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;
import java.awt.Label;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Essa classe consiste na constru��o de um servidor que receba conex�o de um ou mais Clients.
 * A conex�o se d� usando de Sockets. O Server seu pr�prio Socket, que aceita a conex�o do Socket
 * dos Clients.
 * 
 * @author  Breno L�vio Silva de Almeida, Daniel Eiji Martins Chiyo, Gabriel Couto Tabak e Lucas Albano de Oliveira
 * @version 1.0
 * @since   09-12-2018
 */

public class ChatServer extends JFrame {
	private JPanel contentPane, panel;
	private PrintWriter client;
	private ArrayList<String> rooms = new ArrayList<>();
	private HashMap<PrintWriter, User> map;
	private JEditorPane textPane;
	private JScrollPane scrollPane;
	private JList list;
	private int port;
	private JTextField textFieldIP;
	private boolean serverStarted = false;

	public class Client implements Runnable	// Implementa Runnable pois iremos trabalhar com uma classe que ser� jogada em Thread
	{
		BufferedReader reader;
		Socket socket;

		public Client(Socket clientSocket, PrintWriter user) {

			/* Vai trabalhar com os sockets individuais do Clients.
			 * Al�m de ter o PrintWriter, para mandar uma mensagem espec�fica para cada client.
			 */

			client = user;
			try 
			{
				socket = clientSocket;
				InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
				reader = new BufferedReader(isReader);
			}
			catch (Exception ex) 
			{
				ex.printStackTrace();
			}

		}

		@Override
		public void run() 
		{
			String message;
			String[] data = new String[3];

			try 
			{
				while ((message = reader.readLine()) != null) // Fica rodando para receber mensagens do Clients
				{
					// Divide nos dados que ser�o trabalhados da mensagem do usu�rio
					data[0] = message.substring(0, message.indexOf(":"));
					data[1] = message.substring(message.indexOf(":") + 1, message.lastIndexOf(":"));
					data[2] = message.substring(message.lastIndexOf(":") + 1, message.length());

					/* Cada mensagem tem um marcador diferente. Uma indica usu�rios conectando, 
					 * disconectando, enviando mensagem para o chat, e simplesmente digitando. 
					 * A partir disso ser�o tratados as mensagem dos Clients de forma diferenciada.
					 */

					if (data[2].equals("Connect")) 
					{
						userAdd(data[0]);
						serverCall((data[0] + ":" + data[1] + ":Chat"));
					} 
					else if (data[2].equals("Disconnect")) 
					{
						userRemove(data[0]);
					} 
					else if (data[2].equals("Chat")) 
					{
						serverCall(message);
					} else if (data[2].equals("Typing")) {
						typing(data[0]);
					}
				} 
			} catch(SocketException sx) { 
				sx.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
				map.remove(client);
			} 
		} 

	}

	public void typing(String usrName) { // O m�todo � usado para mandar para os usu�rios de uma sala que h� outra pessoa digitando uma mensagem
		String usrRoom = "";
		PrintWriter user = null;

		for(PrintWriter writer : map.keySet()){

			if(map.get(writer).getUsername().equals(usrName)) {
				usrRoom = map.get(writer).getRoom();
				user = writer;
				break;
			}

		} 

		for(PrintWriter writer : map.keySet()){

			if(map.get(writer).getRoom().equals(usrRoom) && usrRoom != "" && !user.equals(writer)) {
				writer.println(":" + usrName + ":Typing");
				writer.flush();
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
				ServerSocket serverSocket = new ServerSocket(port); // Inicia o server com um dado socket e uma porta espec�fica
				serverStarted = true;

				while (true) // Fica rodando para reconhecer clients que se conectarem ao servidor
				{
					Socket clientSocket = serverSocket.accept(); 
					PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
					map.put(writer, new User("",""));

					Thread listener = new Thread(new Client(clientSocket, writer));
					listener.start();

					addText("Someone with IP " + clientSocket.getInetAddress().getHostAddress() + " connected. \n");
				}
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(null, "Server not started. Try changing the port.", "Error", 0);
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

	public void userAdd (String usrName) // Coloca usu�rio no HashMap
	{
		map.get(client).setUsername(usrName);
	}

	public void userRemove (String usrName) // Remove usu�rio do HashMap
	{
		for (PrintWriter user : map.keySet()) {
			if(map.get(user).getUsername().equals(usrName)) {
				map.remove(user);
				break;
			}
		}
	}

	public boolean isDuplicate(String usrName) { // V� se tem outras pessoas com nome igual na sala
		for (PrintWriter user : map.keySet()) {
			if(map.get(user).getUsername().equals(usrName)) {
				return true;
			}
		}

		return false;
	}

	public void serverCall(String message) 
	{
		/* Esse � o m�todo mais importante do programa. Ele ir� trabalhar com as mensagens enviadas pelos usu�rios
		 * para poder direcionar a usu�rios espec�ficos. Por exemplo, usu�rio de uma sala X s� pode ter sua
		 * mensagem enviada para usu�rios de sala X. Usu�rio digita o comando nickname, s� pode ser reconhecido
		 * fora de uma sala, como exigido pelo trabalho.
		 */

		String [] data = new String[3];

		data[0] = message.substring(0, message.indexOf(":"));
		data[1] = message.substring(message.indexOf(":") + 1, message.lastIndexOf(":"));
		data[2] = message.substring(message.lastIndexOf(":") + 1, message.length());

		boolean send = true;
		String usrRoom = "";

		if(data[2].equals("Chat")) {
			for(PrintWriter user : map.keySet()) {
				if(map.get(user).getUsername().equals(data[0])){
					if(map.get(user).getRoom().equals("")){ // USUARIO N�O TEM SALA. COMANDOS PARA LER JOIN, NICKNAME E LIST
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
								user.println(":No rooms available.:Chat");
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
										if(map.get(userInRoom).getRoom().equals(room)) {
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
									user.println(":Server already have 10 rooms. All of them contain at least one user.:Chat");
									user.flush();
								} else {
									rooms.remove(roomToDelete);

								}

							} 

							if(!limitRoom) { // ENTRA EM UMA SALA J� QUE N�O TEM PROBLEMAS DE LIMITE DE 10 SALAS
								usrRoom = data[1].substring(6, data[1].lastIndexOf('>'));
								map.get(user).setRoom(usrRoom);;

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


								String users = "";

								for(PrintWriter writer : map.keySet()){

									if(map.get(writer).getRoom().equals(usrRoom) && !user.equals(writer)) {
										users = users.concat((map.get(writer).getUsername()) + "/");
										writer.println(":" + data[0] + " enters room " + usrRoom + ".:Chat");
										writer.flush();
									}

								} 

								users = users.concat(data[0]);

								for(PrintWriter writer : map.keySet()){

									if(map.get(writer).getRoom().equals(usrRoom) && !user.equals(writer)) {
										writer.println(":-" + users + ":User");
										writer.flush();
									}

								} 

								user.println(":You entered the room - " + usrRoom + " -" + users + ":User");
								user.flush();

								send = false;
								break;

							}

						} else if(data[1].startsWith("nickname <") && data[1].endsWith(">") && data[1].length() > 11) {

							String usrName = data[1].substring(10, data[1].indexOf(">"));

							if(!isDuplicate(usrName)) {
								for(PrintWriter writer : map.keySet()){

									if(map.get(writer).getRoom().equals(usrRoom)) {
										writer.println(":Your new name is " + usrName + ".:Chat");
										writer.flush();

										map.get(writer).setUsername(usrName);

										writer.println(":" + usrName + ":Nickname");
										writer.flush();
										break;
									}

								} 
							} else {
								user.println(":" + usrName + " is already used!:Chat");
								user.flush();
							}

						} else if (data[1].startsWith("\\")){

						} else {

							user.println(":You must enter in a room!:Chat");
							user.flush();
						}

						break;
					} else { // USUARIO JA ESTA EM UMA SALA. COMANDOS PARA LER: \
						usrRoom = map.get(user).getRoom();

						if(data[1].startsWith("\\")) {
							send = false;
							String users = "";

							for(PrintWriter writer : map.keySet()){

								if(map.get(writer).getRoom().equals(usrRoom) && !user.equals(writer)) {
									users = users.concat((map.get(writer).getUsername()) + "/");
									writer.println(":" + data[0] + " leaves room " + usrRoom + ".:Chat");
									writer.flush();
								}

							} 

							for(PrintWriter writer : map.keySet()){

								if(map.get(writer).getRoom().equals(usrRoom) && !user.equals(writer)) {
									writer.println(":-" + users + ":User");
									writer.flush();
								}

							} 

							map.get(user).setRoom("");
							user.println(":You left the room. -" + users + ":User");
							user.flush();

							break;
						}

					}
				} else if (data[0].equals("Server")) {
					for(PrintWriter writer : map.keySet()){
						writer.println(message);
						writer.flush();
					} 
				}
			}



			if (send) {
				for(PrintWriter writer : map.keySet()){

					if(map.get(writer).getRoom().equals(usrRoom)) {
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
		setTitle("Server");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 539, 315);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setResizable(false);
		setLocationRelativeTo(null);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(161, 52, 362, 186);
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
		panel.setBounds(0, 0, 151, 261);
		contentPane.add(panel);

		list = new JList();
		list.setBounds(10, 21, 131, 229);
		panel.add(list);

		JButton btnIniciar = new JButton("Start");

		btnIniciar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(!serverStarted) {
					port = Integer.parseInt(JOptionPane.showInputDialog(null, "Input a port the server will be hosted. e.g. 6789", "Server Port", 1));
					Thread starter = new Thread(new ServerStart());
					starter.start();

					addText("Server started!\n");
				} else {
					addText("Server already started!\n");
				}

			}
		});

		btnIniciar.setBounds(294, 252, 89, 23);
		contentPane.add(btnIniciar);

		textFieldIP = new JTextField();
		textFieldIP.setEditable(false);
		textFieldIP.setBounds(314, 11, 124, 20);
		contentPane.add(textFieldIP);
		textFieldIP.setColumns(10);
		try {
			textFieldIP.setText(Inet4Address.getLocalHost().getHostAddress());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}

		Label label = new Label("Server IP");
		label.setBounds(248, 11, 62, 22);
		contentPane.add(label);

	}

	public void addText(String str) {
		textPane.setText(textPane.getText() + str);
		textPane.select(textPane.getText().length(), textPane.getText().length()); // Desce o Scroll da �rea de texto
	}
}
