package cn.it.test_risk_data.service.impl;

import cn.it.test_risk_data.dao.UserRiskDataInfoBeanMapper;
import cn.it.test_risk_data.dao.UserRiskDataResultBeanMapper;
import cn.it.test_risk_data.domain.RiskDataReqParam;
import cn.it.test_risk_data.domain.UserRiskDataInfoBean;
import cn.it.test_risk_data.domain.UserRiskDataResultBean;
import cn.it.test_risk_data.enums.RiskChannelDataTypeEnum;
import cn.it.test_risk_data.service.RiskDataFactoryService;
import cn.it.test_risk_data.service.RiskDataJobService;
import com.sinaif.king.common.utils.SnowFlakeUtil;
import com.sinaif.king.dao.data.UserRiskDataInfoBeanMapper;
import com.sinaif.king.dao.data.UserRiskDataResultBeanMapper;
import com.sinaif.king.dao.user.UserInfoMapper;
import com.sinaif.king.enums.terminal.TerminalEnum;
import com.sinaif.king.model.app.userbase.UserCertificateInfoVO;
import com.sinaif.king.model.app.userbase.UserCommonAO;
import com.sinaif.king.model.finance.data.UserRiskDataInfoBean;
import com.sinaif.king.model.finance.data.UserRiskDataResultBean;
import com.sinaif.king.model.finance.user.UserInfo;
import com.sinaif.king.service.riskdata.RiskDataFactoryService;
import com.sinaif.king.service.riskdata.RiskDataJobService;
import com.sinaif.king.service.user.UserCertificateInfoService;
import com.sinaif.task.enums.RiskChannelDataTypeEnum;
import com.sinaif.task.vo.RiskDataReqParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class RiskDataJobServiceImpl implements RiskDataJobService {
    private static final Logger logger = LoggerFactory.getLogger(RiskDataJobServiceImpl.class);

    @Autowired
    private RiskDataFactoryService riskDataFactoryService;

    @Autowired
    private UserRiskDataInfoBeanMapper userRiskDataInfoBeanMapper;

   /* @Autowired
    private UserCertificateInfoService userCertificateInfoService;*/

//    @Autowired
//    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserRiskDataResultBeanMapper userRiskDataResultBeanMapper;

    private ExecutorService executor = new ThreadPoolExecutor(
            5,
            50,
            10L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue(500));

    @Override
    public void scanUserInfo() {
        try {
            logger.info("定时任务开始扫描用户");
            List<UserRiskDataInfoBean> userRiskDataInfoBeans = userRiskDataInfoBeanMapper.selectByProcessStatus(0, TerminalEnum.FASTCASH.getCode());
            logger.info("扫描到未用户总数：" + userRiskDataInfoBeans.size());
            RiskChannelDataTypeEnum[] values = RiskChannelDataTypeEnum.values();
            //逐一扫描插进result表
            for (UserRiskDataInfoBean userRiskDataInfoBean : userRiskDataInfoBeans) {
                logger.info("扫描到用户userId={}", userRiskDataInfoBean.getUserId());
                for (RiskChannelDataTypeEnum value : values) {
                    UserRiskDataResultBean userRiskDataResultBean = new UserRiskDataResultBean();
                    //id
                    //userRiskDataResultBean.setId(SnowFlakeUtil.generateId() + "");
                    userRiskDataResultBean.setTerminalId("1001");
                    String[] strArr = value.name().split("_");
                    //厂家
                    userRiskDataResultBean.setChannelName(strArr[0].trim());
                    //类型
                    userRiskDataResultBean.setDataType(strArr[1].trim());
                    //用户id
                    userRiskDataResultBean.setUserId(userRiskDataInfoBean.getUserId());
                    //存result表
                    userRiskDataResultBeanMapper.insertSelective(userRiskDataResultBean);
                }
                //更新info表 1-已处扫描
                UserRiskDataInfoBean userRiskDataInfoBean1 = new UserRiskDataInfoBean();
                userRiskDataInfoBean1.setId(userRiskDataInfoBean.getId());
                userRiskDataInfoBean1.setProcessStatus(1);
                userRiskDataInfoBeanMapper.updateByPrimaryKeySelective(userRiskDataInfoBean1);
                logger.info("用户 uerId={} 已成功扫描", userRiskDataInfoBean.getUserId());
            }

        } catch (Exception e) {
            logger.info("错误异常信息：" + e.getMessage());
        }
        logger.info("定时任务扫描用户结束");
    }

    @Override
    public void dealUser() {

        try {
            logger.info("定时任务开始处理用户数据");
            long start = System.currentTimeMillis();
           /* List<UserRiskDataResultBean> userRiskDataResultBeans = userRiskDataResultBeanMapper.selectAll(0, "1001");
            logger.info("扫描到待处理用户总数：" + userRiskDataResultBeans.size());
            if (userRiskDataResultBeans != null && !userRiskDataResultBeans.isEmpty()) {
                for (UserRiskDataResultBean userRiskDataResultBean : userRiskDataResultBeans) {
                    logger.info("开始处理用户userId={}", userRiskDataResultBean.getUserId());
                    //用户信息唯一
                    //UserCertificateInfoVO userCertificateInfoVO = userCertificateInfoService.queryCertificateInfo(new UserCommonAO("1001", userRiskDataResultBean.getUserId()));
                    //UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userRiskDataResultBean.getUserId());
                    //logger.info("用户认证信息 UserCertificateInfoVO={},用户基本信息 UserInfo={}", userCertificateInfoVO, userInfo);
                    RiskDataReqParam riskDataReqParam;
                    if (userCertificateInfoVO != null) {
                        //解析用户信息存入对象
                        riskDataReqParam = new RiskDataReqParam();
                        riskDataReqParam.setId(userRiskDataResultBean.getId());
                       // riskDataReqParam.setUserId(userCertificateInfoVO.getUserId());
                        //riskDataReqParam.setGender(userCertificateInfoVO.getGender());
                       // riskDataReqParam.setAdhaarNumber(userCertificateInfoVO.getIdCardNo());
                        //riskDataReqParam.setPanNumber(userCertificateInfoVO.getPanCardNo());
                        //String fullName = userCertificateInfoVO.getIdCardName().trim();
                        //riskDataReqParam.setBirthday(userCertificateInfoVO.getBirthday());
                        riskDataReqParam.setFullName(fullName);
                        String firstName = "";
                        String middleName = "";
                        String lastName = "";
                        //数据库存全名
                        if (fullName != null && fullName.length() > 0) {
                            String[] fullNameArr = fullName.trim().split("\\s+");
                            if (fullNameArr != null && fullNameArr.length > 0) {
                                if (fullNameArr.length == 3) {
                                    firstName = fullNameArr[0];
                                    middleName = fullNameArr[1];
                                    lastName = fullNameArr[2];
                                }
                                if (fullNameArr.length == 2) {
                                    firstName = fullNameArr[0];
                                    lastName = fullNameArr[1];
                                }
                                if (fullNameArr.length == 1) {
                                    firstName = fullNameArr[0];
                                }
                            }
                        }

                        riskDataReqParam.setFirstName(firstName);
                        riskDataReqParam.setMiddleName(middleName);
                        riskDataReqParam.setLastName(lastName);

                        //获取手机号
                      *//*  if (userInfo != null) {
                            //riskDataReqParam.setPhoneNumber(userInfo.getUsername().trim().substring(2));//去掉91
                        }*//*
*/
                 /*   } else {
                        continue;*/
                   /* }
                    if (riskDataReqParam != null) {
                        logger.info("处理用户定时任务参数信息  riskDataReqParam={}", riskDataReqParam);
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                riskDataFactoryService.getService(userRiskDataResultBean.getChannelName() + "_" + userRiskDataResultBean.getDataType()).callRiskData(riskDataReqParam);
                            }
                        });
                    }
                    logger.info("处理用户userId={} 完成", userRiskDataResultBean.getUserId());*/
                }

            }
            /*long end = System.currentTimeMillis();
            logger.info("本次耗时:" + (end - start) + "毫秒");
        } catch (Exception e) {
            logger.error("异常错误信息：" + e.getMessage());

        }
        logger.info("定时任务处理用户完成");

    }*/

    @Override
    public void dealWaitStatus() {
        try {
            logger.info("开始处理异步任务");
            //定时查找0-未处理完的状态的记录
            List<UserRiskDataResultBean> userRiskDataResultBeans = userRiskDataResultBeanMapper.selectAll(2, TerminalEnum.FASTCASH.getCode());
            logger.info("扫描到待处理用户总数：" + userRiskDataResultBeans.size());
            //调用工厂模式进行数据处理
            if (userRiskDataResultBeans != null && !userRiskDataResultBeans.isEmpty()) {
                for (UserRiskDataResultBean userRiskDataResultBean : userRiskDataResultBeans) {
                    if (userRiskDataResultBean != null) {
                        logger.info("处理异步定时任务单条参数  userRiskDataResultBean={}", userRiskDataResultBean);
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                riskDataFactoryService.getService(userRiskDataResultBean.getChannelName().trim() + "_" + userRiskDataResultBean.getDataType().trim()).callRiskDataAsycResult(userRiskDataResultBean);
                            }
                        });
                    }
                }
            }

        } catch (Exception e) {
            logger.error("异常错误信息：" + e.getMessage());
        }
        logger.info("处理异步任务结束");
    }


}
