/*
package cn.it.test_risk_data.service.impl;

import cn.it.test_risk_data.enums.RiskChannelDataTypeEnum;
import cn.it.test_risk_data.service.RiskDataService;
import com.alibaba.fastjson.JSONObject;
import com.sinaif.king.common.security.MD5Utils;
import com.sinaif.king.common.task.TaskQueueManager;
import com.sinaif.king.common.utils.HttpClientUtils;
import com.sinaif.king.common.utils.StringUtils;
import com.sinaif.king.dao.data.UserRiskDataResultBeanMapper;
import com.sinaif.king.enums.app.DeviceUploadTypeEnum;
import com.sinaif.king.enums.finance.message.LogReqResultBean;
import com.sinaif.king.enums.terminal.TerminalEnum;
import com.sinaif.king.model.app.device.UserSmsBean;
import com.sinaif.king.model.device.UploadDeviceMongoBean;
import com.sinaif.king.model.finance.data.UserRiskDataMongoBean;
import com.sinaif.king.model.finance.data.UserRiskDataResultBean;
import com.sinaif.king.mongo.UserRiskDataMongoDao;
import com.sinaif.king.service.message.LogRequestService;
import com.sinaif.king.service.remote.IResourceRemoteService;
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

*/
/**
 * 主序短信报告
 *//*

@Service
public class ZhuXuSmsServiceImpl implements RiskDataService {
    private static final Logger logger = LoggerFactory.getLogger(ZhuXuSmsServiceImpl.class);

    @Override
    public RiskChannelDataTypeEnum getServiceName() {
        return RiskChannelDataTypeEnum.ZHUXU_SMSREPORT;//回调其他接口获取结果
    }

    //账号
    @Value("${zhuxu.account:xxxx}")
    private String productNo;

    //密钥
    @Value("${zhuxu.password:xxxx}")
    private String md5SignKey;

    //短信报告url
    @Value("${zhuxu.sms:xxxx}")
    private String smsUrl;

    @Autowired
    private UploadDeviceResultService uploadDeviceResultService;

    @Autowired
    private LogRequestService logRequestService;

    @Autowired
    private UserRiskDataResultBeanMapper userRiskDataResultBeanMapper;

    @Autowired
    private UserRiskDataMongoDao userRiskDataMongoDao;

    @Override
    public void callRiskData(RiskDataReqParam riskDataReqParam) {

        try {
            logger.info("用户 userId={} 开始调用ZHUXU_SMSREPORT接口", riskDataReqParam.getUserId());
            if (riskDataReqParam != null) {
                //信息更新result表
                UserRiskDataResultBean userRiskDataResultBean = new UserRiskDataResultBean();
                //表主键id
                userRiskDataResultBean.setId(riskDataReqParam.getId());
                JSONObject requestData = new JSONObject();
                requestData.put("call", "openapi.applySmsReport");
                String phoneNo = riskDataReqParam.getPhoneNumber();
                if (StringUtils.isBlank(phoneNo)) {
                    logger.info("phoneNo是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("phoneNo是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                requestData.put("phoneNo", phoneNo);
                JSONObject sign = getSign(requestData);

                //获取短信
                List<UploadDeviceMongoBean> smsMongoList = uploadDeviceResultService.queryUploadRecord(riskDataReqParam.getUserId(), DeviceUploadTypeEnum.SMS.val, "1000");
                List<Map<String, Object>> resultList = new ArrayList<>();
                if (smsMongoList == null || smsMongoList.size() <= 0) {
                    logger.info("短信是必选参数");
                    userRiskDataResultBean.setStatus(4);
                    userRiskDataResultBean.setRemark("短信是必选参数");
                    userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    return;
                }
                for (UploadDeviceMongoBean smsMongo : smsMongoList) {
                    if (smsMongo == null || StringUtils.isEmpty(smsMongo.getContent())) {
                        continue;
                    }

                    List<UserSmsBean> userSmsList = JSONObject.parseArray(smsMongo.getContent(), UserSmsBean.class);
                    if (userSmsList == null || userSmsList.isEmpty()) {
                        continue;
                    }

                    for (UserSmsBean userSms : userSmsList) {
                        if (userSms == null || resultList.contains(userSms)) {
                            continue;
                        }
                        String smscontent = userSms.getSmscontent();
                        String smstime = userSms.getSmstime();

                        if (StringUtils.isBlank(smscontent) || StringUtils.isBlank(smstime)) {
                            continue;
                        }
                        Map map = new HashMap<String, Object>();
                        //厂商确认过address可以不传
                        map.put("address", "");
                        //必要
                        map.put("body", smscontent);
                        //必要
                        map.put("date", smstime);
                        resultList.add(map);
                    }
                }

                Map<String, Object> smsData = new HashMap<>();
                smsData.put("smsInfo", resultList);
                smsData.put("phoneNo", phoneNo);
                sign.put("smsData", smsData);
                String jsonStr = sign.toJSONString();
                logger.info("请求body：" + jsonStr);

                // 接口请求响应信息记录到日志服务
                LogReqResultBean request = new LogReqResultBean();
                request.setProductid("RISK_DATA");
                request.setTerminalid(TerminalEnum.FASTCASH.getCode());
                request.setBusinessid(riskDataReqParam.getId());
                request.setBusinesstype(getServiceName().name());
                request.setRequesttime(new Date());
                request.setRequestbody(jsonStr);
                request.setRequestdesc(smsUrl);
                logger.info("请求url：" + smsUrl);
                String respStr;
                try {
                    respStr = HttpClientUtils.sendPostByJsonAndHeader(smsUrl, null, jsonStr);//响应
                    logger.info("response：" + respStr);
                    request.setResponsetime(new Date());
                    request.setResponsebody(respStr);
                    JSONObject responseObject = JSONObject.parseObject(respStr);

                    if (responseObject != null) {
                        String status = responseObject.getString("code");
                        String message = responseObject.getString("msg");
                        request.setResponseid(status);
                        request.setRemark(message);
                        if ("success".equals(message)) {
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
           */
