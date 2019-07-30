<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://v3x.seeyon.com/bridges/spring-portlet-html" prefix="html"%>
<%@ include file="/WEB-INF/jsp/common/INC/noCache.jsp"%>
<%@ include file="/WEB-INF/jsp/common/common_footer.jsp"%>

<link rel="stylesheet" type="text/css" href="<c:url value="/common/css/default.css${v3x:resSuffix()}" />">
<link rel="stylesheet" type="text/css" href="<c:url value="/skin/default/skin.css${v3x:resSuffix()}" />">
${v3x:skin()}
<script type="text/javascript" charset="UTF-8" src="<c:url value="/common/js/V3X.js${v3x:resSuffix()}" />"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/ajax.do?managerName=syncOrgManager"></script>
<html:link renderURL="/synorgController.do" var="syncUrl" />
<html>
<head>
<title>同步设置</title>

<script type="text/javascript">
	var v3x = new V3X();
	
	var manager = new syncOrgManager();
	
	getA8Top().showLocation("<span class=\"nowLocation_ico\"><img src=\"/seeyon/main/skin/frame/harmony/menuIcon/personal.png\"></span><span class=\"nowLocation_content\"><a style=\"cursor:default\" >组织同步插件</a> > <a href=\"javascript:void(0)\" class=\"hand\" onclick=\"showMenu(\'/seeyon/synorgController.do?method=synchOperation\')\">同步设置</a>")

	/**
	 * 点击是否同步密码选项事件
	 */
	function change(){
		if(document.getElementById("Member").checked){
			document.getElementById("synPasswordTure").disabled = false;
			document.getElementById("synPasswordFalse").disabled = false;
		}else{
			document.getElementById("synPasswordTure").disabled = true;
			document.getElementById("synPasswordFalse").disabled = true;
		}
	}
	
	/**
	 * 点击立即同步后，执行手动同步任务
	 */
	function handSync(){
		manager.synThreadOperation();
		document.getElementById("handSyncButton").disabled = true;
		// 显示同步任务正在进行。。
		document.getElementById("synchState").style.display = 'block';
		document.getElementById("submitButton").disabled = true;
	}
	
	/**
	 * 表单提交事件
	 */
	function autoSynchron(form){
		var setTimeRadioObj = document.all.setTimeRadio;
		if(setTimeRadioObj.checked){
			if(document.getElementById("isStart1").checked && document.getElementById("intervalTimeRadio").checked){
				if(document.all.intervalDay.value == '0' && document.all.intervalHour.value == '0' && document.all.intervalMin.value == '0'){
					alert("同步间隔时间必须大于0");
					return false;
				}
			}
		    document.all.submitButton.disabled = true;
		}
		getA8Top().startProc('');
		return true;
	}
	
	/**
	 * 点击启用/停用自动同步 事件
	 */
	function setOptionEnable(flag){
		var radio1Obj = document.all.setTimeRadio;
		var radio2Obj = document.all.intervalTimeRadio;
		var typeObj = document.getElementById("typeSel");
		var hourObj = document.all.hour;
		var minObj = document.all.min;
		var intervalDayObj = document.all.intervalDay;
		var intervalHourObj = document.all.intervalHour;
		var intervalMinObj = document.all.intervalMin;
		
		if(flag){
			if(radio1Obj.checked){
				clickSetTimeRadio();
			}
			if(radio2Obj.checked){
				clickIntervalTimeRadio();
			}
		}else{
			radio1Obj.disabled = true;
			radio2Obj.disabled = true;
			typeObj.disabled = true;
			hourObj.disabled = true;
			minObj.disabled = true;
			intervalDayObj.disabled = true;
			intervalHourObj.disabled = true;
			intervalMinObj.disabled = true;
		}
	}
	
	/**
	 * 点击指定时间后，将间隔时间的选择项置灰
	 */
	function clickSetTimeRadio(){
		var radio1Obj = document.all.setTimeRadio;
		var radio2Obj = document.all.intervalTimeRadio;
		var typeObj = document.getElementById("typeSel");
		var hourObj = document.all.hour;
		var minObj = document.all.min;
		var intervalDayObj = document.all.intervalDay;
		var intervalHourObj = document.all.intervalHour;
		var intervalMinObj = document.all.intervalMin;
	
		radio1Obj.disabled = false;
		radio2Obj.disabled = false;
		typeObj.disabled = false;
		hourObj.disabled = false;
		minObj.disabled =  false;
		intervalDayObj.disabled = true;
		intervalHourObj.disabled = true;
		intervalMinObj.disabled = true;
	}
	
	/**
	 * 点击间隔时间后，将指定时间的选择项置灰
	 */
	function clickIntervalTimeRadio(){
		var radio1Obj = document.all.setTimeRadio;
		var radio2Obj = document.all.intervalTimeRadio;
		var typeObj = document.getElementById("typeSel");
		var hourObj = document.all.hour;
		var minObj = document.all.min;
		var intervalDayObj = document.all.intervalDay;
		var intervalHourObj = document.all.intervalHour;
		var intervalMinObj = document.all.intervalMin;
		radio1Obj.disabled = false;
		radio2Obj.disabled = false;
		typeObj.disabled = true;
		hourObj.disabled = true;
		minObj.disabled =  true;
		intervalDayObj.disabled = false;
		intervalHourObj.disabled = false;
		intervalMinObj.disabled = false;
	}
	
	//定时调用
	function intervalometer(){
		// 获取同步状态
        var isSyning = manager.isSyning();
		if(isSyning){
			// 手动同步按钮不可用
			document.getElementById("handSyncButton").disabled = true;
			// 显示同步任务正在进行。。
			document.getElementById("synchState").style.display = 'block';
			// 提交按钮不可用
			document.getElementById("submitButton").disabled = true;
		}else{
			// 手动同步按钮可用
			document.getElementById("handSyncButton").disabled = false;
			// 隐藏提示信息
			document.getElementById("synchState").style.display = 'none';
			// 提交按钮可用
			document.getElementById("submitButton").disabled = false;	
		}
	}
	
	
	function init(){
	    alert("如果是【集团版】涉及多单位组织机构同步，【同步范围设置】必须勾选【单位】！");
		if(${isSynching}){
			// 手动同步按钮不可用
			document.getElementById("handSyncButton").disabled = true;
			// 显示同步任务正在进行。。
			document.getElementById("synchState").style.display = 'block';
			// 提交按钮不可用
			document.getElementById("submitButton").disabled = true;
		}
		
		if( '${isAutoSync}'=='true'){
			if('${synchTimeType}'=='0'){
				clickSetTimeRadio();
			}else{
				clickIntervalTimeRadio();
			}
		}
		//定时扫描同步状态
		window.setInterval("intervalometer()",10000);
	}
	
