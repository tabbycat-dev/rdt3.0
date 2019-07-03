/*
 * Packet class 
 *
 * @author Tan P Nguyen
 * 
 */
import java.nio.ByteBuffer;

public class Packet {

    private DataHandler dataHandler = new DataHandler();
    private byte startFlag;
    private byte seqNo;
    private byte[] dataBlock = new byte[100];
    private byte checksum;
    private byte endFlag;

    public Packet(char startFlag, byte[] dataBlock, int seqNo, char endFlag) {      //GOOD
        this.startFlag = (byte) startFlag;
        this.seqNo = this.stringToByte(Integer.toString(seqNo));
        this.endFlag = (byte) endFlag;
        //fillHeader(this.endFlag, endFlag);
        if (dataBlock.length == 100) {
            this.dataBlock = dataBlock;
        } else {
            //System.out.println("__**: Please add convert to dataBlock.^^_^^" + dataBlock.length);
            this.dataBlock = this.paddPayLoad(dataBlock);
        }
        fillHeader(this.checksum, this.createSum());
    }

    /**
     * create Checksum
     *
     * @param
     * @return int checksum
     */
    public int createSum() {
        int result = this.calculateSum(this.makePckt());
        return result;//ADD negative SUm
    }

    /**
     * Checksum method calculateSum
     *
     * @param byte[] frame_U
     * @return int checksum
     */
    public int calculateSum(byte[] frame_U) {//calculate SUM for CHECKSUM
        int CRC_POLYNOM = 0x9c;
        int CRC_PRESET = 0xFF;
        int crc_U = CRC_PRESET;
        int x = CRC_PRESET;
        for (int i = 0; i < frame_U.length; i++) {
            if (i == 102) {//checksum is here
                x ^= Byte.toUnsignedInt(frame_U[i]);
                int y = this.byteToInt(frame_U[i]);
                continue;
            }
            crc_U ^= Byte.toUnsignedInt(frame_U[i]);
            for (int j = 0; j < 8; j++) {
                if ((crc_U & 0x01) != 0) {
                    crc_U = (crc_U >>> 1) ^ CRC_POLYNOM;
                } else {
                    crc_U = (crc_U >>> 1);
                }
            }
        }
        return crc_U;
    }

    /**
     * padds the payload of the packet if the packet payload is not 1004.
     *
     * @param beforePadd
     * @return
     */
    public byte[] paddPayLoad(byte[] beforePadd) {                                              //GOOOD                         
        ByteBuffer buffer = ByteBuffer.allocate(100);
        int counter = 0;
        int toAdd = (100 - beforePadd.length);

        byte[] toReturn = new byte[100];
        byte padding = 0;

        buffer.put(beforePadd);
        for (int i = 0; i != (toAdd); i++) {
            buffer.put(padding);
            counter++;
        }
        buffer.flip();
        buffer.get(toReturn);
        return toReturn;
    }
/**
 * COnvert sequence No 0 or 1 for example (char)48 or 49
 * @param s
 * @return byte of s
 */
    public byte stringToByte(String s) {
        //String s = Integer.toString(sx);
        int asciiInt;
        int sum = 0;
        for (int i = 0; i < s.length(); i++) {
            asciiInt = (int) s.charAt(i);
            sum = sum + asciiInt;
        }
        byte x = (byte) sum;
        char ch = (char) x;
        return x;
    }

    /**
     *
     * @param pckt
     */
    public Packet(byte[] pckt) {//for receive                                                    //Error for sure

        this.breakPacket(pckt);
        this.dataBlock = new byte[100];
        this.extractDataBlock(pckt);
    }

    /**
     * extracts packets payload from raw recived packet
     *
     * @param pcktContents
     * @return
     */
    public void extractDataBlock(byte[] pcktContents) {                                         //ERROR for sure
        ByteBuffer buffer = ByteBuffer.allocate(100);
        int start = 2;
        for (int i = 0; i != 100; i++) {

            buffer.put(pcktContents[start]);
            start++;
        }
        buffer.flip();
        buffer.get(this.dataBlock);
//        System.out.println("__-->ExtractdataBlock: dataBlock: " + new String(this.dataBlock));
        buffer.clear();
    }

    /**
     * breaks a packets header apart from raw packet.
     *
     * @param pcktContents
     * @param target
     */
    public void breakPacket(byte[] pcktContents) {         
        for (int i = 0; i != 104; i++) {
            if (i == 0) {
                this.startFlag = pcktContents[i];
            }
            if (i == 1) {
                this.seqNo = pcktContents[i];
            }
            if (i == 102) {
                this.checksum = pcktContents[i];
            }
            if (i == 103) {
                this.endFlag = pcktContents[i];
            }
        }
    }

