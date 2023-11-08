
/*
1. Name: John W. Smillie
 
2. Date: 2023-11-4
 
3. Java version:  "20.0.2" 2023-07-18
   - Java(TM) SE Runtime Environment (build 20.0.2+9-78)

 
4. Precise command-line compilation examples / instructions:
 
    > javac MiniWebserver.java
    > javac MiniWebserver.java
    
5. Precise examples / instructions to run this program:
 
    - In terminal:
    
    > java MiniWebserver.java

    - In a browser

      http://localhost:2540/WebAdd.html

    
 
6. Full list of files needed for running the program:
 
    a. MiniWebserver.java
    b. WebAdd.html


7. Notes:
   
8. Thanks:
    - This program uses provided helper code and references from:
        - MiniWebserver Assignment: MyTelnetClient.java, MyListener.java, WebResponse.java
        - HostServer Assignment: HostServer.java
        - MyWebserver Assignment: ReadFiles.java
        - MyWebserver Tips: 150 line web server found here: https://cs.au.dk/~amoeller/WWW/javaweb/server.html
        
    Additionally, I referenced the following pages for help with I/O operations on the files:
        - https://mkyong.com/java/how-to-convert-file-into-an-array-of-bytes/
        - https://www.digitalocean.com/community/tutorials/java-read-file-line-by-line

## Answers to Questions:
1. Explain how MIME-types are used to tell the browser what data is coming:
   - The MIME-type will be a string sent to the browser from the server that accompanies a 
     file sent from the server to the browser. The MIME-type is directly associatd
     with the file extension of the file. Since the data must be sent in raw bytes over the data link layer,
     it is necessary to provide context to the browser of the incoming bytes. This is done by including the
     "Content-type" and the "Content-Length" with the HTTP header sent to the browser from the server. The HTTP header
     precedes the data in the byte stream, so the browser is informed of the type of incoming data, and its length
     before receiving the data itself. In this manner, the browser knows what to expect of the incoming data, and how to 
     process that data. 

2. Explain how you would return the contents of requested actual disk files (web pages) of type HTML (text/html): 
   - As mentioned above, the data must ultimately be sent as bytes through the data link layer. Thus, the browser 
     does not know the context of the information it's receiving, and how to interpret that data. Firstly, to 
     send the browser the contents of the file, it is necessary to render the data of the file into an array of bytes.
     In my program, I used Files.readAllBytes() which takes a file path as an argument, and returns a byte array. I can 
     thank the above link (mkyong.com) for an idea of how this is done. It is also necessary that sockets are used so that the 
     data is sent to the correct IP address/port of the paricipating machines. Once the sockets are established, they 
     are used to create InputStreams and OutputStreams which enable the machines to read in and write out the data between the machines. 
     All of this being accomplished, the machines are able to pass streams of bytes to one another, and hence, the contents
     of a file can be sent as an array of bytes to a browser from a server. However, this is not quite enough - the browser still needs
     instructions on how to interpret the data. The HTTP header (also sent as an array of bytes) preceding the data will include the MIME-type. 
     In the case of HTML files, the MIME-type will be issued as "Content-Type: text/html" which informs the browser to render the incoming data 
     stream as an .html document.  

3. Explain how you would return the contents of requested actual disk files (web pages) of type TEXT (text/plain): 
   - This process for sending the contents of a .txt file to a browser is in many ways identical to sending the .html file as described above.
     The file will need to be rendered as an array of bytes, at which point the server can send the byte array to the browser over the data link layer.
     Once again, the browser will need instructions on how to interpret the incoming data respresented as a stream of bytes. The server will first
     send an HTTP header to the browser. Within this header will be the MIME-type indicating the type of data the browser will be receiving. For a .txt
     file, the MIME-type will appear as "Content-Type: text/plain" which informs the browser to render the incoming data stream as an .txt document.
*/

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class MiniWebserver {
// track the total replies sent from the server
static int replies = 0;
// clientCookie is assigned and incremented with each new client
static int cookieAssignment = 100;
// simple DB to hold the client history - number of requests here
static HashMap<Integer, WebAddClient> clientDB = new HashMap<>();

  public static void main(String a[]) throws IOException {
    
    int q_len = 6; 
    int port = 2540;
    Socket connectionSocket;
    
    ServerSocket servsock = new ServerSocket(port, q_len);

    System.out.println("John Smillie's MiniWebserver running at 2540.");
    System.out.println("Point Firefox browser to http://localhost:2540/WebAdd.html\n");
    while (true) {
      // listener socket
      connectionSocket = servsock.accept();
      new WebserverWorker (connectionSocket).start();
    }
  }
}

