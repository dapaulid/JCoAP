package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coap.Resource;
import coap.Resources;

public class ResourcesTest {
	@Test
	public void TwoResourceTest () {
		String resourceInput1 = "</sensors/temp>;ct=41;n=\"TemperatureC\"";
		String resourceInput2 = "</myUri/something>;n=\"MyName\";d=\"/someRef/path\";ct=42;sz=10;obs";
		
		//Build link format string
		String resourceInput = resourceInput1 + "," + resourceInput2;
	
		//Construct two resources from link format substrings
		Resource res1 = Resource.fromLinkFormat(resourceInput1);
		Resource res2 = Resource.fromLinkFormat(resourceInput2);
		
		//Build resources from assembled link format string
		Resources resources = Resources.fromLinkFormat(resourceInput);
		
		//Check if resources are in hash map
		assertTrue(resources.hasResource(res1.getResourceName()));
		assertTrue(resources.hasResource(res2.getResourceName()));
		
		//Check if link format string equals input
		String expectedLinkFormat = res1.toLinkFormat() + "," + res2.toLinkFormat();
		assertEquals(expectedLinkFormat, resources.toLinkFormat());
	}
	
	
}