    /**
     * makes a packet from the data given.
     *
     * @return
     */
    public byte[] makePckt() {                                                         //GOOD
        byte[] toReturn = new byte[104];
        ByteBuffer buffer = ByteBuffer.allocate(104);
        buffer.put(this.startFlag);
        buffer.put(this.seqNo);
        buffer.put(this.dataBlock);
        buffer.put(this.checksum);
        buffer.put(this.endFlag);
        buffer.flip();
        buffer.get(toReturn);
        buffer.clear();
        return toReturn;
    }

    /**
     * fills the header of the packet with the needed data that is given.
     *
     * @param target
     * @param dataToConvert
     */
    public void fillHeader(byte target, int dataToConvert) {
        if (target == this.checksum) {
            this.checksum = (byte) dataToConvert;
        }
    }

    public byte getStartFlag() {
        return this.startFlag;
    }

    public int getStartFlagInt() {
        return (int) this.startFlag;
    }

    public byte[] getDataBlock() {
        return this.dataBlock;
    }

    public String getDataBlockString() {
        return new String(this.dataBlock);
    }

    public byte getChecksum() {
        return this.checksum;
    }

    public int getChecksumInt() {
        int CRC_PRESET = 0xFF;
        int x = CRC_PRESET;
        x ^= Byte.toUnsignedInt(this.checksum);
        return x;
    }

    public byte getSeqNo() {
        return seqNo;
    }

    public char getSeqChar() {
        //return (int)seqNo;
        char ch = (char) this.seqNo;
        return ch;
    }

    public boolean checkSequence(char expSeqCh) {
        if (expSeqCh == this.getSeqChar()) {
            return true;
        }
        return false;
    }

    public char getEndChar() {
        //return (int)seqNo;
        char ch = (char) this.endFlag;
        return ch;
    }

    public byte getEndFlag() {
        return this.endFlag;
    }

    public String getEndFlagString() {
        String str = new Character((char) this.endFlag).toString();
        return str;
    }

    public int getEndFlagInt() {
        return (int) this.endFlag;
    }

    public int seqMethod(int expSeq) {
        if (expSeq == 1) {
            return 0;
        } else {
            return 1;
        }
    }

    public boolean isNotCorrupted(byte[] frame_U) {//IS NOT CORRUPTED????                               ///ERROR with Byte.toUnasignInteger()
        int CRC_POLYNOM = 0x9c;                                                                         //Byte.toUnsignedInt
        int CRC_PRESET = 0xFF;
        int crc_U = CRC_PRESET;
        int x = CRC_PRESET;
        for (int i = 0; i < frame_U.length; i++) {
            if (i == 102) {//checksum is here
                x ^= Byte.toUnsignedInt(frame_U[i]);

                continue;
            }
            crc_U ^= Byte.toUnsignedInt(frame_U[i]);
            //System.out.println("i:"+i+ ";"+frame_U[i]);

            for (int j = 0; j < 8; j++) {
                if ((crc_U & 0x01) != 0) {
                    crc_U = (crc_U >>> 1) ^ CRC_POLYNOM;
                    //System.out.println("j: "+j+ ";"+crc_U);
                } else {
                    crc_U = (crc_U >>> 1);
                }
            }
        }
        if (crc_U + x == 255) {
            return true;
        } else {
            return false;
        }
    }

    public int calculateCRC8(byte[] frame_U) {//IS NOT CORRUPTED????
        int CRC_POLYNOM = 0x9c;
        int CRC_PRESET = 0xFF;
        int crc_U = CRC_PRESET;
        for (int i = 0; i < frame_U.length; i++) {

            crc_U ^= Byte.toUnsignedInt(frame_U[i]);

            for (int j = 0; j < 8; j++) {
                if ((crc_U & 0x01) != 0) {
                    crc_U = (crc_U >>> 1) ^ CRC_POLYNOM;
                } else {
                    crc_U = (crc_U >>> 1);
                }
            }
        }
        return crc_U;

    }

    public int byteToInt(byte b) {
        int CRC_PRESET = 0xFF;
        int x = CRC_PRESET;
        x ^= Byte.toUnsignedInt(b);
        return x;
    }

    public byte intToByte(int number) {
        return (byte) number;
    }
}
