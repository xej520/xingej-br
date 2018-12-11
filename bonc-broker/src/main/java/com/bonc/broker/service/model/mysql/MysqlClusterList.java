/**
 * 
 */
package com.bonc.broker.service.model.mysql;

import com.bonc.broker.service.model.base.BaseClusterList;

import java.util.List;


/**
 * @author Yx Liu
 * @date 2018年6月6日
 *
 */
public class MysqlClusterList extends BaseClusterList {

    private List<MysqlCluster> items;

    /**
     * @return the items
     */
    public List<MysqlCluster> getItems() {
        return items;
    }

    /**
     * @param items
     *            the items to set
     */
    public void setItems(List<MysqlCluster> items) {
        this.items = items;
    }

}
