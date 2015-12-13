package jibble;

import java.io.File;
import java.net.Socket;

import org.junit.Test;

public class RequestThreadTest {

	

	
	
	@Test
	public void testRequestThread()  {
		Socket socket = null;
		
		
		RequestThread RQThread = new RequestThread(socket, new File("test"));
		
		
	}
	
	
	
	@Test(expected = NullPointerException.class)
	public void testRun() {
        RequestThread RQThread = new RequestThread(null, null);
        RQThread.run();
	}
	
//	@Test(timeout=10000)
//	public void testRun2() {
//        RequestThread RQThread = new RequestThread(null, null);
//        RQThread.run();
//	}

}
