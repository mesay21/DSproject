package org.dsproject.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
	
	
	public static void main(String[] arg) throws IOException {
		Socket client = null;
		BufferedReader in = null;
		BufferedWriter os = null;
		String cmd = "client";
		InetAddress nodeAdress = InetAddress.getByName(arg[1]);
	    try {	    	
	    	client = new Socket(nodeAdress, Integer.parseInt(arg[2]));
	    	System.out.println("Connection Established ....");
	    }
	    catch (IOException e) {
	        System.out.println(e);
	    }
		try{
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			   
		}catch (IOException e){
			System.out.println(e);			   
		}
		try{
			os = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			   
		}catch (IOException e){
			   System.out.println(e);			   
		}
		   
		for (int j = 3; j < arg.length; j++){
			   cmd = cmd + "," +arg[j]  ;			   
		}
	    os.write(cmd);
		os.newLine();
		os.flush();
	    System.out.println("Service request sent......");		   
		String rv = in.readLine();
		client.close();
		os.close();
		in.close();
		if (rv.isEmpty()){
			System.out.println("key not found");
		}else{
			System.out.printf("Response received: %s\n", rv);
		}		   
	    
	}
}

