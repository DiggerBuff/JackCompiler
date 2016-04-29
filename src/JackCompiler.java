import java.io.File;
import java.util.ArrayList;

public class JackCompiler {
	// DATA MEMBERS------------------------------------------------------------------------------------- 
	private File inputFile;
	private File outputFile;
	private String VMDir;
	
	// CONSTRUCTOR-------------------------------------------------------------------------------------------
	public JackCompiler(File file) {
		this.inputFile = file;
	}
	
	// METHODS-------------------------------------------------------------------------------------------
	public void run() {
		createVMDirectory();
		
		if (inputFile.isFile()) {
			if(inputFile.getAbsolutePath().endsWith(".jack")) {

				setOutputFile(inputFile);
				
				CompilationEngine ce = new CompilationEngine(inputFile, outputFile);
				System.out.println("Compiling : " + inputFile.getName());
				ce.compileClass();
				System.out.println("Finished Compiling : " + inputFile.getName());
			}
		}
		else if (inputFile.isDirectory()) {
			ArrayList<File> jackFiles = new ArrayList<File>();
			jackFiles = getJackFiles(inputFile);

			for (File f : jackFiles) {
				setOutputFile(f);

				CompilationEngine ce = new CompilationEngine(f, outputFile);
				System.out.println("Compiling : " + f.getName());
				ce.compileClass();
				System.out.println("Finished Compiling : " + f.getName());
			}
		}
		else {
			throw new IllegalArgumentException("need a .jack file or directory");
		}
	}
	
	public void createVMDirectory() {
		if (inputFile.isFile()) {
			VMDir = (inputFile.getParentFile()).getAbsolutePath() + "\\VM";
		}
		else if (inputFile.isDirectory()) {
			VMDir = inputFile.getAbsolutePath() + "\\VM";
		}
		else {
			throw new IllegalArgumentException("need a .jack file or directory");
		}
		File dir = new File(VMDir);
		dir.mkdirs();
		return;
	}

	// GETTERS / SETTERS-------------------------------------------------------------------------------------------
	private static ArrayList<File> getJackFiles(File directory) {
		File[] files = directory.listFiles();

		ArrayList<File> jackFiles = new ArrayList<File>();

		for (File f : files){
			if (f.getName().endsWith(".jack")){
				jackFiles.add(f);
			}
		}
		
		return jackFiles;
	}

	public void setOutputFile(File inputFile) {
		this.outputFile = new File(VMDir + "\\" + inputFile.getName().substring(0, inputFile.getName().lastIndexOf(".")) +  ".vm");
		return;
	}
	
	// MAIN-------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("need a .jack file or directory");
		}
		
		File file = new File(args[0]);
		
		JackCompiler jackAttack = new JackCompiler(file);
		jackAttack.run();
		
		return;
	}
}
