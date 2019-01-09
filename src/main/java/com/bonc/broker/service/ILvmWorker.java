package com.bonc.broker.service;

import com.bonc.broker.exception.BrokerException;

/**
 * @author xingej
 * @param <T>
 */
public interface ILvmWorker<T> {
    /**
     * 注册LVM
     * @param object
     */
    void registerLvm(T object) throws BrokerException;

    /**
     * 删除lvm
     * @param object
     */
    void delLvm(T object);

}
