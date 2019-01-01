/*
 * @(#)Exporter.java  1.1  2006-11-29
 *
 * Copyright (c) 2008 Werner Randelshofer
 * Staldenmattweg 2, CH-6405 Immensee, Switzerland
 * All rights reserved.
 *
 * The copyright of this software is owned by Werner Randelshofer. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Werner Randelshofer. For details see accompanying license terms.
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
