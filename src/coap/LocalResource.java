package coap;


public class LocalResource extends Resource {

	public LocalResource(String resourceIdentifier, boolean hidden) {
		super(resourceIdentifier, hidden);
	}
	public LocalResource(String resourceIdentifier) {
		super(resourceIdentifier, false);
	}
	
	// REST Operations /////////////////////////////////////////////////////////
	
	@Override
	public void performGET(GETRequest request) {
		request.respond(CodeRegistry.RESP_NOT_IMPLEMENTED);
	}

	@Override
	public void performPUT(PUTRequest request) {
		request.respond(CodeRegistry.RESP_NOT_IMPLEMENTED);
	}
	
	@Override
	public void performPOST(POSTRequest request) {
		request.respond(CodeRegistry.RESP_NOT_IMPLEMENTED);
	}
	
	@Override
	public void performDELETE(DELETERequest request) {
		request.respond(CodeRegistry.RESP_NOT_IMPLEMENTED);
	}

	@Override
	public void createNew(PUTRequest request, String newIdentifier) {
		request.respond(CodeRegistry.RESP_NOT_IMPLEMENTED);
	}	
	
}
