package com.seeyon.ctp.organization.dao;

import com.seeyon.apps.addressbook.manager.AddressBookManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.config.SystemConfig;
import com.seeyon.ctp.common.config.manager.ConfigManager;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.customize.manager.CustomizeManager;
import com.seeyon.ctp.common.excel.DataRecord;
import com.seeyon.ctp.common.excel.FileToExcelManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.fontimage.FontImageManger;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.metadata.Column;
import com.seeyon.ctp.common.metadata.manager.MetadataManager;
import com.seeyon.ctp.common.po.BasePO;
import com.seeyon.ctp.common.po.config.ConfigItem;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.*;
import com.seeyon.ctp.organization.bo.OrganizationMessage.OrgMessage;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.manager.OuterWorkerAuthUtil;
import com.seeyon.ctp.organization.po.*;
import com.seeyon.ctp.organization.principal.NoSuchPrincipalException;
import com.seeyon.ctp.organization.principal.PrincipalManager;
import com.seeyon.ctp.organization.selectpeople.manager.SelectPeopleManager;
import com.seeyon.ctp.organization.selectpeople.manager.SelectPeoplePanel;
import com.seeyon.ctp.organization.util.OrgTree;
import com.seeyon.ctp.organization.util.OrgTreeNode;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UniqueList;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import java.text.DecimalFormat;
import java.util.*;

public class OrgHelper {

    private final static Log log = LogFactory.getLog(OrgHelper.class);

    static Object[][] EntityType = new Object[8][3];
    static{
        EntityType[0] = new Object[]{V3xOrgAccount.class,       OrgUnit.class,      OrgConstants.ORGENT_TYPE.Account.name()};
        EntityType[1] = new Object[]{V3xOrgDepartment.class,    OrgUnit.class,      OrgConstants.ORGENT_TYPE.Department.name()};
        EntityType[2] = new Object[]{V3xOrgMember.class,        OrgMember.class,    OrgConstants.ORGENT_TYPE.Member.name()};
        EntityType[3] = new Object[]{V3xOrgPost.class,          OrgPost.class,      OrgConstants.ORGENT_TYPE.Post.name()};
        EntityType[4] = new Object[]{V3xOrgTeam.class,          OrgTeam.class,      OrgConstants.ORGENT_TYPE.Team.name()};
        EntityType[5] = new Object[]{V3xOrgLevel.class,         OrgLevel.class,     OrgConstants.ORGENT_TYPE.Level.name()};
        EntityType[6] = new Object[]{V3xOrgRole.class,          OrgRole.class,      OrgConstants.ORGENT_TYPE.Role.name()};
        EntityType[7] = new Object[]{V3xOrgUnit.class,          OrgUnit.class,      OrgConstants.ORGENT_TYPE.Unit.name()};
//        EntityType[7] = new Object[]{V3xOrgDutyLevel.class,     OrgDutyLevel.class, OrgConstants.ORGENT_TYPE.DutyLevel.name()};
    }


    private static OrgCache orgCache0;
    private static OrgCache getOrgCache(){
        if(orgCache0 == null){
            orgCache0 = (OrgCache)AppContext.getBean("orgCache");
        }

        return orgCache0;
    }
    private static OrgManager orgManager0;
    public static OrgManager getOrgManager(){
        if(orgManager0 == null){
            orgManager0 = (OrgManager)AppContext.getBean("orgManager");
        }

        return orgManager0;
    }
    
    private static AddressBookManager addressBookManager0;
    public static AddressBookManager getAddressBookManager() {
    	if(addressBookManager0 == null){
    		addressBookManager0 = (AddressBookManager)AppContext.getBean("addressBookManager");
    	}
		return addressBookManager0;
	}
    
	private static MetadataManager metadataManager0;
    private static MetadataManager getMetadataManager(){
        if(metadataManager0 == null){
            metadataManager0 = (MetadataManager)AppContext.getBean("metadataManager");
        }

        return metadataManager0;
    }

    private static PrincipalManager principalManager0;
    private static PrincipalManager getPrincipalManager(){
        if(principalManager0 == null){
            principalManager0 = (PrincipalManager)AppContext.getBean("principalManager");
        }

        return principalManager0;
    }

    private static CustomizeManager customizeManager0;
    public static CustomizeManager getCustomizeManager() {
        if(customizeManager0 == null){
            customizeManager0 = (CustomizeManager) AppContext.getBean("customizeManager");
        }

        return customizeManager0;
    }
    private static FontImageManger fontImageManger0;
    public static FontImageManger getFontImageMnager(){
    	if(fontImageManger0 == null){
    		fontImageManger0 = (FontImageManger) AppContext.getBean("fontImageManger");
        }

        return fontImageManger0;
    }
    private static SelectPeopleManager selectPeopleManager0;
    public static SelectPeopleManager getSelectPeopleManager() {
        if(selectPeopleManager0 == null){
            selectPeopleManager0 = (SelectPeopleManager) AppContext.getBean("selectPeopleManager");
        }
        
        return selectPeopleManager0;
    }

    private static ConfigManager configManager0;

    public static ConfigManager getConfigManager() {
        if (configManager0 == null) {
            configManager0 = (ConfigManager) AppContext.getBean("configManager");
        }

        return configManager0;
    }
    
    private static EnumManager enumManagerNew;
    
    public static EnumManager getEnumManagerNew() {
        if (enumManagerNew == null) {
            enumManagerNew = (EnumManager) AppContext.getBean("enumManagerNew");
        }

        return enumManagerNew;
    }

    /**
     * 从一个列表中得到一个排序号。目前的算法是取最大的加一，以表达后来者最后的概念。
     * @param list
     * @return
     */
	public static <T extends V3xOrgEntity>int assignEntitySortID(HashMap<Long, T> list) {
		int number = 0;
		if (list != null) {
			for (Iterator<T> it = list.values().iterator(); it.hasNext();) {
				V3xOrgEntity entity = it.next();
				if (number <= entity.getSortId())
					number = entity.getSortId().intValue() + 1;
			}
		}
		return number;

	}

	/**
	 * 将组织模型实体列表变成ID列表，主要用于组的组员信息处理
	 * @param entityList 实体列表
	 * @return ID列表
	 */
	public static List<Long> entityListToIdList(List<V3xOrgEntity> entityList) {
	    List<Long> idList = new ArrayList<Long>();
	    for (V3xOrgEntity ent : entityList) {
	        idList.add(ent.getId());
	    }
	    return idList;
	}

	/**
	 * 从列表中获取实体类型
	 * @param orgEntity
	 * @return
	 */
	public static Class<? extends V3xOrgEntity> getEntityType(V3xOrgEntity orgEntity) {
		if (orgEntity instanceof V3xOrgAccount) {
			return V3xOrgAccount.class;
		}

		if (orgEntity instanceof V3xOrgPost) {
			return V3xOrgPost.class;
		}

		if (orgEntity instanceof V3xOrgDepartment) {
			return V3xOrgDepartment.class;
		}
		if (orgEntity instanceof V3xOrgLevel) {
			return V3xOrgLevel.class;
		}
		//政务版--职级
		if (orgEntity instanceof V3xOrgDutyLevel) {
			return V3xOrgDutyLevel.class;
		}

		if (orgEntity instanceof V3xOrgMember) {
			return V3xOrgMember.class;
		}

		if (orgEntity instanceof V3xOrgTeam) {
			return V3xOrgTeam.class;
		}

		if (orgEntity instanceof V3xOrgRole) {
			return V3xOrgRole.class;
		}

		return null;
	}

	/**
	 * 从列表中获取实体类型
	 * @param simpleName
	 * @return
	 */
	public static Class<? extends V3xOrgEntity> getEntityTypeBySimpleName(String simpleName) {

		if (simpleName.equals(V3xOrgAccount.class.getSimpleName())) {
			return V3xOrgAccount.class;
		}

		if (simpleName.equals(V3xOrgPost.class.getSimpleName())) {
			return V3xOrgPost.class;
		}

		if (simpleName.equals(V3xOrgDepartment.class.getSimpleName())) {
			return V3xOrgDepartment.class;
		}
		if (simpleName.equals(V3xOrgLevel.class.getSimpleName())) {
			return V3xOrgLevel.class;
		}
		//政务版--职级
		if (simpleName.equals(V3xOrgDutyLevel.class.getSimpleName())) {
			return V3xOrgDutyLevel.class;
		}

		if (simpleName.equals(V3xOrgMember.class.getSimpleName())) {
			return V3xOrgMember.class;
		}

		if (simpleName.equals(V3xOrgTeam.class.getSimpleName())) {
			return V3xOrgTeam.class;
		}

		if (simpleName.equals(V3xOrgRole.class.getSimpleName())) {
			return V3xOrgRole.class;
		}


		return null;
	}

    public static String getEntityTypeByClassSimpleName(String simpleName) {

        if (simpleName.equals(V3xOrgAccount.class.getSimpleName())) {
            return OrgConstants.ORGENT_TYPE.Account.name();
        }

        if (simpleName.equals(V3xOrgPost.class.getSimpleName())) {
            return OrgConstants.ORGENT_TYPE.Post.name();
        }

        if (simpleName.equals(V3xOrgDepartment.class.getSimpleName())) {
            return OrgConstants.ORGENT_TYPE.Department.name();
        }
        if (simpleName.equals(V3xOrgLevel.class.getSimpleName())) {
            return OrgConstants.ORGENT_TYPE.Level.name();
        }
        //政务版--职级
        if (simpleName.equals(V3xOrgDutyLevel.class.getSimpleName())) {
            return OrgConstants.ORGENT_TYPE.DutyLevel.name();
        }

        if (simpleName.equals(V3xOrgMember.class.getSimpleName())) {
            return OrgConstants.ORGENT_TYPE.Member.name();
        }

        if (simpleName.equals(V3xOrgTeam.class.getSimpleName())) {
            return OrgConstants.ORGENT_TYPE.Team.name();
        }

        if (simpleName.equals(V3xOrgRole.class.getSimpleName())) {
            return OrgConstants.ORGENT_TYPE.Role.name();
        }


        return null;
    }

	// 从列表中获取实体类型
    public static Class<? extends V3xOrgEntity> getEntityTypeByOrgConstantsType(String typeName) {

        if (typeName.equals(OrgConstants.ORGENT_TYPE.Account.name())) {
            return V3xOrgAccount.class;
        }

        if (typeName.equals(OrgConstants.ORGENT_TYPE.Post.name())) {
            return V3xOrgPost.class;
        }

        if (typeName.equals(OrgConstants.ORGENT_TYPE.Department.name())) {
            return V3xOrgDepartment.class;
        }
        if (typeName.equals(OrgConstants.ORGENT_TYPE.Level.name())) {
            return V3xOrgLevel.class;
        }
        //政务版--职级
        if (typeName.equals(OrgConstants.ORGENT_TYPE.DutyLevel.name())) {
            return V3xOrgDutyLevel.class;
        }

        if (typeName.equals(OrgConstants.ORGENT_TYPE.Member.name())) {
            return V3xOrgMember.class;
        }

        if (typeName.equals(OrgConstants.ORGENT_TYPE.Team.name())) {
            return V3xOrgTeam.class;
        }

        if (typeName.equals(OrgConstants.ORGENT_TYPE.Role.name())) {
            return V3xOrgRole.class;
        }
        return null;
    }

	public static String parseElements(String typeAndIds) {
        if(StringUtils.isBlank(typeAndIds)){
            return null;
        }
        List<String> elements = new ArrayList<String>();
        StringTokenizer str = new StringTokenizer(typeAndIds, "|,");
        //token数量必须要成双，否则type和id不对应会报错
        while (str.hasMoreTokens() && str.countTokens() > 1) {
            String type = str.nextToken();
            String id = str.nextToken();

            String e = parseElement1(type, id);
            if(StringUtils.isNotBlank(e)){
                elements.add(e);
            }
        }

        return StringUtils.join(elements.iterator(), ",");
    }
	/**
	 * 获取实体list的对应选人界面的回填格式
	 *
	 * @param list
	 * @return string[0]:ID,string[1]:name
	 * @throws BusinessException
	 */
	public static String getSelectPeopleStr(List<V3xOrgEntity> list) throws BusinessException {
		String ids = parseElements(list, "id", "entityType");
		String names = showOrgEntities(list, "id", "entityType", null);

		HashMap map =new HashMap();
		map.put("value", ids);
		map.put("text", names);
		return JSONUtil.toJSONString(map);

    }
	
	
	/**
	 * 获取实体list的对应选人界面的回填格式
	 *
	 * @param list
	 * @return string[0]:ID,string[1]:name
	 * @throws BusinessException
	 */
	public static String getSelectPeopleStrExt(List<EntityIdTypeDsBO> list) throws BusinessException {
		String ids = parseElementsExt(list, "id", "dsc","dscType");
		String names = showOrgEntitiesExt(list, "id", "dsc","dscType", null);

		HashMap map =new HashMap();
		map.put("value", ids);
		map.put("text", names);
		return JSONUtil.toJSONString(map);

    }
	
