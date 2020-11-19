package cn.it.test_risk_data.service;

import cn.it.test_risk_data.domain.RiskDataReqParam;
import cn.it.test_risk_data.domain.UserRiskDataResultBean;
import cn.it.test_risk_data.enums.RiskChannelDataTypeEnum;

public interface RiskDataService {

    //厂商数据类型枚举
    RiskChannelDataTypeEnum getServiceName();

    void callRiskData(RiskDataReqParam riskDataReqParam);

    void callRiskDataAsycResult(UserRiskDataResultBean userRiskDataResultBean);


}
