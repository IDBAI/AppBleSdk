package com.revenco.database.bean;

import java.io.Serializable;

/**
 * <p>PROJECT : AppBleSdk</p>
 * <p>COMPANY : wanzhong</p>
 * <p>AUTHOR : Administrator on 2017-03-30 15:21.</p>
 * <p>CLASS DESCRIBE :</p>
 * <p>CLASS_VERSION : 1.0.0</p>
 */
public class UserBean implements Serializable {
    private static final long serialVersionUID = 3943267343179492569L;
    public int ID;
    /**
     *
     */
    public String userId;
    /**
     * 手机号码
     */
    public String mobileNum;
    /**
     * 小区ID
     */
    public String communityId;
    /**
     * 保留字段
     */
    public String tag;
}
