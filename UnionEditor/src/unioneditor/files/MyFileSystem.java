package unioneditor.files;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import main.ConvertUnion;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import format.FormatUnionClass;

public class MyFileSystem {

	IWorkspace workspace;
	IWorkspaceRoot root;
	IProject project;
	IFolder folder;
	List<IFile> files;

	public MyFileSystem() {
		this.workspace = ResourcesPlugin.getWorkspace();
		this.root = workspace.getRoot();
		this.project = root.getProject("MyProject");
		this.folder = project.getFolder("MyFolder");
	}

	// additional methods-----------------------------------

	public List<IFile> findFileSuffix(IContainer container, String suffix, List<IFile> files) throws CoreException {
		for (IResource r : container.members()) {
			if (r instanceof IContainer) {
				files.addAll(findFileSuffix((IContainer) r, suffix, files));
			} else if (r instanceof IFile 
					&& r.getName().substring(r.getName().lastIndexOf('.') + 1).equals(suffix)) {
				files.add((IFile) r);
			}
		}
		return files;
	}
	
	public List<IFile> findFilename(IContainer container, String filename, List<IFile> files) throws CoreException {
		for (IResource r : container.members()) {
			if (r instanceof IContainer) {
				files.addAll(findFilename((IContainer) r, filename, files));
			} else if (r instanceof IFile 
					&& r.getName().substring(0,r.getName().lastIndexOf('.')).equals(filename)) {
				files.add((IFile) r);
			}
		}
		return files;
	}

	public void associateJavaFile() throws CoreException, IOException {
		files = new ArrayList<IFile>();
		files = findFileSuffix(folder, "myfile", files);

		if (files.size() != 0) {// there is at least one file with "myfile" affix
			for (IFile file : files) {
				InputStream content = file.getContents();
				
				// convert *.myfile contents to java style and decide class name				
				ConvertUnion cu = new ConvertUnion(content);
				String className = "Union" + cu.getUnion().getName();
				FormatUnionClass fu = new FormatUnionClass(cu.getUnion(), className);
				InputStream newContent = new ByteArrayInputStream(fu.toString().getBytes());

				// create java file
				IFile newFile = folder.getFile(className + ".java");
				if (newFile.exists()) {
					newFile.setContents(newContent, IResource.NONE, null);
				} else {
					newFile.create(newContent, IResource.NONE, null);
				}
			}
		}
	}

	public void createProject() throws CoreException {
		// find/create resources
			if (!project.exists()) {
				project.create(null);
			}
			if (!project.isOpen()) {
				project.open(null);
			}
			if (!folder.exists()) {
				folder.create(IResource.NONE, true, null);
			}
	}
}