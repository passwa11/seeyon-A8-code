package com.seeyon.apps.doc.api;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.apps.doc.bo.DocLibBO;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.doc.bo.DocTreeBO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.filemanager.V3XFile;

/**
 * 知识管理接口
 */
public interface DocApi {

    /**
     * 根据文档类型获取文档库中收藏的文档
     * 
     * @param memberId 人员id
     * @param frType 文档类型
     * @throws BusinessException
     * @return
     */
    public List<Long> findFavoriteByType(Long memberId, Long frType) throws BusinessException;

    /**
     * 根据文档类型获取文档库中收藏的文档
     * 
     * @param memberId 人员id
     * @param frType 文档类型
     * @return key:收藏源id,value:收藏时间
     * @throws BusinessException
     */
    public Map<Long, Date> findFavoritesByType(Long memberId, Long frType) throws BusinessException;

    /**
     * 获取某个人的个人文档库
     * @param memberId 人员id
     * @return
     */
    public DocLibBO getPersonalLibOfUser(Long memberId) throws BusinessException;

    /**
    * 获取人员能管理的文档库id集合<br>
     *
     * 正常:<br>
     *     1、传入正确的人员id且该人员有管理的文档库，返回文档库id集合<br>
     *     2、传入正确的人员id且该人员没有管理的文档库，返回空<br>
     *
    * @param memberId 人员id
    * @return
    */
    public List<Long> findDocLibsByOwner(Long memberId) throws BusinessException;

    /**
     * 获取人员在某个单位下能访问的文档库<br>
     *
     * 正常:<br>
     *     1、传入正确的人员id和单位id且该人员有能访问的文档库，返回文档库集合<br>
     *     2、传入正确的人员id和单位id且该人员没有能访问的文档库，返回空<br>
     *
     * @param memberId 人员id
     * @param accountId 单位id
     * @return
     * @throws BusinessException
     */
    public List<DocLibBO> findDocLibs(Long memberId, Long accountId) throws BusinessException;

    /**
     * 获取文档<br>
     *
     * 正常:<br>
     *     1、传入正确的文档id，能获取到文档的实体<br>
     *
     * @param id 要获取文档的id
     * @return
     * @throws BusinessException
     */
    public DocResourceBO getDocResource(Long id) throws BusinessException;

    /**
     * 获取文档名称
     *
     * 正常:<br>
     *     1、传入正确的文档id，返回文档名称<br>
     *
     * @param id 文档id
     * @return
     * @throws BusinessException
     */
    public String getDocResourceName(Long id) throws BusinessException;

    /**
     * 判断文档是否存在<br>
     *
     * 正常:<br>
     *     1、传入正确的文档id，返回true<br>
     *     2、传入已删除的文档id，返回false<br>
     *
     * @param id 所要判断的文档id
     * @return
     */
    public boolean isDocResourceExisted(Long id) throws BusinessException;

    /**
     * 根据sourceId集合删除对应文档<br>
     *
     * 正常:<br>
     *     1、传入正确的人员id、文档sourceId集合，能删除集合里的文档<br>
     *
     * @param memberId 人员id（当前用户）
     * @param sourceIds 要删除文档的sourceId集合，如affairId
     * @throws BusinessException
     */
    public void deleteDocResources(Long memberId, List<Long> sourceIds) throws BusinessException;

    /**
     * 获取多个文档<br>
     *
     * 正常:<br>
     *     1、传入正确的文档id集合，返回文档集合<br>
     *
     * @param ids 要获取文档的id集合
     * @return
     * @throws BusinessException
     */
    public List<DocResourceBO> findDocResources(List<Long> ids) throws BusinessException;

    /**
     * 获取文档夹下符合文档类型的文档<br>
     *
     * 正常:<br>
     *     1、传入正确的文档夹id、正确的文档类型集合且文档夹下有文档，返回文档集合<br>
     *     2、传入正确的文档夹id、正确的文档类型集合且文档夹下没有文档，返回空<br>
     *
     * @param folderId 目标文档夹id
     * @param types 文档类型集合（即内容类型）
     * @return
     */
    public List<DocResourceBO> findDocResourcesByType(Long folderId, List<String> types) throws BusinessException;

    /**
     * 获取关联人员共享给我的文档<br>
     *
     * 正常:<br>
     *     1、传入正确的人员id、正确的关联人员id且有共享文档，返回文档集合<br>
     *     2、传入正确的人员id、正确的关联人员id且没有共享文档，返回空<br>
     *
     * @param memberId 人员id（当前用户）
     * @param relateId 关联人员id
     * @return
     * @throws BusinessException
     */
    public List<DocTreeBO> findShareDocs(Long memberId, Long relateId) throws BusinessException;

