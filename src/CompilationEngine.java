import java.io.File;

public class CompilationEngine {
	private JackTokenizer tokenizer;
	private SymbolTable symbolTable;
	private VMWriter vm;
	private String className;
	private String subrName;
	private JackTokenizer.KeyWord keyword;
	
	// To set for each symbol to add to the symbol table
	private String name;
	private String type;
	private Symbol.Kind kind;
	private int labelIndex;
	
	public CompilationEngine(File inputFile, File outputFile) {	
		this.tokenizer = new JackTokenizer(inputFile);
		this.symbolTable = new SymbolTable();
		this.vm = new VMWriter(outputFile);
		this.labelIndex = 0;

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
		
		// Get class name
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			throw new IllegalArgumentException("Not an identifier");
		}		
		
		this.className = tokenizer.identifier();
		
		// Get '{'
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("{")) {
			System.out.println(tokenizer.getToken());
			throw new IllegalArgumentException("Expected '{'");
		}

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

		// End and close
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

		// Get ('static' | 'field')
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			throw new IllegalArgumentException("Not a keyword");
		}
		if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.STATIC) && !tokenizer.keyWord().equals(JackTokenizer.KeyWord.FIELD)) {
			throw new IllegalArgumentException("Expected 'static' or 'field'");
		}

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
				type = tokenizer.getToken();
			}
		}
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
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

		name = tokenizer.identifier();
		symbolTable.define(name, type, kind);			
		
		// Get (',' varName)*
		tokenizer.advance();
		
		while (tokenizer.getToken().equals(",")) {
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(",")) {
				throw new IllegalArgumentException("Expected ','");
			}

			tokenizer.advance();
			
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}

			name = tokenizer.identifier();
			symbolTable.define(name, type, kind);
			
			tokenizer.advance();
		}
		
		// Get ';'
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals(";")) {
			throw new IllegalArgumentException("Expected ';'");
		}

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

		// Get ('constructor' | 'function' | 'method')
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			throw new IllegalArgumentException("Not a keyword");
		}
		if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CONSTRUCTOR) && 
				!tokenizer.keyWord().equals(JackTokenizer.KeyWord.FUNCTION) &&
				!tokenizer.keyWord().equals(JackTokenizer.KeyWord.METHOD)) {
			throw new IllegalArgumentException("Expected 'constructor', 'function', or 'method'");
		}

		keyword = tokenizer.keyWord();

		symbolTable.startSubroutine();	

		// for method this is the first argument
        if (tokenizer.keyWord() == JackTokenizer.KeyWord.METHOD){
            symbolTable.define("this", className, Symbol.Kind.ARG);
        }

		// Get ('void' | type)
		tokenizer.advance();
		
		if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
			if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.INT) && 
					!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CHAR) &&
					!tokenizer.keyWord().equals(JackTokenizer.KeyWord.BOOLEAN) &&
					!tokenizer.keyWord().equals(JackTokenizer.KeyWord.VOID)) {
				throw new IllegalArgumentException("Expected an int, char, boolean, or void");
			}
			else {
				type = tokenizer.getToken();
			}
		}
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			type = tokenizer.getToken();
		}
		else {
			throw new IllegalArgumentException("Expected a type");
		}

		// Get subroutineName
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			throw new IllegalArgumentException("Not an identifier");
		}
		
		this.subrName = tokenizer.identifier();
		
		
		// Get '('
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("(")) {
			throw new IllegalArgumentException("Expected '('");
		}

		compileParameterList();

		// Get ')'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals(")")) {
			throw new IllegalArgumentException("Expected ')'");
		}

		compileSubroutineBody();

		// Check for more classVarDec
		compileSubroutine();

		return;
	}
	
	public void compileParameterList() {
		// Determine if parameter or ')'
		tokenizer.advance();
		
		if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			tokenizer.index--;
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
				type = tokenizer.getToken();
			}
		}
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
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

		name = tokenizer.getToken();
		symbolTable.define(name, type, Symbol.Kind.ARG);
		
		
		// Get (',' type varName)*
		tokenizer.advance();
		
		while (tokenizer.getToken().equals(",")) {
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(",")) {
				throw new IllegalArgumentException("Expected ','");
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
					type = tokenizer.getToken();
				}
			}
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
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

			name = tokenizer.getToken();
			symbolTable.define(name, type, Symbol.Kind.ARG);
			
			tokenizer.advance();
		}
		
		tokenizer.index--;

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

		//Check multiple varDec
		compileVarDec();
		
		// Declare a new function
		writeFunctionDec();
		
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

		return;
	}
	
	public void writeFunctionDec() {
		vm.writeFunction(currentFunction(), symbolTable.varCount(Symbol.Kind.VAR));
		
		if (keyword.equals(JackTokenizer.KeyWord.CONSTRUCTOR)) {
			vm.writePush(VMWriter.Segment.CONST, symbolTable.varCount(Symbol.Kind.FIELD));
			vm.writeCall("Memory.alloc", 1);
			vm.writePop(VMWriter.Segment.POINTER, 0);
		}
		else if (keyword.equals(JackTokenizer.KeyWord.METHOD)) {
			vm.writePush(VMWriter.Segment.ARG, 0);
			vm.writePop(VMWriter.Segment.POINTER, 0);
		}	
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
		
		while(tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD) && tokenizer.keyWord().equals(JackTokenizer.KeyWord.VAR)) {

			kind = Symbol.Kind.VAR;

			// Get type
			tokenizer.advance();

			if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD)) {
				if (!tokenizer.keyWord().equals(JackTokenizer.KeyWord.INT) && 
						!tokenizer.keyWord().equals(JackTokenizer.KeyWord.CHAR) &&
						!tokenizer.keyWord().equals(JackTokenizer.KeyWord.BOOLEAN)) {
					throw new IllegalArgumentException("Expected an int, char, or boolean");
				}
				else {
					type = tokenizer.getToken();
				}
			}
			else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
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

			name = tokenizer.getToken();
			symbolTable.define(name, type, kind);

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

				name = tokenizer.getToken();
				symbolTable.define(name, type, kind);

				tokenizer.advance();
			}

			// Get ';'
			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(";")) {
				throw new IllegalArgumentException("Expected ';'");
			}

			tokenizer.advance();
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

		while (!tokenizer.getToken().equals("}")) {

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

		return;
	}

	public void compileDo() {

		compileSubroutineCall();

		// Get ';'
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals(";")) {
			throw new IllegalArgumentException("Expected ';'");
		}

		// Pop return value from subroutine call
		// pop temp 0
		vm.writePop(VMWriter.Segment.TEMP, 0);
		
		return;
	}

	public void compileLet() {

		// Get varName
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			throw new IllegalArgumentException("Not an identifier");
		}

		String varName = tokenizer.getToken();
		System.out.println("Let : varName = " + name);

		boolean arrayVar = false;

		// Get ('[' expression ']') or '='
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (tokenizer.getToken().equals("[")) {

			arrayVar = true;

			// Need to push base address onto stack
			System.out.println("Let : arrayVar base address " + varName);
			vm.writePush(getSegment(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));

			compileExpression();

			// Get ']'
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("]")) {
				throw new IllegalArgumentException("Expected ']'");
			}

			// Need to add expression to base address
			vm.writeArithmetic(VMWriter.Command.ADD);

			tokenizer.advance();
		}

		// Get '='
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("=")) {
			throw new IllegalArgumentException("Expected '='");
		}

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


		// handle arrayVar
		if (arrayVar) {
			// expression is at top of stack, need to pop to temp to get at address + offset
			vm.writePop(VMWriter.Segment.TEMP, 0);
			// put address + offset in that
			vm.writePop(VMWriter.Segment.POINTER, 1);
			// push expression
			vm.writePush(VMWriter.Segment.TEMP, 0);
			// pop expression to *that
			vm.writePop(VMWriter.Segment.THAT, 0);
		}
		else {
			vm.writePop(getSegment(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));
		}

		return;
	}
	
	private VMWriter.Segment getSegment(Symbol.Kind kindOf) {
		switch (kindOf) {
			case STATIC:return VMWriter.Segment.STATIC;
			case FIELD:	return VMWriter.Segment.THIS;
			case ARG:	return VMWriter.Segment.ARG;
			case VAR:	return VMWriter.Segment.LOCAL;
			default:	return VMWriter.Segment.NONE;
		}
	}
	
	public void compileWhile() {
		
		String whileStartLabel = "WHILE_" + labelIndex++;
		String whileEndLabel = "WHILE_END_" + labelIndex++;
		
		vm.writeLabel(whileStartLabel);

		// Get '('
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("(")) {
			throw new IllegalArgumentException("Expected '('");
		}

		compileExpression();

		// Get ')'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals(")")) {
			throw new IllegalArgumentException("Expected ')'");
		}
		
		// not
		vm.writeArithmetic(VMWriter.Command.NOT);
		
		// if-goto WHILE_END
		vm.writeIf(whileEndLabel);

		// Get '{'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("{")) {
			throw new IllegalArgumentException("Expected '{'");
		}

		compileStatements();

		// Get '}'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("}")) {
			throw new IllegalArgumentException("Expected '}'");
		}
		
		// goto WHILE
		vm.writeGoto(whileStartLabel);
		
		// label while_end
		vm.writeLabel(whileEndLabel);

		return;
	}

	public void compileReturn() {

		// Check if ';' or expression
		tokenizer.advance();

		if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			if (tokenizer.symbol().equals(";")) {
				vm.writePush(VMWriter.Segment.CONST, 0);
				vm.writeReturn();
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
		
		vm.writeReturn();

		return;
	}

	public void compileIf() {

		// Get '('
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			System.out.println(tokenizer.getToken());
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("(")) {
			throw new IllegalArgumentException("Expected '('");
		}

		compileExpression();

		// Get ')'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals(")")) {
			throw new IllegalArgumentException("Expected ')'");
		}

		//not
		vm.writeArithmetic(VMWriter.Command.NOT);

		// if goto if_False_label
		String ifFalseLabel = "IF_FALSE_" + labelIndex++;

		vm.writeIf(ifFalseLabel);

		// Get '{'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("{")) {
			throw new IllegalArgumentException("Expected '{'");
		}

		compileStatements();

		// Get '}'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("}")) {
			throw new IllegalArgumentException("Expected '}'");
		}

		// goto if end
		String ifEndLabel = "IF_END_" + labelIndex++;

		vm.writeGoto(ifEndLabel);

		// label if false
		vm.writeLabel(ifFalseLabel);

		// Check for else
		tokenizer.advance();

		if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD) && tokenizer.keyWord().equals(JackTokenizer.KeyWord.ELSE)) {
			compileElse();
		}
		else {
			tokenizer.index--;
		}

		// label if end
		vm.writeLabel(ifEndLabel);

		return;
	}
	
	public void compileElse() {

		// Get '{'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("{")) {
			throw new IllegalArgumentException("Expected '{'");
		}

		compileStatements();

		// Get '}'
		tokenizer.advance();
		
		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		if (!tokenizer.symbol().equals("}")) {
			throw new IllegalArgumentException("Expected '}'");
		}

		return;
	}
	
	public void compileExpression() {

		compileTerm();

		// Get (op term)*
		tokenizer.advance();

		boolean isCall = false;

		while (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL) && JackTokenizer.opSet.contains(tokenizer.symbol())) {

			VMWriter.Command command = null;
			String call = "";

			switch(tokenizer.symbol()){
				case "+": command = VMWriter.Command.ADD;break;
				case "-": command = VMWriter.Command.SUB;break;
				case "=": command = VMWriter.Command.EQ;break;
				case ">": command = VMWriter.Command.GT;break;
				case "<": command = VMWriter.Command.LT;break;
				case "&": command = VMWriter.Command.AND;break;
				case "|": command = VMWriter.Command.OR;break;
				case "*": call = "Math.multiply"; isCall = true; break;
				case "/": call = "Math.divide"; isCall = true; break;
			}

			compileTerm();

			if (isCall) {
				vm.writeCall(call, 2);
				isCall = false;
			}
			else {
				vm.writeArithmetic(command);
			}

			tokenizer.advance();
		}

		tokenizer.index--;

		return;
	}
	
	private void compileSubroutineCall() {

		// Get 'subroutineName' / 'className' / 'varName'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
			throw new IllegalArgumentException("Not an identifier");
		}

		name = tokenizer.getToken();

		int nArgs = 0;

		// Get '(' or '.'
		tokenizer.advance();

		if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			throw new IllegalArgumentException("Not a symbol");
		}
		// subroutineName ( expressionList )
		if (tokenizer.symbol().equals("(")) {

			// push this
			vm.writePush(VMWriter.Segment.POINTER, 0);

			// process expression list and get number of expressions + 1 for this
			nArgs = compileExpressionList() + 1;

			// call currentClass.subroutineName numberOfExpressions
			vm.writeCall(className + "." + name, nArgs);

			// Get ')'
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(")")) {
				throw new IllegalArgumentException("Expected ')'");
			}
		}
		// (className | varName).subroutineName 
		else if (tokenizer.symbol().equals(".")) {
			
			String type = symbolTable.typeOf(name);

			// Get 'subroutineName'
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {
				throw new IllegalArgumentException("Not an identifier");
			}

			String subroutineName = tokenizer.identifier();
			
			if (type.equals("int") || type.equals("boolean") || type.equals("char") || type.equals("void")) {
				throw new IllegalArgumentException("Cannot make subroutine call on " + type);
			}
			// className
			else if (type.equals("")) {
				name = name + "." + subroutineName;
			}
			// varName
			else {
				// push segment offset
				System.out.println("varName : push segment offset : push +_kindOf : " + name );
				vm.writePush(getSegment(symbolTable.kindOf(name)), symbolTable.indexOf(name));
				nArgs = 1;
				// change name to classOfVar.subroutineName
				name = symbolTable.typeOf(name) + "." + subroutineName;	
			}

			// Get '('
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals("(")) {
				throw new IllegalArgumentException("Expected '('");
			}

			// should be + 1 for varName and + 0 for className
			nArgs = compileExpressionList() + nArgs;

			// Get ')'
			tokenizer.advance();

			if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				throw new IllegalArgumentException("Not a symbol");
			}
			if (!tokenizer.symbol().equals(")")) {
				throw new IllegalArgumentException("Expected ')'");
			}

			// call (className | varName).subroutineName numberOfExpressions
			System.out.println("writeCall : name " + name + " nArgs : " + nArgs);
			vm.writeCall(name, nArgs);

		} 
		else {
			throw new IllegalArgumentException("Expected '(' or '.'");
		}

		return;
	}

	public void compileTerm() {

		tokenizer.advance();

		// integerConstant
		if (tokenizer.getType().equals(JackTokenizer.TokenType.INT_CONST)) {
			// push constant intConst
			System.out.println("Term : push constant int " + tokenizer.getToken());
			vm.writePush(VMWriter.Segment.CONST, Integer.parseInt(tokenizer.getToken()));
		}
		// stringConstant
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.STRING_CONST)) {
			String string = tokenizer.getToken();
			int length = string.length();
			// push string length
			System.out.println("Term : push string length " + string);
			vm.writePush(VMWriter.Segment.CONST, length);
			// call String.new 1
			vm.writeCall("String.new", 1);

			for (int i = 0; i < length; i++) {
				// get ascii value of each char
				int ascii = (int) string.charAt(i);
				// push ascii
				System.out.println("Term : push ascii value " + ascii);
				vm.writePush(VMWriter.Segment.CONST, ascii);
				// call String.appendChar 2
				vm.writeCall("String.appendChar", 2);
			}
		}
		// keywordConstant TRUE
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD) && (tokenizer.keyWord().equals(JackTokenizer.KeyWord.TRUE))) {
			// push true (not 0)
			System.out.println("Term : push true " + tokenizer.getToken());
			vm.writePush(VMWriter.Segment.CONST, 0);
			vm.writeArithmetic(VMWriter.Command.NOT);
		}
		// keywordConstant FALSE
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD) && (tokenizer.keyWord().equals(JackTokenizer.KeyWord.FALSE))) {
			// push FALSE (0)
			System.out.println("Term : push false " + tokenizer.getToken());
			vm.writePush(VMWriter.Segment.CONST, 0);
		}
		// keywordConstant NULL
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD) && (tokenizer.keyWord().equals(JackTokenizer.KeyWord.NULL))) {
			// push NULL (0)
			System.out.println("Term : push null " + tokenizer.getToken());
			vm.writePush(VMWriter.Segment.CONST, 0);
		}
		// keywordConstant THIS
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.KEYWORD) && (tokenizer.keyWord().equals(JackTokenizer.KeyWord.THIS))) {
			// push THIS
			System.out.println("Term : push this " + tokenizer.getToken());
			vm.writePush(VMWriter.Segment.POINTER, 0);
		}
		// Could be varName | varName '[' expression ']' | subroutineCall
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.IDENTIFIER)) {

			String identifier = tokenizer.getToken();

			// Check for following '[' | '('
			tokenizer.advance();

			if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
				// varName '[' expression ']'  
				if (tokenizer.symbol().equals("[")) {

					System.out.println("Term : varName [ expression ] " + identifier);
					vm.writePush(getSegment(symbolTable.kindOf(identifier)), symbolTable.indexOf(identifier));

					compileExpression();

					// Get ']'
					tokenizer.advance();

					if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
						throw new IllegalArgumentException("Not a symbol");
					}
					if (!tokenizer.symbol().equals("]")) {
						throw new IllegalArgumentException("Expected ']'");
					}
					
					vm.writeArithmetic(VMWriter.Command.ADD);
					
					vm.writePop(VMWriter.Segment.POINTER, 1);
					
					vm.writePush(VMWriter.Segment.THAT, 0);
				}
				// subroutineCall == subroutineName '(' || (className | varName) '.'
				else if (tokenizer.symbol().equals("(") || tokenizer.symbol().equals(".")) {
					tokenizer.index = tokenizer.index - 2;

					compileSubroutineCall();
				}
				// varName if followed by symbol
				else {
					tokenizer.index--;

					System.out.println("Term : varName symbol " + identifier);
					vm.writePush(getSegment(symbolTable.kindOf(identifier)), symbolTable.indexOf(identifier));
				}
			}
			// varName if followed by non-symbol
			else {
				tokenizer.index--;

				System.out.println("Term : varName non-symbol " + identifier);
				vm.writePush(getSegment(symbolTable.kindOf(identifier)), symbolTable.indexOf(identifier));
			}
		}
		// Get '(' expression ')' | unaryOp term
		else if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			// '(' expression ')' 
			if (tokenizer.symbol().equals("(")) {

				compileExpression();

				// Get ')'
				tokenizer.advance();

				if (!tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
					throw new IllegalArgumentException("Not a symbol");
				}
				if (!tokenizer.symbol().equals(")")) {
					throw new IllegalArgumentException("Expected ')'");
				}
			}
			// unaryOp term
			else {
				if (!tokenizer.symbol().equals("-") && !tokenizer.symbol().equals("~")) {
					System.out.println("token : " + tokenizer.getToken());
					throw new IllegalArgumentException("Expected '-' or '~'");
				}

				String op = tokenizer.symbol();

				compileTerm();

				if (op.equals("-")) {
					vm.writeArithmetic(VMWriter.Command.NEG);
				}
				else {
					vm.writeArithmetic(VMWriter.Command.NOT);
				}
			}
		}
		else {
			throw new IllegalArgumentException("Expected a term");
		}
	}
	
	public int compileExpressionList() {
		
		int nArgs = 0;

		// Check for ')'
		tokenizer.advance();

		if (tokenizer.getType().equals(JackTokenizer.TokenType.SYMBOL)) {
			if (tokenizer.symbol().equals(")")) {
				tokenizer.index--;
				return nArgs;
			}
		}

		tokenizer.index--;
		
		nArgs++;

		compileExpression();

		// Get (',' expression)*
		tokenizer.advance();

		while (tokenizer.getToken().equals(",")) {

			nArgs++;
			
			compileExpression();

			tokenizer.advance();
		}

		tokenizer.index--;

		return nArgs;
	}
	
    private String currentFunction(){

        if (className.length() != 0 && subrName.length() !=0){
            return className + "." + subrName;
        }
        
        return "";
    }
}
