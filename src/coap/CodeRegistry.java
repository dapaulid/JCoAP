package coap;

/*
 * This class describes the CoAP Code Registry as defined in 
 * draft-ietf-core-coap-05, section 11.1
 * 
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class CodeRegistry {
	
	// Constants ///////////////////////////////////////////////////////////////
	
	public static final int EMPTY_MESSAGE                         = 0;
	
	// Method Codes ////////////////////////////////////////////////////////////
	public static final int METHOD_GET                            = 1;
	public static final int METHOD_POST                           = 2;
	public static final int METHOD_PUT                            = 3;
	public static final int METHOD_DELETE                         = 4;
	
	// Response Codes //////////////////////////////////////////////////////////
	public static final int RESP_CLASS_SUCCESS                    = 2;
	public static final int RESP_CLASS_CLIENT_ERROR               = 4;
	public static final int RESP_CLASS_SERVER_ERROR               = 5;
	
	public static final int RESP_CREATED                          = 65;
	public static final int RESP_DELETED                          = 66;
	public static final int RESP_VALID                            = 67;
	public static final int RESP_CHANGED                          = 68;
	public static final int RESP_CONTENT                          = 69;
	
	public static final int RESP_BAD_REQUEST                      = 128;
	public static final int RESP_UNAUTHORIZED                     = 129;
	public static final int RESP_BAD_OPTION                       = 130;
	public static final int RESP_FORBIDDEN                        = 131;
	public static final int RESP_NOT_FOUND                        = 132;
	public static final int RESP_METHOD_NOT_ALLOWED               = 133;
	
	public static final int RESP_REQUEST_ENTITY_TOO_LARGE         = 141;
	
	public static final int RESP_UNSUPPORTED_MEDIA_TYPE           = 143;
	
	public static final int RESP_INTERNAL_SERVER_ERROR            = 160;
	public static final int RESP_NOT_IMPLEMENTED                  = 161;
	public static final int RESP_BAD_GATEWAY                      = 162;
	public static final int RESP_SERVICE_UNAVAILABLE              = 163;
	public static final int RESP_GATEWAY_TIMEOUT                  = 164;
	public static final int RESP_PROXYING_NOT_SUPPORTED           = 165;
	
	// Static Functions ////////////////////////////////////////////////////////
	
	/*
	 * Checks whether a code indicates a request
	 * 
	 * @param code The code to check
	 * @return True iff the code indicates a request
	 */
	public static boolean isRequest(int code) {
		return (code >= 1) && (code <= 31);
	}
	
	/*
	 * Checks whether a code indicates a response
	 * 
	 * @param code The code to check
	 * @return True iff the code indicates a response
	 */
	public static boolean isResponse(int code) {
		return (code >= 64) && (code <= 191);
	}
	
	/*
	 * Checks whether a code is valid
	 * 
	 * @param code The code to check
	 * @return True iff the code is valid
	 */
	public static boolean isValid(int code) {
		return (code >= 0) && (code <= 255);
	}

	/*
	 * Returns the response class of a code
	 * 
	 * @param code The code to check
	 * @return The response class of the code
	 */	
	public static int responseClass(int code) {
		return (code >> 5) & 0x7;
	}

	/*
	 * Returns a string representation of the code
	 * 
	 * @param code The code to describe
	 * @return A string describing the code
	 */
	public static String toString(int code) {

		switch (code) {
		case EMPTY_MESSAGE:
			return "Empty Message";
		
		case METHOD_GET:    
			return "GET Request";
		case METHOD_POST:   
			return "POST Request";
		case METHOD_PUT:    
			return "PUT Request";
		case METHOD_DELETE: 
			return "DELETE Request";
		
		case RESP_CREATED: 
			return "2.01 Created";
		case RESP_DELETED: 
			return "2.02 Deleted";
		case RESP_VALID: 
			return "2.03 Valid";
		case RESP_CHANGED: 
			return "2.04 Changed";
		case RESP_CONTENT: 
			return "2.05 Content";
		case RESP_BAD_REQUEST: 
			return "4.00 Bad Request";
		case RESP_UNAUTHORIZED: 
			return "4.01 Unauthorized";
		case RESP_BAD_OPTION: 
			return "4.02 Bad Option";
		case RESP_FORBIDDEN: 
			return "4.03 Forbidden";
		case RESP_NOT_FOUND: 
			return "4.04 Not Found";
		case RESP_METHOD_NOT_ALLOWED: 
			return "4.05 Method Not Allowed";
		case RESP_REQUEST_ENTITY_TOO_LARGE: 
			return "4.13 Request Entity Too Large";
		case RESP_UNSUPPORTED_MEDIA_TYPE: 
			return "4.15 Unsupported Media Type";
		case RESP_INTERNAL_SERVER_ERROR: 
			return "5.00 Internal Server Error";
		case RESP_NOT_IMPLEMENTED: 
			return "5.01 Not Implemented";
		case RESP_BAD_GATEWAY: 
			return "5.02 Bad Gateway";
		case RESP_SERVICE_UNAVAILABLE: 
			return "5.03 Service Unavailable";
		case RESP_GATEWAY_TIMEOUT: 
			return "5.04 Gateway Timeout";
		case RESP_PROXYING_NOT_SUPPORTED: 
			return "5.05 Proxying Not Supported";
		}
		
		if (isValid(code)) {
			
			if (isRequest(code)) {
				return String.format("Unknown Request [code %d]", code);
			} else if (isResponse(code)) {
				return String.format("Unknown Response [code %d]", code);
			} else {
				return String.format("Reserved [code %d]", code);
			}
			
		} else {
			return String.format("Invalid Message [code %d]", code);
		}
	}
}
