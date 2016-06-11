package com.gmail.irclark2000.objc.java.translate;

import java.util.ArrayList;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import com.gmail.irclark2000.objc.java.JavaBaseListener;
import com.gmail.irclark2000.objc.java.JavaParser;
import com.gmail.irclark2000.objc.java.JavaParser.TypeContext;


/**
 * Parse Java File and Genrate Output
 * 
 * @author irclark
 *
 */
public class ParserJavaListener extends JavaBaseListener {
	ArrayList<JavaClassDescription> declarationList = new ArrayList<>();
	int currentDeclaration = -1;
	ParseTreeProperty<String> code = new ParseTreeProperty<String>();
	ParseTreeProperty<ArrayList<String>> list = new ParseTreeProperty<ArrayList<String>>();

	String getCode(ParseTree ctx) {
		return code.get(ctx);
	}

	void setCode(ParseTree ctx, String s) {
		code.put(ctx, s);
	}

	String getCodeNotNull(ParseTree ctx) {
		if (ctx != null) {
			return code.get(ctx);
		} else {
			return "";
		}
	}

	void setList(ParseTree ctx, ArrayList<String> s) {
		list.put(ctx, s);
	}

	ArrayList<String> getList(ParseTree ctx) {
		return list.get(ctx);
	}

	/**
	 * Make a parsejavalistener
	 * 
	 */
	public ParserJavaListener() {

	}

	@Override
	public void exitTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {

		if (ctx.classDeclaration() != null) {

		} else {

		}

	}

