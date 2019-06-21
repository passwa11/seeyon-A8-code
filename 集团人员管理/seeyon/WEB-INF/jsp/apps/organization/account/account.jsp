<%--
 $Author: lilong $
 $Rev: 4423 $
 $Date:: 2012-09-24 18:13:06#$:
  
 Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 This software is the proprietary information of Seeyon, Inc.
 Use is subject to license terms.
--%>
<%@ page contentType="text/html; charset=UTF-8" isELIgnored="false"%>
<!DOCTYPE html>
<html class="h100b over_hidden">
<head>
<%@ include file="/WEB-INF/jsp/apps/organization/account/account_js.jsp"%>
<title>AccountManager</title>
<style>
.stadic_head_height {
    height: 0px;
}

.stadic_body_top_bottom {
    bottom: 50px;
    top: 100px;
}

.stadic_footer_height {
    height: 50px;
}

.stadic_layout_body {
    overflow-x: hidden;
    overflow-y: auto;
}

.stadic_right {
    float: right;
    width: 100%;
    height: 100%;
    position: absolute;
    z-index: 100;
}

.stadic_right .stadic_content {
    margin-left: 49%;
    height: 100%;
}

.stadic_left {
    width: 48%;
    float: left;
    position: absolute;
    height: 100%;
    z-index: 300;
}

.groupSizeDiv {
	width: 16.6%;
	height: 100%;
	float: left;
	border-right: 2px solid #F1F0EE;
	border-bottom: 5px solid #F1F0EE;
    cursor: pointer;
}

.ManageAccountSizeDiv {
	width: 16.6%;
	height: 100%;
	float: left;
	border-right: 2px solid #F1F0EE;
	border-bottom: 5px solid #F1F0EE;
    cursor: pointer;
}

.accountSizeDiv {
    width: 12.5%;
    height: 100%;
    float: left;
    border-right: 2px solid #F1F0EE;
    border-bottom: 5px solid #F1F0EE;
    cursor: pointer;
}

.block-div-inner {
    width: 100%;
    text-align: center;
}

.block-span-inner {
    font-size: 14px;
    color: #454b4c;
}
</style>
<script>
    $(document).ready(function() {
        $("#all-blocks").bind("mouseover", function() {
            if ($(this).width() < 1142) {
                $(this).css({
                    "overflow-x" : "scroll"
                });
            }
        });
        
        $("#all-blocks").bind("mouseout", function() {
            $(this).css({
                "overflow-x" : "hidden"
            });
        });
    });
</script>
</head>
<body class="h100b over_hidden">
    <div id='layout' class="comp bg_color_white" comp="type:'layout'">
    <c:if test="${ctp:currentUser().groupAdmin}">
   	   <div class="comp" comp="type:'breadcrumb',code:'T02_listAccounts'"></div>
    </c:if>
    <c:if test="${ctp:currentUser().administrator}">
  	  <div class="comp" comp="type:'breadcrumb',code:'T02_viewAccount'"></div>
    </c:if>
        <div class="layout_north" layout="height:40,sprit:false,border:false">
            <div id="toolbar"></div>
            <div id="searchDiv"></div>
        </div>
        <div class="layout_west" id="west" layout="width:200,minWidth:50,maxWidth:300">
            <div id="unitTree"></div>
        </div>
       <div class="layout_center over_hidden" layout="border:true" id="center">
            <div id="table_area">
                <table id="unitTable" class="flexme3" style="display: none"></table>
            </div>
            <div class="stadic_layout form_area"  id="form_area">
                <form id="accountForm" name="accountForm" method="post">
                <input type="hidden" id="orgAccountId" value="-1">
                <input type="hidden" id="id" value="-1">
                <input type="hidden" id="path" value="-1">
                <input type="hidden" id="isGroup" value="false">
                <input type="hidden" id="adminMemberId" value="-1">
                <input type="hidden" id="oldAdminName" value="">
                <input type="hidden" id="levelScope" value="-1">
                <input type="hidden" id="superior" name="superior" value="-1">
                <input type="hidden" id="isCopyGroupLevel" name="isCopyGroupLevel" value="0">
                <input type="hidden" id="isLdapEnabled" name="isLdapEnabled" value="false">
                <div class="stadic_layout_head stadic_head_height">
                </div>

            	<div id="all-blocks" style="width:100%;height:90px;overflow-y:hidden;">
                    <div style="min-width:1142px;height:85px;">
