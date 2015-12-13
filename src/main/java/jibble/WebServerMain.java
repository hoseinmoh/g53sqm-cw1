package jibble;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



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
    	

    	
    	File f = new File(WebServerConfig.DEFAULT_ROOT_DIRECTORY + "/index.html");
    	
    	System.out.println("Jibble web server (modified by Tim Mohammad Hossein Mohanna 013867 for G53SQM)");
//    	try {
//			System.out.println("Root Directory: " + f.getCanonicalPath());
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
    	//System.out.println("Port: " + WebServerConfig.DEFAULT_PORT);
    	
    	
//    	try {
//			Scanner in = new Scanner(new FileReader("config.txt"));
//			
//			while(in.hasNextLine()) {
//				System.out.println(in.nextLine());
//			}
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
    	
        
        String rootDir = WebServerConfig.DEFAULT_ROOT_DIRECTORY;
        int port = WebServerConfig.DEFAULT_PORT;
        
        
        
        if(args.length == 0) {
        	try {
    			System.out.println("Root Directory: " + new File(rootDir).getCanonicalPath());
    		} catch (IOException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
        	System.out.println("Port: " + WebServerConfig.DEFAULT_PORT);
        }
        
        if (args.length > 0) {
            rootDir = args[0];
        	try {
    			System.out.println("Root Directory: " + new File(rootDir).getCanonicalPath());
    		} catch (IOException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
        	
        }
         if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
                System.out.println("Port: " + port);
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
    
    //testasd

}