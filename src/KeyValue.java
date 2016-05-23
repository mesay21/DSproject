

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class KeyValue  {

	/*
	 A class used to save key and value in an array and to a file
	 Constructor: public KeyValue()
	 Methods: 1. public void setValue(int key, String value, String p)
	 				arguments: Integer key to save
	 							String value to save and
	 							path variable(type String) refers to nodes file location
	 				Return: none
	 		  2. public String getValue(int i, String path)
	 		  		arguments: Type 
	 							
	 */
	public ArrayList<Integer> key;
	public ArrayList<String> value;
	public ArrayList<Integer> version;
	
	public KeyValue(){		
			this.key = new ArrayList<Integer>();
			this.value = new ArrayList<String>();
			this.version = new ArrayList<Integer>();
	}

	public void setValue(int key, String value, String p, int version) {
		// TODO Auto-generated method stub
		String addValue = key + "," + value + "," + version + "\n"; 
		this.key.add(key);
		this.value.add(value);
		try{
			DataOutputStream buffer = new DataOutputStream(new FileOutputStream(p, true));;
			buffer.writeBytes(addValue);
			buffer.close();
		}catch (Exception e){
			System.err.println("file write error");
		}
	}

	public String getValue(int i, String path) throws IOException {
		// TODO Auto-generated method stub
		String value  = "";
		if(this.key.contains(i) == true ){			
			value = this.value.get(this.key.indexOf(i));
			
		}else{
			value = this.readFromFile(i, path);
			
		}
		return value;
	}

	public void writeToFile(ArrayList<Integer> keyToWrite, ArrayList<String> valueToWrite, String p3) throws IOException {
		// TODO Auto-generated method stub

			DataOutputStream kvwrite = new DataOutputStream(new FileOutputStream(p3, true));
			for(int i =0; i<keyToWrite.size(); i++){
				String data = keyToWrite.get(i)  + "," + valueToWrite.get(i) +"\n";
				kvwrite.writeBytes(data);
			}
			kvwrite.close();
		//}
	}

	public String readFromFile(int i, String path) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader buffer = new BufferedReader(new FileReader(path));
		String readLine = buffer.readLine();
		String value = "";
		while(readLine != null){
			String[] lineData = readLine.split(",");
			
			if(Integer.parseInt(lineData[0]) == i){
				value = lineData[1];
				break;
			}
			if(buffer.ready() == false){
				break;
			}
			readLine = buffer.readLine();
		}
		buffer.close();
		return value;
	}
	
	public boolean modifyValue(int i, String newValue, String path, int nodeId) throws IOException {
		BufferedReader buffer = null;
		String[] p = path.split("/");
		p[p.length - 1] = "temp.txt";
		String tempPath = "";
		for(int s = 1; s<p.length; s++){
			tempPath  = tempPath + "/" + p[s];
			
		}
		String modify = i + "," + newValue  + "\n";
		File oldFile = new File(path);
		File newFile = new File(tempPath);
		BufferedWriter tempBuffer = null;
		String readLine = null;
		
		try{
			buffer = new BufferedReader(new FileReader(path));
			tempBuffer = new BufferedWriter(new FileWriter(tempPath));
		}catch(FileNotFoundException e){
			System.err.println(e);
		}
		readLine = buffer.readLine();
		
		while(readLine != null){
			String[] lineData = readLine.split(",");
			
			if(Integer.parseInt(lineData[0]) != i){
				tempBuffer.write(readLine + "\n");
				
			}
			//check if EOF reached(buffer will not block @ EOF)
			if(buffer.ready() == false){
				tempBuffer.write(modify);
				break;
			}
			readLine = buffer.readLine();
		}
		
		buffer.close();
		tempBuffer.close();
		oldFile.delete();
		newFile.renameTo(oldFile);
		
		return true;
	}
	public void modifyFile(int pred, String path, int nodeId) throws IOException {
		BufferedReader buffer = null;
		String[] p = path.split("/");
		p[p.length - 1] = "temp.txt";
		String tempPath = "";
		for(int s = 1; s<p.length; s++){
			tempPath  = tempPath + "/" + p[s];
			
		}
		File oldFile = new File(path);
		File newFile = new File(tempPath);
		BufferedWriter tempBuffer = null;
		String readLine = null;
		
		try{
			buffer = new BufferedReader(new FileReader(path));
			tempBuffer = new BufferedWriter(new FileWriter(tempPath));
		}catch(FileNotFoundException e){
			System.err.println(e);
		}
		readLine = buffer.readLine();
		while(readLine != null){
			String[] lineData = readLine.split(",");
			if(pred > nodeId){
				if(Integer.parseInt(lineData[0]) < nodeId || Integer.parseInt(lineData[0]) > pred){
					tempBuffer.write(readLine + "\n");
					
				}
			}else{
				if(Integer.parseInt(lineData[0]) < nodeId & Integer.parseInt(lineData[0]) > pred){
					tempBuffer.write(readLine + "\n");
					
				}				
			}
			//check if EOF reached(buffer will not block @ EOF)
			if(buffer.ready() == false){
				break;
			}
			readLine = buffer.readLine();
		}
		
		buffer.close();
		tempBuffer.close();
		oldFile.delete();
		newFile.renameTo(oldFile);		
	}
}	
		

