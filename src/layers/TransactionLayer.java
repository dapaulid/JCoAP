package layers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import coap.Message;
import coap.Option;
import coap.OptionNumberRegistry;
import coap.Request;
import coap.Response;

public class TransactionLayer extends UpperLayer {
	
	public TransactionLayer() {
		// member initialization
		// TODO randomize initial token?
		this.currentToken = 0xCAFE;
	}

	// I/O implementation //////////////////////////////////////////////////////
	
	@Override
	protected void doSendMessage(Message msg) throws IOException {
		
		if (msg instanceof Request) {
			Request request = (Request) msg;
			
			// set token option
			if (request.getFirstOption(OptionNumberRegistry.TOKEN) == null) {
				request.setOption(new Option(currentToken, OptionNumberRegistry.TOKEN));
			}
			
			// associate token with request
			tokenMap.put(currentToken, request);
			
			// compute next token
			++currentToken;
		}
		sendMessageOverLowerLayer(msg);
	}	
	
	@Override
	protected void doReceiveMessage(Message msg) {

		// retrieve token option
		Option tokenOpt = msg.getFirstOption(OptionNumberRegistry.TOKEN);
		
		if (msg instanceof Response) {

			Response response = (Response) msg;
			
			Request request = null;
			
			if (tokenOpt != null) {
				
				// retrieve request corresponding to token
				int token = tokenOpt.getIntValue();
				request = tokenMap.get(token);
				
				/*if (request == null) {
					System.out.printf("[%s] WARNING: Unexpected response, Token=0x%x\n",
						getClass().getName(), token);
				}*/
			} else {
				// no token option present (blame server)
				
				System.out.printf("[%s] WARNING: Token missing for matching response to request\n",
					getClass().getName());
				
				// try to use buddy for matching response to request
				if (response.getBuddy() instanceof Request) {
					
					request = (Request)response.getBuddy();

					System.out.printf("[%s] Falling back to buddy matching for %s\n",
						getClass().getName(), response.key());
				}
			}
			
			// check if received response needs confirmation
			if (response.isConfirmable()) {
				try {
					// reply with ACK if response matched to request,
					// otherwise reply with RST
					
					Message reply = response.newReply(request != null);

					sendMessageOverLowerLayer(reply);

				} catch (IOException e) {
					System.out.printf("[%s] ERROR: Failed to reply to confirmable response:\n",
						getClass().getName());
					e.printStackTrace();
				}
			}

			if (request != null) {
				
				// attach request to response
				response.setRequest(request);
			}/* else {
				
				// log unsuccessful matching
				System.out.printf("[%s] ERROR: Failed to match response to request:\n",
					getClass().getName());
				response.log();
			}*/
			
		} else if (msg instanceof Request) {
			
			// incoming request: 
			if (tokenOpt != null) {
				tokenMap.put(tokenOpt.getIntValue(), (Request) msg);
			}
		}

		deliverMessage(msg);
	}
	
	private Map<Integer, Request> tokenMap
		= new HashMap<Integer, Request>();

	private int currentToken;
}
