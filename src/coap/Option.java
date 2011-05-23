package coap;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/*
 * This class describes the functionality of the CoAP messages
 * 
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class Option {

	// Constructors ////////////////////////////////////////////////////////////
	
	/*
	 * This is a constructor for a new option with a given number, based on a
	 * given byte array
	 * 
	 * @param raw The byte array
	 * @param nr The option number
	 * 
	 * @return A new option with a given number based on a byte array
	 */
	public Option (byte[] raw, int nr) {
		setValue(raw);
		setOptionNr(nr);
	}
	
	/*
	 * This is a constructor for a new option with a given number, based on a
	 * given string
	 * 
	 * @param str The string
	 * @param nr The option number
	 * 
	 * @return A new option with a given number based on a string
	 */
	public Option (String str, int nr) {
		setStringValue(str);
		setOptionNr(nr);
	}
	
	/*
	 * This is a constructor for a new option with a given number, based on a
	 * given integer value
	 * 
	 * @param val The integer value
	 * @param nr The option number
	 * 
	 * @return A new option with a given number based on a integer value
	 */
	public Option (int val, int nr) {
		setIntValue(val);
		setOptionNr(nr);
	}
	
	
	// Procedures //////////////////////////////////////////////////////////////
	
	/*
	 * This method sets the data of the current option based on a string input
	 * 
	 * @param str The string representation of the data which is stored in the
	 *            current option.
	 */
	private void setStringValue(String str) {
		value = ByteBuffer.wrap(str.getBytes());	
	}
	
	/*
	 * This method sets the data of the current option based on a integer value
	 * 
	 * @param val The integer representation of the data which is stored in the
	 *            current option.
	 */
	private void setIntValue(int val) {
		int neededBytes = 4;
		if (val == 0) {
			value = ByteBuffer.allocate(1);
			value.put((byte) 0);
		} else {ByteBuffer aux = ByteBuffer.allocate(4);
			aux.putInt(val);
			for (int i=3; i >= 0; i--) {
				if (aux.get(3-i) == 0x00) {
					neededBytes--;
				} else {
					break;
				}
			}
			value = ByteBuffer.allocate(neededBytes);
			for (int i = neededBytes - 1; i >= 0; i--) {
				value.put(aux.get(3-i));
			}
		}
	}
	
	/*
	 * This method sets the number of the current option
	 * 
	 * @param nr The option number.
	 */
	public void setOptionNr (int nr) {
		optionNr = nr;
	}
	
	/*
	 * This method sets the current option's data to a given byte array
	 * 
	 * @param value The byte array.
	 */
	public void setValue (byte[] value) {
		this.value = ByteBuffer.wrap(value);
	}
	
	
	// Functions ///////////////////////////////////////////////////////////////
	
	
	/*
	 * This method returns the data of the current option as byte array
	 * 
	 * @return The byte array holding the data
	 */
	public byte[] getRawValue() {
		return value.array();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + optionNr;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Option other = (Option) obj;
		if (optionNr != other.optionNr)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!Arrays.equals(this.getRawValue(),other.getRawValue()))
			return false;
		return true;
	}

	/*
	 * This method returns the option number of the current option
	 * 
	 * @return The option number as integer
	 */
	public int getOptionNumber() {
		return optionNr;
	}
	
	/*
	 * This method returns the name that corresponds to the option number.
	 * 
	 * @return The name of the option
	 */
	public String getName() {
		return OptionNumberRegistry.toString(optionNr);
	}

	/*
	 * This method returns the length of the option's data in the ByteBuffer
	 * 
	 * @return The length of the data stored in the ByteBuffer as number of bytes
	 */
	public int getLength() {
		return value.capacity();
	}
	
	/*
	 * This method returns the value of the option's data as string
	 * 
	 * @return The string representation of the current option's data
	 */
	public String getStringValue () {
		String result = "";
		try {
			result = new String(value.array(), "UTF8");
		} catch (UnsupportedEncodingException e) {
			System.err.println("String conversion error");
		}
		return result;
	}
	
	/*
	 * This method returns the value of the option's data as integer
	 * 
	 * @return The integer representation of the current option's data
	 */
	public int getIntValue () {
		int byteLength = value.capacity();
		ByteBuffer temp = ByteBuffer.allocate(4);
		for (int i=0; i < (4-byteLength); i++) {
			temp.put((byte)0);
		}
		for (int i=0; i < byteLength; i++) {
			temp.put(value.get(i));
		}
		
		int val = temp.getInt(0);
		return val;
	}
	
	/*
	 * This method returns the current option's data as byte array
	 * 
	 * @return The current option's data as byte array.
	 */
	public Object getValue () {
		return value;
	}
	
	private static String hex(byte[] data) {

		final String digits = "0123456789ABCDEF";
		
		if (data != null) {
			
			StringBuilder builder = new StringBuilder(data.length * 3);
			for (int i = 0; i < data.length; i++) {
				builder.append(digits.charAt((data[i] >> 4) & 0xF));
				builder.append(digits.charAt(data[i] & 0xF));
				if (i < data.length-1) {
					builder.append(' ');
				}
			}
			return builder.toString();
		} else {
			return null;
		}
	}
	
	/*
	 * Returns a human-readable string representation of the option's value
	 * 
	 * @Return The option value represented as a string
	 */
	public String getDisplayValue() {
		switch (optionNr) {
		case OptionNumberRegistry.CONTENT_TYPE:
			return MediaTypeRegistry.toString(getIntValue());
		case OptionNumberRegistry.MAX_AGE:
			return String.format("%d s", getIntValue());
		case OptionNumberRegistry.PROXY_URI:
			return getStringValue();
		case OptionNumberRegistry.ETAG:
			return hex(getRawValue());
		case OptionNumberRegistry.URI_HOST:
			return getStringValue();
		case OptionNumberRegistry.LOCATION_PATH:
			return getStringValue();
		case OptionNumberRegistry.URI_PORT:
			return String.valueOf(getIntValue());
		case OptionNumberRegistry.LOCATION_QUERY:
			return getStringValue();
		case OptionNumberRegistry.URI_PATH:
			return getStringValue();
		case OptionNumberRegistry.OBSERVE:
			return String.valueOf(getIntValue());
		case OptionNumberRegistry.TOKEN:
			return hex(getRawValue());
		case OptionNumberRegistry.URI_QUERY:
			return getStringValue();
		case OptionNumberRegistry.BLOCK:
		case OptionNumberRegistry.BLOCK1:
		case OptionNumberRegistry.BLOCK2:

			// TODO put this code elsewhere, as TransferLayer uses the same
			int value = getIntValue();
			
			int szx = value      & 0x7;
			int m   = value >> 3 & 0x1;
			int num = value >> 4      ;
			
			int size = 1 << (szx + 4);
			
			return String.format("NUM: %d, SZX: %d (%d bytes), M: %d", 
				num, szx, size, m);			
			
		default:
			return hex(getRawValue());
		}
	}
	
	// Attributes //////////////////////////////////////////////////////////////
	
	//The current option's data
	private ByteBuffer value;
	
	//The current option's number
	private int optionNr;
}
