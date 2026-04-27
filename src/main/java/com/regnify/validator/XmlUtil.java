/**
 * 
 */
package com.regnify.validator;

import java.io.StringReader;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * 
 */
public class XmlUtil {

    /**
     * Parse XML with namespace awareness
     */
    public static Document parse(String xml) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        return factory
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
    }

    /**
     * Get single value using XPath
     */
    public static String getValue(Document doc, String expression) {
        try {
            XPath xpath = createXPath();
            return xpath.evaluate(expression, doc);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get node count
     */
    public static int getNodeCount(Document doc, String expression) {
        try {
            XPath xpath = createXPath();
            return ((org.w3c.dom.NodeList)
                    xpath.evaluate(expression, doc, XPathConstants.NODESET)).getLength();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Create XPath with namespace mapping
     */
    private static XPath createXPath() {
        XPath xpath = XPathFactory.newInstance().newXPath();

        xpath.setNamespaceContext(new NamespaceContext() {

            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "cbc":
                        return "cbc";
                    case "cac":
                        return "cac";
                    default:
                        return null;
                }
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                return null;
            }
        });

        return xpath;
    }
}
