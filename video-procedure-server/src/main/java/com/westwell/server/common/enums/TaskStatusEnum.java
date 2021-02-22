package com.westwell.server.common.enums;


public enum TaskStatusEnum {

    DOING("doing", "处理中"),
    FAIL("fail", "失败"),
    SUCCESS("success", "成功");

    private String code;

    private String value;

    TaskStatusEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }




    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
