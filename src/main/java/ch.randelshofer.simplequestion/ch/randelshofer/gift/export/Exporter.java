/* @(#)Exporter.java
 * Copyright © Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.export;

import ch.randelshofer.gift.parser.*;
import java.io.*;
import ch.randelshofer.io.*;
import java.util.*;

/**
 * Exporter.
 *
 * @author Werner Randelshofer
 * @version 1.1 2006-11-29 Added baseDir parameter.
 * <br>1.0 2. August 2006 Created.
 */
public interface Exporter {
    /**
     * FIXME - Implement this 
     *
     * @param questions List of questions.
     */
    public void export(List<Question> questions, File f, ConfigurableFileFilter cff, File documentBase) throws IOException;
}
