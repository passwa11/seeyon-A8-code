package com.seeyon.v3x.edoc.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.form.util.StringUtils;
import com.seeyon.ctp.form.util.infopath.CabFileResourceProvider;
import com.seeyon.ctp.form.util.infopath.IFormResoureProvider;
import com.seeyon.ctp.form.util.infopath.InfoPathObject;
import com.seeyon.ctp.form.util.infopath.InfoPath_xsl;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.form.InputObject;
import com.seeyon.v3x.edoc.form.SeeyonformSelect;
import com.seeyon.v3x.edoc.form.SeeyonformText;
import com.seeyon.v3x.edoc.form.SeeyonformTextArea;

public class EdocFormHelper {
    private static final Log LOGGER = LogFactory.getLog(EdocFormHelper.class);
    
    /** XSN内部文件 sampledata.xml **/
    public static final String XSN_INNERFILE_SAMPLEDATA = "sampledata.xml";
    
    /** XSN内部文件 view1.xsl **/
    public static final String XSN_INNERFILE_VIEW1 = "view1.xsl";
    
    /** XSN定义是有些特殊字体在IE9下存在兼容性问题，进行替换操作   arr[0] 有问题的字体， arr[1]需要替换成的字体**/
    public static final String[][] SPECIAL_FORM_FONT_SIZE = {{"FONT-SIZE: x-small", "FONT-SIZE: 10pt"},
                                                              {"FONT-SIZE: xx-small", "FONT-SIZE: 8pt"},
                                                              {"FONT-SIZE: small", "FONT-SIZE: 12pt"},
                                                              {"FONT-SIZE: medium", "FONT-SIZE: 14pt"},
                                                              {"FONT-SIZE: large", "FONT-SIZE: 18pt"},
                                                              {"FONT-SIZE: x-large", "FONT-SIZE: 24pt"},
                                                              {"FONT-SIZE: xx-large", "FONT-SIZE: 36pt"}};
    
    public static String getCreateEdocFormContent(String xml, String xslt) throws BusinessException {
        //根据XML和 XSLT生成基本的文单结构(公文元素中 还没数据)
        String edocFormContent = initSeeyonForm(xml, xslt);

        //将公文元素为select类型的，设置到公文单中 (例如：公文种类，文件密集等)    

        return edocFormContent;
    }

    private static String initSeeyonForm(String xml, String xslt) throws BusinessException {
        String str = xml;
        int xslStart = str.indexOf("&&&&&&&  xsl_start  &&&&&&&&");
        int dataStart = str.indexOf("&&&&&&&&  data_start  &&&&&&&&");
        int inputStart = str.indexOf("&&&&&&&&  input_start  &&&&&&&&");
        if (xslStart == -1)
            throw new BusinessException(ResourceUtil.getString("edoc.error.no.find")+"xsl");//没有找到:
        if (dataStart == -1)
            throw new BusinessException(ResourceUtil.getString("edoc.error.no.find")+"data");
        if (inputStart == -1)
            throw new BusinessException(ResourceUtil.getString("edoc.error.no.find")+"input");
        //String xsl = str.substring(xslStart + 28, dataStart);
        String data = str.substring(dataStart + 30, inputStart);
        String finput = str.substring(inputStart + 31);

        String content = getViewFormContent(data, xslt);

        content = initHtml(finput, content);

        return content;

    }

    private static String initHtml(String finput, String content) throws BusinessException{
        finput = getXmlContent(finput);
        List<InputObject> fieldInputList = paseFormatXML(finput);
        String afterConvertContent = convertHtml(fieldInputList,content);
        
        return afterConvertContent;
    }
    
    public static String toJsStr(String str)
    {
        String strTemp=str.replace("\"","&quot;"); 
        strTemp=strTemp.replace("<","&lt;");
        strTemp=strTemp.replace(">","&gt;");   
        return strTemp;
    }
    
    
    private static String convertHtml(List<InputObject> fieldInputList,String content) throws BusinessException{
        int colgroup_start = content.indexOf("<colgroup>");
        int colgroup_end = content.indexOf("</colgroup>")+"</colgroup>".length();
        //暂时先将<colgroup> 中的去掉，因为里面的col标签不符合xml规范
        content = content.substring(0,colgroup_start) + content.substring(colgroup_end);
        //将&nbsp也进行替换，不符合xml规范
        content = content.replaceAll("&nbsp", "&#160");
        
        
        Document htmlDoc = strToXMLDocment(content);
        List<Element> spanList=htmlDoc.selectNodes("//div/table/tbody/tr/td/div/span");
        for(Element span : spanList){
            String name = span.attributeValue("binding");
            for(InputObject field : fieldInputList){
                if(field.getFieldName().equals(name)){
                    field.change(span);
                    
                    break;
                }
            }
        }
        return htmlDoc.asXML();
    }

