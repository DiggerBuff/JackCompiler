import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

public class CompilationEngine {
	private JackTokenizer tokenizer;
	private FileWriter fw, fwT;
	
	public CompilationEngine(File inputFile, File outputFileT, File outputFile) {		
		this.tokenizer = new JackTokenizer(inputFile);
		
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
			
			// Get varName
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}
			
			fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			
			
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
					}
				}
				else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
					fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
				}
				else {
					throw new IllegalArgumentException("Expected a type");
				}
				
				tokenizer.advance();
				
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
					throw new IllegalArgumentException("Not an identifier");
				}
				
				fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
				
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
			
			compileVarDec();
			
			//compile statements
			fw.write("<statements>\n");
			
			compileStatements();
			
			fw.write("</statements>\n");

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
			
			// Get varName
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}

			fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");

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
			
			fw.write("</varDec>\n");
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void compileStatements() {

		// Determine if statement, or }
		tokenizer.advance();

		if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			tokenizer.index--;
			return;
		}

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
			throw new IllegalArgumentException("Expected a statement");
		}

		// Check for more statements
		compileStatements();

		return;
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

			//compileSubroutineCall();
			// THIS IS A PLACEHOLDER TO DEAL WITH EXPRESSION UNTIL I IMPLEMENT IT
			tokenizer.advance();
			fw.write("<SUBROUTINECALL> " + tokenizer.getToken() + " </SUBROUTINECALL>\n");
			tokenizer.advance();
			fw.write("<SUBROUTINECALL> " + tokenizer.getToken() + " </SUBROUTINECALL>\n");
			tokenizer.advance();
			fw.write("<SUBROUTINECALL> " + tokenizer.getToken() + " </SUBROUTINECALL>\n");
			tokenizer.advance();
			fw.write("<SUBROUTINECALL> " + tokenizer.getToken() + " </SUBROUTINECALL>\n");
			tokenizer.advance();
			fw.write("<SUBROUTINECALL> " + tokenizer.getToken() + " </SUBROUTINECALL>\n");

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

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}

			fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");

			// Get ('[' expression ']') or '='
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (tokenizer.getToken().equals("[")) {
				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
				compileExpression();
				
				if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					throw new IllegalArgumentException("Not a symbol");
				}
				if (!tokenizer.symbol().equals("]")) {
					throw new IllegalArgumentException("Expected ']'");
				}

				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			}

			// Get '='
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("=")) {
				throw new IllegalArgumentException("Expected '='");
			}

			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			//compileExpression();
			// THIS IS A PLACEHOLDER TO DEAL WITH EXPRESSION UNTIL I IMPLEMENT IT
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}

			fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");

			// Get ';'
			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(";")) {
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
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("(")) {
				throw new IllegalArgumentException("Expected '('");
			}
			
			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			compileExpression();
			
			fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");

			// Get ')'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(")")) {
				throw new IllegalArgumentException("Expected ')'");
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
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("(")) {
				throw new IllegalArgumentException("Expected '('");
			}
			
			fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
			
			compileExpression();
			
			fw.write("<keyword> " + tokenizer.getToken() + " </keyword>\n");

			// Get ')'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(")")) {
				throw new IllegalArgumentException("Expected ')'");
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
<<<<<<< HEAD
		return;
	}
	
	
	private void compileSubroutineCall() {
		// TODO Auto-generated method stub
		
	}

	
	public void compileTerm() {
		return;
=======
		try {
			fw.write("<expression>\n");
			
			compileTerm();
			
			// Get (op term)*
			tokenizer.advance();
			
			while (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL) && JackTokenizer.opSet.contains(tokenizer.symbol())) {

				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
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

			if (tokenizer.getType().equals(JackTokenizer.TokenType.INT_CONST)) {

				fw.write("<integerConstant> " + tokenizer.intVal() + " </integerConstant>\n");

			}
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.STRING_CONST)) {
				fw.write("<stringConstant> " + tokenizer.stringVal() + " </stringConstant>\n");
			}
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD) && 
						(tokenizer.keyWord().equals(JackTokenizer.KeyWord.TRUE) || 
							tokenizer.keyWord().equals(JackTokenizer.KeyWord.FALSE) ||
							tokenizer.keyWord().equals(JackTokenizer.KeyWord.NULL) ||
							tokenizer.keyWord().equals(JackTokenizer.KeyWord.THIS))) {

				fw.write("<keywordConstant> " + tokenizer.keyWord() + " </keywordConstant>\n");
			}
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				// Could be varName | varName '[' expression ']' | subroutineCall
				// Get '[' | '('
				tokenizer.advance();

				if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					// varName '[' expression ']'  
					if (tokenizer.symbol().equals("[")) {
						tokenizer.index = tokenizer.index - 2;
						
						tokenizer.advance();
						
						fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
						
						tokenizer.advance();
						
						fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
						
						compileExpressionList();
						
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
					else if (tokenizer.symbol().equals("(")) {
						tokenizer.index = tokenizer.index - 2;
						
						compileSubroutineCall();
						
					}
					else {
						tokenizer.index = tokenizer.index - 2;
						
						tokenizer.advance();
						
						fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
					}
				}
				else {
					tokenizer.index = tokenizer.index - 2;
					
					tokenizer.advance();
					
					fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
				}

				fw.write("<symbol> " + tokenizer.symbol() + " </symbol>\n");
				
				fw.write("<identifier> " + tokenizer.identifier() + " </identifier>\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

>>>>>>> ec206f216251612746a062194036e362f7fdb1ea
	}
	
	public void compileExpressionList() {
		return;
	}
}
