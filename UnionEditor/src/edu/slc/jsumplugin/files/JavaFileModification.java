package edu.slc.jsumplugin.files;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import ast.Ast.Traversal;
import ast.Ast.Variant;
import ast.CompareAst.CompareUnions;
import ast.Type.*;

public class JavaFileModification {
	
	/*
	 * main function & method------------------------------------------------------------------------------------- 
	 */	

	public static void modifyInstance(ICompilationUnit iUnit, Traversal t, CompareUnions compareUnions) throws 
	JavaModelException, IllegalArgumentException, MalformedTreeException, org.eclipse.jface.text.BadLocationException {
		// insert
		for (Variant v : compareUnions.getTraversalInstaces(t, 0)) {
			CompilationUnit astRoot = parse(iUnit);
			// get text edits
			Pair<ast.Type, String> traversal_union_type = compareUnions.getUnionTypeInTraversal(t);
			TextEdit edits = insertInstance(astRoot, t, v, traversal_union_type);
			// apply the text edits to the compilation unit
			Document document = new Document(iUnit.getSource());
			edits.apply(document);
			// adding statement to the buffer iUnit will be created in the addInstance in the JavaSystem
			iUnit.getBuffer().setContents(document.get());
		}
		// delete
		for (Variant v : compareUnions.getTraversalInstaces(t, 1)) {
			CompilationUnit astRoot = parse(iUnit);
			// get text edits
			Pair<ast.Type, String> traversal_union_type = compareUnions.getUnionTypeInTraversal(t);
			TextEdit edits = removeInstance(astRoot, t, v, traversal_union_type);
			// apply the text edits to the compilation unit
			Document document = new Document(iUnit.getSource());
			edits.apply(document);
			// adding statement to the buffer iUnit will be created in the addInstance in the JavaSystem
			iUnit.getBuffer().setContents(document.get());
		}

	}
	
	public static void modifyVisitorInterpreter(String union_name, ICompilationUnit iUnit, CompareUnions compareUnions) throws JavaModelException, MalformedTreeException, BadLocationException {
		// insert
		for (Variant v : compareUnions.compareVariants_Unions.get(union_name).getInsertions()) {
			CompilationUnit astRoot = parse(iUnit);
			// get text edits
			TextEdit edits = insertVisitInInterpreter(astRoot, v);
			// apply the text edits to the compilation unit
			Document document = new Document(iUnit.getSource());
			edits.apply(document);
			// adding statement to the buffer iUnit will be created in the addInstance in the JavaSystem
			iUnit.getBuffer().setContents(document.get());
		}
		
		// delete
		for (Variant v : compareUnions.compareVariants_Unions.get(union_name).getDeletions()) {
			CompilationUnit astRoot = parse(iUnit);
			// get text edits
			TextEdit edits = removeVisitMethod(astRoot, v);
			// apply the text edits to the compilation unit
			Document document = new Document(iUnit.getSource());
			edits.apply(document);
			// adding statement to the buffer iUnit will be created in the addInstance in the JavaSystem
			iUnit.getBuffer().setContents(document.get());
		}
	}


	public static void modifyVisitorInterface(String union_name, ICompilationUnit iUnit, CompareUnions compareUnions) throws JavaModelException, MalformedTreeException, BadLocationException {		
		// insert
		for (Variant v : compareUnions.compareVariants_Unions.get(union_name).getInsertions()) {
			CompilationUnit astRoot = parse(iUnit);
			// get text edits
			TextEdit edits = insertVisitInInterface(astRoot, v);
			// apply the text edits to the compilation unit
			Document document = new Document(iUnit.getSource());
			edits.apply(document);
			// adding statement to the buffer iUnit will be created in the addInstance in the JavaSystem
			iUnit.getBuffer().setContents(document.get());
		}
		
		// delete
		for (Variant v : compareUnions.compareVariants_Unions.get(union_name).getDeletions()) {
			CompilationUnit astRoot = parse(iUnit);
			// get text edits
			TextEdit edits = removeVisitMethod(astRoot, v);
			// apply the text edits to the compilation unit
			Document document = new Document(iUnit.getSource());
			edits.apply(document);
			// adding statement to the buffer iUnit will be created in the addInstance in the JavaSystem
			iUnit.getBuffer().setContents(document.get());
		}
	}
	
	
		
	
	/*
	 * helper method------------------------------------------------------------------------------------- 
	 */
	
