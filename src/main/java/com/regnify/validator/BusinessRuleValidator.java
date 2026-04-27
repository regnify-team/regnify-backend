/**
 * 
 */
package com.regnify.validator;

import java.util.List;

import org.w3c.dom.Document;

import com.regnify.dto.response.ValidationMessage;

/**
 * 
 */
public class BusinessRuleValidator implements Validator {

    public void validate(Document doc, List<ValidationMessage> messages) {

        int lines = XmlUtil.getNodeCount(doc, "//cac:InvoiceLine");

        if (lines == 0) {
            messages.add(new ValidationMessage("BR-16", "BUSINESS", "No invoice lines", "ERROR", "//cac:InvoiceLine"));
        }
    }
}
