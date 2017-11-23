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
		getPeersThread = new GetPeersThread(serverClient, responseWindow, peerList, peerID, peerServerPort);
		getPeersThread.start();	
		peerList.addAll(getPeersThread.getPeerList());
		while(true) {
			if(!getPeersThread.isAlive()) {
				showMessage("thread dead\n");
				try {
					connectToPeers();
					break;
				}
				catch(Exception e) {
					continue;
				}
			}	
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
	
	private void connectToPeers() {
		try {
			showMessage("Attempting to open peer server socket\n");
			peerClient = peerServer.accept();
			peerConnectionThread = new PeerConnectionThread(responseWindow, peerClient, Integer.parseInt(peerList.get(peerList.size() - 1)[0]), peerList);
			peerConnectionThread.start();
		} 
		catch (IOException e) {
			e.printStackTrace();
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