/* {
                "code": "0000",
                    "msg": "success",
                    "data": {
                "orderNo": "b0dfe81c0c7d4bc1a7b6b9355388613a"
            },
                "timestamp": 1601017412707
            }*//*


                String orderNo = "";
                if (resultJson != null) {
                    //msg为success为成功
                    if ("success".equals(resultJson.getString("msg"))) {
                        JSONObject data = resultJson.getJSONObject("data");
                        if (data != null) {
                            orderNo = data.getString("orderNo");
                        }

                        //状态2-处理中
                        userRiskDataResultBean.setStatus(2);
                        //存callInfo
                        userRiskDataResultBean.setCallInfo(orderNo);
                        */
/*//*
/是否收费 0-免费 1-收费
                        userRiskDataResultBean.setPricingStrategy(1);*//*

                        //更新时间
                        userRiskDataResultBean.setUpdateTime(new Date());
                        //信息更新
                        logger.info("userRiskDataResultBean={}", userRiskDataResultBean);
                        userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    }

                    if ("9999".equals(resultJson.getString("code"))) {
                        //状态6-未查到相关信息
                        userRiskDataResultBean.setStatus(6);
                        //是否收费 0-免费 1-收费
                        userRiskDataResultBean.setPricingStrategy(0);
                        userRiskDataResultBean.setRemark("该用户信息报告不存在" + resultJson.getString("msg"));
                        //更新时间
                        userRiskDataResultBean.setUpdateTime(new Date());
                        //信息更新
                        logger.info("userRiskDataResultBean={}", userRiskDataResultBean);
                        userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("异常错误：" + e.getMessage());
        }
        logger.info("用户 userId={} 调用YUSHIFINTECH_BLACKLIST接口结束", riskDataReqParam.getUserId());

    }


    @Override
    public void callRiskDataAsycResult(UserRiskDataResultBean userRiskDataResultBean) {

        try {
            logger.info("用户 userId={} 开始调用ZHUXU_SMSREPORT回调接口服务", userRiskDataResultBean.getUserId());
            //回调逻辑
            if (userRiskDataResultBean != null) {
                JSONObject requestData = new JSONObject();
                requestData.put("call", "openapi.getSmsReport");
                requestData.put("orderNo", userRiskDataResultBean.getCallInfo());

                JSONObject sign1 = getSign(requestData);
                String jsonStr = sign1.toJSONString();
                logger.info("请求body={}", jsonStr);
                // 接口请求响应信息记录到日志服务
                LogReqResultBean request = new LogReqResultBean();
                request.setProductid("RISK_DATA");
                request.setTerminalid(TerminalEnum.FASTCASH.getCode());
                request.setBusinessid(userRiskDataResultBean.getId());
                request.setBusinesstype(getServiceName().name());
                request.setRequesttime(new Date());
                request.setRequestbody(jsonStr);
                request.setRequestdesc(smsUrl);
                logger.info("请求url={}", smsUrl);
                String respStr = null;
                try {
                    respStr = HttpClientUtils.sendPostByJsonAndHeader(smsUrl, null, jsonStr);//响应
                    request.setResponsetime(new Date());
                    request.setResponsebody(respStr);
                    JSONObject responseObject = JSONObject.parseObject(respStr);

                    if (responseObject != null) {
                        String status = responseObject.getString("code");
                        String message = responseObject.getString("msg");
                        request.setResponseid(status);
                        request.setRemark(message);
                        if ("success".equals(message)) {
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
                    //throw e;
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
           */
