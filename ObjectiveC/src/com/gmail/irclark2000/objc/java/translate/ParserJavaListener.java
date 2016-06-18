package com.gmail.irclark2000.objc.java.translate;

import java.util.ArrayList;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import com.gmail.irclark2000.objc.CodeFormatter;
import com.gmail.irclark2000.objc.ParseOptions;
import com.gmail.irclark2000.objc.java.JavaBaseListener;
import com.gmail.irclark2000.objc.java.JavaParser;
import com.gmail.irclark2000.objc.java.JavaParser.BlockStatementContext;
import com.gmail.irclark2000.objc.java.JavaParser.ClassOrInterfaceModifierContext;
import com.gmail.irclark2000.objc.java.JavaParser.FormalParameterContext;
import com.gmail.irclark2000.objc.java.JavaParser.SwitchBlockStatementGroupContext;
import com.gmail.irclark2000.objc.java.JavaParser.SwitchLabelContext;
import com.gmail.irclark2000.objc.java.JavaParser.TypeArgumentContext;
import com.gmail.irclark2000.objc.java.JavaParser.TypeContext;
import com.gmail.irclark2000.objc.java.JavaParser.VariableDeclaratorContext;
import com.gmail.irclark2000.objc.java.JavaParser.VariableModifierContext;

/**
 * Parse Java File and Generate Output
 * 
 * @author irclark
 *
 */
public class ParserJavaListener extends JavaBaseListener {
	ArrayList<JavaClassDescription> declarationList = new ArrayList<>();
	MethodSignature activeMethod = null;
	int currentDeclaration = -1;
	ParseTreeProperty<String> code = new ParseTreeProperty<String>();
	ParseTreeProperty<ArrayList<String>> list = new ParseTreeProperty<ArrayList<String>>();
	private CodeFormatter codeFormat = new CodeFormatter();
	private ParseOptions options;
	private ArrayList<ArrayList<JavaClassDescription>> myDescriptions;

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
	 * Make a ParseJavaListener to translate code
	 * 
	 * @param options
	 *            user selected options for coding
	 * @param myDescriptions list of descriptions from parsed files
	 * 
	 */
	public ParserJavaListener(ParseOptions options, ArrayList<ArrayList<JavaClassDescription>> myDescriptions) {
		this.options = options;
		this.myDescriptions = myDescriptions;

	}

	@Override
	public void exitTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {
		String code;
		String modifier = "";
		String sep = "";
		for (ClassOrInterfaceModifierContext cim : ctx.classOrInterfaceModifier()) {
			modifier += sep + getCode(cim);
			sep = " ";
		}
		code = getCodeNotNull(ctx.classDeclaration());
		code += getCodeNotNull(ctx.enumDeclaration());
		code += getCodeNotNull(ctx.interfaceDeclaration());
		code += getCodeNotNull(ctx.annotationTypeDeclaration());
		setCode(ctx, code);
		myDescriptions.add(declarationList);
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
		code += getCodeNotNull(ctx.classOrInterfaceType());
		code += getCodeNotNull(ctx.primitiveType());
		for (int i = 1; i < ctx.children.size(); i++) {
			code += "[]";
		}
		setCode(ctx, code);
	}

	@Override
	public void exitPrimitiveType(JavaParser.PrimitiveTypeContext ctx) {
		String code = ctx.getText();
		setCode(ctx, code);
	}

	// FIXME: currently just returns the first identifier only;
	@Override
	public void exitClassOrInterfaceType(JavaParser.ClassOrInterfaceTypeContext ctx) {
		String myCode = ctx.getText();
		String.format(myCode);

		String code = "";
		String sep = "";
		for(int i=0; i < ctx.Identifier().size(); i++) {
			code += sep + ctx.Identifier(i).getText();
			code += getCodeNotNull(ctx.typeArguments(i));
			sep = ".";
		}

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
		String myCode = ctx.getText();
		String.format(myCode);
		String code = "";
		if (ctx.getChild(0).getText().equals(";")) {
			code = "\n" + indentCode() + ";";
		} else if (ctx.block() != null) {
			code = getCode(ctx.block());
		} else {
			String sep = "";
			for (int i = 0; i < ctx.modifier().size(); i++) {
				code += (sep + getCode(ctx.modifier(i)));
				sep = ", ";
			}
			code += " " + getCode(ctx.memberDeclaration());
		}
		setCode(ctx, code);
	}