<%--						周刘成--%>
						<div class="block-div groupSizeDiv hidden" style="width:12%;" onclick="openOrgWindow('z7')">
							<div class="block-div-inner">
								<span id="zpeople" style="font-size:40px;color:#37AAF3;" logColor="#37AAF3">0</span>
							</div>
							<div class="block-div-inner">
								<span class="block-span-inner" logColor="rgb(69,75,76)">集团人员</span>
							</div>
						</div>
                        <%-- 集团 --%>
                        <div class="block-div groupSizeDiv hidden" style="width:12%;"  onclick="openOrgWindow('1')">
                            <div class="block-div-inner">
                                <span id="accountSize" style="font-size:40px;color:#00A0F0;" logColor="#00A0F0">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.groupAccount')}</span>
                            </div>
                        </div>
                        <div class="block-div groupSizeDiv hidden" style="width:12%;"  onclick="openOrgWindow('2')">
                            <div class="block-div-inner">
                                <span id="gPostSize" style="font-size:40px;color:#00C2F8;" logColor="#00C2F8">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.groupPost')}</span>
                            </div>
                        </div>
                        <div class="block-div groupSizeDiv hidden"  style="width:12%;" onclick="openOrgWindow('3')">
                            <div class="block-div-inner">
                                <span id="gLevelSize" style="font-size:40px;color:#00D6A4;" logColor="#00D6A4">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.groupLevel')}</span>
                            </div>
                        </div>
                        <div class="block-div groupSizeDiv hidden" style="width:12%;"  onclick="openOrgWindow('4')">
                            <div class="block-div-inner">
                                <span id="gRoleSize" style="font-size:40px;color:#FF9E22;" logColor="#FF9E22">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.groupRole')}</span>
                            </div>
                        </div>
                        <div class="block-div groupSizeDiv hidden" style="width:12%;"  onclick="openOrgWindow('5')">
                            <div class="block-div-inner">
                                <span id="concurrentSize" style="font-size:40px;color:#00C1F7;" logColor="#00C1F7">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.groupConcurrent')}</span>
                            </div>
                        </div>
                        <div class="block-div groupSizeDiv hidden" style="width:15.8%;" onclick="openOrgWindow('6')">
                            <div class="block-div-inner">
                                <span id="gTeamSize" style="font-size:40px;color:#37AAF3;" logColor="#37AAF3">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.groupTeam')}</span>
                            </div>
                        </div>
                        
                        <%-- 能够管理子单位的单位根 --%>
                        <div class="block-div ManageAccountSizeDiv hidden" style="width:15%;" onclick="openOrgWindow('11')">
                            <div class="block-div-inner">
                                <span id="mAccountSize" style="font-size:40px;color:#37AAF3;" logColor="#37AAF3">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.groupAccount')}</span>
                            </div>
                        </div>
                        
                       <div class="block-div ManageAccountSizeDiv hidden" style="width:15%;" onclick="openOrgDialog('12')">
                            <div class="block-div-inner">
                                <span id="mConcurrentSize" style="font-size:40px;color:#37AAF3;" logColor="#37AAF3">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.groupConcurrent')}</span>
                            </div>
                        </div>
                        
                       <div class="block-div ManageAccountSizeDiv hidden" style="width:15%;" onclick="openOrgDialog('13')">
                            <div class="block-div-inner">
                                <span id="mRedistributionSize" style="font-size:40px;color:#37AAF3;" logColor="#37AAF3">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('member.undistributed.member')}</span>
                            </div>
                        </div>
                        
                       <div class="block-div ManageAccountSizeDiv hidden" style="width:54%;">
                            <div class="block-div-inner">
                                <span style="font-size:40px;color:#37AAF3;" logColor="#37AAF3"></span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)"></span>
                            </div>
                        </div>
                        
                        <%-- 单位 --%>
                        <c:if test="${ctp:getSystemSwitch('allow_group_edit_account') == 'enable'}">
                        <div class="block-div accountSizeDiv hidden" onclick="openOrgDialog('1')">
                            <div class="block-div-inner">
                                <span id="deptSize" style="font-size:40px;color:#00A0F0;" logColor="#00A0F0">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.accountDept')}</span>
                            </div>
                        </div>
                        <div class="block-div accountSizeDiv hidden" onclick="openOrgDialog('2')">
                            <div class="block-div-inner">
                                <span id="postSize" style="font-size:40px;color:#00C2F8;" logColor="#00C2F8">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.accountPost')}</span>
                            </div>
                        </div>
                        <div class="block-div accountSizeDiv hidden" onclick="openOrgDialog('3')">
                            <div class="block-div-inner">
                                <span id="levelSize" style="font-size:40px;color:#00D6A4;" logColor="#00D6A4">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.accountLevel')}</span>
                            </div>
                        </div>
                        <div class="block-div accountSizeDiv hidden" onclick="openOrgDialog('4')">
                            <div class="block-div-inner">
                                <span id="memberSize" style="font-size:40px;color:#FF9E22;" logColor="#FF9E22">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.accountMember')}</span>
                            </div>
                        </div>
                        <div class="block-div accountSizeDiv hidden" onclick="openOrgDialog('5')">
                            <div class="block-div-inner">
                                <span id="roleSize" style="font-size:40px;color:#00C1F7;" logColor="#00C1F7">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.accountRole')}</span>
                            </div>
                        </div>
                        <div class="block-div accountSizeDiv hidden" onclick="openOrgDialog('6')">
                            <div class="block-div-inner">
                                <span id="extMemberSize" style="font-size:40px;color:#37AAF3;" logColor="#37AAF3">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.accountExtMember')}</span>
                            </div>
                        </div>
                        <div class="block-div accountSizeDiv hidden" onclick="openOrgDialog('7')">
                            <div class="block-div-inner">
                                <span id="teamSize" style="font-size:40px;color:#EA46B2;" logColor="#EA46B2">0</span>
                            </div>
                            <div class="block-div-inner">
                                <span class="block-span-inner" logColor="rgb(69,75,76)">${ctp:i18n('group.control.accountTeam')}</span>
                            </div>
                        </div>
                        <div class="block-div accountSizeDiv hidden" style="width:11%;" onclick="openOrgDialog('8')">
                            <div class="block-div-inner" style="padding-top:30px;padding-bottom:20px;">
                                <span style="font-size:16px;color:rgb(69,75,76);" logColor="rgb(69,75,76)">${ctp:i18n('group.control.accountWork')}</span>
                            </div>
                        </div>
                        </c:if>
                        <div style="clear:both;"></div>
                    </div>
                </div>

                <div class="stadic_layout_body stadic_body_top_bottom clearfix" id="formField" style="display: none">
                    <div class="stadic_right">
                        <div class="stadic_content">
                            <p class="align_right"><font color="red">*</font>${ctp:i18n('org.form.must')}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>
							<table width="90%" border="0" cellspacing="0" cellpadding="0" align="left" style="table-layout:fixed;">
								<tr class="nonGroup">
									<th nowrap="nowrap" style="width:120px"><label
										  for="text"><strong>${ctp:i18n('org.account_form.fieldset3Name.label')}</strong></label>
									</th>
									<td width="100%">&nbsp;</td>
								</tr>
								<!--adminName-->
								<tr class="nonGroup">
									<th nowrap="nowrap"><font color="red">*</font><label
										  for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.adminName.label')}:</label></th>
									<td><div class="common_txtbox_wrap">
											<input type="text" onKeydown="canInput(this);" id="adminName" maxLength="40" name="adminName" class="validate" validate="type:'string', name:'${ctp:i18n('org.account_form.adminName.label')}', notNull:true, maxLength:40, regExp:'^[\\w-]+$', errorMsg:'${ctp:i18n('loginName_isCriterionWord')}'" />
										</div>
									</td>
								</tr>
								<!--adminPassword-->
								<tr id="adminPasswordtr" class="nonGroup">
									<th nowrap="nowrap"><font color="red">*</font><label
										  for="text" class="margin_r_10 hand">${ctp:i18n('account.system.newpassword')}:</label>
									</th>
									<td><div class="common_txtbox_wrap">
											<input type="password" id="adminPass" value="123456"
												class="validate"
												validate="type:'string',name:'${ctp:i18n('account.system.newpassword')}',notNull:true,minLength:6,maxLength:50" />
										</div>
									</td>
								</tr>
								<input type="password" id="adminPasshide" value="123456"/>
								<!--validatepassword-->
								<tr id="validatepasswordtr" class="nonGroup">
									<th nowrap="nowrap"><font color="red">*</font><label
										  for="text" class="margin_r_10 hand">${ctp:i18n('account.system.validatepassword')}:</label>
									</th>
									<td><div class="common_txtbox_wrap">
											<input type="password" id="adminPass1" value="123456"
												class="validate"
												validate="type:'string',name:'${ctp:i18n('account.system.validatepassword')}',notNull:true,minLength:6,maxLength:50" />
										</div></td>
								</tr>
								<tr id="pswpromotetr" class="nonGroup">
									<th nowrap="nowrap">&nbsp;</th>
									<td class='green'>
											${ctp:i18n('manager.vaildate.length')}
									</td>
								</tr>
								<tr id="pswchecktr" class="nonGroup">
									<th nowrap="nowrap">&nbsp;</th>
									<td><div class="common_txtbox">
											<label for="checkManager"><input id="checkManager" type="checkbox" name="checkManager" value="0" />&nbsp;&nbsp;${ctp:i18n('account.message')}</label>
										</div>
									</td>
								</tr>
								
								<!-- 允许管理子单位的组织模型  -->
								<tr id="managementSubunitTr" class="nonGroup">
									<th nowrap="nowrap">&nbsp;</th>
									<td><div class="common_txtbox">
											<label for="managementSubunitTr">
											<input id="managementSubunit" type="checkbox" name="managementSubunit"/>
											${ctp:i18n('org.account.managementSubunit')}
