/*
 * @(#)ILIASQuestionPoolExporter.java  1.4  2008-02-20
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
package ch.randelshofer.gift.export.ilias;

import ch.randelshofer.gift.export.Exporter;
import ch.randelshofer.gift.parser.*;
import ch.randelshofer.io.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import java.io.*;

import org.jhotdraw.util.ResourceBundleUtil;
import nanoxml.*;

/**
 * Exports a collection of <code>Question</code>'s to an ILIAS question pool.
 *
 * @author Werner Randelshofer
 */
public class ILIASQuestionPoolExporter implements Exporter {

    private String language = "de";
    private String author = System.getProperty("user.name");
    private int oidLen;
    private HashMap<Object, String> oidMap;
    private final static XMLElement dom = new XMLElement(null, false, false);
    private ResourceBundleUtil labels;
    private boolean isShuffleAnswers = false;

    /**
     * Creates a new instance.
     */
    public ILIASQuestionPoolExporter() {
        labels = new ResourceBundleUtil(ResourceBundle.getBundle("ch.randelshofer.gift.Labels"));
    }

    public void export(List<Question> questions, File file, ConfigurableFileFilter cff, File baseDir) throws IOException {
        computeQuestionOIds(questions);

        // Remove "_qpl.zip" ending from file name.
        String baseName = file.getName();
        if (baseName.toLowerCase().endsWith(".zip")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }
        if (baseName.toLowerCase().endsWith("_qpl")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }

        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(new File(file.getParentFile(), baseName + "_qpl.zip")))) {
            zout.putNextEntry(new ZipEntry(baseName + "_qpl/" + baseName + "_qpl.xml"));
            exportQPL(questions, baseName, zout);
            zout.closeEntry();
            zout.putNextEntry(new ZipEntry(baseName + "_qpl/" + baseName + "_qti.xml"));
            exportQTI(questions, zout);
            zout.closeEntry();
        }
    }

    /** This method is package private for unit tests. */
    void computeQuestionOIds(List<Question> questions) {
        oidMap = new HashMap<Object, String>();
        oidLen = (int) Math.log10(questions.size() + 1) + 1;
        for (Question q : questions) {
            getOid(q);
        }
    }

    private String getOidForTitle(Object o) {
        String oid = getOid(o);
        return oid.substring(1);
    }

    private String getOid(Object o) {
        if (oidMap.containsKey(o)) {
            return oidMap.get(o);
        } else {
            String oid = Integer.toString(oidMap.size() + 1);
            oid = "_" + (("0000000000" + oid).substring(10 + oid.length() - Math.max(oid.length(), oidLen)));
            oidMap.put(o, oid);
            return oid;
        }
    }

    private void exportQPL(List<Question> questions, String name, OutputStream out) throws IOException {
        XMLElement contentObject = dom.createElement("ContentObject");
        contentObject.setAttribute("Type", "Questionpool_Test");


        // MetaData
        // --------
        XMLElement metaData = dom.createElement("MetaData");
        XMLElement general = dom.createElement("General");
        general.setAttribute("Structure", "Hierarchical");
        XMLElement elem = dom.createElement("Identifier");
        elem.setAttribute("Entry", name);
        elem.setAttribute("Catalog", "ILIAS");
        general.addChild(elem);
        elem = dom.createElement("Title");
        elem.setAttribute("Language", language);
        elem.setContent(name);
        general.addChild(elem);
        elem = dom.createElement("Language");
        elem.setAttribute("Language", language);
        general.addChild(elem);
        elem = dom.createElement("Description");
        elem.setAttribute("Language", language);
        general.addChild(elem);
        elem = dom.createElement("Keyword");
        elem.setAttribute("Language", language);
        general.addChild(elem);
        metaData.addChild(general);
        contentObject.addChild(metaData);


        // Page Objects
        for (Question q : questions) {
            XMLElement pageObject = dom.createElement("PageObject");
            XMLElement pageContent = dom.createElement("PageContent");
            XMLElement question = dom.createElement("Question");
            question.setAttribute("QRef", oidMap.get(q));
            pageContent.addChild(question);
            pageObject.addChild(pageContent);
            contentObject.addChild(pageObject);
        }

        Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        w.write("<!DOCTYPE Test SYSTEM \"http://www.ilias.uni-koeln.de/download/dtd/ilias_co.dtd\">\n");
        contentObject.write(w);
        w.flush();
    }

    /** This method is package private for unit tests. */
    void exportQTI(List<Question> questions, OutputStream out) throws IOException {
        XMLElement questestinterop = dom.createElement("questestinterop");
        /*
        questestinterop.setAttribute("xmlns","http://www.imsglobal.org/xsd/ims_qtiasiv1p2");
        questestinterop.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
        questestinterop.setAttribute("xsi:schemaLocation","https://www.imsglobal.org/sites/default/files/xsd/ims_qtiasiv1p2p1.xsd");
*/
        // Items
        for (Question q : questions) {

            AnswerListType iliasType = getILIASType(q);
            if (iliasType == null) {
                throw new IOException("ILIASQuestionPoolExporter no ILIAS type for: " + q);
            } else {
                XMLElement item;
                switch (iliasType) {
                    case CLOZE:
                        item = createClozeItem(q);
                        break;
                    case MATCHING_PAIR:
                        item = createMatchingPairItem(q);
                        break;
                    case MULTIPLE_CHOICE:
                        item = createMultipleChoiceItem(q);
                        break;
                    case SINGLE_CHOICE:
                        item = createSingleChoiceItem(q);
                        break;
                    default:
                        throw new IOException("ILIASQuestionPoolExporter Illegal type " + iliasType + " for: " + q);
                }
                if (item == null) {
                    throw new IOException("ILIASQuestionPoolExporter ILIAS does not support the question.<br>" + q);
                } else {
                    questestinterop.addChild(item);
                }
            }
        }


        PrintWriter w = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        w.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        //w.println("<!DOCTYPE questestinterop SYSTEM \"ims_qtiasiv1p2p1.dtd\">");
        w.println("<!DOCTYPE questestinterop SYSTEM \"http://www.imsglobal.org/question/qtiv1p2p1/XMLSchemav1p2p1/xmla/ims_qtiasiv1p2p1schema/dtds/qtiasifulldtd/ims_qtiasiv1p2p1.dtd\">");

        // Due to a bug in ILIAS 3.8.3, the QTI file must not contain
        // any extraneous spaces and line breaks.
        questestinterop.write(w);
        w.flush();
    }

    private XMLElement createClozeItem(Question q) throws IOException {
        XMLElement item = dom.createElement("item");
        addCommonElementsToItem(q, item);

        // Item-Metadata
        XMLElement itemmetadata = dom.createElement("itemmetadata");
        XMLElement qtimetadata = dom.createElement("qtimetadata");
        XMLElement qtimetadatafield = dom.createElement("qtimetadatafield");
        XMLElement fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("ILIAS_VERSION");
        qtimetadatafield.addChild(fieldlabel);
        XMLElement fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent("3.8.3 2007-09-23");
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        qtimetadatafield = dom.createElement("qtimetadatafield");
        fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("QUESTIONTYPE");
        qtimetadatafield.addChild(fieldlabel);
        fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent("CLOZE QUESTION");
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        qtimetadatafield = dom.createElement("qtimetadatafield");
        fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("AUTHOR");
        qtimetadatafield.addChild(fieldlabel);
        fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent(author);
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);

        qtimetadatafield = dom.createElement("qtimetadatafield");
        fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("textgaprating");
        qtimetadatafield.addChild(fieldlabel);
        fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent("ci");
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);

        qtimetadatafield = dom.createElement("qtimetadatafield");
        fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("fixedTextLength");
        qtimetadatafield.addChild(fieldlabel);
        fieldentry = dom.createElement("fieldentry");
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);

        itemmetadata.addChild(qtimetadata);
        item.addChild(itemmetadata);

        // ------------
        // Presentation
        // ------------
        XMLElement presentation = dom.createElement("presentation");
        presentation.setAttribute("label", q.getDescriptiveTitle());
        XMLElement flow = dom.createElement("flow");

        // We need this to properly create the code for the result processing
        HashMap<BooleanAnswer, Object> wrongAnswerMap = new HashMap<>();

        for (Object o : q.getBody()) {
            if (o instanceof String) {
                String str = (String) o;
                XMLElement material = dom.createElement("material");
                XMLElement mattext = dom.createElement("mattext");
                mattext.setAttribute("texttype", "text/plain");
                mattext.setContent(encodeMattext(str));
                material.addChild(mattext);
                flow.addChild(material);

            } else if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;

                AnswerListType type = al.getType();
                if (type != null) {
                    switch (type) {
                        case BOOL: {
                            XMLElement response_str = dom.createElement("response_str");
                            response_str.setAttribute("ident", getOid(al));
                            response_str.setAttribute("rcardinality", "Single");

                            XMLElement render_choice = dom.createElement("render_choice");
                            // Never shuffle boolean answers
                            render_choice.setAttribute("shuffle", "No");

                            BooleanAnswer ba = (BooleanAnswer) al.answers().getFirst();
                            wrongAnswerMap.put(ba, new Object());

                            // Create choice for "TRUE" answer
                            XMLElement response_label = dom.createElement("response_label");
                            response_label.setAttribute("ident", "0");
                            XMLElement material = dom.createElement("material");
                            XMLElement mattext = dom.createElement("mattext");
                            mattext.setAttribute("texttype", "text/plain");
                            mattext.setContent(labels.getString("exporter.booleanAnswerTrue"));
                            material.addChild(mattext);
                            response_label.addChild(material);
                            render_choice.addChild(response_label);

                            // Create choice for "FALSE" answer
                            response_label = dom.createElement("response_label");
                            response_label.setAttribute("ident", "1");
                            material = dom.createElement("material");
                            mattext = dom.createElement("mattext");
                            mattext.setAttribute("texttype", "text/plain");
                            mattext.setContent(labels.getString("exporter.booleanAnswerFalse"));
                            material.addChild(mattext);
                            response_label.addChild(material);
                            render_choice.addChild(response_label);

                            response_str.addChild(render_choice);
                            flow.addChild(response_str);

                            break;
                        }
                        case CLOZE: {
                            XMLElement response_str = dom.createElement("response_str");
                            response_str.setAttribute("ident", getOid(al));
                            response_str.setAttribute("rcardinality", "Single");

                            XMLElement render_fib = dom.createElement("render_fib");
                            render_fib.setAttribute("fibtype", "String");
                            render_fib.setAttribute("prompt", "Box");

                            XMLElement response_label = dom.createElement("response_label");
                            response_label.setAttribute("ident", getOid(al.answers().getFirst()));

                            render_fib.addChild(response_label);

                            response_str.addChild(render_fib);
                            flow.addChild(response_str);

                            break;
                        }
                        case NUMERIC: {
                            if (al.answers().size() != 1) {
                                throw new IOException("ILIAS does not support multiple numeric answers in a cloze question.<br>" + q);
                            }

                            XMLElement response_num = dom.createElement("response_num");
                            response_num.setAttribute("ident", getOid(al));
                            response_num.setAttribute("numtype", "Decimal");
                            response_num.setAttribute("rcardinality", "Single");

                            XMLElement render_fib = dom.createElement("render_fib");
                            render_fib.setAttribute("fibtype", "Decimal");
                            render_fib.setAttribute("prompt", "Box");

                            Answer answer = al.answers().get(0);
                            if (answer instanceof IntervalAnswer) {
                                IntervalAnswer ia = (IntervalAnswer) answer;
                                render_fib.setAttribute("columns", Math.max(5, 1 + Math.max(Double.toString(ia.getMin()).length(), Double.toString(ia.getMax()).length())));
                                render_fib.setAttribute("minnumber", Double.toString(ia.getMin()));
                                render_fib.setAttribute("maxnumber", Double.toString(ia.getMax()));
                            } else if (answer instanceof NumberAnswer) {
                                NumberAnswer ia = (NumberAnswer) answer;
                                render_fib.setAttribute("columns", Math.max(5, 1 + Double.toString(ia.getNumber()).length()));
                                render_fib.setAttribute("minnumber", Double.toString(ia.getNumber() - Math.abs(ia.getErrorMargin())));
                                render_fib.setAttribute("maxnumber", Double.toString(ia.getNumber() + Math.abs(ia.getErrorMargin())));
                            } else {
                                throw new IOException("ILIAS does not support this numerical answer type in a cloze question.<br>" + q);
                            }

                            response_num.addChild(render_fib);
                            flow.addChild(response_num);

                            break;
                        }
                        case MATCHING_PAIR:
                            // Not supported
                            throw new IOException(labels.getFormatted("exporter.unsupportedMatchingPairInClozeText", q));
                            //break; not reached
                        case MULTIPLE_CHOICE:
                            // Not supported
                            throw new IOException("ILIAS does not support a multiple choice question with multiple answers in a cloze text.<br>" + q);
                            //break; not reached
                        case SINGLE_CHOICE: {
                            XMLElement response_str = dom.createElement("response_str");
                            response_str.setAttribute("ident", getOid(al));
                            response_str.setAttribute("rcardinality", "Single");

                            XMLElement render_choice = dom.createElement("render_choice");
                            render_choice.setAttribute("shuffle", isShuffleAnswers ? "Yes" : "No");

                            int index = 0;
                            for (Answer a : al.answers()) {
                                if (a instanceof ChoiceAnswer) {
                                    ChoiceAnswer ta = (ChoiceAnswer) a;

                                    XMLElement response_label = dom.createElement("response_label");
                                    //response_label.setAttribute("ident", getOid(ta));
                                    response_label.setAttribute("ident", Integer.toString(index));
                                    XMLElement material = dom.createElement("material");
                                    XMLElement mattext = dom.createElement("mattext");
                                    mattext.setAttribute("texttype", "text/plain");
                                    mattext.setContent(encodeMattext(ta.getText()));

                                    material.addChild(mattext);
                                    response_label.addChild(material);
                                    render_choice.addChild(response_label);
                                } else {
                                    throw new IOException("ILIAS does not support all multiple choice answer types in a cloze text.<br>" + q);
                                    //System.out.println("ILIASQuestionPoolExporter: Warning cloze question does not support all multiple choice answer types.");
                                }
                                index++;
                            }

                            response_str.addChild(render_choice);
                            flow.addChild(response_str);

                            break;
                        }
                        default:
                            throw new IOException("ILIAS does not support this cloze question.<br>" + q);
                    }
                }
            }
        }
        presentation.addChild(flow);
        item.addChild(presentation);

        // -----------------
        // Result processing
        // -----------------
        XMLElement resprocessing = dom.createElement("resprocessing");
        XMLElement outcomes = dom.createElement("outcomes");
        XMLElement decvar = dom.createElement("decvar");
        XMLElement itemfeedback = null;
        outcomes.addChild(decvar);
        resprocessing.addChild(outcomes);

        for (Object o : q.getBody()) {
            if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;

                int index = 0;
                for (Answer a : al.answers()) {
                    if (a instanceof ChoiceAnswer) {
                        ChoiceAnswer ta = (ChoiceAnswer) a;

                        XMLElement respcondition = dom.createElement("respcondition");
                        respcondition.setAttribute("continue", "Yes");
                        XMLElement conditionvar = dom.createElement("conditionvar");
                        XMLElement varequal = dom.createElement("varequal");
                        varequal.setAttribute("respident", getOid(al));
                        varequal.setContent(ta.getText());
                        conditionvar.addChild(varequal);
                        respcondition.addChild(conditionvar);
                        XMLElement setvar = dom.createElement("setvar");
                        setvar.setAttribute("action", "Add");
                        // Weight value is irrelevant. We always score 1 for
                        // correct answers and 0 for incorrect answers.
                        setvar.setContent((ta.isCorrect()) ? "1" : "0");
                        respcondition.addChild(setvar);
                        resprocessing.addChild(respcondition);

                    } else if (a instanceof NumberAnswer) {
                        NumberAnswer na = (NumberAnswer) a;

                        XMLElement respcondition = dom.createElement("respcondition");
                        respcondition.setAttribute("continue", "Yes");
                        XMLElement conditionvar = dom.createElement("conditionvar");
                        XMLElement varequal = dom.createElement("varequal");
                        varequal.setAttribute("respident", getOid(al));
                        varequal.setContent(na.getNumberAsString());
                        conditionvar.addChild(varequal);
                        respcondition.addChild(conditionvar);
                        XMLElement setvar = dom.createElement("setvar");
                        setvar.setAttribute("action", "Add");
                        // Weight value is irrelevant. We always score 1 for
                        // correct answers and 0 for incorrect answers.
                        setvar.setContent("1");
                        respcondition.addChild(setvar);

                        XMLElement displayfeedback = dom.createElement("displayfeedback");
                        displayfeedback.setAttribute("feedbacktype", "Response");
                        displayfeedback.setAttribute("linkrefid", getOid(a));
                        respcondition.addChild(displayfeedback);
                        resprocessing.addChild(respcondition);

                        itemfeedback = dom.createElement("itemfeedback");
                        itemfeedback.setAttribute("ident", getOid(a));
                        itemfeedback.setAttribute("view", "All");
                        XMLElement flow_mat = dom.createElement("flow_mat");
                        XMLElement material = dom.createElement("material");
                        XMLElement mattext = dom.createElement("mattext");
                        material.addChild(mattext);
                        flow_mat.addChild(material);
                        itemfeedback.addChild(flow_mat);

                    } else if (a instanceof IntervalAnswer) {
                        IntervalAnswer na = (IntervalAnswer) a;

                        XMLElement respcondition = dom.createElement("respcondition");
                        respcondition.setAttribute("continue", "Yes");
                        XMLElement conditionvar = dom.createElement("conditionvar");
                        XMLElement varequal = dom.createElement("varequal");
                        varequal.setAttribute("respident", getOid(al));
                        varequal.setContent(na.getMinAsString() + ".." + na.getMaxAsString());
                        conditionvar.addChild(varequal);
                        respcondition.addChild(conditionvar);
                        XMLElement setvar = dom.createElement("setvar");
                        setvar.setAttribute("action", "Add");
                        // Weight value is irrelevant. We always score 1 for
                        // correct answers and 0 for incorrect answers.
                        setvar.setContent("1");
                        respcondition.addChild(setvar);
                        resprocessing.addChild(respcondition);

                    } else if (a instanceof BooleanAnswer) {
                        BooleanAnswer ba = (BooleanAnswer) a;

                        // Create response condition for the TRUE answer 
                        XMLElement respcondition = dom.createElement("respcondition");
                        respcondition.setAttribute("continue", "Yes");
                        XMLElement conditionvar = dom.createElement("conditionvar");
                        XMLElement varequal = dom.createElement("varequal");
                        varequal.setAttribute("respident", getOid(al));
                        varequal.setContent(labels.getString("exporter.booleanAnswerTrue"));
                        conditionvar.addChild(varequal);
                        respcondition.addChild(conditionvar);
                        XMLElement setvar = dom.createElement("setvar");
                        setvar.setAttribute("action", "Add");
                        // Weight value is irrelevant. We always score 1.
                        //setvar.setContent(Integer.toString(ba.getWeight()));
                        setvar.setContent((ba.isTrue()) ? "1" : "0");
                        respcondition.addChild(setvar);
                        resprocessing.addChild(respcondition);

                        // Create response condition for the FALSE answer
                        respcondition = dom.createElement("respcondition");
                        respcondition.setAttribute("continue", "Yes");
                        conditionvar = dom.createElement("conditionvar");
                        varequal = dom.createElement("varequal");
                        varequal.setAttribute("respident", getOid(al));
                        //varequal.setContent(getOid(wrongAnswerMap.get(ba)));
                        varequal.setContent(labels.getString("exporter.booleanAnswerFalse"));
                        conditionvar.addChild(varequal);
                        respcondition.addChild(conditionvar);
                        setvar = dom.createElement("setvar");
                        setvar.setAttribute("action", "Add");
                        // Weight value is irrelevant. We always score 0.
                        //setvar.setContent(Integer.toString(100 - ba.getWeight()));
                        setvar.setContent((ba.isTrue()) ? "0" : "1");
                        respcondition.addChild(setvar);
                        resprocessing.addChild(respcondition);
                    } else {
                        throw new IOException("ILIAS does not support all answer types in cloze question.<br>" + q);
                    }
                    index++;
                }
            }

        }
        item.addChild(resprocessing);
        if (itemfeedback != null) {
            item.addChild(itemfeedback);
        }
        return item;
    }

    private void addCommonElementsToItem(Question q, XMLElement item) {
        item.setAttribute("ident", oidMap.get(q));

        String title = q.getDescriptiveTitle();
        item.setAttribute("title", title);

        XMLElement qticomment = dom.createElement("qticomment");
        qticomment.setContent(getOidForTitle(q));
        item.addChild(qticomment);

        XMLElement duration = dom.createElement("duration");
        duration.setContent("P0Y0M0DT0H1M0S");
        item.addChild(duration);

    }

    private XMLElement createMatchingPairItem(Question q) throws IOException {
        XMLElement item = dom.createElement("item");
        addCommonElementsToItem(q, item);

        // Item-Metadata
        XMLElement itemmetadata = dom.createElement("itemmetadata");
        XMLElement qtimetadata = dom.createElement("qtimetadata");
        XMLElement qtimetadatafield = dom.createElement("qtimetadatafield");
        XMLElement fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("ILIAS_VERSION");
        qtimetadatafield.addChild(fieldlabel);
        XMLElement fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent("3.4.5 2005-09-27");
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        qtimetadatafield = dom.createElement("qtimetadatafield");
        fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("QUESTIONTYPE");
        qtimetadatafield.addChild(fieldlabel);
        fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent("MATCHING QUESTION");
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        qtimetadatafield = dom.createElement("qtimetadatafield");
        fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("AUTHOR");
        qtimetadatafield.addChild(fieldlabel);
        fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent(author);
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        itemmetadata.addChild(qtimetadata);
        item.addChild(itemmetadata);

        // ------------
        // Presentation
        // ------------
        XMLElement presentation = dom.createElement("presentation");
        presentation.setAttribute("label", q.getDescriptiveTitle());
        XMLElement flow = dom.createElement("flow");

        // We need this to properly create the code for the result processing
        HashMap<BooleanAnswer, Object> wrongAnswerMap = new HashMap<>();

        for (Object o : q.getBody()) {
            if (o instanceof String) {
                XMLElement material = dom.createElement("material");
                XMLElement mattext = dom.createElement("mattext");
                mattext.setAttribute("texttype", "text/plain");
                mattext.setContent(encodeMattext((String) o));

                material.addChild(mattext);
                flow.addChild(material);
            } else if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;

                XMLElement response_grp = dom.createElement("response_grp");
                response_grp.setAttribute("ident", getOid(al));
                response_grp.setAttribute("rcardinality", "Multiple");

                XMLElement render_choice = dom.createElement("render_choice");
                render_choice.setAttribute("shuffle", isShuffleAnswers ? "Yes" : "No");

                StringBuilder matchGroup = new StringBuilder();
                for (Answer a : al.answers()) {
                    if (a instanceof MatchingPairAnswer) {
                        MatchingPairAnswer pa = (MatchingPairAnswer) a;
                        if (matchGroup.length() > 0) {
                            matchGroup.append(',');
                        }
                        matchGroup.append(getOid(pa.getValue()));
                    }
                }


                for (Answer a : al.answers()) {
                    if (a instanceof MatchingPairAnswer) {
                        MatchingPairAnswer pa = (MatchingPairAnswer) a;

                        XMLElement response_label = dom.createElement("response_label");
                        response_label.setAttribute("ident", getOid(pa));
                        response_label.setAttribute("match_max", "1");
                        response_label.setAttribute("match_group", matchGroup.toString());
                        XMLElement material = dom.createElement("material");
                        XMLElement mattext = dom.createElement("mattext");
                        mattext.setAttribute("texttype", "text/plain");
                        mattext.setContent(encodeMattext(pa.getValue()));

                        material.addChild(mattext);
                        response_label.addChild(material);
                        render_choice.addChild(response_label);

                        response_label = dom.createElement("response_label");
                        response_label.setAttribute("ident", getOid(pa.getValue()));
                        material = dom.createElement("material");
                        mattext = dom.createElement("mattext");
                        mattext.setAttribute("texttype", "text/plain");
                        mattext.setContent(encodeMattext(pa.getKey()));
                        material.addChild(mattext);
                        response_label.addChild(material);
                        render_choice.addChild(response_label);
                    } else {
                        throw new IOException("ILIASQuestionPoolExporter.createMatchingPairItem unsupported answer type " + a.getClass());
                    }
                }

                response_grp.addChild(render_choice);
                flow.addChild(response_grp);
            }
        }
        presentation.addChild(flow);
        item.addChild(presentation);

        // -----------------
        // Result processing
        // -----------------
        XMLElement resprocessing = dom.createElement("resprocessing");
        XMLElement outcomes = dom.createElement("outcomes");
        XMLElement decvar = dom.createElement("decvar");
        outcomes.addChild(decvar);
        resprocessing.addChild(outcomes);

        for (Object o : q.getBody()) {
            if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;

                for (Answer a : al.answers()) {
                    if (a instanceof MatchingPairAnswer) {
                        MatchingPairAnswer pa = (MatchingPairAnswer) a;

                        XMLElement respcondition = dom.createElement("respcondition");
                        respcondition.setAttribute("continue", "Yes");
                        XMLElement conditionvar = dom.createElement("conditionvar");
                        XMLElement varsubset = dom.createElement("varsubset");
                        varsubset.setAttribute("respident", getOid(al));
                        varsubset.setContent(getOid(pa) + "," + getOid(pa.getValue()));
                        conditionvar.addChild(varsubset);
                        respcondition.addChild(conditionvar);
                        XMLElement setvar = dom.createElement("setvar");
                        setvar.setAttribute("action", "Add");
                        // Weight value is irrelevant. We always score 1 for
                        // a matching pair.
                        setvar.setContent("1");
                        respcondition.addChild(setvar);
                        resprocessing.addChild(respcondition);
                    }
                }
            }
        }
        item.addChild(resprocessing);
        return item;
    }

    private XMLElement createMultipleChoiceItem(Question q) throws IOException {
        XMLElement item = dom.createElement("item");
        addCommonElementsToItem(q, item);

        // Item-Metadata
        XMLElement itemmetadata = dom.createElement("itemmetadata");
        XMLElement qtimetadata = dom.createElement("qtimetadata");
        XMLElement qtimetadatafield = dom.createElement("qtimetadatafield");
        XMLElement fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("ILIAS_VERSION");
        qtimetadatafield.addChild(fieldlabel);
        XMLElement fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent("3.4.5 2005-09-27");
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        qtimetadatafield = dom.createElement("qtimetadatafield");
        fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("QUESTIONTYPE");
        qtimetadatafield.addChild(fieldlabel);
        fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent("MULTIPLE CHOICE QUESTION");
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        qtimetadatafield = dom.createElement("qtimetadatafield");
        fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("AUTHOR");
        qtimetadatafield.addChild(fieldlabel);
        fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent(author);
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        itemmetadata.addChild(qtimetadata);
        item.addChild(itemmetadata);

        // ------------
        // Presentation
        // ------------
        XMLElement presentation = dom.createElement("presentation");
        presentation.setAttribute("label", q.getDescriptiveTitle());
        XMLElement flow = dom.createElement("flow");

        // We need this to properly create the code for the result processing
        HashMap<BooleanAnswer, Object> wrongAnswerMap = new HashMap<>();

        for (Object o : q.getBody()) {
            if (o instanceof String) {
                XMLElement material = dom.createElement("material");
                XMLElement mattext = dom.createElement("mattext");
                mattext.setAttribute("texttype", "text/plain");
                mattext.setContent(encodeMattext((String) o));
                material.addChild(mattext);
                flow.addChild(material);
            } else if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;

                XMLElement response_lid = dom.createElement("response_lid");
                //response_lid.setAttribute("ident", getOid(al));
                response_lid.setAttribute("ident", "MCMR");
                response_lid.setAttribute("rcardinality", "Multiple");

                XMLElement render_choice = dom.createElement("render_choice");
                render_choice.setAttribute("shuffle", isShuffleAnswers ? "Yes" : "No");

                int index = 0;
                for (Answer a : al.answers()) {
                    if (a instanceof ChoiceAnswer) {
                        ChoiceAnswer ta = (ChoiceAnswer) a;

                        XMLElement response_label = dom.createElement("response_label");
                        //response_label.setAttribute("ident", getOid(a));
                        response_label.setAttribute("ident", Integer.toString(index));
                        XMLElement material = dom.createElement("material");
                        XMLElement mattext = dom.createElement("mattext");
                        mattext.setAttribute("texttype", "text/plain");
                        mattext.setContent(encodeMattext(ta.getText()));
                        material.addChild(mattext);
                        response_label.addChild(material);
                        render_choice.addChild(response_label);
                    } else {
                        System.out.println("createMultiChoiceMultiAnswerItem unsupported answer type " + a.getClass());
                    }
                    index++;
                }
                response_lid.addChild(render_choice);
                flow.addChild(response_lid);
            }
        }
        presentation.addChild(flow);
        item.addChild(presentation);

        // -----------------
        // Result processing
        // -----------------
        XMLElement resprocessing = dom.createElement("resprocessing");
        XMLElement outcomes = dom.createElement("outcomes");
        XMLElement decvar = dom.createElement("decvar");
        outcomes.addChild(decvar);
        resprocessing.addChild(outcomes);

        for (Object o : q.getBody()) {
            if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;

                int index = 0;
                for (Answer a : al.answers()) {
                    if (a instanceof ChoiceAnswer) {
                        ChoiceAnswer ta = (ChoiceAnswer) a;

                        XMLElement respcondition = dom.createElement("respcondition");
                        respcondition.setAttribute("continue", "Yes");
                        XMLElement conditionvar = dom.createElement("conditionvar");
                        if (ta.isCorrect() || ta.getWeight() > 0) {
                            XMLElement varequal = dom.createElement("varequal");
                            //varequal.setAttribute("respident", getOid(al));
                            varequal.setAttribute("respident", "MCMR");
                            //varequal.setContent(getOid(ta));
                            varequal.setContent(Integer.toString(index));
                            conditionvar.addChild(varequal);
                        } else {
                            XMLElement not = dom.createElement("not");
                            XMLElement varequal = dom.createElement("varequal");
                            //varequal.setAttribute("respident", getOid(al));
                            varequal.setAttribute("respident", "MCMR");
                            //varequal.setContent(getOid(ta));
                            varequal.setContent(Integer.toString(index));
                            not.addChild(varequal);
                            conditionvar.addChild(not);
                        }
                        respcondition.addChild(conditionvar);
                        XMLElement setvar = dom.createElement("setvar");
                        setvar.setAttribute("action", "Add");
                        // Weight value is irrelevant. We always score 1 for
                        // an answer.
                        setvar.setContent("1");
                        respcondition.addChild(setvar);

                        XMLElement displayfeedback = dom.createElement("displayfeedback");
                        displayfeedback.setAttribute("feedbacktype", "Response");
                        displayfeedback.setAttribute("linkrefid", "True_" + index);
                        respcondition.addChild(displayfeedback);

                        resprocessing.addChild(respcondition);

                    }
                    index++;
                }
            }
        }
        item.addChild(resprocessing);

        // ----------------
        // Feedback
        // ----------------
        for (Object o : q.getBody()) {
            if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;

                int index = 0;
                for (Answer a : al.answers()) {
                    if (a instanceof ChoiceAnswer) {
                        ChoiceAnswer ta = (ChoiceAnswer) a;

                        XMLElement itemfeedback = dom.createElement("itemfeedback");
                        itemfeedback.setAttribute("ident", "True_" + index);
                        itemfeedback.setAttribute("view", "All");

                        XMLElement flow_mat = dom.createElement("flow_mat");
                        XMLElement material = dom.createElement("material");
                        XMLElement mattext = dom.createElement("mattext");
                        mattext.setAttribute("texttype", "text/plain");
                        // ILIAS 3.4.x does not support feedback, that's why
                        // we leave mattext empty.
                        material.addChild(mattext);
                        flow_mat.addChild(material);
                        itemfeedback.addChild(flow_mat);

                        item.addChild(itemfeedback);

                    } else if (a instanceof BooleanAnswer) {
                        BooleanAnswer ba = (BooleanAnswer) a;

                        // Create feedback for the correct answer
                        XMLElement itemfeedback = dom.createElement("itemfeedback");
                        itemfeedback.setAttribute("ident", "True_" + (index * 2));
                        itemfeedback.setAttribute("view", "All");

                        XMLElement flow_mat = dom.createElement("flow_mat");
                        XMLElement material = dom.createElement("material");
                        XMLElement mattext = dom.createElement("mattext");
                        mattext.setAttribute("texttype", "text/plain");
                        // ILIAS 3.4.x does not support feedback, that's why
                        // we leave mattext empty.
                        material.addChild(mattext);
                        flow_mat.addChild(material);
                        itemfeedback.addChild(flow_mat);

                        item.addChild(itemfeedback);

                        // Create feedback for the incorrect answer
                        itemfeedback = dom.createElement("itemfeedback");
                        itemfeedback.setAttribute("ident", "True_" + (index * 2 + 1));
                        itemfeedback.setAttribute("view", "All");

                        flow_mat = dom.createElement("flow_mat");
                        material = dom.createElement("material");
                        mattext = dom.createElement("mattext");
                        mattext.setAttribute("texttype", "text/plain");
                        // ILIAS 3.4.x does not support feedback, that's why
                        // we leave mattext empty.
                        material.addChild(mattext);
                        flow_mat.addChild(material);
                        itemfeedback.addChild(flow_mat);

                        item.addChild(itemfeedback);
                    }
                    index++;
                }
            }
        }
        item.addChild(resprocessing);


        return item;
    }

    private XMLElement createSingleChoiceItem(Question q) {
        XMLElement item = dom.createElement("item");
        addCommonElementsToItem(q, item);

        // Item-Metadata
        XMLElement itemmetadata = dom.createElement("itemmetadata");
        XMLElement qtimetadata = dom.createElement("qtimetadata");
        XMLElement qtimetadatafield = dom.createElement("qtimetadatafield");
        XMLElement fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("ILIAS_VERSION");
        qtimetadatafield.addChild(fieldlabel);
        XMLElement fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent("3.4.5 2005-09-27");
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        qtimetadatafield = dom.createElement("qtimetadatafield");
        fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("QUESTIONTYPE");
        qtimetadatafield.addChild(fieldlabel);
        fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent("MULTIPLE CHOICE QUESTION");
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        qtimetadatafield = dom.createElement("qtimetadatafield");
        fieldlabel = dom.createElement("fieldlabel");
        fieldlabel.setContent("AUTHOR");
        qtimetadatafield.addChild(fieldlabel);
        fieldentry = dom.createElement("fieldentry");
        fieldentry.setContent(author);
        qtimetadatafield.addChild(fieldentry);
        qtimetadata.addChild(qtimetadatafield);
        itemmetadata.addChild(qtimetadata);
        item.addChild(itemmetadata);

        // ------------
        // Presentation
        // ------------
        XMLElement presentation = dom.createElement("presentation");
        presentation.setAttribute("label", q.getDescriptiveTitle());
        XMLElement flow = dom.createElement("flow");

        // We need this to properly create the code for the result processing
        HashMap<BooleanAnswer, Object> wrongAnswerMap = new HashMap<>();

        for (Object o : q.getBody()) {
            if (o instanceof String) {
                XMLElement material = dom.createElement("material");
                XMLElement mattext = dom.createElement("mattext");
                mattext.setAttribute("texttype", "text/plain");
                mattext.setContent(encodeMattext((String) o));
                material.addChild(mattext);
                flow.addChild(material);
            } else if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;

                XMLElement response_lid = dom.createElement("response_lid");
                // The magical identifier "MCSR" is used to determine whether
                // we are facing a Multiple Choice Single Response question or
                // a Multiple Choice Multiple Response question
                //response_lid.setAttribute("ident", getOid(al));
                response_lid.setAttribute("ident", "MCSR"); // Magical identifier!

                response_lid.setAttribute("rcardinality", "Single");

                XMLElement render_choice = dom.createElement("render_choice");
                render_choice.setAttribute("shuffle", (isShuffleAnswers && al.getType() != AnswerListType.BOOL) ? "Yes" : "No");
                int index = 0;
                for (Answer a : al.answers()) {
                    if (a instanceof ChoiceAnswer) {
                        ChoiceAnswer ta = (ChoiceAnswer) a;

                        XMLElement response_label = dom.createElement("response_label");

                        // This identifier is used as an index by ILIAS
                        //response_label.setAttribute("ident", getOid(a));
                        response_label.setAttribute("ident", Integer.toString(index));
                        XMLElement material = dom.createElement("material");
                        XMLElement mattext = dom.createElement("mattext");
                        mattext.setAttribute("texttype", "text/plain");
                        mattext.setContent(encodeMattext(ta.getText()));
                        material.addChild(mattext);
                        response_label.addChild(material);
                        render_choice.addChild(response_label);

                    } else if (a instanceof BooleanAnswer) {
                        BooleanAnswer ba = (BooleanAnswer) a;
                        wrongAnswerMap.put(ba, new Object());

                        // Create choice for "TRUE" answer
                        XMLElement response_label = dom.createElement("response_label");
                        // This identifier is used as an index by ILIAS
                        //response_label.setAttribute("ident", getOid((ba.isTrue()) ? ba : wrongAnswerMap.get(ba)));
                        response_label.setAttribute("ident", Integer.toString(index));
                        XMLElement material = dom.createElement("material");
                        XMLElement mattext = dom.createElement("mattext");
                        mattext.setAttribute("texttype", "text/plain");
                        mattext.setContent(labels.getString("exporter.booleanAnswerTrue"));
                        material.addChild(mattext);
                        response_label.addChild(material);
                        render_choice.addChild(response_label);

                        index++;

                        // Create choice for "FALSE" answer
                        response_label = dom.createElement("response_label");
                        // This identifier is used as an index by ILIAS
                        //response_label.setAttribute("ident", getOid((ba.isTrue()) ? wrongAnswerMap.get(ba) : ba));
                        response_label.setAttribute("ident", Integer.toString(index));
                        material = dom.createElement("material");
                        mattext = dom.createElement("mattext");
                        mattext.setAttribute("texttype", "text/plain");
                        mattext.setContent(labels.getString("exporter.booleanAnswerFalse"));
                        material.addChild(mattext);
                        response_label.addChild(material);
                        render_choice.addChild(response_label);

                    } else {
                        System.out.println("createMultiChoiceSingleAnswerItem unsupported answer type " + a.getClass());

                    }
                    index++;
                }
                response_lid.addChild(render_choice);
                flow.addChild(response_lid);
            }
        }
        presentation.addChild(flow);
        item.addChild(presentation);

        // -----------------
        // Result processing
        // -----------------
        XMLElement resprocessing = dom.createElement("resprocessing");
        XMLElement outcomes = dom.createElement("outcomes");
        XMLElement decvar = dom.createElement("decvar");
        outcomes.addChild(decvar);
        resprocessing.addChild(outcomes);

        for (Object o : q.getBody()) {
            if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;

                int index = 0;
                for (Answer a : al.answers()) {
                    if (a instanceof ChoiceAnswer) {
                        ChoiceAnswer ta = (ChoiceAnswer) a;

                        XMLElement respcondition = dom.createElement("respcondition");
                        respcondition.setAttribute("continue", "Yes");
                        XMLElement conditionvar = dom.createElement("conditionvar");
                        XMLElement varequal = dom.createElement("varequal");
                        // Magical identifier!
                        //varequal.setAttribute("respident", getOid(al));
                        varequal.setAttribute("respident", "MCSR");
                        // Magical index!
                        //varequal.setContent(getOid(ta));
                        varequal.setContent(Integer.toString(index));
                        conditionvar.addChild(varequal);
                        respcondition.addChild(conditionvar);
                        XMLElement setvar = dom.createElement("setvar");
                        setvar.setAttribute("action", "Add");
                        // Weight value is irrelevant. We always score 1 for
                        // correct answers and 0 for incorrect answers.
                        setvar.setContent((ta.isCorrect() || ta.getWeight() > 0) ? "1" : "0");
                        respcondition.addChild(setvar);
                        resprocessing.addChild(respcondition);

                    } else if (a instanceof BooleanAnswer) {
                        BooleanAnswer ba = (BooleanAnswer) a;

                        // Create response condition for the correct answer
                        XMLElement respcondition = dom.createElement("respcondition");
                        respcondition.setAttribute("continue", "Yes");
                        XMLElement conditionvar = dom.createElement("conditionvar");
                        XMLElement varequal = dom.createElement("varequal");
                        // Magical identifier!
                        //varequal.setAttribute("respident", getOid(al));
                        varequal.setAttribute("respident", "MCSR");
                        // Magical index!
                        //varequal.setContent(getOid(ba));
                        varequal.setContent(Integer.toString(ba.isTrue() ? 0 : 1));
                        conditionvar.addChild(varequal);
                        respcondition.addChild(conditionvar);
                        XMLElement setvar = dom.createElement("setvar");
                        setvar.setAttribute("action", "Add");
                        // Weight value is irrelevant. We always score 1.
                        //setvar.setContent(Integer.toString(ba.getWeight()));
                        setvar.setContent("1");
                        respcondition.addChild(setvar);
                        resprocessing.addChild(respcondition);

                        // Create response condition for the wrong answer
                        respcondition = dom.createElement("respcondition");
                        respcondition.setAttribute("continue", "Yes");
                        conditionvar = dom.createElement("conditionvar");
                        varequal = dom.createElement("varequal");
                        // Magical index!
                        //varequal.setAttribute("respident", getOid(al));
                        varequal.setAttribute("respident", "MCSR");
                        // Magical index!
                        //varequal.setContent(getOid(wrongAnswerMap.get(ba)));
                        varequal.setContent(Integer.toString(ba.isTrue() ? 1 : 0));
                        conditionvar.addChild(varequal);
                        respcondition.addChild(conditionvar);
                        setvar = dom.createElement("setvar");
                        setvar.setAttribute("action", "Add");
                        // Weight value is irrelevant. We always score 0.
                        //setvar.setContent(Integer.toString(100 - ba.getWeight()));
                        setvar.setContent("0");
                        respcondition.addChild(setvar);
                        resprocessing.addChild(respcondition);
                    }

                    index++;
                }
            }
        }
        item.addChild(resprocessing);
        return item;
    }

    /**
     * Returns the question for use with ILIAS.
     * Returns null, if there is no suitable question type for ILIAS.
     */
    public AnswerListType getILIASType(Question q) {
        LinkedList<Object> body = q.getBody();

        // The body must consist of at least two elements
        if (body.size() < 2) {
            return null;
        }

        // If the body consists of exactly two elements, and the first
        // element is text, we can create the corresponding question type in
        // ILIAS in most cases.
        if (body.size() == 2 &&
                (body.getFirst() instanceof String) &&
                (body.getLast() instanceof AnswerList)) {
            AnswerList al = (AnswerList) body.getLast();
            switch (al.getType()) {
                case BOOL:
                    return AnswerListType.SINGLE_CHOICE;
                case CLOZE:
                    return AnswerListType.CLOZE;
                case NUMERIC:
                    return AnswerListType.CLOZE;
                case MATCHING_PAIR:
                    return AnswerListType.MATCHING_PAIR;
                case MULTIPLE_CHOICE:
                    return AnswerListType.MULTIPLE_CHOICE;
                case SINGLE_CHOICE:
                    return AnswerListType.SINGLE_CHOICE;
                default:
                    return null;
            }
        }

        // If the answer list is not at the end of the body, or if the
        // body contains multiple answer lists, in most cases, we can
        // create the CLOZE type in ILIAS
        AnswerListType type = null;
        for (Object o : body) {
            if (o instanceof AnswerList) {
                AnswerList al = (AnswerList) o;
                switch (al.getType()) {
                    case BOOL:
                        if (type == null || type == AnswerListType.CLOZE) {
                            type = AnswerListType.CLOZE;
                        } else {
                            return null;
                        }
                        break;
                    case CLOZE:
                    case NUMERIC:
                        if (type == null || type == AnswerListType.CLOZE) {
                            type = AnswerListType.CLOZE;
                        } else {
                            return null;
                        }
                        break;
                    case MATCHING_PAIR:
                        if (type == null) {
                            type = AnswerListType.MATCHING_PAIR;
                        } else {
                            return null;
                        }
                        break;
                    case MULTIPLE_CHOICE:
                        if (type == null || type == AnswerListType.CLOZE) {
                            type = AnswerListType.CLOZE;
                        } else {
                            return null;
                        }
                        break;
                    case SINGLE_CHOICE:
                        if (type == null || type == AnswerListType.CLOZE) {
                            type = AnswerListType.CLOZE;
                        } else {
                            return null;
                        }
                        break;
                    default:
                        return null;
                }
            }
        }
        return type;
    }

    private String encodeMattext(String str) {
        return str;
    /*
    StringBuilder buf = new StringBuilder();
    StringTokenizer tt = new StringTokenizer(str, "\n", true);
    while (tt.hasMoreTokens()) {
    String token = tt.nextToken();
    if (token.equals(".")) {
    // skip full stops
    } else {
    buf.append(token);
    }
    }
    return buf.toString();
     */
    }
}
