package cn.it.test_risk_data.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 第三方数据工厂类
 */

@Component
public class RiskDataFactoryService implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Map<String, RiskDataService> serviceMap;

    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void init() {
        readWriteLock.readLock().lock();
        try {
            if (serviceMap == null) {
                readWriteLock.readLock().unlock();
                readWriteLock.writeLock().lock();
                try {
                    if (serviceMap == null) {
                        serviceMap = new HashMap<>(100);
                        Map<String, RiskDataService> maps =
                                this.applicationContext.getBeansOfType(RiskDataService.class);
                        maps.forEach((name, service) -> {
                            serviceMap.put(service.getServiceName().name(), service);
                        });
                    }
                } finally {
                    readWriteLock.readLock().lock();
                    readWriteLock.writeLock().unlock();
                }
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public RiskDataService getService(String serviceName) {
        init();
        return serviceMap.get(serviceName);
    }

    public boolean hasService(String serviceName) {
        return this.getService(serviceName) != null;
    }

}
