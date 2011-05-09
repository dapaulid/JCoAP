package example;

import coap.GETRequest;
import coap.PayloadInputStream;
import coap.Request;
import coap.Resources;
import coap.Response;

/*
 * This class implements a simple CoAP client for testing purposes.
 * 
 * Currently, it just performs resource discovery on the test server.
 *  
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class TestClient {

	//TODO: MUST USE UTF-8 for link format
	//private static final String linkFormatEncoding = "UTF-8";
	private static final String linkFormatEncoding = "ISO-8859-1";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// create a new GET request
		Request request = new GETRequest();
		
		// perform request on the libcoap test server
		request.setURI("coap://vs0.inf.ethz.ch:61616/.well-known/core");
		
		// enable response queue in order to perform 
		// blocking receiveResponse() calls
		request.enableResponseQueue(true);
		
		
		try {
			
			// execute the request on the remote endpoint
			request.execute();
			
			// block until response to the request is received
			Response response = request.receiveResponse();
			
			// output the response
			System.out.println("Response received:");
			response.log();
			
			// create stream to read payload
			PayloadInputStream in = new PayloadInputStream(response);
			
			// read payload containing link format into string
			String linkFormat = in.readString(linkFormatEncoding);
			
			// create resources by parsing the link format
			Resources resources = Resources.fromLinkFormat(linkFormat);
			
			// output the resources
			System.out.println("Resources discovered:");
			resources.log();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
