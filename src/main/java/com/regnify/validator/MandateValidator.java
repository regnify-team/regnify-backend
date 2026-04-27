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
public class MandateValidator implements Validator {

    @Override
    public void validate(Document doc, List<ValidationMessage> messages) {

        checkMandatory(doc, messages, "//cbc:ID");
        checkMandatory(doc, messages, "//cbc:IssueDate");
        checkMandatory(doc, messages, "//cbc:IssueTime");

        checkForbidden(doc, messages, "//cbc:UUID");

        // Conditional
        String type = XmlUtil.getValue(doc, "//cbc:InvoiceTypeCode");
        if ("388".equals(type)) {
            checkMandatory(doc, messages, "//cbc:Note");
        }
    }

    private void checkMandatory(Document doc, List<ValidationMessage> messages, String xpath) {
        String val = XmlUtil.getValue(doc, xpath);
        if (val == null || val.isEmpty()) {
            messages.add(new ValidationMessage("MISSING", "MANDATE", "Missing field", "ERROR", xpath));
        }
    }

    private void checkForbidden(Document doc, List<ValidationMessage> messages, String xpath) {
        String val = XmlUtil.getValue(doc, xpath);
        if (val != null && !val.isEmpty()) {
            messages.add(new ValidationMessage("FORBIDDEN", "MANDATE", "Field not allowed", "ERROR", xpath));
        }
    }
}