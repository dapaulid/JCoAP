package coap;

import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

/*
 * This class describes the functionality of a set of CoAP resources,
 * e.g. for resource discovery
 * 
 * @author Dominique Im Obersteg & Daniel Pauli
 * @version 0.1
 * 
 */
public class Resources {
	
	// Constructors ////////////////////////////////////////////////////////////
	
	/*
	 * This is a constructor for a resource set
	 */
	public Resources () {
		data = new TreeMap<String, Resource>();
	}
	
	
	// Procedures //////////////////////////////////////////////////////////////
	
	/*
	 * This method adds a resource to the set of resources
	 * 
	 * @param resource The resource to add
	 */
	public void addResource (Resource resource) {
		//Add Resource with it's identifier as key
		data.put(resource.getResourceIdentifier(), resource);
	}
	
	/*
	 * This method removes a resource from the set of resources
	 * 
	 * @param resourceIdentifier The identifier of the resource to remove
	 */
	public void removeResource (String resourceIdentifier) {
		//Remove Resource with the key 'resourceIdentifier'
		data.remove(resourceIdentifier);
	}
	
	
	// Functions ///////////////////////////////////////////////////////////////
	
	/*
	 * This method returns a resource set from a link format string
	 * 
	 * @param linkFormatString The link format representation of the resources
	 * @return The resource set
	 */
	public static Resources fromLinkFormat (String linkFormatString) {
		Resources resources = new Resources();
		
		//Resources are separated by comma ->tokenize input string
		StringTokenizer tokens = new StringTokenizer(linkFormatString, ",");
		
		//Get resources
		while (tokens.hasMoreTokens()) {
			resources.addResource(Resource.fromLinkFormat(tokens.nextToken()));
		}
		return resources;
	}
	
	/*
	 * This method returns a link format string for the current resource set
	 * 
	 * @return The link format string representing the current resource set
	 */
	public String toLinkFormat () {
		//Create new StringBuilder
		StringBuilder linkFormat = new StringBuilder();
		
		//Get set representation of hash map
		Set<Entry<String, Resource>> content = data.entrySet();
		
		//Get iterator on set
		Iterator<Entry<String, Resource>> it = content.iterator();
		
		//Loop over all set elements
		while (it.hasNext()) {
			Entry<String, Resource> currentEntry = it.next();
			
			//Get Resource of current set element
			Resource currentResource = currentEntry.getValue();
			
			//Convert Resource to string representation and add semicolon
			linkFormat.append(currentResource.toLinkFormat());
			linkFormat.append(",");
		}
		//Remove last semicolon
		linkFormat.deleteCharAt(linkFormat.length()-1);
		
		return linkFormat.toString();
	}
	
	/*
	 * This method returns the current resource set hash map
	 * 
	 * @return The current resource set hash map
	 */
	public Map<String, Resource> getResources () {
		return this.data;
	}
	
	/*
	 * This method returns a resource from the set given a resource identifier
	 * 
	 * @param resourceIdentifier The identifier of the resource to get
	 * @return The resource with a given identifier
	 */
	public Resource getResource (String resourceIdentifier) {
		if (hasResource(resourceIdentifier)) {
			return ((Resource) data.get(resourceIdentifier));
		} else {
			System.err.println("Method 'getResource' in Resources.java:");
			System.err.println("Resource '" + resourceIdentifier + "' is not available");
			return null;
		}
	}
	
	/*
	 * This method whether a resource with a given identifier is present in the
	 * set
	 * 
	 * @param resourceIdentifier The identifier of the resource to check for
	 *                           availability in the set
	 * @return Whether the resource with a given identifier is available in the
	 *         set
	 */
	public boolean hasResource (String resourceIdentifier) {
		return data.containsKey(resourceIdentifier);
	}
	
	public int getResourceCount () {
		return data.size();
	}
	
	public void log() {
		//Create new StringBuilder
		StringBuilder linkFormat = new StringBuilder();
		
		//Get set representation of hash map
		Set<Entry<String, Resource>> content = data.entrySet();
		
		//Get iterator on set
		Iterator<Entry<String, Resource>> it = content.iterator();
		
		//Loop over all set elements
		while (it.hasNext()) {
			Entry<String, Resource> currentEntry = it.next();
			
			//Get Resource of current set element
			linkFormat.append("Resource: ");
			linkFormat.append(currentEntry.getKey());
			linkFormat.append("\n");
		}
		System.out.println(linkFormat.toString());
	}
	
	
	// Attributes //////////////////////////////////////////////////////////////
	
	//The hash map representing the set of resources
	private Map<String, Resource> data;
}
