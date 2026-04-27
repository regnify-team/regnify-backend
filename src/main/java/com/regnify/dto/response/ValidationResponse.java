/**
 * 
 */
package com.regnify.dto.response;

import java.util.List;

/**
 * 
 */
public class ValidationResponse {
	private String status;
	private List<ValidationMessage> messages;

	public ValidationResponse(String status, List<ValidationMessage> messages) {
		this.status = status;
		this.messages = messages;
	}

	public String getStatus() {
		return status;
	}

	public List<ValidationMessage> getMessages() {
		return messages;
	}
}