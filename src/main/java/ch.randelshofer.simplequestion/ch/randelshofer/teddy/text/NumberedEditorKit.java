/* @(#)NumberedEditorKit.java
 *
 * Copyright (c) 2005 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and
 * contributors of the JHotDraw project ("the copyright holders").
 * You may not use, copy or modify this software, except in
 * accordance with the license agreement you entered into with
 * the copyright holders. For details see accompanying license terms.
 *
 * Original version (c) Stanislav Lapitsky
 * http://www.developer.com/java/other/article.php/3318421
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
