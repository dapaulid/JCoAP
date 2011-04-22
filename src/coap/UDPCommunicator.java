package coap;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/*
 * This class describes the functionality of an UDP layer that is able
 * to exchange CoAP messages.
 * 
 * Messages are exchanged over an unreliable channel and thus may
 * arrive out of order, appear duplicated, or go missing without notice.
 * 
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class UDPCommunicator implements Runnable {
	
	// CoAP specific definitions ///////////////////////////////////////////////
	
	// default CoAP UDP port
	//private static final int DEFAULT_PORT      = 61616;
	
	// default CoAP port as defined in draft-ietf-core-coap-05, section 7.1:
	// MUST be supported by a server for resource discovery and 
	// SHOULD be supported for providing access to other resources. 
	private static final int IANA_TBD_PORT     = 5683;
	
	// buffer size for incoming datagrams
	// TODO find correct value
	private static final int RX_BUFFER_SIZE    = 1024;
	
	// Constructors ////////////////////////////////////////////////////////////
	
	/*
	 * Constructor for a new UDP Communicator
	 * 
	 * @param receiver The subscriber being notified about received messages
	 * @param port The local UDP port to listen for incoming messages
	 */	
	public UDPCommunicator(MessageReceiver receiver, int port)
		throws SocketException
	{
		// initialize members
		this.receiver = receiver;
		this.socket = new DatagramSocket(port);
		this.listenerThread = new Thread(this);
		
		// stop listener thread when application is required to terminate
		this.listenerThread.setDaemon(true);
		
		// start listening right from the beginning
		this.listenerThread.start();
		
	}

	/*
	 * Constructor for a new UDP Communicator
	 * 
	 * @param receiver The subscriber being notified about received messages
	 */	
	public UDPCommunicator(MessageReceiver receiver) 
		throws SocketException
	{
		this(receiver, 0); // use any available port on the local host machine
		// TODO use -1 instead of 0?
	}

	// Procedures //////////////////////////////////////////////////////////////
	
	/*
	 * Sends a message to the destination given by its URI
	 * 
	 * @param msg The message to be sent
	 */
	public void sendMessage(Message msg) throws IOException {
		
		// assemble datagram components:
		
		// retrieve payload
		byte[] payload = msg.toByteArray();
		
		// retrieve remote address
		// throws UnknownHostException, subclass of IOException
		InetAddress address = InetAddress.getByName(msg.getURI().getHost());
		
		// retrieve remote port
		int port = msg.getURI().getPort();
		if (port < 0) port = IANA_TBD_PORT;
		
		// create datagram		
		DatagramPacket datagram = 
			new DatagramPacket(payload, payload.length, address, port);
		
		// send it over the UDP socket
		socket.send(datagram);
		
		System.out.printf("[%s] Datagram sent, size = %d\n", 
			getClass().getName(), datagram.getLength());
	}

	// Implementation //////////////////////////////////////////////////////////
	
	/*
	 * Implements the handler for incoming messages
	 */
	@Override
	public void run() {
		
		// always listen for incoming datagrams
		while (true) {
			
			// allocate buffer
			byte[] buffer = new byte[RX_BUFFER_SIZE];
			
			// initialize new datagram
			DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
			
			// receive datagram
			try {
			
				socket.receive(datagram);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			System.out.printf("[%s] Datagram received, size = %d\n", 
				getClass().getName(), datagram.getLength());

			// extract message data from datagram
			byte[] data = Arrays.copyOfRange(datagram.getData(), 
					datagram.getOffset(), datagram.getLength()); 
			
			// create new message from the received data
			Message msg = new Message(data);
			
			// assemble URI components from datagram
			
			String 	scheme 		= "coap";
			String 	userInfo 	= null;
			String 	host 		= datagram.getAddress().getHostName();
			int 	port 		= datagram.getPort();
			String 	path 		= null;
			String 	query 		= null;
			String 	fragment 	= null;
			
			// set message URI to sender URI
			try {
				msg.setURI(new URI(scheme, userInfo, host, port, path, query, fragment));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// notify receiver
			if (receiver != null) {
				receiver.receiveMessage(msg);
			}

		}
		
	}
	
	// Functions ///////////////////////////////////////////////////////////////
	
	public int getPort() {
		return socket.getLocalPort();
	}
	
	// Attributes //////////////////////////////////////////////////////////////
	
	private MessageReceiver receiver;
	private DatagramSocket socket;
	private Thread listenerThread;
}