    private static List<InputObject> paseFormatXML(String finput) throws BusinessException{
        List<InputObject> inputList = new ArrayList<InputObject>();
        
        Document inputDoc = strToXMLDocment(finput);
        Element root = inputDoc.getRootElement();
        if(!"FieldInputList".equals(root.getName())){
            throw new BusinessException(ResourceUtil.getString("edoc.error.xml.seeyonformat")+ResourceUtil.getString("edoc.error.xml.seeyonformat.no.node","FieldInputList"));//XML信息不是SeeyonFormat的格式!找不到 FieldInputList 节点
        }
        List<Element> fieldInputList=inputDoc.selectNodes("//FieldInput");
        if(fieldInputList == null){
            throw new BusinessException(ResourceUtil.getString("edoc.error.xml.seeyonformat")+"<br>"+ResourceUtil.getString("edoc.error.xml.seeyonformat.no.node","FieldInput"));  //XML信息不是SeeyonFormat的格式!<br>找不到 FieldInput 节点
        }
        for(int i = 0 ; i < fieldInputList.size(); i++){
            Element fieldInput = fieldInputList.get(i);
            String name = fieldInput.attributeValue("name");
            if (Strings.isBlank(name)){
                throw new BusinessException(ResourceUtil.getString("edoc.error.xml.seeyonformat")+"<br>"+ResourceUtil.getString("edoc.error.xml.seeyonformat.input.no.property","FieldInput","name"));//XML信息不是SeeyonFormat的格式!<br> FieldInput节点没有name属性
            }
            
            //获得FieldInput节点的 type属性
            String type = fieldInput.attributeValue("type");
            if (Strings.isBlank(type)){
                throw new BusinessException(ResourceUtil.getString("edoc.error.xml.seeyonformat")+"<br>"+ResourceUtil.getString("edoc.error.xml.seeyonformat.input.no.property","FieldInput","type"));//XML信息不是SeeyonFormat的格式!<br> FieldInput节点没有type属性
            }
            if("label".equals(type)){
                
            }else if("text".equals(type)){
                SeeyonformText text = new SeeyonformText(fieldInput);
                inputList.add(text);
            }else if("textarea".equals(type)){
                SeeyonformTextArea textarea = new SeeyonformTextArea(fieldInput);
                inputList.add(textarea);
            }else if("select".equals(type)){
                SeeyonformSelect select = new SeeyonformSelect(fieldInput);
                inputList.add(select);
            }
        }
        return inputList;
    }
    
    
    public static Document strToXMLDocment(String xmlStr) throws BusinessException{// Str是传入的一段XML内容的字符串
        Document document = null;
        try {
            // DocumentHelper.parseText(str)这个方法将传入的XML字符串转换处理后返回一个Document对象
            document = DocumentHelper.parseText(xmlStr);
        } catch (DocumentException e) {
            LOGGER.error("", e);
            throw new BusinessException(ResourceUtil.getString("edoc.error.xml.parse.failed"));//解析XML信息失败!
        }
        return document;
    }
    
    
    public static void main(String[] args) {
        String  str = "<div xmlns:xdImage=\"www.seeyon.com/form/2007\" xmlns:xdFormatting=\"www.seeyon.com/form/2007\""+
                "                xmlns:xdSolution=\"www.seeyon.com/form/2007\" xmlns:xdXDocument=\"www.seeyon.com/form/2007\""+
                "                xmlns:xdExtension=\"www.seeyon.com/form/2007\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\""+
                "                xmlns:xd=\"www.seeyon.com/form/2007\" xmlns:msxsl=\"www.seeyon.com/form/2007\""+
                "                xmlns:my=\"www.seeyon.com/form/2007\" align=\"center\">"+
                "                <table border=\"1\" borderColor=\"buttontext\""+
                "                    style=\"BORDER-RIGHT: medium none; TABLE-LAYOUT: fixed; BORDER-TOP: medium none; BORDER-LEFT: medium none; WIDTH: 624px; BORDER-BOTTOM: medium none; BORDER-COLLAPSE: collapse; WORD-WRAP: break-word\""+
                "                    class=\"xdLayout\">"+
                "                    <colgroup>"+
                "                        <col style=\"WIDTH: 106px\">"+
                "                            <col style=\"WIDTH: 179px\">"+
                "                                <col style=\"WIDTH: 101px\">"+
                "                                    <col style=\"WIDTH: 238px\">"+
                "                    </colgroup>"+
                "                    <tbody vAlign=\"top\">"+
                "                        <tr style=\"MIN-HEIGHT: 30px\">"+
                "                            <td"+
                "                                style=\"BORDER-RIGHT: #000000 1pt; PADDING-RIGHT: 10px; BORDER-TOP: #000000 1pt; PADDING-LEFT: 10px; PADDING-BOTTOM: 1px; VERTICAL-ALIGN: middle; BORDER-LEFT: #000000 1pt; PADDING-TOP: 1px; BORDER-BOTTOM: #ff0000 1.5pt solid; BACKGROUND-COLOR: transparent\""+
                "                                colSpan=\"4\">"+
                "                                <div>"+
                "                                    <font size=\"5\">&nbsp;"+
                "                                    </font>"+
                "                                    <font size=\"6\" color=\"#ff0000\">"+ResourceUtil.getString("edoc.formstyle.dispatch")+"</font>"+
                "                                </div>"+
                "                            </td>"+
                "                        </tr>"+
                "                    </tbody>"+
                "                </table>"+
                "            </div>";
        try {
            strToXMLDocment(str);
        } catch (BusinessException e) {
            // TODO Auto-generated catch block
//            log.error("", e);
        }
    }
    
    
    //取得xml文件的内容
    private static String getXmlContent(String xml) {
        int i = 0;
        for (i = 0; i < xml.length(); i++) {
            if (xml.charAt(i) == '<') {
                break;
            }
        }
        return xml.substring(i, xml.length());
    }
    
