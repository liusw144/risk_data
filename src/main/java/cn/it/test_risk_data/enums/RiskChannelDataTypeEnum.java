package cn.it.test_risk_data.enums;

/**
 * 数据厂商的数据类型枚举
 */
public enum RiskChannelDataTypeEnum {

    ADVANCE_BLACKLIST(1, "Advance黑名单"),
    ADVANCE_CREDITREPORT(2, "Advance征信"),
    PEAKSECURITIES_MOBILEAUTHENTICATION(3, "摩比神奇手机号码服务提供商认证"),
    PEAKSECURITIES_BLACKLIST(4, "摩比神奇黑名单"),
    ZHUXU_SMSREPORT(5, "主序短信报告"),
    SHUIXIANG_BLACKLIST(6, "水象黑名单"),
    SHUIXIANG_DECISIONSCORE(7, "水象决策分"),
    RISKCLOUD_MULTILOAN(8, "闪云金科多头查询"),
    RISKCLOUD_SEARCHCOMPANY(9, "闪云金科公司查询"),
    RISKCLOUD_REVERSEGEOCODING(10, "闪云金科经纬度转换"),
    YUSHIFINTECH_BLACKLIST(11, "与时科技黑名单");

    private Integer code;
    private String desc;

    RiskChannelDataTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
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
            RiskChannelDataTypeEnum[] values = RiskChannelDataTypeEnum.values();
            if (values != null && values.length > 0) {
                for (RiskChannelDataTypeEnum value : values) {
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
