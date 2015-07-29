package edu.slc.jsumplugin.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Set;

import main.ConvertUnion;
import format.*;
import edu.slc.jsumplugin.files.*;

import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

import ast.Ast.*;
import ast.CompareAst;
import ast.CompareAst.CompareUnions;
import ast.Type;

public class JavaFileSystem {

	static final String UNION_SUFFIX = "union";
	static final String UNION_UNITHEADER = "Union";

	private IWorkspaceRoot root;
	public IProject project;
	public IJavaProject javaProject;
	public IFolder srcFolder;
	public IPackageFragment mainFragment;
	public List<Unions> listOfUnions;	
	private List<Unions> previousUnions;
	private IFolder generatedFolder;
	private IPackageFragment generatedFragment;
	
	// Constructor with project name
	public JavaFileSystem(String projectName) throws CoreException, IOException {
		/*
		 * set up project
		 */
		this.root = ResourcesPlugin.getWorkspace().getRoot();
		this.project = root.getProject(projectName);
		this.srcFolder = project.getFolder("src");
		this.generatedFolder = project.getFolder("generated-sources");
		
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
				JavaCore.newSourceEntry(project.getFullPath().append("src")),
				JavaCore.newSourceEntry(project.getFullPath().append("generated-sources")),
				JavaRuntime.getDefaultJREContainerEntry() };

		javaProject.setRawClasspath(buildPath, project.getFullPath().append("bin"), null);

		// create folder by using resources package
		if (!srcFolder.exists()) { srcFolder.create(true, true, null); }
		if (!generatedFolder.exists()) { generatedFolder.create(true, true, null); }

		// Add folder to Java element
		IPackageFragmentRoot srcFolderRoot = javaProject.getPackageFragmentRoot(srcFolder);
		IPackageFragmentRoot generatedFolderRoot = javaProject.getPackageFragmentRoot(generatedFolder);
		
		// create package fragment
		this.mainFragment = srcFolderRoot.getPackageFragment("main");
		if (!mainFragment.exists()) { srcFolderRoot.createPackageFragment("main", true, null); }
		
		this.generatedFragment = generatedFolderRoot.getPackageFragment("generated");
		if (!generatedFragment.exists()) { generatedFolderRoot.createPackageFragment("generated", true, null); }
		
		this.previousUnions = null;
		this.listOfUnions = findAllUnions();
		