	private static TextEdit removeVisitMethod(CompilationUnit astRoot, Variant v) throws JavaModelException, IllegalArgumentException {
		// create a ASTRewrite
		AST ast = astRoot.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
		
		MethodDeclaration targetMethod = null;
		// for getting insertion position (first class in the file)
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
		for (MethodDeclaration methodDecl : typeDecl.getMethods()) {
			SingleVariableDeclaration parameter = (SingleVariableDeclaration) methodDecl.parameters().get(0);
			String variant_name = parameter.getName().toString();
			String variant_type = parameter.getType().toString();
			// determine if it's the variant that we want to remove
			if (variant_name.equals(v.getName().toLowerCase()) && variant_type.equals(v.getName())) {
				targetMethod = methodDecl;
				break;
			}
			
		}
		// create ListRewrite
		ListRewrite listRewrite = rewriter.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.remove(targetMethod, null);
		TextEdit edits = rewriter.rewriteAST();
		return edits;
	}
	
	private static TextEdit insertVisitInInterpreter(CompilationUnit astRoot, Variant v) throws JavaModelException, IllegalArgumentException {
		// create a ASTRewrite
		AST ast = astRoot.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
		// for getting insertion position (first class in the file)
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
		
		// create parameter for method
		SingleVariableDeclaration parameter = ast.newSingleVariableDeclaration();
		parameter.setName(ast.newSimpleName(v.getName().toLowerCase()));
		parameter.setType(ast.newSimpleType(ast.newName(v.getName())));
		
		
		// create method
		MethodDeclaration newMethodDecl = ast.newMethodDeclaration();
		newMethodDecl.parameters().add(parameter);
		newMethodDecl.setName(ast.newSimpleName("visit"));
		newMethodDecl.setBody(ast.newBlock());
		newMethodDecl.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		
		// create ListRewrite
		ListRewrite listRewrite = rewriter.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(newMethodDecl, null);
		TextEdit edits = rewriter.rewriteAST();
		return edits;
	}
	
	private static TextEdit insertVisitInInterface(CompilationUnit astRoot, Variant v) throws JavaModelException, IllegalArgumentException {
		// create a ASTRewrite
		AST ast = astRoot.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
		// for getting insertion position (first class in the file)
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
		
		// create parameter for method
		SingleVariableDeclaration parameter = ast.newSingleVariableDeclaration();
		parameter.setName(ast.newSimpleName(v.getName().toLowerCase()));
		parameter.setType(ast.newSimpleType(ast.newName(v.getName())));
		// create method
		MethodDeclaration newMethodDecl = ast.newMethodDeclaration();
		newMethodDecl.parameters().add(parameter);
		newMethodDecl.setName(ast.newSimpleName("visit"));
		
		// create ListRewrite
		ListRewrite listRewrite = rewriter.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(newMethodDecl, null);
		TextEdit edits = rewriter.rewriteAST();
		return edits;
	}

	private static TextEdit removeInstance(CompilationUnit astRoot, Traversal t, Variant v, Pair<ast.Type, String> traversal_union_type) throws JavaModelException, IllegalArgumentException {
		// create a ASTRewrite
		AST ast = astRoot.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
		// for getting insertion position (first method)
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
		MethodDeclaration methodDecl = typeDecl.getMethods()[0];
		Block block = methodDecl.getBody();// add to this block
		
		// loop through all statements in this block
		for (Object o : block.statements()) {
			Statement s = (Statement) o;
			while (s instanceof IfStatement) {
				IfStatement is = (IfStatement) s;
				if (is.getExpression() instanceof InstanceofExpression) {
					InstanceofExpression instanceOfExp = (InstanceofExpression) is.getExpression();		
					if (instanceOfExp.getLeftOperand() instanceof Name) {
						Name leftExp = (Name) instanceOfExp.getLeftOperand();
						if (leftExp.resolveBinding() instanceof IVariableBinding) {
							// check if cases match traversal and the variant
							String left_name = leftExp.getFullyQualifiedName();
							String right_TypeName = instanceOfExp.getRightOperand().toString();
							if (left_name.equals(traversal_union_type.b) && right_TypeName.equals(v.getName())) {
								// replace the current if statement with its else statements, but only the previous comments will remain
								rewriter.replace(is, is.getElseStatement(), null);
								// create ListRewrite
								TextEdit edits = rewriter.rewriteAST();
								return edits;
								}
						}
					}
				}
				s = is.getElseStatement();
			}
		}
		return null;
	}

