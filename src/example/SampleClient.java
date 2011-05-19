package example;

/*
 * This class implements a simple CoAP client for testing purposes.
 * 
 * Usage: java -jar SampleClient.jar [-l] METHOD URI [PAYLOAD]
 *   METHOD  : {GET, POST, PUT, DELETE, DISCOVER}
 *   URI     : The URI to the remote endpoint or resource
 *   PAYLOAD : The data to send with the request
 * Options:
 *   -l      : Wait for multiple responses
 * 
 * Examples:
 *   SampleClient DISCOVER coap://localhost
 *   SampleClient POST coap://someServer.org:61616 my data
 * 
 *   
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import coap.*;

public class SampleClient {
	
	private static final String DISCOVERY_RESOURCE = "/.well-known/core";

	private static final int IDX_METHOD  = 0;
	private static final int IDX_URI     = 1;
	private static final int IDX_PAYLOAD = 2;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// initialize parameters
		String method  = null;
		String uri     = null;
		String payload = null;
		boolean loop   = false;

		// input parameters
		
		if (args.length == 0) {
			printInfo();
			return;
		}
	
		int idx = 0;
		for (String arg : args) {
			if (arg.startsWith("-")) {
				if (arg.equals("-l")) {
					loop = true;
				} else {
					System.out.printf("Unrecognized option: %s\n", arg);
				}
			} else {
				switch (idx) {
				case IDX_METHOD:	
					method = arg.toUpperCase();
					break;
				case IDX_URI:	
					uri = arg;
					break;
				case IDX_PAYLOAD: 
					payload = arg;
					break;
				default:
					System.out.printf("Unexpected argument: %s\n", arg);
				}
				++idx;
			}
		}
			
		// create request 
		
		if (method == null) {
			System.err.printf("Method not specified\n");
			return;
		}
		Request request = newRequest(method);
		if (request == null) {
			System.err.printf("Unknown method: %s\n", method);
			return;
		}
		if (uri == null) {
			System.err.printf("URI not specified\n");
		}
		if (method.equals("DISCOVER") && !uri.endsWith(DISCOVERY_RESOURCE)) {
			uri = uri + DISCOVERY_RESOURCE;
		}
		try {
			request.setURI(new URI(uri));
		} catch (URISyntaxException e) {
			System.err.printf("Failed to parse URI: %s\n", e.getMessage());
			return;
		}
		request.setPayload(payload);
		
		// execute request
		
		request.enableResponseQueue(true);
		try {
			request.execute();
		} catch (IOException e) {
			System.err.printf("Failed to execute request: %s\n", e.getMessage());
			return;
		}

		do {
		
			// receive response
			
			System.out.println("Receiving response...");
			Response response = null;
			try {
				response = request.receiveResponse();
				
				// check for indirect response
				if (response != null && response.isEmptyACK()) {
					System.out.println("Request acknowledged, waiting for separate response...");
					
					response = request.receiveResponse();
				}
				
			} catch (InterruptedException e) {
				System.err.printf("Failed to receive response: %s\n", e.getMessage());
				return;
			}
	
			// output response
			
			if (response != null) {
				
				response.log();
				System.out.printf("Round Trip Time: %d ms\n", response.getRTT());
				
				if (response.hasFormat(MediaTypeRegistry.LINK_FORMAT)) {
					String linkFormat = response.getPayloadString();
					Resource root = RemoteResource.newRoot(linkFormat);
					if (root != null) {
						System.out.println("\nDiscovered resources:");
						root.log();
					} else {
						System.err.printf("Failed to parse link format\n");
					}
				} else {
					if (method.equals("DISCOVER")) {
						System.out.println("Server error: Link format not specified");
					}
				}
				
			} else {
				System.out.println("No response received.");
				break;
			}
			
		} while (loop);
		
		System.out.println();
	}
	
	public static void printInfo() {
		System.out.println("Californium Java CoAP Sample Client");
		System.out.println();
		System.out.println("Usage: SampleClient [-l] METHOD URI [PAYLOAD]");
		System.out.println("  METHOD  : {GET, POST, PUT, DELETE, DISCOVER}");
		System.out.println("  URI     : The URI to the remote endpoint or resource");
		System.out.println("  PAYLOAD : The data to send with the request");
		System.out.println("Options:");
		System.out.println("  -l      : Wait for multiple responses");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("  SampleClient DISCOVER coap://localhost");
		System.out.println("  SampleClient POST coap://someServer.org:61616 my data");
	}

	private static Request newRequest(String method) {
		if (method.equals("GET")) {
			return new GETRequest();
		} else if (method.equals("POST")) {
			return new POSTRequest();
		} else if (method.equals("PUT")) {
			return new PUTRequest();
		} else if (method.equals("DELETE")) {
			return new DELETERequest();
		} else if (method.equals("DISCOVER")){
			return new GETRequest();
		} else {
			return null;
		}
	}

}
