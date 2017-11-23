import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import java.io.*;

public class PeerConnectionThread extends Thread{
	JTextArea responseWindow;
	Socket peerServer;
	ArrayList<Socket> clientSockets;
	BufferedReader peerServerInput;
	ArrayList<BufferedReader> peerClientInputList;
	ObjectInputStream peerServerInputObject;
	ArrayList<ObjectInputStream> peerClientInputObjectList;
	PrintWriter peerServerOutput;
	ArrayList<PrintWriter> peerClientOutputList;
	ObjectOutputStream peerServerOutputObject;
	ArrayList<ObjectOutputStream> peerClientOutputObjectList;
	ArrayList<String[]> peerList;
	ArrayList<Integer> distanceToNextPeerList;
	ArrayList<String[]> peersToConnectTo;
	int peerID, peerLocation;
	String serverLine, clientLine;
	
	public PeerConnectionThread(JTextArea responseWindow, Socket peerServer, int peerID, ArrayList<String[]> peerList){
		this.responseWindow = responseWindow;
		this.peerServer = peerServer;
		this.peerID = peerID;
		this.peerList = peerList;
		ArrayList<Socket> clientSockets = new ArrayList<Socket>();
		ArrayList<Integer> distanceToNextPeerList = new ArrayList<Integer>();
		ArrayList<String[]> peersToConnectTo = new ArrayList<String[]>();
		ArrayList<BufferedReader> peerClientInputList = new ArrayList<BufferedReader>();
		ArrayList<ObjectInputStream> peerClientInputObjectList = new ArrayList<ObjectInputStream>();
		ArrayList<PrintWriter> peerClientOutputList = new ArrayList<PrintWriter>();
		ArrayList<ObjectOutputStream> peerClientOutputObjectList = new ArrayList<ObjectOutputStream>();
	}
	
	private void getDistanceToNextPeerList() {
		int i = 1;
		while (i < peerList.size()) {
			distanceToNextPeerList.add(i);
			i = i*2;
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
	}
	
	private void choosePeersToConnectTo() {
		for (int i = 0; i < distanceToNextPeerList.size(); i++) {
			int nextLocation = (peerLocation + distanceToNextPeerList.get(i)) % peerList.size();
			if(nextLocation == peerLocation) {
				break;
			}
			String[] temp = peerList.get(nextLocation);
			peersToConnectTo.add(temp);
		}
	}
	
	private void openPeerServerStreams() {
		try {
			peerServerInput = new BufferedReader(new InputStreamReader(peerServer.getInputStream()));
			peerServerInputObject = new ObjectInputStream(peerServer.getInputStream());
			peerServerOutput = new PrintWriter(peerServer.getOutputStream(), true);
			peerServerOutputObject = new ObjectOutputStream(peerServer.getOutputStream());
			peerServerOutputObject.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void connectToPeers() {
		for (int i = 0; i < peersToConnectTo.size(); i++) {
			String[] temp = peersToConnectTo.get(i);
			try {
				Socket client = new Socket (temp[1], Integer.parseInt(temp[2]));
				showMessage("Peer client connected to: " + temp[1] + " on port: " + temp[2] + "\n");
				clientSockets.add(client);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void openPeerClientStreams() {
		try {
			for(Socket client: clientSockets) {
				BufferedReader peerClientInput = new BufferedReader(new InputStreamReader(client.getInputStream()));
				peerClientInputList.add(peerClientInput);
				ObjectInputStream peerClientInputObject = new ObjectInputStream(client.getInputStream());
				peerClientInputObjectList.add(peerClientInputObject);
				PrintWriter peerClientOutput = new PrintWriter(client.getOutputStream(), true);
				peerClientOutputList.add(peerClientOutput);
				ObjectOutputStream peerClientOutputObject = new ObjectOutputStream(client.getOutputStream());
				peerClientOutputObject.flush();
				peerClientOutputObjectList.add(peerClientOutputObject);
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void communicateOverP2P() {
		try {	
			peerServerOutput.println("SERVER-START");
			for(PrintWriter output: peerClientOutputList) {
				output.println("CLIENT-START");
			}
			
			while(true) {
				serverLine = peerServerInput.readLine();
				if(serverLine.equals("CLIENT-START")) {
					showMessage("Connected to peer as server\n");
				}
				for(BufferedReader input: peerClientInputList) {
					clientLine = input.readLine();
					if(clientLine.equals("SERVER-START")) {
						showMessage("Connected to peer as client\n");
					}
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
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
		openPeerServerStreams();
		connectToPeers();
		openPeerClientStreams();
		communicateOverP2P();
	}
}
