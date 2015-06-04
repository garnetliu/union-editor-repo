package edu.slc.jsumplugin.files;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import ast.Ast.*;

public class JavaFileSystem {

	static final String UNION_SUFFIX = "union";

	private IWorkspaceRoot root;
	private IProject project;
	private IJavaProject javaProject;
	private IFolder folder;
	private IPackageFragment fragment;
	private List<Unions> unions;
	
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
		javaProject = JavaCore.create(project);

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
		List<String> javaFilenames = new ArrayList<String>();
		for (Unions union : unions) {
			// convert content of union to java style and decide class name
			String className = "Union" + union.getName();
			javaFilenames.add(className + ".java");
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
		// report/clear extra java file
		clearExtra(javaFilenames);
	}
	
	private void clearExtra(List<String> classNames) throws CoreException, IOException {
		for (ICompilationUnit javaFile : findICompilationUnits(fragment)) {
			if (!classNames.contains(javaFile.getElementName())) {
				System.out.println("found additional java file: " + javaFile.getElementName());
				//javaFile.delete(true, null);
			}
		}
	}

	public void insertVariants(int unionsChoice, int unionChoice, int offset, IDocument document) throws BadLocationException, CoreException, IOException {
		unions = findAllUnions();
		FormatUnionVariants fv = new FormatUnionVariants(unions.get(unionsChoice), unionChoice);
		document.replace(offset, 0, fv.toString());
	}
	
	// helper methods-----------------------------------
	public List<Unions> findAllUnions() throws CoreException, IOException {// bin and src both as IContainer, so repetition happens if searching by project
		List<Unions> unions = new ArrayList<Unions>();
		List<IFile> files = new ArrayList<IFile>();
		files = findFileSuffix(folder, UNION_SUFFIX, files);
		
		if (files.size() != 0) {// there is at least one file with wanted affix
			for (IFile file : files) {
				InputStream content = file.getContents();
				// convert *.union contents to java style and decide class name
				ConvertUnion cu = new ConvertUnion(content);
				unions.add(cu.getUnion());
			}
		}
		return unions;
	}

	private List<ICompilationUnit> findAllICompilationUnits() throws CoreException {
		List<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
		try {
			IPackageFragmentRoot[] packageFragmentRoots = javaProject.getAllPackageFragmentRoots();
			for (int i = 0; i < packageFragmentRoots.length; i++) {
				IPackageFragmentRoot packageFragmentRoot = packageFragmentRoots[i];
				IJavaElement[] fragments = packageFragmentRoot.getChildren();
				for (int j = 0; j < fragments.length; j++) {
					IPackageFragment fragment = (IPackageFragment) fragments[j];
					IJavaElement[] javaElements = fragment.getChildren();
					for (int k = 0; k < javaElements.length; k++) {
						IJavaElement javaElement = javaElements[k];
						if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
							units.add((ICompilationUnit) javaElement);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return units;
	}

	private List<ICompilationUnit> findICompilationUnits(IPackageFragment fragment) throws CoreException {
		List<ICompilationUnit> units = new ArrayList<ICompilationUnit>();
		IJavaElement[] javaElements = fragment.getChildren();
		for (int k = 0; k < javaElements.length; k++) {
			IJavaElement javaElement = javaElements[k];
			if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
				units.add((ICompilationUnit) javaElement);
			}
		}
		return units;
	}
	
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