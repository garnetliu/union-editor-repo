package unioneditor.editors;//include org.eclipse.jface...in the required plug-ins

import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ResourceAction;

public class UnionEditor extends TextEditor {
	
	private ColorManager colorManager;

	public UnionEditor() {
		super();
		colorManager = new ColorManager();
		// install the source configuration
		setSourceViewerConfiguration(new UnionConfiguration(colorManager));
		// install the document provider
		// setDocumentProvider(new MyDocumentProvider());
	}

	protected void createActions() {
		super.createActions();
		// ... add other editor actions here
//		IWorkbenchAction fSaveAction = ActionFactory.SAVE.create(getSite().getWorkbenchWindow());
//		setAction(ITextEditorActionConstants.SAVE, fSaveAction);
		
	}

	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
