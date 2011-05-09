package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coap.Resource;

public class ResourceTest {

	@Test
	public void simpleTest () {
		String input = "</sensors/temp>;ct=41;n=\"TemperatureC\"";
		Resource res = Resource.fromLinkFormat(input);
		
		assertEquals("/sensors/temp",res.getResourceIdentifier());
		assertEquals(41,res.getContentTypeCode());
		assertEquals("TemperatureC", res.getResourceName());
	}
	
	@Test
	public void extendedTest () {
		String input = "</myUri/something>;n=\"MyName\";d=\"/someRef/path\";ct=42;sz=10;obs";
		Resource res = Resource.fromLinkFormat(input);
		
		assertEquals("/myUri/something",res.getResourceIdentifier());
		assertEquals("MyName", res.getResourceName());
		assertEquals("/someRef/path", res.getInterfaceDescription());
		assertEquals(42,res.getContentTypeCode());
		assertEquals(10, res.getMaximumSizeEstimate());
		assertTrue(res.isObservable());
		
	}
	
	@Test
	public void conversionTest () {
		String ref = "</myUri/something>;n=\"MyName\";d=\"/someRef/path\";ct=42;sz=10;obs";
		Resource res = Resource.fromLinkFormat(ref);
		String result = res.toLinkFormat();
		assertEquals(ref, result);
	}
}
