/**
 * 
 */
package com.bonc.broker.service.model.mysql;

/**
 * @author LYX
 *
 */
public class NodeOp {
    private String operator;
    private String nodename;

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

    /**
     * @return the nodename
     */
    public String getNodename() {
        return nodename;
    }

    /**
     * @param nodename
     *            the nodename to set
     */
    public void setNodename(String nodename) {
        this.nodename = nodename;
    }

}
