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
public class CardinalityValidator implements Validator {

    public void validate(Document doc, List<ValidationMessage> messages) {

        int count = XmlUtil.getNodeCount(doc, "/Invoice/cbc:ID");
        if (count != 1) {
            messages.add(new ValidationMessage("CARDINALITY", "STRUCTURAL", "Must appear once", "ERROR", "/Invoice/cbc:ID"));
        }
    }
}