/* {
                "code": "0000",
                    "msg": "success",
                    "data": {
                    "url": "http://sensorsart-crawler-oss.oss-ap-south-1.aliyuncs.com/crawler/report/test/13366093860_e990ca82c8da4d08b74c55ee0777d1f2_report?Expires=1601190438&OSSAccessKeyId=LTAI4GExdr4q9UMKRwJmYvGp&Signature=saLT2Ic0ay4CTfZB%2FRxfte0mCJI%3D",
                        "status": 2,
                        "expireTime": "2020-09-27 15:07:18"
            },
                "timestamp": 1601017577650
            }*//*


                if (resultJson != null) {
                    if ("success".equals(resultJson.getString("msg"))) {
                        JSONObject data = resultJson.getJSONObject("data");
                        if (data != null) {
                            String url = data.getString("url");
                            if (StringUtils.isBlank(url)) {
                                return;
                            }
                            //根据url下载文件 读取文件内容
                            String smsReoprtStrs = readSmsReoprt(url);
                            if (StringUtils.isBlank(smsReoprtStrs)) {
                                return;
                            }
                            //存mongodb
                            UserRiskDataMongoBean userRiskDataMongoBean = new UserRiskDataMongoBean();
                            //mongo使用result表的id
                            userRiskDataMongoBean.setId(userRiskDataResultBean.getId());
                            //存userId
                            userRiskDataMongoBean.setUserId(userRiskDataResultBean.getUserId());
                            //业务线
                            userRiskDataMongoBean.setTerminalid("1001");
                            //厂商
                            userRiskDataMongoBean.setChannel(RiskChannelTypeEnum.ZHUXU.name());
                            //数据类型
                            userRiskDataMongoBean.setType(RiskDataTypeEnum.SMSREPORT.name());

                            //创建时间
                            userRiskDataMongoBean.setCreatetime(new Date());
                            userRiskDataMongoBean.setData(smsReoprtStrs);
                            logger.debug("userRiskDataMongoBean={}", userRiskDataMongoBean);
                            userRiskDataMongoDao.sava(userRiskDataMongoBean);

                            //信息更新result表
                            //状态1-已处理
                            userRiskDataResultBean.setStatus(1);
                            //是否收费 0-免费 1-收费
                            userRiskDataResultBean.setPricingStrategy(1);
                            //更新时间
                            userRiskDataResultBean.setUpdateTime(new Date());
                            userRiskDataResultBean.setRemark("解析报告成功");
                            //记录更新时间
                            userRiskDataResultBean.setUpdateTime(new Date());
                            logger.info("userRiskDataResultBean={}", userRiskDataResultBean);
                            userRiskDataResultBeanMapper.updateByPrimaryKeySelective(userRiskDataResultBean);
                        }
                    }

                }

            }
        } catch (Exception e) {
            logger.error("异常错误信息：" + e.getMessage());
        }
        logger.info("用户 userId={} 调用ZHUXU_SMSREPORT回调接口服务结束", userRiskDataResultBean.getUserId());
    }

    private JSONObject getSign(JSONObject requestData) {
        requestData.put("productNo", productNo);
        requestData.put("timestamp", System.currentTimeMillis());
        //2.将参数排序，并做处理
        List<String> keyList = new ArrayList<String>(requestData.keySet());
        Collections.sort(keyList);
        StringBuffer signStr = new StringBuffer("");
        for (String keyStr : keyList) {
            signStr.append(keyStr);
            signStr.append("=");
            signStr.append(requestData.get(keyStr));
            signStr.append("&");
        }
        signStr.append(md5SignKey);
        //3.md5签名
        String sign = MD5Utils.md5(signStr.toString());
        //4.set md5签名值到请求参数对象
        requestData.put("sign", sign);
        return requestData;
    }

    */
/**
     * @param
     * @return 根据url将文件读取文件内容
     *//*

    private String readSmsReoprt(String url) {

        StringBuffer str = new StringBuffer();
        File file = StringUtils.downloadFile1(url);
        BufferedReader bufr = null;
        try {
            FileReader fr2 = new FileReader(file);
            bufr = new BufferedReader(fr2);
            String line = null;
            //BufferedReader提供了按行读取文本文件的方法readLine()
            //readLine()返回行有效数据，不包含换行符，未读取到数据返回null
            while ((line = bufr.readLine()) != null) {
                str.append(line);
            }
        } catch (IOException e) {
            System.out.println("异常：" + e.toString());
        } finally {
            try {
                if (bufr != null)
                    bufr.close();
            } catch (IOException e) {
                System.out.println("异常：" + e.toString());
            }
        }
        return str.toString();
    }
}
*/
