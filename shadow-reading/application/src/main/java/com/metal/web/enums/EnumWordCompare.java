package com.metal.web.enums;

/**
 * @program: shadow-reading
 * @description: compare word result
 * @author: metal
 * @create: 2025-01-16 10:29
 **/
public enum EnumWordCompare implements BaseEnums<Integer, String> {

    MATCH(0, "match success"),
    PARTIAL_MATCH(1, "partial match"),
    MIS_MATCHED(2, "match fail"),
    MISSED(3, "no this word reading"),
    EXTRA(4, "reading extra word");

    private final Integer code;
    private final String name;

    EnumWordCompare(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
