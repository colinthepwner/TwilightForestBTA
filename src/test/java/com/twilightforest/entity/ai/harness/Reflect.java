package com.twilightforest.entity.ai.harness;

import java.lang.reflect.Field;

final class Reflect {

	private Reflect() {
	}

	static void set(Object target, String field, Object value) {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field f = type.getDeclaredField(field);
				f.setAccessible(true);
				f.set(target, value);
				return;
			} catch (NoSuchFieldException e) {

				type = type.getSuperclass();
			} catch (IllegalAccessException e) {
				throw new AssertionError("cannot write " + field + " on " + target.getClass(), e);
			}
		}
		throw new AssertionError("no field '" + field + "' anywhere on " + target.getClass());
	}
}
