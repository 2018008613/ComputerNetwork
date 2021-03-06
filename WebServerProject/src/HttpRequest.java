import java.io.*;
import java.net.*;
import java.util.*;

final class HttpRequest implements Runnable {
	final static String CRLF = "\r\n";
	Socket socket;
	
	public HttpRequest(Socket socket) throws Exception{
		this.socket = socket;
	}
	
	public void run() {
		try {
			processRequest();
		} catch(Exception e) {
			System.out.println(e);
		}
	}


	private void processRequest() throws Exception{
	
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
	
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
	
		String requestLine = br.readLine();
	
		if(requestLine != null) {
		
			StringTokenizer tokens = new StringTokenizer(requestLine);
			System.out.println("request line: " + requestLine.toString());
			tokens.nextToken();
		
			String fileName = tokens.nextToken();
		
			fileName = "." + fileName;
		
			FileInputStream fis = null;
			boolean fileExists = true;
		
			try {
				fis = new FileInputStream(fileName);
			} catch (FileNotFoundException e) {
				fileExists = false;
			}
		
			System.out.println("Incoming!!!");
			
			String statusLine = null;
			String contentTypeLine = null;
			String entityBody = null;
			
			String version = tokens.nextToken().toUpperCase();
			if(version.endsWith("1.0"))
	        {
	        	statusLine = "HTTP/1.0 400 BAD REQUEST(HTTP PROTOCOL VERSION)" + CRLF;
	    	    contentTypeLine = "Content-Type: text/html" + CRLF;
	    	    entityBody = "<HTML>" + "<HEAD><TITLE>BAD REQUEST</TITLE></HEAD>" +"<BODY>Not Found</BODY></HTML>";
	        }
			else if(fileExists) {
				statusLine = "Http/1.1 200 OK" + CRLF;
				contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
			}
			else
			{
				statusLine = "HTTP/1.1 404 Not found" + CRLF;
				contentTypeLine = "Content-type: text/html" + CRLF;
				entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>Not Found</BODY></HTML>";
			}
		
			os.writeBytes(statusLine);
		
			os.writeBytes(contentTypeLine);
		
			os.writeBytes(CRLF);
		
			if(fileExists) {
				sendBytes(fis,os);
			}
			else {
				os.writeBytes(entityBody);
			}
		
			if(fis!=null) {
				fis.close();
			}
		}
		os.close();
		br.close();
		socket.close();
	}
	
	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception{
		
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		while((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
	}
	
	private static String contentType(String fileName) {
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if(fileName.endsWith(".gif") || fileName.endsWith(".GIF")) {
			return "image/gif";
		}
		if(fileName.endsWith(".jpg"))
		{
			return "image/jpg";
		}
		if(fileName.endsWith(".java"))
		{
			return "java file";
		}
		return "application/octet-stream";
	}
}