class WebAddClient{
  int clientID; 
  int visits;
  String name;
  
  public WebAddClient(){
    this.clientID = MiniWebserver.cookieAssignment++;
    this.visits = 0;
    MiniWebserver.clientDB.put(clientID, this);
  }
}


class WebserverWorker extends Thread {    
  Socket sock; 
           
  WebserverWorker (Socket s) {
    // Connection socket
    sock = s;
  } 
  public void run(){
    PrintStream out = null;   // Input from the socket
    BufferedReader in = null; // Output to the socket
    int cookie = 0; 
    boolean isNewClient = true;       

    File file = new File("WebAdd.html");
    try {
      out = new PrintStream(sock.getOutputStream()); // instantiate the output stream
      in = new BufferedReader(new InputStreamReader(sock.getInputStream())); // instantiate the input stream
      // read in the request line from the user containing the pertinent info to serve the webpage
      String requestLine = in.readLine();
      // read the remainder of the header
      String nextline = in.readLine();
      // check if browser is sending a cookie
      // if no cookie, then we have a new client
      while (nextline.length() > 0){
        if(nextline.contains("Cookie:")){
          cookie = Integer.parseInt(nextline.substring(8, nextline.length()).trim());
          isNewClient = false;
        }
        nextline = in.readLine();
      }
      // respond accordingly to the request 
      // bad request
      if(!requestLine.startsWith("GET") || 
      !(requestLine.endsWith("HTTP/1.1") || requestLine.endsWith("HTTP/1.0"))){
        out.println("HTTP/1.0 400 Bad Request");
        out.println("Connection: close");
        out.println("Content-Length: 400"); 
        out.println("Content-Type: text/html \r\n\r\n");
        out.println("<h1>400 Bad Request</h1>");
      }
      else{ 
        String request = requestLine.substring(5, requestLine.length()-9).trim();
        // attack request - trying to access the file system
        if(request.contains("..") || request.endsWith("~")){
          out.println("HTTP/1.0 403 Forbidden Request");
          out.println("Connection: close");
          out.println("Content-Length: 400"); 
          out.println("Content-Type: text/html \r\n\r\n");
          out.print("<h1>403 Forbidden Request</h1>");
      }
      // unrecongnized file requested
        else if(!request.contains("WebAdd.html")){
          out.println("HTTP/1.0 404 Not Found");
          out.println("Connection: close");
          out.println("Content-Length: 400"); 
          out.println("Content-Type: text/html \r\n\r\n");
          out.println("<h1>404 Not Found</h1><p>Your request is not valid</p>");
        }
      // WebAdd.html visited by new client. 
      // instantiate a new client and server the webpage
      else if(request.equals("WebAdd.html")){
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        if (isNewClient){
          WebAddClient client = new WebAddClient();
          cookie = client.clientID;                
          sendHTMLSetCookie(fileBytes, out, cookie, isNewClient);
        }
        else if (MiniWebserver.clientDB.get(cookie).name != null){
          // include hello message
          includeHelloAndPrintWebAdd(file, out, cookie);
        }
        else{                 
          sendHTML(fileBytes, out);
        }
        //System.out.println(requestLine);
        MiniWebserver.clientDB.get(cookie).visits++;
        System.out.println("Client: " + cookie + " Visits: " + MiniWebserver.clientDB.get(cookie).visits);
      }
      // html form input supplied with the GET request
      else{
          String name = getNameFromInput(request);
          MiniWebserver.clientDB.get(cookie).name = name;
          MiniWebserver.clientDB.get(cookie).visits++;
          System.out.println("Client: " + cookie + " Visits: " + MiniWebserver.clientDB.get(cookie).visits);
          updateAndPrintWebAdd(file, requestLine, out, cookie);
        
      }
    }
      // in house tracking of total server requests
      System.out.println("Total server replies: " + Integer.toString(++MiniWebserver.replies) + "\n" );
      sock.close(); // close this connection, but not the server;
    } catch (IOException x) {
      System.out.println("Error: Connection reset. Listening again...");
    }
  }
  

