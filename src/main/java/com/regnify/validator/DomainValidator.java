package com.regnify.validator;

import java.util.List;

import org.w3c.dom.Document;

import com.regnify.dto.response.ValidationMessage;

public class DomainValidator implements Validator {

	public void validate(Document doc, List<ValidationMessage> messages) {

		validate(doc, messages, "//cbc:DocumentCurrencyCode", "SAR");
		validate(doc, messages, "//cbc:TaxCurrencyCode", "SAR");
	}

	private void validate(Document doc, List<ValidationMessage> messages, String xpath, String expected) {
		String val = XmlUtil.getValue(doc, xpath);
		if (val != null && !val.equals(expected)) {
			messages.add(new ValidationMessage("INVALID_DOMAIN", "DOMAIN", "Invalid value", "ERROR", xpath));
		}
	}
}
