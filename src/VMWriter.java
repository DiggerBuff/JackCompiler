import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class VMWriter {
	public enum Segment { CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP, NONE }
	
	public enum Command { ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT }
	
	private FileWriter fw;
	private static HashMap<Segment, String> segmentMap = new HashMap<Segment, String>();
	private static HashMap<Command, String> commandMap = new HashMap<Command, String>();

	static {	
		segmentMap.put(Segment.CONST, 		"constant");
		segmentMap.put(Segment.ARG, 		"argument");
		segmentMap.put(Segment.LOCAL, 		"local");
		segmentMap.put(Segment.STATIC, 		"static");
		segmentMap.put(Segment.THIS, 		"this");
		segmentMap.put(Segment.THAT, 		"that");
		segmentMap.put(Segment.POINTER, 	"pointer");
		segmentMap.put(Segment.TEMP, 		"temp");
         
		commandMap.put(Command.ADD, "add");
		commandMap.put(Command.SUB, "sub");
		commandMap.put(Command.NEG, "neg");
		commandMap.put(Command.EQ, 	"eq");
		commandMap.put(Command.GT, 	"gt");
		commandMap.put(Command.LT, 	"lt");
		commandMap.put(Command.AND, "and");
		commandMap.put(Command.OR, 	"or");
		commandMap.put(Command.NOT, "not");
    }
	
	public VMWriter(File file) {
		try {
			this.fw = new FileWriter(file);
		} catch (IOException e) {
			System.out.println("AAAAAAAAAHHHHH!!!!");
		}
		return;
	}

	public void writePush (Segment segment, int index) {
		try {
			fw.write("push " + segmentMap.get(segment) + " " + String.valueOf(index) + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writePop (Segment segment, int index) {
		try {
			fw.write("pop " + segmentMap.get(segment) + " " + String.valueOf(index)  + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeArithmetic (Command command) {
		try {
			fw.write(commandMap.get(command)+ "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeLabel(String label) {
		try {
			fw.write("label " + label + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeGoto (String label) {
		try {
			fw.write("goto " + label + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeIf (String label) {
		try {
			fw.write("if-goto " + label + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeCall (String name, int nArgs) {
		try {
			fw.write("call " + name + " " + String.valueOf(nArgs) + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeFunction (String functionName, int nLocals) {
		try {
			fw.write("function " + functionName + " " + String.valueOf(nLocals) + "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeReturn() {
		try {
			fw.write("return\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
