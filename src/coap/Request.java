package coap;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * This class describes the functionality of a CoAP Request as
 * a subclass of a CoAP Message. It provides:
 * 
 * - operations to answer a request by a response using respond()
 * 
 * - different ways how to handle incoming responses:
 *     1) by overriding the protected method handleResponse(), e.g. 
 *        using anonymous inner classes
 *     2) by registering a handler using registerResponseHandler()
 *     3) by calling the blocking method receiveResponse()
 * 
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class Request extends Message {
	
	// Constructors ////////////////////////////////////////////////////////////
	
	/*
	 * Constructor for a new CoAP message
	 * 
	 * @param code The method code of the message
	 * @param confirmable True if the request is to be sent as a Confirmable
	 */	
	public Request(int code, boolean confirmable) {
		super(confirmable ? 
			messageType.Confirmable : messageType.Non_Confirmable, 
			code);
	}
	
	// Methods /////////////////////////////////////////////////////////////////
	
	/*
	 * Executes the request on the endpoint specified by the URI
	 * 
	 */
	public void execute() throws IOException {
		
		Communicator comm = defaultCommunicator();
		if (comm != null) {
			comm.sendMessage(this);
		}
	}
	
	/*
	 * Places a new response to this request, e.g. to answer it
	 * 
	 * @param response A response to this request
	 */
	public void respond(Response response) {
		
		// assign response to this request
		response.setRequest(this);
		
		// enqueue response
		if (responseQueueEnabled()) {
			if (!responseQueue.offer(response)) {
				System.out.println("ERROR: Failed to enqueue response to request");
			}
		}

		// call handler method
		handleResponse(response);		
		
		// notify response handlers
		if (responseHandlers != null) {
			for (ResponseHandler handler : responseHandlers) {
				handler.handleResponse(response);
			}
		}
	}
	
	/*
	 * Returns a response that was placed using respond() and
	 * blocks until such a response is available.
	 * 
	 * NOTE: In order to safely use this method, the call useResponseQueue(true)
	 * is required BEFORE any possible respond() calls take place
	 * 
	 * @return The next response that was placed using respond()
	 */
	public Response receiveResponse() throws InterruptedException {
		
		// response queue required to perform this operation
		if (!responseQueueEnabled()) {
			System.out.println("WARNING: Missing useResponseQueue(true) call, responses may be lost");
			enableResponseQueue(true);
		}
		
		return responseQueue.take();
	}

	/*
	 * Registers a handler for responses to this request
	 * 
	 * @param handler The observer to add to the handler list
	 */
	public void registerResponseHandler(ResponseHandler handler) {

		if (handler != null) {
			
			// lazy creation of response handler list
			if (responseHandlers == null) {
				responseHandlers = new ArrayList<ResponseHandler>();
			}
			
			responseHandlers.add(handler);
		}
	}

	/*
	 * Unregisters a handler for responses to this request
	 * 
	 * @param handler The observer to remove from the handler list
	 */	
	public void unregisterResponseHandler(ResponseHandler handler) {

		if (handler != null && responseHandlers != null) {
			
			responseHandlers.remove(handler);
		}
	}

	/*
	 * Enables or disables the response queue
	 * 
	 * NOTE: The response queue needs to be enabled BEFORE any possible
	 *       calls to receiveResponse()
	 * 
	 * @param enable True to enable and false to disable the response queue,
	 * respectively
	 */
	public void enableResponseQueue(boolean enable) {
		if (enable != responseQueueEnabled()) {
			responseQueue = enable ? new LinkedBlockingQueue<Response>() : null;
		}
	}
	
	/*
	 * Checks if the response queue is enabled
	 * 
	 * NOTE: The response queue needs to be enabled BEFORE any possible
	 *       calls to receiveResponse()
	 * 
	 * @return True iff the response queue is enabled
	 */	
	public boolean responseQueueEnabled() {
		return responseQueue != null;
	}	
	
	// Subclassing /////////////////////////////////////////////////////////////
	
	/*
	 * This method is called whenever a response was placed to this request.
	 * Subclasses can override this method in order to handle responses.
	 * 
	 * @param response The response to handle
	 */
	protected void handleResponse(Response response) {
		// Do nothing
	}
	
	/*
	 * Direct subclasses need to override this method in order to invoke
	 * the according method of the provided RequestHandler (visitor pattern)
	 * 
	 * @param handler A handler for this request
	 */
	protected void dispatch(RequestHandler handler) {
		System.out.printf("Unable to dispatch request with code '%s'", 
			CodeRegistry.toString(getCode()));
	}
	
	// Class functions /////////////////////////////////////////////////////////

	/*
	 * Returns the default communicator used for outgoing requests
	 * 
	 * @return The default communicator
	 */
	public static Communicator defaultCommunicator() throws SocketException {
		
		// lazy initialization
		if (DEFAULT_COMM == null) {
			DEFAULT_COMM = new Communicator();
		}
		return DEFAULT_COMM;
	}
		
	// Class attributes ////////////////////////////////////////////////////////
	
	// the default communicator for request objects (lazy initialized)
	private static Communicator DEFAULT_COMM;
	
	// Attributes //////////////////////////////////////////////////////////////
	
	// list of response handlers that are notified about incoming responses
	List<ResponseHandler> responseHandlers;
	
	// queue used to store responses that will be retrieved using 
	// receiveResponse() 
	BlockingQueue<Response> responseQueue;
}
