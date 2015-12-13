package jibble;

/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of Jibble Web Server / WebServerLite.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: RequestThread.java,v 1.2 2004/02/01 13:37:35 pjm2 Exp $

*/

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A thread which deals with an individual request to the web server. This is
 * passed a socket from the WebServer when a connection is accepted.
 * 
 * @author Copyright Paul Mutton, http://www.jibble.org/
 */
public class RequestThread implements Runnable {

	public RequestThread(Socket socket, File rootDir) {
		_socket = socket;
		_rootDir = rootDir;

	}

	// handles a connction from a client.
	public void run() {
		String postBoundary = null, filename = null, contentLength = null;
		PrintWriter fout = null;

		String ip = "unknown";
		String request = "unknown";
		int bytesSent = 0;
		BufferedInputStream reader = null;
		try {
			ip = _socket.getInetAddress().getHostAddress();
			BufferedReader in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
			BufferedOutputStream out = new BufferedOutputStream(_socket.getOutputStream());
			outToClient = new DataOutputStream(_socket.getOutputStream());

			request = in.readLine();
			// currentLine = inFromClient.readLine();

			System.out.println(request);

			String path = "";
			// Read the first line from the client.

			// System.out.println("THIS IS THE REQUEST " + request);

			if (request != null && request.startsWith("GET ")
					&& (request.endsWith(" HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
				path = request.substring(4, request.length() - 9);
				// System.out.println("THIS IS THE PATH " + path);
			} else if (request != null && request.startsWith("POST ")
					&& (request.endsWith(" HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
				// //path = request.substring(4, request.length() - 9);
				try {
					System.out.println("POST request");
					do {
						request = in.readLine();

						if (request.indexOf("Content-Type: multipart/form-data") != -1) {
							String boundary = request.split("boundary=")[1];
							// The POST boundary

							while (true) {
								request = in.readLine();
								if (request.indexOf("Content-Length:") != -1) {
									contentLength = request.split(" ")[1];
									System.out.println("Content Length = " + contentLength);
									break;
								}
							}

							// Content length should be < 2MB
							if (Long.valueOf(contentLength) > 2000000L) {
								sendResponse(200, "File size should be < 2MB", false);
							}

							while (true) {
								request = in.readLine();
								if (request.indexOf("--" + boundary) != -1) {
									filename = in.readLine().split("filename=")[1].replaceAll("\"", "");
									String[] filelist = filename.split("\\" + System.getProperty("file.separator"));
									filename = filelist[filelist.length - 1];
									System.out.println("File to be uploaded = " + filename);
									break;
								}
							}

							String fileContentType = in.readLine().split(" ")[1];
							System.out.println("File content type = " + fileContentType);

							in.readLine(); // assert(inFromClient.readLine().equals(""))
											// : "Expected line in POST request
											// is "" ";

							fout = new PrintWriter(filename);
							String prevLine = in.readLine();
							request = in.readLine();

							// Here we upload the actual file contents
							while (true) {
								if (request.equals("--" + boundary + "--")) {
									fout.print(prevLine);
									break;
								} else {
									fout.println(prevLine);
								}
								prevLine = request;
								request = in.readLine();
							}

							sendResponse(200, "File " + filename + " Uploaded..", false);
							fout.close();
						} // if
					} while (in.ready()); // End of do-while
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				// Invalid request type (no "GET")
				Logger.log(ip, request, 405);
				_socket.close();
				return;
			}

			// Read in and store all the headers.

			// Specify String types of HasMap for safety - TJB
			// HashMap headers = new HashMap();
			HashMap<String, String> headers = new HashMap<String, String>();
			String line = null;
			while ((line = in.readLine()) != null) {
				// System.out.println("THIS IS THE LINE " + line);
				line = line.trim();
				if (line.equals("")) {
					break;
				}
				int colonPos = line.indexOf(":");
				if (colonPos > 0) {
					String key = line.substring(0, colonPos);
					String value = line.substring(colonPos + 1);
					// System.out.println("THIS IS THE KEY " + key);
					// System.out.println("THIS IS THE VALUE " + value);
					headers.put(key, value.trim());
				}
			}

			// URLDecocer.decode(String) is deprecated - added "UTF-8" - TJB
			File file = new File(_rootDir, URLDecoder.decode(path, "UTF-8"));

			file = file.getCanonicalFile();

			if (!file.toString().startsWith(_rootDir.toString())) {
				// Uh-oh, it looks like some lamer is trying to take a peek
				// outside of our web root directory.
				Logger.log(ip, request, 404);
				out.write(("HTTP/1.0 403 Forbidden\r\n" + "Content-Type: text/html\r\n"
						+ "Expires: Thu, 01 Dec 1994 16:00:00 GMT\r\n" + "\r\n" + "<h1>403 Forbidden</h1><code>" + path
						+ "</code><p><hr>" + "<i>" + WebServerConfig.VERSION + "</i>").getBytes());
				out.flush();
				_socket.close();
				return;
			}

			if (file.isDirectory()) {
				// Check to see if there are any index files in the directory.
				for (int i = 0; i < WebServerConfig.DEFAULT_FILES.length; i++) {
					File indexFile = new File(file, WebServerConfig.DEFAULT_FILES[i]);
					if (indexFile.exists() && !indexFile.isDirectory()) {
						file = indexFile;
						break;
					}
				}
				if (file.isDirectory()) {
					// print directory listing
					Logger.log(ip, request, 200);
					if (!path.endsWith("/")) {
						path = path + "/";
					}
					File[] files = file.listFiles();
					out.write(("HTTP/1.0 200 OK\r\n" + "Content-Type: text/html\r\n"
							+ "Expires: Thu, 01 Dec 1994 16:00:00 GMT\r\n" + "\r\n" + "<h1>Directory Listing</h1>"
							+ "<h3>" + path + "</h3>" + "<table border=\"0\" cellspacing=\"8\">"
							+ "<tr><td><b>Filename</b><br></td><td align=\"right\"><b>Size</b></td><td><b>Last Modified</b></td></tr>"
							+ "<tr><td><b><a href=\"../\">../</b><br></td><td></td><td></td></tr>").getBytes());
					for (int i = 0; i < files.length; i++) {
						file = files[i];
						if (file.isDirectory()) {
							out.write(("<tr><td><b><a href=\"" + path + file.getName() + "/\">" + file.getName()
									+ "/</a></b></td><td></td><td></td></tr>").getBytes());
						} else {
							out.write(("<tr><td><a href=\"" + path + file.getName() + "\">" + file.getName()
									+ "</a></td><td align=\"right\">" + file.length() + "</td><td>"
									+ new Date(file.lastModified()).toString() + "</td></tr>").getBytes());
						}
					}
					out.write(("</table><hr>" + "<i>" + WebServerConfig.VERSION + "</i>").getBytes());
					out.flush();
					_socket.close();
					return;
				}
			}

			if (!file.exists()) {
				// The file was not found.
				Logger.log(ip, request, 404);
				out.write(("HTTP/1.0 404 File Not Found\r\n" + "Content-Type: text/html\r\n"
						+ "Expires: Thu, 01 Dec 1994 16:00:00 GMT\r\n" + "\r\n" + "<h1>404 File Not Found</h1><code>"
						+ path + "</code><p><hr>" + "<i>" + WebServerConfig.VERSION + "</i>").getBytes());
				out.flush();
				_socket.close();
				return;
			}

			String extension = WebServerConfig.getExtension(file);

			// Execute any files in any cgi-bin directories under the web root.
			if (file.getParent().indexOf("cgi-bin") >= 0) {
				try {
					out.write("HTTP/1.0 200 OK\r\n".getBytes());
					ServerSideScriptEngine.execute(out, headers, file, path);
					out.flush();
					Logger.log(ip, path, 200);
				} catch (Throwable t) {
					// Internal server error!
					Logger.log(ip, request, 500);
					out.write(("Content-Type: text/html\r\n\r\n" + "<h1>Internal Server Error</h1><code>" + path
							+ "</code><hr>Your script produced the following error: -<p><pre>" + t.toString()
							+ "</pre><hr><i>" + WebServerConfig.VERSION + "</i>").getBytes());
					out.flush();
					_socket.close();
					return;
				}
				out.flush();
				_socket.close();
				return;
			}

			reader = new BufferedInputStream(new FileInputStream(file));

			Logger.log(ip, request, 200);
			String contentType = (String) WebServerConfig.MIME_TYPES.get(extension);
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			out.write(("HTTP/1.0 200 OK\r\n" + "Date: " + new Date().toString() + "\r\n"
					+ "Server: JibbleWebServer/1.0\r\n" + "Content-Type: " + contentType + "\r\n"
					+ "Expires: Thu, 01 Dec 1994 16:00:00 GMT\r\n" + "Content-Length: " + file.length() + "\r\n"
					+ "Last-modified: " + new Date(file.lastModified()).toString() + "\r\n" + "\r\n").getBytes());

			if (WebServerConfig.SSI_EXTENSIONS.contains(extension)) {
				reader.close();
				ServerSideIncludeEngine.deliverDocument(out, file);
				_socket.close();
				return;
			}

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = reader.read(buffer, 0, 4096)) != -1) {
				out.write(buffer, 0, bytesRead);
				bytesSent += bytesRead;
			}
			out.flush();
			reader.close();
			_socket.close();

		} catch (IOException e) {
			Logger.log(ip, "ERROR " + e.toString() + " " + request, 0);
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception anye) {
					// Do nothing.
				}
			}
		}
	}

	private Socket _socket;
	private File _rootDir;
	DataOutputStream outToClient = null;
	String HTML_START = "<html>" + "<title>HTTP POST</title>" + "<body>";
	String HTML_END = "</body>" + "</html>";

	public void sendResponse(int statusCode, String responseString, boolean isFile) throws Exception {

		String status = null;
		String serverdetails = "Server: Java HTTPServer";
		String contentLengthLine = null;
		String fileName = null;
		String contentTypeLine = "Content-Type: text/html" + "\r\n";
		FileInputStream fin = null;

		if (statusCode == 200)
			status = "HTTP/1.1 200 OK" + "\r\n";
		else
			status = "HTTP/1.1 404 Not Found" + "\r\n";

		if (isFile) {
			fileName = responseString;
			fin = new FileInputStream(fileName);
			contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
			if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
				contentTypeLine = "Content-Type: \r\n";
		} else {
			responseString = HTML_START + responseString + HTML_END;
			contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
		}

		outToClient.writeBytes(status);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");

		if (isFile)
			sendFile(fin, outToClient);
		else
			outToClient.writeBytes(responseString);

		outToClient.close();
	}

	public void sendFile(FileInputStream fin, DataOutputStream out) throws Exception {
		byte[] buffer = new byte[1024];
		int bytesRead;

		while ((bytesRead = fin.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
		}
		fin.close();
	}

}