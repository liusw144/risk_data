package cn.it.test_risk_data.service.impl;

import cn.it.test_risk_data.dao.UserRiskDataMongoDao;
import cn.it.test_risk_data.domain.UserRiskDataMongoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRiskDataMongoDaoImpl implements UserRiskDataMongoDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void sava(UserRiskDataMongoBean userRiskDataMongoBean) {
        if (userRiskDataMongoBean == null) {
            return;
        }
        mongoTemplate.save(userRiskDataMongoBean);
    }
}