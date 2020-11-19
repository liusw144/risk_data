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

@Service
public class AdvanceCreditReportRiskDataServiceImpl implements RiskDataService {

    private static final Logger logger = LoggerFactory.getLogger(AdvanceCreditReportRiskDataServiceImpl.class);
    //advance征信报告挂靠线上key
    @Value("${advance.key:xxxx}")
    private String key;
    //advance征信报告挂靠线上url
    @Value("${advance.creditReportUrl:xxxx}")
    private String creditReportUrl;

    @Autowired
    private UserRiskDataMongoDao userRiskDataMongoDao;

    @Autowired
    private LogRequestService logRequestService;

    @Autowired
    private UserRiskDataResultBeanMapper userRiskDataResultBeanMapper;

    @Override
    public RiskChannelDataTypeEnum getServiceName() {
        return RiskChannelDataTypeEnum.ADVANCE_CREDITREPORT;
    }

    @Override
    public void callRiskData(RiskDataReqParam riskDataReqParam) {
        logger.info("用户 userId={} 开始调用ADVANCE_CREDITREPORT接口服务", riskDataReqParam.getUserId());
        try {
            if (riskDataReqParam != null) {
                UserRiskDataResultBean userRiskDataResultBean = new UserRiskDataResultBean();
                userRiskDataResultBean.setId(riskDataReqParam.getId());
                String phoneNumber1 = riskDataReqParam.getPhoneNumber();
                //电话是必选参数 +91
                if (StringUtils.isBlank(phoneNumber1)) {
                    logger.info("phoneNumber是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("phoneNumber是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                String phoneNumber = "+91" + phoneNumber1;
                //firstName必选参数
                String firstName = riskDataReqParam.getFirstName();
                if (StringUtils.isBlank(firstName)) {
                    logger.info("firstName是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("firstName是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                //adhaarNumber必选
                String adhaarNumber = riskDataReqParam.getAdhaarNumber();
                if (StringUtils.isBlank(adhaarNumber)) {
                    logger.info("adhaarNumber是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("adhaarNumber是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                //panNumber必选
                String panNumber = riskDataReqParam.getPanNumber();
                if (StringUtils.isBlank(panNumber)) {
                    logger.info("panNumber是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("panNumber是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                //头部参数
                Map<String, String> headerParam = new HashMap<>();
                headerParam.put("X-ADVAI-KEY", key);
                //请求参数
                Map<String, Object> jsonMap = new HashMap<>();

                jsonMap.put("aadhaarNumber", adhaarNumber);
                jsonMap.put("panNumber", panNumber);
                jsonMap.put("phoneNumber", phoneNumber);
                jsonMap.put("firstName", firstName);
                jsonMap.put("middleName", riskDataReqParam.getMiddleName());
                jsonMap.put("lastName", riskDataReqParam.getLastName());
                //格式‘yyyy-MM-DD
                jsonMap.put("birthday", riskDataReqParam.getBirthday());
                String jsonStr = JSON.toJSONString(jsonMap);
                logger.info("请求体 body:" + jsonStr);
                // 接口请求响应信息记录到日志服务
                LogReqResultBean request = new LogReqResultBean();
                request.setProductid("RISK_DATA");
                request.setTerminalid(TerminalEnum.FASTCASH.getCode());
                request.setBusinessid(riskDataReqParam.getId());
                request.setBusinesstype(getServiceName().name());
                request.setRequesttime(new Date());
                request.setRequestbody(jsonStr);
                request.setRequestdesc(creditReportUrl);
                logger.info("请求地址 url:" + jsonStr);
                String respStr;
                try {
                    //获取请求响应
                    respStr = HttpClientUtils.sendPostByJsonAndHeader(creditReportUrl, headerParam, jsonStr);
                    logger.debug("响应信息 response={}", respStr);
                    request.setResponsetime(new Date());
                    request.setResponsebody(respStr);
                    JSONObject responseObject = JSONObject.parseObject(respStr);

                    if (responseObject != null) {
                        //响应code
                        String status = responseObject.getString("code");
                        //响应msg
                        String message = responseObject.getString("message");
                        //响应code
                        request.setResponseid(status);//数据库长度只有10
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
                if (resultJson != null) {
                    if ("SUCCESS".equals(resultJson.getString("code"))) {
                        //根据status_code状态码进行存储策略 1-成功 2-失败 100-参数错误 101-内部服务器错误
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
                        userRiskDataMongoBean.setType(RiskDataTypeEnum.CREDITREPORT.name());
                        //创建时间
                        userRiskDataMongoBean.setCreatetime(new Date());
                        //请求成功结果
                        userRiskDataMongoBean.setData(resultJson.getJSONObject("data").toJSONString());
                        //存mongoDB
                        logger.debug("userRiskDataMongoBean={}", userRiskDataMongoBean);
                        userRiskDataMongoDao.sava(userRiskDataMongoBean);

                        //状态1-已处理
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
                        logger.info("userRiskDataResultBean = {}", userRiskDataResultBean);
                        userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    }

                    //参数错误情况
                    if ("PARAMETER_ERROR".equals(resultJson.getString("code"))) {
                        //状态4-参数校验错误
                        userRiskDataResultBean.setStatus(3);
                        //0-免费
                        userRiskDataResultBean.setPricingStrategy(0);
                        //备注
                        userRiskDataResultBean.setRemark("参数校验错误：" + resultJson.getString("message"));
                        //更新时间
                        userRiskDataResultBean.setUpdateTime(new Date());
                        //存入记录
                        logger.info("userRiskDataResultBean = {}", userRiskDataResultBean);
                        userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("异常错误信息：" + e.getMessage());
        }
        logger.info("用户 userId={} 调用接口ADVANCE_CREDITREPORT服务结束", riskDataReqParam.getUserId());

    }


    @Override
    public void callRiskDataAsycResult(UserRiskDataResultBean userRiskDataResultBean) {
        return;
    }
}
*/
