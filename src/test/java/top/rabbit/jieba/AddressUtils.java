package top.rabbit.jieba;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

final class AddressUtils {

    private static Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static long decAddressOf(Object obj) {
        Object[] array = new Object[]{obj};

        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        int addressSize = unsafe.addressSize();
        long objectAddress;
        switch (addressSize) {
            case 4:
                objectAddress = unsafe.getInt(array, baseOffset);
                break;
            case 8:
                objectAddress = unsafe.getLong(array, baseOffset);
                break;
            default:
                throw new Error("unsupported address size: " + addressSize);
        }

        return objectAddress;
    }

    public static String hexAddressOf(Object obj) {
        return Long.toHexString(decAddressOf(obj));
    }

    public static void printDecAddress(Object obj) {
        System.out.println(decAddressOf(obj));
    }

    public static void printHexAddress(Object obj) {
        System.out.println(hexAddressOf(obj));
    }

    public static boolean isSameAddress(Object obj1, Object obj2) {
        return (decAddressOf(obj1) == decAddressOf(obj2));
    }
}
