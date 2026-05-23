package com.regnify.helper;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

public class UBLNamespaceContext implements NamespaceContext {
    public String getNamespaceURI(String prefix) {
        return switch (prefix) {
            case "invoice" -> "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
            case "cac" -> "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
            case "cbc" -> "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
            default -> null;
        };
    }
    public String getPrefix(String uri) { return null; }
    public Iterator<String> getPrefixes(String uri) { return null; }
}
