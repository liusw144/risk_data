package cn.it.test_risk_data.job;

import cn.it.test_risk_data.service.RiskDataJobService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.log.XxlJobLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RiskDataJob {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RiskDataJobService riskDataJobService;

    /**
     * 定时任务1 将用户信息info表扫描到result表
     */
    @XxlJob("riskDataScanJobHandler")
    public ReturnT<String> riskDataScanJobHandler(String param) throws Exception {
        String msgPrefix = "[riskDataService]";
        logger.info("{}processing start", msgPrefix);
        XxlJobLogger.log("{}processing start", msgPrefix);
        logger.info("{}processing start", msgPrefix);

        try {
            riskDataJobService.scanUserInfo();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            }
            XxlJobLogger.log("{}processing failed", msgPrefix);
            logger.info("{}processing failed", msgPrefix);
            logger.error(e.getMessage(), e);
            return ReturnT.FAIL;
        }
        XxlJobLogger.log("{}processing finish", msgPrefix);
        logger.info("{}processing finish", msgPrefix);
        return ReturnT.SUCCESS;
    }

    /**
     * 任务2 处理result表未处理的数据
     */
    @XxlJob("riskDataDealJobHandler")
    public ReturnT<String> riskDataDealJobHandler(String param) throws Exception {
        String msgPrefix = "[riskDataService]";
        logger.info("{}processing start", msgPrefix);
        XxlJobLogger.log("{}processing start", msgPrefix);
        logger.info("{}processing start", msgPrefix);

        try {
            riskDataJobService.dealUser();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            }
            XxlJobLogger.log("{}processing failed", msgPrefix);
            logger.info("{}processing failed", msgPrefix);
            logger.error(e.getMessage(), e);
            return ReturnT.FAIL;
        }
        XxlJobLogger.log("{}processing finish", msgPrefix);
        logger.info("{}processing finish", msgPrefix);
        return ReturnT.SUCCESS;
    }

    /**
     * 定时任务3 将等待结果状态进行继续处理
     */
    @XxlJob("riskDataDealWaitJobHandler")
    public ReturnT<String> riskDataDealWaitJobHandler(String param) throws Exception {
        String msgPrefix = "[riskDataService]";
        logger.info("{}processing start", msgPrefix);
        XxlJobLogger.log("{}processing start", msgPrefix);
        logger.info("{}processing start", msgPrefix);

        try {
            riskDataJobService.dealWaitStatus();
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw e;
            }
            XxlJobLogger.log("{}processing failed", msgPrefix);
            logger.info("{}processing failed", msgPrefix);
            logger.error(e.getMessage(), e);
            return ReturnT.FAIL;
        }
        XxlJobLogger.log("{}processing finish", msgPrefix);
        logger.info("{}processing finish", msgPrefix);
        return ReturnT.SUCCESS;
    }


}
