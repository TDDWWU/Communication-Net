import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Class for packet content that represents file information
 *
 */
public class FileInfoContent extends PacketContent {

	String header;
	int packet_size;
	byte[] packet;


	/**
	 * Constructor that takes in information about a file.
	 * 
	 * @param header Initial header.
	 * @param packet_size     Size of packet you send.
	 */

	FileInfoContent(String header,int packet_size, byte[] packet) {
		type = FILEINFO;
		this.header = header;
		this.packet_size=packet_size;
		this.packet = packet; 
		
	}
   
	/**
	 * Constructs an object out of a datagram packet.
	 *
     */
	protected FileInfoContent(ObjectInputStream oin) {
		try {
			type = FILEINFO;
			header = oin.readUTF();
			packet_size = oin.readInt();
			packet = new byte[packet_size];
			 oin.readFully(packet);
     
		
			
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Writes the content into an ObjectOutputStream
	 *
	 */
	protected void toObjectOutputStream(ObjectOutputStream oout) {
		try {
			oout.writeUTF(header);
			oout.writeInt(packet_size);
			oout.write(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the content of the packet as String.
	 *
	 * @return Returns the content of the packet as String.
	 */
	public String toString() {
		return "Filename: " + header + "recived" ;
	}

	/**
	 * Returns the file name contained in the packet.
	 *
	 * @return Returns the file name contained in the packet.
	 */
	public String getHeader() {
		return header;
	}
    public int getpacketsize(){
		
		return packet_size;
	}
	public byte [] getpacket(){

		return packet;
	}
	/**
	 * Returns the file size contained in the packet.
	 *
	 * @return Returns the file size contained in the packet.
	 */

	/**
	 * add the userID method, and now we can add headee in packet
	 * 
	 */


}

