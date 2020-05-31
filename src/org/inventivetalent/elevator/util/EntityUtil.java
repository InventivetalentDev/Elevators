/*
 *
 */

package org.inventivetalent.elevator.util;

import java.lang.reflect.Method;

public class EntityUtil {

	static Class<?> Entity;
	static Class<?> EntityMinecartAbstract;

	static Method setPositionRotation;

	static {
		Entity = Reflection.getNMSClass("Entity");
		EntityMinecartAbstract = Reflection.getNMSClass("EntityMinecartAbstract");

		try {
			setPositionRotation = Entity.getDeclaredMethod("setPositionRotation", double.class, double.class, double.class, float.class, float.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