    /**
     * 将EntityType|EntityId,EntityType|EntityId转换成名称字符串
     *
     * @param typeAndIds Member|13241234,Department|23452345234
     * @return (致远)开发中心、(股份)U8事业本部、(金融)赵大伟
     */
    public static String showOrgEntities(String typeAndIds, PageContext pageContext) {
        if(StringUtils.isBlank(typeAndIds)){
            return null;
        }

        StringTokenizer str = new StringTokenizer(typeAndIds, ",|");
        List<String[]> entities = new ArrayList<String[]>();
        while (str.hasMoreTokens()) {
            String type = str.nextToken();
            String id = str.nextToken();

            entities.add(new String[]{type, id});
        }

        String separator = getOrgEntitiesSeparator(pageContext);

        return showOrgEntities1(entities, separator);
    }
    /**
     * 将EntityType|EntityId,EntityType|EntityId转换成名称字符串
     * * @param separator 分隔符
     * @param typeAndIds Member|13241234,Department|23452345234
     * @return (致远)开发中心、(股份)U8事业本部、(金融)赵大伟
     */
    public static String showOrgEntities(String typeAndIds, String separator) {
        if(StringUtils.isBlank(typeAndIds)){
            return null;
        }

        StringTokenizer str = new StringTokenizer(typeAndIds, ",|");
        List<String[]> entities = new ArrayList<String[]>();
        while (str.hasMoreTokens()) {
            String type = str.nextToken();
            String id = str.nextToken();

            entities.add(new String[]{type, id});
        }

        return showOrgEntities1(entities, separator);
    }
    /**
     * 将格式为EntityId,EntityId的数据转换成Element[]
     *
     * @param ids 1234123,234534563
     * @param type 指定类型
     * @param separator 显示内容的间隔符号
     * @return
     */
    public static String showOrgEntities(String ids, String type, String separator) {
        if(StringUtils.isBlank(ids)){
            return null;
        }
        List<String[]> entities = new ArrayList<String[]>();

        String[] idstr = ids.split(",");
        for (String id : idstr) {
            entities.add(new String[]{type, id});
        }

        return showOrgEntities1(entities, separator);
    }
    /**
     * 将格式为EntityId,EntityId的数据转换成Element[]
     *
     * @param ids 1234123,234534563
     * @param type 指定类型
     * @param pageContext
     * @return
     */
    public static String showOrgEntities(String ids, String type, PageContext pageContext) {
        String separator = getOrgEntitiesSeparator(pageContext);
        return showOrgEntities(ids, type, separator);
    }
    public static String showOrgEntities(List<Object[]> entities, String separator){
        List<String[]> entities1 = new ArrayList<String[]>();
        for (Object[] strings : entities) {
            entities1.add(new String[]{String.valueOf(strings[0]), String.valueOf(strings[1])});
        }

        return showOrgEntities1(entities1, separator);
    }
    /**
     * 将对象人员/部门/单位等数据链接起来，显示方式为： (致远)开发中心、(股份)U8事业本部、(金融)赵大伟
     *
     * 自动根据
     *
     * <pre>
     * public class TempleteAuth extends BaseModel implements Serializable {
     *  private Long authId;
     *  private String authType;
     *  private Integer sort;
     *  private long templeteId;
     *
     *  //setter / getter
     * }
     * </pre>
     *
     * ${v3x:showOrgEntities(List<TempleteAuth>, "authId", "authType", pageContext)}
     *
     * @param list 数据集合
     * @param idProperty V3xOrgEntity的id
     * @param typeProperty V3xOrgEntity的type
     * @param pageContext
     * @return
     */
    public static String showOrgEntities(Collection<? extends Object> list, String idProperty, String typeProperty, PageContext pageContext){
        if (list == null || list.isEmpty() || StringUtils.isBlank(idProperty)
                || StringUtils.isBlank(typeProperty)) {
            return null;
        }

        List<String[]> entities = new ArrayList<String[]>();
        for (Object object : list) {
            if(object == null){
                log.warn("", new Exception("Collection中的数据有null"));
                continue;
            }
            try {
                String type = String.valueOf(PropertyUtils.getProperty(object, typeProperty));
                String id = String.valueOf(PropertyUtils.getProperty(object, idProperty));

                if(StringUtils.isBlank(type) || id == null
                        || id.equals(String.valueOf(com.seeyon.ctp.common.constants.Constants.GLOBAL_NULL_ID))){
                    continue;
                }

                entities.add(new String[]{type, id});
            }
            catch (Exception e) {
                log.error("取得Bean属性", e);
            }
        }

        String separator = getOrgEntitiesSeparator(pageContext);
        return showOrgEntities1(entities, separator);
    }
    
    public static String showOrgEntitiesExt(Collection<? extends Object> list, String idProperty, String typeProperty, String dscTypeProperty, PageContext pageContext){
        if (list == null || list.isEmpty() || StringUtils.isBlank(idProperty)
                || StringUtils.isBlank(typeProperty)) {
            return null;
        }

        List<String[]> entities = new ArrayList<String[]>();
        for (Object object : list) {
            if(object == null){
                log.warn("", new Exception("Collection中的数据有null"));
                continue;
            }
            try {
                String type = String.valueOf(PropertyUtils.getProperty(object, typeProperty));
                String id = String.valueOf(PropertyUtils.getProperty(object, idProperty));
                String dscType = String.valueOf(PropertyUtils.getProperty(object, dscTypeProperty));

                if(StringUtils.isBlank(type) || id == null
                        || id.equals(String.valueOf(com.seeyon.ctp.common.constants.Constants.GLOBAL_NULL_ID))){
                    continue;
                }

                entities.add(new String[]{type, id, dscType});
            }
            catch (Exception e) {
                log.error("取得Bean属性", e);
            }
        }

        String separator = getOrgEntitiesSeparator(pageContext);
        return showOrgEntities1(entities, separator);
    }

    private static String getOrgEntitiesSeparator(PageContext pageContext) {
        String key = "common.separator.label";
        String separator = ResourceUtil.getString(key);
        if(key.equals(separator) && pageContext!=null)separator = _(pageContext, key);
        if(key.equals(separator)){
        	separator = "、";
        }
        return separator;
    }
    /**
     * 国际化,不支持参数
     *
     * @param pageContext
     * @param key
     * @return
     */
    public static String _(PageContext pageContext, String key) {
        return ResourceBundleUtil.getString(pageContext, key);
    }
    /**
     * 组装最终显示的人员信息
     *
     * @param entities Object[(Stirng)类型, (Long)对应id]
     * @param separator 分隔符
     * @return 王文京(集团)、徐石、胡守云、王五(金融)
     */
    private static String showOrgEntities1(List<String[]> entities, String separator){
        if(entities == null || entities.isEmpty()){
            return null;
        }

        Long loginAccountId = -1L;
        User user = AppContext.getCurrentUser();
        if(user != null){
            loginAccountId = user.getLoginAccount();
        }

        boolean isShowAccountShortname = false;
        List<Long> accountIds = new ArrayList<Long>();
        List<String> names = new ArrayList<String>();

        for (String[] object : entities) {
            try {
                String typeStr = object[0];
                String idStr = object[1];
                String dscType = "";
                if(object.length==3 && "1".equals(object[2])){
                	dscType = "("+ResourceUtil.getString("selectPeople.excludeChildDepartment")+")";
                }

                if(idStr.equals(String.valueOf(com.seeyon.ctp.common.constants.Constants.GLOBAL_NULL_ID))){
                    return null;
                }

                Object[] elements = parseElement(typeStr, idStr);
                //数据不存在
                if(elements == null){
                    continue;
                }

                Long entityAccountId = (Long)elements[1];

                if(!isShowAccountShortname && !Strings.equals(loginAccountId, -1L) && !Strings.equals(loginAccountId, entityAccountId)){
                    isShowAccountShortname = true;
                }

                if(V3xOrgEntity.ORGENT_TYPE_ACCOUNT.equals(typeStr) || Strings.equals(elements[3], Boolean.TRUE)){
                    accountIds.add(com.seeyon.ctp.common.constants.Constants.GLOBAL_NULL_ID);
                }
                else{
                    accountIds.add(entityAccountId);
                }

                names.add((String)elements[0]+ dscType);
            }
            catch (Exception e) {
                log.error("取得Bean属性:"+e.getLocalizedMessage(), e);
            }
        }

        if(isShowAccountShortname){
            for (int i = 0; i < accountIds.size(); i++) {
                long aId = accountIds.get(i);
                if(aId == com.seeyon.ctp.common.constants.Constants.GLOBAL_NULL_ID || Strings.equals(loginAccountId, aId)){
                    continue;
                }

                V3xOrgAccount account = getAccount(aId);

                if(account != null){
                    String name = names.get(i) + "(" + account.getShortName() + ")";
                    names.set(i, name);
                }
            }
        }

        return join(names, separator);
    }
    /**
     * 将集合连接起来
     * @param list
     * @param separator 分隔符
     * @return
     */
    public static String join(Collection<? extends Object> list, String separator){
        if (list == null || list.isEmpty()) {
            return null;
        }

        return StringUtils.join(list.iterator(), separator);
    }

    /**
     * 将授权、发布范围等信息连接成elements 格式EntityType|EntityId|EntityName|AccountId<br>
     * 注意：id或者type为null，以及id=-1的将被过滤掉
     *
     * <pre>
     * public class TempleteAuth extends BaseModel implements Serializable {
     *  private Long authId;
     *  private String authType;
     *  private Integer sort;
     *  private long templeteId;
     *
     *  //setter / getter
     * }
     *
     * 转换
     * parseElements(List<TempleteAuth>, "authId", "authType")
     *
     * 结果
     * Member|1234123|谭敏锋|34561234,Department|234534563|开发中心|34561234
     * </pre>
     *
     * @param list
     *            发布范围、授权集合
     * @param idProperty
     *            组织模型实体的Id字段的属性
     * @param typeProperty
     *            组织模型实体的类型字段的属性
     * @param accountType
     *            组织模型实体的所属单位字段的属性
     * @return
     */
    public static String parseElements(Collection<? extends Object> list,
            String idProperty, String typeProperty) {
        if (list == null || list.isEmpty() || StringUtils.isBlank(idProperty)
                || StringUtils.isBlank(typeProperty)) {
            return null;
        }

        List<String> elements = new ArrayList<String>();
        for (Object object : list) {
            if(object == null){
                log.warn("", new Exception("Collection中的数据有null"));
                break;
            }
            try {
                String type = String.valueOf(PropertyUtils.getProperty(object, typeProperty));
                String id = String.valueOf(PropertyUtils.getProperty(object, idProperty));

                if(StringUtils.isBlank(type) || StringUtils.isBlank(id)){
                    continue;
                }

                String e = parseElement1(type, id);
                if(StringUtils.isNotBlank(e)){
                    elements.add(e);
                }
            }
            catch (Exception e) {
                log.error("取得Bean属性", e);
            }
        }

        return StringUtils.join(elements.iterator(), ",");
    }
    
    public static String parseElementsExt(Collection<? extends Object> list,
            String idProperty, String typeProperty,String dscTypeProperty) {
        if (list == null || list.isEmpty() || StringUtils.isBlank(idProperty)
                || StringUtils.isBlank(typeProperty)) {
            return null;
        }

        List<String> elements = new ArrayList<String>();
        for (Object object : list) {
            if(object == null){
                log.warn("", new Exception("Collection中的数据有null"));
                break;
            }
            try {
                String type = String.valueOf(PropertyUtils.getProperty(object, typeProperty));
                String id = String.valueOf(PropertyUtils.getProperty(object, idProperty));
                Object tempDscType = PropertyUtils.getProperty(object, dscTypeProperty);
                String dscType = null;
                if(tempDscType!=null){
                	dscType = String.valueOf(tempDscType);
                }

                if(StringUtils.isBlank(type) || StringUtils.isBlank(id)){
                    continue;
                }

                String e = parseElement1Ext(type, id, dscType);
                if(StringUtils.isNotBlank(e)){
                    elements.add(e);
                }
            }
            catch (Exception e) {
                log.error("取得Bean属性", e);
            }
        }

        return StringUtils.join(elements.iterator(), ",");
    }

