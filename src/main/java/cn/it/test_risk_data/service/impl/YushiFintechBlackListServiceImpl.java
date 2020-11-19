/*
package cn.it.test_risk_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sinaif.king.common.task.TaskQueueManager;
import com.sinaif.king.common.utils.HttpClientUtils;
import com.sinaif.king.common.utils.StringUtils;
import com.sinaif.king.common.utils.YushiAESUtil;
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
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

*/
/**
 * 与时科技黑名单
 *//*

@Service
public class YushiFintechBlackListServiceImpl implements RiskDataService {

    private static final Logger logger = LoggerFactory.getLogger(YushiFintechBlackListServiceImpl.class);
    //与时科技APP_ID
    @Value("${yushifintech.appId:xxxx}")
    private String appId;
    //与时科技密钥
    @Value("${yushifintech.key:xxxx}")
    private String key;

    //黑名单测试url
    @Value("${yushifintech.blackListUrl:xxxx}")
    private String blackListUrl;

    @Autowired
    private LogRequestService logRequestService;

    @Autowired
    private UserRiskDataResultBeanMapper userRiskDataResultBeanMapper;

    @Autowired
    private UserRiskDataMongoDao userRiskDataMongoDao;

    @Override
    public RiskChannelDataTypeEnum getServiceName() {
        return RiskChannelDataTypeEnum.YUSHIFINTECH_BLACKLIST;
    }

    @Override
    public void callRiskData(RiskDataReqParam riskDataReqParam) {
        logger.info("用户 userId={} 开始调用YUSHIFINTECH_BLACKLIST接口服务", riskDataReqParam.getUserId());
        try {
            if (riskDataReqParam != null) {
                //信息更新result表
                UserRiskDataResultBean userRiskDataResultBean = new UserRiskDataResultBean();
                //表主键id
                userRiskDataResultBean.setId(riskDataReqParam.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("method", "shield.risk.blacklist.detail");
                bodyMap.put("timestamp", System.currentTimeMillis());

                String mobile = riskDataReqParam.getPhoneNumber();
                String pan = riskDataReqParam.getPanNumber();
                String aadhaar = riskDataReqParam.getAdhaarNumber();
                if (StringUtils.isBlank(mobile) && StringUtils.isBlank(pan) && StringUtils.isBlank(aadhaar)) {
                    logger.info("mobile&pan&aadhaar不能同时不存在");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("mobile&pan&aadhaar不能同时不存在");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("mobile", mobile);
                jsonObject.put("pan", pan);
                jsonObject.put("aadhaar", aadhaar);
                jsonObject.put("real_name", riskDataReqParam.getFullName());
                bodyMap.put("data", jsonObject);

                Map<String, String> bodyMap2 = new HashMap<>();

                bodyMap2.put("app_id", appId);
                bodyMap2.put("encrypt_msg", YushiAESUtil.encrypt(JSONObject.toJSONString(bodyMap), key));

                String jsonStr = JSON.toJSONString(bodyMap2);
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
                    respStr = HttpClientUtils.sendPostByJsonAndHeader(blackListUrl, null, jsonStr);//响应
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
                    request.setStatus(1);
                    if (logRequestService != null) {
                        logger.info("响应的数据保存开始：\n");
                        TaskQueueManager.getIntance().addTask(new LogDbTask(request, logRequestService));
                        logger.info("响应的数据保结束：\n");
                    }

                }


                JSONObject resultJson = JSONObject.parseObject(respStr);

                if (resultJson != null && 200 == resultJson.getInteger("code")) {

                    UserRiskDataMongoBean userRiskDataMongoBean = new UserRiskDataMongoBean();
                    //mongo使用result表的id
                    userRiskDataMongoBean.setId(riskDataReqParam.getId());
                    //存userId
                    userRiskDataMongoBean.setUserId(riskDataReqParam.getUserId());
                    //业务线
                    userRiskDataMongoBean.setTerminalid("1001");
                    //厂商
                    userRiskDataMongoBean.setChannel(RiskChannelTypeEnum.YUSHIFINTECH.name());
                    //数据类型
                    userRiskDataMongoBean.setType(RiskDataTypeEnum.BLACKLIST.name());
                    //创建时间
                    userRiskDataMongoBean.setCreatetime(new Date());
                    //请求成功结果
                    userRiskDataMongoBean.setData(resultJson.getString("data"));
                    //存mongoDB
                    logger.debug("请求userRiskDataMongoBean={}", userRiskDataMongoBean);
                    userRiskDataMongoDao.sava(userRiskDataMongoBean);

                    //状态1-已处理
                    userRiskDataResultBean.setStatus(1);
                    //是否收费 0-免费 1-收费
                    userRiskDataResultBean.setPricingStrategy(1);
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
        logger.info("用户 userId={} 调用YUSHIFINTECH_BLACKLIST接口结束", riskDataReqParam.getUserId());

    }

    @Override
    public void callRiskDataAsycResult(UserRiskDataResultBean userRiskDataResultBean) {
        return;
    }

    */
/*
     * 加密
     *//*

    private static String encrypt(String key, String cleartext) {
        if (null == cleartext || cleartext.length() == 0) {
            return cleartext;
        }
        try {
            byte[] result = encrypt(key, cleartext.getBytes());
            return new String(Base64.encodeBase64String(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    */
/**
     * 加密
     *//*

    private static byte[] encrypt(String key, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }
}


*/
