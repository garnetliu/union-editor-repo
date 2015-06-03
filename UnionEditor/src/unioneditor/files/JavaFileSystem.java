package unioneditor.files;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.ConvertUnion;
import format.*;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

import ast.Ast.Union;

public class JavaFileSystem {

	private IWorkspace workspace;
	private IWorkspaceRoot root;
	private IProject project;
	private IFolder folder;
	IPackageFragment fragment;
	List<Union> unions;
	List<ICompilationUnit> javaFiles;

	// Constructor
	public JavaFileSystem() throws CoreException, IOException {
		/*
		 * set up project
		 */
		this.root = ResourcesPlugin.getWorkspace().getRoot();
		this.project = root.getProject("TestJavaProject");
		this.folder = project.getFolder("src");
		
		// create a project
		if (!project.exists()) { project.create(null); }
		if (!project.isOpen()) { project.open(null); }
		
		// set the Java nature
		IProjectDescription description = project.getDescription();
		description.setNatureIds(new String[] { JavaCore.NATURE_ID });

		// create the project
		project.setDescription(description, null);
		IJavaProject javaProject = JavaCore.create(project);

		// set the build path
		IClasspathEntry[] buildPath = {
				JavaCore.newSourceEntry(project.getFullPath().append("src")), JavaRuntime.getDefaultJREContainerEntry() };

		javaProject.setRawClasspath(buildPath, project.getFullPath().append("bin"), null);

		// create folder by using resources package
		if (!folder.exists()) { folder.create(true, true, null); }

		// Add folder to Java element
		IPackageFragmentRoot srcFolder = javaProject.getPackageFragmentRoot(folder);

		// create package fragment
		this.fragment = srcFolder.getPackageFragment("main");
		if (!fragment.exists()) { srcFolder.createPackageFragment("main", true, null); }
		
		/*
		 * collect all unions and java files
		 */
		this.unions = findAllUnions();
//		this.javaFiles = new ArrayList<ICompilationUnit>();
//		findAllICompilationUnits(project, "java", javaFiles);
		
		// add resource change listener
		//JavaCore.addElementChangedListener(new MyJavaElementChangeReporter());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				new IResourceChangeListener() {
					public void resourceChanged(IResourceChangeEvent event) {
						System.out.println("Something changed!");
					}
				});
	}


	// major methods
	public void associateJavaFiles() throws CoreException, IOException {
		unions = findAllUnions();
		List<String> classNames = new ArrayList<String>();
		for (Union union : unions) {
			// convert content of union to java style and decide class name
			String className = "Union" + union.getName();
			classNames.add(className);
			FormatUnionClass fu = new FormatUnionClass(union, className);
			Formatter packageHeading = new Formatter();
			packageHeading.format("package %s;\n", fragment.getElementName());
			String classContent = packageHeading.toString() + fu.toString();
			packageHeading.close();
			// InputStream newContent = new ByteArrayInputStream(fu.toString().getBytes());
			// MultiTextEdit edit = new MultiTextEdit(); edit.addChild(new InsertEdit(0, fu.toString()));

			// create java file
			ICompilationUnit newFile = fragment.getCompilationUnit(className+".java");
			if (newFile.exists()) {
				// replace the content of original buffer
				newFile.getBuffer().replace(0, newFile.getBuffer().getLength(), classContent);
			} else {
				fragment.createCompilationUnit(className + ".java", classContent, false, null);
			}
			
		}
		//clear extra java file
		//clearExtra(classNames);
	}
	
//	private void clearExtra(List<String> classNames) throws CoreException, IOException {
//		for (ICompilationUnit javaFile : findAllICompilationUnits(project, "java", javaFiles)) {
//			if (!classNames.contains(javaFile.getElementName())) {
//				System.out.println(javaFile.getElementName());
//				javaFile.delete(true, null);
//			}
//		}
//	}


	public void insertVariants(Integer unionChoice, Integer offset, IDocument document) throws BadLocationException {
		FormatUnionVariants fv = new FormatUnionVariants(unions.get(unionChoice));
		document.replace(offset, 0, fv.toString());
	}
	
	// helper methods-----------------------------------
	
	public List<Union> findAllUnions() throws CoreException, IOException {
		List<Union> unions = new ArrayList<Union>();
		List<IFile> files = new ArrayList<IFile>();
		files = findFileSuffix(project, "myfile", files);
		
		if (files.size() != 0) {// there is at least one file with wanted affix
			for (IFile file : files) {
				InputStream content = file.getContents();
				// convert *.myfile contents to java style and decide class name				
				ConvertUnion cu = new ConvertUnion(content);
				unions.add(cu.getUnion());
			}
		}
		return unions;
	}

//	private List<ICompilationUnit> findAllICompilationUnits(
//			IContainer container, String suffix, List<ICompilationUnit> units) throws CoreException {
//		for (IResource r : container.members()) {
//			if (r instanceof IContainer) {
//				findAllICompilationUnits((IContainer) r, suffix, units);
//			} else if (r instanceof ICompilationUnit) {
//					units.add((ICompilationUnit) r);
//			}
//		}
//		return units;
//	}
	
	private List<IFile> findFileSuffix(
			IContainer container, String suffix, List<IFile> files) throws CoreException {
		for (IResource r : container.members()) {
			if (r instanceof IContainer) {
				findFileSuffix((IContainer) r, suffix, files);
			} else if (r instanceof IFile
					&& r.getName().substring(r.getName().lastIndexOf('.') + 1).equals(suffix)) {
					files.add((IFile) r);
			}
		}
		return files;
	}
	
	private List<IFile> findFilename(IContainer container, String filename, List<IFile> files) throws CoreException {
		for (IResource r : container.members()) {
			if (r instanceof IContainer) {
				findFilename((IContainer) r, filename, files);
			} else if (r instanceof IFile 
					&& r.getName().substring(0,r.getName().lastIndexOf('.')).equals(filename)) {
				files.add((IFile) r);
			}
		}
		return files;
	}
}