package coap;

public class PUTRequest extends Request {

	public PUTRequest() {
		super(CodeRegistry.METHOD_PUT, true);
	}
	
	@Override
	protected void dispatch(RequestHandler handler) {
		handler.performPUT(this);
	}	
}
