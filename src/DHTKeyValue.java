

import java.io.IOException;

public interface DHTKeyValue {
	public void setValue(int key, String value) ;
	public String getValue(int i) throws IOException;
}
