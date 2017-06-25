import java.net.*;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class FileHandler {

    private final int BUF_SIZE = 64*1024;

    public FileHandler () {

    }


    public Metadata readFileToBytes(String filename) throws IOException {

        FileInputStream fis = new FileInputStream(filename);
        Metadata mdata = new Metadata(filename, fis.getChannel().size());
        byte[] buf = new byte[BUF_SIZE];
        int read = 0;
        while ((read = fis.read(buf)) > 0) {
            mdata.addChunk(new Data(buf));
        }
        return mdata;
    }

    public void restoreFile(LinkedList<Data> dataList, String filename) throws FileNotFoundException, IOException, NullPointerException {
        FileOutputStream stream = new FileOutputStream(filename);
        for (Data d : dataList) {
            assert(d.getData() != null);
            stream.write(d.getData());
        }
        stream.close();
    }

    public void serializeMetadata(Metadata o) throws IOException, ClassNotFoundException {
		FileOutputStream fos = new FileOutputStream("metadata/" + o.getFilename() + ".sdi");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
        o.eraseChunks();
		oos.writeObject(o);
		oos.flush();
		oos.close();
		fos.close();
	}

    public void serializeData(Data o) throws IOException, ClassNotFoundException {
        FileOutputStream fos = new FileOutputStream("chunks/" + Long.toString(o.getHashChunk()));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(o);
        oos.flush();
        oos.close();
        fos.close();
    }

	public Hashtable <String,Metadata> recoverMetadata() throws IOException, FileNotFoundException, ClassNotFoundException {
        LinkedList<String> files = sdiFiles("metadata");
        Hashtable <String,Metadata> aux = new Hashtable<String,Metadata>();
        for (String fname : files) {
            FileInputStream fis = new FileInputStream(fname);
    		ObjectInputStream ois = new ObjectInputStream(fis);
            Metadata tmp = (Metadata) ois.readObject();
            aux.put(tmp.getFilename(), tmp);
            fis.close();
    		ois.close();
        }
		return aux;
	}

    public LinkedList<String> sdiFiles(String directory) {
        LinkedList<String> files = new LinkedList<String>();
        File dir = new File(directory);
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".sdi")) {
                files.add(directory + "/" + file.getName());
            }
        }
        return files;
    }

	public Boolean verifyMetadata() throws IOException, FileNotFoundException {
		File file = new File("metadata.dat");
		if(file.exists()) return true;
		return false;
	}
}
