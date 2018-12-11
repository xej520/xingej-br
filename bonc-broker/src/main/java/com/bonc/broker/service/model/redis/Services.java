/**
 * 
 */
package com.bonc.broker.service.model.redis;

/**
 * @author Yx Liu
 * @date 2018年6月6日
 *
 */
public class Services {

    private RedisServiceInfo master;
    private RedisServiceInfo slave;
    private RedisServiceInfo sentinel;

    /**
     * @return the master
     */
    public RedisServiceInfo getMaster() {
        return master;
    }

    /**
     * @param master
     *            the master to set
     */
    public void setMaster(RedisServiceInfo master) {
        this.master = master;
    }

    /**
     * @return the slave
     */
    public RedisServiceInfo getSlave() {
        return slave;
    }

    /**
     * @param slave
     *            the slave to set
     */
    public void setSlave(RedisServiceInfo slave) {
        this.slave = slave;
    }

    /**
     * @return the sentinel
     */
    public RedisServiceInfo getSentinel() {
        return sentinel;
    }

    /**
     * @param sentinel
     *            the sentinel to set
     */
    public void setSentinel(RedisServiceInfo sentinel) {
        this.sentinel = sentinel;
    }

}
