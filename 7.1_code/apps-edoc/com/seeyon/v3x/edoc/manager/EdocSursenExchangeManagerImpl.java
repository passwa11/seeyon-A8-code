package com.seeyon.v3x.edoc.manager;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import com.seeyon.apps.edoc.manager.EdocSursenExchangeManager;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.oainterface.common.OAInterfaceException;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionDisplaySetEnum;
import com.seeyon.v3x.edoc.domain.EdocElement;
import com.seeyon.v3x.edoc.domain.EdocFormElement;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.util.EdocOpinionDisplayUtil;
import com.seeyon.v3x.edoc.webmodel.EdocFormModel;
import com.seeyon.v3x.edoc.webmodel.EdocOpinionModel;
import com.seeyon.v3x.edoc.webmodel.FormOpinionConfig;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;

public class EdocSursenExchangeManagerImpl implements EdocSursenExchangeManager {
	private static Log LOGGER = CtpLogFactory.getLog(EdocSursenExchangeManagerImpl.class);
	private EdocManager edocManager;
	private EdocFormManager edocFormManager;
	private EdocElementManager edocElementManager;
	private TemplateManager templeteManager;
	private AffairManager affairManager;
	@Override
	public String[] exportOfflineEdocModel(long id) throws BusinessException {

        String allXml = "";
        EdocSummary summary;
        String[] result = new String [2];
        try {
            summary = edocManager.getColAllById(id);
            if(summary==null) throw new OAInterfaceException(32013,"公文不存在");    

            V3xOrgMember sender;
            try {
                sender =summary.getStartMember();

                User user = new User();
                user.setId(sender.getId());
                user.setLoginName(sender.getLoginName());
                user.setName(sender.getName());
                user.setAccountId(sender.getOrgAccountId());
                CurrentUser.set(user);
            } catch (Exception e) {
            	LOGGER.error(e);
            }

            EdocFormModel fm = edocFormManager.getEdocFormModel(summary
                    .getFormId(), summary, -1);
            allXml = fm.getXml();

            int nDataStart = allXml.indexOf("&&&&&&&&  data_start  &&&&&&&&");
            int nInputStart = allXml.indexOf("&&&&&&&&  input_start  &&&&&&&&");
            String data = allXml.substring(nDataStart + 30, nInputStart);
            String inputData = allXml.substring(nInputStart + 31);
            String xslt = fm.getXslt();

            V3xOrgMember startMember = summary.getStartMember();
            // 取得所有的文单元素名称
            Set<String> fields = new HashSet<String>();
            List<EdocFormElement> elementList2 = edocFormManager.getEdocFormElementByFormId(summary.getFormId());
            for(EdocFormElement ele : elementList2){
                EdocElement element = edocElementManager.getEdocElementsById(ele.getElementId());
                fields.add(element.getFieldName());
            }
            /**
             * 查找公文单意见元素显示，由于公文查询意见元素代码调整，改用新方法处理
             * @Date 2011-12-25
             * @author lilong
             */
            //公文处理意见回显到公文单,排序    EdocOpinionDisplayConfig
            long flowPermAccout = EdocHelper.getFlowPermAccountId(summary.getOrgAccountId(), summary, templeteManager);
            FormOpinionConfig displayConfig = edocFormManager.getEdocOpinionDisplayConfig(summary.getFormId(),flowPermAccout); 
            Map<String,EdocOpinionModel> map = edocManager.getEdocOpinion(summary,OpinionDisplaySetEnum.DISPLAY_LAST.getValue().equals(displayConfig.getOpinionType()));
            CtpAffair ctpAffair = affairManager.getSenderAffair(summary.getId());
            List<V3xHtmDocumentSignature> signatuers=new ArrayList<V3xHtmDocumentSignature>(); 
            Map<String,Object> strMap = EdocOpinionDisplayUtil.convertOpinionToString(map, displayConfig, ctpAffair, false, signatuers);
            fields.addAll(map.keySet());
            xslt = xslt.replaceAll("\\t", "");
            /**
             * fix bug AEIGHT-8169
             * 20120904 lilong导出离线公文单样式乱问题，加入默认的css样式，
             * 同时replace掉<pre>标签防止显示文字串行
             */
            xslt = xslt.replaceAll("<xsl:template match=\"my:myFields\">",
                    "<xsl:template match=\"my:myFields\"><style type=\"text/css\">" +
                    "body, td, th, input, textarea, div, select, p {font-family: Arial, Helvetica, sans-serif;font-size: 12px;vertical-align: middle;}</style>");
            xslt = xslt.replaceAll("<pre>", "");
            xslt = xslt.replaceAll("</pre>", "");
            for (String key : fields) {
/*              if ((opinions.get(key) != null)
                        && opinions.get(key) instanceof String) {*/
                if(key==null) continue;
                    xslt = xslt.replace("<xsl:value-of select=\"my:" + key
                            + "\"/>", "<xsl:value-of select=\"my:" + key + "\""
                            + " disable-output-escaping=\"yes\"" + "/>");
//              }
            }
            
            /**
             * 国新客户BUG，导出离线公文单样式问题 lilong 20130603
             * Fix AEIGHT-9555
             */
            xslt = xslt.replaceAll("border=\"1\"", "");
            xslt = xslt.replaceAll("FONT-SIZE: x-small", "");
            result = new String[]{ translateEdocXml(data, inputData, strMap),xslt };
            
        } catch (Exception e) {
        	LOGGER.error(e);
        }
        return result;
    
	}
    
