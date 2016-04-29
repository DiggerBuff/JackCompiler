import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
	private JackTokenizer tokenizer;
	private SymbolTable symbolTable;
	private VMWriter vm;
	private String className;
	private String subrName;
	private int nLocals;
	
	// To set for each symbol to add to the symbol table
	private String name;
	private String type;
	private Symbol.Kind kind;
	
	public CompilationEngine(File inputFile, File outputFile) {	
		this.tokenizer = new JackTokenizer(inputFile);
		this.symbolTable = new SymbolTable();
		this.vm = new VMWriter(outputFile);

		return;
	}

	public void compileClass() {
		// Get 'class'
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			throw new IllegalArgumentException("Not a keyword");
		}
		if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CLASS)) {
			throw new IllegalArgumentException("class");
		}
		
		//fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
		
		
		// Get classname
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			throw new IllegalArgumentException("Not an identifier");
		}		
		
		// NEW CODE*********************************************************************************************************
		this.className = tokenizer.identifier();
		vm.write("Classname : " + className);
		
		// Get '{'
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("{")) {
			System.out.println(tokenizer.getToken());
			throw new IllegalArgumentException("Expected '{'");
		}
		
		//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
		

		compileClassVarDec();
		
		compileSubroutine();
		

		// Get '}'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("}")) {
			throw new IllegalArgumentException("Expected '}'");
		}

		//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
		
		
		// End and close
		//fw.write("</class>\n");
		
		vm.close();

		return;
		
	}

	public void compileClassVarDec() {
		// Determine if classVarDec, subroutineDec, or }
		tokenizer.advance();
		
		if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			tokenizer.index--;
			return;
		}
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.CONSTRUCTOR) || 
					tokenizer.keyWord().equals(JackTokenizer.KeyWord.FUNCTION) ||
					tokenizer.keyWord().equals(JackTokenizer.KeyWord.METHOD)) {
				tokenizer.index--;
				return;
			}
		}
		
		//fw.write("<classVarDec>\n");
		
		// Get ('static' | 'field')
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			throw new IllegalArgumentException("Not a keyword");
		}
		if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.STATIC) && !tokenizer.keyWord().equals(JackTokenizer.KeyWord.FIELD)) {
			throw new IllegalArgumentException("Expected 'static' or 'field'");
		}
		
		//fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
		
		// NEW CODE -------------------------------------------------------------------------------------------
		if (tokenizer.getToken().equals("static")) {
			kind = Symbol.Kind.STATIC;
		}
		else {
			kind = Symbol.Kind.FIELD;
		}
		
		// Get type
		tokenizer.advance();
		
		if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.INT) && 
					!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CHAR) &&
					!tokenizer.keyWord().equals(JackTokenizer.KeyWord.BOOLEAN)) {
				throw new IllegalArgumentException("Expected an int, char, or boolean");
			}
			else {
				//fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
				
				// NEW CODE -------------------------------------------------------------------------------------------
				type = tokenizer.getToken();
			}
		}
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			
			// NEW CODE -------------------------------------------------------------------------------------------
			type = tokenizer.getToken();
		}
		else {
			throw new IllegalArgumentException("Expected a type");
		}
		
		// Get varName
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			throw new IllegalArgumentException("Not an identifier");
		}
		
		//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
		
		// NEW CODE -------------------------------------------------------------------------------------------
		name = tokenizer.identifier();
		symbolTable.define(name, type, kind);
		vm.write(symbolTable.classMap.get(name).toString());			
		
		// Get (',' varName)*
		tokenizer.advance();
		
		while (tokenizer.getToken().equals(",")) {
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(",")) {
				throw new IllegalArgumentException("Expected ','");
			}
			
			//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}
			
			//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			
			// NEW CODE -------------------------------------------------------------------------------------------
			name = tokenizer.identifier();
			symbolTable.define(name, type, kind);
			vm.write(symbolTable.classMap.get(name).toString());
			
			tokenizer.advance();
		}
		
		// Get ';'
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals(";")) {
			throw new IllegalArgumentException("Expected ';'");
		}
		
		//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
		
		//fw.write("</classVarDec>\n");
		
		// Check for more classVarDec
		compileClassVarDec();

		return;
	}

	public void compileSubroutine() {
		// Determine if subroutineDec, or }
		tokenizer.advance();
		
		if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			tokenizer.index--;
			return;
		}
		
		//fw.write("<subroutineDec>\n");
		
		// Get ('constructor' | 'function' | 'method')
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			throw new IllegalArgumentException("Not a keyword");
		}
		if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CONSTRUCTOR) && 
				!tokenizer.keyWord().equals(JackTokenizer.KeyWord.FUNCTION) &&
				!tokenizer.keyWord().equals(JackTokenizer.KeyWord.METHOD)) {
			System.out.println(tokenizer.getToken());
			System.out.println(tokenizer.getType());
			System.out.println(tokenizer.keyWord());
			throw new IllegalArgumentException("Expected 'constructor', 'function', or 'method'");
		}
		
		//fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
		
		// Get ('void' | type)
		tokenizer.advance();
		
		if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.INT) && 
					!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CHAR) &&
					!tokenizer.keyWord().equals(JackTokenizer.KeyWord.BOOLEAN) &&
					!tokenizer.keyWord().equals(JackTokenizer.KeyWord.VOID)) {
				System.out.println(tokenizer.keyWord());
				throw new IllegalArgumentException("Expected an int, char, boolean, or void");
			}
			else {
				//fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
			}
		}
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
		}
		else {
			throw new IllegalArgumentException("Expected a type");
		}

		
		// Get subroutineName
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			throw new IllegalArgumentException("Not an identifier");
		}
		
		//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
		
		this.subrName = tokenizer.identifier();
		
		
		// Get '('
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("(")) {
			throw new IllegalArgumentException("Expected '('");
		}
		
		//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
		
		// NEW CODE -------------------------------------------------------------------------------------------
		symbolTable.startSubroutine();	

		compileParameterList();

		// Get ')'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals(")")) {
			throw new IllegalArgumentException("Expected ')'");
		}

		//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");

		compileSubroutineBody();

		//fw.write("</subroutineDec>\n");

		// NEW CODE -------------------------------------------------------------------------------------------
		symbolTable.classScope();

		// Check for more classVarDec
		compileSubroutine();

		return;
	}
	
	public void compileParameterList() {
		// Determine if parameter or ')'
		tokenizer.advance();
		
		if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			tokenizer.index--;
			//fw.write("</parameterList>\n");
			return;
		}
		
		// Get type
		if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.INT) && 
					!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CHAR) &&
					!tokenizer.keyWord().equals(JackTokenizer.KeyWord.BOOLEAN)) {
				throw new IllegalArgumentException("Expected an int, char, or boolean");
			}
			else {
				//fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
				
				// NEW CODE -------------------------------------------------------------------------------------------
				type = tokenizer.getToken();
			}
		}
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
		}
		else {
			throw new IllegalArgumentException("Expected a type");
		}
		
		// Get varName
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			throw new IllegalArgumentException("Not an identifier");
		}
		
		//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
		
		// NEW CODE -------------------------------------------------------------------------------------------
		name = tokenizer.getToken();
		symbolTable.define(name, type, Symbol.Kind.ARG);
		vm.write(symbolTable.subroutineMap.get(name).toString());
		
		
		// Get (',' type varName)*
		tokenizer.advance();
		
		while (tokenizer.getToken().equals(",")) {
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(",")) {
				throw new IllegalArgumentException("Expected ','");
			}
			
			//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			// Get type
			tokenizer.advance();
			
			if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.INT) && 
						!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CHAR) &&
						!tokenizer.keyWord().equals(JackTokenizer.KeyWord.BOOLEAN)) {
					throw new IllegalArgumentException("Expected an int, char, or boolean");
				}
				else {
					//fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
					
					// NEW CODE -------------------------------------------------------------------------------------------
					type = tokenizer.getToken();
				}
			}
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			}
			else {
				throw new IllegalArgumentException("Expected a type");
			}
			
			// Get varName
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}
			
			//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			
			// NEW CODE -------------------------------------------------------------------------------------------
			name = tokenizer.getToken();
			symbolTable.define(name, type, Symbol.Kind.ARG);
			vm.write(symbolTable.subroutineMap.get(name).toString());
			
			tokenizer.advance();
		}
		
		tokenizer.index--;
		
		//fw.write("</parameterList>\n");

		return;
	}
	
	public void compileSubroutineBody() {
		// Get '{'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("{")) {
			throw new IllegalArgumentException("Expected '{'");
		}

		//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
		
		//Check multiple varDec
		
		compileVarDec();
		
		//compile statements

		compileStatements();


		// Get '}'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("}")) {
			throw new IllegalArgumentException("Expected '{'");
		}

		//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
		
		//fw.write("</subroutineBody>\n");
		
		vm.writeFunction(className, subrName, nLocals);
		
		return;
	}
	
	public void compileVarDec() {
		// Get 'var' or return if statement
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			throw new IllegalArgumentException("Not a keyword");
		}
		if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.VAR)) {
			tokenizer.index--;
			return;
		}
		
		nLocals = 0;

		while(tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD) && tokenizer.keyWord().equals(JackTokenizer.KeyWord.VAR)) {

			//fw.write("<varDec>\n");
			
			// NEW CODE -------------------------------------------------------------------------------------------
			kind = Symbol.Kind.VAR;
			
			//fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");

			// Get type
			tokenizer.advance();

			if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.INT) && 
						!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CHAR) &&
						!tokenizer.keyWord().equals(JackTokenizer.KeyWord.BOOLEAN)) {
					throw new IllegalArgumentException("Expected an int, char, or boolean");
				}
				else {
					//fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
				}
			}
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			}
			else {
				throw new IllegalArgumentException("Expected a type");
			}
			
			// NEW CODE -------------------------------------------------------------------------------------------
			type = tokenizer.getToken();
			

			// Get varName
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}

			//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			
			// NEW CODE -------------------------------------------------------------------------------------------
			name = tokenizer.getToken();
			symbolTable.define(name, type, kind);
			vm.write(symbolTable.subroutineMap.get(name).toString());
			
			nLocals++;

			// Get (',' varName)*
			tokenizer.advance();

			while (tokenizer.getToken().equals(",")) {
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					throw new IllegalArgumentException("Not a symbol");
				}
				if (!tokenizer.symbol().equals(",")) {
					throw new IllegalArgumentException("Expected ','");
				}

				//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");

				tokenizer.advance();

				if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
					throw new IllegalArgumentException("Not an identifier");
				}

				//fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
				
				// NEW CODE -------------------------------------------------------------------------------------------
				name = tokenizer.getToken();
				symbolTable.define(name, type, kind);
				vm.write(symbolTable.subroutineMap.get(name).toString());
				
				nLocals++;

				tokenizer.advance();
			}

			// Get ';'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(";")) {
				throw new IllegalArgumentException("Expected ';'");
			}

			//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");

			tokenizer.advance();
			
			//fw.write("</varDec>\n");
		}
		
		tokenizer.index--;
		
		return;
	}
	
	public void compileStatements() {
		// Determine if statement, or }
		tokenizer.advance();

		if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			tokenizer.index--;
			return;
		}
		
		//fw.write("<statements>\n");
		
		boolean noReturn = true;

		while (!tokenizer.getToken().equals("}")) {

			// Get ('constructor' | 'function' | 'method')
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				throw new IllegalArgumentException("Not a keyword for a statement");
			}
			if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.LET)) {
				compileLet();
				noReturn = true;
			}
			else if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.IF)) {
				compileIf();
				noReturn = true;
			}
			else if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.WHILE)) {
				compileWhile();
				noReturn = true;
			}
			else if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.DO)) {
				compileDo();
				noReturn = true;
			}
			else if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.RETURN)) {
				compileReturn();
				noReturn = false;
			}
			else {
				throw new IllegalArgumentException("Expected a statement");
			}

			tokenizer.advance();

		}
		
		if (noReturn) {
			vm.writeReturn();
		}
		
		tokenizer.index--;
		
		//fw.write("</statements>\n");

		return;
	}

	public void compileDo() {
		// Get 'do'
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			throw new IllegalArgumentException("Not a keyword");
		}
		if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.DO)) {
			throw new IllegalArgumentException("do");
		}

		//fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");

		compileSubroutineCall();

		// Get ';'
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals(";")) {
			throw new IllegalArgumentException("Expected ';'");
		}

		//fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
		
		//fw.write("</doStatement>\n");

		// Pop return value from subroutine call
		// pop temp 0
		vm.writePop(VMWriter.Segment.TEMP, 0);
		
		return;
	}

	public void compileLet() {
		try {
			//fw.write("<letStatement>\n");

			// Get 'let'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				throw new IllegalArgumentException("Not a keyword");
			}
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.LET)) {
				throw new IllegalArgumentException("let");
			}

			fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");

			// Get varName
			tokenizer.advance();
			System.out.println(" let varname : " + tokenizer.getToken());

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}

			fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			
			// NEW CODE -------------------------------------------------------------------------------------------
			name = tokenizer.getToken();
			// If the variable exists in the subroutine scope
			if (!(symbolTable.subroutineMap.get(tokenizer.identifier()) == null)) {
				fw.write("SETTING SUBR : " + symbolTable.subroutineMap.get(name).toString());
			}
			else if (!(symbolTable.classMap.get(tokenizer.identifier()) == null)) {
				fw.write("SETTING CLASS : " + symbolTable.classMap.get(name).toString());
			}
			else {
				throw new IllegalArgumentException("Identifier could not be found");
			}
			

			// Get ('[' expression ']') or '='
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (tokenizer.getToken().equals("[")) {
				System.out.println("'[' expression ']' happened");
				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
				compileExpression();
				
				// Get ']'
				tokenizer.advance();
				
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					throw new IllegalArgumentException("Not a symbol");
				}
				if (!tokenizer.symbol().equals("]")) {
					throw new IllegalArgumentException("Expected ']'");
				}

				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
				tokenizer.advance();
			}

			// Get '='
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("=")) {
				System.out.println(tokenizer.getToken());
				throw new IllegalArgumentException("Expected '='");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			compileExpression();

			// Get ';'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(";")) {
				System.out.println(tokenizer.getToken());
				throw new IllegalArgumentException("Expected ';'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			fw.write("</letStatement>\n");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileWhile() {
		try {
			fw.write("<whileStatement>\n");

			// Get 'while'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				throw new IllegalArgumentException("Not a keyword");
			}
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.WHILE)) {
				throw new IllegalArgumentException("while");
			}

			fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");

			// Get '('
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("(")) {
				throw new IllegalArgumentException("Expected '('");
			}
			
			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			compileExpression();

			// Get ')'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(")")) {
				throw new IllegalArgumentException("Expected ')'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");

			// Get '{'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("{")) {
				throw new IllegalArgumentException("Expected '{'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			compileStatements();

			// Get '}'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("}")) {
				throw new IllegalArgumentException("Expected '}'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			fw.write("</whileStatement>\n");
			
			return;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileReturn() {
		try {
			fw.write("<returnStatement>\n");

			// Get 'return'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				throw new IllegalArgumentException("Not a keyword");
			}
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.RETURN)) {
				throw new IllegalArgumentException("return");
			}

			fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");

			// Check if ';' or expression
			tokenizer.advance();
			
			if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				if (tokenizer.symbol().equals(";")) {
					
					fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
					
					fw.write("</returnStatement>\n");
					
					return;
				}
			}
			
			tokenizer.index--;
			
			compileExpression();

			// Get ';'
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(";")) {
				throw new IllegalArgumentException("Expected ';'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			fw.write("</returnStatement>\n");
			
			return;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileIf() {
		try {
			fw.write("<ifStatement>\n");

			// Get 'if'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				throw new IllegalArgumentException("Not a keyword");
			}
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.IF)) {
				System.out.println(tokenizer.keyWord());
				throw new IllegalArgumentException("if");
			}

			fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");

			// Get '('
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				System.out.println(tokenizer.getToken());
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("(")) {
				throw new IllegalArgumentException("Expected '('");
			}
			
			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			compileExpression();

			// Get ')'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(")")) {
				throw new IllegalArgumentException("Expected ')'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");

			// Get '{'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("{")) {
				throw new IllegalArgumentException("Expected '{'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			compileStatements();

			// Get '}'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("}")) {
				throw new IllegalArgumentException("Expected '}'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			
			// Check for else
			tokenizer.advance();
			
			if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.ELSE)) {
				compileElse();
			}
			else {
				tokenizer.index--;
			}
			
			fw.write("</ifStatement>\n");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileElse() {
		try {
			fw.write("<elseStatement>\n");

			// Get 'else'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				throw new IllegalArgumentException("Not a keyword");
			}
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.ELSE)) {
				throw new IllegalArgumentException("else");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");

			// Get '{'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("{")) {
				throw new IllegalArgumentException("Expected '{'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			compileStatements();

			// Get '}'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("}")) {
				throw new IllegalArgumentException("Expected '}'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");

			fw.write("</elseStatement>\n");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileExpression() {
		try {
			fw.write("<expression>\n");
			
			compileTerm();
			
			// Get (op term)*
			tokenizer.advance();
			
			while (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL) && JackTokenizer.opSet.contains(tokenizer.symbol())) {
				
				String text = tokenizer.symbol();
				
				if (text.equals("<")) {
					text = "&lt;";
				}
				else if (text.equals(">")) {
					text = "&gt;";
				}
				else if (text.equals("&")) {
					text = "&amp;";
				}
				
				fw.write("<symbol> " + text + " </symbol>\n");
				
				compileTerm();
				
				tokenizer.advance();
			}
			
			tokenizer.index--;
			
			fw.write("</expression>\n");

			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void compileSubroutineCall() {
		try {
			// Get 'subroutineName' / 'className' / 'varName'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}

			fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			
			// NEW CODE -------------------------------------------------------------------------------------------
			name = tokenizer.getToken();
			// If the variable exists in the subroutine scope
			if (!(symbolTable.subroutineMap.get(tokenizer.identifier()) == null)) {
				fw.write("Compile subroutine (subr) : " + symbolTable.subroutineMap.get(name).toString());
			}
			else if (!(symbolTable.classMap.get(tokenizer.identifier()) == null)) {
				fw.write("Compile subroutine (class) : " + symbolTable.classMap.get(name).toString());
			}
			else {
				fw.write("Compile subroutine (unknown) : " + name);
			}
			
			// Get '(' or '.'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (tokenizer.symbol().equals("(")) {
				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
				compileExpressionList();
				
				// Get ')'
				tokenizer.advance();

				if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					throw new IllegalArgumentException("Not a symbol");
				}
				if (!tokenizer.symbol().equals(")")) {
					throw new IllegalArgumentException("Expected ')'");
				}

				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			}
			else if (tokenizer.symbol().equals(".")) {
				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
				// Get 'subroutineName'
				tokenizer.advance();
				
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
					throw new IllegalArgumentException("Not an identifier");
				}

				fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
				
				// Get '('
				tokenizer.advance();

				if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					throw new IllegalArgumentException("Not a symbol");
				}
				if (!tokenizer.symbol().equals("(")) {
					throw new IllegalArgumentException("Expected '('");
				}

				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
				compileExpressionList();
				
				// Get ')'
				tokenizer.advance();

				if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					throw new IllegalArgumentException("Not a symbol");
				}
				if (!tokenizer.symbol().equals(")")) {
					throw new IllegalArgumentException("Expected ')'");
				}

				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
			} 
			else {
				throw new IllegalArgumentException("Expected '(' or '.'");
			}
			
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void compileTerm() {
		try {
			fw.write("<term>\n");
			
			tokenizer.advance();
			System.out.println("Compile term called on : " + tokenizer.getToken());
			
			// integerConstant
			if (tokenizer.getType().equals(JackTokenizer.TokenType.INT_CONST)) {
				fw.write("<integerConstant> " + tokenizer.intVal() + " </integerConstant>\n");
			}
			
			// stringConstant
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.STRING_CONST)) {
				fw.write("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>\n");
			}
			
			// keywordConstant
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD) && 
						(tokenizer.keyWord().equals(JackTokenizer.KeyWord.TRUE) || 
							tokenizer.keyWord().equals(JackTokenizer.KeyWord.FALSE) ||
							tokenizer.keyWord().equals(JackTokenizer.KeyWord.NULL) ||
							tokenizer.keyWord().equals(JackTokenizer.KeyWord.THIS))) {

				fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
			}
			
			// Could be varName | varName '[' expression ']' | subroutineCall
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				
				// Check for following '[' | '('
				tokenizer.advance();
				System.out.println("Initial else if entered : " + tokenizer.getToken() + " should be ;");

				if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					// varName '[' expression ']'  
					if (tokenizer.symbol().equals("[")) {
						
						// Backtrack back to before varName
						tokenizer.index = tokenizer.index - 2;
						
						tokenizer.advance();
						System.out.println("Entered varName '[' expression ']' : " + tokenizer.getToken());
						
						fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
						
						// NEW CODE -------------------------------------------------------------------------------------------
						name = tokenizer.getToken();
						// If the variable exists in the subroutine scope
						if (!(symbolTable.subroutineMap.get(tokenizer.identifier()) == null)) {
							fw.write("Compile term (subr) : " + symbolTable.subroutineMap.get(name).toString());
						}
						else if (!(symbolTable.classMap.get(tokenizer.identifier()) == null)) {
							fw.write("Compile term (class) : " + symbolTable.classMap.get(name).toString());
						}
						else {
							throw new IllegalArgumentException("CompileTerm : Identifier could not be found");
						}
						
						// Get '['
						tokenizer.advance();
						
						fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
						
						compileExpression();
						
						// Get ']'
						tokenizer.advance();

						if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
							throw new IllegalArgumentException("Not a symbol");
						}
						if (!tokenizer.symbol().equals("]")) {
							throw new IllegalArgumentException("Expected ']'");
						}

						fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
						
					}
					// subroutineCall == subroutineName '(' || (className | varName) '.'
					else if (tokenizer.symbol().equals("(") || tokenizer.symbol().equals(".")) {
						tokenizer.index = tokenizer.index - 2;
						System.out.println("Should be identifier for subroutine : " + tokenizer.getToken());
						
						compileSubroutineCall();
						
					}
					// varName if followed by symbol
					else {
						tokenizer.index = tokenizer.index - 2;
						
						tokenizer.advance();
						System.out.println("Identifier then symbol[^[(] : " + tokenizer.getToken());
						
						fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
						
						// NEW CODE -------------------------------------------------------------------------------------------
						name = tokenizer.getToken();
						// If the variable exists in the subroutine scope
						if (!(symbolTable.subroutineMap.get(tokenizer.identifier()) == null)) {
							fw.write("Compile term (subr) : " + symbolTable.subroutineMap.get(name).toString());
						}
						else if (!(symbolTable.classMap.get(tokenizer.identifier()) == null)) {
							fw.write("Compile term (class) : " + symbolTable.classMap.get(name).toString());
						}
						else {
							throw new IllegalArgumentException("CompileTerm (varName) : Identifier could not be found");
						}
					}
				}
				// varName if followed by non-symbol
				else {
					tokenizer.index = tokenizer.index - 2;
					
					tokenizer.advance();
					System.out.println("Identifier then nonSymbol : " + tokenizer.getToken());
					
					fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
					
					// NEW CODE -------------------------------------------------------------------------------------------
					name = tokenizer.getToken();
					// If the variable exists in the subroutine scope
					if (!(symbolTable.subroutineMap.get(tokenizer.identifier()) == null)) {
						fw.write("Compile term (subr) : " + symbolTable.subroutineMap.get(name).toString());
					}
					else if (!(symbolTable.classMap.get(tokenizer.identifier()) == null)) {
						fw.write("Compile term (class) : " + symbolTable.classMap.get(name).toString());
					}
					else {
						throw new IllegalArgumentException("CompileTerm (varName) : Identifier could not be found");
					}
				}
			}
			// Get '(' expression ')' | unaryOp term
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				// '(' expression ')' 
				if (tokenizer.symbol().equals("(")) {
					System.out.println("'(' expression ')' : " + tokenizer.getToken());
					// Get '('
					fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
					
					compileExpression();
					
					// Get ')'
					tokenizer.advance();

					if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
						throw new IllegalArgumentException("Not a symbol");
					}
					if (!tokenizer.symbol().equals(")")) {
						throw new IllegalArgumentException("Expected ')'");
					}

					fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				}
				// unaryOp term
				else {
					System.out.println("unaryOp term : " + tokenizer.getToken());
					if (!tokenizer.symbol().equals("-") && !tokenizer.symbol().equals("~")) {
						tokenizer.index--;
						tokenizer.index--;
						tokenizer.advance();
						System.out.println("Previous token : " + tokenizer.getToken());
						
						throw new IllegalArgumentException("Expected '-' or '~'");
					}

					fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
					
					compileTerm();
				}
			}
			else {
				throw new IllegalArgumentException("Expected a term");
			}
			
			fw.write("</term>\n");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileExpressionList() {
		try {
			fw.write("<expressionList>\n");
			
			// Check for ')'
			tokenizer.advance();
			
			if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				if (tokenizer.symbol().equals(")")) {
					fw.write("</expressionList>\n");
					tokenizer.index--;
					System.out.println("Empty expression list");
					return;
				}
			}
			
			tokenizer.index--;
			
			compileExpression();

			// Get (',' expression)*
			tokenizer.advance();

			while (tokenizer.getToken().equals(",")) {

				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
				compileExpression();

				tokenizer.advance();
			}
			
			tokenizer.index--;
			
			fw.write("</expressionList>\n");
			
			return;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
