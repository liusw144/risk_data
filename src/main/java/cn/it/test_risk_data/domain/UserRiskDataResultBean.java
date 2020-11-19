package cn.it.test_risk_data.domain;

import java.io.Serializable;
import java.util.Date;

public class UserRiskDataResultBean implements Serializable {
    /**
     * 数据id
     */
    private String id;

    private String terminalId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 数据厂商
     */
    private String channelName;

    /**
     * 数据类型
     */
    private String dataType;
    /**
     * 处理状况 0-未处理 1-已处理 2-处理中 3-不处理
     */
    private Integer status;
    /**
     * 回调信息
     */

    private String callInfo;

    /**
     * 是否收费 0-免费 1-付费
     */
    private Integer pricingStrategy;

    /**
     * 备注
     */
    private String remark;

    private Date updateTime;

    private Date createTime;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCallInfo() {
        return callInfo;
    }

    public void setCallInfo(String callInfo) {
        this.callInfo = callInfo;
    }

    public Integer getPricingStrategy() {
        return pricingStrategy;
    }

    public void setPricingStrategy(Integer pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}