/*
 * Created on 2004-7-11
 *
 */
package net.joinwork.bpm.engine.wapi;

import com.seeyon.ctp.workflow.exception.BPMException;

/**
 * 事务操作的简化接口.
 * 根据配置不同，可能是JDBC事务，也可能是JTA事务.
 * @author dinghong
 * @version 1.00
 */
public interface Transaction {
	/**
	 * 提交一个事务
	 * @throws BPMException
	 */
	public void commit()throws BPMException;
	/**
	 * 回滚一个事务
	 * @throws BPMException
	 */
	public void rollback()throws BPMException;
}
