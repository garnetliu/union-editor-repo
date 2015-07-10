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
			//List<CommentLoc> comments = getComments(iUnit);
			// get text edits
			Pair<ast.Type, String> traversal_union_type = compareUnions.getUnionTypeInTraversal(t);
//			TextEdit edits = removeInstance(astRoot, t, v, traversal_union_type, comments);
//			// apply the text edits to the compilation unit
//			Document document = new Document(iUnit.getSource());
//			edits.apply(document);
//			// adding statement to the buffer iUnit will be created in the addInstance in the JavaSystem
//			iUnit.getBuffer().setContents(document.get());
			removeInstance(iUnit, astRoot, t, v, traversal_union_type);
		}
		// modify
		for (Variant v : compareUnions.getTraversalInstaces(t, 2)) {
			CompilationUnit astRoot = parse(iUnit);
			// get text edits
			Pair<ast.Type, String> traversal_union_type = compareUnions.getUnionTypeInTraversal(t);
			TextEdit edits = insertModifiedMessageInstances(astRoot, t, v, compareUnions.getVariantModifyMessage(t, v), traversal_union_type);
			// apply the text edits to the compilation unit
			Document document = new Document(iUnit.getSource());
			edits.apply(document);
			// adding statement to the buffer iUnit will be created in the addInstance in the JavaSystem
			iUnit.getBuffer().setContents(document.get());
			
		}

	}

	public static void modifyVisitorInterpreter(String union_name, ICompilationUnit iUnit, CompareUnions compareUnions) 
			throws JavaModelException, MalformedTreeException, BadLocationException {
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
		
		// modify
		for (Variant v: compareUnions.compareVariants_Unions.get(union_name).getModified()) {
			CompilationUnit astRoot = parse(iUnit);
			// get text edits
			TextEdit edits = insertModifiedMessageInInterpreter(astRoot, v, compareUnions.getVariantModifyMessage(union_name, v));
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
		
//		// modify
//		for (Variant v: compareUnions.compareVariants_Unions.get(union_name).getModified()) {
//			CompilationUnit astRoot = parse(iUnit);
//			// get text edits
//			TextEdit edits = insertModifiedMessageInInterface(astRoot, v, compareUnions.getVariantModifyMessage(union_name, v));
//			// apply the text edits to the compilation unit
//			Document document = new Document(iUnit.getSource());
//			edits.apply(document);
//			// adding statement to the buffer iUnit will be created in the addInstance in the JavaSystem
//			iUnit.getBuffer().setContents(document.get());
//		}
	}
	
	
		
	
	/*
	 * helper method------------------------------------------------------------------------------------- 
	 */

	private static TextEdit insertModifiedMessageInInterpreter(CompilationUnit astRoot, Variant v, String variantModifyMessage) throws JavaModelException, IllegalArgumentException {
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
		
		// add variant modify comment to the then block
		ListRewrite stubComment = rewriter.getListRewrite(targetMethod.getBody(), Block.STATEMENTS_PROPERTY);
		Statement placeHolder = (Statement) rewriter.createStringPlaceholder(variantModifyMessage, ASTNode.BLOCK);
		stubComment.insertFirst(placeHolder, null);
		
		TextEdit edits = rewriter.rewriteAST();
		return edits;
	}
	
	private static TextEdit insertModifiedMessageInInterface(CompilationUnit astRoot, Variant v, String variantModifyMessage) throws JavaModelException, IllegalArgumentException {
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
		
		// add variant modify comment to the then block
		ListRewrite stubComment = rewriter.getListRewrite(typeDecl, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		MethodDeclaration placeHolder = (MethodDeclaration) rewriter.createStringPlaceholder(variantModifyMessage, ASTNode.METHOD_DECLARATION);
		stubComment.insertBefore(placeHolder, targetMethod, null);
		
		
		TextEdit edits = rewriter.rewriteAST();
		return edits;
	}
	
	private static TextEdit removeVisitMethod(CompilationUnit astRoot, Variant v) 
			throws JavaModelException, IllegalArgumentException {
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
	
//	private static TextEdit removeInstance(CompilationUnit astRoot, Traversal t, Variant v, 
//			Pair<ast.Type, String> traversal_union_type, List<CommentLoc> comments) throws JavaModelException, IllegalArgumentException {
//		// create a ASTRewrite
//		AST ast = astRoot.getAST();
//		ASTRewrite rewriter = ASTRewrite.create(ast);
//		// for getting insertion position (first method)
//		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
//		MethodDeclaration methodDecl = typeDecl.getMethods()[0];
//		Block block = methodDecl.getBody();// add to this block
//		
//		// loop through all statements in this block
//		for (Object o : block.statements()) {
//			Statement s = (Statement) o;
//			while (s instanceof IfStatement) {
//				IfStatement is = (IfStatement) s;
//				if (is.getExpression() instanceof InstanceofExpression) {
//					InstanceofExpression instanceOfExp = (InstanceofExpression) is.getExpression();		
//					if (instanceOfExp.getLeftOperand() instanceof Name) {
//						Name leftExp = (Name) instanceOfExp.getLeftOperand();
//						if (leftExp.resolveBinding() instanceof IVariableBinding) {
//							// check if cases match traversal and the variant
//							String left_name = leftExp.getFullyQualifiedName();
//							String right_TypeName = instanceOfExp.getRightOperand().toString();
//							if (left_name.equals(traversal_union_type.b) && right_TypeName.equals(v.getName())) {
//								// replace the current if statement with its else statements, but only the previous comments will remain
//								Statement isElse = is.getElseStatement();
//								for (CommentLoc cl : comments) {
//									if (cl.start > isElse.getStartPosition() && cl.end < (isElse.getStartPosition() + isElse.getLength())) {
//										System.out.println(cl.content);
//										// add comment
//										if ((isElse instanceof IfStatement)) {
////											ListRewrite addComment = rewriter.getListRewrite(isElse, Block.STATEMENTS_PROPERTY);
////											Statement placeHolder = (Statement) rewriter.createStringPlaceholder(cl.content, ASTNode.BLOCK);
////											addComment.insertAt(isElse, cl.start, null);
//										}
//									}
//								}
//								
//								
//								rewriter.replace(is, isElse, null);
//								// create ListRewrite
//								TextEdit edits = rewriter.rewriteAST();
//								return edits;
//							}
//						}
//					}
//				}
//				s = is.getElseStatement();
//			}
//		}
//		return null;
//	}
	
	private static TextEdit insertModifiedMessageInstances(CompilationUnit astRoot, Traversal t, Variant v, 
			String modifiedMessage, Pair<ast.Type, String> traversal_union_type) throws JavaModelException, IllegalArgumentException {
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
								// add variant modify comment to the then block
								ListRewrite stubComment = rewriter.getListRewrite(is.getThenStatement(), Block.STATEMENTS_PROPERTY);
								Statement placeHolder = (Statement) rewriter.createStringPlaceholder(modifiedMessage, ASTNode.BLOCK);
								stubComment.insertFirst(placeHolder, null);
							}
						}
					}
				}
				s = is.getElseStatement();
			}
		}
		
		
		TextEdit edits = rewriter.rewriteAST();
		return edits;
	}
	
	private static void removeInstance(ICompilationUnit iUnit, CompilationUnit astRoot, Traversal t, Variant v,
			Pair<ast.Type, String> traversal_union_type) throws JavaModelException, IllegalArgumentException {

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
								// replace the current if statement with its else statements (through the ICompilation unit)
								Statement isElse = is.getElseStatement();
								int isStart = is.getStartPosition();
								int elseStart = isElse.getStartPosition();
								int elseEnd = isElse.getStartPosition() + isElse.getLength();
								String contents = iUnit.getBuffer().getContents();
								String elseContents = contents.substring(elseStart, elseEnd);
								String newContents = contents.substring(0, isStart) + elseContents + contents.substring(elseEnd, contents.length());
								//System.out.println(newContents);
								iUnit.getBuffer().setContents(newContents);
								
//								astRoot = parse(iUnit);
//								typeDecl = (TypeDeclaration) astRoot.types().get(0);
//								methodDecl = typeDecl.getMethods()[0];
//								block = methodDecl.getBody();// add to this block
								
							}
						}
					}
				}
				s = is.getElseStatement();
			}
		}
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
		List<CommentLoc> comments = new ArrayList<CommentLoc>();
		for (Comment comment : (List<Comment>) cu.getCommentList()) {
		    comment.accept(new CommentVisitor(cu, unit.getBuffer().getContents(), comments));
		}
		return cu;
	}
	
	private static List<CommentLoc> getComments(ICompilationUnit unit) throws JavaModelException {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		// visit comments
		List<CommentLoc> comments = new ArrayList<CommentLoc>();
		for (Comment comment : (List<Comment>) cu.getCommentList()) {
		    comment.accept(new CommentVisitor(cu, unit.getBuffer().getContents(), comments));
		}
		return comments;
	}
}

// comment visitor
class CommentVisitor extends ASTVisitor {
	CompilationUnit cu;
	String source;
	private List<CommentLoc> comments;

	public CommentVisitor(CompilationUnit cu, String source, List<CommentLoc> comments) {
		super();
		this.cu = cu;
		this.source = source;
		this.comments = comments;
	}

	public boolean visit(LineComment node) {
		int start = node.getStartPosition();
		int end = start + node.getLength();
		node.getAlternateRoot();
		String comment = source.substring(start, end);
		comments.add(new CommentLoc(start, end, comment));
		return true;
	}

	public boolean visit(BlockComment node) {
		int start = node.getStartPosition();
		int end = start + node.getLength();
		String comment = source.substring(start, end);
		comments.add(new CommentLoc(start, end, comment));
		return true;
	}
	
	public boolean visit(Javadoc node) {
		int start = node.getStartPosition();
		int end = start + node.getLength();
		String comment = source.substring(start, end);
		comments.add(new CommentLoc(start, end, comment));
		return true;
	}
}

class CommentLoc {
	
	public int start;
	public int end;
	public String content;

	public CommentLoc(int start, int end, String content) {
		this.start = start;
		this.end = end;
		this.content = content;
	}
}
