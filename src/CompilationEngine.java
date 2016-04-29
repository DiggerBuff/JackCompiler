import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
	private JackTokenizer tokenizer;
	private SymbolTable symbolTable;
	private FileWriter fw, fwT;
	private String className;
	
	// To set for each symbol to add to the symbol table
	private String name;
	private String type;
	private Symbol.Kind kind;
	
	public CompilationEngine(File inputFile, File outputFileT, File outputFile) {		
		this.tokenizer = new JackTokenizer(inputFile);
		this.symbolTable = new SymbolTable();
		
		try {
			this.fw = new FileWriter(outputFile);
			this.fwT = new FileWriter(outputFileT);
		} catch (IOException e) {
			System.out.println("Couldn't create file writers");
		}
		
		makeXML();

		return;
	}

	private void makeXML() {
		try {

			fwT.write("<tokens>\n");

			while(tokenizer.hasMoreTokens()) {
				tokenizer.advance();

				JackTokenizer.TokenType type = tokenizer.getType();

				if (type.equals(JackTokenizer.TokenType.KEYWORD)) {
					fwT.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
				}
				else if (type.equals(JackTokenizer.TokenType.SYMBOL)) {
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
					
					fwT.write("<symbol> " + text + " </symbol>\n");
				}
				else if (type.equals(JackTokenizer.TokenType.IDENTIFIER)) {
					String text = tokenizer.identifier();
					fwT.write("<identifier> " + text + " </identifier>\n");
				}
				else if (type.equals(JackTokenizer.TokenType.INT_CONST)) {
					fwT.write("<integerConstant> " + tokenizer.getToken() + " </integerConstant>\n");
				}
				else if (type.equals(JackTokenizer.TokenType.STRING_CONST)) {
					fwT.write("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>\n");
				}
				else {
					System.out.println(type);
					throw new Exception("UNIDENTIFIED TOKEN");
				}
			}
			
			fwT.write("</tokens>\n");
			
			fwT.close();
			
			tokenizer.index = 0;

		} catch (Exception e) {
			System.out.println("XML stuff messed up");
		}
	}

	public void compileClass() {
		try {
			fw.write("<class>\n");
			
			
			// Get 'class'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				throw new IllegalArgumentException("Not a keyword");
			}
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CLASS)) {
				throw new IllegalArgumentException("class");
			}
			
			fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
			
			
			// Get classname
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}

			fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			
			
			// NEW CODE*********************************************************************************************************
			this.className = tokenizer.identifier();
			symbolTable.classScope();
			
			
			// Get '{'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("{")) {
				System.out.println(tokenizer.getToken());
				throw new IllegalArgumentException("Expected '{'");
			}
			
			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			

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

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			
			// End and close
			fw.write("</class>\n");
			
			fw.close();

			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void compileClassVarDec() {
		try {
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
			
			fw.write("<classVarDec>\n");
			
			// Get ('static' | 'field')
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				throw new IllegalArgumentException("Not a keyword");
			}
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.STATIC) && !tokenizer.keyWord().equals(JackTokenizer.KeyWord.FIELD)) {
				throw new IllegalArgumentException("Expected 'static' or 'field'");
			}
			
			fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
			
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
					fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
					
					// NEW CODE -------------------------------------------------------------------------------------------
					type = tokenizer.getToken();
				}
			}
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
				
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
			
			fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			
			// NEW CODE -------------------------------------------------------------------------------------------
			name = tokenizer.identifier();
			symbolTable.define(name, type, kind);
			fw.write(symbolTable.classMap.get(name).toString());
			System.out.println("*************" + symbolTable.classMap.get(name).toString());
			
			
			// Get (',' varName)*
			tokenizer.advance();
			
			while (tokenizer.getToken().equals(",")) {
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					throw new IllegalArgumentException("Not a symbol");
				}
				if (!tokenizer.symbol().equals(",")) {
					throw new IllegalArgumentException("Expected ','");
				}
				
				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
				tokenizer.advance();
				
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
					throw new IllegalArgumentException("Not an identifier");
				}
				
				fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
				
				// NEW CODE -------------------------------------------------------------------------------------------
				name = tokenizer.identifier();
				symbolTable.define(name, type, kind);
				fw.write(symbolTable.classMap.get(name).toString());
				
				tokenizer.advance();
			}
			
			// Get ';'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(";")) {
				throw new IllegalArgumentException("Expected ';'");
			}
			
			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			fw.write("</classVarDec>\n");
			
			// Check for more classVarDec
			compileClassVarDec();

			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void compileSubroutine() {
		try {
			// Determine if subroutineDec, or }
			tokenizer.advance();
			
			if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				tokenizer.index--;
				return;
			}
			
			fw.write("<subroutineDec>\n");
			
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
			
			fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
			
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
					fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
				}
			}
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			}
			else {
				throw new IllegalArgumentException("Expected a type");
			}

			
			// Get subroutineName
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

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");

			compileSubroutineBody();

			fw.write("</subroutineDec>\n");

			// NEW CODE -------------------------------------------------------------------------------------------
			symbolTable.classScope();

			// Check for more classVarDec
			compileSubroutine();

			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileParameterList() {
		try {
			fw.write("<parameterList>\n");
			
			// Determine if parameter or ')'
			tokenizer.advance();
			
			if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				tokenizer.index--;
				fw.write("</parameterList>\n");
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
					fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
					
					// NEW CODE -------------------------------------------------------------------------------------------
					type = tokenizer.getToken();
				}
			}
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			}
			else {
				throw new IllegalArgumentException("Expected a type");
			}
			
			// Get varName
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}
			
			fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			
			// NEW CODE -------------------------------------------------------------------------------------------
			name = tokenizer.getToken();
			symbolTable.define(name, type, Symbol.Kind.ARG);
			fw.write(symbolTable.subroutineMap.get(name).toString());
			
			
			// Get (',' type varName)*
			tokenizer.advance();
			
			while (tokenizer.getToken().equals(",")) {
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					throw new IllegalArgumentException("Not a symbol");
				}
				if (!tokenizer.symbol().equals(",")) {
					throw new IllegalArgumentException("Expected ','");
				}
				
				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
				// Get type
				tokenizer.advance();
				
				if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
					if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.INT) && 
							!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CHAR) &&
							!tokenizer.keyWord().equals(JackTokenizer.KeyWord.BOOLEAN)) {
						throw new IllegalArgumentException("Expected an int, char, or boolean");
					}
					else {
						fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
						
						// NEW CODE -------------------------------------------------------------------------------------------
						type = tokenizer.getToken();
					}
				}
				else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
					fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
				}
				else {
					throw new IllegalArgumentException("Expected a type");
				}
				
				// Get varName
				tokenizer.advance();
				
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
					throw new IllegalArgumentException("Not an identifier");
				}
				
				fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
				
				// NEW CODE -------------------------------------------------------------------------------------------
				name = tokenizer.getToken();
				symbolTable.define(name, type, Symbol.Kind.ARG);
				fw.write(symbolTable.subroutineMap.get(name).toString());
				
				tokenizer.advance();
			}
			
			tokenizer.index--;
			
			fw.write("</parameterList>\n");

			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileSubroutineBody() {
		try {
			fw.write("<subroutineBody>\n");

			// Get '{'
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("{")) {
				throw new IllegalArgumentException("Expected '{'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
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

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			fw.write("</subroutineBody>\n");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileVarDec() {
		try {
			// Get 'var' or return if statement
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				throw new IllegalArgumentException("Not a keyword");
			}
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.VAR)) {
				tokenizer.index--;
				return;
			}

			while(tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD) && tokenizer.keyWord().equals(JackTokenizer.KeyWord.VAR)) {

				fw.write("<varDec>\n");
				
				// NEW CODE -------------------------------------------------------------------------------------------
				kind = Symbol.Kind.VAR;
				
				fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");

				// Get type
				tokenizer.advance();

				if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
					if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.INT) && 
							!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CHAR) &&
							!tokenizer.keyWord().equals(JackTokenizer.KeyWord.BOOLEAN)) {
						throw new IllegalArgumentException("Expected an int, char, or boolean");
					}
					else {
						fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");
					}
				}
				else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
					fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
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

				fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
				
				// NEW CODE -------------------------------------------------------------------------------------------
				name = tokenizer.getToken();
				symbolTable.define(name, type, kind);
				fw.write(symbolTable.subroutineMap.get(name).toString());

				// Get (',' varName)*
				tokenizer.advance();

				while (tokenizer.getToken().equals(",")) {
					if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
						throw new IllegalArgumentException("Not a symbol");
					}
					if (!tokenizer.symbol().equals(",")) {
						throw new IllegalArgumentException("Expected ','");
					}

					fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");

					tokenizer.advance();

					if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
						throw new IllegalArgumentException("Not an identifier");
					}

					fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
					
					// NEW CODE -------------------------------------------------------------------------------------------
					name = tokenizer.getToken();
					symbolTable.define(name, type, kind);
					fw.write(symbolTable.subroutineMap.get(name).toString());

					tokenizer.advance();
				}

				// Get ';'
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					throw new IllegalArgumentException("Not a symbol");
				}
				if (!tokenizer.symbol().equals(";")) {
					throw new IllegalArgumentException("Expected ';'");
				}

				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");

				tokenizer.advance();
				
				fw.write("</varDec>\n");
			}
			
			tokenizer.index--;
			
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileStatements() {
		try {
			// Determine if statement, or }
			tokenizer.advance();

			if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				tokenizer.index--;
				return;
			}
			
			fw.write("<statements>\n");

			while (!tokenizer.getToken().equals("}")) {

				// Get ('constructor' | 'function' | 'method')
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
					throw new IllegalArgumentException("Not a keyword for a statement");
				}
				if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.LET)) {
					compileLet();
				}
				else if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.IF)) {
					compileIf();
				}
				else if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.WHILE)) {
					compileWhile();
				}
				else if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.DO)) {
					compileDo();
				}
				else if (tokenizer.keyWord().equals(JackTokenizer.KeyWord.RETURN)) {
					compileReturn();
				}
				else {
					System.out.println(tokenizer.getToken());
					throw new IllegalArgumentException("Expected a statement");
				}

				tokenizer.advance();

			}
			
			tokenizer.index--;
			
			fw.write("</statements>\n");

			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void compileDo() {
		try {
			fw.write("<doStatement>\n");

			// Get 'do'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				throw new IllegalArgumentException("Not a keyword");
			}
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.DO)) {
				throw new IllegalArgumentException("do");
			}

			fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");

			compileSubroutineCall();

			// Get ';'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(";")) {
				throw new IllegalArgumentException("Expected ';'");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			fw.write("</doStatement>\n");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void compileLet() {
		try {
			fw.write("<letStatement>\n");

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
