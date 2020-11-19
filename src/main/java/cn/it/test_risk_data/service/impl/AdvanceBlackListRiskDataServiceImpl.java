/*
package cn.it.test_risk_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sinaif.king.common.task.TaskQueueManager;
import com.sinaif.king.common.utils.HttpClientUtils;
import com.sinaif.king.common.utils.StringUtils;
import com.sinaif.king.dao.data.UserRiskDataResultBeanMapper;
import com.sinaif.king.enums.finance.message.LogReqResultBean;
import com.sinaif.king.enums.terminal.TerminalEnum;
import com.sinaif.king.model.finance.data.UserRiskDataMongoBean;
import com.sinaif.king.model.finance.data.UserRiskDataResultBean;
import com.sinaif.king.mongo.UserRiskDataMongoDao;
import com.sinaif.king.service.message.LogRequestService;
import com.sinaif.king.service.riskdata.RiskDataService;
import com.sinaif.king.service.task.LogDbTask;
import com.sinaif.task.enums.RiskChannelDataTypeEnum;
import com.sinaif.task.enums.RiskChannelTypeEnum;
import com.sinaif.task.enums.RiskDataTypeEnum;
import com.sinaif.task.vo.RiskDataReqParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

*/
/**
 * advance黑名单
 *//*

@Service
public class AdvanceBlackListRiskDataServiceImpl implements RiskDataService {

    private static final Logger logger = LoggerFactory.getLogger(AdvanceBlackListRiskDataServiceImpl.class);
    @Value("${advance.apiKey:xxxx}")
    private String apiKey;

    @Value("${advance.blackListCheckUrl:xxxx}")
    private String blackListCheckUrl;

    @Autowired
    private LogRequestService logRequestService;

    @Autowired
    private UserRiskDataMongoDao userRiskDataMongoDao;
    @Autowired
    private UserRiskDataResultBeanMapper userRiskDataResultBeanMapper;

    @Override
    public RiskChannelDataTypeEnum getServiceName() {
        return RiskChannelDataTypeEnum.ADVANCE_BLACKLIST;
    }

    @Override
    public void callRiskData(RiskDataReqParam riskDataReqParam) {
        try {
            logger.info("用户 userId={} 开始调用ADVANCE_BLACKLIST接口服务", riskDataReqParam.getUserId());
            if (riskDataReqParam != null) {
                UserRiskDataResultBean userRiskDataResultBean = new UserRiskDataResultBean();
                //主键id
                userRiskDataResultBean.setId(riskDataReqParam.getId());
                String pan = riskDataReqParam.getPanNumber();
                //pan必选参数
                if (StringUtils.isBlank(pan)) {
                    logger.info("pan是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("pan是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                String phoneNumber1 = riskDataReqParam.getPhoneNumber();
                if (StringUtils.isBlank(phoneNumber1)) {
                    logger.info("phoneNumber是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("phoneNumber是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                //电话是必选参数 +91
                String phoneNumber = "+91" + phoneNumber1;
                //header参数
                Map<String, String> headerParam = new HashMap<>();
                headerParam.put("X-ADVAI-KEY", apiKey);
                //body请求参数
                Map<String, Object> jsonMap = new HashMap<>();

                jsonMap.put("pan", pan);
                jsonMap.put("phoneNumber", phoneNumber);
                jsonMap.put("name", riskDataReqParam.getFullName());
                String jsonStr = JSON.toJSONString(jsonMap);
                logger.info("请求body:" + jsonStr);
                // 接口请求响应信息记录到日志服务
                LogReqResultBean request = new LogReqResultBean();
                request.setProductid("RISK_DATA");
                request.setTerminalid(TerminalEnum.FASTCASH.getCode());
                //状态表id
                request.setBusinessid(riskDataReqParam.getId());
                request.setBusinesstype(getServiceName().name());
                request.setRequesttime(new Date());
                request.setRequestbody(jsonStr);
                request.setRequestdesc(blackListCheckUrl);
                logger.info("请求url:" + blackListCheckUrl);
                String respStr;
                try {
                    //获取请求响应
                    respStr = HttpClientUtils.sendPostByJsonAndHeader(blackListCheckUrl, headerParam, jsonStr);
                    request.setResponsetime(new Date());
                    request.setResponsebody(respStr);
                    JSONObject responseObject = JSONObject.parseObject(respStr);

                    if (responseObject != null) {
                        //响应code
                        String status = responseObject.getString("code");
                        //响应msg
                        String message = responseObject.getString("message");
                        //响应code
                        request.setResponseid(status);
                        //响应msg
                        request.setRemark(message);
                        if ("SUCCESS".equals(status) || "SUCCESS_PASS".equals(status)) {
                            request.setStatus(1);
                            request.setRequeststatus(1);
                            request.setResponsestatus(1);

                        } else {
                            request.setStatus(0);
                            request.setRequeststatus(0);
                            request.setResponsestatus(0);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    throw e;
                } finally {
                    request.setResponsetime(new Date());
                    request.setCreatetime(new Date());
                    request.setStatus(1);
                    if (logRequestService != null) {
                        logger.info("响应的数据保存开始：\n");
                        TaskQueueManager.getIntance().addTask(new LogDbTask(request, logRequestService));
                        logger.info("响应的数据保结束：\n");
                    }

                }
                JSONObject resultJson = JSONObject.parseObject(respStr);
                logger.info("响应参数：" + respStr);
                if (resultJson != null) {

                    //成功情况
                    if ("SUCCESS_PASS".equals(resultJson.getString("code")) || "SUCCESS".equals(resultJson.getString("code"))) {

                        UserRiskDataMongoBean userRiskDataMongoBean = new UserRiskDataMongoBean();
                        //mongo使用result表的id
                        userRiskDataMongoBean.setId(riskDataReqParam.getId());
                        //存userId
                        userRiskDataMongoBean.setUserId(riskDataReqParam.getUserId());
                        //业务线
                        userRiskDataMongoBean.setTerminalid("1001");
                        //厂商
                        userRiskDataMongoBean.setChannel(RiskChannelTypeEnum.ADVANCE.name());
                        //数据类型
                        userRiskDataMongoBean.setType(RiskDataTypeEnum.BLACKLIST.name());
                        //创建时间
                        userRiskDataMongoBean.setCreatetime(new Date());
                        //请求成功结果
                        userRiskDataMongoBean.setData(resultJson.getJSONObject("data").toJSONString());
                        //存mongoDB
                        logger.info("userRiskDataMongoBean={}", userRiskDataMongoBean);
                        userRiskDataMongoDao.sava(userRiskDataMongoBean);

                        //信息更新result表
                        //更新状态1-成功
                        userRiskDataResultBean.setStatus(1);
                        //是否收费 0-免费 1-收费
                        if ("FREE".equals(resultJson.getString("pricingStrategy"))) {
                            userRiskDataResultBean.setPricingStrategy(0);
                        }

                        if ("PAY".equals(resultJson.getString("pricingStrategy"))) {
                            userRiskDataResultBean.setPricingStrategy(1);
                        }
                        //更新时间
                        userRiskDataResultBean.setUpdateTime(new Date());
                        //信息更新
                        logger.info("serRiskDataResultBean={}", userRiskDataResultBean);
                        userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    }

                    //参数错误情况
                    if ("PARAMETER_ERROR".equals(resultJson.getString("code"))) {
                        //请求状态4-参数错误
                        userRiskDataResultBean.setStatus(4);
                        //0-免费
                        userRiskDataResultBean.setPricingStrategy(0);
                        //备注
                        userRiskDataResultBean.setRemark("参数错误：" + resultJson.getString("message"));
                        //更新时间
                        userRiskDataResultBean.setUpdateTime(new Date());
                        //存入记录
                        logger.info("userRiskDataResultBean={}", userRiskDataResultBean);
                        userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    }

                }

            }
        } catch (Exception e) {
            logger.error("异常错误信息：" + e.getMessage());
        }
        logger.info("用户 userId={} 调用ADVANCE_BLACKLIST接口服务结束", riskDataReqParam.getUserId());

    }

    @Override
    public void callRiskDataAsycResult(UserRiskDataResultBean userRiskDataResultBean) {
        return;
    }
}
*/