</script>
</head>
<body style="overflow: auto;" onload="init();">
	<span id="nowLocation"></span>
	<form action="${syncUrl}?method=synchOperation" name="autoForm" method="post" onsubmit="return autoSynchron(this)">
		<table border="0" cellpadding="0" cellspacing="0" width="100%" height="100%" align="center">
			<tr class="page2-header-line">
				<td width="100%" height="41" valign="top" class="page-list-border-LRD">
					<table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0">
						<tr class="page2-header-line">
							<td width="45" class="page2-header-img"><div class="notepager"></div></td>
							<td id="notepagerTitle1" class="page2-header-bg">同步参数设置</td>
							<td height="20" nowrap >
								<div id="synchState" style="display: none;" ><font color="red" size="5">同步任务正在运行中。。。。。。</font></div>
							</td>
							<td class="page2-header-line padding-right" align="right">&nbsp;</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td id="ssslist_td" class="categorySet-head" valign="top">
					<div id="ssslist" overflow: auto;">
						<table width="90%" height="100%" border="0" cellspacing="0" cellpadding="0" align="center">
							<tr valign="middle">
								<td height="50" valign="top">
								<br>
									<b>&nbsp;&nbsp;自动同步:</b>&nbsp;&nbsp; 
									<label for="isStart1">
					      				<input id="isStart1" name="isAutoSync" type="radio" value="1" onclick="setOptionEnable(true)" ${isAutoSync?'checked':''}>启用
					      			</label>&nbsp;&nbsp;
					    			<label for="isStart2">
					      				<input id="isStart2" name="isAutoSync" type="radio" value="0" onclick="setOptionEnable(false)" ${isAutoSync?'':'checked'}>停用
   					    			</label>
									&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
									<input type="button" value="立即同步" id="handSyncButton" ${isSynching ? 'disabled' : ''} onclick="handSync();"/>
								</td>
							</tr>
							<tr>
								<td valign="top">
									<fieldset>
										<legend>
											<b>设置选项</b>
										</legend>
										<table width="100%" id="autoSynchOption" border="0" cellspacing="0" cellpadding="0" align="center" height="200">
											<tr height="30">
												<td width="20%" rowspan="2" nowrap="nowrap" align="right" style="vertical-align: middle;">
													<font color="red">*</font> 同步时间设置：
												</td>
												<td width="5%" nowrap="nowrap">
													<label for="setTimeRadio">
														<input id="setTimeRadio" type="radio" name="synchTimeType" ${synchTimeType==0?'checked':''} ${isAutoSync?'':'disabled'} value="setTime" onclick="clickSetTimeRadio()"> 
														指定时间：
													</label>
												</td>
												<td width="75%" nowrap="nowrap">
													<select id="typeSel" name="date" ${isAutoSync?'':'disabled'}>
														<option value="0" ${date == '0'?'selected':''}>每天</option>
														<option value="2" ${date == '2'?'selected':''}>每周一</option>
														<option value="3" ${date == '3'?'selected':''}>每周二</option>
														<option value="4" ${date == '4'?'selected':''}>每周三</option>
														<option value="5" ${date == '5'?'selected':''}>每周四</option>
														<option value="6" ${date == '6'?'selected':''}>每周五</option>
														<option value="7" ${date == '7'?'selected':''}>每周六</option>
														<option value="1" ${date == '1'?'selected':''}>每周日</option>
													</select> 
													<select name="hour" ${isAutoSync?'':'disabled'}>
														<option value="0" ${hour == '0'?'selected':''}>0</option>
														<option value="1" ${hour == '1'?'selected':''}>1</option>
														<option value="2" ${hour == '2'?'selected':''}>2</option>
														<option value="3" ${hour == '3'?'selected':''}>3</option>
														<option value="4" ${hour == '4'?'selected':''}>4</option>
														<option value="5" ${hour == '5'?'selected':''}>5</option>
														<option value="6" ${hour == '6'?'selected':''}>6</option>
														<option value="7" ${hour == '7'?'selected':''}>7</option>
														<option value="8" ${hour == '8'?'selected':''}>8</option>
														<option value="9" ${hour == '9'?'selected':''}>9</option>
														<option value="10" ${hour == '10'?'selected':''}>10</option>
														<option value="11" ${hour == '11'?'selected':''}>11</option>
														<option value="12" ${hour == '12'?'selected':''}>12</option>
														<option value="13" ${hour == '13'?'selected':''}>13</option>
														<option value="14" ${hour == '14'?'selected':''}>14</option>
														<option value="15" ${hour == '15'?'selected':''}>15</option>
														<option value="16" ${hour == '16'?'selected':''}>16</option>
														<option value="17" ${hour == '17'?'selected':''}>17</option>
														<option value="18" ${hour == '18'?'selected':''}>18</option>
														<option value="19" ${hour == '19'?'selected':''}>19</option>
														<option value="20" ${hour == '20'?'selected':''}>20</option>
														<option value="21" ${hour == '21'?'selected':''}>21</option>
														<option value="22" ${hour == '22'?'selected':''}>22</option>
														<option value="23" ${hour == '23'?'selected':''}>23</option>
												</select> 时
												<select name="min" ${isAutoSync?'':'disabled'}>
														<option value="0" ${min == '0'?'selected':''}>0</option>
														<option value="5" ${min == '5'?'selected':''}>5</option>
														<option value="10" ${min == '10'?'selected':''}>10</option>
														<option value="15" ${min == '15'?'selected':''}>15</option>
														<option value="20" ${min == '20'?'selected':''}>20</option>
														<option value="30" ${min == '30'?'selected':''}>30</option>
														<option value="40" ${min == '40'?'selected':''}>40</option>
														<option value="50" ${min == '50'?'selected':''}>50</option>
												</select>分</td>
											</tr>
											<tr height="30">
												<td nowrap="nowrap" style="vertical-align: middle;">
													<label for="intervalTimeRadio">
														<input ${isAutoSync?'':'disabled'} id="intervalTimeRadio" type="radio" name="synchTimeType" ${synchTimeType!=0?'checked':''} value="intervalTime" onclick="clickIntervalTimeRadio()" />
														间隔时间：
													</label>
												</td>
												<td style="vertical-align: middle;">
													<select name="intervalDay" ${isAutoSync?'':'disabled'}>
															<option value="0" ${intervalDay == '0'?'selected':''}>0</option>
															<option value="1" ${intervalDay == '1'?'selected':''}>1</option>
															<option value="2" ${intervalDay == '2'?'selected':''}>2</option>
															<option value="3" ${intervalDay == '3'?'selected':''}>3</option>
															<option value="4" ${intervalDay == '4'?'selected':''}>4</option>
															<option value="5" ${intervalDay == '5'?'selected':''}>5</option>
															<option value="6" ${intervalDay == '6'?'selected':''}>6</option>
													 </select> 天
													 <select name="intervalHour" ${isAutoSync?'':'disabled'}>
															<option value="0" ${intervalHour == '0'?'selected':''}>0</option>
															<option value="1" ${intervalHour == '1'?'selected':''}>1</option>
															<option value="2" ${intervalHour == '2'?'selected':''}>2</option>
															<option value="3" ${intervalHour == '3'?'selected':''}>3</option>
															<option value="4" ${intervalHour == '4'?'selected':''}>4</option>
															<option value="5" ${intervalHour == '5'?'selected':''}>5</option>
															<option value="6" ${intervalHour == '6'?'selected':''}>6</option>
															<option value="8" ${intervalHour == '8'?'selected':''}>8</option>
															<option value="10" ${intervalHour == '10'?'selected':''}>10</option>
															<option value="12" ${intervalHour == '12'?'selected':''}>12</option>
															<option value="24" ${intervalHour == '24'?'selected':''}>24</option>
													 </select> 小时 
													 <select id="type" name="intervalMin" ${isAutoSync?'':'disabled'}>
															<option value="0" ${intervalMin == '0'?'selected':''}>0</option>
															<option value="5" ${intervalMin == '5'?'selected':''}>5</option>
															<option value="10" ${intervalMin == '10'?'selected':''}>10</option>
															<option value="15" ${intervalMin == '15'?'selected':''}>15</option>
															<option value="20" ${intervalMin == '20'?'selected':''}>20</option>
															<option value="30" ${intervalMin == '30'?'selected':''}>30</option>
															<option value="40" ${intervalMin == '40'?'selected':''}>40</option>
															<option value="50" ${intervalMin == '50'?'selected':''}>50</option>
															<option value="60" ${intervalMin == '60'?'selected':''}>60</option>
													 </select> 分钟
												 </td>
											</tr>
											
											<tr height="60">
												<td nowrap="nowrap" align="right" style="vertical-align: middle;"><font color="red">*</font> 同步范围设置：</td>
												<td style="vertical-align: middle;" colspan="2">
												    <input  type="checkbox" id="Unit" name="synModul" value="Unit" <c:if test="${fn:contains(synScope,'Unit')}"> checked</c:if> /> 单位&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
													<input  type="checkbox" id="Department" name="synModul" value="Department" <c:if test="${fn:contains(synScope,'Department')}"> checked</c:if> /> 部门&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
												    <input  type="checkbox" id="Post" name="synModul" value="Post" <c:if test="${fn:contains(synScope,'Post')}"> checked</c:if> /> 岗位&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
												    <input  type="checkbox" id="Level" name="synModul" value="Level" <c:if test="${fn:contains(synScope,'Level')}"> checked</c:if> /> 职务
													<input  type="checkbox" id="Member" name="synModul" value="Member" onclick="change()"  <c:if test="${fn:contains(synScope,'Member')}"> checked</c:if> /> 人员&nbsp;&nbsp;
													（是否同步密码
													<input  id="synPasswordTure" type="radio" name="synPassword" value="true"  ${synPassword=='true'? 'checked':''} <c:if test="${!fn:contains(synScope,'Member')}"> disabled</c:if> /> 是
													<input  id="synPasswordFalse" type="radio" name="synPassword" value="false"  ${synPassword=='false'? 'checked':''} <c:if test="${!fn:contains(synScope,'Member')}"> disabled</c:if> /> 否）&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                           			<input  type="checkbox"  name="synModul" value="DeptRole" <c:if test="${fn:contains(synScope,'DeptRole')}"> checked</c:if> /> 部门角色
                                           		</td>
											</tr>
											<tr height="100">
												<td colspan="3">
													<table width="100%" border="0" cellspacing="0" cellpadding="0" align="center"  height="90">
														<tr height="30">
															<td width="19%" rowspan="4" nowrap="nowrap" align="right" style="vertical-align: middle;">
																<font color="red">*</font> 同步参数设置：
															</td>
															<td width="6%">
																岗位名称
															</td>
															<td width="15%" nowrap="nowrap">
																<div class="common_txtbox_wrap">
																	<input type="text" class="validate word_break_all" name="defaultPostName"  value="<c:out value="${defaultPostName}" escapeXml="true" />"  id="defaultPostName"   validate="notNull"/> 
																</div>
															</td>
															<td width="60%">
																<font color="red">&nbsp;&nbsp;（人员岗位信息为空时的默认岗位名称，编码为00000000）</font>
															</td>
														</tr>
														<tr height="30">
															<td>
																职务名称
															</td>
															<td nowrap="nowrap" style="vertical-align: middle;">
																<div class="common_txtbox_wrap">
																	<input  type="text" name="defaultLevelName" value="<c:out value="${defaultLevelName}" escapeXml="true" />" id="defaultLevelName"   validate="notNull" /> 
																</div>
															</td>
															<td>
																<font color="red">&nbsp;&nbsp;（人员职务信息为空时的默认职务名称，编码为00000000）</font>
															 </td>
														</tr>
														<tr height="30">
															<td>
																登录密码
															</td>
															<td nowrap="nowrap" style="vertical-align: middle;">
																<div class="common_txtbox_wrap">
																	<input  type="text" name="defaultPassword" value="<c:out value="${defaultPassword}" escapeXml="true" />" id="defaultPassword"    validate="notNull"/> 
																</div>
															</td>
															<td>
																<font color="red">&nbsp;&nbsp;（新增人员时的默认登录密码）</font>
															</td>
														</tr>
														<tr height="30">
															<td>
																根部门编码
															</td>
															<td nowrap="nowrap" style="vertical-align: middle;">
																<div class="common_txtbox_wrap">
																	<input  type="text" name="rootDeptCode" value="<c:out value="${rootDeptCode}" escapeXml="true" />" id="rootDeptCode" /> 
																</div>
															</td>
															<td>
																<font color="red">&nbsp;&nbsp;（中间表根部门编码）</font>
															</td>
														</tr>
													</table>
													</td>
											</tr>

											
										</table>
									</fieldset>
								</td>
							</tr>
							<tr>
								<td>
									<p class="description-lable">
										<b>说明</b><br />
										1.为了不影响白天的工作，同步任务运行时间应该选择在夜间进行。
										<br>
										2.如果不同步岗位或职务，则人员的岗位和职务信息也不会同步，岗位和职务信息将设置为默认值，编码为00000000。
										<br>
										3.人员的岗位类型默认为管理类，职务级别序号默认为1，如需调整请到组织机构设置
									</p>
								</td>

							</tr>

						</table>
					</div>
				</td>
			</tr>
			<tr>
				<td height="20" align="center" class="tab-body-bg bg-advance-bottom" style="height: 20px; border: 0px">
					<input type="submit" id="submitButton" value="确定" class="button-default-2">&nbsp; 
					<input type="button" onclick="getA8Top().document.getElementById('homeIcon').click();" value="取消" class="button-default-2">
				</td>
			</tr>
		</table>
	</form>
	<iframe id="addConfigFrame" name="addConfigFrame" frameborder="0" height="0" width="0" scrolling="no" marginheight="0" marginwidth="0"></iframe>
	<iframe name="hiddenFrame" id="hiddenFrame" width="0" height="0" frameborder="0"></iframe>
</body>
</html>