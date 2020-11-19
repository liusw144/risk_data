package cn.it.test_risk_data.enums;

/**
 * 数据类型枚举
 */
public enum RiskDataTypeEnum {

    BLACKLIST(1, "黑名单"),
    CREDITREPORT(2, "征信报告"),
    MOBILEAUTHENTICATION(3, "手机号码服务提供商认证"),
    SMSREPORT(4, "短信报告"),
    DECISIONSCORE(5, "决策分"),
    MULTILOAN(6, "多头查询"),
    SEARCHCOMPANY(7, "公司名称查询"),
    REVERSEGEOCODING(8, "经纬度转换");

    private int code;
    private String desc;

    RiskDataTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 根据code获取去value
     *
     * @param code
     * @return
     */
    public static String getNameByCode(Integer code) {
        if (code != null) {
            RiskDataTypeEnum[] values = RiskDataTypeEnum.values();
            if (values != null && values.length > 0) {
                for (RiskDataTypeEnum value : values) {
                    if (value.getCode() == code) {
                        return value.name();
                    }
                }
            }
            return null;
        }
        return null;

    }
}