    /**
     * 将格式为EntityId,EntityId的数据转换成Element[]
     *
     * @param ids 1234123,234534563
     * @param type 指定类型
     * @return
     */
    public static String parseElements(String ids, String type) {
        if(StringUtils.isBlank(ids)){
            return null;
        }
        List<String> elements = new ArrayList<String>();

        String[] idstr = ids.split(",");
        for (String id : idstr) {
            String e = parseElement1(type, id);
            if(StringUtils.isNotBlank(e)){
                elements.add(e);
            }
        }

        return StringUtils.join(elements.iterator(), ",");
    }
    private static String parseElement1(String typeStr, String idStr){
        Object[] elements = parseElement(typeStr, idStr);
        if(elements == null){
            return null;
        }

        return typeStr + "|" + idStr + "|" + elements[0] + "|" + elements[1] + "|" + elements[2];
    }
    
    private static String parseElement1Ext(String typeStr, String idStr, String dscType){
        Object[] elements = parseElement(typeStr, idStr);
        if(elements == null){
            return null;
        }

        return typeStr + "|" + idStr + "|" + elements[0] + "|" + elements[1] + "|" + elements[2]+(dscType!=null?"|" + dscType:"");
    }

    private static Object[] parseElement(String typeStr, String idStr){
        try {
            String[] types = typeStr.split("_");
            String[] ids   = idStr.split("_");

            List<String> elementName = new ArrayList<String>();
            long accountId = -1;
            boolean isEnabled = true;
            boolean isAdmin = false;
            for(int j = 0; j < types.length; j++) {
                String type = types[j];
                String id = ids[j];

                //固定角色
                if(type.equals(V3xOrgEntity.ORGENT_TYPE_ROLE)){
                    OrgConstants.Role_NAME roleName = null;
                    try { roleName =  OrgConstants.Role_NAME.valueOf(id); }catch (Throwable e) { }

                    if(roleName != null){
                        elementName.add(ResourceUtil.getString("sys.role.rolename." + id));
                    }
                    else{
                        V3xOrgRole entity = getOrgManager().getRoleById(Long.parseLong(id));
                        if(entity == null){
                            return null;
                        }

                        elementName.add(entity.getShowName());
                        accountId = entity.getOrgAccountId();
                        isEnabled = entity.isValid();
                    }
                }
                else if("Node".equals(type) || "OrgTeam".equals(type) || "ExchangeAccount".equals(type)){
                    SelectPeoplePanel p = getSelectPeopleManager().getPanel(type);
                    Object[] name = new Object[]{id, null};
                    if(p != null){
                        name = p.getName(id, AppContext.getCurrentUser().getLoginAccount());
                    }
                    
                    elementName.add((String)name[0]);
                    accountId = (Long)name[1];
                }
                else if(V3xOrgEntity.ORGENT_TYPE_MEMBER.equals(type) || "Admin".equals(type) || "Guest".equals(type)){
                	if(Strings.isEmpty(id)){
                		return null;
                	}
                    V3xOrgMember member = getMember(Long.parseLong(id));
                    if(member == null){
                        return null;
                    }

                    elementName.add(showMemberName0(member, false, true));
                    accountId = member.getOrgAccountId();
                    isAdmin = member.getIsAdmin();
                }
                else if("FormField".equals(type))
                {//表单控件:  选人控件类型@字段#显示名称  ; 如：Member@field0022#人员
                    elementName.add(StringUtils.substringAfter(id, "#"));
                }
                else if (V3xOrgEntity.ORGENT_TYPE_JOINACCOUNTTAG.equals(type)) {
                    CtpEnumItem item = getEnumManagerNew().getCtpEnumItem(Long.parseLong(ids[j]));
                    if (item == null) {
                        return null;
                    }
                    elementName.add(item.getLabel());
                }
                else{
                    V3xOrgEntity entity = getOrgManager().getGlobalEntity(type, Long.parseLong(ids[j]));
                    if(entity == null){
                        return null;
                    }
                    elementName.add(entity.getName());
                    accountId = entity.getOrgAccountId();
                    isEnabled = entity.isValid();
                }
            }

            return new Object[]{StringUtils.join(elementName.iterator(), "-"), accountId, isEnabled, isAdmin};
        }
        catch (Exception e) {
            log.error("取得Bean属性", e);
        }

        return null;
    }

	/**
	 * 获取实体list的对应选人界面的字符串
	 *
	 * @param list
	 * @return
	 * @throws BusinessException
	 */
	public static String getSelectPeopleStrSimple(List<V3xOrgEntity> list) throws BusinessException {
		String ids = new String();
		String names = new String();
		//HashMap map =new HashMap();
		for (V3xOrgEntity v3xent : list) {
			ids= ids+ OrgHelper.boTostr(v3xent)+"|"+v3xent.getId()+",";
			names = names + v3xent.getName()+",";
		}
		if(!"".equals(ids)){
			ids = ids.substring(0, ids.length()-1);
			names = names.substring(0, names.length()-1);
		}
		//map.put("value", ids);
		//map.put("text", names);
		return ids;

    }
	/**
	 * 解析选人组件的字符串为数组
	 * @param selectPeopleStr
	 * @return
	 */
	public static String[][] getSelectPeopleElements(String selectPeopleStr) {
		String[][] results = null;
		if (StringUtils.isNotBlank(selectPeopleStr)) {
			String[] entities = selectPeopleStr.split(",");

			results = new String[entities.length][2];

			int i = 0;
			for (String entity : entities) {
				String[] items = entity.split("[|]");
				results[i][0] = items[0];
				results[i][1] = items[1];

				i++;
			}
		}

		return results;
	}
	/**
	 * 获取实体list的对应选人界面的回填格式
	 *
	 * @param list
	 * @return string[0]:ID,string[1]:name
	 * @throws BusinessException
	 */
	public static String getSelectPeopleStr(V3xOrgEntity ent) throws BusinessException {
		List list = new ArrayList();
		list.add(ent);
		return getSelectPeopleStr(list);

    }

    /**
     * 根据类型插入计算应该赋值的path
     *
     * @param classType 父组织的类型可以是Account或者Department
     * @param parentId 父节点ID
     * @return
     * @since CTP2.0
     */
    public synchronized static <T extends V3xOrgUnit> String getPathByPid4Add(Class<T> classType, Long parentId) {
    	return getPathByPid4Add(classType,parentId,null);
    }
    
    /**
     * 根据类型插入计算应该赋值的path 是否不包含停用
     *
     * @param classType 父组织的类型可以是Account或者Department
     * @param parentId 父节点ID
     * @param  isEnable 默认null|false包含停用 。true为不包含
     * @return
     * @since CTP2.0
     */
    public synchronized static <T extends V3xOrgUnit> String getPathByPid4Add(Class<T> classType, Long parentId,Boolean disIncludeDisable) {
        V3xOrgUnit pUnit = getOrgCache().getV3xOrgEntity(classType, parentId);
        String fatherPath = pUnit.getPath();
        List<V3xOrgUnit> childs = getOrgCache().getChildUnitsByPid(classType, parentId,disIncludeDisable,true);
        int unitId = 1;
        // 获得该组织的路径，找到最后一个号码加1
        if (childs != null && Strings.isNotEmpty(childs)) {
        	Collections.sort(childs, CompareUnitPath.getInstance());
        	
        	V3xOrgUnit tempUnit = childs.get(childs.size()-1);
            String tempPath = tempUnit.getPath();

            unitId = Integer.parseInt(tempPath.substring(tempPath.length() - 4)) + 1;
        }
        return fatherPath + String.valueOf(new DecimalFormat("0000").format(unitId));
    }

    /**
     * 根据组织的实体id获取父关系的实体id
     * 只针对unit组织表
     * @param entityId
     * @param unitType 组织类型，单位或部门，参数取值范围<code>OrgConstants.UnitType</code>
     * @return
     * @throws BusinessException
     */
    public static Long getParentIdByEntityId(String unitType, Long entityId) throws BusinessException {
        if (null == entityId)
            throw new BusinessException("传入参数实体id为空!");
        V3xOrgUnit unit = null;
        if (StringUtils.isNotBlank(unitType)) {
            if (unitType.equals(OrgConstants.UnitType.Account.name())) {
                unit = getOrgCache().getV3xOrgEntity(V3xOrgAccount.class, entityId);
            } else if (unitType.equals(OrgConstants.UnitType.Department.name())) {
                unit = getOrgCache().getV3xOrgEntity(V3xOrgDepartment.class, entityId);
            }
        }
        if (null == unit){
        	throw new BusinessException("获取组织实体为空！");
        }
        
        V3xOrgUnit parentUnit = getParentUnit(unit);
        if(parentUnit == null){
        	throw new BusinessException("获取组织实体为空！");
        }
        
        return parentUnit.getId();
    }

	public static String getRoleShowNameById(OrgManager orgManager, Long roleId){
		V3xOrgRole role = null;
		try {
			role = orgManager.getRoleById(roleId);
		}
		catch (Exception e) {
		}

		if(role == null){
			return null;
		}

		return getRoleShowNameByName(role.getName());
	}

	public static String getRoleShowNameByName(String roleName){
		String key = "sys.role.rolename." + roleName;
		String label = ResourceUtil.getString(key);
		if(key.equals(label)){
			return roleName;
		}
		else{
			return label;
		}
	}
	public static Object StrtoBoolean(Object field){
		if(field==null){
			return null;
		}
		if("true".equals(field)){
			return true;
		}else if("false".equals(field)){
			return false;
		}
		return field;
	}
    /**
     * 将实体集合，过滤掉无效人员返回实体的ID集合<br>
     *
     * @param entities 组织模型集合
     */
    @SuppressWarnings("unchecked")
    public static Collection<Long> getEntityIds(Collection<? extends V3xOrgEntity> entities) {
        return CollectionUtils.collect(entities, new Transformer() {
            @Override
            public Long transform(Object o) {
                V3xOrgEntity entity = (V3xOrgEntity) o;
                if (null != entity && entity.isValid()) {
                    return entity.getId();
                } else {
                    return null;
                }
            }
        });
    }

	/**
	 * 按名称取得指定实体的属性值。
	 * 实现一些常用的，避免反射，未实现的使用PropertyUtils.getProperty取。
	 * @param ent
	 * @param property 属性名称
	 * @return 实体指定属性的值。
	 * @throws BusinessException
	 */
	public static Object getProperty(V3xOrgEntity ent,String property) throws BusinessException
	{
		if(ent==null)throw new BusinessException("null entity.");
		return EntityPropertyHelper.getInstance().getValue(ent, property);
	}

	@SuppressWarnings("unchecked")
	public static <T extends V3xOrgEntity> T cloneEntityImmutableDecorator(T ent) {
        if (ent == null)
            return null;

        if (ent instanceof V3xOrgMember) {
            return (T)  ((V3xOrgMember) ent).cloneImmutableDecorator();
        }
        
        return cloneEntity(ent);
	}
	
