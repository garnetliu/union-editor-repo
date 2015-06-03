package unioneditor.actions;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import unioneditor.files.JavaFileSystem;

public class RightClickFileAction extends ActionDelegate implements IEditorActionDelegate {
	
	private JavaFileSystem jf;

	/**
	 * constructor
	 * @throws IOException 
	 */
	public RightClickFileAction() throws CoreException, IOException {
		this.jf = new JavaFileSystem();
	}
	

	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		MessageBox box = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		box.setMessage("associating myfile to java file...");
		box.open();
		
		try {
			jf.associateJavaFiles();
		} catch (CoreException | IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}
}
