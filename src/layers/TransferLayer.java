package layers;

import java.io.IOException;

import coap.Message;
import coap.Option;
import coap.OptionNumberRegistry;

public class TransferLayer extends UpperLayer {

	public static void decodeBlock(Option blockOpt) {
		int value = blockOpt.getIntValue();
		
		int szx = value      & 0x7;
		int m   = value >> 3 & 0x1;
		int num = value >> 4      ;
		
		int size = 1 << (szx + 4);
		System.out.printf("NUM: %d, SZX: %d (%d bytes), M: %d", num, szx, size, m);
	}
	
	public static Option encodeBlock(int num, int szx, int m) {
		int value = 0;
		
		value |= (szx & 0x7)     ;
		value |= (m   & 0x1) << 3;
		value |= num         << 4;
		
		return new Option(value, OptionNumberRegistry.BLOCK);
	}
	
	@Override
	protected void doReceiveMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doSendMessage(Message msg) throws IOException {
		// TODO Auto-generated method stub

	}

}
