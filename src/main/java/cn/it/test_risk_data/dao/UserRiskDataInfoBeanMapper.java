package cn.it.test_risk_data.dao;

import cn.it.test_risk_data.domain.UserRiskDataInfoBean;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserRiskDataInfoBeanMapper {
    int deleteByPrimaryKey(String id);

    int insert(UserRiskDataInfoBean record);

    int insertSelective(UserRiskDataInfoBean record);

    UserRiskDataInfoBean selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(UserRiskDataInfoBean record);

    int updateByPrimaryKey(UserRiskDataInfoBean record);

    List<UserRiskDataInfoBean> selectByProcessStatus(@Param("processStatus") Integer processStatus, @Param("terminalId") String terminalId);
}