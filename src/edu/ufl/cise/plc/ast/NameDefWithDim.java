package edu.ufl.cise.plc.ast;

import edu.ufl.cise.plc.IToken;

public class NameDefWithDim extends NameDef{

	final Dimension dim;

	public NameDefWithDim(IToken firstToken, String name, String type, Dimension dim) {
		super(firstToken,name,type);
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
		return "NameDefWithDim [dim=" + dim + ", name=" + name + ", type=" + type + "]";
	}
	

}
