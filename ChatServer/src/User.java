
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

