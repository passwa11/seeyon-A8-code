package com.seeyon.apps.doc.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.seeyon.apps.doc.bo.DocLibBO;
import com.seeyon.apps.doc.bo.DocResourceBO;
import com.seeyon.apps.doc.bo.DocTreeBO;
import com.seeyon.apps.doc.manager.DocActionManager;
import com.seeyon.apps.doc.manager.DocFilingManager;
import com.seeyon.apps.doc.manager.DocHierarchyManager;
import com.seeyon.apps.doc.manager.DocLibManager;
import com.seeyon.apps.doc.manager.DocMimeTypeManager;
import com.seeyon.apps.doc.manager.KnowledgeFavoriteManager;
import com.seeyon.apps.doc.manager.KnowledgeManager;
import com.seeyon.apps.doc.manager.KnowledgeRise;
import com.seeyon.apps.doc.po.DocLibPO;
import com.seeyon.apps.doc.po.DocMimeTypePO;
import com.seeyon.apps.doc.po.DocResourcePO;
import com.seeyon.apps.doc.util.Constants;
import com.seeyon.apps.doc.util.DocMgrUtils;
import com.seeyon.apps.doc.util.KnowledgeIntegralPropertiesUtil;
import com.seeyon.apps.doc.vo.DocTreeVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.Strings;

public class DocApiImpl extends AbstractDocApi implements DocApi {

    private DocHierarchyManager      docHierarchyManager;
    private DocLibManager            docLibManager;
    private DocFilingManager         docFilingManager;
    private DocActionManager         docActionManager;
    private KnowledgeFavoriteManager knowledgeFavoriteManager;
    private KnowledgeRise            knowledgeRise;
    private KnowledgeManager         knowledgeManager;
    private DocMimeTypeManager       docMimeTypeManager;

    public void setDocHierarchyManager(DocHierarchyManager docHierarchyManager) {
        this.docHierarchyManager = docHierarchyManager;
    }

    public void setDocLibManager(DocLibManager docLibManager) {
        this.docLibManager = docLibManager;
    }

    public void setDocFilingManager(DocFilingManager docFilingManager) {
        this.docFilingManager = docFilingManager;
    }

    public void setDocActionManager(DocActionManager docActionManager) {
        this.docActionManager = docActionManager;
    }

    public void setKnowledgeFavoriteManager(KnowledgeFavoriteManager knowledgeFavoriteManager) {
        this.knowledgeFavoriteManager = knowledgeFavoriteManager;
    }

    public void setKnowledgeRise(KnowledgeRise knowledgeRise) {
        this.knowledgeRise = knowledgeRise;
    }

    public void setKnowledgeManager(KnowledgeManager knowledgeManager) {
        this.knowledgeManager = knowledgeManager;
    }

    public void setDocMimeTypeManager(DocMimeTypeManager docMimeTypeManager) {
        this.docMimeTypeManager = docMimeTypeManager;
    }

    @Override
    public List<Long> findFavoriteByType(Long memberId, Long frType) throws BusinessException {
        DocLibPO docLibPO = docLibManager.getPersonalLibOfUser(memberId);
        return knowledgeFavoriteManager.findFavoriteByType(docLibPO.getId(), frType);
    }

    @Override
    public Map<Long, Date> findFavoritesByType(Long memberId, Long frType) throws BusinessException {
        DocLibPO docLibPO = docLibManager.getPersonalLibOfUser(memberId);
        return knowledgeFavoriteManager.findFavoritesByType(docLibPO.getId(), frType);
    }

    @Override
    public DocLibBO getPersonalLibOfUser(Long memberId) throws BusinessException {
        DocLibPO docLibPO = docLibManager.getPersonalLibOfUser(memberId);
        return DocMgrUtils.docLibPOToBO(docLibPO);
    }

    @Override
    public List<Long> findDocLibsByOwner(Long memberId) throws BusinessException {
        return docLibManager.getLibsByOwner(memberId);
    }

    @Override
    public List<DocLibBO> findDocLibs(Long memberId, Long accountId) throws BusinessException {
        List<DocLibBO> docLibBO = new ArrayList<DocLibBO>();
        List<DocLibPO> docLibPOs = docLibManager.getDocLibsByUserId(memberId, accountId);
        for (DocLibPO docLibPO : docLibPOs) {
            docLibBO.add(DocMgrUtils.docLibPOToBO(docLibPO));
        }
        return docLibBO;
    }

