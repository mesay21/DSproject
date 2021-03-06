package org.dsproject.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DHTNode extends KeyValue{

	public String nodeId;
	ArrayList<Integer> possibleNodeId;
	ArrayList<String> nodeList;
	static String p1 = "/home/mesay/Desktop/ds_project";
	
	
	public DHTNode(String nodeId){
		this.nodeId = nodeId;
		this.nodeList = new ArrayList<String>();
		this.possibleNodeId = new ArrayList<Integer>();
	}
	
	public DHTNode(ArrayList<String> nodeList, String nodeId){
		this.nodeId = nodeId;
		this.nodeList = nodeList;
		this.possibleNodeId = new ArrayList<Integer>();
	}
	
	private int sendToNode(int newKey) {
		int nodeKey = Integer.parseInt(this.nodeId.split(",")[0]);
		int responsibleNodeId;
		int[] key = new int[nodeList.size()+2];
		key[0] = nodeKey;
		key[1] = newKey;
		for(int i = 0; i < nodeList.size(); i++){
			key[i + 2] = Integer.parseInt(nodeList.get(i).split(",")[0]);
		}
		Arrays.sort(key);
		int indx = Arrays.binarySearch(key, newKey);
		if(indx == key.length - 1){
			responsibleNodeId = key[0];
		}else{
			responsibleNodeId = key[indx + 1];
		}
		
		return responsibleNodeId;
	}
	
	public String createFile(int flag) {
		String p3 = "/home/mesay/Desktop/ds_project/" + this.nodeId.split(",")[0] + ".txt";
		Path p2 = Paths.get(p1);
		Path p4 = Paths.get(p3);
		if (Files.isDirectory(p2) == false){
			try{
				Files.createDirectory(p2);
				Files.createFile(p4);
				if(flag == 1){
					StringGenerator str = new StringGenerator();
					ArrayList<Integer> key = new ArrayList<Integer>();
					boolean test = true;
					while(test){
						int newKey = new Random().nextInt((int)Math.pow(2, 16));
						if(!key.contains(newKey) & !possibleNodeId.contains(newKey)){
							key.add(newKey);
						}
						if(key.size() == 100){
							break;
						}
					}
					for(Integer i: key){
						setValue(i, str.randomString(20), p3);
					}
				}
			}catch(IOException e){
				System.out.println("Error occured: " + e);
			}

		}else if(Files.exists(p4)==false){
			try{
				//create node file and populate with key value pairs if the node is a starting node
				Files.createFile(p4);
				if(flag == 1){
					StringGenerator str = new StringGenerator();
					ArrayList<Integer> key = new ArrayList<Integer>();
					boolean test = true;
					while(test){
						int newKey = new Random().nextInt((int)Math.pow(2, 16));
						if(!key.contains(newKey) & !possibleNodeId.contains(newKey)){
							key.add(newKey);
						}
						if(key.size() == 100){
							break;
						}
					}
					for(Integer i: key){
						setValue(i, str.randomString(20), p3);
					}
				}
				
			}catch(IOException e){
				System.out.println("Error occured: " + e);
			}
		}
		return p3;
		
	}
	
	public void joinNetwork(String nodeAddr, KeyValue kv, String path) throws IOException{
		
		Socket client = null;
		BufferedReader in = null;
		BufferedWriter os = null;
		String joinRequest = "node,join,1," + this.nodeId;
	    try {
	    	InetAddress nodeAdress = InetAddress.getByName(nodeAddr.split(",")[1]);
	    	client = new Socket(nodeAdress, Integer.parseInt(nodeAddr.split(",")[2]));
	    	System.out.println("new node joining ....");
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
		
	    os.write(joinRequest);	    
		os.newLine();
		os.flush();	
		String readLine = in.readLine();
		while(readLine != null){
			if(!readLine.isEmpty()){
				if(!this.nodeList.contains(readLine)){
					this.nodeList.add(readLine);
				}
			}		
			readLine = in.readLine();
			
		}
		int[] succ = findSuccPred();
		
		os.close();
		in.close();
		client.close();		
		for(int s = 0; s < this.nodeList.size(); s++){
			int nodeId = Integer.parseInt(this.nodeList.get(s).split(",")[0]);
			if(nodeId == succ[1]){
				joinRequest = "node,join,0," + this.nodeId;
			    try {
			    	InetAddress nodeAdress = InetAddress.getByName(this.nodeList.get(s).split(",")[1]);
			    	client = new Socket(nodeAdress, Integer.parseInt(this.nodeList.get(s).split(",")[2]));
			    }catch (IOException e) {
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
			    os.write(joinRequest);
				os.newLine();
				os.flush();	
				String data = in.readLine();
				while(data != null){
					String val = this.getValue(Integer.parseInt(data.split(",")[0]), path);
					int nodeKey = Integer.parseInt(this.nodeId.split(",")[0]);
					if(val.isEmpty()){
						kv.setValue(Integer.parseInt(data.split(",")[0]),
						data.split(",")[1], path);
					}else{
							   // check if key exists in array and file and update both
						if(kv.key.indexOf(Integer.parseInt(data.split(",")[0])) == -1){
								   
							kv.modifyValue(Integer.parseInt(data.split(",")[0]), data.split(",")[1], path, nodeKey);			   
								   
						}else{
							kv.value.set(kv.key.indexOf(Integer.parseInt(data.split(",")[0])), data.split(",")[1]);
							kv.modifyValue(Integer.parseInt(data.split(",")[0]), data.split(",")[1], path, nodeKey);
						}						   						   
					}
					data = in.readLine();
				}
				os.close();
				in.close();
				client.close();
			}else{
				String addRequest = "node,add," + this.nodeId;
			    try {
			    	InetAddress nodeAdress = InetAddress.getByName(this.nodeList.get(s).split(",")[1]);
			    	client = new Socket(nodeAdress, Integer.parseInt(this.nodeList.get(s).split(",")[2]));
			    }catch (IOException e) {
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
			    os.write(addRequest);
				os.newLine();
				os.flush();	
				os.close();
				in.close();
				client.close();
				
			}

		}
		kv.modifyFile(succ[0], path, Integer.parseInt(this.nodeId.split(",")[0]));
	}
	public void joinRequest(String newNodeAddr, BufferedWriter os, int pred, int succ){
		String file = p1 + "/" + nodeId.split(",")[0] + ".txt";
		int newNodeKey = Integer.parseInt(newNodeAddr.split(",")[0]);
		ArrayList<String> data = new ArrayList<String>();
		try {
			data = readFromFile(file, newNodeKey, pred, succ);
		} catch (IOException e) {
			System.out.println("file read error");
			e.printStackTrace();
		}
		if(!(data.isEmpty())){
			for(String s: data){
				try {
					os.write(s);
					os.newLine();
					os.flush();	
				} catch (IOException e) {
					System.out.println("data send error");
					e.printStackTrace();
				}
			}								
		}
	}
	public void joinRequest(String newNodeAddr, BufferedWriter os){
		ArrayList<String> data = new ArrayList<String>();
		data = this.nodeList;
		if(!(data.isEmpty())){
			for(String s: data){
				try {
					os.write(s);
					os.newLine();
					os.flush();	
				} catch (IOException e) {
					System.out.println("node list send error");
					e.printStackTrace();
				}
			}
		}
			
	}

	public int[] findSuccPred(){
		int[] succPred = new int[2];
		int numEl = this.nodeList.size() + 1;
		int[] key = new int[numEl];				
		key[0] = Integer.parseInt(this.nodeId.split(",")[0]);
		
		for(int s = 0; s < this.nodeList.size(); s++){
			key[s+1] = Integer.parseInt(this.nodeList.get(s).split(",")[0]);
		}
		
		Arrays.sort(key);
		int indx = Arrays.binarySearch(key, Integer.parseInt(this.nodeId.split(",")[0]));
		if(indx == key.length-1){
			succPred[1] = key[0];
		}else{
			succPred[1] = key[indx + 1];
		}
		if(indx == 0){
			succPred[0] = key[indx + 1];
		}else{
			succPred[0] = key[indx - 1];
		}
		
		return succPred;
	}
	
	public int[] findSuccPred(int nodeId){
		int[] succPred = new int[2];
		int numEl = this.nodeList.size() + 1;
		int[] key = new int[numEl];				
		key[0] = Integer.parseInt(this.nodeId.split(",")[0]);
		for(int s = 0; s < this.nodeList.size(); s++){
			key[s+1] = Integer.parseInt(this.nodeList.get(s).split(",")[0]);
		}

		Arrays.sort(key);
		int indx = Arrays.binarySearch(key, nodeId);
		if(indx == key.length-1){
			succPred[1] = key[0];
		}else{
			succPred[1] = key[indx + 1];
		}
		if(indx == 0){
			succPred[0] = key[1];
		}else{
			succPred[0] = key[indx - 1];
		}
		return succPred;
	}
	
	
	public boolean isSuccessor(String newNodeAddr){
		int succ = 0;
		int numEl = this.nodeList.size() + 2;
		int[] key = new int[numEl];
		
		key[0] = Integer.parseInt(newNodeAddr.split(",")[0]);
		key[1] = key[1] = Integer.parseInt(this.nodeId.split(",")[0]);
		
		for(int s = 0; s < this.nodeList.size(); s++){
			key[s+2] = Integer.parseInt(this.nodeList.get(s).split(",")[0]);
		}
		
		Arrays.sort(key);
		int indx = Arrays.binarySearch(key, Integer.parseInt(newNodeAddr.split(",")[0]));
		if(indx == key.length-1){
			succ = key[0];
		}else{
			succ = key[indx + 1];
		}
		
		if(succ == Integer.parseInt(this.nodeId.split(",")[0])){
			return true;
		}
		
		return false;
		
	}
	
	public void leaveNetwork() throws IOException{
		int[] succ = findSuccPred();
		
		for(String s: this.nodeList){
			Socket client = null;
			String path = p1 + "/"+ this.nodeId.split(",")[0] + ".txt"; 
		
			if(succ[1] == Integer.parseInt(s.split(",")[0])){
			    try {
			    	InetAddress nodeAdress = InetAddress.getByName(s.split(",")[1]);
			    	client = new Socket(nodeAdress, Integer.parseInt(s.split(",")[2]));
			    	System.out.println("node connecting to transfer data ....");
			    }
			    catch (IOException e) {
			        System.out.println(e);
			    }
				BufferedReader in = new BufferedReader(new FileReader(path));
				BufferedWriter os = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
					
				String rmvrqst = "node,remove," + this.nodeId;

				String data = in.readLine();
				ArrayList<String> dataTowrite = new ArrayList<String>();
				while(data != null){
					dataTowrite.add(data);
					data = in.readLine();
				}
				for(String str: dataTowrite){
					String uprqst = "node,update," + str;
					os.write(uprqst);
					os.newLine();
					//os.flush();					
					
				}
				os.write(rmvrqst);
				os.newLine();
				os.flush();
				os.close();
				in.close();
				client.close();
		
			}else{
				String rmvrqst = "node,remove," + this.nodeId;
			    try {
			    	InetAddress nodeAdress = InetAddress.getByName(s.split(",")[1]);
			    	client = new Socket(nodeAdress, Integer.parseInt(s.split(",")[2]));
			    }
			    catch (IOException e) {
			        System.out.println(e);
			    }			    
				
			    BufferedWriter os = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				os.write(rmvrqst);
				os.newLine();
				os.flush();
				os.close();
				client.close();
			}
			
		}

		
	}
	public ArrayList<String> readFromFile(String path,int newNodeId , int pred, int succ) throws IOException {
		ArrayList<String> data = new ArrayList<String>();
		String[] p = path.split("/");
		p[p.length - 1] = "temp.txt";
		String tempPath = "";
		for(int s = 1; s<p.length; s++){
			tempPath  = tempPath + "/" + p[s];
			
		}
		File oldFile = new File(path);
		File newFile = new File(tempPath);
		BufferedReader buffer = new BufferedReader(new FileReader(path));
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempPath));
		String readLine = buffer.readLine();
		while(readLine != null){
			String[] lineData = readLine.split(",");
			if(pred == succ){
				if(newNodeId < succ){
					if((Integer.parseInt(lineData[0]) < newNodeId) || (Integer.parseInt(lineData[0]) >
						pred)){				
						data.add(readLine);
						// remove from key and value arrays
						int key = Integer.parseInt(readLine.split(",")[0]);
						if(this.key.contains(key)){
							int indx = this.key.indexOf(key);
							this.key.remove(indx);
							this.value.remove(indx);
						}
					}else{
						writer.write(readLine);
						writer.newLine();
						writer.flush();
						
					}
				}else{
					if((Integer.parseInt(lineData[0]) < newNodeId) & (Integer.parseInt(lineData[0]) >
					pred)){				
						data.add(readLine);
						// remove from key and value arrays
						int key = Integer.parseInt(readLine.split(",")[0]);
						if(this.key.contains(key)){
							int indx = this.key.indexOf(key);
							this.key.remove(indx);
							this.value.remove(indx);
						}
					}else{
						writer.write(readLine);
						writer.newLine();
						writer.flush();
					
					}					
				}
			}else{
				if((Integer.parseInt(lineData[0]) < newNodeId) & (Integer.parseInt(lineData[0]) >
				pred)){				
					data.add(readLine);
					// remove from key and value arrays
					int key = Integer.parseInt(readLine.split(",")[0]);
					if(this.key.contains(key)){
						int indx = this.key.indexOf(key);
						this.key.remove(indx);
						this.value.remove(indx);
					}
				}else{
					writer.write(readLine);
					writer.newLine();
					writer.flush();
				
				}
			}
			
			if(buffer.ready() == false){
				break;
			}
			readLine = buffer.readLine();
		}
		buffer.close();
		writer.close();
		oldFile.delete();
		newFile.renameTo(oldFile);
		return data;
	}
	
	public static void main(String[] arg) throws IOException {
		if (arg.length < 3){
			System.out.println("Specify node id, IP address and port");
			System.exit(0);
		}
		InetAddress nodeAdress = InetAddress.getByName(arg[1]);

		String filePath = null;
		DHTNode node = null;
		KeyValue kv = new KeyValue();
		int flag = 0;
		if(arg.length > 3){
			
			String nodeId = arg[0] + "," + arg[1] + "," + arg[2];
			ArrayList<String> nodeList = new ArrayList<String>();
			for(int s=3; s < arg.length; s = s + 3){
				String address = arg[s] + "," + arg[s+1] + "," + arg[s+2];
				
				nodeList.add(address);
			}
			
			node = new DHTNode(nodeList, nodeId);
			filePath = node.createFile(flag);
			node.joinNetwork(nodeList.get(0), kv, filePath);
			for(int i = 1; i < 32; i++){
				node.possibleNodeId.add(2048*i);
			}
		}else{
			flag = 1;
			String address = arg[0] + "," + arg[1] + "," + arg[2];
			node = new DHTNode(address);
			filePath = node.createFile(flag);
			for(int i = 1; i < 32; i++){
				node.possibleNodeId.add(2048*i);
			}
		}
		
		
		
		ServerSocket server = null;
		BufferedReader in = null;
		BufferedWriter os = null;
		Socket serviceSocket = null;
		String clientSent = null;

		   while(true){
			   try {
				    server = new ServerSocket(Integer.parseInt(arg[2]), 10, nodeAdress);
			    }
			    catch (IOException e) {
			         System.out.println(e);
			     }			   
			   try {
				   System.out.println("waiting for connection.....");
				   serviceSocket = server.accept();
				   System.out.println("client connected.....");
				    
			    }
			    catch (IOException e) {
			         System.out.println(e);
			     }
		   
			   // read and write from/to the socket
			   try{
				   in = new BufferedReader(new InputStreamReader(serviceSocket.getInputStream()));
				   
			   }catch (IOException e){
				   System.out.println(e);			   
			   }
			   
			   try{
				   os = new BufferedWriter(new OutputStreamWriter(serviceSocket.getOutputStream()));
				   
			   }catch (IOException e){
				   System.out.println(e);			   
			   }
			   clientSent = in.readLine();
			   ArrayList<String> rcvdData = new ArrayList<String>();
			   while(true){
				   rcvdData.add(clientSent);				   			
				   if(in.ready() == false){
					   break;
				   }
				   clientSent = in.readLine();
			   }
			   
			   for(String str: rcvdData){
				   String[] task = str.split(",");
				   switch(task[0]){
				   case "node":				   
					   switch(task[1]){
					   case "update":
						   System.out.println("Processing update request....");					   
						   String val = kv.getValue(Integer.parseInt(task[2]), filePath);
						   int nodeKey = Integer.parseInt(node.nodeId.split(",")[0]);
						   if(val.isEmpty()){
							   kv.setValue(Integer.parseInt(task[2]), task[3], filePath);
						   }else{
								   // check if key exists in array and file and update both
							   if(kv.key.indexOf(Integer.parseInt(task[2])) == -1){
									   
								   kv.modifyValue(Integer.parseInt(task[2]), task[3], filePath, nodeKey);			   
									   
							   }else{
								   kv.value.set(kv.key.indexOf(Integer.parseInt(task[2])), task[3]);
								   kv.modifyValue(Integer.parseInt(task[2]), task[3], filePath, nodeKey);
							   }
								   
								   
						   }
							
						   os.write("Update successful");
						   os.newLine();
						   break;
					   case "get":
						   System.out.println("Searching for value.....");
						   String value = kv.getValue(Integer.parseInt(task[2]), filePath);
						   System.out.println("value sent");
						   os.write(value);						   
						   os.newLine();
						   os.flush();
						   break;
					   case "join":
						   String nodeAddr = task[3] + "," + task[4] + "," + task[5];
						   if(Integer.parseInt(task[2]) == 1){
							   node.joinRequest(nodeAddr, os);
							   if(!node.nodeList.contains(nodeAddr)){
								   node.nodeList.add(nodeAddr);
							   }
						   }else{
							   if(!node.nodeList.contains(nodeAddr)){
								   node.nodeList.add(nodeAddr);
							   }
							   int[] succPred = node.findSuccPred(Integer.parseInt(task[3]));
							   node.joinRequest(nodeAddr, os, succPred[0], succPred[1]);
						   }
						   break;
					   case "add":
						   String nodeAdd = task[2] + "," + task[3] + "," + task[4];
						   if(!node.nodeList.contains(nodeAdd)){
							   node.nodeList.add(nodeAdd);
						   }
						   
						   os.write("node added to list");
						   os.newLine();
						   os.flush();
						   System.out.println("new node joined the network");
						   break;
					   case "remove":
						   String nodeId = task[2] + "," + task[3] + "," + task[4];
						   System.out.println("node to be removed: " + nodeId);
						   node.nodeList.remove(nodeId);
						   System.out.println("node removed from list");
						   break;
						default:
					   
					   }
					   break;
				   case "client":
					   
					   switch(task[1]){
					   case "update":
						   int responsibleNodeKey = node.sendToNode(Integer.parseInt(task[2]));
						   if(Integer.parseInt(node.nodeId.split(",")[0]) == responsibleNodeKey){
							   System.out.println("Processing update request....");					   
							   String val = kv.getValue(Integer.parseInt(task[2]), filePath);
							   int nodeKey = Integer.parseInt(node.nodeId.split(",")[0]);
							   if(val.isEmpty()){
								   kv.setValue(Integer.parseInt(task[2]), task[3], filePath);
							   }else{
									   // check if key exists in array and file and update both
								   if(kv.key.indexOf(Integer.parseInt(task[2])) == -1){
										   
									   kv.modifyValue(Integer.parseInt(task[2]), task[3], filePath, nodeKey);			   
										   
								   }else{
									   kv.value.set(kv.key.indexOf(Integer.parseInt(task[2])), task[3]);
									   kv.modifyValue(Integer.parseInt(task[2]), task[3], filePath, nodeKey);
								   }
									   
									   
							   }
							   os.write("Update successful");
							   os.newLine();
							   os.flush();
							   
						   }else{
							   int indx = 0;
							   for (String s: node.nodeList){
								   if(responsibleNodeKey == Integer.parseInt(s.split(",")[0])){
									   indx = node.nodeList.indexOf(s);
								   }
							   }
								Socket client = null;
								BufferedReader reader = null;
								BufferedWriter writer = null;
								String cmd = "node,update," + task[2] + "," + task[3];
								InetAddress nodeId = InetAddress.getByName(node.nodeList.get(indx).split(",")[1]);
							    try {	    	
							    	client = new Socket(nodeId, Integer.parseInt(node.nodeList.get(indx).split(",")[2]));
							    	System.out.println("Connection Established ....");
							    }
							    catch (IOException e) {
							        System.out.println(e);
							    }
								try{
									reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
									   
								}catch (IOException e){
									System.out.println(e);			   
								}
								try{
									writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
									   
								}catch (IOException e){
									   System.out.println(e);			   
								}
								writer.write(cmd);
								writer.newLine();
								writer.flush();
								os.write(reader.readLine());
								os.newLine();
								os.flush();
						   }
							
						   
						   break;
					   case "get":
						   int responsibleNodeKey1 = node.sendToNode(Integer.parseInt(task[2]));
						   if(Integer.parseInt(node.nodeId.split(",")[0]) == responsibleNodeKey1){
							   System.out.println("Searching for value.....");
							   String value = kv.getValue(Integer.parseInt(task[2]), filePath);
							   System.out.println("value sent");
							   os.write(value);						   
							   os.newLine();
							   os.flush();
						   }else{
							   int indx = 0;
							   for (String s: node.nodeList){
								   if(responsibleNodeKey1 == Integer.parseInt(s.split(",")[0])){
									   indx = node.nodeList.indexOf(s);
								   }
							   }
								Socket client = null;
								BufferedReader reader = null;
								BufferedWriter writer = null;
								String cmd = "node,get," + task[2];
								InetAddress nodeId = InetAddress.getByName(node.nodeList.get(indx).split(",")[1]);
							    try {	    	
							    	client = new Socket(nodeId, Integer.parseInt(node.nodeList.get(indx).split(",")[2]));
							    	System.out.println("Connection Established ....");
							    }
							    catch (IOException e) {
							        System.out.println(e);
							    }
								try{
									reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
									   
								}catch (IOException e){
									System.out.println(e);			   
								}
								try{
									writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
									   
								}catch (IOException e){
									   System.out.println(e);			   
								}
								writer.write(cmd);
								writer.newLine();
								writer.flush();
								os.write(reader.readLine());
								os.newLine();
								os.flush();   
							   
						   }
						   break;
					   case "leave":
						   node.leaveNetwork();
						   os.write("node left");
						   os.close();
						   in.close();
						   serviceSocket.close();
						   server.close();
						   System.out.println("Leaving the network....");
						   System.exit(0);
						   break;
					   default:
						   os.write("Invalid Command");
						   os.newLine();
						   os.flush();
					  }
					  break;
					  default:
				   }
	
			   }
					   
			   os.close();
			   in.close();
			   serviceSocket.close();
			   server.close();
			   System.out.println("client disconnected");
		   }		   
	}	

}
