package com.bonc.broker.service;

/**
 * @author xingej
 * @param <T>
 */
public interface ILvmWorker<T> {
    /**
     * 注册LVM
     * @param object
     */
    void registerLvm(T object);

    /**
     * 删除lvm
     * @param object
     */
    void delLvm(T object);

}