    @Override
    public DocResourceBO getDocResource(Long id) throws BusinessException {
        DocResourcePO docResourcePO = docHierarchyManager.getDocResourceById(id);
        return DocMgrUtils.docResourcePOToBO(docResourcePO);
    }

    @Override
    public String getDocResourceName(Long id) throws BusinessException {
        return docHierarchyManager.getNameById(id);
    }

    @Override
    public boolean isDocResourceExisted(Long id) throws BusinessException {
        return docHierarchyManager.docResourceExist(id);
    }

    @Override
    public void deleteDocResources(Long memberId, List<Long> sourceIds) throws BusinessException {
        docHierarchyManager.deleteDocByResources(sourceIds, memberId);
    }

    @Override
    public List<DocResourceBO> findDocResources(List<Long> ids) throws BusinessException {
        List<DocResourceBO> list = new ArrayList<DocResourceBO>();
        if (Strings.isNotEmpty(ids)) {
            List<DocResourcePO> poList = docHierarchyManager.getDocsByIds(ids);
            for (DocResourcePO doc : poList) {
                DocResourceBO bo = DocMgrUtils.docResourcePOToBO(doc);
                DocMimeTypePO mime = docMimeTypeManager.getDocMimeTypeById(doc.getMimeTypeId());
                if (doc.getIsFolder()) {//初始化文档图标
                    String src = mime.getIcon();
                    bo.setIcon(src.substring(0, src.indexOf("|")));
                } else {
                    bo.setIcon(mime.getIcon());
                }
                list.add(bo);
            }
        }
        return list;
    }

    @Override
    public List<DocResourceBO> findDocResourcesByType(Long folderId, List<String> types) throws BusinessException {
        List<DocResourceBO> docResourceBO = new ArrayList<DocResourceBO>();
        String newStr = "";
        for (String type : types) {
            newStr = type + ",";
        }
        List<DocResourcePO> docResourcePOs = docHierarchyManager.getDocsInFolderByType(folderId, newStr.substring(0, newStr.length() - 1));
        for (DocResourcePO docResourcePO : docResourcePOs) {
            docResourceBO.add(DocMgrUtils.docResourcePOToBO(docResourcePO));
        }
        return docResourceBO;
    }

    @Override
    public List<DocTreeBO> findShareDocs(Long memberId, Long relateId) throws BusinessException {
        List<DocTreeBO> docTreeBO = new ArrayList<DocTreeBO>();
        List<DocTreeVO> docTreeVOs = docHierarchyManager.getShareDocsByOwnerId(memberId, relateId);
        for (DocTreeVO docTreeVO : docTreeVOs) {
            docTreeBO.add(DocMgrUtils.docTreeVOToBO(docTreeVO));
        }
        return docTreeBO;
    }

    @Override
    public boolean hasDocsBySource(Long sourceId) throws BusinessException {
        return docHierarchyManager.hasDocsInProject(sourceId);
    }

    @Override
    public boolean hasOpenPermission(Long docId, Long userId) {
        return docHierarchyManager.hasOpenPermission(docId, userId);
    }

    @Override
    public String getPhysicalPath(String logicalPath, String separator, boolean needSub1, int beginIndex) throws BusinessException {
        return docHierarchyManager.getPhysicalPathDetail(logicalPath, separator, needSub1, beginIndex);
    }

    @Override
    public boolean canPigeonhole(int category) throws BusinessException {
        return Constants.canPigeonholeByAppKey(category);
    }

    @Override
    public boolean hasSamePigeonhole(Long docId, List<Long> sourceIds, int category) throws BusinessException {
        return docHierarchyManager.judgeSamePigeonhole(docId, category, sourceIds);
    }

    @Override
    public List<Long> pigeonhole(Long memberId, int category, List<Long> sourceIds, List<Boolean> hasAttachments, Long docLibId, Long destFolderId, Integer pigeonholeType) throws BusinessException {
        return docFilingManager.pigeonholeAsLink(category, sourceIds, hasAttachments, docLibId, destFolderId, memberId, pigeonholeType);
    }