	private static TextEdit insertInstance(CompilationUnit astRoot, Traversal t, Variant v, Pair<ast.Type, String> traversal_union_type) 
			throws MalformedTreeException, org.eclipse.jface.text.BadLocationException, JavaModelException, IllegalArgumentException {
		// create a ASTRewrite
		AST ast = astRoot.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
		// for getting insertion position (first method)
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
		MethodDeclaration methodDecl = typeDecl.getMethods()[0];
		Block block = methodDecl.getBody();// add to this block
		// create new conditionalExp
		Type rightType = ast.newSimpleType(ast.newName(v.getName()));		
		Name leftExp = ast.newName(traversal_union_type.b.toString());
		InstanceofExpression conditionExp = ast.newInstanceofExpression();
		conditionExp.setLeftOperand(leftExp);
		conditionExp.setRightOperand(rightType);
		// create new if statement
		IfStatement newIfStatement = ast.newIfStatement();
		newIfStatement.setExpression(conditionExp);
		
		//set then statement
		Block then = ast.newBlock();
		Assignment assign = ast.newAssignment();
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName(v.getName().toLowerCase()));
		VariableDeclarationExpression left = ast.newVariableDeclarationExpression(fragment);
		left.setType((Type) ASTNode.copySubtree(ast, rightType));
		assign.setLeftHandSide(left);
		assign.setOperator(Assignment.Operator.ASSIGN);
		CastExpression right = ast.newCastExpression();
		right.setType((Type) ASTNode.copySubtree(ast,rightType));
		right.setExpression((Expression) ASTNode.copySubtree(ast, leftExp));
		assign.setRightHandSide(right);
		then.statements().add(ast.newExpressionStatement(assign));
		
		if (t.getReturn_type().toString().equals("void")) {} 
		else if (t.getReturn_type() instanceof NumericType) {
			ReturnStatement returnStmt = ast.newReturnStatement();
			returnStmt.setExpression(ast.newNumberLiteral("0"));
			then.statements().add(returnStmt);
			newIfStatement.setThenStatement(then);
		} else if (t.getReturn_type() instanceof BooleanType) {
			ReturnStatement returnStmt = ast.newReturnStatement();
			returnStmt.setExpression(ast.newBooleanLiteral(false));
			then.statements().add(returnStmt);
			newIfStatement.setThenStatement(then);
		} else {
			ReturnStatement returnStmt = ast.newReturnStatement();
			returnStmt.setExpression(ast.newNullLiteral());
			then.statements().add(returnStmt);
			newIfStatement.setThenStatement(then);
		}
		// add stub comment
		ListRewrite stubComment = rewriter.getListRewrite(then, Block.STATEMENTS_PROPERTY);
		Statement placeHolder = (Statement) rewriter.createStringPlaceholder("// TODO Auto-generated case match pattern", ASTNode.BLOCK);
		stubComment.insertFirst(placeHolder, null);
	
		// loop through the if else instances and insert at last
		for (Object o : block.statements()) {
			Statement s = (Statement) o;
			while (s instanceof IfStatement) {
				IfStatement is = (IfStatement) s;
				if (is.getElseStatement() instanceof Block) {
					// reach the last statement
					Block finalElse = (Block) ASTNode.copySubtree(ast, is.getElseStatement());
					newIfStatement.setElseStatement(finalElse);
					rewriter.replace(is.getElseStatement(), newIfStatement, null);
					break;
				}
				s = is.getElseStatement();
			}
			
		}
		
		TextEdit edits = rewriter.rewriteAST();
		return edits;
	}
	
	private static CompilationUnit parse(ICompilationUnit unit) throws JavaModelException {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		// visit comments
//		for (Comment comment : (List<Comment>) cu.getCommentList()) {
//		    comment.accept(new CommentVisitor(cu, unit.getBuffer().getContents()));
//		}
		return cu;
	}

}

// comment visitor
class CommentVisitor extends ASTVisitor {
	CompilationUnit cu;
	String source;

	public CommentVisitor(CompilationUnit cu, String source) {
		super();
		this.cu = cu;
		this.source = source;
	}

	public boolean visit(LineComment node) {
		int start = node.getStartPosition();
		int end = start + node.getLength();
		node.getAlternateRoot();
		String comment = source.substring(start, end);
		System.out.println(comment);
		return true;
	}

	public boolean visit(BlockComment node) {
		int start = node.getStartPosition();
		int end = start + node.getLength();
		String comment = source.substring(start, end);
		System.out.println(comment);
		return true;
	}
	
	public boolean visit(Javadoc node) {
		int start = node.getStartPosition();
		int end = start + node.getLength();
		String comment = source.substring(start, end);
		System.out.println(comment);
		return true;
	}

}
