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
public class FormatValidator implements Validator {

    public void validate(Document doc, List<ValidationMessage> messages) {

        String date = XmlUtil.getValue(doc, "//cbc:IssueDate");
        if (date != null && !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            messages.add(new ValidationMessage("INVALID_DATE", "FORMAT", "Invalid date", "ERROR", "//cbc:IssueDate"));
        }

        String time = XmlUtil.getValue(doc, "//cbc:IssueTime");
        if (time != null && !time.matches("\\d{2}:\\d{2}:\\d{2}")) {
            messages.add(new ValidationMessage("INVALID_TIME", "FORMAT", "Invalid time", "ERROR", "//cbc:IssueTime"));
        }
    }
}
