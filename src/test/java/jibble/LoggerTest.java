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
public class LoggerTest extends TestCase {

	/**
	 * Test method for {@link jibble.Logger#log(java.lang.String, java.lang.String, int)}.
	 */
	@Test
	public void testLog()  {
		Logger.log(null, null, 0);
	}

}
