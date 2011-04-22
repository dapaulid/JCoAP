package coap;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

/*
 * This class describes the functionality of the CoAP messages
 * 
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class Option {

	public Option (byte[] raw, int nr) {
		setRawValue(raw);
		setOptionNr(nr);
		
	}
	public byte[] getRawValue() {
		return data.array();
	}

	public int getOptionNumber() {
		return optionNr;
	}

	public int getLength() {
		return data.capacity();
	}
	
	public String getStringValue () {
		CharBuffer buf = data.asCharBuffer();
		String str = buf.toString();
		// TODO: check string validity
		return str;
	}
	
	public int getIntValue () {
		IntBuffer buf = data.asIntBuffer();
		int val = buf.get();
		//TODO: check int validity
		return val;
	}
	
	public void setRawValue (byte[] raw) {
		data = ByteBuffer.wrap(raw);
	}
	
	public void setOptionNr (int nr) {
		optionNr = nr;
	}
	
	private ByteBuffer data;
	private int optionNr;
	

}