	@Override
	public void exitModifier(JavaParser.ModifierContext ctx) {
		String code = "";
		if (ctx.classOrInterfaceModifier() != null) {
			code = getCode(ctx.classOrInterfaceModifier());
		} else {
			code = ctx.getText();
		}
		setCode(ctx, code);
	}

	@Override
	public void exitClassOrInterfaceModifier(JavaParser.ClassOrInterfaceModifierContext ctx) {
		String code = "";
		if (ctx.annotation() != null) {
			code = getCode(ctx.annotation());
		} else {
			code = ctx.getText();
		}
		setCode(ctx, code);
	}

	@Override
	public void exitAnnotation(JavaParser.AnnotationContext ctx) {
		setCode(ctx, ctx.getText());
	}

	@Override
	public void exitMemberDeclaration(JavaParser.MemberDeclarationContext ctx) {
		String code = ctx.getText();
		if (ctx.methodDeclaration() != null) {
			code = getCode(ctx.methodDeclaration());
		} else if (ctx.genericMethodDeclaration() != null) {
			code = getCode(ctx.genericMethodDeclaration());
		} else if (ctx.fieldDeclaration() != null) {
			code = getCode(ctx.fieldDeclaration());
		} else if (ctx.constructorDeclaration() != null) {
			code = getCode(ctx.constructorDeclaration());
		} else if (ctx.genericConstructorDeclaration() != null) {
			code = getCode(ctx.genericConstructorDeclaration());
		} else if (ctx.interfaceDeclaration() != null) {
			code = getCode(ctx.interfaceDeclaration());
		} else if (ctx.annotationTypeDeclaration() != null) {
			code = getCode(ctx.annotationTypeDeclaration());
		} else if (ctx.classDeclaration() != null) {
			code = getCode(ctx.annotationTypeDeclaration());
		} else if (ctx.enumDeclaration() != null) {
			code = getCode(ctx.annotationTypeDeclaration());
		}
		setCode(ctx, code);
	}

