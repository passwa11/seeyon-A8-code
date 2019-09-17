package com.seeyon.apps.govdoc.constant;

/**
 * Created by xuker on 2016-3-15.
 * 公文快速发文的空节点流程 常量
 */
public class GovdocQuickSendWorkFlow {
	/**
     * 数据库中预置的一个空节点流程的ID
     */
    public static final Long processId = -1L;
    /**
     * 在流程输入框中显示的文字
     */
    public static final String workflowNodesInfo = "快速发文流程";
    /**
     * 数据库中预置的空节点流程的XML
     */
    public static final String processXml = "<ps><p t=\"p\" s=\"false\" i=\"\" n=\"\" d=\"\" u=\"\"><n i=\"start\" n=\"发起者\" t=\"8\" d=\"\" x=\"50\" y=\"70\" q=\"\" h=\"\" g=\"\" f=\"\"><a k=\"\" c=\"1\" j=\"false\" i=\"false\" f=\"\" g=\"\" h=\"false\" d=\"\" l=\"\" m=\"\" b=\"\" vj=\"0\"/><s i=\"collaboration\" n=\"协同\" d=\"\" t=\"17\" l=\"\" q=\"\" p=\"\" o=\"\" u=\"-1\" h=\"-1\" v=\"-1\" rs=\"\" w=\"-1\" na=\"-1\" na_b=\"0\" na_i=\"0\" k=\"\" cy=\"\" g=\"0\" j=\"single\" f=\"-8044863865448479521\" e=\"7441956695529377365\" r=\"5427041362230332291\" z=\"\" FR=\"\" DR=\"\" s=\"success\" m=\"false\" ca=\"\" c=\"1\" b=\"0\" a=\"\" tm=\"1\" qid=\"\" sid=\"\" sa=\"0\"/></n><n i=\"end\" n=\"end\" t=\"4\" d=\"\" x=\"260\" y=\"70\" q=\"\"><s i=\"collaboration\" n=\"协同\" d=\"\" t=\"17\" l=\"\" q=\"\" p=\"\" o=\"\" u=\"-1\" h=\"-1\" v=\"-1\" rs=\"\" w=\"-1\" na=\"-1\" na_b=\"0\" na_i=\"0\" k=\"\" cy=\"\" g=\"0\" j=\"single\" f=\"-8044863865448479521\" e=\"7441956695529377365\" r=\"-417076050432210954\" z=\"\" FR=\"\" DR=\"\" s=\"success\" m=\"false\" ca=\"\" c=\"1\" b=\"0\" a=\"\" tm=\"1\" qid=\"\" sid=\"\" sa=\"0\"/></n><n i=\"15178208197411\" n=\"空节点\" t=\"6\" d=\"\" x=\"155\" y=\"70\" q=\"\" h=\"\" g=\"\" f=\"\" b=\"normal\" e=\"0\" l=\"1000\" c=\"false\" a=\"1\"><a k=\"roleadmin\" c=\"1\" j=\"false\" i=\"false\" f=\"BlankNode\" g=\"Node\" h=\"false\" d=\"空节点\" l=\"\" m=\"\" b=\"\" vj=\"0\"/><s i=\"inform\" n=\"知会\" d=\"\" t=\"17\" l=\"\" q=\"\" p=\"\" o=\"\" u=\"-1\" h=\"-1\" v=\"-1\" rs=\"\" w=\"-1\" na=\"-1\" na_b=\"0\" na_i=\"0\" k=\"\" cy=\"\" g=\"0\" j=\"single\" f=\"-8044863865448479521\" e=\"7441956695529377365\" r=\"-417076050432210954\" z=\"\" FR=\"\" DR=\"\" s=\"success\" m=\"false\" ca=\"\" c=\"1\" b=\"0\" a=\"\" tm=\"1\" qid=\"\" sid=\"\" sa=\"0\"/></n><l i=\"15178208113850\" n=\"\" t=\"11\" d=\"\" k=\"15178208197411\" j=\"end\" o=\"0\" h=\"3\" m=\"\" e=\"\" b=\"\" a=\"\" c=\"\"/><l i=\"15178208197412\" n=\"\" t=\"11\" d=\"\" k=\"start\" j=\"15178208197411\" o=\"0\" h=\"3\" m=\"\" e=\"\" b=\"\" a=\"\" c=\"\"/></p></ps>";

}

/**
 * 对应表wf_process_templete
 * 手动在表单模板设置中建一个空节点流程，然后拷出workflow字段
 * <ps><p t="p" s="false" i="" n="" d="" u="">
 * <n i="start" n="发起者" t="8" d="" x="50" y="70" q="" h="" g="" f="">
 * <a k="" c="1" j="false" i="false" f="" g="" h="false" d="" l="" m="" b="" vj="0"/>
 * <s i="collaboration" n="协同" d="" t="17" l="" q="" p="" o="" u="-1" h="-1" v="-1" rs=""
 *  w="-1" na="-1" na_b="0" na_i="0" k="" cy="" g="0" j="single" f="-8044863865448479521" e="7441956695529377365" 
 *  r="5427041362230332291" z="" FR="" DR="" s="success" m="false" ca="" c="1" b="0" a="" tm="1" qid="" sid="" sa="0"/>
 *  </n>
 *  <n i="end" n="end" t="4" d="" x="260" y="70" q="">
 *  <s i="collaboration" n="协同" d="" t="17" l="" q="" p="" o="" u="-1" h="-1" v="-1" rs="" 
 *  w="-1" na="-1" na_b="0" na_i="0" k="" cy="" g="0" j="single" f="-8044863865448479521" e="7441956695529377365" 
 *  r="-417076050432210954" z="" FR="" DR="" s="success" m="false" ca="" c="1" b="0" a="" tm="1" qid="" sid="" sa="0"/>
 *  </n>
 *  <n i="15178208197411" n="空节点" t="6" d="" x="155" y="70" q="" h="" g="" f="" b="normal" e="0" l="1000" c="false" a="1">
 *  <a k="roleadmin" c="1" j="false" i="false" f="BlankNode" g="Node" h="false" d="空节点" l="" 
 *  m="" b="4191330919388496736" vj="0"/>
 *  <s i="inform" n="知会" d="" t="17" l="" q="" p="" o="" u="-1" h="-1" v="-1" rs="" w="-1" na="-1" 
 *  na_b="0" na_i="0" k="" cy="" g="0" j="single" f="-8044863865448479521" e="7441956695529377365" 
 *  r="-417076050432210954" z="" FR="" DR="" s="success" m="false" ca="" c="1" b="0" a="" tm="1" qid="" sid="" sa="0"/>
 *  </n>
 *  <l i="15178208113850" n="" t="11" d="" k="15178208197411" j="end" o="0" h="3" m="" e="" b="" a="" c=""/>
 *  <l i="15178208197412" n="" t="11" d="" k="start" j="15178208197411" o="0" h="3" m="" e="" b="" a="" c=""/>
 *  </p>
 *  </ps>
 **/