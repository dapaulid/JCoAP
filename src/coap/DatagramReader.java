package coap;

import java.io.ByteArrayInputStream;

/*
 * This class describes the functionality to read raw
 * network-ordered datagrams on bit-level.
 * 
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class DatagramReader {

	// Constructors ////////////////////////////////////////////////////////////	
	
	/*
	 * Initializes a new BitReader object
	 * 
	 * @param byteArray The byte array to read from
	 * 
	 */
	public DatagramReader(byte[] byteArray) {
		
		// initialize underlying byte stream
		byteStream = new ByteArrayInputStream(byteArray);

		// initialize bit buffer
		currentByte = 0;
		currentBitIndex = -1; // indicates that no byte read yet
	}
	
	// Procedures //////////////////////////////////////////////////////////////
	
	/*
	 * Reads a sequence of bits from the stream
	 * 
	 * @param numBits The number of bits to read
	 * @return An integer containing the bits read
	 * 
	 */
	public int read(int numBits) {
		
		int bits = 0; // initialize all bits to zero
	
		for (int i = numBits-1; i >= 0; i--) {
			
			// check whether new byte needs to be read
			if (currentBitIndex < 0) {
				readCurrentByte();
			}
			
			// test current bit
			boolean bit = (currentByte >> currentBitIndex & 1) != 0;
			if (bit) {
				// set bit at i-th position
				bits |= (1 << i);
			}
			
			// decrease current bit index
			--currentBitIndex;

		}
		
		return bits;
	}
	
	/*
	 * Reads a sequence of bytes from the stream
	 * 
	 * @param count The number of bytes to read
	 * @return The sequence of bytes read from the stream
	 * 
	 */
	public byte[] readBytes(int count) {
		
		// for negative count values, read all bytes left
		if (count < 0) count = byteStream.available();
		
		// allocate byte array
		byte[] bytes = new byte[count];

		// are there bits left to read in buffer?
		if (currentBitIndex >= 0) {
			
			for (int i = 0; i < count; i++) {
				bytes[i] = (byte) read(Byte.SIZE);
			}
			
		} else {
			
			// if bit buffer is empty, call can be delegated
			// to byte stream to increase performance
			byteStream.read(bytes, 0, bytes.length);
		}
		
		return bytes;
	}

	/*
	 * Reads the complete sequence of bytes left in the stream
	 * 
	 * @return The sequence of bytes left in the stream
	 * 
	 */
	public byte[] readBytesLeft() {
		return readBytes(-1);
	}
	
	// Utilities ///////////////////////////////////////////////////////////////
	
	/*
	 * Reads new bits from the stream
	 */
	private void readCurrentByte() {
		
		// try to read from byte stream
		int val = byteStream.read();
		
		if (val >= 0) {
			// byte successfully read
			currentByte = (byte) val;
		} else {
			// end of stream reached;
			// return implicit zero bytes
			currentByte = 0;
		}
		
		// reset current bit index
		currentBitIndex = Byte.SIZE-1;
	}
	
	// Attributes //////////////////////////////////////////////////////////////

	private ByteArrayInputStream byteStream;
	
	private byte currentByte;
	private int currentBitIndex;
	
}