    /**
     * 根据文档源id判断是否有文档（不算文档夹，不论层级）
     * 如：判断一个项目或项目阶段文档夹下是否有文档<br>
     *
     * 正常:<br>
     *     1、传入正确的文档源id且文档夹下有文档，返回true<br>
     *     2、传入正确的文档源id且文档夹下没有文档，返回false<br>
     *
     * @param sourceId 文档源id，如：项目id，项目阶段id
     * @return
     */
    public boolean hasDocsBySource(Long sourceId) throws BusinessException;

    /**
     * 判断某个用户对某个文档夹是否拥有打开权限
     * @param docId 文档id
     * @param memberId 人员id
     * @return
     */
    public boolean hasOpenPermission(Long docId, Long memberId);

    /**
     * 根据逻辑路径解析成文字路径<br>
     *
     * 正常:<br>
     *     1、传入正确的所有参数，返回文字路径<br>
     *
     * @param logicalPath 逻辑路径
     * @param separator 返回值的分隔符
     * @param needSub1 解析逻辑路径的时候是否需要将逻辑路径减1
     * @param beginIndex 从逻辑路径的第几个坐标开始解析
     * @return
     */
    public String getPhysicalPath(String logicalPath, String separator, boolean needSub1, int beginIndex) throws BusinessException;

    /**
     * 判断协同、公文等模块是否可以归档<br>
     *
     * 正常:<br>
     *     1、传入可归档的模块标识（如新闻），返回true<br>
     *     2、传入不可归档的模块标识（如计划），返回false<br>
     *
     * @param category 模块标识（如协同、公文等）
     * @return
     * @throws BusinessException
     */
    public boolean canPigeonhole(int category) throws BusinessException;

    /**
     * 判断是否已经存在归档<br>
     *
     * 正常:<br>
     *     1、传入正确的所有参数且要归档的源有一个已归档，返回true<br>
     *     2、传入正确的所有参数且要归档的源都未归档，返回false<br>
     *
     * @param docId 目标文档夹id
     * @param sourceIds 要归档的源id集合
     * @param category 应用标识{@link com.seeyon.ctp.common.constants.ApplicationCategoryEnum}
     * @return 有一个存在就返回true
     */
    public boolean hasSamePigeonhole(Long docId, List<Long> sourceIds, int category) throws BusinessException;

    /**
     * 多文件归档，需要判定权限<br>
     *
     * 正常:<br>
     *     1、传入归档类型为无、模块id为非公文模块id（如协同id）、其他参数正确，返回归档后的文档id集合<br>
     *     2、传入归档类型为部门归档、模块id为公文模块id、其他参数正确，返回归档后的文档id集合<br>
     *     3、传入归档类型为单位归档、模块id为公文模块id、其他参数正确，返回归档后的文档id集合<br>
     *
     * @param memberId 人员id（当前用户）
     * @param category 应用标识{@link com.seeyon.ctp.common.constants.ApplicationCategoryEnum}
     * @param sourceIds 要归档的源id集合
     * @param hasAttachments 是否有附件集合，与的源id集合一一对应
     * @param docLibId 目标文档库id
     * @param destFolderId 目标文件夹id
     * @param pigeonholeType 0：单位归档；1：部门归档；默认为无
     * @return 归档后的文档id集合
     * @throws BusinessException
     */
    public List<Long> pigeonhole(Long memberId, int category, List<Long> sourceIds, List<Boolean> hasAttachments, Long docLibId, Long destFolderId, Integer pigeonholeType) throws BusinessException;

    /**
     * 获取子文件夹id，归档到《父文档夹-子文件夹》中<br>
     * @param destFolderId 父文档夹id
     * @param childFolderName 子文件夹名称
     * @param isCreate 如果没有子文件夹是否进行创建
     * @return
     * @throws BusinessException
     */
    public Long getPigeonholeFolder(Long destFolderId, String childFolderName, boolean isCreate) throws BusinessException;

    /**
     * 文件归档，不需要判定权限<br>
     * @param memberId 人员id（当前用户）
     * @param category 应用标识{@link com.seeyon.ctp.common.constants.ApplicationCategoryEnum}
     * @param sourceId 要归档的源id
     * @param hasAttachments 是否有附件
     * @param destFolderId 目标文档库id
     * @param pigeonholeType 归档类型，0：单位归档；1：部门归档；默认为无
     * @param keyWord 关键字
     * @return 归档后的文档id
     * @throws BusinessException
     */
    public Long pigeonholeWithoutAcl(Long memberId, Integer category, Long sourceId, boolean hasAttachments, Long destFolderId, Integer pigeonholeType, String keyWord) throws BusinessException;