    @Override
    public Long getPigeonholeFolder(Long destFolderId, String childFolderName, boolean isCreate) throws BusinessException {
        return docFilingManager.getPigeonholeFolder(destFolderId, childFolderName, isCreate);
    }

    @Override
    public Long pigeonholeWithoutAcl(Long memberId, Integer category, Long sourceId, boolean hasAttachments, Long destFolderId, Integer pigeonholeType, String keyWord) throws BusinessException {
        return docFilingManager.pigeonholeAsLinkWithoutAcl(category, sourceId, hasAttachments, null, destFolderId, memberId, pigeonholeType, keyWord);
    }

    @Override
    public Long attachmentPigeonhole(V3XFile v3xFile, Long destFolderId, Long memberId, Long accountId, boolean needClone, String keyWord, Integer pigeonholeType) throws BusinessException {
        return docFilingManager.attachmentPigeonhole(v3xFile, destFolderId, memberId, accountId, needClone, keyWord, pigeonholeType);
    }

    @Override
    public void moveWithoutAcl(Long memberId, Long sourceId, Long destParentId) throws BusinessException {
        docFilingManager.moveDocWithoutAcl(memberId, sourceId, destParentId);
    }

    @Override
    public void updatePigehole(Long memberId, Long sourceId, int category) throws BusinessException {
        docFilingManager.updatePigeHoleFile(sourceId, category, memberId);
    }

    @Override
    public List<Map<String, Long>> findFavorites(Long memberId, List<Long> docIds) throws BusinessException {
        return knowledgeFavoriteManager.getFavoriteSource(docIds, memberId);
    }

    @Override
    public Long insertDocAction(Long actionUserId, Long userAccountId, Date actionTime, Integer actionType, Long subjectId, String description) throws BusinessException {
        return docActionManager.insertDocAction(actionUserId, userAccountId, actionTime, actionType, subjectId, description);
    }

    @Override
    public Long getKnowledgeRiseCount(Long acountId, Date sdate, Date edate) throws BusinessException {
        return knowledgeRise.getKnowledgeRiseCount(acountId, sdate, edate);
    }

    @Override
    public boolean isAllAccountExclude(Long accountId) throws BusinessException {
        return KnowledgeIntegralPropertiesUtil.isAllAccountExclude(accountId);
    }

    @Override
    public Set<Long> getMemberIdsByAccount(Long accountId) throws BusinessException {
        return KnowledgeIntegralPropertiesUtil.getMemberIdsByAccount(accountId);
    }

    @Override
    public Long favorite(Long memberId, Long accountId, Long sourceId, Integer favoriteType, Integer appKey, Boolean hasAtt) throws BusinessException {
        if (favoriteType == null) {//默认为正文收藏
            favoriteType = 3;
        }

        Long rootId = knowledgeManager.getMyDocRootFolder(memberId).getId();
        return knowledgeFavoriteManager.saveDocFavorite(appKey, sourceId, hasAtt, rootId, "", memberId, accountId, favoriteType);
    }

    @Override
    public Boolean cancelFavorite(Long docId, Long sourceId) throws BusinessException {
        docId = docId == null ? -1L : docId;
        return knowledgeFavoriteManager.favoriteCancel(docId, sourceId);
    }

    @Override
    public DocResourceBO getProjectFolderByProjectId(Long projectId) throws BusinessException {
        DocResourcePO po = docHierarchyManager.getProjectFolderByProjectId(projectId);
        return DocMgrUtils.docResourcePOToBO(po);
    }
    
    @Override
	public void updateDocResourceFRNameByColSummaryId(String frname, Long summaryId) {
		docHierarchyManager.updateDocResourceFRNameByColSummaryId(frname,summaryId);
	}

	@Override
	public void updateDocMetadataAvarchar1ByColSummaryId(String avarchar1, Long summaryId) {
		docHierarchyManager.updateDocMetadataAvarchar1ByColSummaryId(avarchar1,summaryId);
	}
	
	public int getNextDocNum(Long docId) throws BusinessException{
	    return docHierarchyManager.getNextDocNum(docId);
	}

}
