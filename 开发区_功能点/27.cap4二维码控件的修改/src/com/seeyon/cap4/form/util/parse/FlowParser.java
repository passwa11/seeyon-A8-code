package com.seeyon.cap4.form.util.parse;

import com.seeyon.ctp.common.log.CtpLogFactory;
import org.apache.commons.logging.Log;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * 流程解析工具类
 * create at 2019-06-19 22:41
 * </br>
 * @author  fuqiang
 * @since v7.1sp
 * @see com.seeyon.cap4.form.util.parse.FlowParser
 */
public class FlowParser {

    private static Log log = CtpLogFactory.getLog(FlowParser.class);
    /**
     * 外部签署节点类型
     */
    public static final String SUPER_NODE = "外部签署";
    /**
     * 信任盖章节点类型
     */
    public static final String SEAL_NODE = "信任盖章";
    /**
     * 信任签字节点类型
     */
    public static final String SIGN_NODE = "信任签字";
    /**
     * 信任度盖章控件类型
     */
    public static final String SEAL_TYPE = "fff486d125bc48e5b01760a76913aa2a";
    /**
     * 信任度签字控件类型
     */
    public static final String SIGN_TYPE = "232b4b0cedc94522b46b69284839673b";
    /**
     * 编辑权限名称
     */
    public static final String EDIT_AUTH = "edit";
    /**
     * 内部公章类型：0-单位公章；1-合同专用章；2-财务专用章；3-签名章；4-其它印章；
     */
    public static final String[] INNER_SEAL_TYPES = {"0", "1", "2", "3", "4"};
    /**
     * 外部公章类型
     */
    public static final String OUTER_SEAL_TYPE = "5";
    /**
     * 解析流程根据流程xml
     * @param flowXml   流程xml，从template或run中获取
     * @return
     */
    public static LinkedHashMap<Double, List<Node>> parse(String flowXml) {
        List<Node> allNodes = new ArrayList<Node>();
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(flowXml);
            Element flowElement = (Element)doc.selectSingleNode("/ps/p");
            Iterator iter = flowElement.elementIterator("n");
            while (iter.hasNext()) {
                Node node = new Node();
                Element nElement = (Element) iter.next();
                Attribute nAtt = nElement.attribute("n");
                if(nAtt != null) {
                    node.setNodeName(nAtt.getValue());
                }
                Attribute xAtt = nElement.attribute("x");
                if(xAtt != null) {
                    node.setX(Double.parseDouble(xAtt.getValue()));
                }
                Attribute yAtt = nElement.attribute("y");
                if(yAtt != null) {
                    node.setY(Double.parseDouble(yAtt.getValue()));
                }
                Attribute iAtt = nElement.attribute("i");
                if(iAtt != null) {
                    node.setNodeId(iAtt.getValue());
                }
                Element aElement = nElement.element("a");
                if(aElement != null) {
                    Attribute aegAtt = aElement.attribute("g");
                    if(aegAtt != null) {
                        node.setNodeType(aegAtt.getValue());
                    }
                    Attribute aefAtt = aElement.attribute("f");
                    if(aefAtt != null) {
                        node.setNodeTypeId(aefAtt.getValue());
                    }
                }
                Element sElement = nElement.element("s");
                if(sElement != null) {
                    Attribute segAtt = sElement.attribute("fv");
                    if(segAtt != null) {
                        node.setFv(segAtt.getValue());
                    }
                    Attribute seiAtt = sElement.attribute("i");
                    if(seiAtt != null) {
                        Element aEl = nElement.element("a");
                        if(aEl != null) {
                            Attribute adAtt = aEl.attribute("d");
                            if(adAtt != null) {
                                if(SUPER_NODE.equals(adAtt.getValue())) {
                                    node.setIsXrdNode(SUPER_NODE);

                                }else{
                                    node.setIsXrdNode(seiAtt.getValue());
                                }
                            }else{
                                node.setIsXrdNode(seiAtt.getValue());
                            }
                        }
                    }
                }
                allNodes.add(node);
            }
            //排序流程节点
            sort(allNodes);
            return splitNodes(allNodes);
        } catch (Exception e) {
            log.error("解析流程异常：", e);
        }
        return null;
    }


    /**
     * 根据节点id，获取当前节点
     * @param nodeMap       全节点map
     * @param activityId    流程id
     * @return
     */
    public static Node getNodeByActivityId(LinkedHashMap<Double, List<Node>> nodeMap, String activityId) {
        Iterator<Map.Entry<Double, List<Node>>> iterator = nodeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Double, List<Node>> item = iterator.next();
            List<Node> values = item.getValue();
            for(Node node : values) {
                if(activityId.equalsIgnoreCase(node.getNodeId())) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * 对流程进行排序
     * @param nodes
     */
    private static void sort(List<Node> nodes) {
        for(int i=0;i<nodes.size();i++) {
            for(int j=0;j<nodes.size()-i-1;j++) {
                if(nodes.get(j).getX() > nodes.get(j+1).getX()) {
                    Node temp = nodes.get(j);
                    nodes.set(j, nodes.get(j+1));
                    nodes.set(j+1, temp);
                }
            }
        }
    }

    /**
     * 拆分节点，如果有分支，则存储到一个list中
     * @param allNodes
     * @return
     */
    private static LinkedHashMap<Double, List<Node>> splitNodes(List<Node> allNodes) {
        LinkedHashMap<Double, List<Node>> nodeMap = new LinkedHashMap<Double, List<Node>>();
        for(int i=0;i<allNodes.size();i++) {
            List<Node> nodes = nodeMap.get(allNodes.get(i).getX());
            if(nodes == null) {
                nodes = new ArrayList<Node>();
            }
            nodes.add(allNodes.get(i));
            nodeMap.put(allNodes.get(i).getX(), nodes);
        }
        return nodeMap;
    }
}
