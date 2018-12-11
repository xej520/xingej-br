/**
 * 
 */
package com.bonc.broker.service.model.mysql;

/**
 * @author LYX
 *
 */
public class ClusterOp {
    private String operator;
    private String master;

    /**
     * @return the master
     */
    public String getMaster() {
        return master;
    }

    /**
     * @param master
     *            the master to set
     */
    public void setMaster(String master) {
        this.master = master;
    }

    /**
     * @return the operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @param operator
     *            the operator to set
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

}
