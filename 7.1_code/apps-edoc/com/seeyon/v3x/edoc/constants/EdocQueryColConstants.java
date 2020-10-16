package com.seeyon.v3x.edoc.constants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.seeyon.ctp.common.constants.SystemProperties;

public class EdocQueryColConstants {
    public static String isG6=SystemProperties.getInstance().getProperty("edoc.isG6");
	public static Map<Integer,String> queryColMap = new HashMap<Integer,String>();
	//发文登记簿查询需要显示的公文要素列
	public static Map<Integer,String> sendEdocColMap = new LinkedHashMap<Integer,String>();
	//收文登记簿查询需要显示的公文要素列
	public static Map<Integer,String> recEdocColMap = new LinkedHashMap<Integer,String>();
	//自定义查询需要显示的公文要素列
	public static Map<Integer,String> sendSearchEdocColMap = new LinkedHashMap<Integer,String>();
	//签报
	public static Map<Integer,String> signSearchEdocColMap = new LinkedHashMap<Integer,String>();
	//收文
	public static Map<Integer,String> recSearchEdocColMap = new LinkedHashMap<Integer,String>();
	
	/*
     * SP1额外增加公文查询列表头显示所有的查询条件
     */
    public static Map<Integer, String> addedSearchLabel = new LinkedHashMap<Integer, String>();
	
	public static String SEND_WORD = "edoc.element.wordno.label";
	public static String SEND_SUBJECT = "edoc.element.subject";
	public static String SEND_SENDINGDATE = "edoc.element.sendingdate";
	public static String SEND_CREATEPERSON = "edoc.edoctitle.createPerson.label";
	public static String SEND_SECRETLEVEL = "edoc.element.secretlevel.simple";
	public static String SEND_KEEPPERIOD = "edoc.element.keepperiod";
	public static String SEND_URGENTLEVEL = "edoc.element.urgentlevel";
	public static String SEND_COPIES = "edoc.element.copies";
	public static String SEND_SENDUNIT = "edoc.element.sendunit";
	public static String SEND_DISTRIBUTER = "true".equals(isG6) ? "edoc.element.receive.distributer" : "exchange.edoc.sendpeople";
	public static String SEND_DEPARTMENT = "edoc.element.senddepartment";
	public static String SEND_REVIEW = "edoc.element.review";
	public static String SEND_SERIALNO = "edoc.element.wordinno.label";
	
	
	public static String REC_SERIALNO = "edoc.element.receive.serial_no";
	public static String REC_SUBJECT = "edoc.element.subject";
	public static String REC_SECRETLEVEL = "edoc.element.secretlevel.simple";
	public static String REC_KEEPPERIOD = "edoc.element.keepperiod";
	public static String REC_URGENTLEVEL = "edoc.element.urgentlevel";
	public static String REC_FROMUNIT = "edoc.edoctitle.fromUnit.label";
	public static String REC_DOCMARK = "edoc.element.docmark";
	public static String REC_COMMUNICATION_DATE = "edoc.element.communication.date";
	public static String REC_COPIES = "edoc.element.copies";
	public static String REC_UNDERTAKER = "edoc.element.undertaker";
	public static String REC_UNDERTAKER_DEP = "edoc.rec.undertaker.dep";
	public static String REC_UNDERTAKER_ACCOUNT = "edoc.rec.undertaker.acccount";
	
	public static String SEND_SERIAL_NO="edoc.element.wordinno.label";
	public static String SEND_DOC_TYPE="edoc.element.doctype";
	public static String SEND_SEND_TYPE="edoc.element.sendtype";
	public static String SEND_PARTY="edoc.element.party";
	public static String SEND_ADMINISTRATIVE="edoc.element.administrative";
	
	
	public static String SEND_TO_UNIT = "edoc.element.sendtounit";
	public static String ISSUER = "edoc.element.issuer";
	
	public static String REG_DATE = "edoc.edoctitle.regDate.label";
	public static String CREATE_DATE = "edoc.edoctitle.createDate.label";
	
