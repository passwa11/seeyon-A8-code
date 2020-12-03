package com.seeyon.cap4.form.util.parse;

/**
 * @description 流程节点
 * </br>
 * @create by fuqiang
 * @create at 2019-06-19 23:16
 * @see com.seeyon.cap4.form.util.parse
 * @since v7.1sp
 */
public class Node {

    //节点id
    private String nodeId;
    //x和y确定流程的位置
    private Double x;
    private Double y;
    //节点名称
    private String nodeName;
    /**
     * 节点类型：user-一个用户；Post-岗位；WF_SUPER_NODE-超级节点;
     * 相对角色：1.（部门主管）：nodeType : Node;nodeName 后四位为：部门主管;
     *         2.（部门分管领导）nodeType : Node;nodeName 后四位为：部门分管领导;
     *         3.（部门管理员）nodeType : Node;nodeName 后四位为：部门管理员;
     *         4.   (部门自定义角色)Department_Role
     *         5. 单位自定义角色：Account_Role
     *         6. 部门岗位：Department_Post
     *         7.
     */
    private String nodeType;
    //节点类型，所对应的id
    private String nodeTypeId;
    //节点所对应的权限，先根据_进行拆分，前面的是pc权限，后者是手机权限；然后在根据.来拆分，第二位为resource所对应的id
    private String fv;
    //信任签字/信任盖章（信任签字和信任盖章需要根据此节点来判断）
    private String isXrdNode;
    /**
     * 是否匹配，公章类型；只要外部节点，绑定盖章控件时，有效；true-匹配；false-不匹配;
     */
    private boolean isMatch = true;

    private int orderno;

    public int getOrderno() {
        return orderno;
    }

    public void setOrderno(int orderno) {
        this.orderno = orderno;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
    }

    public String getIsXrdNode() {
        return isXrdNode;
    }

    public void setIsXrdNode(String isXrdNode) {
        this.isXrdNode = isXrdNode;
    }

    public String getFv() {
        return fv;
    }

    public void setFv(String fv) {
        this.fv = fv;
    }

    public String getNodeTypeId() {
        return nodeTypeId;
    }

    public void setNodeTypeId(String nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
}
