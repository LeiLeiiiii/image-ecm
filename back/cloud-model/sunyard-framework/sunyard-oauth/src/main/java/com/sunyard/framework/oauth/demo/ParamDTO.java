package com.sunyard.framework.oauth.demo;

/**
 * @author P-JWei
 * @date 2023/7/20 14:17:42
 * @title 例子
 * @description
 */
public class ParamDTO {

    private String name;

    private String sex;

    private String phone;

    private String param;

    public String getParam(){
        return param;
    }

    public void setParam(String param){
        this.param = param;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
