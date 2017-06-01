import java.net.*;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.util.*;
import java.io.*;

import broadcast.BroadcastServer;
import broadcast.Peer;


class Client {
	private static LinkedList<String> peers;
	private static LinkedList<Metadata> metadados;
	private static String addr;
	private static BroadcastServer service;

	public Client () {
		peers = new LinkedList<>();
		metadados = new LinkedList<>();
	}

	public static void main(String[] args) {
		try {
			URL url = new URL("http://127.0.0.1:9876/p2pfs?wsdl");
			QName qname = new QName("http://broadcast/", "BroadcastServerImplService");
			addr = getIp();
			Service ws = Service.create(url, qname);
			service = ws.getPort(BroadcastServer.class);
			if(service.helloPeer(addr)) {
				System.out.println("Oi");
			}
			// peers = service.getPeers("teste");
			new Thread (getPeerList).start();
			readFileToBytes(service);
			while(true);

			// service.byePeer("teste");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void readFileToBytes(BroadcastServer service)  throws IOException {
		RandomAccessFile raf = new RandomAccessFile("/tmp/teste.txt", "r");
		Metadata m = new Metadata("teste", raf.length());
		long sizePerChunk = raf.length()/3;
		long remainingBytes = raf.length() % 3;
		long maxReadBytes = 64 * 1024; // 8KB
		for(int i = 1; i <= 3; i++) {
			if(sizePerChunk < maxReadBytes) {
				long numReads = sizePerChunk / maxReadBytes;
				long numRemainingReads = sizePerChunk % maxReadBytes;
				for(int j = 0; j <numReads; j++) {
					byte[] d = readWrite(raf, maxReadBytes);
					m.addChunk(new Data(d));
				}
				if(numRemainingReads > 0) {
					byte[] d = readWrite(raf, numRemainingReads);
					m.addChunk(new Data(d));
				}
			} else {
				byte[] d = readWrite(raf, sizePerChunk);
				m.addChunk(new Data(d));
			}

		}
		if(remainingBytes > 0) {
			byte[] d = readWrite(raf, remainingBytes);
			m.addChunk(new Data(d));
		}
		for(int i = 0; i < m.getChunks().size(); i++)
			System.out.println(new String(m.getChunks().get(i).getData(), "UTF-8"));
		raf.close();
	}

	static byte[] readWrite(RandomAccessFile raf, long numBytes) throws IOException {
		byte[] buf = new byte[(int) numBytes];
		int val = raf.read(buf);
		if(val != -1) return buf;
		return buf;
	}

	private static Runnable getPeerList = new Runnable () {
		public void run() {
			try {
				while (true) {
					peers = service.getPeers(addr);
					System.out.println(peers.toString());
					Thread.sleep(30000);
				}
			} catch (Exception e) {}
		}
	};

	public static String getExtIp() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	public static String getIp() throws Exception {
		String ret = null;
		try {
            InetAddress ipAddr = InetAddress.getLocalHost();
			ret = ipAddr.getHostAddress();
            System.out.println(ret);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
		return ret;
	}

}
