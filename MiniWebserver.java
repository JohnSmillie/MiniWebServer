
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
    - I have made adjustments to WebAdd.html to account for including the client information.
      It is recommended that you use the version of WebAdd.html included with the submission.
      In fact, my program will not work without the updated webpage. 

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
static int i = 0;
static int clientID = 100;

  public static void main(String a[]) throws IOException {
    int q_len = 6; /* Number of requests for OS to queue */
    int port = 2540;
    Socket sock;
    
    ServerSocket servsock = new ServerSocket(port, q_len);

    System.out.println("John Smillie's MiniWebserver running at 2540.");
    System.out.println("Point Firefox browser to http://localhost:2540/WebAdd.html\n");
    while (true) {
      // the server will be listening on localhost:2540
      // this could be modified to run with command line inputs indicating the IP address and
      // port number for any machine anywhere in the world

      // wait for the next client connection:
      sock = servsock.accept();
      new WebserverWorker (sock, clientID).start();
    }
  }
}

// A WebAddClient is instantiated when the WebAdd.html page is first visited
// The createMyWebAdd method serves up a version of WebAdd.html with an incrementally increasing
// clientID and a number of requests starting at 1 (this request) as part of the form in readonly mode
// In this way, the clientID and current request number are included with the query string upon 
// subsequent requests. 
// An initializeWebClient method could've been used instead of creating an entire class - it would 
// look almost identical to createMyWebAdd. 
// Because I don't have control of the browser, there was no way to assign a cookie so that my server
// could recognize who was submitting input. I had to create the cookie and embed it into the document.
// By using an input field with readonly attributes, I was able to submit the assigned clientID along with 
// the other information into the query string.
// Because of this solution, I new client is created with every visit to WebAdd.html, even coming from the
//same machine.

class WebAddClient{
  int clientID = MiniWebserver.clientID++;
  int requestCount = 1;
  String myWebAdd; 
  
  public WebAddClient(File file){
    this.myWebAdd = createMyWebAdd(file);
  }
  public String createMyWebAdd(File file){
    String myWebAdd = "";
    try{
    BufferedReader reader = new BufferedReader(new FileReader((file)));
    String nextLine = reader.readLine();
    while(nextLine != null){
      if(nextLine.contains("placeHolderID")){
        System.out.println("true");
        nextLine = "<input form=\"form1\" name=\"ID\" id=\"ID\" value=\"" + this.clientID  +"\" style=\"border:none;\" readonly>";
      }
      else if (nextLine.contains("placeHolderRequest")){
        System.out.println("true");
        nextLine = "<input form=\"form1\" name=\"Request\" id=\"Request\" value=\"" + this.requestCount + "\" style=\"border:none;\" readonly>";
      }
      myWebAdd += nextLine;
      nextLine = reader.readLine();
    }
    reader.close();
    }catch(IOException e){e.printStackTrace();}
    //System.out.println(myWebAdd);
    return myWebAdd;
  }
}


class WebserverWorker extends Thread {    // Class definition
  Socket sock;                   // Class member, socket, local to ListnWorker.
  WebserverWorker (Socket s, int clientID) {sock = s;} // Constructor, assign arg s to local sock
  
