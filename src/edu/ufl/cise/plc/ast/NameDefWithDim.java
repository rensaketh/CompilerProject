package edu.ufl.cise.plc.ast;
import edu.ufl.cise.plc.IToken;
import edu.ufl.cise.plc.ast.Types.Type;
public class NameDefWithDim extends NameDef{
	final Dimension dim;
	public NameDefWithDim(IToken firstToken, String type, String name, Dimension
			dim) {
		super(firstToken,type, name);
		this.dim = dim;
	}
	public NameDefWithDim(IToken firstToken, IToken type, IToken name, Dimension
			dim) {
		super(firstToken,type, name);
		this.dim = dim;
	}
	public Dimension getDim() {
		return dim;
	}
	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitNameDefWithDim(this,arg);
	}
	@Override
	public String toString() {
		return "NameDefWithDim [dim=" + dim + ", name=" + name + ", type=" +
				type + "]";
	}
}