		// add resource change listener
		//JavaCore.addPreProcessingResourceChangedListener(new MyResourceChangeReporter());
	}


	/*
	 * create files-------------------------------------------------------------------------------------
	 */
	public void associateJavaFiles() throws CoreException, IOException {
		listOfUnions = findAllUnions();
		List<String> javaFilenames = new ArrayList<String>();
		int i = 0;
		swipeGeneratedPackage();
		for (Unions unions : listOfUnions) {
			if (unions.hasVisitors()) {
				List<String> traversalInterfaceClasses = new ArrayList<String>();
				createVisitors(unions, previousUnions.get(i), traversalInterfaceClasses);
			} else {
				createInstances(unions, previousUnions.get(i));
			}
			createUnionClass(unions, javaFilenames);
			i++;
		}
	}


	private void createInstances(Unions unions, Unions unions_beforeEdit) throws JavaModelException {
		for (Traversal t : unions.getTraversals()) {
			FormatUnionInstance fut = new FormatUnionInstance(unions, t);
			String className = fut.getClassName();
			// create java file
			ICompilationUnit iUnit = mainFragment.getCompilationUnit(className + ".java");
			if (iUnit.exists()) {
				//System.out.printf("file %s already exists...comparing edits\n", className + ".java");
				// replace the content of original buffer after comparing
				try {
					CompareUnions compareUnions = new CompareUnions(unions, unions_beforeEdit);
					JavaFileModification.modifyInstance(iUnit, t, compareUnions);
				} catch (IllegalArgumentException | MalformedTreeException | BadLocationException e) {
					e.printStackTrace();
				}
				mainFragment.createCompilationUnit(className + ".java", iUnit.getBuffer().getContents(), true, null);
			} else {
				Formatter packageHeading = new Formatter();
				packageHeading.format("package %s;\n", mainFragment.getElementName());
				packageHeading.format("import %s.%s.*;\n", generatedFragment.getElementName(), UNION_UNITHEADER + unions.getName());
				String classContent = packageHeading.toString() + fut.toString();
				packageHeading.close();
				mainFragment.createCompilationUnit(className + ".java", classContent, false, null);
			}
		}
	}

	private void createVisitors(Unions unions, Unions unions_beforeEdit, List<String> traversalInterfaceClasses) throws JavaModelException {
		for (Traversal t : unions.getTraversals()) {
				String union_name = t.getUnionArg(unions).toString();
				String interface_name = createVisitorInterface(unions, unions_beforeEdit, union_name, t, traversalInterfaceClasses);
				createVisitorInterpreter(unions, unions_beforeEdit, union_name, t, interface_name);
				createVisitorTraversalMethod(unions, unions_beforeEdit, union_name, t);
			}
		}
		
	private void createVisitorTraversalMethod(Unions unions, Unions unions_beforeEdit, String union_name, Traversal t) throws JavaModelException {
		FormatVisitorTraversalMethod fvtm = new FormatVisitorTraversalMethod(unions, t);
		String className = fvtm.getClassName();
		// create java file
		ICompilationUnit iUnit = mainFragment.getCompilationUnit(className+".java");
		if (iUnit.exists()) {
			//System.out.printf("file %s already exists...leaving the file as is\n", className + ".java");
//			System.out.printf("file %s already exists...comparing edits\n", className + ".java");
//			// replace the content of original buffer after comparing
//			try {
//				CompareUnions compareUnions = new CompareUnions(unions, unions_beforeEdit);
//				JavaFileModification.modifyVisitorTraversalMethod(union_name, iUnit, compareUnions, t);
//			} catch (MalformedTreeException | BadLocationException e) {
//				e.printStackTrace();
//			}
//			fragment.createCompilationUnit(className + ".java", iUnit.getBuffer().getContents(), true, null);
		} else {
			Formatter packageHeading = new Formatter();
			packageHeading.format("package %s;\n", mainFragment.getElementName());
			packageHeading.format("import %s.%s.*;\n", generatedFragment.getElementName(), UNION_UNITHEADER + unions.getName());
			String classContent = packageHeading.toString() + fvtm.toString();
			packageHeading.close();
			mainFragment.createCompilationUnit(className + ".java", classContent, false, null);
		}
		
	}

	private void createVisitorInterpreter(Unions unions, Unions unions_beforeEdit, String union_name, 
			Traversal t, String interface_name) throws JavaModelException {
		FormatVisitorFunction fvf = new FormatVisitorFunction(unions, union_name, t);
		String className = fvf.getClassName();
		// create java file
		ICompilationUnit iUnit = mainFragment.getCompilationUnit(className+".java");
		if (iUnit.exists()) {
			//System.out.printf("file %s already exists...comparing edits\n", className + ".java");
			// replace the content of original buffer after comparing
			try {
				CompareUnions compareUnions = new CompareUnions(unions, unions_beforeEdit);
				JavaFileModification.modifyVisitorInterpreter(union_name, iUnit, compareUnions, t);
			} catch (MalformedTreeException | BadLocationException e) {
				e.printStackTrace();
			}
			mainFragment.createCompilationUnit(className + ".java", iUnit.getBuffer().getContents(), true, null);
		} else {
			Formatter packageHeading = new Formatter();
			packageHeading.format("package %s;\n", mainFragment.getElementName());
			packageHeading.format("import %s.%s.*;\n", generatedFragment.getElementName(), UNION_UNITHEADER + unions.getName());
			packageHeading.format("import %s.%s;\n", generatedFragment.getElementName(), interface_name);
			String classContent = packageHeading.toString() + fvf.toString();
			packageHeading.close();
			mainFragment.createCompilationUnit(className + ".java", classContent, false, null);
		}
	}
		



	private String createVisitorInterface(Unions unions, Unions unions_beforeEdit, String union_name, 
			Traversal t, List<String> traversalInterfaceClasses) throws JavaModelException {
		FormatVisitorInterface fvi = new FormatVisitorInterface(unions, union_name, t);
		String className = fvi.getClassName();
		traversalInterfaceClasses.add(className);
		// create java file
		ICompilationUnit iUnit = generatedFragment.getCompilationUnit(className+".java");
		if (iUnit.exists()) {
			//System.out.printf("file %s already exists...comparing edits\n", className + ".java");
			// replace the content of original buffer after comparing
			try {
				CompareUnions compareUnions = new CompareUnions(unions, unions_beforeEdit);
				JavaFileModification.modifyVisitorInterface(union_name, iUnit, compareUnions, t);
			} catch (MalformedTreeException | BadLocationException e) {
				e.printStackTrace();
			}
			mainFragment.createCompilationUnit(className + ".java", iUnit.getBuffer().getContents(), true, null);
		} else {
			Formatter packageHeading = new Formatter();
			packageHeading.format("package %s;\n", generatedFragment.getElementName());
			//packageHeading.format("import %s.%s.*;\n", mainFragment.getElementName(), UNION_UNITHEADER + unions.getName());
			packageHeading.format("import %s.%s.*;\n", generatedFragment.getElementName(), UNION_UNITHEADER + unions.getName());
			String classContent = packageHeading.toString() + fvi.toString();
			packageHeading.close();
			generatedFragment.createCompilationUnit(className + ".java", classContent, false, null);
		}
		return className;
	}



