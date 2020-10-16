此补丁包包含两块内容：会议排座、短信接口
1.添加致远提供的控件修复包
2.添加SQLServer JDBC jar包
3.修改以下文件使之符合生产环境：
1）：seeyon/apps_res/cap/customCtrlResource/meetingSeatResources/js/meetingSeat.js 
line:46          fieldId 为生产环境实际fieldId  
 // 回填图片展示的控件ID
self.backFieldName = "field0051";
2）：seeyon/apps_res/cap/customCtrlResource/meetingSeatResources/js/meetingSeat1.js 
line:59  改为生产环境实际表单
 //data: {'meetingId':transParams.messageObj.formdata.formmains.formmain_0180.field0028.value},     --生产环境
data: {'meetingId':transParams.messageObj.formdata.formmains.formmain_0333.field0028.value},         --测试环境
3）：修改数据入库SQL，MeetingSeatDaoImpl.java
4)   :  数据库配置文件修改 Formson0256.hbm.xml