    /**
     * 通过xsn文件的物理路径解析内部的xml(包含的field列表)
     * @Author      : xuqiangwei
     * @Date        : 2014年11月3日下午5:00:42
     * @param path : xsn的物理绝对路径
     * @return
     * @throws BusinessException 
     */
    public static String parseInfoPathXMl(String path) throws BusinessException {

        String ret = null;
        
        if(Strings.isNotBlank(path)){
            ByteArrayInputStream fInfopathxsn = new ByteArrayInputStream(StringUtils.readFileData(path));
            IFormResoureProvider fResourceProvider = new CabFileResourceProvider(fInfopathxsn);
    
            // 调用表单模块的接口进行解析
            ret = fResourceProvider.loadResource(XSN_INNERFILE_SAMPLEDATA);
        }
        
        return ret;
    }
    
    /**
     * 通过xsn文件的物理路径解析xsl(页面展示的xsl)
     * @Author      : xuqiangwei
     * @Date        : 2014年11月3日下午5:03:17
     * @param path : xsn的物理绝对路径
     * @return
     * @throws BusinessException 
     */
    public static String parseInfoPathXSL(String path) throws BusinessException{
        
        String ret = null;
        
        if(Strings.isNotBlank(path)){
            ByteArrayInputStream fInfopathxsn = new ByteArrayInputStream(StringUtils.readFileData(path));
            IFormResoureProvider fResourceProvider = new CabFileResourceProvider(fInfopathxsn);
            
            //调用表单模块的接口进行解析
            String xsl = fResourceProvider.loadResource(XSN_INNERFILE_VIEW1);
            InfoPath_xsl info = new InfoPath_xsl();
            info.setFileInfo(xsl);
            
            HashMap<String, Long> imgs = parseFormImgs(fResourceProvider);
            
            info.covertContent(imgs);
            
            ret = EdocFormHelper.replaceFontSize(info.getFileInfo());
            
            ret = ret.replaceAll("/FormImgView\\?imgId=", "/fileUpload.do?method=showRTE&type=image&fileId=");//替换3.5图片地址
        }
        
        return ret;
    }
    
