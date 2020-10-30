/*
 * @(#)ExternalAnswer.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.parser;

/**
 * ExternalAnswer.
 *
 * @author Werner Randelshofer
 * @version 1.0 28. November 2006 Created.
 */
public class ExternalAnswer extends Answer {
    private String externalReference;

    /**
     * Creates a new instance.
     */
    public ExternalAnswer() {
    }

    public boolean canBeInSameList(Answer that) {
        return false;
    }

    public String toString() {
        return getExternalReference().toString();
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String newValue) {
        this.externalReference = newValue;
    }
}
