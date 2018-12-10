/**
 * A classe serve para tratar os usu�rios (clients) como objetos, cada um tendo seu pr�prio nome e sala.
 * Eles s�o associados com uma chave, que seria o PrintWriter, em um HashMap.
 * 
 * @author  Breno L�vio Silva de Almeida, Daniel Eiji Martins Chiyo, Gabriel Couto Tabak e Lucas Albano de Oliveira
 * @version 1.0
 * @since   09-12-2018
 */

public class User {
	private String usrName, room;

	User(String usrName, String room){
		this.usrName = usrName;
		this.room = room;
	}

	public String getUsername() {
		return usrName;
	}

	public String getRoom() {
		return room;
	}

	public void setUsername(String usrName) {
		this.usrName = usrName;
	}

	public void setRoom(String room) {
		this.room = room;
	}


}

