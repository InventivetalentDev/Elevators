/*
 *
 */

package org.inventivetalent.elevator.util;

import java.lang.reflect.Method;

public class EntityUtil {

	public static void setEntitySize(Object entity, float f, float f1) {
		try {
			setSize.invoke(entity, f, f1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static Class<?> Entity;
	static Class<?> EntityMinecartAbstract;

	static Method setSize;
	static Method setPositionRotation;

	static {
		Entity = Reflection.getNMSClass("Entity");
		EntityMinecartAbstract = Reflection.getNMSClass("EntityMinecartAbstract");

		try {
			setSize = Entity.getDeclaredMethod("setSize", float.class, float.class);
			setPositionRotation = Entity.getDeclaredMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