</label>
										</div>
									</td>
								</tr>
								
								<!--serverPermission-->
								<tr class="nonGroup">
									<th nowrap="nowrap"><label for="text" class="margin_r_10 hand">
										${ctp:i18n('org.account_form.permission.server.account')}:
									</label></th>
									<td><span id="serverPermission"></span>
									</td>
								</tr>
								<!--m1Permission-->
								<tr class="nonGroup">
									<th nowrap="nowrap"><label for="text" class="margin_r_10 hand">
										${ctp:i18n('org.account_form.permission.m1.account')}(${MxVersion}):
									</label></th>
									<td><span id="m1Permission"></span>
									</td>
								</tr>
								<!--单位扩展信息-->
								<tr class="nonGroup">
									<th><label for="text"><strong>${ctp:i18n('org.account_form.fieldset4Name.label')}</strong></label>
									</th>
									<td>&nbsp;</td>
								</tr>
								<!--集团拓展信息-->
								<tr class="cGroup">
									<th><label for="text"><strong>${ctp:i18n('org.group_form.fieldset4Name.label')}</strong></label>
									</th>
									<td width="80%">&nbsp;</td>
								</tr>
								<!--account_category-->
								<tr>
									<th><label for="text" class="margin_r_10 hand">${ctp:i18n('org.metadata.account_category.label')}:</label></th>
									<td><select id="unitCategory" name="unitCategory"
										class="codecfg w100b"
										codecfg="codeId:'org_property_account_category'">
											<option value=""></option>
										</select>
									</td>
								</tr>
								<!--chiefLeader-->
								<tr>
									<th><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.manager.label')}:</label></th>
									<td><div class="common_txtbox_wrap">
											<input type="text" id="chiefLeader" class="validate"
												validate="type:'string',name:'${ctp:i18n('org.account_form.manager.label')}',maxLength:50" />
										</div></td>
								</tr>
								<!--address-->
								<tr>
									<th><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.address.label')}:</label></th>
									<td><div class="common_txtbox_wrap">
											<input type="text" id="address" class="validate"
												validate="type:'string',name:'${ctp:i18n('org.account_form.address.label')}',maxLength:50" />
										</div></td>
								</tr>
								<!--zipCode-->
								<tr>
									<th><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.zipCode.label')}:</label></th>
									<td><div class="common_txtbox_wrap">
											<input type="text" id="zipCode" class="validate"
												validate="type:'string',name:'${ctp:i18n('org.account_form.zipCode.label')}',maxLength:50" />
										</div></td>
								</tr>
								<!--telephone-->
								<tr>
									<th><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.telephone.label')}:</label></th>
									<td><div class="common_txtbox_wrap">
											<input type="text" id="telephone" class="validate"
												validate="type:'string',name:'${ctp:i18n('org.account_form.telephone.label')}',maxLength:50" />
										</div></td>
								</tr>
								<!--fax-->
								<tr>
									<th><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.fax.label')}:</label></th>
									<td><div class="common_txtbox_wrap">
											<input type="text" id="fax" class="validate"
												validate="type:'string',maxLength:50,name:'${ctp:i18n('org.account_form.fax.label')}'" />
										</div></td>
								</tr>
								<!--ipAddress-->
								<tr>
									<th><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.ipAddress.label')}:</label></th>
									<td><div class="common_txtbox_wrap">
											<input type="text" id="ipAddress" class="validate"
												validate="type:'string',maxLength:50,name:'${ctp:i18n('org.account_form.ipAddress.label')}'" />
										</div></td>
								</tr>
								<!--unitMail-->
								<tr>
									<th><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.accountMail.label')}:</label></th>
									<td><div class="common_txtbox_wrap">
											<input type="text" id="unitMail" class="validate"
												validate="type:'email',maxLength:50,name:'${ctp:i18n('org.account_form.accountMail.label')}'" />
										</div></td>
								</tr>
							</table>
                        </div>
                    </div>
                    <div class="stadic_left">
                        <p class="align_right">&nbsp;</p>
						<table width="95%" border="0" cellspacing="0" cellpadding="0"
							align="right" style="table-layout:fixed;">
							<tr class="nonGroup">
								<th width="100"><label for="text"><strong>${ctp:i18n('org.account_form.fieldset1Name.label')}</strong></label>
								</th>
								<td width="100%">&nbsp;</td>
							</tr>
							<tr class="cGroup">
								<th><label for="text"><strong>${ctp:i18n('org.group_form.fieldset1Name.label')}</strong></label>
								</th>
								<td width="75%">&nbsp;</td>
							</tr>
							<!--name-->
							<tr class="nonGroup">
								<th><font color="red">*</font><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.name.label')}:</label></th>
								<td width="100%"><div class="common_txtbox_wrap">
										<input type="text" id="name" class="validate"
											validate="type:'string',name:'${ctp:i18n('org.account_form.name.label')}',notNullWithoutTrim:true,maxLength:80,avoidChar:'\\\/|><:*?%$|,\&#39;\&#34;'" />
									</div>
								</td>
							</tr>
							<!--group_name-->
							<tr class="cGroup">
								<th><font color="red">*</font><label for="text" class="margin_r_10 hand">${ctp:i18n('org.group_form.name.label')}:</label></th>
								<td width="100%"><div class="common_txtbox_wrap">
										<input type="text" id="gname" class="validate"
											validate="type:'string',name:'${ctp:i18n('org.group_form.name.label')}',notNullWithoutTrim:true,maxLength:30,avoidChar:'\\\/|><:*?&%$|,\&#39;\&#34;'" />
									</div>
								</td>
							</tr>
							<!--secondName-->
							<tr>
								<th><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.secondName.label')}:</label></th>
								<td><div class="common_txtbox_wrap">
										<input type="text" id="secondName" class="validate"
											validate="type:'string',name:'${ctp:i18n('org.account_form.secondName.label')}',maxLength:84" />
									</div></td>
							</tr>
							<!--shortName-->
							<tr class="nonGroup">
								<th><font color="red">*</font><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.shortname.label')}:</label></th>
								<td><div class="common_txtbox_wrap">
										<input type="text" id="shortName" class="validate"
											validate="type:'string',name:'${ctp:i18n('org.account_form.shortname.label')}',notNullWithoutTrim:true,maxLength:50,avoidChar:'\\\/|><:*?%$\&#39;'" />
									</div></td>
							</tr class="cGroup">
							<!--group_shortName-->
							<tr class="cGroup">
								<th><font color="red">*</font><label for="text" class="margin_r_10 hand">${ctp:i18n('org.group_form.shortname.label')}:</label></th>
								<td><div class="common_txtbox_wrap">
										<input type="text" id="gshortName" class="validate"
											validate="type:'string',name:'${ctp:i18n('org.group_form.shortname.label')}',notNullWithoutTrim:true,maxLength:20,avoidChar:'\\\/|><:*?&%$\&#39;'" />
									</div></td>
							</tr>
							<!--code-->
							<tr class="nonGroup">
								<th><font color="red">*</font><label  
									for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.code.label')}:</label></th>
								<td><div class="common_txtbox_wrap">
										<input type="text" id="code" class="validate"
											validate="type:'string',name:'${ctp:i18n('org.account_form.code.label')}',notNullWithoutTrim:true,maxLength:50" />
									</div></td>
							</tr>
							<!--ldap/ad-->
							<tr id="ldapSet">
								<th>
								<c:if test="${'false' == LdapCanOauserLogon || false == LdapCanOauserLogon}"><font color="red">*</font></c:if>
								<label for="text" class="margin_r_10 hand">${ctp:i18n('ldap.lable.node')}:</label></th>
								<td><div class="common_txtbox_wrap">
										<input type="text" id="ldapOu" name="ldapOu"/>
									</div>
								</td>
							</tr>
							<!--group_code-->
							<tr class="cGroup">
								<th><font color="red">*</font><label  
									for="text" class="margin_r_10 hand">${ctp:i18n('org.group_form.code.label')}:</label></th>
								<td><div class="common_txtbox_wrap">
										<input type="text" id="gcode" class="validate"
											validate="type:'string',name:'${ctp:i18n('org.account_form.code.label')}',notNullWithoutTrim:true,maxLength:50,avoidChar:'!@#$%^&amp;\&#39;*+'" />
									</div></td>
							</tr>
							<!--sortId-->
							<tr id="sortIdTr" class="nonGroup">
								<th><font color="red">*</font><label  
									for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.sortId.label')}:</label></th>
								<td><div class="common_txtbox_wrap">
										<input type="text" id="sortId" class="validate" maxlength="9"
											validate="name:'${ctp:i18n('org.account_form.sortId.label')}',notNullWithoutTrim:true,isInteger:true,maxValue:9999999999,minValue:1" />
									</div>
								</td>
							</tr>
							<!--sortIdtype-->
							<tr id="sortIdTypeTr" class="nonGroup">
								<th nowrap="nowrap">&nbsp;</th>
								<td>
									<div class="common_radio_box clearfix">
										<label for="text" class="margin_r_10 hand">${ctp:i18n('org.sort.repeat.deal')}:</label>
										<label class="margin_r_10 hand"> <input
											id="sortIdtype1" type="radio" value="0" name="sortIdtype"
											class="radio_com">${ctp:i18n('org.sort.insert')}
										</label> <label class="margin_r_10 hand"> <input
											id="sortIdtype2" type="radio" value="1" name="sortIdtype"
											class="radio_com">${ctp:i18n('org.sort.repeat')}
										</label>
									</div>
								</td>
							</tr>
							<!--enabled-->
							<tr id="statusTr" class="nonGroup">
								<th nowrap="nowrap"><label for="text" class="margin_r_10 hand">${ctp:i18n('common.state.label')}:</label></th>
								<td>
									<div class="common_radio_box clearfix">
										<label class="margin_r_10 hand">
											<input type="radio" value="true" name="enabled" class="radio_com">${ctp:i18n('common.state.normal.label')}
										</label> 
										<label class="margin_r_10 hand">
											<input type="radio" value="false" name="enabled" class="radio_com">${ctp:i18n('common.state.invalidation.label')}
										</label>
									</div>
								</td>
							</tr>
							<!--description-->
							<tr class="nonGroup">
								<th nowrap="nowrap" valign="top"><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.decription.label')}:</label></th>
								<td>
									<div class="common_txtbox clearfix word_break_all">
										<textarea id="description" name="description" rows="5" class="validate w100b"
											validate="type:'string',name:'${ctp:i18n('org.account_form.decription.label')}',maxLength:1000,avoidChar:'!@#$^&amp;\&#39;*+'"></textarea>
									</div>
								</td>
							</tr>
							<!-- 单位自定义登录开关 -->
							<tr id="isCustomLoginTr" class="nonGroup">
								<th nowrap="nowrap"><label for="text" class="margin_r_10 hand">${ctp:i18n('customlogin.label.0')}:</label></th>
								<td>
									<div class="common_radio_box clearfix">
										<label class="margin_r_10 hand">
											<input type="radio" value="false" name="isCustomLoginUrl" class="radio_com">${ctp:i18n('customlogin.label.no')}
										</label>
										<label class="margin_r_10 hand">
											<input type="radio" value="true" name="isCustomLoginUrl" class="radio_com">${ctp:i18n('customlogin.label.yes')}
										</label>
									</div>
								</td>
							</tr>
							<!-- 单位自定义登录地址 TODO国际化和文字注释 -->
							<tr id="customLoginUrlTr" class="nonGroup">
								<th><label  
									for="text" class="margin_r_10 hand">${ctp:i18n('customlogin.lable.url')}:</label></th>
								<td>${ctp:i18n('customlogin.label.url.des')}<input type="text" id="customLoginUrl" name="customLoginUrl" maxlength＝"20" length="20">	
								</td>
							</tr>
							<tr id="customLoginUrlDescTr" class="nonGroup">
								<th>&nbsp;</th>
								<td class='green'>${ctp:i18n('customlogin.label.url.des.all')}</td>
							</tr>
							<!--group_decription-->
							<tr class="cGroup">
								<th nowrap="nowrap" valign="top"><label for="text" class="margin_r_10 hand"><strong>${ctp:i18n('group.description')}</strong></label></th>
								<td>
									<div class="common_txtbox clearfix">
										<textarea id="gdescription" name="gdescription" rows="5" class="validate w100b" validate="type:'string',name:'${ctp:i18n('group.description')}',maxLength:200,avoidChar:'!@#$%^&amp;\&#39;*+'"></textarea>
									</div>
								</td>
							</tr>
							<tr id="isAdmin1" class="nonGroup">
								<th><label for="text"><strong>${ctp:i18n('org.account_form.fieldset2Name.label')}</strong></label>
								</th>
								<td>&nbsp;</td>
							</tr>
							<tr class="cGroup">
								<th><label for="text"><strong>${ctp:i18n('org.group_form.fieldset2Name.label')}</strong></label>
								</th>
								<td>&nbsp;</td>
							</tr>
							<!--关联信息是否是集团-->
							<tr id="isAdmin2" class="nonGroup">
								<th nowrap="nowrap"><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.isRoot.label')}:</label></th>
								<td class="new-column">${ctp:i18n('org.account_form.isRoot.no')}</td>
							</tr>
							<tr class="cGroup">
								<th nowrap="nowrap"><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.isRoot.label')}:</label></th>
								<td class="new-column">${ctp:i18n('org.account_form.isRoot.yes')}</td>
							</tr>
							<!--serverPermission-->
							<tr class="cGroup">
								<th nowrap="nowrap"><label for="text" class="margin_r_10 hand">
									${ctp:i18n('org.account_form.permission.server.group')}:
								</label></th>
								<td><span id="serverPermissionGroup" class="margin_l_10 hand"></span>
								</td>
							</tr>
							<!--m1Permission-->
							<tr class="cGroup">
								<th nowrap="nowrap"><label for="text" class="margin_r_10 hand">
									${ctp:i18n('org.account_form.permission.m1.group')}(${MxVersion}):
								</label></th>
								<td><span id="m1PermissionGroup"></span>
								</td>
							</tr>
							<!--unit reg count permission-->
							<tr class="cGroup">
								<th nowrap="nowrap"><label for="text" class="margin_r_10 hand">
									${ctp:i18n('org.permission.unitReg.info')}:
								</label></th>
								<td><span id="unitRegCounts"></span>
								</td>
							</tr>
							<!--superiorName-->
							<tr id="isAdmin3" class="nonGroup">
								<th nowrap="nowrap"><label for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.superior.label')}:</label></th>
								<td><div class="common_txtbox_wrap">
										<input type="text" id="superiorName" name="superiorName"
											class="comp"
											comp="type:'selectPeople',mode:'open',panels:'Account',selectType:'Account',maxSize:1,onlyShowChildrenAccount:true" />
									</div>
								</td>
							</tr>
							<!-- permission -->
							<tr id="isAdmin4" class="nonGroup">
								<th nowrap="nowrap"><font color="red">*</font><label
									  for="text" class="margin_r_10 hand">${ctp:i18n('org.account_form.permission.label')}:</label></th>
								<td>&nbsp;</td>
							</tr>
							<tr id="permissionType1tr" class="nonGroup">
								<th nowrap="nowrap">
									<!--统一设置--> 
									<label class="margin_r_10 hand">
										<input type="radio" value="0" name="permissionType"
										class="radio_com">${ctp:i18n('org.account_form.permission.unified.label')}:
								</label>
								</th>
								<td>
									<div class="common_drop_list_text">
										<select id="pType" name="pType" class="w100b">
											<option value="1">${ctp:i18n('org.account_form.permission.unified.all.label')}</option>
											<option value="0">${ctp:i18n('org.account_form.permission.unified.not.label')}</option>
										</select>
									</div>
								</td>
							</tr>
							<tr id="permissionType2tr" class="nonGroup">
								<th nowrap="nowrap">
									<!--分级设置--> 
									<label class="margin_r_10 hand">
										<input type="radio" value="1" name="permissionType"
										class="radio_com">${ctp:i18n('org.account_form.permission.level.label')}:
								</label>
								</th>
								<td><input id="sLevel" type="checkbox" name="sLevel" />&nbsp;${ctp:i18n('org.account_form.permission.level.s.label')}<br />
									<input id="pLevel" type="checkbox" name="pLevel" />&nbsp;${ctp:i18n('org.account_form.permission.level.p.label')}<br />
									<input id="xLevel" type="checkbox" name="xLevel" />&nbsp;${ctp:i18n('org.account_form.permission.level.x.label')}
								</td>
							</tr>
							<tr id="permissionType3tr" class="nonGroup">
								<th nowrap="nowrap">
									<!--自由设置设置--> 
									<label class="margin_r_10 hand">
										<input type="radio" value="2" name="permissionType"
										class="radio_com">${ctp:i18n('org.account_form.permission.free.label')}:
								</label>
								</th>
								<td><select id="rangType" name="rangType" class="w100b">
										<option value="1" selected>${ctp:i18n('org.account_form.permission.range.can.label')}</option>
										<option value="0">${ctp:i18n('org.account_form.permission.range.cannot.label')}</option>
									</select>
								</td>
							</tr>
							<tr id="permissionType4tr" class="nonGroup">
								<th nowrap="nowrap">&nbsp;</th>
								<td><div class="common_txtbox_wrap">
										<input type="text" id="range" name="range" class="comp validate"
											comp="type:'selectPeople',mode:'open',panels:'Account',selectType:'Account',isCanSelectGroupAccount:false" validate="type:'string',name:'${ctp:i18n('org.account_form.permission.free.label')}',notNull:true"/>
									</div>
								</td>
							</tr>
						</table>
                    </div>
                </div>
                
	             <div class="stadic_layout_body stadic_body_top_bottom clearfix" id="tree_area" style="height:92%;">
	            	<iframe style="height: 95%;width: 100%" id="showAccountTreeFrame" name ="showAccountTreeFrame" src=""></iframe>
	            </div>
            
                </form>
                <div class="stadic_layout_footer stadic_footer_height page_action_btn"  id="formFooter" style="display: none;">
				    <div id="button_area" align="center" class="button_container border_t padding_t_10">
                        <table width="100%" border="0">
                            <tbody>
                                <tr width="100%">
                                    <td width="30%" align="center">&nbsp;
                                        <label class="margin_r_10 hand" for="conti" id="lconti">
                                            <span id="lianxu"><input id="conti" class="radio_com" value="0" type="checkbox"
                                            checked="checked">${ctp:i18n('continuous.add')}&nbsp;</span>
                                        </label>
                                        <label class="margin_r_10 hand" for="copyGroupLevel" id="lcopyGL">
                                            <span id="copyGL"><input id="copyGroupLevel" name="copyGroupLevel" class="radio_com" type="checkbox">${ctp:i18n('org.account_form.copyGroupLevel')}</span>
                                        </label>
                                    </td>
                                    <td width="50%">
                                        <a id="btnok" href="javascript:void(0)"
                                            class="common_button common_button_emphasize margin_r_10">${ctp:i18n('common.button.ok.label')}</a>
                                        &nbsp;&nbsp;
                                        <a id="btncancel" href="javascript:void(0)"
                                            class="common_button common_button_gray">${ctp:i18n('common.button.cancel.label')}</a>
                                    </td>
                                    <td width="20%"></td>
                                </tr>
                            </tbody>
                        </table>
					</div>
                </div>
            </div>
		</div>
    </div>
</body>
<script type="text/javascript">
    $("#adminPasshide").hide();

    for (var i = 0; i < $(".block-div").length; i++) {
        $(".block-div").eq(i).bind("mouseover", function() {
            $(this).css({
                "background-color" : "#2592DF"
            });
            $(this).children(".block-div-inner").each(function() {
                $(this).find("span").css({
                    "color" : "#fff"
                });
            });
        });
        
        $(".block-div").eq(i).bind("mouseout", function() {
            $(this).css({
                "background-color" : "#fff"
            });
            $(this).children(".block-div-inner").each(function() {
                $(this).find("span").css({
                    "color" : $(this).find("span").attr("logColor")
                });
            });
        });
    }
</script>
</html>