//	// create union class with visitor interface imports
//	private void createUnionClass(Unions unions, List<String> javaFilenames, List<String> traversalInterfaceClasses) throws CoreException, IOException {
//		// convert content of union to java style and decide class name
//		String className = UNION_UNITHEADER + unions.getName();
//		javaFilenames.add(className + ".java");
//		FormatUnionClass fu = new FormatUnionClass(unions, className);
//		Formatter packageHeading = new Formatter();
//		//packageHeading.format("package %s;\n", mainFragment.getElementName());
//		packageHeading.format("package %s;\n", generatedFragment.getElementName());
//		for (String importMsg : traversalInterfaceClasses) {
//			packageHeading.format("import %s.%s;\n", mainFragment.getElementName(), importMsg);
//		}
//		String classContent = packageHeading.toString() + fu.toString();
//		packageHeading.close();
//		
//		// create java file
//		ICompilationUnit iUnit = generatedFragment.getCompilationUnit(className+".java");
//		if (iUnit.exists()) {
//			// replace the content of original buffer
//			generatedFragment.createCompilationUnit(className + ".java", classContent, true, null);
//		} else {
//			generatedFragment.createCompilationUnit(className + ".java", classContent, false, null);
//		}
//	}
	
	// create regular union class
	private void createUnionClass(Unions unions, List<String> javaFilenames) throws CoreException, IOException {
		// convert content of union to java style and decide class name
		String className = UNION_UNITHEADER + unions.getName();
		javaFilenames.add(className + ".java");
		FormatUnionClass fu = new FormatUnionClass(unions, className);
		Formatter packageHeading = new Formatter();
		//packageHeading.format("package %s;\n", mainFragment.getElementName());
		packageHeading.format("package %s;\n", generatedFragment.getElementName());
		//packageHeading.format("import %s.%s.*;\n", mainFragment.getElementName(), UNION_UNITHEADER + unions.getName());
		String classContent = packageHeading.toString() + fu.toString();
		packageHeading.close();
		
		// create java file
		ICompilationUnit iUnit = generatedFragment.getCompilationUnit(className+".java");
		if (iUnit.exists()) {
			// replace the content of original buffer
			generatedFragment.createCompilationUnit(className + ".java", classContent, true, null);
		} else {
			generatedFragment.createCompilationUnit(className + ".java", classContent, false, null);
		}
	}


	/*
	 * check files-------------------------------------------------------------------------------------
	 */
	

	private void swipeGeneratedPackage() throws JavaModelException {
		for (ICompilationUnit file : generatedFragment.getCompilationUnits()) {
			file.delete(true, null);
		}
	}
	
	
	public void checkUnionFiles() throws CoreException, IOException {
		listOfUnions = findAllUnions();
		int i = 0;
		for (Unions unions : listOfUnions) {
			CompareUnions compareUnions = new CompareUnions(unions, previousUnions.get(i));
			}
			i++;
		}

	
	/*
	 * helper methods-------------------------------------------------------------------------------------
	 */
	
	
	public List<Unions> findAllUnions() throws CoreException, IOException {// bin and src both as IContainer, so repetition happens if searching inside of project		
		if (listOfUnions != null) {
			previousUnions = new ArrayList<Unions>();
			previousUnions.addAll(listOfUnions);
		}
		
		List<Unions> unions = new ArrayList<Unions>();
		List<IFile> files = new ArrayList<IFile>();
		files = findFileSuffix(srcFolder, UNION_SUFFIX, files);
		
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
			IPackageFragmentRoot[] packageFragmentRoots = javaProject
					.getAllPackageFragmentRoots();
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