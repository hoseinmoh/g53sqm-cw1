package main.java.jibble;




import java.io.File;
import java.io.IOException;

/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of Jibble Web Server / WebServerLite.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: WebServerMain.java,v 1.2 2004/02/01 13:37:35 pjm2 Exp $

*/

//test
/**
 * This class contains the main method for the Jibble Web Server.
 * 
 * @author Copyright Paul Mutton, http://www.jibble.org/
 */
public class WebServerMain {

    public static void main(String[] args) {
    	
    	File f = new File("webfiles/index.html");
    	
    	System.out.println("Jibble web server (modified by Tim Mohammad Hossein Mohanna 013867 for G53SQM)");
    	try {
			System.out.println("Root Directory: " + f.getCanonicalPath());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	System.out.println("Port: " + WebServerConfig.DEFAULT_PORT);
        
        String rootDir = WebServerConfig.DEFAULT_ROOT_DIRECTORY;
        int port = WebServerConfig.DEFAULT_PORT;
        
        if (args.length > 0) {
            rootDir = args[0];
        }
        
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                // Stick with the default value.
            }
        }
        
        try {
            WebServer server = new WebServer(rootDir, port);
            server.activate();
        }
        catch (WebServerException e) {
            System.out.println(e.toString());
        }
    }
    
    //test

}