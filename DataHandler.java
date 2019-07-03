
import java.io.*;
import java.nio.ByteBuffer;

/**
 *
 * @author Nicholas Bohm
 * @author Dakota Vanwormer
 */
public class DataHandler {

    private File directory;
    private FileOutputStream fos;
    private FileInputStream fis;
    private ByteArrayOutputStream baos;

    /**
     * Constructs the datahandler for the receiver.
     *
     * @param directory
     */
    public DataHandler(String directory) {
        this.setDirectory(directory);
        try {
            this.fos = new FileOutputStream(this.directory + "\\text.txt");
            this.baos = new ByteArrayOutputStream();

        } catch (FileNotFoundException ex) {
        }
    }

    /**
     * creates the datahandler for the sender.
     *
     * @param directory
     * @param fileName
     */
    public DataHandler(String directory, String fileName) {
        try {
            this.setDirectory(directory);
            this.fis = new FileInputStream(this.directory + "\\" + fileName);
        } catch (FileNotFoundException ex) {
        }
    }

    /**
     * creates the data handler for the packet.
     */
    public DataHandler() {

    }

    /**
     * gets the file length from the file that is being sent.
     *
     * @param fileName
     * @return
     */
    public long getFileLength(String fileName) {
        File hold = new File(this.directory + "\\" + fileName);
        return hold.length();
    }

    /**
     * converts bytes to a file.
     *
     * @param data
     */
    public void ByteToFile(byte[] data) {
        try {

            this.baos.write(data);
            this.baos.writeTo(fos);
            this.baos.reset();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * converts a file into bytes of the given size.
     *
     * @param fileName
     * @param size
     * @return
     */
    public byte[] FileToByte(String fileName, int size) {
        int counter = 0;
        byte[][] store = new byte[size][1];
        try {
            for (int i = 0; i < size; i++) {
                if (this.fis.read(store[i], 0, 1) != -1) {
                    counter++;
                }
            }
            byte[] toReturn = new byte[counter];
            for (int i = 0; i < counter; i++) {
                toReturn[i] = store[i][0];
            }
            return toReturn;
        } catch (FileNotFoundException ex) {
        } catch (IOException io) {
        }
        return null;
    }

    /**
     * converts bytes to ints.
     *
     * @param convert
     * @return
     */
    public int byteToInt(byte[] convert) {
        ByteBuffer buffer = ByteBuffer.allocate(convert.length);
        for (int i = 0; i != convert.length; i++) {
            buffer.put(convert[i]);
        }
        buffer.flip();
        return buffer.getInt();
    }

    /**
     * converts ints to bytes.
     *
     * @param convert
     * @param toAllocate
     * @return
     */
    public byte[] intToByte(int convert, int toAllocate) {
        byte[] dst = new byte[toAllocate];
        ByteBuffer buffer = ByteBuffer.allocate(toAllocate);
        buffer.putInt(convert);
        buffer.flip();
        buffer.get(dst);
        return dst;
    }

    /**
     * sets the directory of the datahandler.
     *
     * @param path
     */
    public void setDirectory(String path) {
        this.directory = new File(path);
    }
}
