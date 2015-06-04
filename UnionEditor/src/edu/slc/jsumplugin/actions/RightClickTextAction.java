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
		this.jf = new JavaFileSystem();
	}
 
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}
 
 
	@Override
	public void run(IAction action) {
		try {
			//get editor
			IEditorPart editorPart = Activator.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			
			if (editorPart instanceof AbstractTextEditor) {
				int offset = 0;
				int length = 0;
				String selectedText = null;
				IEditorSite iEditorSite = editorPart.getEditorSite();
				if (iEditorSite != null) {
					//get selection provider
					ISelectionProvider selectionProvider = iEditorSite
							.getSelectionProvider();
					if (selectionProvider != null) {
						ISelection iSelection = selectionProvider
								.getSelection();
						//offset
						offset = ((ITextSelection) iSelection).getOffset();
						if (!iSelection.isEmpty()) {
							selectedText = ((ITextSelection) iSelection)
									.getText();
							//length
							length = ((ITextSelection) iSelection).getLength();
							
							//get union names
							List<Unions> unions = jf.findAllUnions();
							String[] unions_names = new String[unions.size()];
							for (int i = 0; i < unions.size(); i++) {
								unions_names[i] = unions.get(i).getName();
							}
							
							
							//report in popup menu
							MessageDialog chooseUnions = new  MessageDialog(shell, "Insert expand", null,
									"Length: " + length + "    Offset: " + offset, 0 , unions_names, 0);	
							int unionsChoice = chooseUnions.open();
							
							//get separate unions in another popup menu
							int unionChoice = 0;
							String[] union_names = unions.get(unionsChoice).getNames().toArray(new String[0]);
							if (union_names.length > 1) {
								MessageDialog chooseUnion = new  MessageDialog(shell, "Insert expand", null,
										"Length: " + length + "    Offset: " + offset, 0 , union_names, 0);
								unionChoice = chooseUnion.open();
							}

							 //insert variants
							 IDocumentProvider provider = ((AbstractTextEditor) editorPart).getDocumentProvider();
							 IDocument document = provider.getDocument(editorPart.getEditorInput());
							 jf.insertVariants(unionsChoice, unionChoice, offset, document);
						}
					}
				}
			}
		} catch (Exception e) {}
	}
 
 
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}
 
}