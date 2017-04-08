JCC = javac

default: Server.class Client.class

Server.class: Server.java
	$(JCC) Server.java

Client.class: Client.java
	$(JCC) Client.java

clean: 
	$(RM) *.class
