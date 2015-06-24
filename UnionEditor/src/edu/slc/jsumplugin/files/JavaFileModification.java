package edu.slc.jsumplugin.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import javax.swing.text.BadLocationException;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import ast.Type.*;
import ast.Ast.*;

public class JavaFileModification {
	
//	public IPackageFragment fragment;
//	public ICompilationUnit[] iUnits;
//	public ICompilationUnit iUnit;

	
	public JavaFileModification(ICompilationUnit iUnit) throws CoreException {
//		this.fragment = (IPackageFragment) iUnit.getParent();
//		this.iUnits = fragment.getCompilationUnits();
//		this.iUnit = iUnit;
	}
	
	/*
	 * main function & method------------------------------------------------------------------------------------- 
	 */
	
	// check the condition expression of the instanceOf chain
	public static void checkConditionExpression(ICompilationUnit iUnit) throws JavaModelException {
		
		List<Type> types = new ArrayList<Type>();
		//loop through all statements in the method
		for (Object o : getFirstMethod(iUnit).getBody().statements()) {
			Statement s = (Statement) o;
			while (s instanceof IfStatement) {
				IfStatement is = (IfStatement) s;
				if (is.getExpression() instanceof InstanceofExpression) {
					InstanceofExpression instanceOfExp = (InstanceofExpression) is.getExpression();
					types.add(instanceOfExp.getRightOperand());
					if (instanceOfExp.getLeftOperand() instanceof Name) {
						Name leftExp = (Name) instanceOfExp.getLeftOperand();
						if (leftExp.resolveBinding() instanceof IVariableBinding) {
							IVariableBinding leftVariableBinding = (IVariableBinding) leftExp.resolveBinding();
							System.out.printf("left expression type: %s, right type: %s. \n", leftVariableBinding.getType().getName(), instanceOfExp.getRightOperand().resolveBinding().getName());
							//System.out.println(leftVariableBinding.getType().isCastCompatible(instanceOfExp.getRightOperand().resolveBinding()));
						}
					}
				}
				s = is.getElseStatement();
			}
		}
		
		ITypeBinding superType = null;
		for (Type t : types) {
			if (superType != null){
				System.out.println(t.resolveBinding().getSuperclass().isEqualTo(superType));
			}
			superType = t.resolveBinding().getSuperclass();
		}
	}
	
	// compare two unions and modify the instance file
	public static void modifyInstance(ICompilationUnit iUnit, Traversal t, Unions beforeEdit, Unions afterEdit) 
			throws JavaModelException, IllegalArgumentException, MalformedTreeException, org.eclipse.jface.text.BadLocationException {
		// loop through all the argument types in the traversal
		for (ast.Type union_type : t.arg_types) {
			// pick the argument types that are union types
			if (afterEdit.getNames().contains(union_type.toString())) {
				// found additional union types
				if (!beforeEdit.getNames().contains(union_type.toString())) { // add this additional union with all the corresponding variants, but what if it's only renamed?	
				} else {
					int beforeVariantSize = beforeEdit.getNames().size();
					int afterVariantSize = afterEdit.getNames().size();
					// check if there are additional variants
					for (Variant v : afterEdit.unions.get(union_type.toString())) {
						// found additional variant
						if (!beforeEdit.unions.get(union_type.toString()).contains(v)) {
							// parse
							CompilationUnit astRoot = parse(iUnit);
							// create a ASTRewrite
							AST ast = astRoot.getAST();
							ASTRewrite rewriter = ASTRewrite.create(ast);
							// for getting insertion position
							TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
							MethodDeclaration methodDecl = typeDecl.getMethods()[0];
							Block block = methodDecl.getBody();// add to this block
							// create new conditionalExp
							Name leftExp = ast.newName(t.getParameterName(0));
							Type rightType = ast.newSimpleType(ast.newName(v.getName()));
							InstanceofExpression conditionExp = ast.newInstanceofExpression();
							conditionExp.setLeftOperand(leftExp);
							conditionExp.setRightOperand(rightType);
							// create new if statement
							IfStatement newIfStatement = ast.newIfStatement();
							newIfStatement.setExpression(conditionExp);
							//set then statement
							Block then = ast.newBlock();
							if (union_type.toString().equals("void")) {} 
							else if (t.return_type instanceof NumericType) {
								ReturnStatement returnStmt = ast.newReturnStatement();
								returnStmt.setExpression(ast.newNumberLiteral("0"));
								then.statements().add(returnStmt);
								newIfStatement.setThenStatement(then);
							} else {
								ReturnStatement returnStmt = ast.newReturnStatement();
								returnStmt.setExpression(ast.newNullLiteral());
								then.statements().add(returnStmt);
								newIfStatement.setThenStatement(then);
							}
							ListRewrite stubComment = rewriter.getListRewrite(then, Block.STATEMENTS_PROPERTY);
							Statement placeHolder = (Statement) rewriter.createStringPlaceholder("// TODO Auto-generated case match pattern", ASTNode.BLOCK);
							stubComment.insertFirst(placeHolder, null);
							//set else
							IfStatement elseIfs = (IfStatement) ASTNode.copySubtree(ast, (ASTNode) block.statements().get(0));
							newIfStatement.setElseStatement(elseIfs);
							// create ListRewrite
							ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
							// listRewrite.insertFirst(newIfStatement, null);
							listRewrite.replace((ASTNode) block.statements().get(0), newIfStatement, null);

							TextEdit edits = rewriter.rewriteAST();
							// apply the text edits to the compilation unit
							Document document = new Document(iUnit.getSource());
							edits.apply(document);
							// this is the code for adding statements
							iUnit.getBuffer().setContents(document.get());
							
							// increament afterVariantSize
							afterVariantSize++;
						}
					}
					// found & remove redundant existing variant
					if (beforeVariantSize != afterVariantSize) {
						for (Variant v : beforeEdit.unions.get(union_type.toString())) {
							// found additional variant
							if (!afterEdit.unions.get(union_type.toString()).contains(v)) {
								
							}
						}
					}
				}
			}
		}
	}
	
	/*
	 * helper method------------------------------------------------------------------------------------- 
	 */
	
	
	public void getAnnotations(ICompilationUnit unit) throws JavaModelException {
		for (IType type : unit.getAllTypes()) {
			for (IAnnotation annotation : type.getAnnotations()) {
				System.out.println(type.getElementName()+ " is a " +annotation.getElementName());
				
			}
		}
	}
	
	private static MethodDeclaration getFirstMethod(ICompilationUnit iUnit) throws JavaModelException {
		// parse compilation unit
		CompilationUnit astRoot = parse(iUnit);
		// choose type and method
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
		MethodDeclaration methodDecl = typeDecl.getMethods()[0];
		return methodDecl;
	}
	
	private static CompilationUnit parse(ICompilationUnit unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

}
