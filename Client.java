import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
class Client {
  public static void main(String args[]) throws Exception
  {
    String action = args[0];
    String serverName = args[1];
    int serveruport = Integer.parseInt(args[2]);
    String localFileName = args[3];
    String remoteFileName = args[4];
    if (action.equals("get"))
    {
      localFileName = args[4];
      remoteFileName = args[3];
    }

    //BufferedReader bu = new BufferedReader(new InputStreamReader(System.in));
    DatagramSocket clientSocket = new DatagramSocket();
    InetAddress serverIP = InetAddress.getByName(serverName);
    byte[] sData = new byte[256];
    byte[] rData = new byte[4096];
    // send nothing
    String tmp = "";
    sData = tmp.getBytes();
    DatagramPacket sPacket = new DatagramPacket(sData, sData.length, serverIP, serveruport);
    clientSocket.send(sPacket);
    DatagramPacket rPacket = new DatagramPacket(rData, rData.length);
    clientSocket.receive(rPacket);
    // handle receivedData
    String listofFiles =  new String(rPacket.getData());
    String[] arrayoffiles = listofFiles.split(" ");
    String servertport = arrayoffiles[0];
    System.out.println("Stage1 finished: list of files received from server.");
    clientSocket.close();
    // end of negotiation phase



    // set up TCP Connection
    Socket client = new Socket(serverName, Integer.parseInt(servertport));
    OutputStream outToServer = client.getOutputStream();
    DataOutputStream out = new DataOutputStream(outToServer);
    InputStream inFromServer = client.getInputStream();
    DataInputStream in = new DataInputStream(inFromServer);

    if (action.equals("put"))
    {
      // send TCP Request
      out.writeUTF("put " + remoteFileName);
      System.out.println("request going out: put " + remoteFileName);
      String response = in.readUTF();

      if (response.equals("ok"))
      {
        Path path = Paths.get("./" + localFileName);
        byte[] data = Files.readAllBytes(path);
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
        System.out.println("stage2 finished: file sent to server");
        out.writeUTF("close");
      }  
      else
      {
        System.out.println(in.readUTF());
      }
    } else { // get Client
      out.writeUTF("get " + remoteFileName);
      System.out.println("request going out: get " + remoteFileName);
      String response = in.readUTF();
      if (response.equals("ok"))
      {
        File f = new File("./" + localFileName);
        f.createNewFile();
        while (true) {
          String block = in.readUTF();
          if(block.equals("close")) {break;}
          FileWriter fw = new FileWriter(localFileName,true);
          BufferedWriter output = new BufferedWriter(fw);
          output.write(block);
          output.close();
          //System.out.println(block);
        }
        System.out.println("stage2 finished: file copied from server");
      }
      else
      {
        System.out.println(response);
      }


    }
    client.close();


  }
}
