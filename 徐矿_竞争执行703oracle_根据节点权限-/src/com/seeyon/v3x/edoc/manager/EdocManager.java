package com.seeyon.v3x.edoc.manager;

import com.seeyon.apps.collaboration.vo.AttachmentVO;
import com.seeyon.apps.edoc.enums.EdocEnum.SendType;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.processlog.ProcessLog;
import com.seeyon.ctp.common.track.po.CtpTrackMember;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocForm;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocRegisterCondition;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.EdocSearchModel;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.edoc.webmodel.MoreSignSelectPerson;
import com.seeyon.v3x.edoc.webmodel.SummaryModel;
import com.seeyon.v3x.isearch.model.ConditionModel;
import com.seeyon.v3x.isearch.model.ResultModel;

import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface EdocManager {
    EdocForm getNewDefaultEdocForm(int var1, Long var2, User var3);

    Long runCase(EdocSummary var1, EdocBody var2, EdocOpinion var3, SendType var4, Map var5, String var6, Long var7, boolean var8, String var9, String var10, String var11) throws BusinessException;

    Long runCase(EdocSummary var1, EdocBody var2, EdocOpinion var3, SendType var4, Map var5, String var6, Long var7, boolean var8, String var9, String var10, String var11, String var12) throws BusinessException;

    Long runCase(EdocSummary var1, EdocBody var2, EdocOpinion var3, SendType var4, Map var5, String var6, Long var7) throws EdocException;

    Long transRunCase(EdocSummary var1, EdocBody var2, EdocOpinion var3, SendType var4, Map var5, String var6, Long var7, boolean var8, String var9, String var10, String var11, String var12) throws BusinessException;

    void claimWorkItem(int var1) throws EdocException;

    String checkHasAclNodePolicyOperation(String var1, String var2) throws BusinessException;

    void zcdb(EdocSummary var1, CtpAffair var2, EdocOpinion var3, String var4, String var5, String var6, String var7, String var8, boolean var9, List<Long[]> var10) throws EdocException, BusinessException;

    void zcdb(CtpAffair var1, EdocOpinion var2, String var3, String var4, String var5, String var6, EdocSummary var7, String var8, String var9, String var10, String var11, String var12, List<Long[]> var13) throws EdocException, BusinessException;

    void deleteEdocOpinion(Long var1) throws EdocException;

    Long saveBackBox(EdocSummary var1, EdocBody var2, EdocOpinion var3) throws EdocException;

    Long saveDraft(EdocSummary var1, EdocBody var2, EdocOpinion var3, String var4) throws EdocException, BusinessException;

    List<EdocSummaryModel> queryTodoList(int var1, Map<String, Object> var2) throws EdocException;

    String checkEdocMark(Long var1, String var2, Integer var3, String var4);

    List<EdocSummaryModel> queryFinishedList(int var1, Map<String, Object> var2) throws EdocException;

    List<EdocSummaryModel> querySentList(int var1, Map<String, Object> var2) throws EdocException;

    List<EdocSummaryModel> querySentList(int var1, long var2, Map<String, Object> var4) throws EdocException;

    List<EdocSummaryModel> queryDraftList(int var1, Map<String, Object> var2) throws EdocException;

    List<EdocSummaryModel> queryDraftList(int var1, long var2, Map<String, Object> var4, int... var5) throws EdocException;

    List<EdocSummaryModel> queryTrackList(int var1, Map<String, Object> var2) throws EdocException;

    List<EdocSummaryModel> queryByCondition4Quote(ApplicationCategoryEnum var1, String var2, String var3, String var4);

    EdocSummary getEdocSummaryById(long var1, boolean var3) throws EdocException;

    EdocSummary getEdocSummaryById(long var1, boolean var3, boolean var4) throws EdocException;

    EdocSummary getColAllById(long var1) throws EdocException;

    void deleteAffair(String var1, long var2) throws Exception;

    void pigeonholeAffair(String var1, long var2, Long var4) throws EdocException, BusinessException;

    void pigeonholeAffair(String var1, CtpAffair var2, Long var3) throws EdocException;

    void pigeonholeAffair(String var1, CtpAffair var2, Long var3, Long var4, boolean var5) throws EdocException;

    void pigeonholeAffair(String var1, long var2, Long var4, Long var5) throws EdocException, BusinessException;

    void pigeonholeAffair(String var1, CtpAffair var2, Long var3, Long var4) throws EdocException;

    EdocSummary getSummaryByCaseId(long var1) throws EdocException;

    Long getSummaryIdByCaseId(long var1) throws EdocException;

    EdocSummary getSummaryByWorkItemId(int var1) throws EdocException;

    String getCaseLogXML(long var1) throws EdocException;

    String getCaseWorkItemLogXML(long var1) throws EdocException;

    String getCaseProcessXML(long var1) throws EdocException;

    int cancelSummary(long var1, long var3, CtpAffair var5, String var6, String var7, EdocOpinion... var8) throws EdocException, BusinessException;

    int edocBackCancelSummary(long var1, long var3, String var5, String var6) throws EdocException, BusinessException;

    List<EdocSummaryModel> queryByCondition(int var1, String var2, String var3, String var4, int var5, Map<String, Object> var6, int... var7);

    List<EdocSummaryModel> queryByCondition1(int var1, String var2, String var3, String var4, long var5, int var7, Map<String, Object> var8, int... var9);

    String getProcessXML(String var1) throws EdocException;

    boolean stepBack(Long var1, Long var2, EdocOpinion var3, Map<String, Object> var4) throws EdocException, BusinessException;

    boolean transStepStop(Long var1, Map<String, Object> var2) throws EdocException, BusinessException;

    boolean takeBack(Long var1) throws EdocException, BusinessException;

    boolean transTakeBack(Long var1) throws EdocException, BusinessException;

    void hasten(String var1, String var2, String var3) throws EdocException;

    void saveBody(EdocBody var1) throws EdocException;

    void saveOpinion(EdocOpinion var1, boolean var2) throws EdocException, BusinessException;

    void saveOpinion(EdocOpinion var1, EdocOpinion var2, boolean var3) throws BusinessException;

    void saveOpinion(EdocOpinion var1, EdocOpinion var2, boolean var3, String var4) throws EdocException, BusinessException;

    String getPolicyBySummary(EdocSummary var1) throws EdocException;

    String getPolicyByAffair(CtpAffair var1, String var2) throws EdocException;

    void update(Long var1, Map<String, Object> var2);

    void update(EdocSummary var1) throws Exception;

    void update(EdocSummary var1, boolean var2) throws Exception;

    void setFinishedFlag(long var1, int var3) throws EdocException;

    boolean updateHtmlBody(long var1, String var3) throws EdocException;

    FormOpinionConfig getEdocFormOpinionConfig(EdocSummary var1);

    Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary var1);

    Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary var1, boolean var2);

    Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary var1, boolean var2, String var3);

    Map<String, EdocOpinionModel> getEdocOpinion(EdocSummary var1, FormOpinionConfig var2);

    Map<String, EdocOpinionModel> getEdocOpinion1(EdocSummary var1, Long var2, FormOpinionConfig var3);

    Hashtable getEdocOpinion(Long var1, LinkedHashMap var2) throws EdocException;

    Hashtable getEdocOpinion(Long var1, Long var2, LinkedHashMap var3) throws EdocException;

    LinkedHashMap getEdocOpinion(Long var1, Long var2, Long var3, String var4) throws EdocException;

    LinkedHashMap getEdocOpinion(EdocSummary var1, Long var2, Long var3, Long var4, String var5) throws EdocException;

    void sendImmediate(Long var1, EdocSummary var2) throws EdocException, BusinessException;

    void sendImmediate(Long var1, EdocSummary var2, boolean var3) throws EdocException, BusinessException;

    EdocOpinion findBySummaryIdAndAffairId(long var1, long var3);

    List<ResultModel> iSearch(ConditionModel var1);

    boolean useMetadataValue(Long var1, Long var2, String var3);

    EdocSummary getSummaryByProcessId(String var1);

    List<EdocSummaryModel> queryByCondition(long var1, EdocSearchModel var3);

    List<EdocSummaryModel> queryByCondition(long var1, EdocSearchModel var3, boolean var4) throws BusinessException;

    List<MoreSignSelectPerson> findMoreSignPersons(String var1);

    Hashtable<Long, EdocSummary> queryBySummaryIds(List<Long> var1);

    void recoidChangeWord(String var1, String var2, String var3, String var4);

    void updateAttachment(EdocSummary var1, CtpAffair var2, User var3, HttpServletRequest var4) throws Exception;

    void saveUpdateAttInfo(int var1, Long var2, List<ProcessLog> var3);

    String getFullArchiveNameByArchiveId(Long var1);

    String getShowArchiveNameByArchiveId(Long var1);

    void setArchiveIdToAffairsAndSendMessages(EdocSummary var1, CtpAffair var2, boolean var3);

    String checkIsCanBeRepealed(String var1);

    String checkIsCanBeTakeBack(String var1) throws BusinessException;

    List<CtpTrackMember> getColTrackMembersByObjectIdAndTrackMemberId(Long var1, Long var2) throws BusinessException;

    void deleteColTrackMembersByObjectId(Long var1);

    EdocBody getEdocBodyByFileid(long var1);

    void createZcdbQuartz(long var1, Date var3) throws EdocException;

    void deleteZcdbQuartz(long var1, Date var3);

    String[] getTimeTextFiledByTimeEnum(int var1);

    String getSendUnitFullName(int var1, String var2, String var3, long var4);

    boolean isHaveHtmlSign(String var1);

    List<EdocOpinion> findEdocOpinion(Long var1, Long var2, String var3);

    List<EdocSummaryModel> queryByDocManager(EdocSearchModel var1, boolean var2, long var3, String var5, String var6, String var7);

    List<SummaryModel> queryRegisterData(int var1, Map<String, String> var2, User var3);

    List<EdocSummaryModel> combQueryByCondition(int var1, EdocSearchModel var2, int var3, int... var4);

    List<EdocSummaryModel> combQueryByCondition(int var1, EdocSearchModel var2, int var3, Map<String, Object> var4, int... var5);

    void update(EdocOpinion var1);

    void update(Long var1, Long var2, String var3, int var4, int var5);

    boolean checkTempleteDisabled(Long var1);

    void setTrack(Long var1, boolean var2, String var3);

    boolean AjaxjudgeHasPermitIssueNewsOrBull(String var1) throws Exception;

    String getPhysicalPath(String var1, String var2, boolean var3, int var4);

    void pigeonholeAffair(String var1, long var2, Long var4, Long var5, String var6) throws EdocException, BusinessException;

    String checkSerialNoExcludeSelf(String var1, String var2);

    List<EdocSummaryModel> getMyEdocDeadlineNotEmpty(Map<String, Object> var1);

    void transFinishWorkItem(CtpAffair var1, Map<String, Object> var2) throws Exception;

    void createPdfBodies(EdocSummary var1, String var2);

    void saveEdocRegisterCondition(EdocRegisterCondition var1);

    List<EdocRegisterCondition> getEdocRegisterCondition(long var1, Map<String, Object> var3, User var4);

    int getEdocRegisterConditionTotal(long var1, int var3, String var4);

    void delEdocRegisterCondition(long var1);

    EdocRegisterCondition getEdocRegisterConditionById(long var1);

    void updateAffairStateWhenClick(CtpAffair var1) throws BusinessException;

    boolean isBeSended(Long var1);

    String canTakeBack(String var1, String var2, String var3, String var4, String var5);

    EdocSummary getEdocSummaryByProcessId(Long var1);

    String[] edocCanStepBack(String var1, String var2, String var3, String var4, String var5, String var6);

    String[] edocCanTemporaryPending(String var1);

    String[] edocCanRepeal(String var1, String var2);

    String[] edocCanWorkflowCurrentNodeSubmit(String var1);

    List<EdocOpinion> findEdocOpinionByAffairId(Long var1, Long var2, String var3, List<Long> var4);

    String getDealExplain(String var1, String var2, String var3);

    void transSetFinishedFlag(EdocSummary var1) throws BusinessException;

    String[] edocCanChangeNode(String var1);

    String[] canStopFlow(String var1);

    String ajaxCheckNodeHasExchangeType(String var1, String var2, String var3) throws BusinessException;

    void appointStepBack(Map<String, Object> var1) throws BusinessException;

    String getDeptSenders(String var1) throws BusinessException;

    String calculateWorkDatetime(String var1) throws BusinessException;

    String ajaxCalcuteNatureDatetime(String var1, Long var2);

    String getEdocAttSizeAndAttDocSize(long var1);

    String checkHasSignaturehtml(long var1);

    List<AttachmentVO> getAttachmentListBySummaryId(Long var1, String var2) throws BusinessException;

    String checkAffairValid(String var1, String var2);

    String getTrackName(String var1);

    FlipInfo getSendRegisterData(FlipInfo var1, Map<String, String> var2) throws BusinessException;

    FlipInfo getRecRegisterData(FlipInfo var1, Map<String, String> var2) throws BusinessException;

    int checkSerialNoExsit(String var1, String var2);

    String isQuickSend(String var1);

    String getBodyType(String var1) throws BusinessException;

    String getRegisterBodyType(String var1) throws BusinessException;

    Map getAttributeSettingInfo(Map<String, String> var1) throws BusinessException;

    void onLeaveDealPage(String var1) throws BusinessException;

    void unLockSummary(Long var1, EdocSummary var2) throws BusinessException;

    void h5CancelRegister(EdocSummary var1) throws BusinessException;

    void unlockEdocAll(Long var1, EdocSummary var2);

    void deleteOpinionByWay(CtpAffair var1, String var2);

    void updateCurrentNodeUser(Long var1, String var2) throws BusinessException;

    String transferEdoc(User var1, long var2, long var4, Long var6, String var7, Boolean var8, String var9, HttpServletRequest var10, List<Long[]> var11) throws BusinessException;

    String transTransferEdoc(User var1, CtpAffair var2, EdocSummary var3, EdocOpinion var4, Long var5, List<Long[]> var6) throws BusinessException;

    String checkCreateRoleAuth(String var1) throws BusinessException;

    String getAffairIdByEdocId(String var1) throws BusinessException;

    String checkAffairIsDone(String var1);
}