	public static String REG_PERSON = "edoc.edoctitle.regPerson.label";
	public static String ISPIG = "edoc.edoctitle.ispig.label";
	public static String PIGE_PATH = "edoc.edoctitle.pigeonholePath.label";
	
	
	public static String COME_EDOC_TYPE = "edoc.element.receive.send_unit_type";
	public static String COPY_TO_UNIT = "edoc.element.copytounit";
	public static String C_PERSON = "edoc.element.cperson";
	public static String KEYWORD = "edoc.element.keyword";
	
	public static String EDOC_AUTHOR = "edoc.element.author";
    //public static String EDOC_SIGN_PERSON = "edoc.stat.signperson";
    public static String EDOC_KEYWORD= "menu.edoc.keyword.label";
    public static String EDOC_INNER_TITLE= "edoc.docmark.inner.title";
    public static String EDOC_START_DATE= "edoc.supervise.serach.startdate";
	
	static{
		int idx = 1;
		
		//--------------------------发文登记簿------------------------------
		//公文标题
		sendEdocColMap.put(idx++, SEND_SUBJECT);
		//发文部门
		sendEdocColMap.put(idx++, SEND_DEPARTMENT);
		//建文日期
		sendEdocColMap.put(idx++, CREATE_DATE);
		//发文字号
		sendEdocColMap.put(idx++, SEND_WORD);
		//内部文号
		sendEdocColMap.put(idx++, SEND_SERIALNO);
		//签发人
		sendEdocColMap.put(idx++, ISSUER);
		//发文日期(签发日期)
		sendEdocColMap.put(idx++, SEND_SENDINGDATE); 
		//拟稿人
		sendEdocColMap.put(idx++, SEND_CREATEPERSON);
		//发文单位
		sendEdocColMap.put(idx++, SEND_SENDUNIT);
	    //复核人
		sendEdocColMap.put(idx++, SEND_REVIEW);
		//秘密等级
		sendEdocColMap.put(idx++, SEND_SECRETLEVEL);
		//保密期限
		sendEdocColMap.put(idx++, SEND_KEEPPERIOD);
		//紧急程度
		sendEdocColMap.put(idx++, SEND_URGENTLEVEL);
		//分发人
		sendEdocColMap.put(idx++, SEND_DISTRIBUTER);
		//主送单位
		sendEdocColMap.put(idx++, SEND_TO_UNIT);
		//抄送单位
		sendEdocColMap.put(idx++, COPY_TO_UNIT);
		//份数
		sendEdocColMap.put(idx++, SEND_COPIES);


		
		//--------------------------收文登记簿------------------------------
		//公文标题
		recEdocColMap.put(idx++, REC_SUBJECT);
		//来文日期 (签收时间)
		recEdocColMap.put(idx++, REC_COMMUNICATION_DATE);
		//来文单位
		recEdocColMap.put(idx++, REC_FROMUNIT);
		//来文字号
		recEdocColMap.put(idx++, REC_DOCMARK);
		//收文编号
		recEdocColMap.put(idx++, REC_SERIALNO);
        //承办单位
		//recEdocColMap.put(idx++, REC_UNDERTAKER_ACCOUNT);
		//承办部门
		//recEdocColMap.put(idx++, REC_UNDERTAKER_DEP);
		//承办人
		recEdocColMap.put(idx++, REC_UNDERTAKER);
		//来文类别
		recEdocColMap.put(idx++, COME_EDOC_TYPE);
		//紧急程度
		recEdocColMap.put(idx++, REC_URGENTLEVEL);
		//主送单位
		recEdocColMap.put(idx++, SEND_SENDUNIT);
		//抄送单位
		recEdocColMap.put(idx++, COPY_TO_UNIT);
		//签发人
		recEdocColMap.put(idx++, ISSUER);
		//会签人
		//recEdocColMap.put(idx++, C_PERSON);
		
		//秘密等级
		recEdocColMap.put(idx++, REC_SECRETLEVEL);
		//保密期限
		recEdocColMap.put(idx++, REC_KEEPPERIOD);
		//主题词 根据国家行政公文规范,去掉主题词
		//recEdocColMap.put(idx++, KEYWORD);
		//登记人
		recEdocColMap.put(idx++, REG_PERSON);
		//登记日期
		recEdocColMap.put(idx++, REG_DATE);
		
		//分发人
		if("true".equals(isG6)){
		    recEdocColMap.put(idx++, SEND_DISTRIBUTER);
		}
		
		//份数
//		recEdocColMap.put(idx++, REC_COPIES);
		
		//--------------------------自定义查询--发文------------------------------
		//sendSearchEdocColMap.putAll(sendEdocColMap);
		sendSearchEdocColMap.put(idx++, SEND_SECRETLEVEL);
		sendSearchEdocColMap.put(idx++, SEND_SUBJECT);
		sendSearchEdocColMap.put(idx++, SEND_WORD);
//		sendSearchEdocColMap.put(idx++, SEND_TO_UNIT);
//		sendSearchEdocColMap.put(idx++, ISSUER);
//		sendSearchEdocColMap.put(idx++, SEND_SENDINGDATE);
		sendSearchEdocColMap.put(idx++, ISPIG);
		sendSearchEdocColMap.put(idx++, PIGE_PATH);
		sendSearchEdocColMap.put(idx++, SEND_COPIES);
		
		//收文
		recSearchEdocColMap.put(idx++, SEND_SECRETLEVEL);
		recSearchEdocColMap.put(idx++, SEND_SUBJECT);
		recSearchEdocColMap.put(idx++, SEND_WORD);
		recSearchEdocColMap.put(idx++, REC_FROMUNIT);
		recSearchEdocColMap.put(idx++, REG_PERSON);
		recSearchEdocColMap.put(idx++, REG_DATE);
		recSearchEdocColMap.put(idx++, ISPIG);
		recSearchEdocColMap.put(idx++, PIGE_PATH);
		recSearchEdocColMap.put(idx++, SEND_COPIES);
		
		//签报
		signSearchEdocColMap.put(idx++, SEND_SECRETLEVEL);
		signSearchEdocColMap.put(idx++, SEND_SUBJECT);
		signSearchEdocColMap.put(idx++, SEND_WORD);
//		signSearchEdocColMap.put(idx++, SEND_TO_UNIT);
		signSearchEdocColMap.put(idx++, SEND_CREATEPERSON);
		signSearchEdocColMap.put(idx++, CREATE_DATE);
		signSearchEdocColMap.put(idx++, ISPIG);
		signSearchEdocColMap.put(idx++, PIGE_PATH);
		signSearchEdocColMap.put(idx++, SEND_COPIES);
		
		/** SP1额外增加公文查询列表头显示所有的查询条件 */
        //公文种类
        addedSearchLabel.put(idx++, SEND_DOC_TYPE);
        //行文类型
        addedSearchLabel.put(idx++, SEND_SEND_TYPE);
        //主送单位
        addedSearchLabel.put(idx++, SEND_TO_UNIT);
        //拟稿人
        addedSearchLabel.put(idx++, EDOC_AUTHOR);
        //签发人
        //addedSearchLabel.put(idx++, EDOC_SIGN_PERSON);
        addedSearchLabel.put(idx++, ISSUER);
        
        //主题词 - 根据国家行政公文规范,去掉主题词
        //addedSearchLabel.put(idx++, EDOC_KEYWORD);
        
        //内部文号
        addedSearchLabel.put(idx++, EDOC_INNER_TITLE);
        //发起日期
        addedSearchLabel.put(idx++, EDOC_START_DATE);
        //发文单位
        addedSearchLabel.put(idx++, SEND_SENDUNIT);
        //签发日期
        addedSearchLabel.put(idx++, SEND_SENDINGDATE);
		
		
		/*
		//内部文号
		sendSearchEdocColMap.put(idx++, SEND_SERIAL_NO);
		//公文种类
		sendSearchEdocColMap.put(idx++, SEND_DOC_TYPE);
		//行文类型
		sendSearchEdocColMap.put(idx++, SEND_SEND_TYPE);
		//党政机关
		//sendSearchEdocColMap.put(idx++, SEND_PARTY);
		//行政机关
		//sendSearchEdocColMap.put(idx++, SEND_ADMINISTRATIVE);
		*/
		
		//加入queryColMap
		queryColMap.putAll(sendEdocColMap);
		queryColMap.putAll(recEdocColMap);
		queryColMap.putAll(sendSearchEdocColMap);
		queryColMap.putAll(recSearchEdocColMap);
		queryColMap.putAll(signSearchEdocColMap);
		queryColMap.putAll(addedSearchLabel);
	}
}


