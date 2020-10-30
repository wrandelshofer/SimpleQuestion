/*
 * @(#)ILIASQuestionPoolExporterTest.java
 * Copyright © 2020 Werner Randelshofer, Switzerland. MIT License.
 */

package ch.randelshofer.gift.export.ilias;

import ch.randelshofer.gift.parser.GIFTParser;
import ch.randelshofer.gift.parser.Question;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ILIASQuestionPoolExporterTest {

    /**
     * This test is disabled, because the format used by ILIAS is not valid.
     */
    @Test
    void testExportQTI() throws Exception {
        List<Question> questions = loadExampleQuestions();
        ILIASQuestionPoolExporter instance = new ILIASQuestionPoolExporter();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        instance.computeQuestionOIds(questions);
        instance.exportQTI(questions, buf);

        InputSource inputSource = new InputSource(new ByteArrayInputStream(buf.toByteArray()));
        boolean valid = validateDtd(inputSource);
        assertTrue(valid, "the document is valid");
        try (OutputStream out = Files.newOutputStream(Paths.get(System.getProperty("user.home"), "exportqti.xml"))) {
            out.write(buf.toByteArray());
        }
    }

    private List<Question> loadExampleQuestions() throws IOException {
        String questions;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        ILIASQuestionPoolExporterTest.class.getResourceAsStream(
                                "/ch/randelshofer/simplequestion/examples.txt"),
                        StandardCharsets.UTF_8))) {

            questions = reader
                    .lines().collect(Collectors.joining("\n"));
        }
        return new GIFTParser().parse(questions);
    }


    /**
     * Returns true if the specified xml document validates against the DTD that it has declared in its header.
     *
     * @param source the input source for the xml document
     * @return true if valid
     * @throws ParserConfigurationException on parser configuration failure
     * @throws IOException                  on io failure
     * @throws SAXException                 on sax failure
     */
    private boolean validateDtd(InputSource source) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

        domFactory.setValidating(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        boolean[] valid = new boolean[]{true};
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void error(SAXParseException exception) throws SAXException {
                System.err.println("error: " + exception.getMessage() + "\n  line: " + exception.getLineNumber() + " column: " + exception.getColumnNumber());
                valid[0] = false;
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                error(exception);
            }

            @Override
            public void warning(SAXParseException exception) throws SAXException {
                error(exception);
            }
        });
        Document doc = builder.parse(source);
        return valid[0];
    }
}