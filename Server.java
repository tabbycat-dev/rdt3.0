/*
 * Server class 
 *
 * @author Tan P Nguyen
 * 
 */
import java.io.File;
import java.io.IOException;
import java.net.*;

class Server {
    //private static final int AVERAGE_DELAY = 100;//milliseconds

    public static void main(String args[]) throws Exception {
        System.out.println("Server");
        Server server = new Server();
        DatagramSocket serverSocket = new DatagramSocket(5000);
        byte[] receiveData = new byte[104];
        byte[] sendData = new byte[104];
        //byte[] byteChecksum = new byte[1024];
        int payLoadSize = 100;
        Packet current = null;
        Packet rcvPkt = null;
        int expSeq = 0;
        char expSeqCh = (char) 48;
        int actSeq;
        char endFlag = (char) 3;// '\u0003';//ETX
        char startFlag = (char) 2;// '\u0002';//'STX'
        boolean run = true;
        String directory = "C:\\Users\\phuct\\Desktop\\serverFolder\\";
        File file;
        String nameOfFile;
        String msg;
        while (true) {

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            rcvPkt = new Packet(receivePacket.getData());
            nameOfFile = rcvPkt.getDataBlockString();
            file = new File(directory + nameOfFile.trim());
            System.out.println("RECEIVED:" + new String(receivePacket.getData()) + " " + rcvPkt.getDataBlockString());
            System.out.println("Client request to send " + nameOfFile);

            DatagramPacket sendPacket
                      = new DatagramPacket(sendData, sendData.length, IPAddress, port);
//files does not exist
// or corrupted packet ?
            boolean nameNotCorrupt = rcvPkt.isNotCorrupted(receivePacket.getData());
            if ((file.exists() != true)||(!nameNotCorrupt)) {
                if (!nameNotCorrupt){
                    System.out.println("Name is corrupted. Send NAK packet to Client.");
                    msg = "NAK";
                }else{
                    System.out.println("File does not exist. Send NUL packet to Client.");
                    msg = "NUL";
                }
                //SEND packet to CLIENT
                current = new Packet(startFlag, msg.getBytes(), expSeq, endFlag);
                sendPacket.setData(current.makePckt()); //SET data 
                serverSocket.send(sendPacket);
                sendPacket
                          = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
//file exists
            } else {
                nameOfFile = rcvPkt.getDataBlockString();
                DataHandler dataHandler = new DataHandler(directory, nameOfFile.trim());
                long fileSize = (dataHandler.getFileLength(nameOfFile.trim()) / payLoadSize) + 1;
//loop over file                
                for (int i = 0; i <= (fileSize + 1); i++) {

                    if (i != (fileSize + 1)) {
                        byte[] currentPayload = dataHandler.FileToByte(
                                  nameOfFile, payLoadSize);
                        current = new Packet(startFlag, currentPayload, expSeq, endFlag);
                    } else {//if finished, send EOT end of transmission
                        current = new Packet(startFlag, "EOT".getBytes(), expSeq, endFlag);
                        System.out.println("Sent EOT packet");
                    }
//Send file to Client
                    sendPacket.setData(current.makePckt()); //SET data 
                    serverSocket.send(sendPacket);
                    System.out.println("Sent pkt " + i + ":" + current.getSeqChar() + current.getDataBlockString());
                    System.out.println("i: " + i + " in file is sent to Client");

                    while (run) {
                        try {
                            //udt_rcv event: feedback packet from client
                            rcvPkt = null;
                            serverSocket.receive(receivePacket);
                            receiveData = receivePacket.getData();
                            rcvPkt = new Packet(receivePacket.getData());
                            System.out.println("\nRECEIVED: " + rcvPkt.getSeqChar() + new String(receivePacket.getData()));
                            //Check corrupt ACK packet
                            boolean isNotCorruptACK = rcvPkt.isNotCorrupted(receiveData);

                            System.out.println("Server check ACK received isNotCorrupt:" + rcvPkt.isNotCorrupted(receivePacket.getData()));
                            //Waiting for ACK 0
                            boolean hasSequence = (rcvPkt.checkSequence(expSeqCh));

                            if ((isNotCorruptACK) && (hasSequence)) {
                                System.out.println("SUCCESSFUL TRANSFER: " + i);

                                actSeq = (char) rcvPkt.getSeqNo();
                                System.out.println("->ACKs received and right Seq : " + actSeq + expSeqCh + rcvPkt.checkSequence(expSeqCh));
                                //change packet Seq
                                expSeq = server.seqMethod(expSeq);
                                expSeqCh = server.seqMethodChar(expSeqCh);
                                run = false;
                                current = null;
                            } else {// if((!hasSequence)|| (!isACK))

                                if (!isNotCorruptACK) {
                                    System.out.println("TRANSFER ERROR: Corrupt ACK packet");
                                }

                                if (!hasSequence) {
                                    System.out.println("TRANSFER ERROR: Wrong ACK sequence");
                                }

                                System.out.println("-Retransmit.........");
//                              current = new Packet(startFlag, currentPayload, expSeq, endFlag);
                                sendPacket.setData(current.makePckt());
                                serverSocket.send(sendPacket);
                                System.out.println("Sent Current" + " " + expSeq + current.getDataBlockString());
                                
                            }
                        } catch (IOException ex) {
                        }
                    }
                    System.out.println("Run: " + run + " .Next packet..");
                    run = true;
                }

                //finish sending all
                System.out.println("Sender is finished. Thank you!");
                //serverSocket.close();
                break;

            }

        }

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
