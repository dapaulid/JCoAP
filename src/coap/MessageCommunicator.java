package coap;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/*
 * This class describes the functionality of a CoAP message layer. It provides:
 * 
 * - Reliable transport of Confirmable messages over the underlying UDP layer
 *   by making use of retransmissions and exponential backoff
 *   
 * - Matching of Confirmables to their corresponding Acknowledgement/Reset
 *   
 * - Detection and cancellation of duplicate messages 
 * 
 * - Retransmission of Acknowledgements/Reset messages upon receiving duplicate
 *   Confirmable messages
 *   
 * These features are transparent to the client of this class.
 *  
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class MessageCommunicator implements MessageReceiver {
	
	// Constants ///////////////////////////////////////////////////////////////
	
	// CoAP Protocol constants as defined in draft-ietf-core-coap-05, section 9
	
	// initial timeout for confirmable messages, used by 
	// the exponential backoff mechanism
	private static final int RESPONSE_TIMEOUT = 2000; // milliseconds
	
	// maximal number of retransmissions before the attempt
	// to transmit a message is canceled
	private static final int MAX_RETRANSMIT = 4;

	private static final int MESSAGE_CACHE_SIZE = 100;
	
	// Nested Classes //////////////////////////////////////////////////////////
	
	/*
	 * Entity class to keep state of retransmissions
	 */
	private static class TxContext {
		Message msg;
		RetransmitTask retransmitTask;
		int numRetransmit;
	}
	
	/*
	 * Utility class used for duplicate detection and reply retransmissions
	 */
	private static class MessageCache extends LinkedHashMap<String, Message> {
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Message> eldest) {
			return size() > MESSAGE_CACHE_SIZE;
		}
		
	}
	
	/*
	 * Utility class used to notify the Communicator class 
	 * about timed-out replies
	 */
	private class RetransmitTask extends TimerTask {

		RetransmitTask(TxContext ctx) {
			this.context = ctx;
		}
		
		@Override
		public void run() {
			handleResponseTimeout(context);
		}
		
		private TxContext context;
	}
	
	// Constructors ////////////////////////////////////////////////////////////
	
	public MessageCommunicator(MessageReceiver receiver, int port) 
		throws SocketException
	{
		// initialize members
		this.receiver = receiver;
		this.comm = new UDPCommunicator(this, port);
		
		// TODO Randomize initial message ID?
		this.messageID = 1;
	}
	
	public MessageCommunicator(MessageReceiver receiver) 
		throws SocketException
	{
		this(receiver, 0);
	}
	
	// Procedures //////////////////////////////////////////////////////////////
	
	public void sendMessage(Message msg) throws IOException {
		
		// set message ID
		msg.setID(nextMessageID());
		
		// send message over unreliable channel
		comm.sendMessage(msg);
		
		// check if message needs confirmation, i.e. a reply is expected
		if (msg.isConfirmable()) {
			
			// create new transmission context
			// to keep track of the Confirmable
			TxContext ctx = addTransmission(msg);
			
			// schedule first retransmission
			scheduleRetransmission(ctx);
			
		} else if (msg.isReply()) {
			
			replyCache.put(msg.key(), msg);
		}
	}

	// Internal ////////////////////////////////////////////////////////////////

	@Override
	public void receiveMessage(Message msg) {
		
		// check for duplicate
		if (dupCache.containsKey(msg.key())) {
		
			// check for retransmitted Confirmable
			if (msg.isConfirmable()) {
				
				// retrieve cached reply
				Message reply = replyCache.get(msg.key());
				if (reply != null) {
					
					// retransmit reply
					try {
						comm.sendMessage(reply);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} else {
					// TODO handling of the case
					// where reply is not in cache
					return;
				}
				
			} else {
				// silently ignore messages of any other type
				return;
			}
			
		} else {
			
			// cache received message
			dupCache.put(msg.key(), msg);
		}
		
		
		// check for reply to Confirmable
		if (msg.isReply()) {
			
			// retrieve context to the incoming message
			TxContext ctx = getTransmission(msg);
			
			if (ctx != null) {
				
				// reply matches to a sent Confirmable
				assert Message.matchBuddies(ctx.msg, msg);
				
				// transmission completed
				removeTransmission(ctx);
				
			} else {
				// TODO Handling of unexpected replies
			}
		}
		
		if (receiver != null) {
			// notify receiver
			receiver.receiveMessage(msg);
		}
	}
	
	private void handleResponseTimeout(TxContext ctx) {
		
		// check if limit of retransmissions reached
		if (ctx.numRetransmit < MAX_RETRANSMIT) {
			
			// retransmit message
			try {

				comm.sendMessage(ctx.msg);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				removeTransmission(ctx);
				return;
			}
			
			// schedule next retransmission
			++ctx.numRetransmit;
			scheduleRetransmission(ctx);
			
		} else {
			
			// cancel transmission
			removeTransmission(ctx);
		}
	}
	
	private synchronized TxContext addTransmission(Message msg) {
		
		if (msg != null) {
		
			// initialize new transmission context
			TxContext ctx = new TxContext();
			ctx.msg            = msg;
			ctx.numRetransmit  = 0;
			ctx.retransmitTask = null;
			
			// add context to context table
			txTable.put(msg.key(), ctx);
			
			return ctx;
		}
		
		return null;
	}
	
	private synchronized TxContext getTransmission(Message msg) {

		// retrieve context from context table		
		return msg != null ? txTable.get(msg.key()) : null;
	}
	
	private synchronized void removeTransmission(TxContext ctx) {

		if (ctx != null) {
		
			// cancel any pending retransmission schedule
			ctx.retransmitTask.cancel();
			ctx.retransmitTask = null;
	
			// remove context from context table
			txTable.remove(ctx.msg.key());

		}
	}
	
	private void scheduleRetransmission(TxContext ctx) {

		// cancel existing schedule (if any)
		ctx.retransmitTask.cancel();
		
		// create new retransmission task
		ctx.retransmitTask = this.new RetransmitTask(ctx);
		
		// calculate timeout by exponential backoff:
		// timeout = RESPONSE_TIMEOUT * 2^numRetransmit
		int timeout = RESPONSE_TIMEOUT << ctx.numRetransmit;
		
		// schedule retransmission task
		timer.schedule(ctx.retransmitTask, timeout);
	}
	
	/*
	 * Returns the next message ID to use out of a consecutive range
	 * 
	 * @return The message ID
	 */
	private int nextMessageID() {
		
		int ID = messageID;
		
		++messageID;
		
		// check for wrap-around
		if (messageID > Message.MAX_ID) {
			messageID = 1;
		}
		
		return ID;
	}
	
	private MessageReceiver receiver;
	
	private UDPCommunicator comm;
	
	private Timer timer
		= new Timer();
	
	private Map<String, TxContext> txTable
		= new HashMap<String, TxContext>();
	
	private MessageCache dupCache
		= new MessageCache();
	
	private MessageCache replyCache
		= new MessageCache();
	
	private int messageID;
	
}
