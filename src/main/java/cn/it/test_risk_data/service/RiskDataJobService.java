package cn.it.test_risk_data.service;

/**
 * 第三方厂商数据定时类
 */
public interface RiskDataJobService {
    // 定时任务1 用户信息扫描到result表
    void scanUserInfo();

    //定时任务2 操作result表
    void dealUser();

    //定时任务3 处理等待状态
    void dealWaitStatus();
}