    private String translateEdocXml(String dataXml, String enumXml,
            Map<String,Object> opinions) throws BusinessException {
        SAXBuilder builder = new SAXBuilder();
        String xmlString = "";
        try {
            Map<String, Map<String, String>> map = parseEnumDict(enumXml, builder);
            // 补充绑定文单意见数据
            StringWriter writer = new StringWriter();
            Document dataDoc = builder.build(new StringReader(dataXml));
            Element root = dataDoc.getRootElement();
            Namespace ns = Namespace.getNamespace("my", "www.seeyon.com/form/2007");
            for (Object key : opinions.keySet()) {
                if ((opinions.get(key) != null) && opinions.get(key) instanceof String) {
                    Element e = new Element(key.toString(), ns);
                    CDATA cdata = new CDATA(opinions.get(key).toString());
                    e.setContent(cdata);
                    root.addContent(e);
                }
            }
            translateEnum(root, map, ns);
            List<org.jdom.Element> children = root.getChildren();
            for (Element element : children) {
                List<Element> contents = element.getContent();
                // 必须只有一个元素
                if(contents.size()==1){
                    org.jdom.Content e = contents.get(0);
                    if(!(e instanceof org.jdom.CDATA)){
                        CDATA cdata = new CDATA(org.apache.commons.lang.StringEscapeUtils.unescapeHtml(e.getValue()));
                        element.setContent(cdata);
                    }
                }
            }
            XMLOutputter outputter = new XMLOutputter();
            outputter.output(dataDoc, writer);
            xmlString =  writer.toString();
        } catch (Exception e) {
            LOGGER.error(e);
        }
		return xmlString;
    }
    
    private void translateEnum(Element root, Map<String, Map<String, String>> map, Namespace ns) {
        // 根据字典翻译XML枚举值
        for (String name : map.keySet()) {
            if (name.length() < 3)
                continue;
            Element child = root.getChild(name.substring(3), ns);
            if (child == null)
                continue;               
            Object value = child.getText();
            if (value == null)
                continue;
            Map<String, String> dict = map.get(name);
            if (!value.equals("")&&dict!=null) {
                String text = dict.get(value);
                if(text!=null)
                child.setText(text);
            }
        }
    }
    
    private Map<String, Map<String, String>> parseEnumDict(String enumXml,
            SAXBuilder builder) throws JDOMException, IOException {
        Reader reader = new StringReader(enumXml);
        Document doc = builder.build(reader);
        // 筛选出枚举项
        XPath xpath = XPath.newInstance("/FieldInputList/FieldInput[@type='select']");
        List list = xpath.selectNodes(doc);
        // 生成枚举字典备用
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        for (Object o : list) {
            Element eFieldInput = (Element) o;
            String name = eFieldInput.getAttributeValue("name");
            Map<String, String> m = new HashMap<String, String>();
            for (Object oi : eFieldInput.getChildren("Input")) {
                Element eInput = (Element) oi;
                m.put(eInput.getAttributeValue("value"), eInput
                        .getAttributeValue("display"));
            }
            map.put(name, m);
        }
        return map;
    }


	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	public void setEdocFormManager(EdocFormManager edocFormManager) {
		this.edocFormManager = edocFormManager;
	}

	public void setEdocElementManager(EdocElementManager edocElementManager) {
		this.edocElementManager = edocElementManager;
	}

	public void setTempleteManager(TemplateManager templeteManager) {
		this.templeteManager = templeteManager;
	}

	public void setAffairManager(AffairManager affairManager) {
		this.affairManager = affairManager;
	}



}
