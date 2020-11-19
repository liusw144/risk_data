package cn.it.test_risk_data.dao;

import cn.it.test_risk_data.domain.UserRiskDataResultBean;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserRiskDataResultBeanMapper {
    int deleteByPrimaryKey(String id);

    int insert(UserRiskDataResultBean record);

    int insertSelective(UserRiskDataResultBean record);

    UserRiskDataResultBean selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(UserRiskDataResultBean record);

    int updateByPrimaryKey(UserRiskDataResultBean record);

    List<UserRiskDataResultBean> selectAll(@Param("status") Integer processResult, @Param("terminalId") String terminalId);
}