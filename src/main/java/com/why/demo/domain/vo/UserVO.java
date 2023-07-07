package com.why.demo.domain.vo;

import com.why.demo.common.annotation.Desensitization;
import com.why.demo.domain.enums.DesensitizationEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("用户信息")
public class UserVO {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("姓名")
    @Desensitization(desensitizationType = DesensitizationEnum.USERNAME)
    private String name;

    @ApiModelProperty("年龄")
    private Integer age;

    @ApiModelProperty("邮箱")
    @Desensitization(desensitizationType = DesensitizationEnum.EMAIL)
    private String email;

    @ApiModelProperty("金额")
    @Desensitization(desensitizationType = DesensitizationEnum.MONEY)
    private String money;

    @ApiModelProperty("身份证号")
    @Desensitization(desensitizationType = DesensitizationEnum.ID_CARD_NO)
    private String idNumber;

    @ApiModelProperty("手机号码")
    @Desensitization(desensitizationType = DesensitizationEnum.MOBILE_PHONE)
    private String mobilePhone;

    @ApiModelProperty("地址")
    @Desensitization(desensitizationType = DesensitizationEnum.ADDRESS)
    private String address;

    @ApiModelProperty("银行卡账号")
    @Desensitization(desensitizationType = DesensitizationEnum.BANK_ACCOUNT)
    private String bankAccount;

    @ApiModelProperty("密码")
    @Desensitization(desensitizationType = DesensitizationEnum.PASSWORD)
    private String password;

    @ApiModelProperty("秘钥")
    @Desensitization(desensitizationType = DesensitizationEnum.SECRET_KEY)
    private String secretKey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}