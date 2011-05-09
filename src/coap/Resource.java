package coap;

import java.util.StringTokenizer;

/*
 * This class describes the functionality of a CoAP resource
 * 
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class Resource {

	// Constructors ////////////////////////////////////////////////////////////
	
	/*
	 * This is a constructor for a new resource
	 */
	public Resource () {
		resourceIdentifier = new String();
		resourceName = new String();
		interfaceDescription = new String();
		contentTypeCode = -1;
		maximumSizeEstimate = -1;
		observable = false;
	}
	
	
	// Procedures //////////////////////////////////////////////////////////////
	
	/*
	 * This method sets an extension attribute given in a string of the form
	 * "...=..."
	 * 
	 * @param linkExtension The link extension string specifying a link
	 *                      extension and a value for it
	 */
	public void populateAttributeFromLinkExtension (String linkExtension) {
		String[] elements = linkExtension.split("=");
		
		String extension = elements[0];
		String value = elements[1];
		
		if (extension.equals("n")) {
			setResourceName(value.substring(1, value.length()-1));
		} else if (extension.equals("d")) {
			setInterfaceDescription(value.substring(1, value.length()-1));
		} else if (extension.equals("ct")) {
			setContentTypeCode(Integer.parseInt(value));
		} else if (extension.equals("sz")) {
			setMaximumSizeEstimate(Integer.parseInt(value));
		} else if (extension.equals("obs")) {
			setObservable(Boolean.parseBoolean(value));
		}
	}
	
	/*
	 * This method sets the resource identifier of the current resource
	 * 
	 * @param resourceURI The resource identifier
	 */
	public void setResourceIdentifier(String resourceIdentifier) {
		this.resourceIdentifier = resourceIdentifier;
	}

	/*
	 * This method sets the resource name of the current resource
	 * 
	 * @param resourceName The resource name
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/*
	 * This method sets the interface description of the current resource
	 * 
	 * @param interfaceDescription The resource interface description
	 */
	public void setInterfaceDescription(String interfaceDescription) {
		this.interfaceDescription = interfaceDescription;
	}

	/*
	 * This method sets the content type code of the current resource
	 * 
	 * @param contentTypeCode The resource contentTypeCode
	 */
	public void setContentTypeCode(int contentTypeCode) {
		this.contentTypeCode = contentTypeCode;
	}

	/*
	 * This method sets the maximum size estimate of the current resource
	 * 
	 * @param maximumSizeExtimate The resource maximum size estimate
	 */
	public void setMaximumSizeEstimate(int maximumSizeEstimate) {
		this.maximumSizeEstimate = maximumSizeEstimate;
	}

	/*
	 * This method sets whether the current resource is observable
	 * 
	 * @param observable The boolean value whether the current resource is
	 *                   observable
	 */
	public void setObservable(boolean observable) {
		this.observable = observable;
	}
	
	/*
	 * This method sets attributes of a given resource according to data from
	 * a given link extension string
	 * 
	 * @param res The resource containing the attributes which should be set
	 * @param linkExtension The string with the link extension data
	 */
	private static void populateAttributeFromLinkExtension (Resource res,
													String linkExtension) {
		//"extension=value" is split to [extension, value]
		String[] elements = linkExtension.split("=");
		
		//Set extension string fo first array element (containing extension)
		String extension = elements[0];
		
		//Set value string if available
		String value = new String();
		if (elements.length > 1) {
			value = elements[1];
		}
		//Set attribute according to extension
		if (extension.equals("n")) {
			res.setResourceName(value.substring(1, value.length()-1));
		} else if (extension.equals("d")) {
			res.setInterfaceDescription(value.substring(1, value.length()-1));
		} else if (extension.equals("ct")) {
			res.setContentTypeCode(Integer.parseInt(value));
		} else if (extension.equals("sz")) {
			res.setMaximumSizeEstimate(Integer.parseInt(value));
		} else if (extension.equals("obs")) {
			res.setObservable(true);
		}
	}
	
	// Functions ///////////////////////////////////////////////////////////////
	
	/*
	 * This method returns a resource from a link format string
	 * 
	 * @param linkFormatString The link format representation of the resource
	 * @return The resource
	 */
	public static Resource fromLinkFormat (String linkFormatString) {
		Resource resource = new Resource();
		
		StringTokenizer tokens = new StringTokenizer(linkFormatString, ";");
		
		//Get resource URI as <....> string and remove < >
		String uri = tokens.nextToken();
		resource.setResourceIdentifier(uri.substring(1, uri.length()-1));
		
		//Rest of tokens has form ...=...
		while (tokens.hasMoreTokens()) {
			populateAttributeFromLinkExtension(resource, tokens.nextToken());
		}
		
		return resource;
	}
	
	/*
	 * This method returns a link format string for the current resource
	 * 
	 * @return The link format string representing the current resource
	 */
	public String toLinkFormat () {
		StringBuilder linkFormat = new StringBuilder();
		linkFormat.append("<");
		linkFormat.append(this.getResourceIdentifier());
		linkFormat.append(">;");
		
		if (!this.getResourceName().isEmpty()) {
			linkFormat.append("n=\"");
			linkFormat.append(this.getResourceName());
			linkFormat.append("\";");
		} 
		if (!this.getInterfaceDescription().isEmpty()) {
			linkFormat.append("d=\"");
			linkFormat.append(this.getInterfaceDescription());
			linkFormat.append("\";");
		}
		if (this.getContentTypeCode() != -1) {
			linkFormat.append("ct=");
			linkFormat.append(this.getContentTypeCode());
			linkFormat.append(";");
		} 
		if (this.getMaximumSizeEstimate() != -1) {
			linkFormat.append("sz=");
			linkFormat.append(this.getMaximumSizeEstimate());
			linkFormat.append(";");
		} 
		if (this.isObservable()) {
			linkFormat.append("obs;");
		}
		//Remove last semicolon
		linkFormat.deleteCharAt(linkFormat.length()-1);
		
		return linkFormat.toString();
	}
	
	/*
	 * This method returns the resource URI of the current resource
	 * 
	 * @return The current resource URI
	 */
	public String getResourceIdentifier() {
		return resourceIdentifier;
	}

	/*
	 * This method returns the resource name of the current resource
	 * 
	 * @return The current resource name
	 */
	public String getResourceName() {
		return resourceName;
	}

	/*
	 * This method returns the interface description of the current resource
	 * 
	 * @return The current resource interface description
	 */
	public String getInterfaceDescription() {
		return interfaceDescription;
	}

	/*
	 * This method returns the content type code of the current resource
	 * 
	 * @return The current resource content type code
	 */
	public int getContentTypeCode() {
		return contentTypeCode;
	}

	/*
	 * This method returns the maximum size estimate of the current resource
	 * 
	 * @return The current resource maximum size estimate
	 */
	public int getMaximumSizeEstimate() {
		return maximumSizeEstimate;
	}

	/*
	 * This method returns whether the current resource is observable or not
	 * 
	 * @return Boolean value whether the current resource is observable
	 */
	public boolean isObservable() {
		return observable;
	}
	
	
	// Attributes //////////////////////////////////////////////////////////////
	
	//The current resource's identifier
	private String resourceIdentifier;
	
	//The current resource's name
	private String resourceName;
	
	//The current resource's interface description
	private String interfaceDescription;
	
	//The current resource's content type code
	private int contentTypeCode;
	
	//The current resource's maximum size estimate
	private int maximumSizeEstimate;
	
	//The current resource's observability
	private boolean observable;
}
