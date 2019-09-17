package com.seeyon.apps.trustdo.manager;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.trustdo.po.XRDUserPO;
import com.seeyon.ctp.util.FlipInfo;

public interface XRDManager {

	/**
	 * 保存致远-手机盾绑定关系对象
	 * @param xrdPo
	 */
	public void save(XRDUserPO xrdPo);
	
	/**
	 * 批量保存致远-手机盾绑定关系对象
	 * @param xrdPoList
	 */
	public void saveList(List<XRDUserPO> xrdPoList);
	
	/**
	 * 删除致远-手机盾绑定关系对象
	 * @param xrdPo
	 */
	public void del(XRDUserPO xrdPo);
	
	/**
	 * 批量删除致远-手机盾绑定关系对象
	 * @param appID
	 * @return
	 */
	public int delList(List<String> appID);
	
	/**
	 * 获取致远-手机盾绑定关系对象列表
	 * @param info
	 * @param params
	 * @return
	 */
	public FlipInfo getAll(FlipInfo info, Map<String, Object> params);
	
	/**
	 * 获取致远-手机盾绑定关系对象
	 * @param id
	 * @return
	 */
	public XRDUserPO get(String id);
	
	/**
	 * 建立绑定关系：用于相应绑定关系表单的建立绑定请求
	 * @param authedScopeIds
	 * @return
	 */
	public String bind(JSONObject authedScopeIds);
	
}
