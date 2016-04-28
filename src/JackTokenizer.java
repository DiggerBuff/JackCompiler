import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {
	public enum TokenType {
		KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
	}
	
	public enum KeyWord {
		CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, 
		VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE, RETURN, 
		TRUE, FALSE, NULL, THIS
	}
	
	private FileReader reader;
	private Scanner scan;
	static final HashMap<String, KeyWord> keyWordMap = new HashMap<String, KeyWord>();
	static final HashSet<String> opSet = new HashSet<String>();
	private String token;
	private TokenType type;
	private ArrayList<String> tokens;
	public int index;
	
	private Pattern patterns;
	private String keyWordPattern = "";
	private String symbolPattern = "";
	private String identifierPattern = "";
	private String intPattern = "";
	private String stringPattern = "";
	
	static {	
        keyWordMap.put("class", 		KeyWord.CLASS);
        keyWordMap.put("method",		KeyWord.METHOD);
        keyWordMap.put("function", 		KeyWord.FUNCTION);
        keyWordMap.put("constructor", 	KeyWord.CONSTRUCTOR);
        keyWordMap.put("int", 			KeyWord.INT);
        keyWordMap.put("boolean", 		KeyWord.BOOLEAN);
        keyWordMap.put("char",			KeyWord.CHAR);
        keyWordMap.put("void", 			KeyWord.VOID);
        keyWordMap.put("var", 			KeyWord.VAR);
        keyWordMap.put("static", 		KeyWord.STATIC);
        keyWordMap.put("field", 		KeyWord.FIELD);
        keyWordMap.put("let", 			KeyWord.LET);
        keyWordMap.put("do", 			KeyWord.DO);
        keyWordMap.put("if", 			KeyWord.IF);
        keyWordMap.put("else", 			KeyWord.ELSE);
        keyWordMap.put("while", 		KeyWord.WHILE);
        keyWordMap.put("return", 		KeyWord.RETURN);
        keyWordMap.put("true", 			KeyWord.TRUE);
        keyWordMap.put("false", 		KeyWord.FALSE);
        keyWordMap.put("null", 			KeyWord.NULL);
        keyWordMap.put("this", 			KeyWord.THIS); 

        opSet.add("+");
        opSet.add("-");
        opSet.add("*");
        opSet.add("/");
        opSet.add("&");
        opSet.add("|");
        opSet.add("<");
        opSet.add(">");
        opSet.add("=");
    }
	
	// CONSTRUCTOR--------------------------------------------------------------------------------------------------------------------------
	public JackTokenizer(File file) {
		try {
			this.reader = new FileReader(file);
			this.scan = new Scanner(reader);
			this.tokens = new ArrayList<String>();
			this.token = "";
			this.index = 0;
			
			String line;
			String tokenStream = "";
			
			while(scan.hasNext()) {
				line = scan.nextLine();
				line = removeComments(line).trim();
				
				if (line.length() > 0) {
					tokenStream = tokenStream + "\n" + line;
				}
			}
			
			createPatterns();
			
			Matcher m = patterns.matcher(tokenStream);
			
			while(m.find()) {
				tokens.add(m.group());
			}			
			
		} catch (FileNotFoundException e) {
			System.out.println("Could not create reader for : " + file.getName());
		}
		
		return;
	}
	
	// METHODS-------------------------------------------------------------------------------------------------------------------------------
	public boolean hasMoreTokens() {
		if(index < tokens.size()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void advance() {
		token = tokens.get(index);
		
		type = tokenType(token);

		index++;
		
		return;
	}

	public TokenType tokenType(String token) {
		if (token.matches(keyWordPattern)) {
			return TokenType.KEYWORD;
		}
		else if (token.matches(symbolPattern)) {
			return TokenType.SYMBOL;		
		}
		else if (token.matches(identifierPattern)) {
			return TokenType.IDENTIFIER;		
		}
		else if (token.matches(intPattern)) {
			return TokenType.INT_CONST;		
		}
		else if (token.matches(stringPattern)) {
			return TokenType.STRING_CONST;		
		}
		else {
			throw new IllegalArgumentException("Unidentified token type");		
		}
	}
	
	public KeyWord keyWord() {
		if (type == TokenType.KEYWORD) {
			return keyWordMap.get(token);
		}
		else {
			throw new IllegalArgumentException("Token not a keyword");
		}
	}
	
	public String symbol() {
		if (type == TokenType.SYMBOL) {
			return token;
		}
		else {
			throw new IllegalArgumentException("Token not a symbol");
		}
	}
	
	public String identifier() {
		if (type == TokenType.IDENTIFIER) {
			return token;
		}
		else {
			throw new IllegalArgumentException("Token not an identifier");
		}
	}
	
	public int intVal() {
		if (type == TokenType.INT_CONST) {
			return Integer.parseInt(token);
		}
		else {
			throw new IllegalArgumentException("Token not an integer");
		}
	}
	
	public String stringVal() {
		if (type == TokenType.STRING_CONST) {
			return token.substring(token.indexOf('"') + 1, token.lastIndexOf('"'));
		}
		else {
			throw new IllegalArgumentException("Token not a string constant");
		}
	}
	
	public String removeComments(String line) {
		if (line.contains("/*")) {
			String start = line.substring(0, line.indexOf("/*"));
			String end = "";
			
			if (line.contains("*/")) {
				end = line.substring(line.indexOf("*/") + 2);
			}
			else {
				String theLine = "";
				while(scan.hasNext()) {
					theLine = scan.nextLine();
					if (theLine.contains("*/")) {
						end = theLine.substring(theLine.indexOf("*/") + 2);
						break;
					}
				}
			}
			
			if (start.contains("//")) {
				start = start.substring(0,  start.indexOf("//"));
			}
			if (end.contains("//")) {
				end = end.substring(0,  end.indexOf("//"));
			}
			line = start + " " + end;
		}
		else if (line.contains("//")) {
			line = line.substring(0,  line.indexOf("//"));
		}
		return line;
	}
	
	public void createPatterns() {
		for (String key : keyWordMap.keySet()) {
			keyWordPattern += key + "|";
		}
		
		symbolPattern = "[\\{\\}\\(\\)\\[\\]\\.\\,\\;\\+\\-\\*\\/\\&\\|\\<\\>\\=\\~]";
		identifierPattern = "[a-zA-Z_][\\w]*";
		intPattern = "\\d+";
		stringPattern = "\"[^\"\n]*\"";
		
		patterns = Pattern.compile(keyWordPattern + symbolPattern + "|" + 
				identifierPattern + "|" + intPattern + "|" + stringPattern);
	}

	public String getToken() {
		return token;
	}
	
	public String peekToken() {
		if (index < tokens.size() - 1) {
			return tokens.get(index + 1);
		}
		else {
			return null;
		}
	}
	
	public TokenType getType() {
		return type;
	}
	
	public TokenType peekType() {
		String nextToken = peekToken();
		if (nextToken.equals(null)) {
			return null;
		}
		return tokenType(nextToken);
	}
}
