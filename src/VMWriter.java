import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
	public enum Segment { CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP }
	
	private FileWriter fw;
	
	public VMWriter(File file) {
		try {
			this.fw = new FileWriter(file);
		} catch (IOException e) {
			System.out.println("AAAAAAAAAHHHHH!!!!");
		}
		return;
	}

	public void writePush (Segment segment, int index) {

	}

	public void writePop (Segment segment, int index) {

	}
	
	public void writeArithmetic () {
		
	}
	
	public void writeLabel(String label) {
		
	}
	
	public void writeGoto (String label) {
		
	}
	
	public void writeIf (String label) {
		
	}
	
	public void writeCall (String label) {
		
	}
	
	public void writeFunction (String className, String function, int nLocals) {
		
	}
	
	public void writeReturn() {
		
	}
	
	public void close() {
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(String text) {
		try {
			fw.write(text + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