    /**
     * 附件归档
     * @param v3xFile 归档的v3xFile
     * @param destFolderId 目标文档夹[非空]
     * @param user 归档用户
     * @param needClone 对v3xFile是否需要克隆
     * @param keyWord 关键字
     * @param pigeonholeType 归档类型
     * @return 归档后的文档id
     * @throws BusinessException
     */
    public Long attachmentPigeonhole(V3XFile v3xFile, Long destFolderId, Long memberId, Long accountId, boolean needClone, String keyWord, Integer pigeonholeType) throws BusinessException;

    /**
     * 移动文档到目标文档夹<br>
     * 
     * 正常:<br>
     *     1、传入正确的所有参数且目标文档夹存在，能成功移动文档<br>
     *     
     * @param memberId 人员id（当前用户）
     * @param sourceId 移动文档的源id
     * @param destFolderId 目标文档夹id
     * @throws BusinessException
     */
    public void moveWithoutAcl(Long memberId, Long sourceId, Long destFolderId) throws BusinessException;

    /**
     * 修改归档内容
     *
     * 正常:<br>
     *     1、所有参数传入正确值，能修改归档内容<br>
     *
     * @param memberId 人员id（当前用户）
     * @param sourceId 源id
     * @param category 应用标识{@link com.seeyon.ctp.common.constants.ApplicationCategoryEnum}
     * @throws BusinessException
     */
    public void updatePigehole(Long memberId, Long sourceId, int category) throws BusinessException;

    /**
     * 获取收藏文档的源id
     *
     * 正常:<br>
     *     1、所有参数传入正确值且文档集合里的文档都是被收藏的，返回文档源集合<br>
     *
     * @param memberId 人员id
     * @param docIds 文档id集合
     * @return 返回集合中map的key为收藏后的文档id，value为收藏源id(如：待办事项id)
     * @throws BusinessException
     */
    public List<Map<String, Long>> findFavorites(Long memberId, List<Long> docIds) throws BusinessException;

    /**
     * 新增操作痕迹
     *
     * 正常:<br>
     *     1、所有参数传入正确值，返回操作痕迹id<br>
     *
     * @param actionUserId 操作者  
     * @param actionTime 操作时间  
     * @param actionType 操作动作  
     * @param subjectId 操作主体 
     * @param description 操作附言  
     * @return 保存后的痕迹ID
     * @throws BusinessException
     */
    public Long insertDocAction(Long actionUserId, Long userAccountId, Date actionTime, Integer actionType, Long subjectId, String description) throws BusinessException;

    /**
     * 获取知识增长数量
     * 
     * @param acountId 单位id
     * @param sdate 开始时间
     * @param edate 结束时间
     * @return
     */
    public Long getKnowledgeRiseCount(Long acountId, Date sdate, Date edate) throws BusinessException;

    /**
     * 是否全单位不积分
     * @param accountId 单位id
     * @return
     * @throws BusinessException
     */
    public boolean isAllAccountExclude(Long accountId) throws BusinessException;

    /**
     * 获取单位下不积分的人员
     * @param accountId 单位id
     * @return
     */
    public Set<Long> getMemberIdsByAccount(Long accountId) throws BusinessException;

    /**
     * 收藏
     * @param memberId 人员id（当前用户）
     * @param accountId 单位id（当前单位）
     * @param sourceId 源id
     * @param favoriteType 收藏类型：默认正文收藏
     * @param appKey 应用key
     * @param hasAtt 是否有附件
     * @return 收藏后的文档id
     * @throws BusinessException
     */
    public Long favorite(Long memberId, Long accountId, Long sourceId, Integer favoriteType, Integer appKey, Boolean hasAtt) throws BusinessException;

    /**
     * 取消收藏
     * @param docId 文档，如果非文档，传-1或者null
     * @param sourceId 其它id
     */
    public Boolean cancelFavorite(Long docId, Long sourceId) throws BusinessException;

    /**
     * <description>获取项目对应的文件夹</description>
     *
     * @param projectId
     * @return
     * @throws BusinessException
     */
    public DocResourceBO getProjectFolderByProjectId(Long projectId) throws BusinessException;
    
    /**
     * 更新FRName
     * @param frname 
     * @param summaryId 协同ID
     */
	public void updateDocResourceFRNameByColSummaryId(String frname, Long summaryId);
	
	/**
	 * 更新Avarchar1
	 * @param Avarchar1
	 * @param summaryId 协同ID
	 */
	public void updateDocMetadataAvarchar1ByColSummaryId(String avarchar1, Long summaryId);
	/**
	 * 获取文档夹下层数量
	 * @param docId
	 * @return
	 */
	public int getNextDocNum(Long docId) throws BusinessException;

}
