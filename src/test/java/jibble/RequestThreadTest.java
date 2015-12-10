package jibble;

import org.junit.Test;

public class RequestThreadTest {

	@Test
	public void testRequestThread()  {
			
	}
	
	
	
	@Test(expected = NullPointerException.class)
	public void testRun() {
        RequestThread RQThread = new RequestThread(null, null);
        RQThread.run();
	}

}
