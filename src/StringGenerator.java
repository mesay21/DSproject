

import java.util.Random;

public class StringGenerator {
	private char[] charArray = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s',
			't','u','v','w','x','y','z'};
	
	public StringGenerator(){
		
	}
	private char getChar(){		
		return charArray[new Random().nextInt(charArray.length)];
	}
	
	public String randomString(int length){
		StringBuilder randStr = new StringBuilder();
		for(int i = 0; i < length; i++){
			randStr.append(getChar());
		}
		return randStr.toString();
	}
}