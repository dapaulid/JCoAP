package coap;

public class POSTRequest extends Request {
	
	public POSTRequest() {
		super(CodeRegistry.METHOD_POST, true);
	}
	
	@Override
	protected void dispatch(RequestHandler handler) {
		handler.performPOST(this);
	}	
}
