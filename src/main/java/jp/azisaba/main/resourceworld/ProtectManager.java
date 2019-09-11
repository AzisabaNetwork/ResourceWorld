package jp.azisaba.main.resourceworld;

public class ProtectManager {

	private static boolean protect = true;

	public static void set(boolean b) {
		protect = b;
	}

	public static boolean get() {
		return protect;
	}
}