  public void run(){
    PrintStream out = null;   // Input from the socket
    BufferedReader in = null; // Output to the socket

    // currently this system is set up to serve WebAdd.html
    // however, with a little string editing to the request line, this could
    // serve any file in our directory
    File file = new File("WebAdd.html");
    try {
      out = new PrintStream(sock.getOutputStream()); // instantiate the output stream
      in = new BufferedReader(new InputStreamReader(sock.getInputStream())); // instantiate the input stream
      // read in the request line from the user containing the pertinent info to serve the webpage
      String firstLine = in.readLine();
      // in house tracking of total server requests
      System.out.println("Sending the HTML Reponse now: " + Integer.toString(MiniWebserver.i++) + "\n" );
      // respond accordingly to the request 
      // bad request
      if(!firstLine.startsWith("GET") || 
      !(firstLine.endsWith("HTTP/1.1") || firstLine.endsWith("HTTP/1.0"))){
        out.println("HTTP/1.0 400 Bad Request");
        out.println("Connection: close");
        out.println("Content-Length: 400"); 
        out.println("Content-Type: text/html \r\n\r\n");
        out.println("<h1>400 Bad Request</h1>");
      }
      // attack request - trying to access the file system
      else if(firstLine.contains("..") || 
        firstLine.substring(0, firstLine.length()-9).equals("~")){
        out.println("HTTP/1.0 403 Forbidden Request");
        out.println("Connection: close");
        out.println("Content-Length: 400"); 
        out.println("Content-Type: text/html \r\n\r\n");
        out.print("<h1>403 Forbidden Request</h1>");
      }
      // webpage request - only able to server WebAdd.html in this iteration
      else if(firstLine.contains("WebAdd.html")){
        // Instantiate a WebAddClient whose sole purpose is to create a unique version
        // of WebAdd.html
        // It is unique due to the embedded clientID and request count created by the class
        // This ensures that subsequent requests from the server will recognize the client
        //WebAddClient client = new WebAddClient(file);
        // myWebAdd will be the unique version (as String) of the WebAdd.html page for the 
        // visiting client
        byte[] fileBytes = Files.readAllBytes(file.toPath());                  //client.myWebAdd.getBytes();
        sendHTML(fileBytes, out);
      }
      // input submission request - produce the updated webpage
      else if(firstLine.contains("WebAdd.fake-cgi")){
        //System.out.println(firstLine);
        updateAndPrintWebAdd(file, firstLine, out);
      }
      // other type request
      else{
        out.println("HTTP/1.0 404 Not Found");
        out.println("Connection: close");
        out.println("Content-Length: 400"); 
        out.println("Content-Type: text/html \r\n\r\n");
        out.println("<h1>404 Not Found</h1><p>Your request is not valid</p>");
      }
  
	 
	
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
  public void updateAndPrintWebAdd(File file, String line, PrintStream out){
    List<String> parsed = parseRequest(line);
    String name = "value=\"" + parsed.get(0) + "\"";
    String num1 = "value=\"" + parsed.get(1) + "\"";
    String num2 = "value=\"" + parsed.get(2) + "\"";
    //String clientID = "value=\"" + parsed.get(3) + "\"";
    //int nextRequest = Integer.parseInt(parsed.get(4))+1;
    //String requestCount = "value=\"" + Integer.toString(nextRequest) + "\"";
    int result = Integer.parseInt(parsed.get(1)) + Integer.parseInt(parsed.get(2));
    
    try{
        BufferedReader buffer = new BufferedReader(new FileReader(file));
        String nextLine = buffer.readLine();
        String printString = "";
        
        while(nextLine != null){
          if(nextLine.contains("value=\"YourName\"")){
            nextLine = "<p><INPUT TYPE=\"text\" NAME=\"person\" size=20 " + name + "</p>";
          }
          else if(nextLine.contains("value=\"4\"")){
            nextLine = "<p><INPUT TYPE=\"text\" NAME=\"num1\" size=5 " + num1 + "></p>"; 
          }
          else if(nextLine.contains("value=\"5\"")){
            nextLine = "<p><INPUT TYPE=\"text\" NAME=\"num2\" size=5 " + num2 + "> </p><br>";
            nextLine += "Result: " + result + "</p><br>";
          }
          /*else if(nextLine.contains("value=\"placeHolderID\"")){
            nextLine = "<input form=\"form1\" name=\"ID\" id=\"ID\"" +  clientID +  " style=\"border: none;\" readonly></p>";
          }
          else if(nextLine.contains("value=\"placeHolderRequest\"")){
            nextLine = "<input form=\"form1\" name=\"Request\" id=\"Request\"" +  requestCount + " style=\"border: none;\" readonly></p>";
          }*/
          
          printString += nextLine;
          nextLine = buffer.readLine();
        }
        
        //out.println(printString);
        byte[] fileBytes = printString.getBytes();
        sendHTML(fileBytes, out);
        buffer.close();
       
        
    }catch(Exception e){e.printStackTrace();}
    
  }

  // attach the HTTP header and send the byte array over the output stream
  // this is going back to the browser 
  public void sendHTML(byte[] fileAsBytes, PrintStream out){
      out.println("HTTP/1.1 200 OK");
      out.println("Connection: close");
      //MIME types:
      int len = fileAsBytes.length;
      out.println("Content-Length: " + Integer.toString(len)); 
      out.println("Content-Type: text/html \r\n\r\n");
      try{
        out.write(fileAsBytes);
      }catch(IOException e){}
  }

  
  // parse the query string
  public List<String> parseRequest(String line){
    List<String> list = new ArrayList<>();
    int startPerson = line.indexOf("person=");
    int startNum1 = line.indexOf("num1=");
    int startNum2 = line.indexOf("num2=");
    int startID = line.indexOf("ID=");
    //int startCount = line.indexOf("Request=");
    int endCount = line.indexOf("HTTP/1.1")-1;
    list.add(line.substring(startPerson+7, startNum1 - 1));
    list.add(line.substring(startNum1+5, startNum2-1));
    list.add(line.substring(startNum2+5, endCount)); //startID-1
    //list.add(line.substring(startID+3, startCount-1));
    //list.add(line.substring(startCount+8, endCount));
    
    return list;
  }
}


/*

Discussion Post 1:

Hello Team!

This is my first web server build, and I'm really enjoying the process. 
The relevance of a web server in today's world is no secret to those of us in this class. 
I've faced many hurdles along the way, which has lead me to a gratifying learning experience. 

The premier question I had was: how does my server receive a request from the browser and 
return an HTML file (or any type of file) to the browser to be displayed? There were several 
steps to uncovering the answer[s] to this question. 

First, by running MyListener, I was able to see what the browser would be sending to the server 
when I visited the site (which at this point was simply localhost:2540/abc). I saw that the 
browser was sending over an HTTP GET request that looked like:

GET /abc HTTP/1.1

Host: localhost:2540

...

Next, by running MyTelnetClient, I was prompted to send my own GET request (similar to the above request) 
to Dr. Elliott's server at DePaul. What I received back from the server looked something like this:

HTTP/1.0 400 Bad Request

Connection: close

Content-Length: 400

Content-Type: text/html

This alerted me to the exchange of HTTP headers that takes place between the browser and the server. 
None of this was a huge surprise, but the entire exchange was happening in the console. I still found 
myself with the same question: how do I get the .html file on my machine (the server) back to the browser? 
I could see that the "what" was laid out for me - I just needed to parse out the request string. But, 
I couldn't figure out the "how".

The WebResponse program illuminated a few key features - primarily the InputStream and OutputStream 
that would be key to the data transfer. Then, I did a bit more research, and found a few helpful links. 
The 150 line web server provided in the MyWebserver  Assignment was immensely helpful on many fronts. 
The fact is, any data sent between the server and the browser would be no more than a stream of bytes. 
This is why the HTTP header, and hence, MIME-types are fundamental to this exchange. They will alert 
the browser to the type of data it will subsequently be receiving from the server. I realized that I 
would need to render my file as an array of bytes to be sent through the data link layer. I found this 
website provided several different ways to accomplish this.

By combining the parsed request string from the browser, the HTTP headers with the MIME-type, the 
InputStream/OutputStream, and the file as an array of bytes, I now had everything I needed to answer 
my initial question. And, it worked!

My second question required a lot more time and lots of trial and error: how do I take the submitted 
form input from WebAdd.html and return a new version of the original .html file now with different 
information?  And how do I accomplish this with every form submission?

More on this later...

P.S. if anyone is working on the MiniWebserver assignment and wants to chat about it please reach out. 
 


Discussion Post 2:

Continuing where my previous post left off, it was my goal that the server be able to:

    - Host the WebAdd.html page and receive input from the user as an HTML form. 
    - Return a modified version of the webpage with the user-entered name and numbers in the input fields, 
      as well as the result of the addition of the two entered numbers.
    - Allow for continuing user-submissions of the form.
    - Assign a Client ID to each visitor and track that client's number of requests. 

Part 1 was trivial since I had already discovered how to display the webpage when visited, and the form was 
already written into the provided WebAdd.html. Thus, I was all setup to receive input from the user. 
The query string sent with the user submission contained the data I needed to update to page, so I wrote a 
method to parse the query string. 

Parts 2 and 3 essentially required the same solution. Hence, once I had solved for part 2, part 3 was inherently
working. At first, my approach to this solution was askew. I made several attempts where I returned a modified 
version of WebAdd.html such that all new visitors to the page would be viewing the modified version, and not 
the original version. I was too focused on modifying the HTML document itself, and not the contents of the 
document. The solution I found for this was to program my server to read in the contents of the HTML document, 
and while doing so modify the contents to include the parsed data, and then output the modified contents of the 
webpage (and not the file itself). Thus, my WebAdd.html page remained the same, but at each user submission my 
server was returning updated content to the browser. (Note: it should be recognized that there are an abundance 
of technologies to accomplish this task)

Finally, surmounting part 4 took a bit of thought. Essentially, I needed a way to assign a cookie to the visitor
so that my server would recognize who was submitting the form. The assignment suggests looking to the ColorServer 
and JokeServer assignments as a reference to assign unique IDs to the client. I was perplexed by this approach, 
because in those examples we were programming the client and had full control over their actions. However, I was 
now working with the browser as the client, and I have no programming control over the browser. But, continuing 
on with the suggestion, I resolved to instantiate a WebAddClient when the webpage was first visited by a user. 
This client would have an ID and a number of requests. [See aside below] My thinking then revolved around including 
user data along with the query string so my server would recognize who was submitting. I modified the web form to 
include a readonly attribute field containing the client ID and number of requests, and created a unique rendering 
of webpage for each new client. In this manner, every submission of the form would provide me with the client ID 
and request number as a key/value pair. I now had everything I needed to track the client and update their number 
of requests. The remainder of the solution was similar to parts 2 and 3.

(Aside: as I was writing this submission it occurred to me that there must be a way for me to assign a cookie to a 
visitor. The solution is in HTTP headers. Live and learn.)

Thanks for reading. 

*/

