package coap;

/*
 * This class describes the CoAP Option Number Registry 
 * as defined in draft-ietf-core-coap-05, 
 * sections 11.2 and 5.4.5
 * 
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class OptionNumberRegistry {
	
	// Constants ///////////////////////////////////////////////////////////////
	
	public static final int RESERVED_0          = 0;
	
	public static final int CONTENT_TYPE        = 1;
	public static final int MAX_AGE             = 2;
	public static final int PROXY_URI           = 3;
	public static final int ETAG                = 4;
	public static final int URI_HOST            = 5;
	public static final int LOCATION_PATH       = 6;
	public static final int URI_PORT            = 7;
	public static final int LOCATION_QUERY      = 8;
	public static final int URI_PATH            = 9;
	public static final int TOKEN               = 11;
	public static final int URI_QUERY           = 15;
	
	public static final int FENCEPOST_DIVISOR    = 14;
	
	// Static Functions ////////////////////////////////////////////////////////
	
	/*
	 * Checks whether an option is elective
	 * 
	 * @param optionNumber The option number to check
	 * @return True iff the option is elective
	 */
	public static boolean isElective(int optionNumber) {
		return (optionNumber & 1) == 0;
	}

	/*
	 * Checks whether an option is critical
	 * 
	 * @param optionNumber The option number to check
	 * @return True iff the option is critical
	 */
	public static boolean isCritical(int optionNumber) {
		return (optionNumber & 1) == 1;
	}

	/*
	 * Checks whether an option is a fencepost option
	 * 
	 * @param optionNumber The option number to check
	 * @return True iff the option is a fencepost option
	 */	
	public static boolean isFencepost(int optionNumber) {
		return optionNumber % FENCEPOST_DIVISOR == 0;
	}
	
	/*
	 * Returns the next fencepost option number following
	 * a given option number
	 * 
	 * @param optionNumber The option number
	 * @return The smallest fencepost option number larger than
	 * the given option number
	 */
	public static int nextFencepost(int optionNumber) {
		return (optionNumber / FENCEPOST_DIVISOR + 1) * FENCEPOST_DIVISOR;
	}

	/*
	 * Returns a string representation of the option number
	 * 
	 * @param code The option number to describe
	 * @return A string describing the option number
	 */
	public static String toString(int optionNumber) {
		switch (optionNumber) {
		case RESERVED_0:
			return "Reserved (0)";
		case CONTENT_TYPE:
			return "Content-Type";
		case PROXY_URI:
			return "Proxy-Uri";
		case ETAG:
			return "ETag";
		case URI_HOST:
			return "Uri-Host";
		case LOCATION_PATH:
			return "Location-Path";
		case URI_PORT:
			return "Uri-Port";
		case LOCATION_QUERY:
			return "Location-Query";
		case URI_PATH:
			return "Uri-Path";
		case TOKEN:
			return "Token";
		case URI_QUERY:
			return "Uri-Query";
		}
		
		return String.format("Unknown option [number %d]", optionNumber);
	}
}
