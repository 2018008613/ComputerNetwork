import java.io.*;
import java.net.*;
import java.util.*;
public class HttpResponse
{
	final static String CRLF = "\r\n";
	/** How big is the buffer used for reading the object */
	final static int BUF_SIZE = 8192;
	/** Maximum size of objects that this proxy can handle. For the
	 * moment set to 100 KB. You can adjust this as needed. */
	final static int MAX_OBJECT_SIZE = 100000000;
	/** Reply status and headers */
	String version;
	int status;
	String statusLine = "";
	String headers = "";
	byte[] body = new byte[MAX_OBJECT_SIZE];
	@Deprecated
	public HttpResponse(DataInputStream fromServer)
	{
		int length = -1;
		boolean gotStatusLine = false;
		System.out.println("\nhttp response");
		try
		{
			String line = fromServer.readLine();
			while (line.length() != 0)
			{
				if (!gotStatusLine)
				{
					statusLine = line;
					gotStatusLine = true;
				}
				else
				{
					headers += line + CRLF;
				}
				if (line.startsWith ("Content-Length:") || line.startsWith ("Content-length:"))
				{
					String[] tmp = line.split(" ");
					length = Integer.parseInt(tmp[1]);
				}
				line = fromServer.readLine();
			}
		}
		catch (IOException e)
		{
			System.out.println("Error reading headers from server: " + e);
			return;
		}
		try
		{
			int bytesRead = 0;
			byte[] buf = new byte[BUF_SIZE];
			boolean loop = false;
			System.out.println("length : " + length);
			if (length == -1)
			{
				loop = true;
			}
			while (bytesRead < length || loop)
			{
				int res = fromServer.read(buf);
				if (res == -1)
				{
					break;
				}
				
				for (int i = 0; i < res && (i + bytesRead) < MAX_OBJECT_SIZE; i++)
				{
					body [bytesRead + i] = buf[i];
				}
				bytesRead += res;
			}
		}
		catch (IOException e)
		{
			System.out.println("Error reading response body: " + e);
			return;
		}
	}
	/**
	 * Convert response into a string for easy re-sending. Only
	 * converts the response headers, body is not converted to a
	 * string.
	 */
	public String toString()
	{
		String res = "";
		res = statusLine + CRLF;
		res += headers;
		res += CRLF;
		return res;
	}
}