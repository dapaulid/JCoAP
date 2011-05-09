package coap;

public class Response extends Message {

	void setRequest(Request request) {
		this.request = request;
	}
	
	public Request getRequest() {
		return request;
	}
	
	private Request request;
}
