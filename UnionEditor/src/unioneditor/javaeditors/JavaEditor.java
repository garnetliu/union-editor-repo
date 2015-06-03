package unioneditor.javaeditors;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.JavaTextTools;

public class JavaEditor extends CompilationUnitEditor {
    public JavaEditor() {
	JavaTextTools jtx = JavaPlugin.getDefault().getJavaTextTools();
//    JavaSourceViewerConfiguration sourceViewerConfiguration = new JavaSourceViewerConfiguration(
//    		JavaTextTools.getColorManager(), JavaPlugin.getDefault().getCombinedPreferenceStore(), this, IJavaPartitions.JAVA_PARTITIONING);
//	setSourceViewerConfiguration(sourceViewerConfiguration);
    }
}
