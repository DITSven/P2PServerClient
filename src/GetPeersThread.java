
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import java.io.*;

public class GetPeersThread extends Thread{
	JTextArea responseWindow;
	Socket serverClient;
	BufferedReader serverClientInput;
	ObjectInputStream serverClientInputObject;
	PrintWriter serverClientOutput;
	private ArrayList<String[]> peerList;
	private String peerID;
	String responseLine;
	private int peerServerPort;
	
	public GetPeersThread(Socket serverClient, JTextArea responseWindow, ArrayList<String[]> peerList, String peerID, int peerServerPort){
		this.serverClient = serverClient;
		this.responseWindow = responseWindow;
		this.setPeerList(peerList);
		this.setPeerID(peerID);
		this.setPeerServerPort(peerServerPort);
	}
	
	private void openServerClientSocket(){
		try {
			serverClient = new Socket("127.0.0.1", 9999);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void openServerClientStreams() {
		try {
		serverClientInput = new BufferedReader(new InputStreamReader(serverClient.getInputStream()));
		serverClientInputObject = new ObjectInputStream(serverClient.getInputStream());
		serverClientOutput = new PrintWriter(serverClient.getOutputStream(), true);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void talkToServer() {
		try {	
			while(true) {
				responseLine = serverClientInput.readLine();
				if(responseLine.equals("SERVER-START")) {
					serverClientOutput.println("CLIENT-START");
					setPeerID(serverClientInput.readLine());
					showMessage(getPeerID() + "\n");	
				}
				if(responseLine.equals("SERVER-PEER-PORT-REQUEST")) {
					serverClientOutput.println(Integer.toString(getPeerServerPort()));
					getPeerList().addAll((ArrayList<String[]>)serverClientInputObject.readObject());
					readPeerList();
					closeConnection();
				}
				if(responseLine.equals("SERVER-CLOSE")){
					break;
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void readPeerList() {
		for (int i = 0; i < getPeerList().size(); i++) {
			String[] temp = getPeerList().get(i);
			showMessage("Peer ID: " + temp[0] + "\n");
			showMessage("Peer IP: " + temp[1] + "\n");
			showMessage("Peer Port: " + temp[2] + "\n");
		}
	}
	
	public void closeConnection() {
		try {
			serverClientOutput.println("CLIENT-CLOSE");
			showMessage("Sent Client Close\n");
			while(true) {
				if(responseLine.equals("SERVER-CLOSE")) {
					serverClientOutput.close();
					serverClientInput.close();
					serverClientInputObject.close();
					serverClient.close();
					break;
				}
			}
			return;
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
		openServerClientSocket();
		openServerClientStreams();
		talkToServer();
	}

	String getPeerID() {
		return peerID;
	}

	void setPeerID(String peerID) {
		this.peerID = peerID;
	}

	ArrayList<String[]> getPeerList() {
		return peerList;
	}

	void setPeerList(ArrayList<String[]> peerList) {
		this.peerList = peerList;
	}

	int getPeerServerPort() {
		return peerServerPort;
	}

	void setPeerServerPort(int peerServerPort) {
		this.peerServerPort = peerServerPort;
	}

}