    @SuppressWarnings("unchecked")
    public static <T extends V3xOrgEntity> T cloneEntity(T ent) {
        if (ent == null)
            return null;

        if (ent instanceof V3xOrgMember) {
            return (T) new V3xOrgMember((V3xOrgMember) ent);
        }
        else if (ent instanceof V3xOrgDepartment) {
            return (T) new V3xOrgDepartment((V3xOrgDepartment) ent);
        }
        else if (ent instanceof V3xOrgRole) {
            return (T) new V3xOrgRole((V3xOrgRole) ent);
        }
        else if (ent instanceof V3xOrgPost) {
            return (T) new V3xOrgPost((V3xOrgPost) ent);
        }
        else if (ent instanceof V3xOrgLevel) {
            return (T) new V3xOrgLevel((V3xOrgLevel) ent);
        }
        else if (ent instanceof V3xOrgDutyLevel){
            return (T) new V3xOrgDutyLevel((V3xOrgDutyLevel)ent);
        }
        else if (ent instanceof V3xOrgTeam) {
            return (T) new V3xOrgTeam((V3xOrgTeam) ent);
        }
        else if (ent instanceof V3xOrgAccount) {
            return (T) new V3xOrgAccount((V3xOrgAccount) ent);
        }

        return null;
    }
    public static void exportToExcel(HttpServletRequest request,
			HttpServletResponse response,FileToExcelManager fileToExcelManager
			,String title,DataRecord dataRecord) throws Exception{
		try {
			//log.info("exportToExcel");
			fileToExcelManager.save(response,title,dataRecord);//tanglh
		} catch (Exception e) {
			//log.error("error",e);
		    log.error("error",e);
		}
	}
	/**
     * PO转BO类（List中的）
     * List<V3xOrgEntity>
     * 由于容器中的泛型继承无法转换，所以此处不使用泛型了，使用时注意
     * @param str
     * @return
     */
    public static List<? extends V3xOrgEntity> listPoTolistBo(List<? extends BasePO> po) {
        if(null == po || po.isEmpty()) {
            return Collections.emptyList();
        }
        List<V3xOrgEntity> result = new ArrayList<V3xOrgEntity>(po.size());
        for (BasePO basePO : po) {
            result.add(poTobo(basePO));
        }
        return result;
    }
    /**
     * PO转BO类（List中的）
     * List<BasePO>
     * 由于容器中的泛型继承无法转换，所以此处不使用泛型了，使用时注意
     * @param str
     * @return
     */
    public static List<? extends BasePO> listBoTolistPo(List<? extends V3xOrgEntity> bo) throws BusinessException {
        if(null == bo || bo.isEmpty()) {
            return Collections.emptyList();
        }
        List<BasePO> result = new ArrayList<BasePO>(bo.size());
        for (V3xOrgEntity b : bo) {
            result.add(boTopo(b));
        }

        return result;
    }
    /**
     * PO类转换成BO类
     * @param po
     * @return
     * @throws BusinessException
     */
    public static Class<? extends V3xOrgEntity> poClassToboClass(Class<? extends BasePO> po) throws BusinessException {
        return (Class)EntityTypeGO(1, po, 0);
    }
    /**
     * BO类转换成PO类
     * @param po
     * @return
     * @throws BusinessException
     */
    public static Class<? extends BasePO> boClassTopoClass(Class<? extends V3xOrgEntity> bo) {
        return (Class)EntityTypeGO(0, bo, 1);
    }
	/**
     * PO类转换成BO类
     * @param po
     * @return
     * @throws BusinessException
     */
    public static V3xOrgEntity poTobo(BasePO po) {
        if(po == null){
            return null;
        }
        if(po.getClass() == OrgRole.class){
            return new V3xOrgRole().fromPO(po);
        }
        if(po.getClass() == OrgMember.class){
            return new V3xOrgMember().fromPO(po);
        }
        if(po.getClass() == OrgTeam.class){
            return new V3xOrgTeam().fromPO(po);
        }
        if(po.getClass() == OrgUnit.class){
            if(((OrgUnit)po).getType().equals(OrgConstants.UnitType.Account.name())){
                return new V3xOrgAccount().fromPO(po);
            }
            if(((OrgUnit)po).getType().equals(OrgConstants.UnitType.Department.name())){
                return new V3xOrgDepartment().fromPO(po);
            }
        }
        if(po.getClass() == OrgPost.class){
            return new V3xOrgPost().fromPO(po);
        }
        if(po.getClass() == OrgLevel.class){
            return new V3xOrgLevel().fromPO(po);
        }
//        if(po.getClass() == OrgDutyLevel.class){
//            return new V3xOrgDutyLevel().fromPO(po);
//        }
        //TODO

        return null;
    }


