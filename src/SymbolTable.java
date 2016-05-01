import java.util.HashMap;

public class SymbolTable {
	private int staticIndex;
	private int fieldIndex;
	private int argIndex;
	private int varIndex;
	public HashMap<String, Symbol> classMap;
	public HashMap<String, Symbol> subroutineMap;
	
	public SymbolTable() {
		this.classMap = new HashMap<String, Symbol>();
		this.subroutineMap = new HashMap<String, Symbol>();
		this.staticIndex = 0;
		this.fieldIndex = 0;
		this.argIndex = 0;
		this.varIndex = 0;
		return;
	}

	public void startSubroutine() {
		this.subroutineMap.clear();
		this.argIndex = 0;
		this.varIndex = 0;
		return;
	}
	
	public void define(String name, String type, Symbol.Kind kind) {
		if (kind.equals(Symbol.Kind.STATIC)) {
			Symbol symbol = new Symbol(type, kind, staticIndex);
			classMap.put(name, symbol);
			staticIndex++;
		}
		else if (kind.equals(Symbol.Kind.FIELD)) {
			Symbol symbol = new Symbol(type, kind, fieldIndex);
			classMap.put(name, symbol);
			fieldIndex++;
		}
		else if (kind.equals(Symbol.Kind.ARG)) {
			Symbol symbol = new Symbol(type, kind, argIndex);
			subroutineMap.put(name, symbol);
			argIndex++;
		}
		else if (kind.equals(Symbol.Kind.VAR)) {
			Symbol symbol = new Symbol(type, kind, varIndex);
			subroutineMap.put(name, symbol);
			varIndex++;
		}
		return;
	}
	
	public int varCount (Symbol.Kind kind) {
		if (kind.equals(Symbol.Kind.STATIC)) {
			return staticIndex;
		}
		else if (kind.equals(Symbol.Kind.FIELD)) {
			return fieldIndex;
		}
		else if (kind.equals(Symbol.Kind.ARG)) {
			return argIndex;
		}
		else if (kind.equals(Symbol.Kind.VAR)) {
			return varIndex;
		}
		return Integer.MIN_VALUE;
	}
	
	public Symbol.Kind kindOf(String name) {
		if (!(classMap.get(name) == null)) {
			return classMap.get(name).getKind();
		}
		else if (!(subroutineMap.get(name) == null)) {
			return subroutineMap.get(name).getKind();
		}
		else {
			return Symbol.Kind.NONE;
		}
	}
	
	public String typeOf(String name) {
		if (!(classMap.get(name) == null)) {
			return classMap.get(name).getType();
		}
		else if (!(subroutineMap.get(name) == null)) {
			return subroutineMap.get(name).getType();
		}
		else {
			return "";
		}
	}
	
	public int indexOf(String name) {
		if (!(classMap.get(name) == null)) {
			return classMap.get(name).getIndex();
		}
		else if (!(subroutineMap.get(name) == null)) {
			return subroutineMap.get(name).getIndex();
		}
		else {
			return -1;
		}
	}
}
