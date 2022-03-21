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

	Map<Pair<Type,Type>, Type> binaryPlusMinusExprs = Map.of(
			new Pair<Type,Type>(INT, INT), INT,
			new Pair<Type,Type>(FLOAT, FLOAT), FLOAT,
			new Pair<Type,Type>(INT, FLOAT), FLOAT,
			new Pair<Type,Type>(FLOAT, INT), FLOAT,
			new Pair<Type,Type>(COLOR, COLOR), COLOR,
			new Pair<Type,Type>(COLORFLOAT, COLORFLOAT), COLORFLOAT,
			new Pair<Type,Type>(COLORFLOAT, COLOR), COLORFLOAT,
			new Pair<Type,Type>(COLOR, COLORFLOAT), COLORFLOAT,
			new Pair<Type,Type>(IMAGE, IMAGE), IMAGE
	);

	Map<Pair<Type,Type>, Type> binaryTimesDivModExprs = Map.of(
			new Pair<Type,Type>(IMAGE, INT), IMAGE,
			new Pair<Type,Type>(IMAGE, FLOAT), IMAGE,
			new Pair<Type,Type>(INT, COLOR), COLOR,
			new Pair<Type,Type>(FLOAT, COLOR), COLOR,
			new Pair<Type,Type>(FLOAT, COLOR), COLORFLOAT,
			new Pair<Type,Type>(COLOR, FLOAT), COLORFLOAT
	);

	Map<Pair<Type,Type>, Type> binaryComparisonExprs = Map.of(
			new Pair<Type,Type>(INT,INT), BOOLEAN,
			new Pair<Type,Type>(FLOAT, INT), BOOLEAN,
			new Pair<Type,Type>(INT, FLOAT), BOOLEAN,
			new Pair<Type,Type>(FLOAT, INT), BOOLEAN
	);

	/*Map<Pair<Type,Type>, Type> binaryExprs = Map.of(
			new Pair<Type,Type>(BOOLEAN,BOOLEAN), BOOLEAN,
			new Pair<Type,Type>(INT, INT), INT,
			new Pair<Type,Type>(FLOAT, FLOAT), FLOAT,
			new Pair<Type,Type>(INT, FLOAT), FLOAT,
			new Pair<Type,Type>(FLOAT, INT), FLOAT,
			new Pair<Type,Type>(COLOR, COLOR), COLOR,
			new Pair<Type,Type>(COLORFLOAT, COLORFLOAT), COLORFLOAT,
			//new Pair<Type,Type>(COLORFLOAT, COLOR), COLORFLOAT,
			//new Pair<Type,Type>(COLOR, COLORFLOAT), COLORFLOAT,
			new Pair<Type,Type>(IMAGE, IMAGE), IMAGE, //fine
			new Pair<Type,Type>(IMAGE, INT), IMAGE,
			new Pair<Type,Type>(IMAGE, FLOAT), IMAGE,
			new Pair<Type,Type>(INT, COLOR), COLOR
			/*new Pair<Type,Type>(FLOAT, COLOR), COLOR,
			new Pair<Type,Type>(FLOAT, COLOR), COLORFLOAT,
			new Pair<Type,Type>(COLOR, FLOAT), COLORFLOAT,
			//new Pair<Type,Type>(INT,INT), BOOLEAN,
			//new Pair<Type,Type>(FLOAT, INT), BOOLEAN,
			//new Pair<Type,Type>(INT, FLOAT), BOOLEAN,
			//new Pair<Type,Type>(FLOAT, INT), BOOLEAN,
			);*/

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
			}
			else if(leftType == IMAGE && rightType == FLOAT) {
				resultType = IMAGE;
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
		identExpr.setType(type);
		return type;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		//TODO  implement this method
		Type conditionType = conditionalExpr.getCondition().getType();
		check(conditionType == BOOLEAN, conditionalExpr, "conditional type not a boolean");
		Type trueCase = conditionalExpr.getTrueCase().getType();
		Type falseCase = conditionalExpr.getFalseCase().getType();
		check(trueCase == falseCase, conditionalExpr, "true case and false case have different types");
		conditionalExpr.setType(trueCase);
		return trueCase;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		//TODO  implement this method
		throw new UnsupportedOperationException();
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
		Declaration target = symbolTable.lookup(assignmentStatement.getName());
		Type targetType = target.getType();
		target.setInitialized(true);
		Expr expression = assignmentStatement.getExpr();
		if(targetType != IMAGE) {
			check(assignmentStatement.getSelector() == null, assignmentStatement, "there should not be a pixel selector");
			if(targetType != expression.getType()) {
				if(targetType == INT && expression.getType() == FLOAT) {
					expression.setCoerceTo(INT);
				}
				else if(targetType == FLOAT && expression.getType() == INT) {
					expression.setCoerceTo(FLOAT);
				}
				else if(targetType == INT && expression.getType() == COLOR) {
					expression.setCoerceTo(INT);
				}
				else if(targetType == COLOR && expression.getType() == INT) {
					expression.setCoerceTo(COLOR);
				}
				else {
					throw new TypeCheckException("expression and target variable are not assignment compatible", assignmentStatement.getSourceLoc());
				}
			}
		}
		else if(targetType == IMAGE && assignmentStatement.getSelector() != null) {

		}
		throw new UnsupportedOperationException("Unimplemented visit method.");
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
		Type rhsType = readStatement.getSource().getType();
		check(rhsType == CONSOLE || rhsType == STRING, readStatement, "must have a console or string as a source");
		symbolTable.lookup(lhsVar).setInitialized(true);
		readStatement.setTargetDec(target);
		return target;
		//throw new UnsupportedOperationException("Unimplemented visit method.");
	}

	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		//TODO:  implement this method
		throw new UnsupportedOperationException("Unimplemented visit method.");
	}


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {		
		//TODO:  this method is incomplete, finish it.  
		
		//Save root of AST so return type can be accessed in return statements
		root = program;
		
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
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		//TODO:  implement this method
		throw new UnsupportedOperationException();
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
