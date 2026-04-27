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
public interface Validator {
	void validate(Document doc, List<ValidationMessage> messages);
}