    /**
     * 显示人员名字，不带单位简称
     *
     * @param memberId
     * @return
     */
    public static String showMemberNameOnly(Long memberId){
        return showMemberName0(memberId, false);
    }
    /**
     * 显示人员名字，永远不显示单位简称
     *
     * @param member
     * @return
     */
    public static String showMemberNameOnly(V3xOrgMember member){
        return showMemberName0(member, false, false);
    }
    /**
     *
     * @param memberId
     * @param isShowAccountName 是否显示单位检查：如果不是一个单位的，则显示单位简称
     * @return
     */
    private static String showMemberName0(Long memberId, boolean isShowAccountName){
        if(memberId == null || memberId == -1){
            return null;
        }

        try {
            if(memberId.equals(V3xOrgEntity.CONFIG_SYSTEM_AUTO_TRIGGER_ID)){
                return ResourceUtil.getString("org.system.auto.trigger");
            }

            V3xOrgMember member = getMember(memberId);

            return showMemberName0(member, isShowAccountName, false);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static V3xOrgMember getMember(Long memberId) {
        if(memberId == null || Strings.equals(memberId, -1L) || Strings.equals(memberId, 0L)){
            return null;
        }

        try {
            return getOrgManager().getEntityByIdNoClone(V3xOrgMember.class, memberId);
        }
        catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }


    /**
     * BO类转换成PO类
     * @param bo
     * @return
     * @throws BusinessException
     */
    public static BasePO boTopo(V3xOrgEntity bo) throws BusinessException {
        return bo.toPO();
    }

    /**
     * 字符串转换成BO类
     * @param str
     * @return
     * @throws BusinessException
     */
    public static Class<? extends V3xOrgEntity> strTobo(String type) throws BusinessException {
    	if("Guest".equals(type)){
    		type = OrgConstants.ORGENT_TYPE.Member.name();
    	}
        return (Class)EntityTypeGO(2, type, 0);
    }
    /**
     * 字符串转换成PO类
     * @param str
     * @return
     * @throws BusinessException
     */
    public static Class<? extends BasePO> strTopo(String type) throws BusinessException {
    	if("Guest".equals(type)){
    		type = OrgConstants.ORGENT_TYPE.Member.name();
    	}
        return (Class)EntityTypeGO(2, type, 1);
    }

    /**
     * BO类转换成字符串
     * @param bo
     * @return
     * @throws BusinessException
     */
    public static String boTostr(V3xOrgEntity bo) throws BusinessException {
        return bo.getEntityType();
    }

    /**
     * PO类转换成字符串
     * @param po
     * @return
     * @throws BusinessException
     */
    public static String poTostr(BasePO po) throws BusinessException {
        return (String)EntityTypeGO(1, po.getClass(), 2);
    }

    private static Object EntityTypeGO(int sourceIndex, Object source, int objectiveIndex){
        for (Object[] et : EntityType) {
            if(et[sourceIndex].equals(source)){
                return et[objectiveIndex];
            }
        }

        return null;
    }
    /**
     * 获取组织的父组织, 如果没有找到，就返回空指针
     *
     * @param orgunit
     * @return
     * @throws BusinessException
     */
    public static V3xOrgUnit getParentUnit(V3xOrgUnit orgunit) {
        String path = orgunit.getPath();
        if(Strings.isNotBlank(path)) {
            if(path.length() > 4){
                String parentpath = path.substring(0, path.length() - 4);

                return getOrgCache().getV3xOrgUnitByPath(parentpath);
            }
        }
        return null;
    }

    /**
     * 获取某单位的可以访问的单位id列表<br>
     * ！！请不要直接使用<code>V3xOrgAccount</code>类中的get方法获取可以访问的单位id列表，一定要使用此方法！!
     * @param unitId
     * @return
     * @throws BusinessException
     */
    public static List<Long> getAccessIdsByUnitId(Long unitId) throws BusinessException {
        List<Long> accessIds = new UniqueList<Long>();
        List<V3xOrgAccount> accessAccounts = getOrgManager().accessableAccountsByUnitId(unitId);
        for (V3xOrgAccount bo : accessAccounts) {
            accessIds.add(bo.getId());
        }
        return accessIds;
    }

    public static V3xOrgPrincipal getV3xOrgPrincipal(Long memberId){
        try {
            String loginName = getPrincipalManager().getLoginNameByMemberId(memberId);
            return new V3xOrgPrincipal(memberId, loginName, OrgConstants.DEFAULT_INTERNAL_PASSWORD);
        }
        catch (NoSuchPrincipalException e) {
            //ignore
        }

        return null;
    }

    /**
     * 得到扩展属性的值
     * @param member
     * @param name 扩展属性的别用，比如EmailAddress
     * @return
     */
    public static Object getMemberExtAttr(V3xOrgMember member, String name) {
        Column column = getMetadataManager().getColumnByAlias("orgMember", name);
        if(column == null ){
            return null;
        }
        String POName = column.getName();

        return member.getPOProperties(POName);
    }

    /**
     * 得到人员的所有的扩展属性Key: 别名，如EmailAddress；Value - PO Field Name
     *
     * @return
     */
    public static Map<String, String> getMemberExtAttrKeyMaps(){
        Map<String, String> result = new HashMap<String, String>();
        List<Column> columns = getMetadataManager().getTable("orgMember").getColumns();
        for (Column column : columns) {
            result.put(column.getAlias(), column.getName());
        }

        return result;
    }

    public static Map<String, Object> getMemberExtAttrs(V3xOrgMember member){
        Map<String, Object> result = new HashMap<String, Object>();

        List<Column> columns = getMetadataManager().getTable("orgMember").getColumns();
        for (Column column : columns) {
            Object value = member.getPOProperties(column.getName());
            result.put(column.getAlias(), value!=null?value:"");
        }

        return result;
    }

    /**
     * 获取外部人员的岗位名称
     * @param member
     * @return
     */
    public static String getExtMemberPriPost(V3xOrgMember member) {
        String extPriPost = "";
        String extPostLevel = (String) member.getProperty("extPostLevel");
		if (Strings.isNotBlank(extPostLevel)) {
            String[] exts = extPostLevel.split(",");
            for (String s : exts) {
                if (s.startsWith("p:")) {
                    extPriPost = s.substring(2);
                    break;
                }
            }
        } else {
            return extPriPost;
        }
        return extPriPost;
    }

    /**
     * 获取外部人员的职务名称
     * @param member
     * @return
     */
    public static String getExtMemberLevel(V3xOrgMember member) {
        String extLevel = "";
        String extPostLevel = (String) member.getProperty("extPostLevel");
		if (Strings.isNotBlank(extPostLevel)) {
            String[] exts = extPostLevel.split(",");
            for (String s : exts) {
                if (s.startsWith("l:")) {
                    extLevel = s.substring(2);
                    break;
                }
            }
        } else {
            return extLevel;
        }
        return extLevel;
    }


    /**
     * 得到机构扩展属性的值
     * @param unit
     * @param name 扩展属性的别用，比如EmailAddress
     * @return
     */
    public static Object getUnitExtAttr(V3xOrgUnit unit, String name) {
        Column column = getMetadataManager().getColumnByAlias("orgUnit", name);
        if(column == null ){
            return null;
        }
        String POName = column.getName();

        return unit.getPOProperties(POName);
    }

    /**
     * 得到机构的所有的扩展属性Key - PO Field Name
     *
     * @return
     */
    public static Map<String, String> getUnitExtAttrKeyMaps(){
        Map<String, String> result = new HashMap<String, String>();
        List<Column> columns = getMetadataManager().getTable("orgUnit").getColumns();
        for (Column column : columns) {
            result.put(column.getAlias(), column.getName());
        }

        return result;
    }

    public static Map<String, Object> getUnitExtAttrs(V3xOrgUnit unit){
        Map<String, Object> result = new HashMap<String, Object>();

        List<Column> columns = getMetadataManager().getTable("orgUnit").getColumns();
        for (Column column : columns) {
            Object value = unit.getPOProperties(column.getName());
            result.put(column.getAlias(), value);
        }

        return result;
    }
    /**
     * 读取系统标志
     *
     * @param flagName 标志名称
     * @return
     */
    public static Object getSysFlag(String flagName){
        SysFlag sysFlag = SysFlag.valueOf(flagName);

        return getSysFlag(sysFlag);
    }
    private static SystemConfig systemConfig = null;
    private static SystemConfig getSystemConfig(){
        if(systemConfig == null){
            systemConfig = (SystemConfig)AppContext.getBean("systemConfig");
        }
        return systemConfig;
    }
    /**
     * 显示人员名字，如果不是一个单位的，则显示单位简称
     *
     * @param memberId
     * @return
     */
    public static String showMemberName(Long memberId){
        return showMemberName0(memberId, true);
    }

    /**
     * 显示部门名称，如果不是一个单位的，则显示单位简称
     * @param deptId
     * @return
     */
    public static String showDepartmentName(Long deptId) {
        return showDepartmentName0(deptId, true);
    }

    /**
     * 显示部门名称，如果不是一个单位的，则显示单位简称
     * @param deptId
     * @param isShowAccountName 是否显示单位简称
     * @return
     */
    private static String showDepartmentName0(Long deptId, boolean isShowAccountName) {
        try {
            if (deptId == null) {
                return "";
            }

            V3xOrgDepartment dept = getOrgManager().getDepartmentById(deptId);
            if (dept == null) {
                return "";
            }

            if (!isShowAccountName || !(Boolean) SysFlag.selectPeople_showAccounts.getFlag()) {
                return dept.getName();
            }

            User user = AppContext.getCurrentUser();
            if (user == null || Strings.equals(user.getLoginAccount(), dept.getOrgAccountId())) { //同一个单位的
                return dept.getName();
            } else {
                V3xOrgAccount account = getAccount(dept.getOrgAccountId());
                if (account == null) {
                    return dept.getName();
                }
                return dept.getName() + "(" + account.getShortName() + ")";
            }
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage(), e);
        }
        return "";
    }

    public static String showOrgPostName(Long postId){
        V3xOrgPost v3xOrgPost = getPost(postId) ;
        try{
            if(v3xOrgPost==null) return "-";
            return v3xOrgPost.getName() ;
        }catch(Exception e){
            log.error("得到v3xOrgPost的为null") ;
            return "-" ;
        }
    }
    public static V3xOrgPost getPost(Long postId) {
        try {
            return getOrgManager().getPostById(postId);
        }
        catch (Exception e) {
            log.warn("", e);
            return null;
        }
    }
    /**
     * 取得指定人员头像图片地址。
     * @param memberId 人员Id。
     * @return 头像的url，包括上下文，形如http://192.168.0.1:8080/seeyon/fileUpload.do...
     */
    public static String getAvatarImageUrl(Long memberId){
        String contextPath = SystemEnvironment.getContextPath();
        return getAvatarImageUrl(memberId, contextPath);
    }
    /**
     * 不管是否自定义过头像，取得默认头像。
     * @param memberId 人员Id。
     * @return 头像的url，包括上下文，形如http://192.168.0.1:8080/seeyon/fileUpload.do...
     */
    public static String getDefaultAvatarImageUrl(Long memberId){
        String contextPath = SystemEnvironment.getContextPath();
        String imageSrc = contextPath + "/apps_res/v3xmain/images/personal/pic.gif";
        try {
            V3xOrgMember member = getOrgManager().getEntityByIdNoClone(V3xOrgMember.class, memberId);
            if(member != null && !member.getIsAdmin()){
                String avatar_showType = getSystemSwitch("avatar_showType");//默认头像显示方式
                if ("name".equals(avatar_showType)) {
                    imageSrc = getFontImageMnager().getFontImagePathForStaffName(memberId,100);
                }
                String isUseDefaultAvatar = getSystemSwitch("default_avatar");
                if("enable".equals(isUseDefaultAvatar)){
                    Object property = member.getProperty("imageid");
                    if (property != null) {
                        String imageId = property.toString();
                        if (Strings.isNotBlank(imageId)) {
                            imageSrc = contextPath + imageId;
                        }
                    }
                }
            }
        }catch (Exception e) {
            log.error("获取默认头像异常:", e);
        }
        return imageSrc;
    }
    /**
     * 取得指定人员头像图片地址。
     * @param memberId 人员Id。
     * @return 头像的url，不包括上下文，形如/fileUpload.do...。上下文由contextPath参数指定。
     */
    public static String getAvatarImageUrl(Long memberId, String contextPath) {
        String imageSrc = contextPath + "/apps_res/v3xmain/images/personal/pic.gif";
        try {
            V3xOrgMember member = getOrgManager().getEntityByIdNoClone(V3xOrgMember.class, memberId);
            if(member != null && !member.getIsAdmin()){
                String avatar_showType = getSystemSwitch("avatar_showType");//默认头像显示方式
                if ("name".equals(avatar_showType)) {
                    imageSrc = getFontImageMnager().getFontImagePathForStaffName(memberId,100);
                }
                
            	//是否允许使用个人自己设置的头像
            	String allow_update_avatar = getSystemSwitch("allow_update_avatar");
            	//是否默认使用管理员设置的头像
            	String isUseDefaultAvatar = getSystemSwitch("default_avatar");
            	if ("enable".equals(allow_update_avatar) || 
            			(!"enable".equals(allow_update_avatar) && !"enable".equals(isUseDefaultAvatar))){
            		String fileName = getCustomizeManager().getCustomizeValue(memberId, "avatar");
            		//允许，且已经设置过了头像，用设置的头像
            		if (fileName != null && !Strings.equals("pic.gif", fileName)) {
            			fileName = fileName.replaceAll(" on", " son");
            			if (fileName.startsWith("fileId")) {
            				//imageSrc = contextPath + "/fileUpload.do?method=showRTE&" + fileName + "&type=image&showType=small&smallPX=100";
                            String imageParam=fileName.replace("fileId","id");
                            imageSrc=contextPath+"/commonimage.do?method=showImage&"+imageParam+"&size=custom&w=100&h=100";
            			} else {
            				imageSrc = contextPath + "/apps_res/v3xmain/images/personal/" + fileName;
            			}
            			return imageSrc;
            		}
            	}if("enable".equals(isUseDefaultAvatar)){
    				Object property = member.getProperty("imageid");
    				if (property != null) {
    					String imageId = property.toString();
    					if (Strings.isNotBlank(imageId)) {
    						imageSrc = contextPath + imageId;
    					}
    				}
            	}
            }
        	
        }
        catch (Exception e) {
        	log.error("获取人员头像异常:", e);
        }

        return imageSrc;
    }
    /**
     * 人员所在单位的名称
     * @param memberId
     * @return
     */
    public static String showOrgAccountNameByMemberid(Long memberId){
        if(memberId==null || memberId == 1 || memberId == 0){
            return "-" ;
        }
        V3xOrgMember m = getMember(memberId) ;
        if(m  == null){
            log.error("获取的人员为空") ;
            return null ;
        }
        return showOrgAccountName(m.getOrgAccountId()) ;
    }

    public static String showOrgAccountShortNameByMemberid(Long memberId) {
        if (memberId == null || memberId == 1 || memberId == 0) {
            return "-";
        }
        V3xOrgMember m = getMember(memberId);
        if (m == null) {
            return "-";
        }

        V3xOrgAccount account = getAccount(m.getOrgAccountId());
        try {
            if (account == null) {
                return "-";
            }
            if (account != null && account.isGroup()) {
                return "-";
            }
            return account.getShortName();
        } catch (Exception e) {
            return "-";
        }
    }

    public static String showDepartmentFullPath(Long departmentId){
        StringBuilder sb = new StringBuilder();
        try {
            V3xOrgDepartment dept = getDepartment(departmentId);
            if(dept != null){
                List<V3xOrgDepartment> pDs = getOrgManager().getAllParentDepartments(departmentId);
                for (V3xOrgDepartment department : pDs) {
                    sb.append(department.getName()).append("/");
                }
                sb.append(dept.getName());
            }

        }
        catch (Exception e) {
        }

        return sb.toString();
    }
    
    public static String showDepartmentFullPath(Long memberId,Long accountId){
        StringBuilder sb = new StringBuilder();
        try {
        	V3xOrgMember member = getMember(memberId);
        	if(member != null){
        		Long departmentId = member.getOrgDepartmentId();
        		//取兼职部门的部门全路径
        		if(!member.getOrgAccountId().equals(accountId)){
        			List<MemberPost> memberPost = getOrgManager().getMemberConcurrentPostsByAccountId(memberId,accountId);
        			if(Strings.isNotEmpty(memberPost)){
        				departmentId = memberPost.get(0).getDepId();
        			}
        		}
        		V3xOrgDepartment dept = getDepartment(departmentId);
        		if(dept != null){
        			//兼职单位
        			List<V3xOrgDepartment> pDs = getOrgManager().getAllParentDepartments(departmentId);
        			for (V3xOrgDepartment department : pDs) {
        				sb.append(department.getName()).append("/");
        			}
        			sb.append(dept.getName());
        		}
        	}

        }
        catch (Exception e) {
        }

        return sb.toString();
    }
    
    public static String showDepartmentFullPathByMemberId(Long memberId,Long departmentId){
        StringBuilder sb = new StringBuilder();
        try {
        	V3xOrgMember member = getMember(memberId);
        	V3xOrgDepartment dept = getDepartment(departmentId);
        	if(member == null || dept == null){
        		return "";
        	}
        	
        	Long memberInDepartmentId = departmentId;
        	if(dept.getIsInternal()){
        		boolean inMainPost = false;
        		//获取这个人所在的所有部门
        		List<MemberPost> memberPosts = getOrgManager().getMemberPosts(dept.getOrgAccountId(), memberId);
        		for(MemberPost mp : memberPosts){
        			if(mp.getDepId().equals(departmentId)){
        				inMainPost = true;
        			}
        			
        		}
        		if(!inMainPost){
        			//获取部门下的子部门,随机取第一个非主岗的部门
        			List<Long> childs = getOrgCache().getSubDeptList(departmentId, OrgCache.SUBDEPT_INNER_ALL);
        			childs.add(departmentId);
        			for(MemberPost mp : memberPosts){
        				 if(childs.contains(mp.getDepId())){
        					 memberInDepartmentId = mp.getDepId();
        				 }
        			}
        		}
        	}
        	
        	V3xOrgDepartment memberInDepartment = getDepartment(memberInDepartmentId);
			List<V3xOrgDepartment> pDs = getOrgManager().getAllParentDepartments(memberInDepartmentId);
			for (V3xOrgDepartment department : pDs) {
				sb.append(department.getName()).append("/");
			}
			sb.append(memberInDepartment == null? "" : memberInDepartment.getName());

        }
        catch (Exception e) {
        }

        return sb.toString();
    }
    
    public static V3xOrgDepartment getDepartment(Long departmentId) {
        try {
            return getOrgManager().getDepartmentById(departmentId);
        }
        catch (Exception e) {
            log.warn("", e);
            return null;
        }
    }
    /**
     * 人员的岗位名称
     * @param memberId
     * @return
     */
    public static String showOrgPostNameByMemberid(Long memberId){
        if(memberId == 1 || memberId == 0){
            return "-" ;
        }
        V3xOrgMember m = getMember(memberId) ;
        if(m  == null){
            log.error("获取的人员为空") ;
            return null ;
        }
        if(m.getIsAdmin()){
            return "-"  ;
        }
        if(m.isV5External()){
        	return getExtMemberPriPost(m);
        }
        return showOrgPostName(m.getOrgPostId()) ;
    }
    /**
     * 获取系统开关配置值
     *
     * @param name
     * @return
     */
    public static String getSystemSwitch(String name){
        return getSystemConfig().get(name);
    }
    /**
     * 读取系统标志
     *
     * @param sysFlag
     * @return
     */
    public static Object getSysFlag(SysFlag sysFlag){
        if(sysFlag == null){
            return null;
        }

        return sysFlag.getFlag();
    }
    /**
	 * 处理组织模型接口抛出的业务异常编码，保证不在核心Manager抛出业务异常
	 * TODO 异常显示的国际化
	 * @param m
	 * @throws BusinessException
	 */
	public static void throwBusinessExceptionTools(OrganizationMessage m) throws BusinessException  {
		List<OrgMessage> mErrorList = m.getErrorMsgs();
		if(!(mErrorList.size() > 0)) {
			return;
		}
    	for (OrgMessage o : mErrorList) {

			switch (o.getCode().ordinal()) {
			case 1:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_REPEAT_NAME"));
			case 2:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_REPEAT_SHORT_NAME"));
			case 3:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_REPEAT_CODE"));
			case 4:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_REPEAT_ADMIN_NAME"));
			case 5:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_ENTITY"));
			case 6:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_DEPARTMENT_ENABLE"));
			case 7:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_ROLE_ENABLE"));
			case 8:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_POST_ENABLE"));
			case 9:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_LEVEL_ENABLE"));
			case 10:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_CHILDACCOUNT"));
			case 11:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_TEAM_ENABLE"));
			case 12:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_MEMBER_ENABLE", o.getEnt().getName()));
			case 13:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPARTMENT_REPEAT_NAME", o.getEnt().getName()));
			case 14:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPARTMENT_EXIST_MEMBER", o.getEnt().getName()));
			case 15:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPARTMENT_EXIST_TEAM", o.getEnt().getName()));
			case 16:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPARTMENT_PARENTID_NULL"));
			case 17:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPARTMENT_PARENTDEPT_DISABLED"));
			case 18:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPARTMENT_PARENTDEPT_SAME"));
			case 19:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPARTMENT_PARENTDEPT_ISCHILD"));
			case 20:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.POST_REPEAT_NAME", o.getEnt().getName()));
			case 21:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.POST_EXIST_MEMBER"));
			case 22:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.LEVEL_EXIST_MEMBER", o.getEnt().getName()));
			case 23:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.LEVEL_EXIST_MAPPING", o.getEnt().getName()));
			case 24:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.MEMBER_DEPARTMENT_DISABLED"));
			case 25:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.MEMBER_POST_DISABLED"));
			case 26:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.MEMBER_LEVEL_DISABLED"));
			case 27:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.MEMBER_REPEAT_POST"));
			case 28:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.MEMBER_EXIST_SIGNET"));
			case 29:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.MEMBER_NOT_EXIST"));
			case 30:
                try {
                    V3xOrgMember dupLoginNameMember = getOrgManager().getMemberById(getPrincipalManager().getMemberIdByLoginName(((V3xOrgMember) o.getEnt()).getV3xOrgPrincipal().getLoginName()));
                    String dupAccountName = showOrgAccountName(dupLoginNameMember.getOrgAccountId());
                    throw new BusinessException(ResourceUtil.getString("MessageStatus.PRINCIPAL_REPEAT_NAME", dupAccountName, dupLoginNameMember.getName()));
                } catch (NoSuchPrincipalException e) {
                    log.error("根据人员登录名获取人员id异常"+e.getLocalizedMessage(), e);
                    throw new BusinessException(ResourceUtil.getString("MessageStatus.PRINCIPAL_REPEAT_NAME"));
                }
			case 31:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.LEVEL_EXIST_MEMBER"));
			case 32:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.REPEAT_PATH"));
			case 33:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.PRINCIPAL_NOT_EXIST"));
			case 34:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ROLE_NOT_EXIST"));
			case 35:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.POST_EXIST_BENCHMARK"));
			case 36:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.OUT_PER_NUM"));
			case 37:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.LEVEL_REPEAT_NAME"));
			case 38:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_MEMBER_ENABLE", o.getEnt().getName()));
			case 39:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_DEPARTMENT_ENABLE"));
			case 40:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_ROLE_ENABLE"));
			case 41:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_POST_ENABLE"));
			case 42:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_LEVEL_ENABLE"));
			case 43:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_CHILDACCOUNT_ENABLE"));
			case 44:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_TEAM_ENABLE"));
			case 45:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.MEMBER_REPEAT_CODE"));
			case 46:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPARTMENT_REPEAT_CODE"));
			case 47:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.MEMBER_CANNOT_DELETE", o.getEnt().getName()));
			case 48:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_CUSTOM_LOGIN_URL_DUPLICATED"));
			case 49:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPARTMENT_EXIST_DEPARTMENT_ENABLE"));
			case 50:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.ACCOUNT_VALID_SUPERACCOUNT_DISABLE"));
			case 51:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.DEPARTMENT_EXIST_EXTDEPARTMENT_ENABLE"));
			case 52:
                throw new BusinessException(ResourceUtil.getString("MessageStatus.MEMBER_EXTERNALACCOUNT_DISABLED"));

			default:
				throw new BusinessException(ResourceUtil.getString("MessageStatus.ERROR"));
			}
		}
    }
	
	
	/**
	 * 处理组织模型接口抛出的业务异常编码，保证不在核心Manager抛出业务异常,http正常返回200,不通过500异常处理，保证前端异常能够正常提示。
	 * @param m
	 * @return
	 * @throws BusinessException
	 */
	public static Map getBusinessExceptionMessage(OrganizationMessage m) throws BusinessException  {
		Map result = new HashMap();
		List<OrgMessage> mErrorList = m.getErrorMsgs();
		if(!(mErrorList.size() > 0)) {
			if(m.getErrorMsgInfos().size()>0){
				result.put(OrganizationMessage.MessageStatus.SUCCESS.name(), "false");
				result.put("msg", m.getErrorMsgInfos().get(0).getMsgInfo());
			}else{
				result.put(OrganizationMessage.MessageStatus.SUCCESS.name(), "true");
			}
			return result;
		}
		
		result.put(OrganizationMessage.MessageStatus.SUCCESS.name(), "false");
    	for (OrgMessage o : mErrorList) {
			switch (o.getCode().ordinal()) {
			case 1:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_REPEAT_NAME"));
				break;
			case 2:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_REPEAT_SHORT_NAME"));
				break;
			case 3:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_REPEAT_CODE"));
				break;
			case 4:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_REPEAT_ADMIN_NAME"));
				break;
			case 5:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_ENTITY"));
				break;
			case 6:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_DEPARTMENT_ENABLE"));
				break;
			case 7:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_ROLE_ENABLE"));
				break;
			case 8:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_POST_ENABLE"));
				break;
			case 9:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_LEVEL_ENABLE"));
				break;
			case 10:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_CHILDACCOUNT"));
				break;
			case 11:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_TEAM_ENABLE"));
				break;
			case 12:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_MEMBER_ENABLE", o.getEnt().getName()));
				break;
			case 13:
				result.put("msg", ResourceUtil.getString("MessageStatus.DEPARTMENT_REPEAT_NAME", o.getEnt().getName()));
				break;
			case 14:
				result.put("msg", ResourceUtil.getString("MessageStatus.DEPARTMENT_EXIST_MEMBER", o.getEnt().getName()));
				break;
			case 15:
				result.put("msg", ResourceUtil.getString("MessageStatus.DEPARTMENT_EXIST_TEAM", o.getEnt().getName()));
				break;
			case 16:
				result.put("msg", ResourceUtil.getString("MessageStatus.DEPARTMENT_PARENTID_NULL"));
				break;
			case 17:
				result.put("msg", ResourceUtil.getString("MessageStatus.DEPARTMENT_PARENTDEPT_DISABLED"));
				break;
			case 18:
				result.put("msg", ResourceUtil.getString("MessageStatus.DEPARTMENT_PARENTDEPT_SAME"));
				break;
			case 19:
				result.put("msg", ResourceUtil.getString("MessageStatus.DEPARTMENT_PARENTDEPT_ISCHILD"));
				break;
			case 20:
				result.put("msg", ResourceUtil.getString("MessageStatus.POST_REPEAT_NAME", o.getEnt().getName()));
				break;
			case 21:
				result.put("msg", ResourceUtil.getString("MessageStatus.POST_EXIST_MEMBER"));
				break;
			case 22:
				result.put("msg", ResourceUtil.getString("MessageStatus.LEVEL_EXIST_MEMBER", o.getEnt().getName()));
				break;
			case 23:
				result.put("msg", ResourceUtil.getString("MessageStatus.LEVEL_EXIST_MAPPING", o.getEnt().getName()));
				break;
			case 24:
				result.put("msg", ResourceUtil.getString("MessageStatus.MEMBER_DEPARTMENT_DISABLED"));
				break;
			case 25:
				result.put("msg", ResourceUtil.getString("MessageStatus.MEMBER_POST_DISABLED"));
				break;
			case 26:
				result.put("msg", ResourceUtil.getString("MessageStatus.MEMBER_LEVEL_DISABLED"));
				break;
			case 27:
				result.put("msg", ResourceUtil.getString("MessageStatus.MEMBER_REPEAT_POST"));
				break;
			case 28:
				result.put("msg", ResourceUtil.getString("MessageStatus.MEMBER_EXIST_SIGNET"));
				break;
			case 29:
				result.put("msg", ResourceUtil.getString("MessageStatus.MEMBER_NOT_EXIST"));
				break;
			case 30:
                try {
                    V3xOrgMember dupLoginNameMember = getOrgManager().getMemberById(getPrincipalManager().getMemberIdByLoginName(((V3xOrgMember) o.getEnt()).getV3xOrgPrincipal().getLoginName()));
                    String dupAccountName = showOrgAccountName(dupLoginNameMember.getOrgAccountId());
                    result.put("msg", ResourceUtil.getString("MessageStatus.PRINCIPAL_REPEAT_NAME", dupAccountName, dupLoginNameMember.getName()));
                    break;
                } catch (NoSuchPrincipalException e) {
                    log.error("根据人员登录名获取人员id异常"+e.getLocalizedMessage(), e);
                    result.put("msg", ResourceUtil.getString("MessageStatus.PRINCIPAL_REPEAT_NAME"));
                    break;
                }
			case 31:
				result.put("msg", ResourceUtil.getString("MessageStatus.LEVEL_EXIST_MEMBER"));
				break;
			case 32:
				result.put("msg", ResourceUtil.getString("MessageStatus.REPEAT_PATH"));
				break;
			case 33:
				result.put("msg", ResourceUtil.getString("MessageStatus.PRINCIPAL_NOT_EXIST"));
				break;
			case 34:
				result.put("msg", ResourceUtil.getString("MessageStatus.ROLE_NOT_EXIST"));
				break;
			case 35:
				result.put("msg", ResourceUtil.getString("MessageStatus.POST_EXIST_BENCHMARK"));
				break;
			case 36:
				result.put("msg", ResourceUtil.getString("MessageStatus.OUT_PER_NUM"));
				break;
			case 37:
				result.put("msg", ResourceUtil.getString("MessageStatus.LEVEL_REPEAT_NAME"));
				break;
			case 38:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_MEMBER_ENABLE", o.getEnt().getName()));
				break;
			case 39:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_DEPARTMENT_ENABLE"));
				break;
			case 40:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_ROLE_ENABLE"));
				break;
			case 41:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_POST_ENABLE"));
				break;
			case 42:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_LEVEL_ENABLE"));
				break;
			case 43:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_CHILDACCOUNT_ENABLE"));
				break;
			case 44:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_EXIST_TEAM_ENABLE"));
				break;
			case 45:
				result.put("msg", ResourceUtil.getString("MessageStatus.MEMBER_REPEAT_CODE"));
				break;
			case 46:
				result.put("msg", ResourceUtil.getString("MessageStatus.DEPARTMENT_REPEAT_CODE"));
				break;
			case 47:
				result.put("msg", ResourceUtil.getString("MessageStatus.MEMBER_CANNOT_DELETE", o.getEnt().getName()));
				break;
			case 48:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_CUSTOM_LOGIN_URL_DUPLICATED"));
				break;
			case 49:
				result.put("msg", ResourceUtil.getString("MessageStatus.DEPARTMENT_EXIST_DEPARTMENT_ENABLE"));
				break;
			case 50:
				result.put("msg", ResourceUtil.getString("MessageStatus.ACCOUNT_VALID_SUPERACCOUNT_DISABLE"));
				break;
			case 51:
				result.put("msg", ResourceUtil.getString("MessageStatus.DEPARTMENT_EXIST_EXTDEPARTMENT_ENABLE"));
				break;
			case 52:
				result.put("msg", ResourceUtil.getString("MessageStatus.MEMBER_EXTERNALACCOUNT_DISABLED"));
				break;

			default:
				result.put("msg", ResourceUtil.getString("MessageStatus.ERROR"));
			}
		}
    	
    	return result;
    }

    /**
    * 比较两个单位是否可见
    * @param sAccountId 当前单位ID
    * @param tAccountId 被比较的单位ID
    * @return true可以访问false不可以访问
    * @throws BusinessException
    */
    public static Boolean checkAccountAccess(Long sAccountId, Long tAccountId) throws BusinessException {
        if (null == sAccountId || null == tAccountId)
            throw new BusinessException("检查单位间可见范围异常传入的ID为NULL");
        if (sAccountId.equals(tAccountId))
            return Boolean.TRUE;//如果传入的单位已比较的单位相同则直接返回true
        Boolean b = Boolean.FALSE;
        List<V3xOrgRelationship> rels = getOrgManager().getV3xOrgRelationship(
                OrgConstants.RelationshipType.Account_AccessScope, sAccountId, null, null);
        for (V3xOrgRelationship rel : rels) {
            if (rel.getObjective0Id() == null)
                return Boolean.FALSE;//如果关系表中没有保存objective0Id则为全部不能访问返回false
            if (tAccountId.equals(rel.getObjective0Id())) {
                if (OrgConstants.Account_AccessScope_Type.CAN_ACCESS.name().equals(rel.getObjective5Id().trim()))
                    return Boolean.TRUE;
                if (OrgConstants.Account_AccessScope_Type.NOT_ACCESS.name().equals(rel.getObjective5Id().trim()))
                    return Boolean.FALSE;
            }
        }
        return b;
    }

    /**
     * 根据单位id判断某单位下是否还有子单位
     * @param accountId
     * @return
     * @throws BusinessException
     */
    public static Boolean isHasChildAccountsById(Long accountId) throws BusinessException {
        Boolean result = false;
        List<V3xOrgAccount> childs = getOrgManager().getChildAccount(accountId, false);
        if (!childs.isEmpty() && childs.size() > 0) {
            result = true;
        }
        return result;
    }

    /**
     * 根据部门id判断某部门下是否还有子部门
     * @param deptId
     * @param isInteranl true为内部部门false外部部门
     * @return
     * @throws BusinessException
     */
    public static Boolean isHasChildDeptsByDeptId(Long deptId, boolean isInteranl) throws BusinessException {
        Boolean result = false;
        List<V3xOrgDepartment> childs = getOrgManager().getChildDepartments(deptId, true, isInteranl);
        if (!childs.isEmpty()) {
            result = true;
        }
        return result;
    }

    /**
     * 检测工作范围
     *
     * @param currentMemberId 当前登录者
     * @param memberId 被访问人
     * @return true:当前登录者可以访问被访问人
     */
    public static boolean checkLevelScope(Long currentMemberId, Long memberId){
        try {
            V3xOrgMember currentMember = getOrgManager().getMemberById(currentMemberId);
            V3xOrgMember member = getOrgManager().getMemberById(memberId);
            return checkLevelScope(currentMemberId,currentMember.getOrgAccountId(),memberId ,member.getOrgAccountId());
        }catch (Exception e) {
            log.warn("检测工作范围", e);
        }
        return false;
    }
    /**
     * 检测工作范围
     *
     * @param currentMemberId 当前登录者
     * @param memberId 被访问人
     * @return true:当前登录者可以访问被访问人
     */
    public static boolean checkLevelScope(Long currentMemberId, Long curAtAccountId, Long memberId ,Long memberAtAccountId){
        OrgManager orgManager = getOrgManager();
        try {
            V3xOrgMember currentMember = orgManager.getMemberById(currentMemberId); // 当前登录者
            V3xOrgMember member = orgManager.getMemberById(memberId); // 被检测的人
            if (currentMember == null) {
                return false;
            }
            
            if(currentMember.isVJoinExternal() || member.isVJoinExternal()){
            	return orgManager.checkLevelForExternal(currentMemberId,memberId);
            }
            //外部人员访问内部人员
            if (!currentMember.getIsInternal() && member.getIsInternal()) {
                Collection<V3xOrgMember> canAccessMembers = OuterWorkerAuthUtil.getCanAccessMembers(currentMemberId,
                        currentMember.getOrgDepartmentId(), currentMember.getOrgAccountId(), orgManager);
                if (canAccessMembers.contains(member)) {
                    return true;
                }
            }
            //内部人员访问外部人员
            if (currentMember.getIsInternal() && !member.getIsInternal()) {
                return canReadOuter(currentMemberId,memberId);
            }
            if (member == null) {
                return false;
            }

            if(currentMember.getIsAdmin())
                return true;

            if(currentMember.getIsAdmin() || member.getIsAdmin()){ //管理员不能发送消息
                return false;
            }

            //同一个部门,或者当前人员是要访问的人员的父部门人员
            if(currentMember.getOrgDepartmentId().longValue() == member.getOrgDepartmentId().longValue() ||
                    orgManager.isInDepartmentPathOf(currentMember.getOrgDepartmentId(), member.getOrgDepartmentId())){
                return true;
            }

            //相同的职务级别
            if(currentMember.getOrgLevelId().longValue() == member.getOrgLevelId().longValue()){
                return true;
            }

            //内部人员都可以看到外部人员
            if(currentMember.getOrgLevelId() == -1 || member.getOrgLevelId() == -1){
                return currentMember.getOrgLevelId() != -1;
            }

            // 副岗在这个部门的有权限
            if (MemberHelper.isSndPostContainDept(currentMember, getChildD(member.getOrgDepartmentId()))) {
                return true;
            }
            if (MemberHelper.isSndPostContainDept(member, getChildD(currentMember.getOrgDepartmentId()))) {
                return true;
            }

            //映射集团职务级别
            int currentAccountLevelScope = orgManager.getAccountById(currentMember.getOrgAccountId()).getLevelScope();
            if ((currentMember.getOrgDepartmentId().equals(member.getOrgDepartmentId()))) {
                //Fix OA-38367 2013-05-22 lilong 去除这里的所在单位如果不控制工作范围，切换单位就不控制的代码，保证与选人界面一致
                return true;
            }

            //切换的单位级别范围
            memberAtAccountId = memberAtAccountId == null ? member.getOrgAccountId() : memberAtAccountId;
            V3xOrgAccount tmp = orgManager.getAccountById(memberAtAccountId);
            if (null == tmp
                    || "null".equals(String.valueOf(tmp.getLevelScope()))) {
                return false;
            }
            int newAccountLevelScope = tmp.getLevelScope();
            if (newAccountLevelScope < 0) {
                return true;
            }
            int currentMemberLevelSortId=0;
            int accountLevelScope=0;
            V3xOrgLevel memberLevel = orgManager.getLevelById(member.getOrgLevelId());
            int memberLevelSortId = memberLevel!=null ? memberLevel.getLevelId() : 0;
            if(!Strings.equals(member.getOrgAccountId(),memberAtAccountId)){
                Map<Long, List<MemberPost>> concurrentPostMap = getOrgManager().getConcurentPostsByMemberId(memberAtAccountId, memberId);
                if (concurrentPostMap != null && !concurrentPostMap.isEmpty()) {
                    Iterator<List<MemberPost>> it = concurrentPostMap.values().iterator();
                    while (it.hasNext()) {
                        boolean isExist = false;
                        List<MemberPost> cnPostList = it.next();
                        for (MemberPost cnPost : cnPostList) {
                            if(cnPost.getLevelId()!= null){
                                memberLevelSortId = orgManager.getLevelById(cnPost.getLevelId()).getLevelId();
                                isExist = true;
                                break;
                            }
                        }
                        if (isExist) {
                            break;
                        }
                    }
                }
            }
            if(currentMember.getOrgAccountId().equals(memberAtAccountId)){
                V3xOrgLevel currentMemberLevel = orgManager.getLevelById(currentMember.getOrgLevelId());
                currentMemberLevelSortId = currentMemberLevel!=null ? currentMemberLevel.getLevelId() : 0;
                accountLevelScope = currentAccountLevelScope;
            }else{
                currentMemberLevelSortId = mappingLevelSortId(member,memberAtAccountId, currentMember,curAtAccountId);
                accountLevelScope = newAccountLevelScope;
            }
            if(currentMemberLevelSortId - memberLevelSortId <= accountLevelScope) {
                return true;
            }
        }
        catch (Exception e) {
            log.warn("检测工作范围", e);
        }

        return false;
    }

    public static Set<Long> getChildD(Long deptId) throws BusinessException{
        List<V3xOrgDepartment>  departs= getOrgManager().getChildDepartments(deptId, false);
        Set<Long> deptids = new HashSet<Long>();
        deptids.add(deptId);
        for(V3xOrgDepartment dept:departs){
            deptids.add(dept.getId());
        }
        return deptids;
    }

    //映射集团职务   by wusb 2010-09-25
    public static int mappingLevelSortId(V3xOrgMember member,Long memberAtAccountId, V3xOrgMember currentMember,Long curAtAccountId) throws BusinessException{

        int currentMemberLevelSortId=0;
        V3xOrgLevel level = null;
        User user = AppContext.getCurrentUser();
        boolean isNeedCheckLevelScope=true;
        if(user.isAdministrator() || user.isGroupAdmin() || user.isSystemAdmin()){ //管理员默认不限制
            isNeedCheckLevelScope = false;
        }
        if (isNeedCheckLevelScope) {
            Map<Long, List<MemberPost>> concurrentPostMap = getOrgManager().getConcurentPostsByMemberId(
                    memberAtAccountId, currentMember.getId());
            if (concurrentPostMap != null && !concurrentPostMap.isEmpty()) { //我在当前单位兼职
                Iterator<List<MemberPost>> it = concurrentPostMap.values().iterator();
                boolean isExist = false;
                while (it.hasNext()) {
                    List<MemberPost> cnPostList = it.next();
                    for (MemberPost cnPost : cnPostList) {
                        if (cnPost != null) {
                            //如果这个memberAtAccountId是member的兼职单位,不准!!!
                            if(cnPost.getDepId().equals(member.getOrgDepartmentId())) {
                                return 0;
                            }
                            Long cnLevelId = cnPost.getLevelId();
                            if (cnLevelId != null) {
                                V3xOrgLevel cnLevel = getOrgManager().getLevelById(cnLevelId);
                                if (cnLevel != null) {
                                    currentMemberLevelSortId = cnLevel.getLevelId();
                                    isExist = true;
                                    break;
                                } else {
                                    level = getOrgManager().getLowestLevel(member.getOrgAccountId());
                                    currentMemberLevelSortId = level != null ? level.getLevelId().intValue() : 0;
                                }
                            }else{
                            	currentMemberLevelSortId = getOrgManager().getLowestLevel(memberAtAccountId).getLevelId();//最低职务级别
                            }
                        }
                    }
                    if (isExist) {
                        break;
                    }
                }
                return currentMemberLevelSortId;
            }
/*            //当前人员查看兼职到自己单位的人
            Map<Long, List<MemberPost>> concurrentPostMap2 = getOrgManager().getConcurentPostsByMemberId(
                    curAtAccountId, member.getId());
            if(concurrentPostMap2 != null && !concurrentPostMap2.isEmpty()) {
                Iterator<List<MemberPost>> it = concurrentPostMap2.values().iterator();
                while (it.hasNext()) {
                    List<MemberPost> cnPostList = it.next();
                    for (MemberPost memberPost : cnPostList) {
                        Long cnLevelId = memberPost.getLevelId();
                        V3xOrgLevel cnLevel = getOrgManager().getLevelById(cnLevelId);
                        if(null == cnLevel) {
                            level = getOrgManager().getLowestLevel(currentMember.getOrgAccountId());
                            return level != null ? level.getLevelId().intValue() : 0;
                        } else {
                            return cnLevel.getLevelId();
                        }
                    }
                }
            }*/

            Long mappingGroupId = getOrgManager().getLevelById(currentMember.getOrgLevelId()).getGroupLevelId();
            Long levelIdOfGroup = (!currentMember.getOrgLevelId().equals(-1L)) ? mappingGroupId : Long.valueOf(-1); //当前登录者对应集团的职务级别id
            //切换单位的所有职务级别
            List<V3xOrgLevel> levels = getOrgManager().getAllLevels(memberAtAccountId);
            for (V3xOrgLevel lvl : levels) {
                if (levelIdOfGroup != null && levelIdOfGroup.equals(lvl.getGroupLevelId())) {
                    level = lvl;
                    break;
                }
            }
            if (level == null) {
                level = getOrgManager().getLowestLevel(memberAtAccountId); //最低职务级别
            }

            if (level != null) {
                currentMemberLevelSortId = level.getLevelId();
            }
        }
        return currentMemberLevelSortId;
    }

    /**
     * 内部人员访问外部人员，同时检查这个外部人员的工作范围
     * @param memberId
     * @param outerId
     * @return
     * @throws BusinessException
     */
    public static boolean canReadOuter(Long memberId, Long outerId) throws BusinessException {
        List<V3xOrgMember> canReadList = getOrgManager().getMemberWorkScopeForExternal(memberId,false);
        for (V3xOrgMember m : canReadList) {
            if(Strings.equals(m.getId(), outerId)) {
                return true;
            }
        }
        return false;
    }





    /**
     * 显示人员名字
     *
     * @param member
     * @param isShowAccountName 是否显示单位检查：如果不是一个单位的，则显示单位简称
     * @param accountAdminShowAccountName 单位管理员是否显示单位名称
     * @return
     */
    private static String showMemberName0(V3xOrgMember member, boolean isShowAccountName, boolean accountAdminShowAccountName){
        if(member == null){
            return null;
        }

        try {
            long memberId = member.getId();
            if(member.getIsAdmin()){
                //集团管理员
                if(getOrgManager().isGroupAdminById(memberId)){
                    return ResourceUtil.getString("org.account_form.groupAdminName.value" + suffix());
                }

                //审计管理员
                if(getOrgManager().isAuditAdminById(memberId)){
                    return ResourceUtil.getString("org.auditAdminName.value");
                }

                //单位管理员
                if(getOrgManager().isAdministratorById(memberId, member.getOrgAccountId())){
                    if(accountAdminShowAccountName && (Boolean)SysFlag.selectPeople_showAccounts.getFlag()){
                        V3xOrgAccount account = getAccount(member.getOrgAccountId());
                        if(account != null){
                            return Strings.escapeNULL(account.getShortName(), account.getName()) + ResourceUtil.getString("org.account_form.adminName.value");
                        }
                    }

                    return ResourceUtil.getString("org.account_form.adminName.value");
                }
                
                if(getOrgManager().isSystemAdminById(memberId)){
                    return ResourceUtil.getString("org.account_form.systemAdminName.value");
                }
            }
            
            if(OrgConstants.GUEST_ID.equals(memberId)){
            	return ResourceUtil.getString("org.guest.default.account.name");
            }

            if(!isShowAccountName || !(Boolean)SysFlag.selectPeople_showAccounts.getFlag()){
                return member.getName();
            }

            User user = AppContext.getCurrentUser();

            if(user == null || Strings.equals(user.getLoginAccount(), member.getOrgAccountId())){ //同一个单位的
                return member.getName();
            }
            else{
                V3xOrgAccount account = getAccount(member.getOrgAccountId());
                if(account == null){
                    return member.getName();
                }

                return member.getName() + "(" + account.getShortName() + ")";
            }
        }
        catch (Exception e) {
            log.warn(e.getLocalizedMessage(), e);
        }

        return member.getName();
    }
    /**
     * 显示人员名字，如果不是一个单位的，则显示单位简称
     *
     * @param member
     * @return
     */
    public static String showMemberName(V3xOrgMember member){

        return showMemberName0(member, true, true);
    }

    /**
     * 国际化Label的后缀， 用于支持政务版的key.<br>
     * 政务版与集团版的Label不同时，需要增加一个以.GOV区分的后缀，引用该key后附加这个<br>
     * 如：<fmt:message key='menu.group.info.set${v3x:suffix()}'/>
     * 集团版引用key：menu.group.info.set<br>
     * 政务版key为：menu.group.info.set.GOV
     * @return
     */
    public static String suffix(){
       return (String)SysFlag.EditionSuffix.getFlag();
    }
    public static String showOrgAccountName(Long accountId){
        V3xOrgAccount account = getAccount(accountId) ;
        try{
            if(account != null && account.isGroup()){
                return "-" ;
            }
            if(account==null){
                return "-" ;
            }
            return account.getName() ;
        }catch(Exception e){
            log.error("得到单位的为null") ;
            return "-" ;
        }
    }
    public static V3xOrgAccount getAccount(Long accountId) {
        try {
            return getOrgManager().getAccountById(accountId);
        }
        catch (Exception e) {
            log.warn("", e);
            return null;
        }
    }
    
	 /**
     * 将组织模型实体列表变成ID和类型列表，主要用于组的组员信息处理
     * @param entityList 实体列表
     * @return ID列表
     */
    public static List<OrgTypeIdBO> entityListToIdTypeList(List<V3xOrgEntity> entityList) {
    	return entityListToIdTypeList(entityList,null);
    }
    
	 /**
     * 将组织模型实体列表变成ID和类型列表，主要用于组的组员信息处理
     * @param entityList 实体列表
     * @return ID列表
     */
    public static List<OrgTypeIdBO> mapToIdTypeList(Map<String,Map<String,String>> map) {
    	List<OrgTypeIdBO> idList = new ArrayList<OrgTypeIdBO>();
    	for (String id : map.keySet()) {
    		OrgTypeIdBO typeIdBO = new OrgTypeIdBO();
    		typeIdBO.setId(id);
    		typeIdBO.setType(map.get(id).get("type"));
    		typeIdBO.setInclude(map.get(id).get("include"));
    		idList.add(typeIdBO);
    	}
    	return idList;
    }
    
    /**
     * 将组织模型实体列表变成ID和类型列表，主要用于组的组员信息处理
     * @param entityList 实体列表
     * @return ID列表
     */
    public static List<OrgTypeIdBO> entityListToIdTypeList(List<V3xOrgEntity> entityList,Map<Long, String> map) {
    	List<OrgTypeIdBO> idList = new ArrayList<OrgTypeIdBO>();
    	for (V3xOrgEntity ent : entityList) {
    		OrgTypeIdBO typeIdBO = new OrgTypeIdBO();
    		typeIdBO.setId(ent.getId());
    		typeIdBO.setType(ent.getEntityType());
    		if(map!=null && map.get(ent.getId())!=null){
    			typeIdBO.setInclude(map.get(ent.getId()));
    		}
    		idList.add(typeIdBO);
    	}
    	return idList;
    }
    
    /**
     * 根据entityType 获取对应 entity类
     * @param entityType
     * @return
     */
    public static <T extends V3xOrgEntity> Class<T> getV3xClass(String entityType){
        if(OrgConstants.ORGENT_TYPE.Member.name().equals(entityType)  || "Guest".equals(entityType) || entityType == null){
            return (Class<T>) V3xOrgMember.class;
        }else
        if(OrgConstants.ORGENT_TYPE.Department.name().equals(entityType)){
            return (Class<T>) V3xOrgDepartment.class;
        }else
        if(OrgConstants.ORGENT_TYPE.Post.name().equals(entityType)){
            return (Class<T>) V3xOrgPost.class;
        }else
        if(OrgConstants.ORGENT_TYPE.Level.name().equals(entityType)){
            return (Class<T>) V3xOrgLevel.class;
        }else
        if(OrgConstants.ORGENT_TYPE.Team.name().equals(entityType)){
            return (Class<T>) V3xOrgTeam.class;
        }else
        if(OrgConstants.ORGENT_TYPE.Account.name().equals(entityType)){
            return (Class<T>) V3xOrgAccount.class;
        }
     return null;   
    }
    
    /**
     * 根据部门和单位id构造成一棵部门树
     * @param list
     * @param accountId
     * @return
     * @throws BusinessException
     */
    public static OrgTree getTree(List<V3xOrgDepartment> list, Long accountId) throws BusinessException {
        List<OrgTreeNode> orgTreeNodes = OrgTree.changeEnititiesToOrgTreeNodes(list);
        V3xOrgAccount currentAccont = getOrgManager().getAccountById(accountId);
        String accountPath =currentAccont.getPath();
        OrgTree orgTree = new OrgTree(orgTreeNodes,accountPath);
        return orgTree;
    }
    
    /**
     * 根据member查其单位上级部门--查三代
     * @param bo
     * @return
     * @throws BusinessException
     */
	
	public static String deptPName(Long departmentId) throws BusinessException{
    	List<V3xOrgDepartment> pDept = getOrgManager().getAllParentDepartments(departmentId);
        String pname = "";
        int length = pDept.size();
        if(length>1){
        	pname = pDept.get(length-2).getName()+"/"+ pDept.get(length-1).getName()+"/";
        }else if(length>0){
        	for(int i=length;i>0;i--){
        		pname += pDept.get(i-1).getName()+"/";
        	}
        }
        pname = pname+ getOrgManager().getDepartmentById(departmentId).getName();
        
        return pname;
    }
	   
    /**
     * 设置当前线程的操作不更新组织模型时间戳
     */
    public static void notUpdateModifyTimestampOfCurrentThread(){
        AppContext.putThreadContext("notUpdateModifyTimestampOfCurrentThread", true);
    }

    /**
     * 判断当前线程的操作是否要更新组织模型时间戳
     * @return true:要更新
     */
    public static boolean isUpdateModifyTimestampOfCurrentThread(){
        return !Strings.equals(AppContext.getThreadContext("notUpdateModifyTimestampOfCurrentThread"), Boolean.TRUE);
    }

    /**
     * 获取V-Join准出单位
     * @return
     * @throws BusinessException
     */
    public static Long getVJoinAllowAccount() throws BusinessException {
        boolean isEnterprise = (Boolean) SysFlag.sys_isEnterpriseVer.getFlag();
        if (isEnterprise) {
            return OrgConstants.ACCOUNTID;
        }

        Long accountId = null;
        ConfigItem configItem = getConfigManager().getConfigItem("v_join", "accountId");
        if (configItem != null) {
            String accountIds = configItem.getConfigValue();
            if (Strings.isNotBlank(accountIds)) {
                String[] jAccountIds = accountIds.split(",");
                for (String jAccountId : jAccountIds) {
                    accountId = Long.parseLong(jAccountId);
                }
            }
        }
        return accountId;
    }

	/**
	 * 将选人界面返回的字符串解析为具体的人   Member|1234567,Departmet|2345678...  -> List<V3xOrgMember>
	 * @return
	 * @throws BusinessException 
	 */
	public static List<V3xOrgMember> getMembersByElements(String typeAndIds) throws BusinessException{
		List<V3xOrgMember> members = new UniqueList<V3xOrgMember>();
		if(Strings.isBlank(typeAndIds)){
			return members;
		}
		String[] typeAndIdArr = typeAndIds.split(",");
		for(String typeAndId : typeAndIdArr){
			String[] tAi = typeAndId.split("\\|");
			String type = tAi[0];
			Long id = Long.valueOf(tAi[1]);
			V3xOrgEntity m = getOrgCache().getV3xOrgEntity(getV3xClass(type), id);
			if (m != null && m.isValid()) {
				if (m instanceof V3xOrgMember) {
					members.add((V3xOrgMember) m);
				} else if (m instanceof V3xOrgDepartment) {
					boolean firtLayer = false;
					if(tAi.length>=3 && "1".equals(tAi[2])){
						firtLayer = true;
					}
					members.addAll(getOrgManager().getMembersByDepartment(m.getId(), firtLayer));
				} else if (m instanceof V3xOrgPost) {
					members.addAll(getOrgManager().getMembersByPost(m.getId()));
				} else if (m instanceof V3xOrgLevel) {
					members.addAll(getOrgManager().getMembersByLevel(m.getId()));
				} else if (m instanceof V3xOrgTeam) {
					members.addAll(getOrgManager().getMembersByTeam(m.getId()));
				}else if (m instanceof V3xOrgAccount) {
					members.addAll(getOrgManager().getAllMembers(m.getId()));
				}
			}
		}
		return members;
	}

}

class EntityPropertyHelper
{
	private final static Log log = LogFactory
			.getLog(EntityPropertyHelper.class);
	private static EntityPropertyHelper INSTANCE ;
	private static Map<String,EntityPropertyGetter<? extends V3xOrgEntity>> map = new HashMap<String, EntityPropertyGetter<? extends V3xOrgEntity>>();

