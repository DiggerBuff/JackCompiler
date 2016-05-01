
public class Symbol {
	public enum Kind {STATIC, INT, FIELD, ARG, VAR, NONE }
	
	private String type;
	private Kind kind;
	private int index;
	
	public Symbol(String type, Kind kind, int index) {
		this.type = type;
		this.kind = kind;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}

	@Override
	public String toString() {
		return "Symbol [type=" + type + ", kind=" + kind + ", index=" + index + "]";
	}
}