	@Override
	public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
		String code = ctx.Identifier().getText();
		JavaClassDescription desc = new JavaClassDescription(code, "class");
		declarationList.add(desc);
		currentDeclaration++;
	}

	@Override
	public void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
		boolean needColon = true;
		String code = ctx.Identifier().getText();
		if (ctx.type() != null) {
			needColon = false;
			code += " : " + getCode(ctx.type());
		}
		if (ctx.typeList() != null) {
			for (String protocol : getList(ctx.typeList())) {
				if (needColon) {
					code += " : ";
					needColon = false;
				} else {
					code += ", ";
				}
				code += protocol;
			}
		}
		code += getCode(ctx.classBody());
		setCode(ctx, code);
		currentDeclaration--;
	}

	@Override
	public void exitTypeList(JavaParser.TypeListContext ctx) {
		ArrayList<String> types = new ArrayList<String>();
		for (TypeContext type : ctx.type()) {
			types.add(getCode(type));
		}
		setList(ctx, types);
	}

	@Override
	public void exitType(JavaParser.TypeContext ctx) {
		String code = "";
		if (ctx.classOrInterfaceType() != null) {
			code += getCode(ctx.classOrInterfaceType());
			if (ctx.children.size() > 1) {

			}

		} else if (ctx.primitiveType() != null) {

		}
		setCode(ctx, code);
	}

	// FIXME: currently just returns the first identifier only;
	@Override
	public void exitClassOrInterfaceType(JavaParser.ClassOrInterfaceTypeContext ctx) {
		String code = ctx.Identifier(0).getText();

		setCode(ctx, code);
	}

	@Override
	public void exitClassBody(JavaParser.ClassBodyContext ctx) {
		String code = indentCode() + "{\n";
		incrementIndentLevel(1);
		for (int i = 0; i < ctx.classBodyDeclaration().size(); i++) {
			code += getCode(ctx.classBodyDeclaration(i));
		}
		incrementIndentLevel(-1);
		code += indentCode() + "\n}";
		setCode(ctx, code);
	}

	@Override
	public void exitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
		String code = "";
		if (ctx.getChild(0).getText().equals(";")){
			code = "\n" + indentCode() + ";";
		} else if (ctx.block() != null) {
			code = getCode(ctx.block());
		} else {
			String sep = "";
			for (int i=0; i < ctx.modifier().size(); i++){
				code += (sep + getCode(ctx.modifier(i)));
				sep =", ";
			}
			code += " " + getCode(ctx.memberDeclaration());
		}
		setCode(ctx, code);

	}
	 @Override
		 public void exitMemberDeclaration(JavaParser.MemberDeclarationContext ctx) {
		 String code = "";
		 setCode(ctx, code);
		 }

	@Override
	public void exitBlock(JavaParser.BlockContext ctx) {
		String code = "\n" + indentCode() + "{\n";
		incrementIndentLevel(1);
		for (int i = 0; i < ctx.blockStatement().size(); i++) {
			code += getCode(ctx.blockStatement(i));
		}
		incrementIndentLevel(-1);
		code += "\n" + indentCode() + "}";
		setCode(ctx, code);

	}

	@Override
	public void exitBlockStatement(JavaParser.BlockStatementContext ctx) {
		String code = "";

		if (ctx.localVariableDeclarationStatement() != null) {
			code += getCode(ctx.localVariableDeclarationStatement());
		} else if (ctx.statement() != null) {
			code += getCode(ctx.statement());
		} else if (ctx.typeDeclaration() != null) {
			code += getCode(ctx.typeDeclaration());
		}
		setCode(ctx, code);
	}

	@Override
	public void exitStatement(JavaParser.StatementContext ctx) {
		String code = "";
		String firstWord = ctx.getChild(0).getText();
		switch (firstWord) {
		case "for":
			code = "\n" + indentCode() + "for (" + getCode(ctx.forControl()) + ") " + getCode(ctx.statement(0));
			break;
		case "if":
			code = "\n" + indentCode() + "if " + getCode(ctx.parExpression()) + getCode(ctx.statement(0));
			if (ctx.statement().size() > 1) {
				code += "\n" + indentCode() + "else " + getCode(ctx.statement(1));
			}
			break;
		case "while":
			code = "\n" + indentCode() + "while " + getCode(ctx.parExpression()) + getCode(ctx.statement(0));
			break;
		case "do":
			code = "\n" + indentCode() + "do " + getCode(ctx.statement(0)) + " while " + getCode(ctx.parExpression());
			break;
		case "switch":
			code = "\n" + indentCode() + "switch " + getCode(ctx.parExpression()) + " {\n";
			incrementIndentLevel(1);
			
			incrementIndentLevel(-1);
			code += "\n" + indentCode() + "}";
			break;

		default:
			if (ctx.block() != null) {
				code = getCode(ctx.block());
			} else if (ctx.Identifier() != null) {
			} else if (ctx.statementExpression() != null) {
				code = "\n" + indentCode() + getCode(ctx.statementExpression());
			} else {
				code = indentCode() + ctx.getText();
			}
		}
		setCode(ctx, code);
	}

	@Override
	public void exitForControl(JavaParser.ForControlContext ctx) {
		String code = "";
		if (ctx.enhancedForControl() != null) {
			code = getCode(ctx.enhancedForControl());
		} else {
			if (ctx.forInit() != null) {
				code += getCode(ctx.forInit()) + ";";
			} else {
				code += ";";
			}
			if (ctx.expression() != null) {
				code += getCode(ctx.expression()) + ";";
			} else {
				code += ";";
			}
			if (ctx.forUpdate() != null) {
				code += getCode(ctx.forUpdate()) + ";";
			}
		}
		setCode(ctx, code);
	}

	@Override
	public void exitForInit(JavaParser.ForInitContext ctx) {
		setCode(ctx, "*");
	}

	@Override
	public void exitEnhancedForControl(JavaParser.EnhancedForControlContext ctx) {
		String code = getCode(ctx.type());
		code += getCode(ctx.variableDeclaratorId());
		code += " in ";
		code += getCode(ctx.expression());
		setCode(ctx, code);
	}

	@Override
	public void exitForUpdate(JavaParser.ForUpdateContext ctx) {
		setCode(ctx, "*");
	}

	@Override
	public void exitVariableDeclaratorId(JavaParser.VariableDeclaratorIdContext ctx) {
		String code = ctx.Identifier().getText();
		for (int i = 1; i < ctx.getChildCount(); i++) {
			code += "[]";
		}
		setCode(ctx, code);
	}

	@Override
	public void exitExpression(JavaParser.ExpressionContext ctx) {
		String working = ctx.getText();
		String code = "";
		if (ctx.primary() != null) {
			code = getCode(ctx.primary());
		} else if (ctx.children.get(0).getText().equals("new")) {
			code = ("create an object") + ":";
			if (ctx.nonWildcardTypeArguments() != null) {
				code += getCode(ctx.nonWildcardTypeArguments());
			} else {
				code += ":";
			}
			code += getCode(ctx.innerCreator());
		} else if (ctx.children.get(0).getText().equals("(")) {
			// cast expression
			code = "(" + getCode(ctx.getChild(1)) + ")" + getCode(ctx.getChild(3));
		} else if (ctx.children.get(1).getText().equals("(")) {
			// FIXME: function call
			code = getCode(ctx.getChild(0)) + "(" + getCodeNotNull(ctx.expressionList()) + ")";
		} else if (ctx.getChild(1).getText().equals("[")) {
			code = getCode(ctx.getChild(0)) + "[" + getCode(ctx.getChild(2)) + "]";
		} else if (ctx.getChild(1).getText().equals(".")) {
			// dot expressions
			// FIXME: abbreviated for now
			if (ctx.Identifier() != null) {
				code = getCode(ctx.getChild(0)) + "." + ctx.Identifier().getText();
			} else {
				code = "";
			}
		} else if (ctx.getChild(1).getText().equals("++") || ctx.getChild(1).getText().equals("++")) {
			// post increment, decrement
			code = getCode(ctx.getChild(0)) + ctx.getChild(1).getText();
		} else if (ctx.getChildCount() == 2) {
			// at this point only prefix instructions are left with two
			// arguments
			code = ctx.getChild(0).getText() + getCode(ctx.getChild(1));
		} else if (ctx.getChildCount() == 3) {
			// should be all binary expressions
			code = getCode(ctx.getChild(0)) + ctx.getChild(1).getText() + getCode(ctx.getChild(2));
		}
		setCode(ctx, code);
	}

	public void exitStatementExpression(JavaParser.StatementExpressionContext ctx) {
		setCode(ctx, getCode(ctx.expression()));
	}

	@Override
	public void exitParExpression(JavaParser.ParExpressionContext ctx) {
		String code = "(" + getCode(ctx.expression()) + ")";
		setCode(ctx, code);
	}

	@Override
	public void exitPrimary(JavaParser.PrimaryContext ctx) {
		// FIXME: deals with limited cases
		String code = "";
		if (ctx.getChild(0).getText().equals("(")) {
			code = "(" + getCode(ctx.getChild(1)) + ")";
		} else if (ctx.literal() != null) {
			// FIXME look for null to be replaced by nil
			code = ctx.literal().getText();
		} else if (ctx.Identifier() != null) {
			code = ctx.Identifier().getText();
		}
		setCode(ctx, code);
	}

	@Override
	public void exitNonWildcardTypeArguments(JavaParser.NonWildcardTypeArgumentsContext ctx) {
		String code = "";
		String sep = "";
		for (String type : getList(ctx.typeList())) {
			code += sep += type;
			sep = ",";
		}
		setCode(ctx, code);
	}

	@Override
	public void exitInnerCreator(JavaParser.InnerCreatorContext ctx) {
		String code = ctx.Identifier().getText();
		if (ctx.nonWildcardTypeArgumentsOrDiamond() != null) {
			code += getCode(ctx.nonWildcardTypeArgumentsOrDiamond());
		}
		code += getCode(ctx.classCreatorRest());
		setCode(ctx, code);
	}

	@Override
	public void exitNonWildcardTypeArgumentsOrDiamond(JavaParser.NonWildcardTypeArgumentsOrDiamondContext ctx) {
		String code = getCodeNotNull(ctx.nonWildcardTypeArguments());
		if (ctx.children.get(0).getText().equals("<")) {
			code += "<>";
		}
		setCode(ctx, code);
	}

	@Override
	public void exitClassCreatorRest(JavaParser.ClassCreatorRestContext ctx) {
		String code = getCode(ctx.arguments());
		code += getCodeNotNull(ctx.classBody());
		setCode(ctx, code);
	}

	@Override
	public void exitArguments(JavaParser.ArgumentsContext ctx) {
		String code = "(";
		code += getCodeNotNull(ctx.expressionList());
		code += ")";
		setCode(ctx, code);
	}

	@Override
	public void exitExpressionList(JavaParser.ExpressionListContext ctx) {
		String eList = "";
		String sep = "";
		for (int i = 0; i < ctx.expression().size(); i++) {
			eList += sep + (getCode(ctx.expression(i)));
			sep = ", ";
		}
		setCode(ctx, eList);
	}

	// @Override
	// public void exit**(JavaParser.**Context ctx) {
	// String code = "";
	// setCode(ctx, code);
	// }

	private void incrementIndentLevel(int increment) {
		declarationList.get(currentDeclaration)
				.setIndentLevel(declarationList.get(currentDeclaration).getIndentLevel() + increment);
	}

	private String indentCode() {
		String tabs = "";
		int count = declarationList.get(currentDeclaration).getIndentLevel();
		for (int i = 0; i < count; i++) {
			tabs += "\t";
		}
		return tabs;
	}

}