  // use the excrated A/V pairs to produce the updated webpage
  // value="" in html indicates the placeholder for the form, which is what we want to replace 
  // read in the file line by line looking for the placeholder values and exchange them with the 
  // extracted A/V pairs 
  // use the lines of the file to create a print string to be rendered as a byte array
  public void updateAndPrintWebAdd(File file, String line, PrintStream out, int cookie){
    List<String> parsed = parseRequest(line);
    String name = "value=\"" + parsed.get(0) + "\"";
    String num1 = "value=\"" + parsed.get(1) + "\"";
    String num2 = "value=\"" + parsed.get(2) + "\"";
    int result = Integer.parseInt(parsed.get(1)) + Integer.parseInt(parsed.get(2));
    
    try{
        BufferedReader buffer = new BufferedReader(new FileReader(file));
        String nextLine = buffer.readLine();
        String printString = "";
        
        while(nextLine != null){
          if(nextLine.contains("Enter your name")){
          nextLine = "<p>Thanks " + MiniWebserver.clientDB.get(cookie).name + "!</p><p> Enter two numbers and my program will return the sum:</p>";
          }
          else if(nextLine.contains("NAME=\"person\"")){
            nextLine = "<p><INPUT TYPE=\"text\" NAME=\"person\" size=20 " + name + "</p>";
          }
          else if(nextLine.contains("NAME=\"num1\"")){
            nextLine = "<p><INPUT TYPE=\"text\" NAME=\"num1\" size=5 " + num1 + "></p>"; 
          }
          else if(nextLine.contains("NAME=\"num2\"")){
            nextLine = "<p><INPUT TYPE=\"text\" NAME=\"num2\" size=5 " + num2 + "> </p>";
            nextLine += "<p>Result: " + result + "</p>";
          }
          printString += nextLine;
          nextLine = buffer.readLine();
        }
        
        //out.println(printString);
        byte[] fileBytes = printString.getBytes();
        sendHTML(fileBytes, out);
        buffer.close();
       
        
    }catch(Exception e){e.printStackTrace();}
    
  }

  public void includeHelloAndPrintWebAdd(File file, PrintStream out, int cookie){
    try{
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String nextLine = reader.readLine();
      String printString = "";
      while(nextLine != null){
        if(nextLine.contains("Enter your name")){
          nextLine = "<p>Welcome back " + MiniWebserver.clientDB.get(cookie).name + "!</p><p> Enter two numbers and my program will return the sum:</p>";
        }
        else if(nextLine.contains("NAME=\"person\"")){
            nextLine = "<p><INPUT TYPE=\"text\" NAME=\"person\" size=20 value=\"" + MiniWebserver.clientDB.get(cookie).name+ "\"</p>";
          }
        printString += nextLine;
        nextLine = reader.readLine();
      }
      byte[] stringBytes = printString.getBytes();
      sendHTML(stringBytes, out);
      reader.close();
    }catch(IOException e){e.printStackTrace();}
    
  }

  // attach the HTTP header and send the byte array over the output stream
  // this is going back to the browser 
  public void sendHTMLSetCookie(byte[] fileAsBytes, PrintStream out, int cookie, boolean isNewClient){
      out.println("HTTP/1.1 200 OK");
      out.println("Connection: close");
      out.println("Server: MyMiniWebserver/1.0");
      if(isNewClient){
        out.println("Set-cookie: " + cookie);
      }
      //MIME types:
      int len = fileAsBytes.length;
      out.println("Content-Length: " + Integer.toString(len)); 
      out.println("Content-Type: text/html \r\n\r\n");
      
      try{
        out.write(fileAsBytes);
      }catch(IOException e){}
  }

  public void sendHTML(byte[] stringAsBytes, PrintStream out){
      out.println("HTTP/1.1 200 OK");
      out.println("Connection: close");
      out.println("Server: MyMiniWebserver/1.0");
      
      //MIME types:
      int len = stringAsBytes.length;
      out.println("Content-Length: " + Integer.toString(len)); 
      out.println("Content-Type: text/html \r\n\r\n");
      
      try{
        out.write(stringAsBytes);
      }catch(IOException e){}
  }

  public String getNameFromInput(String line){
    int startPerson = line.indexOf("person=");
    int endPerson = line.indexOf("num1=");
    return line.substring(startPerson+7, endPerson-1);
  }
  // parse the query string
  public List<String> parseRequest(String line){
    List<String> list = new ArrayList<>();
    int startPerson = line.indexOf("person=");
    int startNum1 = line.indexOf("num1=");
    int startNum2 = line.indexOf("num2=");
    int end = line.indexOf("HTTP/1.1")-1;
    list.add(line.substring(startPerson+7, startNum1-1));
    list.add(line.substring(startNum1+5, startNum2-1));
    list.add(line.substring(startNum2+5, end));
    
    
    return list;
  }
}