    /**
     * 通过xsn文件的物理路径计息xml和xsl
     * @Author      : xuqiangwei
     * @Date        : 2014年11月3日下午5:05:47
     * @param path : xsn的物理绝对路径
     * @return ret[0] : xsn的xml内容，ret[1] : xsn内容
     * @throws BusinessException 
     * @example:
     *      <p>
     *      String[] ret = EdocFormHelper.parseInfoPathXMLAndXSL("D:/text.xsn");<br/>
     *      String xsnXML = ret[0];<br/>
     *      String xsnXSL = ret[1];
     *      </p>
     *      
     */
    public static String[] parseInfoPathXMLAndXSL(String path) throws BusinessException{
        
        String[] ret = new String[]{"", ""};
        
        if(Strings.isNotBlank(path)){
            ByteArrayInputStream fInfopathxsn = new ByteArrayInputStream(StringUtils.readFileData(path));
            IFormResoureProvider fResourceProvider = new CabFileResourceProvider(fInfopathxsn);
            
            //调用表单模块的接口进行解析
            String xml = fResourceProvider.loadResource(XSN_INNERFILE_SAMPLEDATA);
            String xsl = fResourceProvider.loadResource(XSN_INNERFILE_VIEW1);
            InfoPath_xsl info = new InfoPath_xsl();
            info.setFileInfo(xsl);
            
            HashMap<String, Long> imgs = parseFormImgs(fResourceProvider);
            
            info.covertContent(imgs);
            
            ret[0] = xml;
            String xslRet = EdocFormHelper.replaceFontSize(info.getFileInfo());
            
            ret[1] = xslRet.replaceAll("/FormImgView\\?imgId=", "/fileUpload.do?method=showRTE&type=image&fileId=");//替换3.5图片地址
        }
        
        return ret;
    }
    
    /**
     * 调用表单接口解析XSN里面的图片
     * 
     * @Author : xuqw
     * @Date : 2015年3月19日下午3:58:00
     * @param fResourceProvider
     * @return
     * @throws BusinessException
     */
    private static HashMap<String, Long> parseFormImgs(IFormResoureProvider fResourceProvider) throws BusinessException {

        // 图片处理
        InfoPathObject xsf = new InfoPathObject();
        FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
        xsf.setFileManager(fileManager);
        xsf.setResourceProvider(fResourceProvider);
        xsf.loadFormXSNFile();
        HashMap<String, Long> imgs = (HashMap<String, Long>) xsf.getLogo();

        return imgs;
    }
    
    /**
     * 替换infoPath的xsl里面的特殊字体
     * @Author      : xuqiangwei
     * @Date        : 2014年12月3日下午5:32:22
     * @param src
     * @return
     */
    private static String replaceFontSize(String src){
        
        if(Strings.isBlank(src)){
            return src;
        }
        
        String ret = src;
        
        for(String[] fontInfo : SPECIAL_FORM_FONT_SIZE){
            
            String xmlFontSize = fontInfo[0];
            String commonFontSize = fontInfo[1];
            ret = ret.replaceAll(xmlFontSize, commonFontSize);
        }
        
        return ret;
    }
    

    /**
     * 通过xslt解释xml 形成html文单结构
     * @param data
     * @param xslt
     * @return
     * @throws BusinessException
     */
    private static String getViewFormContent(String data, String xslt) throws BusinessException {
        String content = "";
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new ByteArrayInputStream(data.getBytes("utf-8")));

            TransformerFactory factory = TransformerFactory.newInstance();

            StreamSource xl = new StreamSource(new ByteArrayInputStream(xslt.getBytes("utf-8")));
            Transformer transformer = factory.newTransformer(xl);

            Properties props = transformer.getOutputProperties();
            props.setProperty(OutputKeys.ENCODING, "utf-8");
            //            props.setProperty(OutputKeys.METHOD,"html");
            //            props.setProperty(OutputKeys.VERSION,"4.0");
            transformer.setOutputProperties(props);
            DocumentSource docSource = new DocumentSource(document);
            StringWriter strWriter = new StringWriter();
            StreamResult docResult = new StreamResult(strWriter);

            transformer.transform(docSource, docResult);
            content = strWriter.toString();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return content;
    }

}
