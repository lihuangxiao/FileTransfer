import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
class Server {
  public static void main(String args[]) throws Exception
  {

    while(true) {
    //select a random uport
    DatagramSocket serverSocket = new DatagramSocket(0);
    ServerSocket TCPServerSocket = new ServerSocket(0);
    int tport = TCPServerSocket.getLocalPort();

    InetAddress localIP = InetAddress.getLocalHost();
    int uport = serverSocket.getLocalPort();
    System.out.println("Waiting on: " + localIP.toString() + ":" + uport);
    byte[] rData = new byte[256];
    byte[] sData = new byte[4096];

    DatagramPacket rPacket = new DatagramPacket(rData, rData.length);
    serverSocket.receive(rPacket);
    String message = new String(rPacket.getData());

    InetAddress remoteIP = rPacket.getAddress();
    int remoteuport = rPacket.getPort();
    System.out.println("Received packet from CLient: " + remoteIP + ":" + String.valueOf(remoteuport));

    String fileNames = String.valueOf(tport);
    fileNames += " ";

    //assumes home directory to be current directory.
    File folder = new File("./");
    File[] listOfFiles = folder.listFiles();
    for (File f : listOfFiles) {
      fileNames += f.toString().substring(2,f.toString().length());
      fileNames += " ";
    }

    sData = fileNames.getBytes();
    DatagramPacket sPacket = new DatagramPacket(sData, sData.length, remoteIP, remoteuport);
    serverSocket.send(sPacket);
    // end of negotiation stage


      Socket server = TCPServerSocket.accept();
      DataInputStream in = new DataInputStream(server.getInputStream());
      DataOutputStream out = new DataOutputStream(server.getOutputStream());


      String input = in.readUTF();
      String[] inputArray = input.split(" ");
      String action = inputArray[0];
      String fileName = inputArray[1];


      if (action.equals("put"))
      {
        File f = new File("./"+fileName);
        if(!f.createNewFile()) {
          out.writeUTF("error fail to create file");
          continue;
        } else {
          out.writeUTF("ok");
        }
        while (true) {
          String block = in.readUTF();
          if(block.equals("close")) {break;}
          FileWriter fw = new FileWriter(fileName,true);
          BufferedWriter output = new BufferedWriter(fw);
          output.write(block);
          output.close();
          //System.out.println(block);
        }
      } else {

        boolean exist = false;
        String[] ArrayofFiles = fileNames.split(" ");
        for (String filename:ArrayofFiles) {
          if (fileName.equals(filename)) {
            exist = true;
          }
        }

        if (!exist) {
          out.writeUTF("error fail to locate file");
          continue;
        } else { // found a file, ready to work
          Path path = Paths.get("./" + fileName);
          byte[] data = Files.readAllBytes(path);
          if (data == null) {
            out.writeUTF("error fail to open file");
            continue;
          } else { //work
            out.writeUTF("ok");

            int datasize = data.length;
            for (int i = 0; i < datasize; i += 256)
            {
              String block = "";
              if (i + 255 < datasize)
              {
                for (int j = i; j <= i + 255; j++) {
                  block += (char)(data[j] & 0xFF);
                }
              }
              else {
                for (int j = i; j < datasize; j++) {
                  block += (char)(data[j] & 0xFF);
                }
              }
              out.writeUTF(block);
            }
            out.writeUTF("close");




          }



        }
      }

      }

      //server.close(); 

  }
}
