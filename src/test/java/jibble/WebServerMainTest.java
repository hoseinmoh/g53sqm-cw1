/**
 * 
 */
package jibble;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * @author hosein
 *
 */
public class WebServerMainTest extends TestCase {

	/**
	 * Test method for {@link jibble.WebServerMain#main(java.lang.String[])}.
	 */
	@Test(expected = WebServerException.class)
	public void testMain() {
		//WebServerMain.main(new String[] {"arg1" , "arg2" , "arg2"});
	}

}
