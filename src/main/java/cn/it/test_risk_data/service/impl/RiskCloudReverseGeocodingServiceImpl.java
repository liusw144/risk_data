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
import com.sinaif.king.model.device.UserGpsMongoBean;
import com.sinaif.king.model.finance.data.UserRiskDataMongoBean;
import com.sinaif.king.model.finance.data.UserRiskDataResultBean;
import com.sinaif.king.mongo.UserRiskDataMongoDao;
import com.sinaif.king.service.message.LogRequestService;
import com.sinaif.king.service.risk.UploadDeviceResultService;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

*/
/**
 * 闪云金科经纬度转换
 *//*

@Service
public class RiskCloudReverseGeocodingServiceImpl implements RiskDataService {

    private static final Logger logger = LoggerFactory.getLogger(RiskCloudReverseGeocodingServiceImpl.class);
    //闪云金科 账号APP_ID
    @Value("${riskcloud.appId:xxxx}")
    private String appId;
    //闪云金科密钥
    @Value("${riskcloud.secret:xxxx}")
    private String secret;
    @Value("${riskcloud.reversegeocoding.url:xxxx}")
    private String reverseGeocodingUrl;

    @Autowired
    private LogRequestService logRequestService;

    @Autowired
    private UserRiskDataResultBeanMapper userRiskDataResultBeanMapper;

    @Autowired
    private UserRiskDataMongoDao userRiskDataMongoDao;

    @Autowired
    private UploadDeviceResultService uploadDeviceResultService;

    @Override
    public RiskChannelDataTypeEnum getServiceName() {
        return RiskChannelDataTypeEnum.RISKCLOUD_REVERSEGEOCODING;
    }

    @Override
    public void callRiskData(RiskDataReqParam riskDataReqParam) {
        try {
            logger.info("用户 userId={} 开始调用RISKCLOUD_REVERSEGEOCODING接口服务", riskDataReqParam.getUserId());
            UserRiskDataResultBean userRiskDataResultBean = new UserRiskDataResultBean();
            //表主键id
            userRiskDataResultBean.setId(riskDataReqParam.getId());
            List<UserGpsMongoBean> gpsMongoBeans = uploadDeviceResultService.queryGPS(riskDataReqParam.getUserId(), "1");
            if (gpsMongoBeans == null || gpsMongoBeans.isEmpty()) {
                logger.info("暂时没有用户位置gps信息");
                userRiskDataResultBean.setStatus(4);
                userRiskDataResultBean.setRemark("暂时没有用户位置gps信息");
                userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                return;
            }

            UserGpsMongoBean userGpsMongoBean = gpsMongoBeans.get(0);
            if (userGpsMongoBean == null || StringUtils.isBlank(userGpsMongoBean.getLongitude()) || StringUtils.isBlank(userGpsMongoBean.getLatitude())) {
                logger.info("暂时没有用户位置gps信息");
                userRiskDataResultBean.setStatus(4);
                userRiskDataResultBean.setRemark("暂时没有用户位置gps信息");
                userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                return;
            }

            if (riskDataReqParam != null) {

                String uid = UUID.randomUUID().toString();

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("signKey", appId);
                jsonObject.put("timestamp", System.currentTimeMillis());
                jsonObject.put("nonce", UUID.randomUUID() + "");

                jsonObject.put("uid", uid);
                jsonObject.put("longitude", userGpsMongoBean.getLongitude());
                jsonObject.put("latitude", userGpsMongoBean.getLatitude());
                jsonObject.put("sign", getSignedStrs(jsonObject));

                String jsonStr = JSON.toJSONString(jsonObject);
                logger.info("请求body={}", jsonStr);
                // 接口请求响应信息记录到日志服务
                LogReqResultBean request = new LogReqResultBean();
                request.setProductid("RISK_DATA");
                request.setTerminalid(TerminalEnum.FASTCASH.getCode());
                request.setBusinessid(riskDataReqParam.getId());
                request.setBusinesstype(getServiceName().name());
                request.setRequesttime(new Date());
                request.setRequestbody(jsonStr);
                request.setRequestdesc(reverseGeocodingUrl);
                logger.info("请求url={}", reverseGeocodingUrl);
                String respStr;
                try {
                    //响应
                    respStr = HttpClientUtils.sendPostByJsonAndHeader(reverseGeocodingUrl, null, jsonStr);
                    request.setResponsetime(new Date());
                    request.setResponsebody(respStr);
                    JSONObject responseObject = JSONObject.parseObject(respStr);

                    if (responseObject != null) {
                        String status = responseObject.getString("statusCode");
                        String message = responseObject.getString("statusMessage");
                        request.setResponseid(status);
                        request.setRemark(message);
                        if ("0".equals(status)) {
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
                    JSONObject data = null;
                    //成功情况
                    if ("0".equals(resultJson.getString("statusCode"))) {
                        data = resultJson.getJSONObject("data");
                        UserRiskDataMongoBean userRiskDataMongoBean = new UserRiskDataMongoBean();
                        //mongo使用result表的id
                        userRiskDataMongoBean.setId(riskDataReqParam.getId());
                        //存userId
                        userRiskDataMongoBean.setUserId(riskDataReqParam.getUserId());
                        //业务线
                        userRiskDataMongoBean.setTerminalid("1001");
                        //厂商
                        userRiskDataMongoBean.setChannel(RiskChannelTypeEnum.RISKCLOUD.name());
                        //数据类型
                        userRiskDataMongoBean.setType(RiskDataTypeEnum.REVERSEGEOCODING.name());
                        //创建时间
                        userRiskDataMongoBean.setCreatetime(new Date());


                        //是否收费 0-免费 1-收费 (statusCode=0 && （errorCode=9000 || errorCode=9007）收费)
                        if ("13000".equals(data.getString("errorCode"))) {
                            //状态1-完成
                            userRiskDataResultBean.setStatus(1);
                            userRiskDataResultBean.setPricingStrategy(1);
                            //userRiskDataResultBean.setRemark("成功查询到该用户的地理信息" + data.getString("errMessage"));
                            userRiskDataMongoBean.setData(data.toJSONString());
                            //成功才存mongoDB
                            logger.info("userRiskDataMongoBean={}", userRiskDataMongoBean);
                            userRiskDataMongoDao.sava(userRiskDataMongoBean);
                        } else if ("13001".equals(data.getString("errorCode"))) {
                            //状态1-完成
                            userRiskDataResultBean.setStatus(1);
                            //请求失败
                            userRiskDataResultBean.setPricingStrategy(3);
                            userRiskDataResultBean.setRemark("经纬度转换失败" + data.getString("errMessage"));
                        } else {
                            //处理异常
                            userRiskDataResultBean.setStatus(5);
                            userRiskDataResultBean.setPricingStrategy(0);
                            userRiskDataResultBean.setRemark("其他的异常错误" + data.getString("errMessage"));
                        }
                        //更新时间
                        userRiskDataResultBean.setUpdateTime(new Date());
                        //信息更新
                        logger.info("userRiskDataResultBean={}", userRiskDataResultBean);
                        userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);

                    }
                    if ("1007".equals(resultJson.getString("statusCode"))) {
                        //状态1-完成
                        userRiskDataResultBean.setStatus(5);
                        //免费
                        userRiskDataResultBean.setPricingStrategy(0);
                        //备注
                        userRiskDataResultBean.setRemark("经纬度转换异常" + resultJson.getString("statusMessage"));
                        //更新时间
                        userRiskDataResultBean.setUpdateTime(new Date());
                        //信息更新
                        logger.info("userRiskDataResultBean={}", userRiskDataResultBean);
                        userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);


                    }
                }

            }
        } catch (Exception e) {
            logger.error("异常错误信息：" + e.getMessage());
        }
        logger.info("用户 userId={} 调用RISKCLOUD_REVERSEGEOCODING接口服务结束", riskDataReqParam.getUserId());
    }

    @Override
    public void callRiskDataAsycResult(UserRiskDataResultBean userRiskDataResultBean) {
        return;
    }

    */
/**
     * 签名方法
     *//*

    private String getSignedStrs(JSONObject data) {
        // appSecretKey为签名秘钥
        // data为其他接口参数
        data.put("appSecret", secret);
        StringBuilder signParam = new StringBuilder();
        Object[] keys = data.keySet().toArray();
        Arrays.sort(keys);
        for (Object key : keys) {
            if ("sign".equals(key)) {
                continue;
            }
            Object valueObject = data.get(key);
            if (valueObject != null && !valueObject.getClass().isAssignableFrom(JSONObject.class)) {
                String value = valueObject.toString();
                if (StringUtils.isNotBlank(value)) {
                    signParam.append(key).append(value);
                }
            }
        }
        String sign = DigestUtils.sha1Hex(signParam.toString());
        data.remove("appSecret");
        return sign;
    }

}
*/
