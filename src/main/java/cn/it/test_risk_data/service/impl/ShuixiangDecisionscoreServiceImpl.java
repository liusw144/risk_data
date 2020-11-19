/*
package cn.it.test_risk_data.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sinaif.king.common.task.TaskQueueManager;
import com.sinaif.king.common.utils.HttpClientUtils;
import com.sinaif.king.common.utils.SnowFlakeUtil;
import com.sinaif.king.common.utils.StringUtils;
import com.sinaif.king.dao.data.UserRiskDataResultBeanMapper;
import com.sinaif.king.enums.app.DeviceUploadTypeEnum;
import com.sinaif.king.enums.finance.message.LogReqResultBean;
import com.sinaif.king.enums.terminal.TerminalEnum;
import com.sinaif.king.model.device.UploadDeviceMongoBean;
import com.sinaif.king.model.device.vo.DeviceInfoVo;
import com.sinaif.king.model.finance.data.UserRiskDataMongoBean;
import com.sinaif.king.model.finance.data.UserRiskDataResultBean;
import com.sinaif.king.model.finance.request.niwodai.NiInstalledAppInfo;
import com.sinaif.king.mongo.UserRiskDataMongoDao;
import com.sinaif.king.service.message.LogRequestService;
import com.sinaif.king.service.risk.UploadDeviceResultService;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

*/
/**
 * 水象云决策分
 *//*

@Service
public class ShuixiangDecisionscoreServiceImpl implements RiskDataService {

    private static final Logger logger = LoggerFactory.getLogger(ShuixiangDecisionscoreServiceImpl.class);

    @Value("${shuixiang.account:xxxx}")
    private String account;
    @Value("${shuixiang.password:xxxx}")
    private String password;

    @Value("${shuixiang.decisionscoreUrl:xxxx}")
    private String decisionscoreUrl;

    @Autowired
    private LogRequestService logRequestService;
    @Autowired
    private UserRiskDataMongoDao userRiskDataMongoDao;
    @Autowired
    private UserRiskDataResultBeanMapper userRiskDataResultBeanMapper;
    @Autowired
    private UploadDeviceResultService uploadDeviceResultService;

    @Override
    public RiskChannelDataTypeEnum getServiceName() {
        return RiskChannelDataTypeEnum.SHUIXIANG_DECISIONSCORE;
    }

    @Override
    public void callRiskData(RiskDataReqParam riskDataReqParam) {
        try {
            logger.info("用户 userId={} 开始调用SHUIXIANG_DECISIONSCORE接口服务", riskDataReqParam.getUserId());
            if (riskDataReqParam != null) {
                //将记录写进result表
                UserRiskDataResultBean userRiskDataResultBean = new UserRiskDataResultBean();
                //result表的id
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

                String firstName = riskDataReqParam.getFirstName();
                if (StringUtils.isBlank(firstName)) {
                    logger.info("firstName是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("firstName是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }

                String lastName = riskDataReqParam.getLastName();
                if (StringUtils.isBlank(lastName)) {
                    logger.info("lastName是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("lastName是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }

                Integer gender = riskDataReqParam.getGender();
                if (gender == null) {
                    logger.info("gender是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("gender是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                String birthday = riskDataReqParam.getBirthday();
                if (StringUtils.isBlank(birthday)) {
                    logger.info("birthday是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("birthday是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                bodyMap.put("transactionNo", "RJ" + System.currentTimeMillis() + (Math.random() * 9 + 1) * 100000);
                bodyMap.put("orderNo", SnowFlakeUtil.generateId() + "");
                bodyMap.put("aadhaarNumber", adhaarNumber);
                bodyMap.put("panNumber", panNumber);
                bodyMap.put("mobile", phoneNumber);
                bodyMap.put("gender", gender);
                bodyMap.put("firstName", firstName);
                bodyMap.put("lastName", lastName);
                bodyMap.put("dateOfBirth", birthday);
                DeviceInfoVo deviceInfoVo = uploadDeviceResultService.queryDeviceinfo(riskDataReqParam.getUserId());
                if (deviceInfoVo == null && StringUtils.isBlank(deviceInfoVo.getImei())) {
                    logger.info("deviceId是必选参数");
                    return;
                }
                bodyMap.put("deviceId", deviceInfoVo.getImei());

                StringBuffer stringBuffer = new StringBuffer();
                List<UploadDeviceMongoBean> installed_apps = uploadDeviceResultService.queryUploadRecord(riskDataReqParam.getUserId(),
                        DeviceUploadTypeEnum.APPLIST.val, "200");
                if (installed_apps != null && !installed_apps.isEmpty()) {
                    for (UploadDeviceMongoBean installed_app : installed_apps) {
                        if (installed_app == null) {
                            continue;
                        }
                        List<NiInstalledAppInfo> niInstalledApps = JSONObject.parseArray(installed_app.getContent(), NiInstalledAppInfo.class);
                        if (niInstalledApps != null && !niInstalledApps.isEmpty()) {
                            for (NiInstalledAppInfo niInstalledApp : niInstalledApps) {
                                if (niInstalledApp == null && niInstalledApp.getAppname() == null) {
                                    continue;
                                }
                                stringBuffer.append(niInstalledApp.getAppname()).append(",");
                            }
                        }
                    }
                }
                //去掉尾部逗号
                String appNameStr = "";
                if (stringBuffer != null) {
                    appNameStr = stringBuffer.toString();
                    if (appNameStr.length() > 0 && appNameStr.endsWith(",")) {
                        appNameStr = appNameStr.substring(0, appNameStr.length() - 1);
                    }

                }
                bodyMap.put("appNames", appNameStr);

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
                request.setRequestdesc(decisionscoreUrl);
                logger.info("请求url={}", decisionscoreUrl);


                String respStr;
                try {
                    respStr = HttpClientUtils.sendPostByJsonAndHeader(decisionscoreUrl, headerMap, jsonStr);//响应
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
                    request.setStatus(1);
                    if (logRequestService != null) {
                        logger.info("响应的数据保存开始：\n");
                        TaskQueueManager.getIntance().addTask(new LogDbTask(request, logRequestService));
                        logger.info("响应的数据保结束：\n");
                    }

                }


                JSONObject resultJson = JSONObject.parseObject(respStr);
                if (resultJson != null) {
                    if ("S0000".equals(resultJson.getString("code"))) {
                        UserRiskDataMongoBean userRiskDataMongoBean = new UserRiskDataMongoBean();
                        //存id
                        userRiskDataMongoBean.setId(riskDataReqParam.getId());
                        userRiskDataMongoBean.setUserId(riskDataReqParam.getUserId());
                        //业务线
                        userRiskDataMongoBean.setTerminalid("1001");
                        //厂商
                        userRiskDataMongoBean.setChannel(RiskChannelTypeEnum.SHUIXIANG.name());
                        //数据类型
                        userRiskDataMongoBean.setType(RiskDataTypeEnum.DECISIONSCORE.name());
                        //创建时间
                        userRiskDataMongoBean.setCreatetime(new Date());
                        JSONObject data = resultJson.getJSONObject("data");
                        if (data != null) {
                            String url = data.getString("reportUrl");
                            if (url != null && url.length() > 0) {
                                //请求成功结果
                                userRiskDataMongoBean.setData(loadJsonStrs(url));
                            } else {
                                return;
                            }

                        }
                        //存mongoDB
                        logger.debug("userRiskDataMongoBean={}",userRiskDataMongoBean);
                        userRiskDataMongoDao.sava(userRiskDataMongoBean);
                        //状态1-已处理
                        userRiskDataResultBean.setStatus(1);
                        //是否收费 1-收费
                        userRiskDataResultBean.setPricingStrategy(1);
                        //更新result表
                        logger.info("userRiskDataResultBean={}",userRiskDataResultBean);
                        userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("错误异常信息：" + e.getMessage());
        }
        logger.info("用户 userId={} 调用SHUIXIANG_DECISIONSCORE接口服务结束", riskDataReqParam.getUserId());
    }

    @Override
    public void callRiskDataAsycResult(UserRiskDataResultBean userRiskDataResultBean) {
        return;
    }

    private String loadJsonStrs(String url) {
        StringBuilder json = new StringBuilder();
        try {
            URL urlObject = new URL(url);
            URLConnection uc = urlObject.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

}
*/
