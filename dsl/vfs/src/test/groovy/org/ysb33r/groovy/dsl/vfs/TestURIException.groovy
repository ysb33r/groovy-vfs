package org.ysb33r.groovy.dsl.vfs

import static org.junit.Assert.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.Ignore
import org.ysb33r.groovy.dsl.vfs.URIException;



public class TestURIException {

	@Test
	public void standardCtor() {
		def u = new URIException("input","reason")
		
		assertEquals u.reason, "reason"
		assertEquals u.input, "input"
		
	}

	@Test
	public void gstringCtor() {
		
		def value = 5
		def u = new URIException("${value}","reason")
		
		assertEquals u.reason, "reason"
		assertEquals u.input, "5"
		
	}


	@Test(expected=URIException)
	public void throwException() {
		throw new URIException("input","reason")
	}
}
