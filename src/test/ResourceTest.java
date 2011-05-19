package test;

import static org.junit.Assert.*;

import org.junit.Test;

import coap.RemoteResource;
import coap.Resource;

public class ResourceTest {

	@Test
	public void simpleTest () {
		String input = "</sensors/temp>;ct=41;n=\"TemperatureC\"";
		Resource root = RemoteResource.newRoot(input);
		
		Resource res = root.getResource("/sensors/temp");
		assertNotNull(res);
		
		assertEquals("temp",res.getResourceIdentifier());
		assertEquals(41,res.getContentTypeCode());
		assertEquals("TemperatureC", res.getResourceName());
	}
	
	@Test
	public void extendedTest () {
		String input = "</myUri/something>;n=\"MyName\";d=\"/someRef/path\";ct=42;sz=10;obs";
		Resource root = RemoteResource.newRoot(input);
		
		Resource res = root.getResource("/myUri/something");
		assertNotNull(res);
		
		assertEquals("something",res.getResourceIdentifier());
		assertEquals("MyName", res.getResourceName());
		assertEquals("/someRef/path", res.getInterfaceDescription());
		assertEquals(42,res.getContentTypeCode());
		assertEquals(10, res.getMaximumSizeEstimate());
		assertTrue(res.isObservable());
		
	}
	
	@Test
	public void conversionTest () {
		String ref = "</myUri>,</myUri/something>;n=\"MyName\";d=\"/someRef/path\";ct=42;sz=10;obs";
		Resource res = RemoteResource.newRoot(ref);
		String result = res.toLinkFormat();
		assertEquals(ref, result);
	}
}
