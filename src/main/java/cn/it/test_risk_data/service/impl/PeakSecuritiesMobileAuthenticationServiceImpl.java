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
import org.apache.commons.codec.digest.DigestUtils;
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
 * 摩比神奇手机号码服务提供商认证
 *//*

@Service
public class PeakSecuritiesMobileAuthenticationServiceImpl implements RiskDataService {

    private static final Logger logger = LoggerFactory.getLogger(PeakSecuritiesMobileAuthenticationServiceImpl.class);
    //摩比神奇线上 账号APP_ID
    @Value("${peaksecurity.appId:xxxx}")
    private String appId;
    @Value("${peaksecurity.secret:xxxx}")
    private String secret;
    //摩比神奇手机号码服务提供商认证测试环境url
    @Value("${peaksecurity.mobileAuthenticationUrl:xxxx}")
    private String mobileAuthenticationUrl;
    @Autowired
    private LogRequestService logRequestService;

    @Autowired
    private UserRiskDataResultBeanMapper userRiskDataResultBeanMapper;

    @Autowired
    private UserRiskDataMongoDao userRiskDataMongoDao;

    @Override
    public RiskChannelDataTypeEnum getServiceName() {
        return RiskChannelDataTypeEnum.PEAKSECURITIES_MOBILEAUTHENTICATION;
    }

    @Override
    public void callRiskData(RiskDataReqParam riskDataReqParam) {

        try {
            logger.info("用户调用 userId={} 开始调用PEAKSECURITIES_MOBILEAUTHENTICATION接口服务", riskDataReqParam.getUserId());
            if (riskDataReqParam != null) {
                UserRiskDataResultBean userRiskDataResultBean = new UserRiskDataResultBean();
                userRiskDataResultBean.setId(riskDataReqParam.getId());
                //手机号码格式10位
                String mobile = riskDataReqParam.getPhoneNumber();
                if (StringUtils.isBlank(mobile)) {
                    logger.info("mobile是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("mobile是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("mobile", mobile);
                String jsonStr = JSON.toJSONString(bodyMap);
                logger.info("请求body：" + jsonStr);
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("x-m-app-id", appId);
                headerMap.put("x-m-signature", DigestUtils.md5Hex(new StringBuilder().append(jsonStr).append(secret).append(appId).toString()));  //签名=md5(string(postbody)+secret+app_id)

                // 接口请求响应信息记录到日志服务
                LogReqResultBean request = new LogReqResultBean();
                request.setProductid("RISK_DATA");
                request.setTerminalid(TerminalEnum.FASTCASH.getCode());
                request.setBusinessid(riskDataReqParam.getId());
                request.setBusinesstype(getServiceName().name());
                request.setRequesttime(new Date());
                request.setRequestbody(jsonStr);
                request.setRequestdesc(mobileAuthenticationUrl);
                logger.info("请求url={}", mobileAuthenticationUrl);
                String respStr;
                try {
                    respStr = HttpClientUtils.sendPostByJsonAndHeader(mobileAuthenticationUrl, headerMap, jsonStr);//响应
                    logger.debug("response={}", respStr);
                    request.setResponsetime(new Date());
                    request.setResponsebody(respStr);
                    JSONObject responseObject = JSONObject.parseObject(respStr);

                    if (responseObject != null) {
                        String status = responseObject.getString("code");
                        String message = responseObject.getString("msg");
                        request.setResponseid(status);
                        request.setRemark(message);
                        if ("200".equals(status)) {
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

                if (resultJson != null && "200".equals(resultJson.getString("code"))) {
                    JSONObject data = resultJson.getJSONObject("data");
                    if (data != null) {
                        String status_code = data.getString("status_code");
                        //状态码1-成功或2-失败 100-无效参数 101-服务内部错误
                        if ("1".equals(status_code) || "2".equals(status_code)) {
                            //根据status_code状态码进行存储策略 1-成功 2-失败 100-参数错误 101-内部服务器错误
                            UserRiskDataMongoBean userRiskDataMongoBean = new UserRiskDataMongoBean();
                            //mongo使用result表的id
                            userRiskDataMongoBean.setId(riskDataReqParam.getId());
                            //存userId
                            userRiskDataMongoBean.setUserId(riskDataReqParam.getUserId());
                            //业务线
                            userRiskDataMongoBean.setTerminalid("1001");
                            //厂商
                            userRiskDataMongoBean.setChannel(RiskChannelTypeEnum.PEAKSECURITIES.name());
                            //数据类型
                            userRiskDataMongoBean.setType(RiskDataTypeEnum.MOBILEAUTHENTICATION.name());
                            //创建时间
                            userRiskDataMongoBean.setCreatetime(new Date());
                            //请求成功结果
                            userRiskDataMongoBean.setData(data.toJSONString());
                            //存mongoDB
                            logger.info("userRiskDataMongoBean={}", userRiskDataMongoBean);
                            userRiskDataMongoDao.sava(userRiskDataMongoBean);

                            //状态1-完成
                            userRiskDataResultBean.setStatus(1);
                            //是否收费 0-免费 1-收费
                            if ("2".equals(status_code)) {
                                userRiskDataResultBean.setPricingStrategy(0);
                            }

                            if ("1".equals(status_code)) {
                                userRiskDataResultBean.setPricingStrategy(1);
                            }
                            //更新时间
                            userRiskDataResultBean.setUpdateTime(new Date());
                            //信息更新
                            logger.info("userRiskDataResultBean={}", userRiskDataResultBean);
                            userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                        }

                        //参数错误情况
                        if ("100".equals(status_code)) {
                            //状态4-参数校验错误
                            userRiskDataResultBean.setStatus(4);
                            //0-免费
                            userRiskDataResultBean.setPricingStrategy(0);
                            //备注
                            userRiskDataResultBean.setRemark("参数校验错误");
                            //更新时间
                            userRiskDataResultBean.setUpdateTime(new Date());
                            //存入记录
                            logger.info("userRiskDataResultBean={}", userRiskDataResultBean);
                            userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                        }

                      */
/*  //服务器内部错误—直接返回无需标记等待下次再次请求接口
                        if ("101".equals(status_code)) {
                            return;
                        }*//*


                    }

                }

            }
        } catch (Exception e) {
            logger.error("异常错误信息：" + e.getMessage());
        }
        logger.info("用户调用 userId={} 调用PEAKSECURITIES_MOBILEAUTHENTICATION接口服务结束", riskDataReqParam.getUserId());
    }

    @Override
    public void callRiskDataAsycResult(UserRiskDataResultBean userRiskDataResultBean) {
        return;
    }

}
*/
