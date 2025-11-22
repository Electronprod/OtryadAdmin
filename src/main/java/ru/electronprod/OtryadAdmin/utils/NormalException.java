package ru.electronprod.OtryadAdmin.utils;

/**
 * This exception is used, when it is needed to call error page without logging
 * it to console
 */
public class NormalException extends RuntimeException {
	static final long serialVersionUID = 1L;

	public NormalException(String message) {
		super(message);
	}
}
