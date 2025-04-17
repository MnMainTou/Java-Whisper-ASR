package com.metal.web.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import cn.hutool.core.util.StrUtil;

/***
 * 枚举接口类 枚举实现该接口 以达到统一配置能力
 *
 * @param <K> key
 * @param <V> value
 */
public interface BaseEnums<K, V> {

    /***
     * code
     *
     * @return
     */
    K getCode();

    /***
     * name
     *
     * @return
     */
    V getName();

    /***
     * 获取实例
     *
     * @param code
     * @return
     */
    static <Z> Z getInstance(Object code, Class<? extends BaseEnums> tClass) {

        if (tClass == null) {
            return null;
        }
        if (tClass.isEnum()) {
            for (BaseEnums enumConstant : tClass.getEnumConstants()) {
                if (enumConstant.getCode().equals(code)) {

                    return (Z)enumConstant;
                }
            }
        } else {

        }
        return null;
    }

    /***
     * 获取枚举列表
     *
     * @param clazz
     * @return
     */
    static List<Map<String, Object>> getEnumList(Class<? extends Enum<? extends BaseEnums>> clazz) {

        final Enum<? extends BaseEnums>[] enums = clazz.getEnumConstants();
        if (null == enums) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> list = new ArrayList<>(enums.length);
        for (Enum<? extends BaseEnums> e : enums) {

            BaseEnums baseEnums = (BaseEnums)e;
            Map<String, Object> map = new HashMap<>();
            map.put("code", baseEnums.getCode());
            map.put("name", baseEnums.getName());
            map.put("stringValue", baseEnums.getName());
            map.put("enName", StrUtil.toCamelCase(e.name().toLowerCase()));
            list.add(map);
        }
        return list;
    }

    /**
     * 根据属性值获取枚举值
     *
     * @param tClass T implements BaseEnums
     * @param predicate predicate
     * @return Optional
     */
    static <T> Optional<T> getInstance(Class<T> tClass, Predicate<T> predicate) {
        return Arrays.stream(tClass.getEnumConstants()).filter(predicate).findFirst();
    }

    /**
     * 根据属性值获取枚举值
     *
     * @param tClass T implements BaseEnums
     * @param predicate predicate
     * @return Optional
     */
    static <T> T getEnum(Class<T> tClass, Predicate<T> predicate) {
        return getInstance(tClass, predicate).orElse(null);
    }

}

