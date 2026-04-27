/**
 * 
 */
package com.regnify.dto.response;

/**
 * 
 */
public class ValidationMessage {

	private String code;
	private String category;
	private String message;
	private String status;
	private String ublPath;

	public ValidationMessage(String code, String category, String message, String status, String ublPath) {
		this.code = code;
		this.category = category;
		this.message = message;
		this.status = status;
		this.ublPath = ublPath;
	}

	public String getCode() {
		return code;
	}

	public String getCategory() {
		return category;
	}

	public String getMessage() {
		return message;
	}

	public String getStatus() {
		return status;
	}

	public String getUblPath() {
		return ublPath;
	}
}
