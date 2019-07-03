/*
 * Client class 
 *
 * @author Tan P Nguyen
 * 
 */
import java.io.*;
import java.net.*;

class Client {

    public static void main(String args[]) throws Exception {
        System.out.println("Client");

        System.out.println("Please enter name of file you'd like to request in Server");
        Client client = new Client();
        BufferedReader inFromUser
                  = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] sendData = new byte[104];
        byte[] receiveData = new byte[104];
        boolean end = false;
        int count = 0;
        Packet ackPkt;
        Packet current;
        String ACKStr = "ACK";
        byte[] ACKData = ACKStr.getBytes();
        Packet prvs;
        int expSeq = 0;
        int prvSeq = 1;
        char expSeqCh = (char) 48;
        char prvSeqCh = (char) 49;
        char endFlag = (char) 3;
        char startFlag = (char) 2;
        long timeStart;
        long start;
        boolean timeout = true;
        char actSeqCh;
        int payLoadSize = 100;
        String fileName = inFromUser.readLine();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4000);
        current = new Packet(startFlag, fileName.getBytes(), expSeqCh, endFlag);
        sendPacket.setData(current.makePckt());
        //receiveData = new byte[104];
        clientSocket.send(sendPacket);
        start = (System.currentTimeMillis());
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        File file = new File("C:\\Users\\phuct\\Desktop\\serverFolder" + fileName);
        file.createNewFile();
        FileWriter fw = new FileWriter(file, true);
        while (!end) {
            //-----------------timeout
            timeStart = (System.currentTimeMillis());
            while (timeout) {
                boolean check = receiveData == null;
                System.out.println("Run timeout: " + timeout + " ,checkData if null: " + check);
                if (receiveData.length > 0) {
                    clientSocket.receive(receivePacket);
                    receiveData = receivePacket.getData();
                    System.out.println("In time!");
                    timeout = false;
                } else if ((System.currentTimeMillis() - timeStart) >= 2) {
                    System.out.println(" Packet Loss. Run timeout: " + timeout);
                    clientSocket.send(sendPacket);
                    timeStart = (System.currentTimeMillis());
                }
            }
            timeout = true;
            //---------------------

            //           clientSocket.receive(receivePacket);
            //           receiveData = receivePacket.getData();
            Packet receiveDataPacket = new Packet(receiveData);

            //CHECK Corrupt(rcvpkt)~flip bit?checksum check?
            boolean isNotCorrupt = receiveDataPacket.isNotCorrupted(receiveData);
            //CHECK received Seq No = 0?
            boolean hasSequence = receiveDataPacket.checkSequence(expSeqCh);//does packet have right sequence
            System.out.println("\nFROM SERVER: " + new String(receivePacket.getData()));
            if (isNotCorrupt && hasSequence)//NOT CORRUPT AND SEQ =0  ACK
            {
                String sentence = receiveDataPacket.getDataBlockString();
                //make sure file exists in Server
                if ((sentence.contains("NUL")) || (sentence.contains("NAK"))) {
                    if (sentence.contains("NUL")) {
                        System.out.println("ERROR. File not found. Please enter another filename");
                        fileName = inFromUser.readLine();
                        current = new Packet(startFlag, fileName.getBytes(), expSeqCh, endFlag);
                    }else{
                        System.out.println("Name of corrupted. Resend to Server. ");
                    }

                    sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 4000);
                    
                    sendPacket.setData(current.makePckt());
                    //receiveData = new byte[104];
                    clientSocket.send(sendPacket);

                    file = new File("C:\\Users\\phuct\\Desktop\\clientFolder\\" + fileName);
                    file.createNewFile();
                    fw = new FileWriter(file, true);

                } else if (!sentence.contains("EOT")) {//not end of transmission
                    fw.append(receiveDataPacket.getDataBlockString());
                    System.out.println("-SUCCESSFUL.Write to file: " + receiveDataPacket.getDataBlockString());
                    fw.flush();
                    current = new Packet(startFlag, ACKData, expSeq, endFlag);
                    //ACK is true
                    //receiveData = new byte[104];
                    sendPacket.setData(current.makePckt());
                    //             System.out.println("Before sent. Check ACK packet isNotCorrupt:" + current.isNotCorrupted(current.makePckt()));
                    clientSocket.send(sendPacket);
                    System.out.println("Sent ACK" + expSeq);
                    //ackPkt = null;
                    //change Expected Seq
                    prvSeqCh = client.seqMethodChar(prvSeqCh);
                    prvSeq = client.seqMethod(prvSeq);
                    expSeq = client.seqMethod(expSeq);
                    expSeqCh = client.seqMethodChar(expSeqCh);
                    System.out.println("Exp seq now is: " + expSeq + " ,prv Seq" + prvSeq);
                    prvs = receiveDataPacket;
                    receiveDataPacket = null;

                } else {//end of transmission
                    current = new Packet(startFlag, ACKData, expSeq, endFlag);
                    sendPacket.setData(current.makePckt());
                    clientSocket.send(sendPacket);
                    System.out.println("Sent ACK" + expSeq);
                    end = true;
                    System.out.println("End is true!");
                }

            } else //corrupt || (!has_sequence)  (DUPLICATE PACKET)/ NAK
            {
                //corrupt
                if (!isNotCorrupt) {
                    System.out.println("-TRANSFER ERROR: corrupt packet");
                }
                //CHECK DUPLICATE
                if (!hasSequence) {
                    System.out.println("-TRANSFER ERROR: duplicate");
                    System.out.println("Expected Seq: " + expSeq);
                }

                //send NAK
                ACKData = "ACK".getBytes();
                current = new Packet(startFlag, ACKData, prvSeq, endFlag);
                sendPacket.setData(current.makePckt());
                clientSocket.send(sendPacket);
                System.out.println("Sent ACK" + prvSeq + "<-Previous ACK");

            }
        }
        //4.close file
        fw.close(); //close write file
        System.out.println("Close the file.");
        //clientSocket.close(); //close socket 
        System.out.println("Total transmission time: " + ((System.currentTimeMillis() - start)) + "ms");

    }

    public int seqMethod(int expSeq) {
        if (expSeq == 1) {
            return 0;
        } else {
            return 1;
        }
    }

    public char seqMethodChar(char expSeq) {
        if (expSeq == (char) 49) {
            return (char) 48;
        } else {
            return (char) 49;
        }
    }
}
