/**
 * 
 */
package com.bonc.broker.service.model.redis;

import com.bonc.broker.service.model.base.BaseClusterList;

import java.util.List;

/**
 * @author Yx Liu
 * @date 2018年6月6日
 *
 */
public class RedisList extends BaseClusterList {
    private List<RedisCluster> items;

    /**
     * @return the items
     */
    public List<RedisCluster> getItems() {
        return items;
    }

    /**
     * @param items
     *            the items to set
     */
    public void setItems(List<RedisCluster> items) {
        this.items = items;
    }

}
