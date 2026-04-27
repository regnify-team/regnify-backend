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
public class StructureValidator implements Validator {

    public void validate(Document doc, List<ValidationMessage> messages) {

        String supplier = XmlUtil.getValue(doc,
                "//cac:AccountingSupplierParty/cac:Party/cbc:RegistrationName");

        if (supplier == null || supplier.isEmpty()) {
            messages.add(new ValidationMessage("STRUCTURE", "STRUCTURAL", "Supplier missing", "ERROR", "Supplier"));
        }
    }
}
