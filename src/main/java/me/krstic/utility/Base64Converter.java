package me.krstic.utility;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base64Converter {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(Base64Converter.class);
	
	public static String encodeBase64(String text) {
		return Base64.getEncoder().encodeToString(text.getBytes());
	}
	
	public static byte[] decodeBase64(byte[] encoded) {
		return Base64.getDecoder().decode(encoded);
	}
}
