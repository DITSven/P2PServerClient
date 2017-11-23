import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import java.io.*;

@SuppressWarnings("serial")
public class ServerClient extends JFrame{
	JTextArea responseWindow;
	ServerSocket peerServer;
	Socket peerClient, serverClient;
	BufferedReader serverClientInput;
	ObjectInputStream serverClientInputObject;
	PrintWriter serverClientOutput;
	GetPeersThread getPeersThread;
	PeerConnectionThread peerConnectionThread;
	ArrayList<String[]> peerList;
	String peerID, responseLine;
	int peerServerPort, peerClientPort, peerIDNumber;
	
	public ServerClient() {
		peerList = new ArrayList<String[]>();
		responseWindow = new JTextArea();
		initServerClient();
	}
	
	private void runPeerDiscovery() {
		synchronized (peerList) {
		getPeersThread = new GetPeersThread(serverClient, responseWindow, peerList, peerID, peerServerPort);
		getPeersThread.start();	
		peerList.addAll(getPeersThread.getPeerList());
		}
	}
	
	private void reservePeerServerPort() {
		peerServerPort = 49152;
		while(true) {
			try {
				peerServer = new ServerSocket(peerServerPort);
				showMessage("Peer Server running on port: " + Integer.toString(peerServerPort) + "\n");
				break;
			} 
			catch (IOException e) {
				if(peerServerPort <= 65535) {
					peerServerPort++;
				}
				else {
					showMessage("Out of available ports!!!!\n");
					break;
				}
			} 
		}
	}
	
	@SuppressWarnings("static-access")
	private void connectToPeers() {
			synchronized (peerList) {
				try {
					peerConnectionThread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				showMessage("peerList size pre thread call: " +  Integer.toString(peerList.size()) + "\n");
				peerConnectionThread = new PeerConnectionThread(responseWindow, peerServer, Integer.parseInt(peerList.get(peerList.size() - 1)[0]), peerList);
				peerConnectionThread.start();
			}
	}
	
	private void initServerClient() {
		add(new JScrollPane(responseWindow));
		setSize(600,160);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				System.exit(0);
		    }
		});
		reservePeerServerPort();
		runPeerDiscovery();
		connectToPeers();
	}
	
	private void showMessage(final String text){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					responseWindow.append(text);
				}
			}
		);
	}

	
}
