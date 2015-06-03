package edu.slc.jsumplugin.files;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;

public class MyJavaElementChangeReporter implements IElementChangedListener {
	
    public void elementChanged(ElementChangedEvent event) {
       IJavaElementDelta delta= event.getDelta();
       if (delta != null) {
          System.out.println("delta received: ");
          System.out.print(delta);
          //traverseAndPrint(delta);
       }
    }
    
    void traverseAndPrint(IJavaElementDelta delta) {
        switch (delta.getKind()) {
            case IJavaElementDelta.ADDED:
                System.out.println(delta.getElement() + " was added");
                break;
            case IJavaElementDelta.REMOVED:
                System.out.println(delta.getElement() + " was removed");
                break;
            case IJavaElementDelta.CHANGED:
                System.out.println(delta.getElement() + " was changed");
                if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0) {
                    System.out.println("The change was in its children");
                }
                if ((delta.getFlags() & IJavaElementDelta.F_CONTENT) != 0) {
                    System.out.println("The change was in its content");
                }
                /* Others flags can also be checked */
                break;
        }
        IJavaElementDelta[] children = delta.getAffectedChildren();
        for (int i = 0; i < children.length; i++) {
            traverseAndPrint(children[i]);
        }
    }


 }