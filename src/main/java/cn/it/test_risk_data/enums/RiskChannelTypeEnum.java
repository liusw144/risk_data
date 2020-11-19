package cn.it.test_risk_data.enums;

/**
 * 数据厂商枚举
 */
public enum RiskChannelTypeEnum {

    ADVANCE(1, "Advance"), //Advance厂商
    PEAKSECURITIES(2, "摩比神奇"), //摩比神奇厂商
    ZHUXU(3, "主序科技"), //主序科技厂商
    SHUIXIANG(4, "水象云"), //水象云厂商
    RISKCLOUD(5, "闪云金科"), //闪云金科厂商
    YUSHIFINTECH(6, "与时科技");//与时科技

    private int code;
    private String desc;

    RiskChannelTypeEnum(int code, String desc) {
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
            RiskChannelTypeEnum[] values = RiskChannelTypeEnum.values();
            if (values != null && values.length > 0) {
                for (RiskChannelTypeEnum value : values) {
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
