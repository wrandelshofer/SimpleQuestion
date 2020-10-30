/*
 * @(#)CAM.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.scorm.cam;

/**
 * CAM holds constants for the SCORM Content Aggregation Model (CAM).
 *
 * @author Werner Randelshofer
 * @version 1.0 October 10, 2006 Created.
 */
public class CAM {
    /**
     * The Namespace URI's for SCORM CAM XML Elements.
     */
    public final static String IMSCP_NS = "http://www.imsproject.org/xsd/imscp_rootv1p1p2";
    public final static String ADLCP_NS = "http://www.adlnet.org/xsd/adlcp_rootv1p2";
    public final static String IMSMD_NS = "http://www.imsglobal.org/xsd/imsmd_rootv1p2p1";

    /**
     * Prevent instance creation.
     */
    private CAM() {
    }


}
