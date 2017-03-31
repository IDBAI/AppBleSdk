package com.revenco.database.helper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 19:28.</p>
 * <p>CLASS DESCRIBE : 通过反射机制为对象属性赋值 </p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class AssignValueForAttributeHelper {
    /**
     * 正则表达式 用于匹配属性的第一个字母
     **/
    private static final String REGEX = "[a-zA-Z]";

    /**
     * 为泛型对象的属性赋值
     *
     * @param obj
     * @param attribute
     * @param value
     */
    public static void setAttrributeValue(Object obj, String attribute, Object value) {
        String method_name = convertToMethodName(attribute, obj.getClass(), true);
        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(method_name)) {
                Class[] parameterC = method.getParameterTypes();
                try {
                    if (parameterC[0] == int.class) {
                        method.invoke(obj, ((Integer) value).intValue());
                        break;
                    } else if (parameterC[0] == float.class) {
                        method.invoke(obj, ((Float) value).floatValue());
                        break;
                    } else if (parameterC[0] == double.class) {
                        method.invoke(obj, ((Double) value).doubleValue());
                        break;
                    } else if (parameterC[0] == byte.class) {
                        method.invoke(obj, ((Byte) value).byteValue());
                        break;
                    } else if (parameterC[0] == char.class) {
                        method.invoke(obj, ((Character) value).charValue());
                        break;
                    } else if (parameterC[0] == boolean.class) {
                        method.invoke(obj, ((Boolean) value).booleanValue());
                        break;
                    } else {
                        method.invoke(obj, parameterC[0].cast(value));
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将成员变量转换为get/set方法 boolean 转换为is方法
     *
     * @param attribute
     * @param objClass
     * @param isSet
     * @return
     */
    private static String convertToMethodName(String attribute, Class objClass, boolean isSet) {
        /** 通过正则表达式来匹配第一个字符 **/
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(attribute);
        StringBuilder sb = new StringBuilder();
        /** 如果是set方法名称 **/
        if (isSet) {
            sb.append("set");
        } else {
            /** get方法名称 **/
            try {
                Field attributeField = objClass.getDeclaredField(attribute);
                /** 如果类型为boolean **/
                if (attributeField.getType() == boolean.class || attributeField.getType() == Boolean.class) {
                    sb.append("is");
                } else {
                    sb.append("get");
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        /** 针对以下划线开头的属性 **/
        if (attribute.charAt(0) != '_' && m.find()) {
            sb.append(m.replaceFirst(m.group().toUpperCase()));
        } else {
            sb.append(attribute);
        }
        return sb.toString();
    }

    public static Object getAttrributeValue(Object obj, String attribute) {
        String methodName = convertToMethodName(attribute, obj.getClass(), false);
        Object value = null;
        try {
            /** 由于get方法没有参数且唯一，所以直接通过方法名称锁定方法 **/
            Method methods = obj.getClass().getDeclaredMethod(methodName);
            if (methods != null) {
                value = methods.invoke(obj);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return value;
    }
}