	@Override
	public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
		String name;
		String type;
		if (ctx.type() != null) {
			type = getCode(ctx.type());
		} else {
			type = ctx.getChild(0).getText();
		}
		name = ctx.Identifier().getText();
		activeMethod = new MethodSignature(name, type);
		declarationList.get(this.currentDeclaration).addMethod(activeMethod);
	}

	@Override
	public void exitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
		String code;
		if (ctx.type() != null) {
			code = "\n" + indentCode() + getCode(ctx.type());
		} else {
			code = "\n" + indentCode() + ctx.getChild(0).getText();
		}
		code += " " + ctx.Identifier().getText();
		code += getCode(ctx.formalParameters()) + " ";
		String sParams = getCode(ctx.formalParameters());
		ArrayList<String> fParams = this.splitParamatersToList(sParams.substring(1, sParams.length() - 1), ", ");
		for (String p : fParams) {
			activeMethod.addParameterType(p);
		}
		if (ctx.getChild(3).getText().charAt(0) == '[') {
			code += ctx.getChild(3).getText() + " ";
		}
		// FIXME: not using throws so far
		if (ctx.methodBody() != null) {
			code += getCode(ctx.methodBody());
		} else {
			code += "\n" + indentCode() + ";";
		}
		activeMethod.setBody(code);
		setCode(ctx, code);
		activeMethod = null;
	}

	@Override
	public void exitGenericMethodDeclaration(JavaParser.GenericMethodDeclarationContext ctx) {
		String code = "";
		code += "\n" + indentCode() + " " + getCode(ctx.typeParameters()) + " " + getCode(ctx.methodDeclaration());
		setCode(ctx, code);
	}

	@Override
	public void enterConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
		String type = ctx.Identifier().getText();
		activeMethod = new MethodSignature(type, type);
		declarationList.get(this.currentDeclaration).addMethod(activeMethod);
	}

	@Override
	public void exitConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
		// FIXME: no throws stuff yet
		String myCode = ctx.getText();
		String.format(myCode);

		String code = "";
		code += " " + ctx.Identifier().getText();
		code += getCode(ctx.formalParameters());
		String sParams = getCode(ctx.formalParameters());
		ArrayList<String> fParams = this.splitParamatersToList(sParams.substring(1, sParams.length() - 1), ", ");
		for (String p : fParams) {
			activeMethod.addParameterType(p);
		}
	
		code += getCode(ctx.constructorBody());
		// FIXME: not using throws so far
		activeMethod.setBody(code);
		activeMethod= null;
		setCode(ctx, code);
	}

	@Override
	public void exitGenericConstructorDeclaration(JavaParser.GenericConstructorDeclarationContext ctx) {
		// FIXME: no throws stuff yet
		String code = "";
		code += "\n" + indentCode() + " " + getCode(ctx.typeParameters()) + " " + getCode(ctx.constructorDeclaration());
		setCode(ctx, code);
	}

	@Override
	public void exitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
		// FIXME: needs lots of work
		setCode(ctx, ctx.getText());
	}

	@Override
	public void exitAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) {
		// FIXME: needs lots of work
		setCode(ctx, ctx.getText());
	}

	@Override
	public void exitTypeParameters(JavaParser.TypeParametersContext ctx) {
		setCode(ctx, ctx.getText());
	}

	@Override
	public void exitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
		setCode(ctx, ctx.getText());
	}

	@Override
	public void exitMethodBody(JavaParser.MethodBodyContext ctx) {
		String code = getCode(ctx.block());
		setCode(ctx, code);
	}

	@Override
	public void exitConstructorBody(JavaParser.ConstructorBodyContext ctx) {
		setCode(ctx, getCode(ctx.block()));
	}

	@Override
	public void exitFormalParameters(JavaParser.FormalParametersContext ctx) {
		String code = "(" + getCodeNotNull(ctx.formalParameterList()) + ")";
		setCode(ctx, code);
	}

	@Override
	public void exitFormalParameterList(JavaParser.FormalParameterListContext ctx) {
		String myCode = ctx.getText();
		String.format(myCode);
		String code = "";
		if (ctx.formalParameter() != null) {
			String sep = "";
			for (FormalParameterContext fP : ctx.formalParameter()) {
				code += sep + getCode(fP);
				sep = ", ";
			}
			if (ctx.lastFormalParameter() != null) {
				code += ", " + getCode(ctx.lastFormalParameter());
			}
		} else {
			code = getCode(ctx.lastFormalParameter());
		}
		setCode(ctx, code);
	}

	public void exitFormalParameter(JavaParser.FormalParameterContext ctx) {
		String code = "";
		String sep = "";
		for (VariableModifierContext vm : ctx.variableModifier()) {
			code += sep + getCode(vm);
			sep = " ";
		}
		code += " " + getCode(ctx.type()) + " " + getCode(ctx.variableDeclaratorId());
		setCode(ctx, code);
	}

	public void exitLastFormalParameter(JavaParser.LastFormalParameterContext ctx) {
		String code = "";
		String sep = "";
		for (VariableModifierContext vm : ctx.variableModifier()) {
			code += sep + getCode(vm);
			sep = " ";
		}
		code += " " + getCode(ctx.type()) + " ... " + getCode(ctx.variableDeclaratorId());
		setCode(ctx, code);
	}

	public void exitVariableModifier(JavaParser.VariableModifierContext ctx) {
		String code = ctx.getText();
		setCode(ctx, code);
	}

	@Override
	public void exitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
		String code = "";
		code += "\n" + indentCode() + getCode(ctx.type()) + " " + getCode(ctx.variableDeclarators()) + ";";
		setCode(ctx, code);
	}

	@Override
	public void exitBlock(JavaParser.BlockContext ctx) {
		String code = "\n" + indentCode() + "{";
		incrementIndentLevel(1);
		for (int i = 0; i < ctx.blockStatement().size(); i++) {
			String mCode = getCode(ctx.blockStatement(i));
			code += "\n" + indentCode() + mCode;
		}
		incrementIndentLevel(-1);
		code += "\n" + indentCode() + "}";
		setCode(ctx, code);

	}

	@Override
	public void exitBlockStatement(JavaParser.BlockStatementContext ctx) {
		String myCode = ctx.getText();
		String.format(myCode);
		String code = "";

		if (ctx.localVariableDeclarationStatement() != null) {
			code += getCode(ctx.localVariableDeclarationStatement());
		} else if (ctx.statement() != null) {
			code += getCode(ctx.statement());
		} else if (ctx.typeDeclaration() != null) {
			code += getCode(ctx.typeDeclaration());
		} else {
			code = ctx.getText();
		}
		setCode(ctx, code);
	}

	@Override
	public void exitStatement(JavaParser.StatementContext ctx) {
		String myCode = ctx.getText();
		String.format(myCode);

		// FIXME: missing throw, try ASSERT synchronized for now
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
			for (SwitchBlockStatementGroupContext sbsg : ctx.switchBlockStatementGroup()) {
				code = "\n" + indentCode() + getCode(sbsg);
			}
			for (SwitchLabelContext sl : ctx.switchLabel()) {
				code = "\n" + indentCode() + getCode(sl);
			}
			incrementIndentLevel(-1);
			code += "\n" + indentCode() + "}";
			break;
		case "return":
			code = "\n" + indentCode() + "return " + getCodeNotNull(ctx.expression(0)) + ";";
			break;
		case "break":			
			code = "\n" + indentCode() + "break";
			if (ctx.Identifier() != null) {
				code += " " + ctx.Identifier().getText();
			}
			code += ";";
			break;
		case "continue":
			if (ctx.Identifier() != null) {
				code += " " + ctx.Identifier().getText();
			}
			code += ";";
			code = "\n" + indentCode() + "continue";
			break;
		case ";":
			code = "\n" + indentCode() + ";";
			break;
		default:
			if (ctx.block() != null) {
				code = getCode(ctx.block());
			} else if (ctx.Identifier() != null) {
				code = "\n" + indentCode() + ctx.Identifier().getText() + " : " + getCode(ctx.statement(0));
			} else if (ctx.statementExpression() != null) {
				code = "\n" + indentCode() + getCode(ctx.statementExpression());
			} else {
				code = indentCode() + ctx.getText();
			}
		}
		setCode(ctx, code);
	}

	// switch section

	@Override
	public void exitSwitchBlockStatementGroup(JavaParser.SwitchBlockStatementGroupContext ctx) {
		String code = "";
		for (SwitchLabelContext sctx : ctx.switchLabel()) {
			code += "\n" + indentCode() + getCode(sctx);
		}
		for (BlockStatementContext bctx : ctx.blockStatement()) {
			code += getCode(bctx);
		}
		setCode(ctx, code);
	}

	@Override
	public void exitSwitchLabel(JavaParser.SwitchLabelContext ctx) {
		String code;
		if (ctx.constantExpression() != null || ctx.enumConstantName() != null) {
			code = "\n" + indentCode() + "case" + getCode(ctx.getChild(1)) + " : ";
		} else {
			code = "\n" + indentCode() + "default" + " : ";
		}
		setCode(ctx, code);
	}

	// for statement stuff
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
		String code = getCodeNotNull(ctx.expressionList());
		code += getCodeNotNull(ctx.localVariableDeclaration());
		setCode(ctx, code);
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
	public void exitLocalVariableDeclarationStatement(JavaParser.LocalVariableDeclarationStatementContext ctx) {
		String code = getCode(ctx.localVariableDeclaration()) + ";";
		setCode(ctx, code);
	}

	@Override
	public void exitLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
		String code = "";
		String sep = "";
		for (VariableModifierContext mod : ctx.variableModifier()) {
			code += sep + getCode(mod);
			sep = " ";
		}
		code += getCode(ctx.type()) + " " + getCode(ctx.variableDeclarators());
		setCode(ctx, code);
	}

	@Override
	public void exitVariableDeclarators(JavaParser.VariableDeclaratorsContext ctx) {
		String code = "";
		String sep = "";
		for (VariableDeclaratorContext vd : ctx.variableDeclarator()) {
			code += sep + getCode(vd);
			sep = ", ";
		}
		setCode(ctx, code);
	}

	@Override
	public void exitVariableDeclarator(JavaParser.VariableDeclaratorContext ctx) {
		String code = "";
		code = getCode(ctx.variableDeclaratorId());
		if (ctx.variableInitializer() != null) {
			code += " " + getCode(ctx.variableInitializer());
		}
		setCode(ctx, code);
	}

	@Override
	public void exitVariableInitializer(JavaParser.VariableInitializerContext ctx) {
		String code = getCodeNotNull(ctx.expression());
		code = getCodeNotNull(ctx.arrayInitializer());
		setCode(ctx, code);
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
	public void exitArrayInitializer(JavaParser.ArrayInitializerContext ctx) {
		setCode(ctx, ctx.getText());
	}

	@Override
	public void exitExpression(JavaParser.ExpressionContext ctx) {
		String working = ctx.getText();
		String code = "";
		if (ctx.primary() != null) {
			code = getCode(ctx.primary());
		} else if (ctx.children.get(0).getText().equals("new")) {
			code = ("create an object : new ");

			code += getCode(ctx.creator());
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
			code = getCode(ctx.getChild(0)) + " " + ctx.getChild(1).getText() + getCode(ctx.getChild(2));
		} else {
			code = working; // default simply outputs line as received
		}
		setCode(ctx, code);
	}

	public void exitCreator(JavaParser.CreatorContext ctx) {
		String working = ctx.getText();
		String.format(working);

		String code = "";
		if (ctx.nonWildcardTypeArguments() != null) {
			code += getCode(ctx.nonWildcardTypeArguments());
			code += " " + getCode(ctx.createdName());
			code += " " + getCode(ctx.classCreatorRest());
		} else {
			code += " " + getCode(ctx.createdName());
			if (ctx.arrayCreatorRest() != null) {
				code += " " + getCode(ctx.arrayCreatorRest());
			} else {
				code += " " + getCode(ctx.classCreatorRest());
			}
		}
		setCode(ctx, code);
	}

	public void exitCreatedName(JavaParser.CreatedNameContext ctx) {
		String working = ctx.getText();
		String.format(working);
		String code = "";
		String sep = "";

		code = getCodeNotNull(ctx.primitiveType());

		for (int i = 0; i < ctx.Identifier().size(); i++) {
			code += sep + ctx.Identifier(i).getText();
			if (ctx.typeArgumentsOrDiamond(i) != null) {
				code += getCode(ctx.typeArgumentsOrDiamond(i));
			}
			sep = ".";
		}
		setCode(ctx, code);
	}
	
	@Override public void exitTypeArgumentsOrDiamond(JavaParser.TypeArgumentsOrDiamondContext ctx) { 
		String working = ctx.getText();
		String.format(working);
		String code = "";
		if (ctx.typeArguments() != null) {
			code = getCode(ctx.typeArguments());
		}else {
			code += "<>";
		}
		setCode(ctx, code);
	}
	
	@Override public void exitTypeArguments(JavaParser.TypeArgumentsContext ctx) {
		String working = ctx.getText();
		String.format(working);

		String code = "<";
		String sep = "";
		for (TypeArgumentContext ta : ctx.typeArgument()){
			code += sep + getCode(ta);
			sep = ", ";
		}
		code += ">";
		setCode(ctx, code);
	}
	
	@Override public void exitTypeArgument(JavaParser.TypeArgumentContext ctx) {
		String working = ctx.getText();
		String.format(working);
		String code = "";
		if(ctx.getChild(0).getText().equals("?")) {
			code = ctx.getText();
		} else {
			code = getCode(ctx.type());
		}
		setCode(ctx, code);
		
	}
	


	public void exitStatementExpression(JavaParser.StatementExpressionContext ctx) {
		String myCode = ctx.getText();
		String.format(myCode);
		String code = getCode(ctx.expression());
		setCode(ctx, code);
	}

	public void exitConstantExpression(JavaParser.ConstantExpressionContext ctx) {
		setCode(ctx, getCode(ctx.expression()));
	}

	public void exitEnumConstantName(JavaParser.EnumConstantNameContext ctx) {
		setCode(ctx, ctx.Identifier().getText());
	}

	@Override
	public void exitParExpression(JavaParser.ParExpressionContext ctx) {
		String code = "(" + getCode(ctx.expression()) + ")";
		setCode(ctx, code);
	}

	@Override
	public void exitPrimary(JavaParser.PrimaryContext ctx) {
		// FIXME: deals with limited cases
		String myCode = ctx.getText();
		String.format(myCode);

		String code = "";
		if (ctx.getChild(0).getText().equals("(")) {
			code = "(" + getCode(ctx.getChild(1)) + ")";
		} else if (ctx.literal() != null) {
			code = ctx.literal().getText();
		} else if (ctx.identifier() != null) {
			// FIXME look for null to be replaced by nil
			code = getCode(ctx.identifier());
		} else if (ctx.nonWildcardTypeArguments() != null) {
			code = getCode(ctx.nonWildcardTypeArguments());
		} else if (ctx.arguments() != null) {
			code = ctx.getText();
		} else {
			code = ctx.getText();
		}
		setCode(ctx, code);
	}

	@Override
	public void exitIdentifier(JavaParser.IdentifierContext ctx) {
		// FIXME: translate identifier
		String code = ctx.getText();
		setCode(ctx, code);
	}

	@Override
	public void exitNonWildcardTypeArguments(JavaParser.NonWildcardTypeArgumentsContext ctx) {
		String code = "";
		String sep = "";
		code += "<";
		for (String type : getList(ctx.typeList())) {
			code += sep += type;
			sep = ", ";
		}
		code += ">";
		setCode(ctx, code);
	}

	@Override
	public void exitInnerCreator(JavaParser.InnerCreatorContext ctx) {
		String code = "";
		code += ctx.Identifier().getText();
		if (ctx.nonWildcardTypeArgumentsOrDiamond() != null) {
			code += getCode(ctx.nonWildcardTypeArgumentsOrDiamond());
		}
		code += getCode(ctx.classCreatorRest());
		setCode(ctx, code);
	}

	@Override
	public void exitNonWildcardTypeArgumentsOrDiamond(JavaParser.NonWildcardTypeArgumentsOrDiamondContext ctx) {
		String code = "";
		if (ctx.children.get(0).getText().equals("<")) {
			code += "<";

			code += ">";
		} else {
			code = getCode(ctx.nonWildcardTypeArguments());
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
	public void exitArrayCreatorRest(JavaParser.ArrayCreatorRestContext ctx) {
		String code = "[";
		if (ctx.arrayInitializer() != null) {
			code += "[";
			for (int i=3; i < ctx.getChildCount(); i++) {
				code += "[]";
			}
			code += getCode(ctx.arrayInitializer());
		} else {
			code += getCode(ctx.expression(0)) + "[";
			for (int i=1; i < ctx.expression().size(); i++) {
				code += "[" + getCode(ctx.expression(i)) + "]";
			}
		}
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
		int nLevel = declarationList.get(currentDeclaration).getIndentLevel() + increment;
		declarationList.get(currentDeclaration).setIndentLevel(nLevel);
	}

	private String indentCode() {
		String tabs = "";
		int count = declarationList.get(currentDeclaration).getIndentLevel();
		for (int i = 0; i < count; i++) {
			tabs += "\t";
		}
		return tabs;
	}

	ArrayList<String> splitParamatersToList(String parameters, String sep) {
		ArrayList<String> list = new ArrayList<String>();
		if (parameters.length() > 1) {
			String[] params = parameters.split(sep);
			for (String param : params) {
				list.add(param.trim());
			}
		}
		return list;
	}

}
