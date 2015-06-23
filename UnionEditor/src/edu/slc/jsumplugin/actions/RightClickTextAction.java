package edu.slc.jsumplugin.actions;
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

import edu.slc.jsumplugin.Activator;
import edu.slc.jsumplugin.files.*;
import ast.Ast.*;
 
 
public class RightClickTextAction implements IObjectActionDelegate {
	private Shell shell;
	private JavaFileSystem jf;
 
	public RightClickTextAction() throws CoreException, IOException {
		super();
		this.jf = new JavaFileSystem("TestJavaProject");
	}
 
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}
 
 
	@Override
	public void run(IAction action) {
//		try {
//			//get editor
//			IEditorPart editorPart = Activator.getDefault().getWorkbench()
//					.getActiveWorkbenchWindow().getActivePage()
//					.getActiveEditor();
//			
//			if (editorPart instanceof AbstractTextEditor) {
//				int offset = 0;
//				int length = 0;
//				String selectedText = null;
//				IEditorSite iEditorSite = editorPart.getEditorSite();
//				if (iEditorSite != null) {
//					//get selection provider
//					ISelectionProvider selectionProvider = iEditorSite
//							.getSelectionProvider();
//					if (selectionProvider != null) {
//						ISelection iSelection = selectionProvider
//								.getSelection();
//						//offset
//						offset = ((ITextSelection) iSelection).getOffset();
//						if (!iSelection.isEmpty()) {
//							selectedText = ((ITextSelection) iSelection)
//									.getText();
//							//length
//							length = ((ITextSelection) iSelection).getLength();
//							
//							//get all union names in all unions
//							List<Unions> unions = jf.findAllUnions();
//							List<String> union_names = new ArrayList<String>();
//							for (Unions us : unions) {
//								for (String union_name : us.getNames()) {
//									union_names.add(union_name);
//								}
//							}
//							
//							String[] unions_names = new String[unions.size()];
//							for (int i = 0; i < unions.size(); i++) {
//								unions_names[i] = unions.get(i).getName();
//							}
//							
//							// report in popup menu
//							MessageDialog chooseUnion = new  MessageDialog(shell, "Insert expand", null,
//									"Length: " + length + "    Offset: " + offset, 0 ,
//									union_names.toArray(new String[union_names.size()]), 0);	
//							int unionChoice = chooseUnion.open();
//
//							 //insert variants
//							 IDocumentProvider provider = ((AbstractTextEditor) editorPart).getDocumentProvider();
//							 IDocument document = provider.getDocument(editorPart.getEditorInput());
//							 jf.insertVariants(unionChoice, offset, document);
//						}
//					}
//				}
//			}
//		} catch (Exception e) {}
	}
 
 
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
 
}