import java.net.*;
import java.util.ArrayList;
import javax.swing.*;
import java.io.*;

public class PeerConnectionClientThread extends Thread {
	JTextArea responseWindow;
	ArrayList<Socket> clientList;
	ArrayList<String[]> peersToConnectTo;
	ArrayList<BufferedReader> inputList;
	ArrayList<ObjectInputStream> inputObjectList;
	ArrayList<PrintWriter> outputList;
	ArrayList<ObjectOutputStream> outputObjectList;
	String responseLine;
	
	public PeerConnectionClientThread(ArrayList<Socket> clientList, ArrayList<String[]> peersToConnectTo, JTextArea responseWindow) {
		this.clientList = clientList;
		this.peersToConnectTo = peersToConnectTo;
		this.responseWindow = responseWindow;
		inputList = new ArrayList<BufferedReader>();
		inputObjectList = new ArrayList<ObjectInputStream>();
		outputList = new ArrayList<PrintWriter>();
		outputObjectList = new ArrayList<ObjectOutputStream>();	
	}
	
	private void openClient() {
		if(!peersToConnectTo.isEmpty()) {
			showMessage("Trying to connect to peers\n");
			for (int i = 0; i < peersToConnectTo.size(); i++) {
				String[] temp = peersToConnectTo.get(i);
				try {
					showMessage("Attempting to connect to: " + temp[1] + " on port: " + temp[2] + "\n");
					Socket client = new Socket (temp[1], Integer.parseInt(temp[2]));
					showMessage("Peer client connected to: " + temp[1] + " on port: " + temp[2] + "\n");
					clientList.add(client);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}	

	private void openClientStreams() {
		showMessage("Trying to open peer client streams\n");
		try {
			for(Socket client: clientList) {
				BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
				inputList.add(input);
				ObjectInputStream inputObject = new ObjectInputStream(client.getInputStream());
				inputObjectList.add(inputObject);
				PrintWriter output = new PrintWriter(client.getOutputStream(), true);
				outputList.add(output);
				ObjectOutputStream outputObject = new ObjectOutputStream(client.getOutputStream());
				outputObject.flush();
				outputObjectList.add(outputObject);
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void talkToServer() {
		try {	
			for(PrintWriter out: outputList) {
				out.println("CLIENT-START");
			}
			while(true) {
				for(BufferedReader in: inputList) {
					responseLine = in.readLine();
					if(responseLine.equals("SERVER-START")) {
						showMessage("Connected to peer as client\n");
					}
					if(responseLine.equals("SERVER-CLOSE")) {
						close();
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
	
	public void close() {
		try {
			for (BufferedReader in : inputList) {
				in.close();
			}
			for (ObjectInputStream inObj : inputObjectList) {
				inObj.close();
			}
			for (PrintWriter out : outputList) {
				out.close();
			}
			for (ObjectOutputStream outObj : outputObjectList) {
				outObj.close();
			}
			for (Socket client : clientList) {
				client.close();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void run() {
		openClient();
		openClientStreams();
		talkToServer();
	}
}
