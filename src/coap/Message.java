package coap;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import option.Option;

/*
 * This class describes the functionality of the CoAP messages
 * 
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class Message {
	
	// CoAP specific definitions ///////////////////////////////////////////////
	
	// number of bits used for the encoding of the CoAP version field
	public static final int VERSION_BITS     = 2;
	
	// number of bits used for the encoding of the message type field
	public static final int TYPE_BITS        = 2;
	
	// number of bits used for the encoding of the option count field
	public static final int OPTIONCOUNT_BITS = 4;
	
	// number of bits used for the encoding of the request method/
	// response code field
	public static final int CODE_BITS        = 8;
	
	// number of bits used for the encoding of the transaction ID
	public static final int ID_BITS         = 16;
	
	// number of bits used for the encoding of the option delta
	public static final int OPTIONDELTA_BITS = 4;
	
	// number of bits used for the encoding of the base option length field
	// if all bits in this field are set to one, the extended option length
	// field is additionally used to encode the option length
	public static final int OPTIONLENGTH_BASE_BITS     = 4;
	
	// number of bits used for the encoding of the extended option length field
	// this field is used when all bits in the base option length field 
	// are set to one
	public static final int OPTIONLENGTH_EXTENDED_BITS = 8;
	
	// Derived constants ///////////////////////////////////////////////////////
	
	public static final int MAX_ID 
		= (1 << ID_BITS)- 1;
	
	// maximum option delta that can be encoded without using fencepost options
	public static final int MAX_OPTIONDELTA
		= (1 << OPTIONDELTA_BITS) - 1;
	
	// maximum option length that can be encoded using 
	// the base option length field only
	public static final int MAX_OPTIONLENGTH_BASE      
		= (1 << OPTIONLENGTH_BASE_BITS) - 2;
	
	// Static Functions ////////////////////////////////////////////////////////
	
	public static Message newAcknowledgement(Message msg) {
		
		Message ack = new Message();
		
		// set message type to Acknowledgement
		ack.setType(messageType.Acknowledgement);
		
		// echo the Message ID
		ack.setID(msg.getID());
		
		// set receiver URI to sender URI of the message
		// to acknowledge
		ack.setURI(msg.getURI());
		
		// create an empty Acknowledgement by default,
		// can be piggy-backed with a response by the user
		ack.setCode(CodeRegistry.EMPTY_MESSAGE);
		
		return ack;
	}
	
	public static Message newReset(Message msg) {
		
		Message rst = new Message();
		
		// set message type to Reset
		rst.setType(messageType.Reset);
		
		// echo the Message ID
		rst.setID(msg.getID());
		
		// set receiver URI to sender URI of the message
		// to reset
		rst.setURI(msg.getURI());
		
		// Reset must be empty
		rst.setCode(CodeRegistry.EMPTY_MESSAGE);
		
		return rst;
	}
	
	/*
	 * Matches two messages to buddies if they have the same message ID
	 * 
	 * @param msg1 The first message
	 * @param msg2 the second message
	 * @return True iif the messages were matched to buddies
	 */
	public static boolean matchBuddies(Message msg1, Message msg2) {
		
		if (
			msg1 != null && msg2 != null &&  // both messages must exist
			msg1 != msg2 &&                  // no message can be its own buddy 
			msg1.getID() == msg2.getID()     // buddy condition: same IDs
		) {
			
			assert msg1.buddy == null;
			assert msg2.buddy == null;
			
			msg1.buddy = msg2;
			msg2.buddy = msg1;
			
			return true;
			
		} else {
			return false;
		}
	}
	
	
	// Constructors ////////////////////////////////////////////////////////////
	/*
	 * Default constructor for a new CoAP message
	 */
	public Message () {
	}
	
	/*
	 * Constructor for a new CoAP message
	 * 
	 * @param uri The URI of the CoAP message
	 * @param payload The payload of the CoAP message
	 */
	public Message(URI uri, messageType type, int code, int id, byte[] payload) {
		this.uri = uri;
		this.payload = payload;
	}
	
	/*
	 * Constructor for a new CoAP message
	 * 
	 * @param byteArray A byte array containing an encoded CoAP message
	 */
	public Message(byte[] byteArray) {
		fromByteArray(byteArray);
	}
	
	// Serialization ///////////////////////////////////////////////////////////

	/*
	 * Encodes the message into its raw binary representation
	 * as specified in draft-ietf-core-coap-05, section 3.1
	 * 
	 * @return A byte array containing the CoAP encoding of the message
	 * 
	 */
	public byte[] toByteArray() {
		
		// create datagram writer to encode options
		DatagramWriter optWriter = new DatagramWriter(); 
		
		int optionCount = 0;
		int lastOptionNumber = 0;
		for (Option opt : getOptionList()) {
			
			// calculate option delta
			int optionDelta = opt.getOptionNumber() - lastOptionNumber;
			
			// ensure that option delta value can be encoded correctly
			while (optionDelta > MAX_OPTIONDELTA) {
				
				// option delta is too large to be encoded:
				// add fencepost options in order to reduce the option delta
				
				// get fencepost option that is next to the last option
				int fencepostNumber = 
					OptionNumberRegistry.nextFencepost(lastOptionNumber);
				
				// calculate fencepost delta
				int fencepostDelta = fencepostNumber - lastOptionNumber;
				
				// correctness assertions
				//assert fencepostDelta > 0: "Fencepost liveness";
				//assert fencepostDelta <= MAX_OPTIONDELTA: "Fencepost safety";
				if (fencepostDelta <= 0) {
					System.out.printf("Fencepost liveness violated: delta = %d\n", fencepostDelta);
				}
				
				if (fencepostDelta > MAX_OPTIONDELTA) {
					System.out.printf("Fencepost safety violated: delta = %d\n", fencepostDelta);
				}

				
				// write fencepost option delta
				optWriter.write(fencepostDelta, OPTIONDELTA_BITS);
				
				// fencepost have an empty value
				optWriter.write(0, OPTIONLENGTH_BASE_BITS);
				//System.out.printf("DEBUG: %d\n", fencepostDelta);
				
				// increment option count
				++optionCount;
				
				// update last option number
				lastOptionNumber = fencepostNumber;
				
				// update option delta
				optionDelta -= fencepostDelta;
			}
			
			// write option delta
			optWriter.write(optionDelta, OPTIONDELTA_BITS);
			
			// write option length
			int length = opt.getLength();
			if (length <= MAX_OPTIONLENGTH_BASE) {
				
				// use option length base field only to encode
				// option lengths less or equal than MAX_OPTIONLENGTH_BASE
				
				optWriter.write(length, OPTIONLENGTH_BASE_BITS);
				
			} else {
				
				// use both option length base and extended field
				// to encode option lengths greater than MAX_OPTIONLENGTH_BASE
				
				int baseLength = MAX_OPTIONLENGTH_BASE + 1;
				optWriter.write(baseLength, OPTIONLENGTH_BASE_BITS);
				
				int extLength = length - baseLength;
				optWriter.write(extLength, OPTIONLENGTH_EXTENDED_BITS);
				
			}

			// write option value
			optWriter.writeBytes(opt.getRawValue());
			
			// increment option count
			++optionCount;
			
			// update last option number
			lastOptionNumber = opt.getOptionNumber();
		}

		
		// create datagram writer to encode message data
		DatagramWriter writer = new DatagramWriter();
		
		// write fixed-size CoAP header
		writer.write(version, VERSION_BITS);
		writer.write(type.ordinal(), TYPE_BITS);
		writer.write(optionCount, OPTIONCOUNT_BITS);
		writer.write(code, CODE_BITS);
		writer.write(messageID, ID_BITS);
		
	
		// write options
		writer.writeBytes(optWriter.toByteArray());
		
		//write payload
		writer.writeBytes(payload);

		// return encoded message
		return writer.toByteArray();
	}

	/*
	 * Decodes the message from the its binary representation
	 * as specified in draft-ietf-core-coap-05, section 3.1
	 * 
	 * @param byteArray A byte array containing the CoAP encoding of the message
	 * 
	 */
	public void fromByteArray(byte[] byteArray) {

		//Initialize DatagramReader
		DatagramReader datagram = new DatagramReader(byteArray);
		
		//Read current version
		version = datagram.read(VERSION_BITS);
		
		//Read current type
		type = getTypeByID(datagram.read(TYPE_BITS));
		
		//Read number of options
		int optionCount = datagram.read(OPTIONCOUNT_BITS);
		
		//Read code
		code = datagram.read(CODE_BITS);
		
		//Read message ID
		messageID = datagram.read(ID_BITS);
		
		//Current option nr initialization
		int currentOption = 0;

		//Loop over all options
		//System.out.println("DEBUG OPTION CNT: " + optionCount);
		for (int i=0; i < optionCount; i++) {
			
			//Read option delta bits
			int optionDelta = datagram.read(OPTIONDELTA_BITS);
			
			currentOption += optionDelta;
			//System.out.printf("DEBUG MSG: %d\n", optionDelta);
			if (OptionNumberRegistry.isFencepost(currentOption))
			{
				//Read number of options
				datagram.read(OPTIONLENGTH_BASE_BITS);
				
				//Fencepost: Reset Option to 0
				//TODO: FIX
				//currentOption = 0;
			} else {
				
				//Read option length
				int length = datagram.read(OPTIONLENGTH_BASE_BITS);
				
				if (length > MAX_OPTIONLENGTH_BASE)
				{
					//Read extended option length
					//length = datagram.read(OPTIONLENGTH_EXTENDED_BITS)
					//		 - (MAX_OPTIONLENGTH_BASE + 1);
					
					length += datagram.read(OPTIONLENGTH_EXTENDED_BITS);
				}
				//Read option
				Option opt = new Option (datagram.readBytes(length), currentOption);
				
				//Add option to message
				addOption(opt);
			}
		}

		
		
		//Get payload
		payload = datagram.readBytesLeft();
		
		/*if (((datagramTailBitSize % 8) == 0) && (datagramTailBitSize > 0)) {
			int datagramTailByteSize = datagramTailBitSize / 8;
			payload = datagram.readBytes(datagramTailByteSize);
		} else {
			//TODO: Error handling
			System.out.println("ALIGNMENT ERROR: Bit Size = " + datagramTailBitSize);
		}*/
	}
	
	
	// Procedures //////////////////////////////////////////////////////////////
	
	/*
	 * This procedure sets the URI of this CoAP message
	 * 
	 * @param uri The URI to which the current message URI should be set to
	 */
	public void setURI(URI uri) {
		// TODO include URI components path, query etc. into message options
		this.uri = uri;
	}
	
	/*
	 * This procedure sets the payload of this CoAP message
	 * 
	 * @param payload The payload to which the current message payload should
	 *                be set to
	 */
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	
	/*
	 * This procedure sets the type of this CoAP message
	 * 
	 * @param msgType The message type to which the current message type should
	 *                be set to
	 */
	public void setType(messageType msgType) {
		this.type = msgType;
	}
	
	/*
	 * This procedure sets the code of this CoAP message
	 * 
	 * @param code The message code to which the current message code should
	 *             be set to
	 */
	public void setCode(int code) {
		this.code = code;
	}
	
	/*
	 * This procedure sets the ID of this CoAP message
	 * 
	 * @param id The message ID to which the current message ID should
	 *           be set to
	 */
	public void setID(int id) {
		this.messageID = id;
	}
	
	// Functions ///////////////////////////////////////////////////////////////
		
	/*
	 * This function returns the URI of this CoAP message
	 * 
	 * @return The current URI
	 */
	public URI getURI() {
		return this.uri;
	}
	
	/*
	 * This function returns the payload of this CoAP message
	 * 
	 * @return The current payload.
	 */
	public byte[] getPayload() {
		return this.payload;
	}
	
	/*
	 * This function returns the version of this CoAP message
	 * 
	 * @return The current version.
	 */
	public int getVersion() {
		return this.version;
	}
	
	/*
	 * This function returns the type of this CoAP message
	 * 
	 * @return The current type.
	 */
	public messageType getType() {
		return this.type;
	}
	
	/*
	 * This function returns the code of this CoAP message
	 * 
	 * @return The current code.
	 */
	public int getCode() {
		return this.code;
	}
	
	/*
	 * This function returns the ID of this CoAP message
	 * 
	 * @return The current ID.
	 */
	public int getID() {
		return this.messageID;
	}

	
	/*
	 * This procedure adds an option to the list of options of this CoAP message
	 * 
	 * @param opt The option which should be added to the list of options of the
	 *            current CoAP message
	 */
	public void addOption(Option opt) {
		
		List<Option> list = options.get(opt.getOptionNumber());
		if (list == null) {
			list = new ArrayList<Option>();
		}
		options.put(opt.getOptionNumber(), list);
	}	
	
	/*
	 * This function returns all options with the given option number
	 * 
	 * @param optionNumber The option number
	 * @return A list containing the options with the given number
	 */
	public List<Option> getOptions(int optionNumber) {
		return options.get(optionNumber);
	}

	/*
	 * Sets all options with the specified option number
	 * 
	 * @param optionNumber The option number
	 * @param opt The list of the options
	 */
	public void setOptions(int optionNumber, List<Option> opt) {
		// TODO Check if all options are consistent with optionNumber
		options.put(optionNumber, opt);
	}
	
	/*
	 * Returns the first option with the specified option number
	 * 
	 * @param optionNumber The option number
	 * @return The first option with the specified number, or null
	 */
	public Option getOption(int optionNumber) {
		
		List<Option> list = getOptions(optionNumber);
		return !list.isEmpty() ? list.get(0) : null;
	}
	
	/*
	 * Sets the first option with the specified option number
	 * 
	 * @param opt The option to set
	 */
	public void setOption(Option opt) {

		List<Option> list = getOptions(opt.getOptionNumber());
		if (list.isEmpty()) {
			addOption(opt);
		} else {
			list.set(0, opt);
		}
	}

	/*
	 * Returns a sorted list of all included options
	 * 
	 * @return A sorted list of all options (copy)
	 */
	public List<Option> getOptionList() {

		List<Option> list = new ArrayList<Option>();
		
		for (List<Option> option : options.values()) {
			list.addAll(option);
		}
		
		return list;
	}	
	
	/*
	 * This function returns the number of options of this CoAP message
	 * 
	 * @return The current number of options.
	 */
	public int getOptionCount() {
		return getOptionList().size();
	}
	
	/*
	 * This function returns the buddy of this CoAP message
	 * Two messages are buddies iif they have the same message ID
	 * 
	 * @return The buddy of the message, if any
	 */
	public Message getBuddy() {
		return this.buddy;
	}
	
	/*
	 * TODO: description
	 */
	public messageType getTypeByID(int id) {
		switch (id) {
			case 0:
				return messageType.Confirmable;
			case 1:
				return messageType.Non_Confirmable;
			case 2:
				return messageType.Acknowledgement;
			case 3:
				return messageType.Reset;
			default:
				return messageType.Confirmable;
		}
	}
	
	/*
	 * This function checks if the message is a request message
	 * 
	 * @return True if the message is a request
	 */
	public boolean isRequest() {
		return CodeRegistry.isRequest(code);
	}
	
	/*
	 * This function checks if the message is a response message
	 * 
	 * @return True if the message is a response
	 */
	public boolean isResponse() {
		return CodeRegistry.isResponse(code);
	}

	public boolean isConfirmable() {
		return type == messageType.Confirmable;
	}
	
	public boolean isNonConfirmable() {
		return type == messageType.Non_Confirmable;
	}
	
	public boolean isAcknowledgement() {
		return type == messageType.Acknowledgement;
	}
	
	public boolean isReset() {
		return type == messageType.Reset;
	}
	
	public boolean isReply() {
		return isAcknowledgement() || isReset();
	}
	
	@Override
	public String toString() {

		String typeStr = "???";
		if (type != null) switch (type) {
			case Confirmable     : typeStr = "CON"; break;
			case Non_Confirmable : typeStr = "NON"; break;
			case Acknowledgement : typeStr = "ACK"; break;
			case Reset           : typeStr = "RST"; break;
			default              : typeStr = "???"; break;
		}
		return String.format("%s: [%s] %s '%s'(%d)",
			key(), typeStr, CodeRegistry.toString(code), 
			new String(payload), payload.length);
	}
	
	/*
	 * Returns a string that is assumed to uniquely identify a message
	 * 
	 * @return A string identifying the message
	 */
	public String key() {
		return String.format("%s#%d", 
			uri != null ? uri.getAuthority() : "", 
			messageID);
	}
	
	// Attributes //////////////////////////////////////////////////////////////
	
	//The message's URI
	private URI uri;
	
	//The message's payload
	private byte[] payload;
	
	/*
	 * The message's version. This must be set to 1. Other numbers are reserved
	 * for future versions
	 */
	private int version = 1;
	
	//The message's type.
	private messageType type;
	
	/*
	 * The message's code
	 * 
	 *      0: Empty
	 *   1-31: Request
	 * 64-191: Response
	 */
	private int code;
	
	//The message's ID
	private int messageID;
	
	// The message's buddy. Two messages are buddies iif
	// they have the same message ID
	private Message buddy;
	
	//The message's options
	private Map<Integer, List<Option>> options
		= new TreeMap<Integer, List<Option>>();
	

	// Declarations ////////////////////////////////////////////////////////////
	/*
	 * The message's type which can have the following values:
	 * 
	 * 0: Confirmable
	 * 1: Non-Confirmable
	 * 2: Acknowledgment
	 * 3: Reset
	 */
	public enum messageType {
		Confirmable,
		Non_Confirmable,
		Acknowledgement,
		Reset
	}
}
