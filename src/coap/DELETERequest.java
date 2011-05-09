package coap;

public class DELETERequest extends Request {

	public DELETERequest() {
		super(CodeRegistry.METHOD_DELETE, true);
	}
	
	@Override
	protected void dispatch(RequestHandler handler) {
		handler.performDELETE(this);
	}	
}
