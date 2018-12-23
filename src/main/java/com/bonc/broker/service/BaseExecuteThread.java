package com.bonc.broker.service;

import com.bonc.broker.common.ExecuteHelper;
import com.bonc.broker.common.ParameterCheckingHelp;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xingej
 */
public abstract class BaseExecuteThread<Map> implements Runnable {
    @Autowired
    protected ParameterCheckingHelp parameterCheckingHelp;

    @Autowired
    public ExecuteHelper executeHelper;

    protected Map data = null;

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

    @Override
    public void run() {
        execute();
    }

    /**
     * 具体业务流程
     */
    protected abstract void execute();
}


