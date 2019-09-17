package com.seeyon.v3x.edoc.manager;

import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkReserve;
import com.seeyon.v3x.edoc.domain.EdocMarkReserveNumber;
import com.seeyon.v3x.edoc.webmodel.EdocMarkReserveVO;

/**
 * 预留文号管理
 * @author tanggl
 *
 */
public interface EdocMarkReserveManager {
	
	/**
	 * 重新加载预留文号的缓存
	 * 使用点：保存预留文号出异常时调用
	 */
	public void reloadCache();
	
	/**
	 * 校验预留文号值域是否重复
	 * 使用点：1文号编辑判断当前文号
	 * @param startNo
	 * @param endNo
	 * @return
	 * @throws BusinessException
	 */
	public boolean checkRepeatMarkReserved(Long markDefineId, int startNo, int endNo) throws BusinessException;
	public boolean checkRepeatMarkReserved(EdocMarkDefinition markDef, int startNo, int endNo) throws BusinessException;
	
	/**
	 * 校验预留文号值域是否重复(不包括某部分的预留文号)
	 * 使用点：1 预留文号添加删除点确定时做同步判断
	 * @param markDefineId
	 * @param startNo
	 * @param endNo
	 * @param thisReservedIdList
	 * @return
	 * @throws BusinessException
	 */
	public boolean checkRepeatMarkReserved(Long markDefineId, int startNo, int endNo, List<Long> thisReservedIdList, List<Long> delReservedIdList) throws BusinessException;
	public boolean checkRepeatMarkReserved(EdocMarkDefinition markDef, int startNo, int endNo, List<Long> thisReservedIdList, List<Long> delReservedIdList) throws BusinessException;

	/**
	 * 文号管理-设置预留文号-添加预留文号
	 * @param markDef
	 * @param addReserveList
	 * @param delReservedIdList
	 * @throws BusinessException
	 */
	public void saveEdocMarkReserve(EdocMarkDefinition markDef, List<EdocMarkReserveVO> addReserveList, List<Long> delReservedIdList) throws BusinessException;
	
	/**
	 * 文号管理-设置预留文号-添加预留文号(G6V61新公文使用方法)
	 * @param user
	 * @param type
	 * @param markDef
	 * @param addReserveList
	 * @param delReservedIdList
	 * @param delReservedNoList
	 * @throws BusinessException
	 */
	public List<EdocMarkReserveNumber> saveMarkReserverNew(User user, Integer type, EdocMarkDefinition markDef, List<EdocMarkReserveVO> addReserveList, List<Long> delReservedIdList, List<String> delReservedNoList) throws BusinessException;
	public List<EdocMarkReserveVO> deleteMarkReserveNew(User user, Integer type, EdocMarkDefinition markDef, List<Long> delReservedIdList, List<String> delReservedNoList) throws BusinessException;
	public Integer getCurrentNoForReserveNew(EdocMarkDefinition markDef) throws BusinessException;
	public void updateMarkReserveIsUsedNew(List<Long> markDefIdList, List<String> markstrList, boolean isUsed) throws BusinessException;
	
	/**
	 * 获取预留文号对象
	 * @param reserveId
	 * @return
	 * @throws BusinessException
	 */
	public EdocMarkReserve getEdocMarkReserveById(Long reserveId) throws BusinessException;
	
	/**
	 * 显示某预留文号段(按格式显示)
	 * @param markDef
	 * @return
	 * @throws BusinessException
	 */
	public Map<Long, List<EdocMarkReserveVO>> findAllEdocMarkReserveListMap() throws BusinessException;
	public List<EdocMarkReserveVO> findEdocMarkReserveList(EdocMarkDefinition markDef) throws BusinessException;
	public List<EdocMarkReserveVO> findEdocMarkReserveList(EdocMarkDefinition markDef, Integer queryNumber) throws BusinessException;
	public List<EdocMarkReserveVO> findEdocMarkReserveList(Integer type, EdocMarkDefinition markDef, Integer queryNumber) throws BusinessException;

	/**
	 * 获取文号某类型的预留文号，通过参数判断是否获取所有预留文号
	 * @param markDefineId
	 * @param type 1线上 2线下 -1所有
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkReserveNumber> findAllEdocMarkReserveNumberList() throws BusinessException;
	public List<EdocMarkReserveNumber> findEdocMarkReserveNumberList(Integer type) throws BusinessException;
	
	/**
	 * 获取某个文号的全部预留文号
	 * @Author      : xuqw
	 * @Date        : 2015年3月17日下午7:25:47
	 * @param markDef 文号对象，为null返回全部
	 * @param type 1线上 2线下 -1所有
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkReserveNumber> findEdocMarkReserveNumberList(EdocMarkDefinition markDef, Integer type) throws BusinessException;
	
	/**
	 * 获取某个文号的全部预留文号
	 * @Author      : xuqw
	 * @Date        : 2015年3月17日下午7:25:47
	 * @param markDef 文号对象，为null返回全部
	 * @param type 1线上 2线下 -1所有
	 * @return
	 * @throws BusinessException
	 */
	public List<EdocMarkReserveNumber> findEdocMarkReserveNumberListByDB(EdocMarkDefinition markDef, Integer type) throws BusinessException;
	
	
	
	/**
	 * 按格式显示某一个文号
	 * @param markDef
	 * @param markNumber 流水号
	 * @return
	 */
	public EdocMarkReserveVO getMarkReserveByFormat(EdocMarkDefinition markDef, Integer markNumber);
	
	/**
	 * 自动跳过预留文号，生成当前文号
	 * @param markDef
	 * @param currentNo
	 * @return
	 */
	public Integer autoMakeEdocMarkCurrentNo(EdocMarkDefinition markDef, Integer currentNo);
	/**
	 * aJax判断是否有预留文号
	 * @param markDefId
	 * @return
	 */
	public String hasMarkReserve(String markDefId) throws BusinessException;
	/**
	 * 根据markDefineId删除预留文号
	 * @param markDefineId
	 * @throws BusinessException
	 */
	public void deleteByMarkDefineId(Long markDefineId) throws BusinessException;
	
}
