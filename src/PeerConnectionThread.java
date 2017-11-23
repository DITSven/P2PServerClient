import java.net.*;
import java.util.ArrayList;
import javax.swing.*;

public class PeerConnectionThread extends Thread{
	JTextArea responseWindow;
	ServerSocket peerServer;
	Socket peerServerClient;
	ArrayList<Socket> clientSockets;
	ArrayList<String[]> peerList;
	ArrayList<Integer> distanceToNextPeerList;
	ArrayList<String[]> peersToConnectTo;
	int peerID, peerLocation;
	String serverLine, clientLine;
	
	public PeerConnectionThread(JTextArea responseWindow, ServerSocket peerServer, int peerID, ArrayList<String[]> peerList){
		this.responseWindow = responseWindow;
		this.peerServer = peerServer;
		this.peerID = peerID;
		this.peerList = peerList;
		clientSockets = new ArrayList<Socket>();
		distanceToNextPeerList = new ArrayList<Integer>();
		peersToConnectTo = new ArrayList<String[]>();
	}
	
	private void getDistanceToNextPeerList() {
		synchronized(peerList){
			if(peerList.size() > 1) {
				int i = 1;
				showMessage("peerList size = " + Integer.toString(peerList.size()) + "\n");
				while (i < peerList.size()) {
					distanceToNextPeerList.add((Integer)i);
					showMessage("Added to distanceToNextPeerList: " + Integer.toString(i) + "\n");
					i = i*2;
				}
				showMessage("Distance to next peers calculated\n");
			}
		}
	}
	
	private void findPeerIDLocationInList() {
		for (int i = 0; i < peerList.size(); i++) {
			String[] temp = peerList.get(i);
			if(temp[0] == Integer.toString(peerID)) {
				peerLocation = i;
				break;
			}
		}
		showMessage("Found peerID Location in list\n");
	}
	
	private void choosePeersToConnectTo() {
		synchronized(peerList){
			if (distanceToNextPeerList != null) {
				for (int i = 0; i < distanceToNextPeerList.size(); i++) {
					int nextLocation = (peerLocation + (int)distanceToNextPeerList.get(i)) % peerList.size();
					if(nextLocation == peerLocation) {
						break;
					}
					String[] temp = peerList.get(nextLocation);
					peersToConnectTo.add(temp);
				}
			}
		}
	}
	
	private void openServerThread() {
		PeerConnectionServerThread serverThread = new PeerConnectionServerThread(peerServer, responseWindow);
		serverThread.start();
	}
	
	private void openClientThread() {
		PeerConnectionClientThread clientThread = new PeerConnectionClientThread(clientSockets, peersToConnectTo, responseWindow);
		clientThread.start();
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
	
	public void run() {
		showMessage("PeerConnectionThread started\n");
		getDistanceToNextPeerList();
		findPeerIDLocationInList();
		choosePeersToConnectTo();
		openServerThread();
		openClientThread();
		
	}
}
