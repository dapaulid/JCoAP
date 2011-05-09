package coap;

public class Payload {
	
	public synchronized void append(byte[] bytes) {
		
		if (bytes != null) {
			if (data != null) {
		
				byte[] oldData = data;
				data = new byte[oldData.length + bytes.length];
				System.arraycopy(oldData, 0, data, 0, oldData.length);
				System.arraycopy(bytes, 0, data, oldData.length, bytes.length);
				
			} else {
				
				data = bytes.clone();
			}
			
			appended(bytes);
		}
	}
	
	protected void appended(byte[] bytes) {
		
	}
	
	protected void completed() {
		
	}
	
	public boolean validPos(int pos) {
		return pos >= 0 && pos < size();
	}
	
	public synchronized int getByte(int pos) {
		while (pos >= size()) {
			if (complete) {
				return -1;
			}
			try {
				wait();
			} catch (InterruptedException e) {
				return -1;
			}
		}
		return data[pos]; 
	}
	
	public synchronized byte[] getBytes(int pos) {
		byte[] bytes = new byte[size() - pos];
		System.arraycopy(data, pos, bytes, 0, bytes.length);
		return bytes;
	}
	
	public synchronized int size() {
		return data != null ? data.length : 0;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	
	private byte[] data;
	private boolean complete;
}
