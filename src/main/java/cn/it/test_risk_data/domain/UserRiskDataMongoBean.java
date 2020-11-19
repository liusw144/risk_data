package cn.it.test_risk_data.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : split
 * @version : 1.0
 * @Description :
 * @Copyright : Sinaif Software Co.,Ltd.Rights Reserved
 * @Company : 海南新浪爱问普惠科技有限公司
 * @CreateDate : 2018年7月24日 下午2:07:03
 */
@Document(collection = "userRiskDataMongoBean")
@Data
public class UserRiskDataMongoBean implements Serializable {
    //主键id
    @Id
    public String id;
    //终端产品业务线id
    private String terminalid;
    //用户id
    private String userId;
    //厂商
    private String channel;
    //报告类型
    private String type;
    //响应结果
    private String data;
    //备注
    private String remark;
    //更新时间
    private Date updatetime;
    //创建时间
    private Date createtime;

}