	private EntityPropertyHelper() throws BusinessException
	{
		init();
	}
	void init() throws BusinessException
	{
		// 通用
		register("id", new EntityPropertyGetter<V3xOrgEntity>() {
			public Object getValue(V3xOrgEntity ent) {
				return ent.getId();
			}
		});
		/**
		 * Entity getName实现。
		 */
		register("name", new EntityPropertyGetter<V3xOrgEntity>() {
			public Object getValue(V3xOrgEntity ent) {
				return ent.getName();
			}
		});

		/**
		 * Entity getOrgAccountId实现。
		 */
		register("orgAccountId", new EntityPropertyGetter<V3xOrgEntity>() {
			public Object getValue(V3xOrgEntity ent) {
				return ent.getOrgAccountId();
			}
		});
		// 特殊
		/**
		 * Member getOrgPostId实现。
		 */
		register(V3xOrgMember.class, "orgPostId", new EntityPropertyGetter<V3xOrgMember>() {
			public Object getValue(V3xOrgMember ent) {
				return ent.getOrgPostId();
			}
		});
		/**
		 * Level getGroupLevelId实现。
		 */
		register(V3xOrgLevel.class, "groupLevelId", new EntityPropertyGetter<V3xOrgLevel>() {
			public Object getValue(V3xOrgLevel ent) {
				return ent.getGroupLevelId();
			}
		});

		/**
		 * Member getCode实现。
		 */
		register(buildKey(V3xOrgMember.class, "code"),
				new EntityPropertyGetter<V3xOrgMember>() {
					public Object getValue(V3xOrgMember entity) {
						return entity.getCode();
					}
				});
		/**
		 * Member getLoginName实现。
		 */
//		register(buildKey(V3xOrgMember.class, "loginName"),
//				new EntityPropertyGetter<V3xOrgMember>() {
//					public Object getValue(V3xOrgMember entity) {
//						return entity.getLoginName();
//					}
//				});

		/**
		 * Member getOrgDepartmentId实现。
		 */
		register(buildKey(V3xOrgMember.class, "orgDepartmentId"),
				new EntityPropertyGetter<V3xOrgMember>() {
					public Object getValue(V3xOrgMember entity) {
						return entity.getOrgDepartmentId();
					}
				});
		/**
		 * Member getOrgLevelId实现。
		 */
		register(buildKey(V3xOrgMember.class, "orgLevelId"),
				new EntityPropertyGetter<V3xOrgMember>() {
					public Object getValue(V3xOrgMember entity) {
						return entity.getOrgLevelId();
					}
				});

	}

