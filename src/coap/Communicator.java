package coap;

import java.io.IOException;
import java.net.SocketException;

import layers.TransferLayer;
import layers.UpperLayer;
import layers.MessageLayer;
import layers.TransactionLayer;
import layers.UDPLayer;

public class Communicator extends UpperLayer {

	// Constants ///////////////////////////////////////////////////////////////
	
	public final static int DEFAULT_PORT       = UDPLayer.DEFAULT_PORT;
	public final static String URI_SCHEME_NAME = UDPLayer.URI_SCHEME_NAME;
	
	// Constructors ////////////////////////////////////////////////////////////
	
	/*
	 * Constructor for a new Communicator
	 * 
	 * @param port The local UDP port to listen for incoming messages
	 */	
	public Communicator(int port, boolean daemon) throws SocketException {
		
		// initialize layers
		transferLayer = new TransferLayer();
		transactionLayer = new TransactionLayer();
		messageLayer = new MessageLayer();
		udpLayer = new UDPLayer(port, daemon);
		
		// connect layers
		buildStack();
	}

	/*
	 * Constructor for a new Communicator
	 * 
	 */
	public Communicator() throws SocketException {
		this(0, true);
	}
	
	// Internal ////////////////////////////////////////////////////////////////
	
	/*
	 * This method connects the layers in order to build the communication stack
	 * 
	 * It can be overridden by subclasses in order to add further layers, e.g.
	 * for introducing a layer that drops or duplicates messages by a
	 * probabilistic model in order to evaluate the implementation.
	 */
	protected void buildStack() {
		
		//this.setLowerLayer(transferLayer);
		//transferLayer.setLowerLayer(transactionLayer);
		this.setLowerLayer(transactionLayer);
		transactionLayer.setLowerLayer(messageLayer);
		messageLayer.setLowerLayer(udpLayer);
		
	}
	
	// I/O implementation //////////////////////////////////////////////////////
	
	@Override
	protected void doSendMessage(Message msg) throws IOException {

		// delegate to first layer
		sendMessageOverLowerLayer(msg);
	}	
	
	@Override
	protected void doReceiveMessage(Message msg) {
		
		if (msg instanceof Response) {
			Response response = (Response) msg;
			
			// initiate custom response handling
			response.handle();
			
		} else if (msg instanceof Request) {
			Request request = (Request) msg;
			
			request.setCommunicator(this);
		}	
		
		// pass message to registered receivers
		deliverMessage(msg);
		
	}
	
	// Queries /////////////////////////////////////////////////////////////////
	
	public int port() {
		return udpLayer.getPort();
	}

	// Attributes //////////////////////////////////////////////////////////////
	
	protected TransferLayer transferLayer;
	protected TransactionLayer transactionLayer;
	protected MessageLayer messageLayer;
	protected UDPLayer udpLayer;
	
}
