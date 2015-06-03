package edu.slc.jsumplugin.editors;

import java.awt.Color;
import java.util.Scanner;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;



public class UnionConfiguration extends SourceViewerConfiguration {
	private ColorManager colorManager;

	public UnionConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}
	
	
	public IPresentationReconciler getPresentationReconciler(ISourceViewer viewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getTagScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		//NonRuleBased...
		
		return reconciler;
	}

	protected ITokenScanner getTagScanner() {
		ITokenScanner tagScanner = null;
		if (tagScanner == null) {
			tagScanner = new UnionRuleScanner(colorManager);
			((RuleBasedScanner) tagScanner).setDefaultReturnToken(new Token(
					new TextAttribute(null)));

		}
		return tagScanner;
	}

}