	private void register(Class<?> clazz,String property,EntityPropertyGetter<? extends V3xOrgEntity> getter) throws BusinessException
	{
		String key = clazz.getSimpleName() + property;
		register(key, getter);
	}
	private void register(String key, EntityPropertyGetter<? extends V3xOrgEntity> getter) {
		map.put(key,getter);
	}
	private EntityPropertyGetter<? extends V3xOrgEntity> getFactory(V3xOrgEntity ent,String property) throws BusinessException
	{
		// 先取通用的，再取专用的
		EntityPropertyGetter<? extends V3xOrgEntity> factory  = map.get(property);
		return factory==null?map.get(buildKey(ent,property)) :factory;
	}
	private String buildKey(V3xOrgEntity ent,String property) throws BusinessException
	{
		return buildKey(ent.getClass(),property);
	}
	private String buildKey(Class<? extends V3xOrgEntity> clazz,String property) throws BusinessException
	{
		return clazz.getSimpleName() + property;
	}
	interface EntityPropertyGetter<T extends V3xOrgEntity>
	{
		Object getValue(T ent);
	}
	/**
	 * 获取指定Entity的指定属性的值。
	 *
	 * @param entity
	 *            组织模型实体
	 * @param property
	 *            属性，如name、code等。
	 * @return 属性的值。
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getValue(V3xOrgEntity ent,String property) throws BusinessException
	{
		EntityPropertyGetter factory = getFactory(ent,property);
		if(factory!=null) return factory.getValue(ent);
		log.warn("使用反射取" + ent.getClass().getName() + "的" + property
				+ "。");
		// 如果没有对应实现，则使用反射。
		try {
			return PropertyUtils.getProperty(ent, property);
		} catch (Exception e) {
			throw new BusinessException(e);
		}
	}
	public static EntityPropertyHelper getInstance() throws BusinessException {
		if(INSTANCE==null) INSTANCE = new EntityPropertyHelper();
		return INSTANCE;
	}
}
