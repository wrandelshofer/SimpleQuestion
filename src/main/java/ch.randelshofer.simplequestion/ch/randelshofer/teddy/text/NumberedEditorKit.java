/*
 * @(#)NumberedEditorKit.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.teddy.text;

import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

/**
 * NumberedEditorKit.
 * <p>
 * Usage:
 * <pre>
 * JEditorPane edit = new JEditorPane();
 * edit.setEditorKit(new NumberedEditorKit());
 * </pre>
 *
 * @author Werner Randelshofer
 * @version $Id: NumberedEditorKit.java 527 2009-06-07 14:28:19Z rawcoder $
 */
public class NumberedEditorKit extends StyledEditorKit {
    public final static long serialVersionUID = 1L;
    private NumberedViewFactory viewFactory;

    @Override
    public ViewFactory getViewFactory() {
        if (viewFactory == null) {
            viewFactory = new NumberedViewFactory();
        }
        return viewFactory;
    }
}
