package edu.ufl.cise.plc;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.Types.Type;

import java.util.List;
import java.util.Map;

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class TypeCheckVisitor implements ASTVisitor {

	SymbolTable symbolTable = new SymbolTable();  
	Program root;
	
	record Pair<T0,T1>(T0 t0, T1 t1){};  //may be useful for constructing lookup tables.
	
	private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
		if (!condition) {
			throw new TypeCheckException(message, node.getSourceLoc());
		}
	}


	//The type of a BooleanLitExpr is always BOOLEAN.  
	//Set the type in AST Node for later passes (code generation)
	//Return the type for convenience in this visitor.  
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		booleanLitExpr.setType(Type.BOOLEAN);
		return Type.BOOLEAN;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		//TODO:  implement this method
		stringLitExpr.setType(STRING);
		return STRING;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		//TODO:  implement this method
		intLitExpr.setType(INT);
		return INT;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		floatLitExpr.setType(Type.FLOAT);
		return Type.FLOAT;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		//TODO:  implement this method
		colorConstExpr.setType(COLOR);
		return COLOR;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		consoleExpr.setType(Type.CONSOLE);
		return Type.CONSOLE;
	}
	
	//Visits the child expressions to get their type (and ensure they are correctly typed)
	//then checks the given conditions.
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		Type redType = (Type) colorExpr.getRed().visit(this, arg);
		Type greenType = (Type) colorExpr.getGreen().visit(this, arg);
		Type blueType = (Type) colorExpr.getBlue().visit(this, arg);
		check(redType == greenType && redType == blueType, colorExpr, "color components must have same type");
		check(redType == Type.INT || redType == Type.FLOAT, colorExpr, "color component type must be int or float");
		Type exprType = (redType == Type.INT) ? Type.COLOR : Type.COLORFLOAT;
		colorExpr.setType(exprType);
		return exprType;
	}
	
	//Maps forms a lookup table that maps an operator expression pair into result type.  
	//This more convenient than a long chain of if-else statements. 
	//Given combinations are legal; if the operator expression pair is not in the map, it is an error. 
	Map<Pair<Kind,Type>, Type> unaryExprs = Map.of(
			new Pair<Kind,Type>(Kind.BANG,Type.BOOLEAN), Type.BOOLEAN,
			new Pair<Kind,Type>(Kind.MINUS, FLOAT), FLOAT,
			new Pair<Kind,Type>(Kind.MINUS, INT),INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,INT), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,COLOR), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,IMAGE), IMAGE,
			new Pair<Kind,Type>(Kind.IMAGE_OP,IMAGE), INT
			);

	//Visits the child expression to get the type, then uses the above table to determine the result type
	//and check that this node represents a legal combination of operator and expression type. 
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
		// !, -, getRed, getGreen, getBlue
		Kind op = unaryExpr.getOp().getKind();
		Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
		//Use the lookup table above to both check for a legal combination of operator and expression, and to get result type.
		Type resultType = unaryExprs.get(new Pair<Kind,Type>(op,exprType));
		check(resultType != null, unaryExpr, "incompatible types for unaryExpr");
		//Save the type of the unary expression in the AST node for use in code generation later. 
		unaryExpr.setType(resultType);
		//return the type for convenience in this visitor.
		return resultType;
	}

	//This method has several cases. Work incrementally and test as you go. 
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		//TODO:  implement this method
		Kind op = binaryExpr.getOp().getKind();
		Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
		Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
		Type resultType = null;
		if(op.equals(Kind.EQUALS) || op.equals(Kind.NOT_EQUALS)) {
			check(leftType == rightType, binaryExpr, "incompatible types for comparison");
			resultType = BOOLEAN;
		}
		else if(op.equals(Kind.AND) || op.equals(Kind.OR)) {
			if(leftType == BOOLEAN && rightType == BOOLEAN) {
				resultType = BOOLEAN;
			}
		}
		else if(op.equals(Kind.LT) || op.equals(Kind.LE) || op.equals(Kind.GT) || op.equals(Kind.GE)) {
			if(leftType == INT && rightType == INT) {
				resultType = BOOLEAN;
			}
			else if(leftType == FLOAT && rightType == FLOAT) {
				resultType = BOOLEAN;
			}
			else if(leftType == INT && rightType == FLOAT) {
				binaryExpr.getLeft().setCoerceTo(FLOAT);
				resultType = BOOLEAN;
			}
			else if(leftType == FLOAT && rightType == INT) {
				binaryExpr.getRight().setCoerceTo(FLOAT);
				resultType = BOOLEAN;
			}
		}
		else if(op.equals(Kind.PLUS) || op.equals(Kind.MINUS) || op.equals(Kind.TIMES) || op.equals(Kind.DIV) || op.equals(Kind.MOD)) {
			if(leftType == INT && rightType == INT) {
				resultType = INT;
			}
			else if(leftType == FLOAT && rightType == FLOAT) {
				resultType = FLOAT;
			}
			else if(leftType == INT && rightType == FLOAT) {
				binaryExpr.getLeft().setCoerceTo(FLOAT);
				resultType = FLOAT;
			}
			else if(leftType == FLOAT && rightType == INT) {
				binaryExpr.getRight().setCoerceTo(FLOAT);
				resultType = FLOAT;
			}
			else if(leftType == COLOR && rightType == COLOR) {
				resultType = COLOR;
			}
			else if(leftType == COLORFLOAT && rightType == COLORFLOAT) {
				resultType = COLORFLOAT;
			}
			else if(leftType == COLORFLOAT && rightType == COLOR) {
				binaryExpr.getRight().setCoerceTo(COLORFLOAT);
				resultType = COLORFLOAT;
			}
			else if(leftType == COLOR && rightType == COLORFLOAT) {
				binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
				resultType = COLORFLOAT;
			}
			else if(leftType == IMAGE && rightType == IMAGE) {
				resultType = IMAGE;
			}
		}
		if(op.equals(Kind.TIMES) || op.equals(Kind.DIV) || op.equals(Kind.MOD)) {
			if(leftType == IMAGE && rightType == INT) {
				resultType = IMAGE;
				//binaryExpr.getRight().setCoerceTo(COLOR);
			}
			else if(leftType == IMAGE && rightType == FLOAT) {
				resultType = IMAGE;
				//binaryExpr.getRight().setCoerceTo(COLORFLOAT);
			}
			else if(leftType == INT && rightType == COLOR) {
				binaryExpr.getLeft().setCoerceTo(COLOR);
				resultType = COLOR;
			}
			else if(leftType == COLOR && rightType == INT) {
				binaryExpr.getRight().setCoerceTo(COLOR);
				resultType = COLOR;
			}
			else if(leftType == FLOAT && rightType == COLOR) {
				binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
				binaryExpr.getRight().setCoerceTo(COLORFLOAT);
				resultType = COLORFLOAT;
			}
			else if(leftType == COLOR && rightType == FLOAT) {
				binaryExpr.getLeft().setCoerceTo(COLORFLOAT);
				binaryExpr.getRight().setCoerceTo(COLORFLOAT);
				resultType = COLORFLOAT;
			}
		}

		check(resultType != null, binaryExpr, "incompatible types for binaryExpr");
		binaryExpr.setType(resultType);
		return resultType;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		//TODO:  implement this method
		String name = identExpr.getText();
		Declaration dec = symbolTable.lookup(name);

		check(dec != null, identExpr, "unidentified identifier " + name);
		check(dec.isInitialized(), identExpr, "using uninitialized variable");
		identExpr.setDec(dec);

		Type type = dec.getType();
		//Type type = (Type) dec.visit(this, arg);
		identExpr.setType(type);
		return type;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		//TODO  implement this method
		Type conditionType = (Type) conditionalExpr.getCondition().visit(this, arg);
		check(conditionType == BOOLEAN, conditionalExpr, "conditional type not a boolean");
		Type trueCase = (Type) conditionalExpr.getTrueCase().visit(this, arg);
		Type falseCase = (Type) conditionalExpr.getFalseCase().visit(this, arg);
		check(trueCase == falseCase, conditionalExpr, "true case and false case have different types");
		conditionalExpr.setType(trueCase);
		return trueCase;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		//TODO  implement this method
		Type heightType = (Type) dimension.getHeight().visit(this, arg);
		Type widthType = (Type) dimension.getWidth().visit(this, arg);
		check(heightType == INT && widthType == INT, dimension, "height and width have to be int types");
		return INT;
		//throw new UnsupportedOperationException();
	}

	@Override
	//This method can only be used to check PixelSelector objects on the right hand side of an assignment. 
	//Either modify to pass in context info and add code to handle both cases, or when on left side
	//of assignment, check fields from parent assignment statement.
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type xType = (Type) pixelSelector.getX().visit(this, arg);
		check(xType == Type.INT, pixelSelector.getX(), "only ints as pixel selector components");
		Type yType = (Type) pixelSelector.getY().visit(this, arg);
		check(yType == Type.INT, pixelSelector.getY(), "only ints as pixel selector components");
		return null;
	}

	@Override
	//This method several cases--you don't have to implement them all at once.
	//Work incrementally and systematically, testing as you go.  
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		//TODO:  implement this method
		String name = assignmentStatement.getName();
		Declaration declaration = symbolTable.lookup(name);
		check(declaration != null, assignmentStatement, "undeclared variable " + name);
		Type targetType = declaration.getType();
		Expr expression = assignmentStatement.getExpr();
		Type expressionType = null;
		if(assignmentStatement.getSelector()==null){
			expressionType = (Type) assignmentStatement.getExpr().visit(this, arg);
		}

		assignmentStatement.setTargetDec(declaration);

		if(targetType != IMAGE) {
			check(assignmentStatement.getSelector() == null, assignmentStatement, "there should not be a pixel selector");
			if(targetType != expressionType) {
				if(targetType == INT && expressionType == FLOAT) {
					expression.setCoerceTo(INT);
				}
				else if(targetType == FLOAT && expressionType == INT) {
					expression.setCoerceTo(FLOAT);
				}
				else if(targetType == INT && expressionType == COLOR) {
					expression.setCoerceTo(INT);
				}
				else if(targetType == COLOR && expressionType == INT) {
					expression.setCoerceTo(COLOR);
				}
				else {
					throw new TypeCheckException("expression and target variable are not assignment compatible", assignmentStatement.getSourceLoc());
				}
			}
		}

		else if(targetType == IMAGE && assignmentStatement.getSelector() == null) {
			if(targetType != expressionType) {
				if(targetType == IMAGE && expressionType == INT) {
					expression.setCoerceTo(COLOR);
				}
				else if(targetType == IMAGE && expressionType == FLOAT) {
					expression.setCoerceTo(COLORFLOAT);
				}
				else if(targetType == IMAGE && expressionType == COLOR) {
					expression.setCoerceTo(COLOR);
				}
				else if(targetType == IMAGE && expressionType == COLORFLOAT) {
					expression.setCoerceTo(COLORFLOAT);
				}
				else {
					throw new TypeCheckException("expression and target variable are not assignment compatible", assignmentStatement.getSourceLoc());
				}
			}
		}
		else if(targetType == IMAGE && assignmentStatement.getSelector() != null) {
			String varX = assignmentStatement.getSelector().getX().getText();
			String varY = assignmentStatement.getSelector().getY().getText();

			if(assignmentStatement.getSelector().getX().getClass() != IdentExpr.class || assignmentStatement.getSelector().getY().getClass() != IdentExpr.class) {
				throw new TypeCheckException("expected ident expressions", assignmentStatement.getSourceLoc());
			}

			boolean insertedX = symbolTable.insert(varX, new VarDeclaration(null, new NameDef(null, "int", varX), null, assignmentStatement.getSelector().getX()));
			check(insertedX, assignmentStatement.getSelector().getX(), "variable " + varX + " already declared");

			boolean insertedY = symbolTable.insert(varY, new VarDeclaration(null, new NameDef(null, "int", varY), null, assignmentStatement.getSelector().getY()));
			check(insertedY, assignmentStatement.getSelector().getY(), "variable " + varY + " already declared");


			assignmentStatement.getSelector().getX().setType(INT);
			assignmentStatement.getSelector().getY().setType(INT);

			symbolTable.lookup(varX).setInitialized(true);
			symbolTable.lookup(varY).setInitialized(true);

			expressionType = (Type) expression.visit(this, arg);

			if(expressionType == COLOR || expressionType == COLORFLOAT || expressionType == FLOAT || expressionType == INT) {
				assignmentStatement.getExpr().setCoerceTo(COLOR);
			}
			else {
				throw new TypeCheckException("expression and target variable are not assignment compatible", assignmentStatement.getSourceLoc());
			}

			symbolTable.remove(varX);
			symbolTable.remove(varY);
		}

		declaration.setInitialized(true);
		return null;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	private Pair<Boolean, Type> assignStatementCompatible(Type target, Type expr, boolean isPixelSelector) {
		//returns compatible and coerce type
		if(target != IMAGE) {
			if(target == expr) {
				return new Pair(true, expr);
			}
			else if(target == INT && expr == FLOAT){
				return new Pair(true, INT);
			}
			else if(target == FLOAT && expr == INT){
				return new Pair(true, FLOAT);
			}
			else if(target == INT && expr == COLOR){
				return new Pair(true, INT);
			}
			else if(target == COLOR && expr == INT){
				return new Pair(true, COLOR);
			}
		}
		else if(target == IMAGE && !isPixelSelector) {
			if(target == expr) {
				return new Pair(true, expr);
			}
			else if(target == IMAGE && expr == INT){
				return new Pair(true, COLOR);
			}
			else if(target == IMAGE && expr == FLOAT){
				return new Pair(true, COLORFLOAT);
			}
			else if(target == IMAGE && expr == COLOR){
				return new Pair(true, COLOR);
			}
			else if(target == IMAGE && expr == COLORFLOAT){
				return new Pair(true, COLORFLOAT);
			}
		}
		else if(target == IMAGE && isPixelSelector) {
			if(expr == COLOR || expr == COLORFLOAT || expr == FLOAT || expr == INT) {
				return new Pair(true, COLOR);
			}
		}
		return new Pair(false, null);
	}

	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		Type sourceType = (Type) writeStatement.getSource().visit(this, arg);
		Type destType = (Type) writeStatement.getDest().visit(this, arg);
		check(destType == Type.STRING || destType == Type.CONSOLE, writeStatement,
				"illegal destination type for write");
		check(sourceType != Type.CONSOLE, writeStatement, "illegal source type for write");
		return null;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		//TODO:  implement this method
		String lhsVar = readStatement.getName();
		Declaration target = symbolTable.lookup(lhsVar);
		check(readStatement.getSelector() == null, readStatement, "cannot have a pixel selector");
		Type rhsType = (Type) readStatement.getSource().visit(this, arg);
		check(rhsType == CONSOLE || rhsType == STRING, readStatement, "must have a console or string as a source");
		symbolTable.lookup(lhsVar).setInitialized(true);

		if(rhsType == CONSOLE) {
			readStatement.getSource().setCoerceTo(target.getType());
		}
		readStatement.setTargetDec(target);
		return null;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		//TODO:  implement this method
		String name = declaration.getName();
		boolean inserted = symbolTable.insert(name,declaration);
		check(inserted, declaration, "variable " + name + " already declared");

		if(declaration.getExpr() != null) {
			Type initializerType = (Type) declaration.getExpr().visit(this, arg);
			if (declaration.getType() == IMAGE) {
				check(initializerType == IMAGE || declaration.getDim() != null || initializerType == STRING, declaration, "type of expression and declared type do not match");
				declaration.getNameDef().setInitialized(true);
			}
			if(declaration.getOp().getKind() == Kind.ASSIGN) {
				Pair<Boolean, Type> checkComp = assignStatementCompatible(declaration.getNameDef().getType(), declaration.getExpr().getType(), false);
				if(checkComp.t0 == true) {
					declaration.getExpr().setCoerceTo(checkComp.t1);
				}
				else {
					throw new TypeCheckException("not assignment compatible", declaration.getSourceLoc());
				}
			}
			else if(declaration.getOp().getKind() == Kind.LARROW) {
				Type targetType = declaration.getNameDef().getType();
				Type rhsType = declaration.getExpr().getType();
				check(rhsType == CONSOLE || rhsType == STRING, declaration, "must have a console or string as a source");
				if(rhsType == CONSOLE) {
					declaration.getExpr().setCoerceTo(targetType);
				}
			}
			declaration.setInitialized(true);
		}
		if(declaration.getDim() != null) {
			Type dimType = (Type) declaration.getDim().visit(this, arg);
			check(dimType == INT, declaration.getDim(), "expected int for type of dimension");
			//declaration.setInitialized(true);
			//return null;
		}
		else if(declaration.getType() == IMAGE && declaration.getNameDef().isInitialized() == false) {
			throw new TypeCheckException("image not initialized", declaration.getSourceLoc());
		}
		/*else {
			throw new TypeCheckException("variable not initialized", declaration.getSourceLoc());
		}*/
		return null;

		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {		
		//TODO:  this method is incomplete, finish it.  
		
		//Save root of AST so return type can be accessed in return statements
		root = program;
		String name = program.getName();
		symbolTable.programName = name;
		List<NameDef> params = program.getParams();

		for (NameDef param : params) {
			param.visit(this, arg);
		}
		
		//Check declarations and statements
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}
		return program;
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		//TODO:  implement this method
		String name = nameDef.getName();
		boolean inserted = symbolTable.insert(name,nameDef);
		check(inserted, nameDef, "variable " + name + "already declared");
		nameDef.setInitialized(true);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		//TODO:  implement this method
		Dimension dim = nameDefWithDim.getDim();
		Type dimType = (Type) dim.visit(this, arg);
		check(dimType == INT, nameDefWithDim, "dimension type has to be int");

		String name = nameDefWithDim.getName();
		boolean inserted = symbolTable.insert(name,nameDefWithDim);
		check(inserted, nameDefWithDim, "variable " + name + "already declared");
		nameDefWithDim.setInitialized(true);
		return null;
		//throw new UnsupportedOperationException();
	}
 
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		Type returnType = root.getReturnType();  //This is why we save program in visitProgram.
		Type expressionType = (Type) returnStatement.getExpr().visit(this, arg);
		check(returnType == expressionType, returnStatement, "return statement with invalid type");
		return null;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		Type expType = (Type) unaryExprPostfix.getExpr().visit(this, arg);
		check(expType == Type.IMAGE, unaryExprPostfix, "pixel selector can only be applied to image");
		unaryExprPostfix.getSelector().visit(this, arg);
		unaryExprPostfix.setType(Type.INT);
		unaryExprPostfix.setCoerceTo(COLOR);
		return Type.COLOR;
	}

}
