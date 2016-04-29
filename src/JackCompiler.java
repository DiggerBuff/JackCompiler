import java.io.File;
import java.util.ArrayList;

public class JackCompiler {
	// DATA MEMBERS------------------------------------------------------------------------------------- 
	private File inputFile;
	private File outputFile;
	private File outputFileT;
	private String XMLDir;
	
	// CONSTRUCTOR-------------------------------------------------------------------------------------------
	public JackCompiler(File file) {
		this.inputFile = file;
	}
	
	// METHODS-------------------------------------------------------------------------------------------
	public void run() {
		createXMLDirectory();
		
		if (inputFile.isFile()) {
			if(inputFile.getAbsolutePath().endsWith(".jack")) {

				setOutputFiles(inputFile);
				
				CompilationEngine ce = new CompilationEngine(inputFile, outputFileT, outputFile);
				System.out.println("Compiling : " + inputFile.getName());
				ce.compileClass();
				System.out.println("Finished Compiling : " + inputFile.getName());
			}
		}
		else if (inputFile.isDirectory()) {
			ArrayList<File> jackFiles = new ArrayList<File>();
			jackFiles = getJackFiles(inputFile);

			for (File f : jackFiles) {
				setOutputFiles(f);

				CompilationEngine ce = new CompilationEngine(f, outputFileT, outputFile);
				System.out.println("Compiling : " + f.getName());
				ce.compileClass();
				System.out.println("Finished Compiling : " + f.getName());
			}
		}
		else {
			throw new IllegalArgumentException("need a .jack file or directory");
		}
	}
	
	public void createXMLDirectory() {
		if (inputFile.isFile()) {
			XMLDir = (inputFile.getParentFile()).getAbsolutePath() + "\\XML";
		}
		else if (inputFile.isDirectory()) {
			XMLDir = inputFile.getAbsolutePath() + "\\XML";
		}
		else {
			throw new IllegalArgumentException("need a .jack file or directory");
		}
		File dir = new File(XMLDir);
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

	public void setOutputFiles(File inputFile) {
		this.outputFile = new File(XMLDir + "\\" + inputFile.getName().substring(0, inputFile.getName().lastIndexOf(".")) +  ".xml");
		this.outputFileT = new File(XMLDir + "\\" + inputFile.getName().substring(0, inputFile.getName().lastIndexOf(".")) +  "T.xml");
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
