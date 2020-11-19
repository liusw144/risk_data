/*
package cn.it.test_risk_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sinaif.king.common.task.TaskQueueManager;
import com.sinaif.king.common.utils.HttpClientUtils;
import com.sinaif.king.common.utils.SnowFlakeUtil;
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
 * 水象云黑名单
 *//*


@Service
public class ShuixiangBlackListServiceImpl implements RiskDataService {

    private static final Logger logger = LoggerFactory.getLogger(ShuixiangBlackListServiceImpl.class);

    @Value("${shuixiang.account:xxxx}")
    private String account;
    @Value("${shuixiang.password:xxxx}")
    private String password;
    @Value("${shuixiang.blackListUrl:xxxx}")
    private String blackListUrl;

    @Autowired
    private LogRequestService logRequestService;
    @Autowired
    private UserRiskDataMongoDao userRiskDataMongoDao;
    @Autowired
    private UserRiskDataResultBeanMapper userRiskDataResultBeanMapper;

    @Override
    public RiskChannelDataTypeEnum getServiceName() {
        return RiskChannelDataTypeEnum.SHUIXIANG_BLACKLIST;
    }

    @Override
    public void callRiskData(RiskDataReqParam riskDataReqParam) {

        try {
            logger.info("用户 userId={} 开始调用SHUIXIANG_BLACKLIST接口服务", riskDataReqParam.getUserId());
            if (riskDataReqParam != null) {
                //信息更新result表
                UserRiskDataResultBean userRiskDataResultBean = new UserRiskDataResultBean();
                //表主键id
                userRiskDataResultBean.setId(riskDataReqParam.getId());
                //header
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("appKey", account);
                headerMap.put("appSecret", password);

                //body
                Map<String, Object> bodyMap = new HashMap<>();
                String adhaarNumber = riskDataReqParam.getAdhaarNumber();
                if (StringUtils.isBlank(adhaarNumber)) {
                    logger.info("adhaarNumber是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("adhaarNumber是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }

                String panNumber = riskDataReqParam.getPanNumber();
                if (StringUtils.isBlank(panNumber)) {
                    logger.info("panNumber是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("panNumber是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                String phoneNumber = riskDataReqParam.getPhoneNumber();

                if (StringUtils.isBlank(phoneNumber)) {
                    logger.info("phoneNumber是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("phoneNumber是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                bodyMap.put("aadhaarNumber", adhaarNumber);
                bodyMap.put("panNumber", panNumber);
                bodyMap.put("mobile", phoneNumber);
                bodyMap.put("transactionNo", "RJ" + System.currentTimeMillis() + (Math.random() * 9 + 1) * 100000);
                bodyMap.put("orderNo", SnowFlakeUtil.generateId() + "");

                String jsonStr = JSON.toJSONString(bodyMap);
                logger.info("请求body={}", jsonStr);
                // 接口请求响应信息记录到日志服务
                LogReqResultBean request = new LogReqResultBean();
                request.setProductid("RISK_DATA");
                request.setTerminalid(TerminalEnum.FASTCASH.getCode());
                //状态表id
                request.setBusinessid(riskDataReqParam.getId());
                request.setBusinesstype(getServiceName().name());
                request.setRequesttime(new Date());
                request.setRequestbody(jsonStr);
                request.setRequestdesc(blackListUrl);
                logger.info("请求url={}", blackListUrl);

                String respStr;
                try {
                    //响应
                    respStr = HttpClientUtils.sendPostByJsonAndHeader(blackListUrl, headerMap, jsonStr);
                    request.setResponsetime(new Date());
                    request.setResponsebody(respStr);
                    JSONObject responseObject = JSONObject.parseObject(respStr);
                    if (responseObject != null) {
                        String status = responseObject.getString("code");
                        String message = responseObject.getString("msg");
                        request.setResponseid(status);
                        request.setRemark(message);
                        if ("S0000".equals(status)) {
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
                    if (logRequestService != null) {
                        logger.info("响应的数据保存开始：\n");
                        TaskQueueManager.getIntance().addTask(new LogDbTask(request, logRequestService));
                        logger.info("响应的数据保结束：\n");
                    }

                }


                JSONObject resultJson = JSONObject.parseObject(respStr);

                if (resultJson != null) {

                }
                if ("S0000".equals(resultJson.getString("code"))) {
                    //根据code状态码进行存储策略 S0000-成功
                    UserRiskDataMongoBean userRiskDataMongoBean = new UserRiskDataMongoBean();
                    //mongo使用result表的id
                    userRiskDataMongoBean.setId(riskDataReqParam.getId());
                    //存userId
                    userRiskDataMongoBean.setUserId(riskDataReqParam.getUserId());
                    //业务线
                    userRiskDataMongoBean.setTerminalid("1001");
                    //厂商
                    userRiskDataMongoBean.setChannel(RiskChannelTypeEnum.SHUIXIANG.name());
                    //数据类型
                    userRiskDataMongoBean.setType(RiskDataTypeEnum.BLACKLIST.name());
                    //创建时间
                    userRiskDataMongoBean.setCreatetime(new Date());
                    //请求成功结果
                    userRiskDataMongoBean.setData(resultJson.getJSONObject("data").toJSONString());
                    //存mongoDB
                    logger.debug("userRiskDataMongoBean={}", userRiskDataMongoBean);
                    userRiskDataMongoDao.sava(userRiskDataMongoBean);
                    //状态1-已处理
                    userRiskDataResultBean.setStatus(1);
                    // hitResult=1 命中收费 是否收费 0-免费 1-收费
                    JSONObject data = resultJson.getJSONObject("data");
                    if (data != null && 1 == data.getIntValue("hitResult")) {
                        userRiskDataResultBean.setPricingStrategy(1);
                        userRiskDataResultBean.setRemark("命中黑名单");
                    } else {
                        userRiskDataResultBean.setPricingStrategy(0);
                        userRiskDataResultBean.setRemark("未命中黑名单");
                    }
                    //更新时间
                    userRiskDataResultBean.setUpdateTime(new Date());
                    //信息更新
                    logger.info("userRiskDataResultBean={}", userRiskDataResultBean);
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                }

            }
        } catch (Exception e) {
            logger.error("异常错误信息：" + e.getMessage());
        }
        logger.info("用户 userId={} 调用SHUIXIANG_BLACKLIST接口服务结束", riskDataReqParam.getUserId());
    }

    @Override
    public void callRiskDataAsycResult(UserRiskDataResultBean userRiskDataResultBean) {
        return;
    }

}
*/
