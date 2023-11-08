## MiniWebServer
# WebServer project for Distributed Systems 1 CSC435

Web Server school project built with Java and serving a basic HTML page

# Tools and features 
- Java Sockets 
- HTTP protocols 
- Cookies
- Multithreaded

# How it works
- This simple web server serves WebAdd.html
- It will create a profile for each new visitor consisting of name and number of visits
- The server has a database which holds each profile
- Cookies are set when a new client visits, and used to recognize past visitors
- Users of the web form will enter their name and two numbers to be added
- The results of the addition will be returned, and the input values will remain in the input fields
- Users who change their name will update that data in the database
- A returning visitor who has already submitted a form will be greeted with their name, and their name will appear in the input field

# How to run the program
- The program is written to run on a single machine
- You will need MiniWebserver.java and WebAdd.html in the same directory
- In terminal:
  - javac MiniWebserver.java
    - This will compile the program and create .class files
  - java MiniWebserver
    - The server will be listening on port 2540
- In a browser go to localhost:2540/WebAdd.html
- Cookies will need to be enabled for the host in browser settings
- Clear cookie data in the browser and return to the webpage to create a new client (the server must remain active)
  
    

