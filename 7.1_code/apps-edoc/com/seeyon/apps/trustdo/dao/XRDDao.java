package com.seeyon.apps.trustdo.dao;

import java.util.List;
import java.util.Map;

import com.seeyon.apps.trustdo.po.XRDUserPO;
import com.seeyon.ctp.util.FlipInfo;

public interface XRDDao {

	/**
	 * 保存致远-手机盾绑定关系对象
	 * @param xrdPo
	 */
	public void save(XRDUserPO xrdPo);
	
	/**
	 * 删除致远-手机盾绑定关系对象
	 * @param xrdPo
	 */
	public void del(XRDUserPO xrdPo);
	
	/**
	 * 获取致远-手机盾绑定关系列表
	 * @param conditions
	 * @param fi
	 * @return
	 */
	public List<XRDUserPO> getAll(Map<String, Object> conditions, FlipInfo fi);
	
	/**
	 * 获取致远-手机盾绑定关系对象
	 * @param id
	 * @return
	 */
	public XRDUserPO getOneByid(Long id);
	
	/**
	 * 根据手机盾帐号获取致远-手机盾绑定关系对象
	 * @param account
	 * @return
	 */
	public List<XRDUserPO> getOneByTrustdoAccount(String account);
}
