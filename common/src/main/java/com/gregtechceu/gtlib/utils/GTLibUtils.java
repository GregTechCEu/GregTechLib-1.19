package com.gregtechceu.gtlib.utils;

import java.util.function.Consumer;

public class GTLibUtils {
	private GTLibUtils() {
		throw new UnsupportedOperationException("can't instantiate LdUtils");
	}

	public static <T> T make(T value, Consumer<T> operate) {
		operate.accept(value);
		return value;
	}
}
