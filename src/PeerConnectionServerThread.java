import java.net.*;
import javax.swing.*;
import java.io.*;

public class PeerConnectionServerThread extends Thread {
	JTextArea responseWindow;
	ServerSocket server;
	Socket client;
	BufferedReader input;
	ObjectInputStream inputObject;
	PrintWriter output;
	ObjectOutputStream outputObject;
	String responseLine;
	
	public PeerConnectionServerThread(ServerSocket server, JTextArea responseWindow) {
		this.server = server;
		this.responseWindow = responseWindow;
	}
	
	private void openServer() {
		showMessage("Attempting to open peer server socket\n");
		try {
			client = server.accept();
		} catch (IOException e) {
			e.printStackTrace();
		}
		showMessage("Peer socket opened\n");
	}
	
	private void openStreams() {
		showMessage("Trying to open peer server streams\n");
		try {
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			inputObject = new ObjectInputStream(client.getInputStream());
			output = new PrintWriter(client.getOutputStream(), true);
			outputObject = new ObjectOutputStream(client.getOutputStream());
			outputObject.flush();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void talkToClient() {
		try {	
			output.println("SERVER-START");
			while(true) {
				responseLine = input.readLine();
				if(responseLine.equals("CLIENT-START")) {
					showMessage("Connected to peer as server\n");
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
		output.println("SERVER-CLOSE");
		try {
			input.close();
			inputObject.close();
			output.close();
			outputObject.close();
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		openServer();
		openStreams();
		talkToClient();
	}
}
