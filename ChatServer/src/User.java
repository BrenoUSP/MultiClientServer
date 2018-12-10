/**
 * A classe serve para tratar os usuários (clients) como objetos, cada um tendo seu próprio nome e sala.
 * Eles são associados com uma chave, que seria o PrintWriter, em um HashMap.
 * 
 * @author  Breno Lívio Silva de Almeida, Daniel Eiji Martins Chiyo, Gabriel Couto Tabak e Lucas Albano de Oliveira
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

