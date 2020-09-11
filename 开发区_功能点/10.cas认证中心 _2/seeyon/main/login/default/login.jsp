<%@ page trimDirectiveWhitespaces="true" %>
<%@ page contentType="text/html; charset=utf-8" isELIgnored="false"%>
<!DOCTYPE html>
<html class="h100b overflow_login">
<head>
<%@ include file="/main/common/login_header.jsp"%>
<c:if test="${includeJsp}">
    <jsp:include page="${pageUrl}" />
</c:if>
<link rel="stylesheet" type="text/css" href="${path}/main/login/default/css/loginSlide.css${ctp:resSuffix()}" />
<link rel="stylesheet" type="text/css" href="${path}/main/login/default/css/login.css${ctp:resSuffix()}" />
<link rel="stylesheet" type="text/css" href="${path}/skin/dist/fonts/common/iconfont.css${ctp:resSuffix()}" />
<script type="text/javascript" src="/seeyon/i18n_init_<%=locale%>.js${ctp:resSuffix()}"></script>
<script type="text/javascript" src="${path}/common/js/passwdcheck.js${ctp:resSuffix()}"></script>
<c:if test="${ctp:hasPlugin('zx')}">
	<script type="text/javascript" src="/seeyon/apps_res/uc/rongcloud/chat.js${ctp:resSuffix()}"></script>
</c:if>
</head>
<body onload="calSelecterHeight();" class="h100b" style="margin:0;padding:0;${loginFrom!='loginPortal'?'overflow:hidden;':'' }">
    <c:choose>
        <c:when test="${loginFrom!='loginPortal'}">
            <!-- 传统登录页 -->
            <div id="login_content">
                <div id="login_wrap">
                    <div id="login_bg" class="slideBox">
                        <ul id="scroll_ul" class="slideImgs"> </ul>
                    </div>
                    <%-- 模板布局：all，全屏；t_b，上下；l_r，左右；center，上下留白；--%>
                    <form method="post" action="${path}/main.do?method=login" id="login_form" name="loginform" onsubmit="checkPwdStrength();">
                        <input id="authorization" type="hidden" name="authorization" value="${authorization}" />
                       	<input id="timezone" type="hidden" name="login.timezone" value=""/>
                       	<input id="province" type="hidden" name="province" value=""/>
                       	<input id="city" type="hidden" name="city" value=""/>
                       	<input id="rectangle" type="hidden" name="rectangle" value=""/>
                        <div id="login_area_div" class="login_area_${layout}">
                            <c:if test="${ctp:getSystemProperty('portal.favicon')!='U8'&&(showQr==null?true:showQr)}">
                                <div class="qrCodeBtn"></div>
                            </c:if>
                            <div class="login_area_body">
                                <c:choose>
                                    <c:when test="${ServerState}">
                                        <!-- 系统维护显示 -->
                                        <div id="maintainArea" class="maintainArea">
                                            ${ctp:i18n_2('login.label.ErrorCode.8',ServerStateComment,OnlineNumber)}
                                        </div>
                                    </c:when>
                                    <c:when test="${sessionScope['com.seeyon.current_user'] != null}">
                                        <!-- 已登录显示 -->
                                        <div id="loggedArea" class="loggedArea">
                                            ${ctp:i18n('login.label.alreadyLogin')}
                                            <br>
                                            <br>
                                            <div class="align_right margin_t_5">
                                                <a onClick="getCtpTop().open('','_self','');getCtpTop().close()" class="color_blue">${ctp:i18n('common.button.close.label')}</a> &nbsp;&nbsp;
                                                <a href='${path}/main.do?method=logout' class="color_blue">${ctp:i18n('login.label.Logout')}</a>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="login_box">
                                            <div class="pic">
                                                <div class="pic_box"><img src="${ctp:avatarImageUrl(cookie.avatarImageUrl==null? 0 : cookie.avatarImageUrl.value)}" /></div>
                                                <div class="pic_box_bg"></div>
                                            </div>
                                            <div class="login_top">
                                                <div id="login_text_div" class="text">
                                                    <c:if test="${serverType == 1}">
                                                        ${ctp:i18n(productCategory)} ${ctp:i18n_1("login.edition.regsiter.number",maxOnline)}
                                                    </c:if>
                                                    <c:if test="${serverType == 2}">
                                                        ${ctp:i18n(productCategory)} ${ctp:i18n_1("login.edition.number",maxOnline)}
                                                    </c:if>
                                                </div>
                                                <!-- 用户名 -->
                                                <div class="username">
                                                    <em class="login_username_em"></em><input id="login_username" name="login_username" type="text" style="border:0;" />
                                                    <input id="trustdo_type" name="trustdo_type" type="hidden" value=""/>
                                                </div>
                                                <!-- 密码 -->
                                                <div class="password">
                                                    <em clas="login_password_em"></em><input id="login_password" name="login_password" type="password" />
                                                </div>
                                                <input type="hidden" id="login_validatePwdStrength" name="login_validatePwdStrength" value="4">
                                                <!-- 验证码 -->
                                                <div class="captcha">
                                                    <c:if test="${verifyCode }">
                                                        <div class="captcha_box">
                                                            <input id="VerifyCode" name="login.VerifyCode" type="text" maxlength="4" />
                                                        </div>
                                                        <img border="0" width="92" height="48" id="VerifyCodeImg"
                                                             align="absmiddle" src="${path}/verifyCodeImage.jpg"
                                                             onClick="changeVerifyCodeImg(1);" title="${ctp:i18n('login.label.VerifyCodeRf')}">
                                                    </c:if>
                                                    <c:if test="${'koal'==caFactory&&'yes'==sslVerifyCertValue&&'noKey'!=keyNum}">
                                                        <input type="hidden" name="keyNum" value="${keyNum }">
                                                    </c:if>
                                                    <c:if test="${hasPluginCA}">
                                                        <input id="caCertMark" type="hidden" name="caCertMark" value="">
                                                    </c:if>
                                                </div>
                                                <!-- 登陆按钮 -->
                                                <div class="login_btn">
                                                    <input type="button" id="login_button" class="point" value="${ctp:i18n('login.label.Login')}" onClick="loginButtonOnClickHandler();" />
                                                    <input id="submit_button" type="submit" style="display: none" value="" />
                                                </div>
                                            </div>
                                        </div>
                                        <div id="login_bottom_div" class="login_bottom">
                                            <div class="fzIntall_area">
                                                <div class="zhixin zhixin_left" title="${ctp:i18n('login.tip.retrievepwd')}">
												<a onclick="retrievePassword();"><font color="white">${ctp:i18n('login.tip.retrievepwd')}</font></a>
												</div>

                                                <div id="login_bottom_div_assistantSetup_classic" class="hand zhixin zhixin_left" title="${ctp:i18n("login.tip.assistantSetup")}" onClick="openAssistantSetup()">
                                                    <font>${ctp:i18n("login.tip.assistantSetup")}</font>
                                                </div>

                                                <c:if test="${ctp:hasPlugin('zx')}">
                                                    <div class="zhixin zhixin_left" title="${ctp:i18n("portal.login.zhixinclient")}" onclick="downloadZX_login()" style="cursor: pointer;">
                                                        <font color="white">${ctp:i18n("portal.login.zhixinclient")}</font>
                                                    </div>
                                                </c:if>

												<c:if test="${ctp:hasPlugin('i18n')}">
													<div class="right zhixin_language_select" style="width:105px;">
														<select id="login_locale"></select>
													</div>
												</c:if>
                                            </div>

                                        </div>
                                        <!-- 第五套布局二维码入口 -->
                                        <div class="qrcodeArea" style="display: none;">
                                        <c:if test="${ctp:getSystemProperty('portal.favicon')!='U8'&&(showQr==null?true:showQr)}">
                                            <span class="qrcodeAreaLogin">${ctp:i18n('portal.login.useQrCode')}</span>
                                        </c:if>
                                        </div>
                                        <c:if test="${loginFrom!='loginPortal'}">
	                                        <!-- 密码错误显示 -->
	                                        <div id="login_error" class="login_error" style="display:none"><div></div></div>
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </form>
                     <div id="login_logo"><img src=""></div>
                    <div class="masks top_mask"></div>
                    <div class="masks bottom_mask"></div>
                    <div class="icp_info"></div>
                </div>
            </div>
            <div class="appendObject"></div>
        </c:when>
        <c:otherwise>
            <%-- 登录前门户--%>
            <div class="div4loginPrePortal">
            <form method="post" action="${path}/main.do?method=login" id="login_form" name="loginform" onsubmit="checkPwdStrength();">
                <input id="authorization" type="hidden" name="authorization" value="${authorization}" />
               	<input id="timezone" type="hidden" name="login.timezone" value=""/>
               	<input id="province" type="hidden" name="province" value=""/>
                <input id="city" type="hidden" name="city" value=""/>
                <input id="rectangle" type="hidden" name="rectangle" value=""/>
                <div id="login_area_div" class="login_area_center fromLoginPrePortal">
                    <div class="qrCodeBtn"></div>
                    <div class="login_area_body">
                        <c:choose>
                            <c:when test="${ServerState}">
                                <!-- 系统维护显示 -->
                                <div id="maintainArea" class="maintainArea">
                                    ${ctp:i18n_2('login.label.ErrorCode.8',ServerStateComment,OnlineNumber)}
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="login_box">
                                    <div class="pic">
                                        <div class="pic_box"></div>
                                        <div class="pic_box_bg"></div>
                                    </div>
                                    <div class="login_top">
                                        <div id="login_text_div" class="text">
                                            <c:if test="${serverType == 1}">
                                                ${ctp:i18n(productCategory)} ${ctp:i18n_1("login.edition.regsiter.number",maxOnline)}
                                            </c:if>
                                            <c:if test="${serverType == 2}">
                                                ${ctp:i18n(productCategory)} ${ctp:i18n_1("login.edition.number",maxOnline)}
                                            </c:if>
                                        </div>
                                        <!-- 用户名 -->
                                        <div class="username">
                                            <em class="login_username_em"></em>
                                            <div>
                                                <input id="login_username" name="login_username" type="text" style="border:0;" />
                                            </div>
                                        </div>
                                        <!-- 密码 -->
                                        <div class="password">
                                            <em clas="login_password_em"></em>
                                            <div>
                                                <input id="login_password" name="login_password" type="password" />
                                            </div>
                                        </div>
                                        <input type="hidden" id="login_validatePwdStrength" name="login_validatePwdStrength" value="4">
                                        <!-- 验证码 -->
                                        <div class="captcha">
                                            <c:if test="${verifyCode }">
                                                <div class="captcha_box">
                                                    <input id="VerifyCode" name="login.VerifyCode" type="text" maxlength="4" />
                                                </div>
                                                <img border="0" width="92" height="48" id="VerifyCodeImg"
                                                     align="absmiddle" src="${path}/verifyCodeImage.jpg"
                                                     onClick="changeVerifyCodeImg(0);" title="${ctp:i18n('login.label.VerifyCodeRf')}">
                                            </c:if>
                                            <c:if test="${'koal'==caFactory&&'yes'==sslVerifyCertValue&&'noKey'!=keyNum}">
                                                <input type="hidden" name="keyNum" value="${keyNum }">
                                            </c:if>
                                            <c:if test="${hasPluginCA}">
                                                <input id="caCertMark" type="hidden" name="caCertMark" value="">
                                            </c:if>
                                        </div>
                                        <!-- 登陆按钮 -->
                                        <div class="login_btn">
                                            <input type="button" id="login_button" class="point" value="${ctp:i18n('login.label.Login')}" onClick="loginButtonOnClickHandler();" />
                                            <input id="submit_button" type="submit" style="display: none" value="" />
                                        </div>
                                    </div>
                                </div>
                                <div id="login_bottom_div" class="login_bottom">
                                    <div class="fzIntall_area">
                                        <div class="zhixin zhixin_left" title="${ctp:i18n('login.tip.retrievepwd')}"><a onclick="retrievePassword();">
										<font>${ctp:i18n('login.tip.retrievepwd')}</font></a>
										</div>

                                        <div id="login_bottom_div_assistantSetup" class="hand zhixin zhixin_center" title="${ctp:i18n("login.tip.assistantSetup")}" onClick="openAssistantSetup()">
                                            <font>${ctp:i18n("login.tip.assistantSetup")}</font>
                                        </div>

                                        <c:if test="${ctp:hasPlugin('zx')}">
                                            <div class="zhixin zhixin_center" title="${ctp:i18n("portal.login.zhixinclient")}" onclick="downloadZX_login()" style="cursor: pointer;">
                                                <font color="white">${ctp:i18n("portal.login.zhixinclient")}</font>
                                            </div>
                                        </c:if>

										<c:if test="${ctp:hasPlugin('i18n')}">
											<div class="right zhixin_language_select" style="width:105px;margin-top:2px">
												<select id="login_locale"></select>
											</div>
										</c:if>
                                    </div>

                                </div>
                                <!-- 第五套布局二维码入口 -->
                                <div class="qrcodeArea" style="display: none;">
                                <c:if test="${ctp:getSystemProperty('portal.favicon')!='U8'&&(showQr==null?true:showQr)}">
                                    <span class="qrcodeAreaLogin">${ctp:i18n("portal.login.useQrCode")}</span>
                                </c:if>
                                </div>
                                <c:if test="${loginFrom!='loginPortal'}">
                                <!-- 密码错误显示 -->
                                <div id="login_error" class="login_error" style="display:none"><i class="tag"></i><div></div></div>
								</c:if>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </form>
            </div>
        </c:otherwise>
    </c:choose>

    <!-- 二维码,7.1sp1调整到这里了 -->
    <div id="QrcodeArea" class="QrcodeArea">
        <div class="QrMask" onclick="javascript:hideQrcodeArea('mask')"></div>
        <div id="QrMask4LoginPre" class="QrMask4LoginPre">
            <div class="loginPreTips">${ctp:i18n("portal.login.pleaseScan")}</div>
        </div>
        <div class="wrapper">
            <div class="closeBtn" onclick="javascript:hideQrcodeArea('btn')"><i class="syIcon sy-close"></i></div>
            <div class="topText">
                <a id="flashQrcode">${ctp:i18n("portal.login.flashQrcode")}</a>
                <span class="padding_lr_10">|</span>
                <a target="_blank" href="${path}/main.do?method=qrCodeHelp">${ctp:i18n("portal.login.wechat.help")}</a>
            </div>
            <div class="ScrollArea">
                <div id="prevBtn" class="prevBtn" onclick="javascript:showPrevQRPage()" onselectstart="return false"><i class="syIcon sy-arrow-left"></i></div>
                <div id="nextBtn" class="nextBtn" onclick="javascript:showNextQRPage()" onselectstart="return false"><i class="syIcon sy-arrow-right"></i></div>
                <div class="qrCodeGroup">
                    <!-- 每屏最多4个二维码，超过4个就得新起一个UL -->
                    <ul>
                        <li id="qrcode1">
                            <div class="qrcode">
                                <!--这里放置微信扫码登录的二维码图片-->
                            </div>
                            <div class="qrtext">${ctp:i18n("portal.login.qrtext")}</div>
                        </li>
                        <li id="qrcode2">
                            <c:if test="${mobile=='m1'}">
                                <div class="qrcode"><img src="${path}/main/login/default/images/mi-qrcode.png${ctp:resSuffix()}"></div>
                            </c:if>
                            <c:if test="${mobile=='m3'}">
								<c:choose>
									<c:when test="${ctp:getSysFlagByName('sys_isGovVer')}">
										<div class="qrcode">
											<img src="${path}/main/login/default/images/g6-m3-qrcode.png${ctp:resSuffix()}">
										</div>
									</c:when>
									<c:otherwise>
										<div class="qrcode">
											<img src="${path}/main/login/default/images/m3-qrcode.png${ctp:resSuffix()}">
										</div>
									</c:otherwise>
								</c:choose>
							</c:if>
							<div class="qrtext">${ctp:i18n("portal.login.qrtext2")}</div>
                        </li>
                        <c:if test="${m3ServerBarCode!='' && m3ServerBarCode!=null}">
                        <li id="qrcode3">
                            <div class="qrcode"><img src="${path}/fileUpload.do?method=showRTE&fileId=${m3ServerBarCode }&type=image"></div>
                            <div class="qrtext">${ctp:i18n('portal.login.m3ServerQr')}</div>
                        </li>
                        </c:if>
                        <li id="qrcode4" style="display: none;">
                            <div class="qrcode"><img id="u37_img" style="width: 171px; height: 171px;" src=""></div>
                            <div class="qrtext">
                                    ${ctp:i18n("trustdo.qrcode.auth")}
                            </div>
                        </li>
                    </ul>
                    <!-- 如果有新增的二维码，请按每4个码组成1个UL的规则进行添加 -->
                    <!--
                    <ul>
                        <li id="qrcode5">
                            <div class="qrcode"><img src="${path}/main/login/default/images/addChangeImg.png${ctp:resSuffix()}"></div>
                            <div class="qrtext">来凑数的测试二维码</div>
                        </li>
                    </ul>
                    -->
                </div>
                <div id="qrCodeGroupNum" class="qrCodeGroupNum"><span class="current"></span></div>
            </div>
        </div>
    </div>

</body>
<c:if test="${loginFrom!='loginPortal'}">
<script type="text/javascript" src="${path}/decorations/js/jquery.loginSlide.js${ctp:resSuffix()}"></script>
</c:if>
<script type="text/javascript" src="${path}/main/login/default/loginPreview.js${ctp:resSuffix()}"></script>
<c:if test="${ctp:getSysFlagByName('sys_isGovVer') and ctp:hasPlugin('trustdo')}">
	<script type="text/javascript" src= "${path}/apps_res/trustdo/js/polling.js${ctp:resSuffix()}"></script>
</c:if>
<script type="text/javascript">
    var verifyCode = "${ctp:escapeJavascript(verifyCode)}";
	var canLocation = "${canLocation}";
	var locationUrl = "${locationUrl}&output=jsonp";
	if(canLocation=='true' && locationUrl!=''){
		$.getScript(locationUrl + "&callback=setCurrentCity");
	}
	function setCurrentCity(json){
		if (json && json.province && json.city && json.rectangle) {
			document.getElementById("province").value= json.province;
			document.getElementById("city").value= json.city;
			document.getElementById("rectangle").value= json.rectangle;
		}
	}
	var btnbgc= "${ctp:escapeJavascript(btnbgc)}";
	var btombgc= "${ctp:escapeJavascript(btombgc)}";
	var bgc= "${ctp:escapeJavascript(bgc)}";
    var login_index = "userLogin";
    var resSuffix = "${ctp:resSuffix()}";
    var loginFromBol = ${loginFrom!='loginPortal'};
    var _layout = "loginPre"; //登录前门户登录框样式
    if(loginFromBol){
        _layout = "${ctp:escapeJavascript(layout)}"; //模板布局：all，全屏；t_b，上下；l_r，左右；center，上下留白；
    }else{
    	var rowList= "${ctp:escapeJavascript(rowList)}";
    	if(rowList==""){
    		rowList="blank";
    	}
    }
    $.ctx.hotSpots = <c:out value="${hotSpotsJsonStr}" default="null" escapeXml="false" />;
    var imgSize=<c:out value="${bgImgSize}" default="null" escapeXml="false" />;
    var entryTime = new Date().getTime();
    var times = 0;
    $(function() {

    	loginDefault();

        if(loginFromBol){
            //选中光标改变背景色
            $("#login_username").focus(function(){
                $(this).parent().css("background-color","#FFF");
            }).blur(function(){
                $(this).parent().css("background-color","rgba(255,255,255,0.7)");
            });

            $("#login_password").focus(function(){
                $(this).parent().css("background-color","#FFF");
            }).blur(function(){
                $(this).parent().css("background-color","rgba(255,255,255,0.7)");
            });
            $("#VerifyCode").focus(function(){
                $(this).parent().css("background-color","#FFF");
            }).blur(function(){
                $(this).parent().css("background-color","rgba(255,255,255,0.7)");
                if(_layout == "center"){
                    $(this).parent().css("background-color","transparent");
                }
            });

            //选中光标改变背景色,第四套登录框特殊处理
            $(".login_area_center #login_username").focus(function(){
                $(this).parent().css("background-color","#FFF");
            }).blur(function(){
                $(this).parent().css("background-color","transparent");
            });

            $(".login_area_center #login_password").focus(function(){
                $(this).parent().css("background-color","#FFF");
            }).blur(function(){
                $(this).parent().css("background-color","transparent");
            });
        }else{
            <%--登录前--%>
            $(".fromLoginPrePortal #login_username").focus(function(){
                $(this).css("background-color","#FFF");
            }).blur(function(){
                $(this).css("background-color","rgba(255,255,255,0.7)");
            });

            $(".fromLoginPrePortal #login_password").focus(function(){
                $(this).css("background-color","#FFF");
            }).blur(function(){
                $(this).css("background-color","rgba(255,255,255,0.7)");
            });
            $(".fromLoginPrePortal #VerifyCode").focus(function(){
                $(this).css("background-color","#FFF");
            }).blur(function(){
                $(this).css("background-color","rgba(255,255,255,0.7)");
            });
        }
        if(loginFromBol){
	        $("#login_username").focus();
	        $("#login_password").blur();
	        $("#VerifyCode").blur();
        }

        //缩小语言选择框宽度
        $("#login_locale_dropdown").mouseenter(function(event) {
            $("#login_locale_dropdown_content").height("auto");
        });
        //IE9以下头像边框不显示
        if ($.browser.msie){
            if($.browser.version < 9){
                $(".pic_box_bg").hide();
                $(".pic_box img").css({"display":"block","width":"92px","height":"92px","border":"2px solid #FFF"});
                //IE9以下背景色处理,统一背景色和底部色
                if(loginFromBol){ //排除登录栏目
                    $("#login_area_div").css({"background":" url(${path}/main/login/default/images/black40.png)"});
                }
                $(".login_bottom").css({"background":" url(${path}/main/login/default/images/whight40.png)"});
            };
        };
        //针对第五套布局显示二维码入口
        if(_layout == "center_big"){
            $(".qrcodeArea").show();
        }else{
            $(".qrcodeArea").hide();
        }
        //二维码
        changeLoginMode(_layout);

        if ($.browser.msie) {
            //辅助程序
            $(".appendObject").append('<OBJECT name="OneSetup" class="hidden" classid="clsid:6076464C-7D15-42DF-829C-7A0194D4D61E" codebase="<c:url value="/common/setup/install.cab" />#version=1,0,0,4" width=0% height=0% align=center hspace=0 vspace=0></OBJECT>');

        }
        if(loginFromBol){
	        //改变窗口大小，重新加载背景图片
	        window.onresize = function(){
                if(_bgialign && _bgialign == "zoom"){
                    setSliderBgImg();
                }
	            changeSlide(_changebgispeed);
	        }
	        //调整登录框的位置
	        adjustLoginPosition();
        }

        initQRcodeArea();

        //个性化设置：是否显示二维码，是否显示版本和并发，登录框底部颜色、按钮颜色、背景颜色
        $('body').append("<style>" +
                "body{background-color:"+ bgc +" }" +
                "#login_button{background-color:"+ btnbgc +" }" +
                ".fromLoginPrePortal .login_bottom{background-color:"+ btombgc +" }" +
                "</style>");
        if(rowList == "qrCode" || rowList == "qrCode,blank"){
            $("#login_text_div").css("visibility", "hidden");
            $(".qrCodeBtn").css("visibility", "visible");
        }else if(rowList == "productInfo" || rowList == "productInfo,blank"){
            $(".qrCodeBtn").css("visibility", "hidden");
            $("#login_text_div").css("visibility", "show");
        }else if(rowList == "qrCode,productInfo,blank" || rowList == "qrCode,productInfo"){
            $("#login_text_div").css("visibility", "visible");
            $(".qrCodeBtn").css("visibility", "visible");
        }else if(rowList == "blank"){
            $("#login_text_div").css("visibility", "hidden");
            $(".qrCodeBtn").css("visibility", "hidden");
        }

        var topValue= "${ctp:escapeJavascript(param.topValue)}";
        <%--if(loginFromBol){--%>
        	//判断是否加载了trustdo插件，若加载：替换login.css为login_trustdo.css
        	<c:if test="${ctp:getSysFlagByName('sys_isGovVer') and ctp:hasPlugin('trustdo')}">
            	changeCssAndLoadJsByTrustdo();
            </c:if>
        <%--}--%>
        if(rowList == "blank"){
            $('body').append("<style>" +
            ".fromLoginPrePortal .login_area_body{margin-top: 5px;}" +
            "</style>");
        }

    });

    var sendSMSCodeTime = 119;
    function UpdateLoginSeed(){
        $.ajax({
            async : false,
            type: "GET",
            url: _ctxPath+"/main.do?method=updateLoginSeed",
            dataType : 'text',
            success : function(data) {
                if(data&&data!=""&&_SecuritySeed!=data){
                    _SecuritySeed=data;//登录加密种子需要更新
                }
            },
            error : function(XMLHttpRequest, textStatus, errorThrown) {
                alert($.i18n('login.tip.failure'));
            }
        });
    }
    function loginCallback() {
        return true;
    }

    function retrievePassword(){
    	$.ajax({
    		sync : true,
    		type: "POST",
    		url: _ctxPath+"/personalBind.do?method=isCanUse",
    		dataType : 'text',
    	      success : function(data) {
    	    	  var isCanUse = jQuery.parseJSON(data);
    	    	     if(isCanUse=="true" || isCanUse==true){
    	    	    	window.open(_ctxPath + '/personalBind.do?method=retrievePassword');
    	    	     }else{
   	    	        	var msg = "${ctp:i18n('personalInfo.bind.cannotuse')}" ;
   	    				retrievePwdMsg = $.messageBox({
   	    					'id':"retrievePwdMessageBox",
   	    					'type': 0,
   	    					'msg': "<div class='msgbox_img_2' style='float:left'></div><div class='margin_t_5 margin_l_5' style='float:left'>" + msg + "</div>"
   	    				});
    	    	     }
    	        },
    	        error : function(XMLHttpRequest, textStatus, errorThrown) {
    	        	 alert($.i18n('login.tip.failure'));
    	        }
    	});
    }

    // 快速需求(Fast Demand):登陆页面验证码可点击刷新,并且连续点击10次以上,禁用一分钟
    function changeVerifyCodeImg(obj){
        if(times >= 10){
            alert("${ctp:i18n('login.label.VerifyCode.tip')}");
            setTimeout(function(){times=0;},60000);
            return;
        }
        times ++;
        //var img=document.getElementsByTagName("img")[obj];// 验证码的图片位置
        var img=document.getElementById("VerifyCodeImg");//不能根据html img标签来获取呀，登录页可以变动，当有img标签的时候获取的不对啊
        img.src="${path}/verifyCodeImage.jpg?" + new Date().getTime();
    }

    function doLoginSubmit(){
    	if(${hasPluginCA} && "iTrus"=="${caFactory}"){//
        	chooseCert();
        }else{
        	<c:if test="${loginFrom=='loginPortal'}">
		    	//ajax登录验证
		    	doAjaxLogin();
			</c:if>
			<c:if test="${loginFrom!='loginPortal'}">
				$("#submit_button").click();
			</c:if>
        }
    }

    //只有登录前门户用
    function doAjaxLogin(){
    	var loginFlag = onLoginSubmit();
    	if(!loginFlag){
    		return;
    	}
    	var formObj=$("#login_form").formobj();
    	formObj["loginFrom"] = "loginPortal";
    	formObj["login.timezone"] = $("#timezone").val();
    	formObj["login.smsVerifyCode"] = $("#smsVerifyCode").val();
    	formObj["login.VerifyCode"] = $("#VerifyCode").val();
    	formObj["province"] = $("#province").val();
    	formObj["city"] = $("#city").val();
    	formObj["rectangle"] = $("#rectangle").val();
    	$.ajax({
    		sync : true,
    		type: "POST",
    		url: "${path}/main.do?method=login",
    		data : formObj,
    		dataType : 'text',
    	      	success : function(data) {
    	      		if(data.indexOf("ok")==0){
    	      			window.top.location.href = _ctxPath + "/indexOpenWindow.jsp";
    	      		}else{
    	      			if(data.indexOf("ok")==0){
        	      			window.top.location.href = _ctxPath + "/indexOpenWindow.jsp";
        	      		}else{
        	      			if(data.indexOf("0;url=")>=0){
    							alert($.i18n('login.tip.failure'));
        	      				reloadLoginPage();
        	      			}else{
    							alert(data);
        	      				reloadLoginPage();
        	      			}
        	      		}
    	      		}
    	        },
    	        error : function(XMLHttpRequest, textStatus, errorThrown) {
    	        	alert($.i18n('login.tip.incorrect'));
      				reloadLoginPage();
    	        }
    	});
    }
	function reloadLoginPage(){
		window.top.location.href = _ctxPath + "/main.do?method=main&loginPortal=1&portalId=-7779029842361826066";
	}
    function loginButtonOnClickHandler() {
        var leaveTime = new Date().getTime();
        if(_SecuritySeed !== "") {
            //仅在开启登录加密时进行加密种子更新校验
            var SeedTimeOutInSeconds=60*30;//默认30分钟
            try{
                SeedTimeOutInSeconds=parseInt(_SecuritySeedTimeOut);
            }
            catch (e) {

            }
            if(SeedTimeOutInSeconds>60){
                //考虑网络原因提前一分钟到期校验
                SeedTimeOutInSeconds=SeedTimeOutInSeconds-60;
            }
            if(_SecuritySeedTimeOut!==""&&leaveTime - entryTime >SeedTimeOutInSeconds*1000) {
                UpdateLoginSeed();
            }
        }
    	var timeZoneId = getTimeZoneId();
   		$("#timezone").val(timeZoneId);

        var login_username = $("#login_username").val();
        if ($.trim(login_username) == "") {
        	doLoginSubmit();
        } else {
            var smsVerifyCode = "";
            if ($("#smsLoginInputDiv").length == 1) {
                smsVerifyCode = $.trim($("#smsVerifyCode").val());
            }
            var isCanUseSMS = ${isCanUseSMS};
            if (!isCanUseSMS || ($("#smsLoginInputDiv").length == 1 && smsVerifyCode != "")) {
            	doLoginSubmit();
            } else {
                //进行短信登录验证
                new portalManager().smsLoginEnabled(login_username, {
                    success: function(telNumber) {
                        if (telNumber && $.trim(telNumber).length > 0) {
                            if ($("#smsLoginInputDiv").length == 0) {
                                var smsHtml = "<div id='smsLoginInputDiv' class='clearfix'>";
                                if ($.browser.msie && $.browser.version < 9) {
                                    smsHtml += "<div class='smsLogin_textbox' style='background:url(/seeyon/main/login/default/images/white.png)'>";
                                }else{
                                    smsHtml += "<div class='smsLogin_textbox'>";
                                }
                                smsHtml += "<input title='${ctp:i18n("systemswitch.inputsmscode.prompt")}' id='smsVerifyCode' name='login.smsVerifyCode' type='text' maxlength='8' />";
                                smsHtml += "</div>";
                                smsHtml += "<div class='smsLogin_btn' id='sendSMSCodeButton'>${ctp:i18n("login.label.getsmscode")}</div>";
                                smsHtml += "</div>";
                                $(".captcha").after(smsHtml);
                                if(loginFromBol){
                                    $("#smsVerifyCode").focus(function(){
                                        $(this).parent().css("background-color","#FFF");
                                    }).blur(function(){
                                        if(_layout == "center"){
                                            $(this).parent().css("background-color","transparent");
                                        }else{
                                            $(this).parent().css("background-color","rgba(255, 255, 255, 0.7)");
                                        }
                                    });
                                }
                                $("#sendSMSCodeButton").click(function() {
                                    if (sendSMSCodeTime != 119) {
                                        return;
                                    }
                                    var login_username = $("#login_username").val();
                                    new portalManager().sendSMSLoginCode(login_username, {
                                        success: function(msg) {
                                            if (msg == "success") {
                                                $("#smsVerifyCode").val("");
                                                var interval = setInterval(function() {
                                                    sendSMSCodeTime--;
                                                    if (sendSMSCodeTime == 0) {
                                                        $("#sendSMSCodeButton").html("${ctp:i18n('login.label.getsmscode')}").removeClass('smsLogin_btn_disable');
                                                        sendSMSCodeTime = 119;
                                                        clearInterval(interval);
                                                    } else {
                                                        $("#sendSMSCodeButton").html(sendSMSCodeTime + " " + "${ctp:i18n('login.label.reget')}").addClass('smsLogin_btn_disable');
                                                    }
                                                }, 1000);
                                                $("#sendSMSCodeButton").html(sendSMSCodeTime + " " + "${ctp:i18n('login.label.reget')}").addClass('smsLogin_btn_disable');
                                            } else {
                                                $("#login_error").css("background-image", "none");
                                                $("#login_error").find("div").html(msg);
                                                $("#login_error").show();
                                            }
                                        }
                                    });
                                });
                                $("#smsLoginInputDiv").show();
                            } else {
                            	doLoginSubmit();
                            }
                        } else {
                            $("#smsVerifyCode").val("");
                            doLoginSubmit();
                        }
                    },
                    error: function() {
                    	doLoginSubmit();
                    }
                });
            }
        }
    }

    function getTimeZoneId(){
    	//获取客户端时区
    	var d = new Date();
    	var timezoneOffset = 0 - d.getTimezoneOffset();
    	var gmtHours = (timezoneOffset/60).toString();
    	//8  -8:30  8:45
    	var gmtHoursArr = gmtHours.split(".");
    	var h = gmtHoursArr[0];
    	if(h>=0){
    		h = "+"+h;
    	}
    	var m = "00";
    	if(gmtHoursArr.length>1){
    		m = Number("0."+gmtHoursArr[1]) * 60;
    	}
    	return  "GMT"+h+":"+m;
    }

    var commonProgressbar = null;

    //开始进度条
    function startProc(title) {
        try {
            var options = {
                title: title
            };
            if (title == undefined) {
                options = {};
            }
            if (commonProgressbar != null) {
                commonProgressbar.start();
            } else {
                commonProgressbar = new MxtProgressBar(options);
            }
        } catch (e) {}
    }

    //结束进度条
    function endProc() {
        try {
            if (commonProgressbar) {
                commonProgressbar.close();
            }
            commonProgressbar = null;
        } catch (e) {}
    }

    var openAssistantFlag = 0;
    function openAssistantSetup() {
        if(openAssistantFlag === 0) {
            openAssistantFlag = 1;
            setTimeout(function(){
                openAssistantFlag = 0;
            },30000);
        }else {
            getCtpTop().$.alert("${ctp:i18n('login.label.assistantTip')}");
            return;
        }
        var myhost= window.location.host;
        var myport= window.location.port;
        if(myport==""){
            myport= "80";
        }
        var ucUrl= "/"+myhost+"/"+myhost+"/"+myport;
        var obj = null;
        try {
            obj = new ActiveXObject("SeeyonActivexInstall.SeeyonInstall");
            var ele = document.getElementById("login_locale");
            var locale = ele != null ? ele.value : '';
            if( ele == null){
                locale = navigator.browserLanguage.replace(/([a-z]{2})(-)*([A-Z|a-z]{2})*/,function($0,$1,$2,$3){
                    var s = $1;
                    if( '-' == $2){
                        s += '_';
                        s += $3.toUpperCase();
                    }
                    return s;
                });
            }

            startProc("${ctp:i18n('vportal.login.initmsg')}");
            var result = obj.Startup(_ctxServer + "/autoinstall/${ctp:getSystemProperty('system.geniusFolder')}", locale + ucUrl, "${exceptPlugin}");
            endProc();
        } catch (e) {
            window.location.href=_ctxServer + '/autoinstall.do?method=regInstallDown64';
            /*
            getA8Top().alterWin = $.dialog({
                htmlId: 'alert',
                title: "${ctp:i18n('download.description.IESet.label')}",
                url: "${path}/genericController.do?ViewPage=apps/autoinstall/downLoadIESet",
                isClear: false,
                width: 420,
                height: 200
            });
            */
        }
    }
    function calSelecterHeight(){
    	var topValue = "${ctp:escapeJavascript(param.topValue)}";
        var languageSize = $("#login_locale_dropdown_content a").length;
        $("#login_locale_dropdown_content").css("overflowX","hidden");
        var languageTop = languageSize*30;
    	if(topValue =="21"){
    		var languageTopStr= "-"+languageTop+"px";
    		$("#login_locale_dropdown_content_iframe").css("top", languageTopStr).css("right", "0");
            $("#login_locale_dropdown_content").css("top",languageTopStr).css("right", "0");
    	}else{
    		var select_h = $("#login_area_div").offset().top +  $("#login_area_div").height() + languageTop;
            if (select_h >= $(window).height()) {
                $("#login_locale_dropdown_content_iframe").css("top", "-"+ $("#login_locale_dropdown_content").height() + "px").css("right", "0");
                $("#login_locale_dropdown_content").css("top","-"+ $("#login_locale_dropdown_content").height() +"px").css("right", "0");
            }else{
                 $("#login_locale_dropdown_content_iframe").css("top","25px").css("right", "0");
                $("#login_locale_dropdown_content").css("top","25px").css("right", "0");
            };
    	}
    }

    function checkPwdStrength(){
    	var login_validatePwdStrength =4;
        var login_password = $("#login_password").val();
        if ($.trim(login_password) == "") {
        }else{
        	login_validatePwdStrength = getPwdStrongForLoginPage(login_password);
        	$("#login_validatePwdStrength").val(login_validatePwdStrength);
        }
        return true;
    }
    //调整登录框的位置
    function adjustLoginPosition(){
        var window_h = $(window).height();
        var login_h = $("#login_area_div").height();
        var login_top = $("#login_area_div").offset().top;
        var login_left = $("#login_area_div").offset().left;

        if (_layout == "all" || _layout == "center" || _layout=="center_big") {//第一套和第四套布局只判断bottom和left
            if (login_left <= 0) {
                $("#login_area_div").css({
                    "left":"0px",
                    "right":"inherit"
                });
            }
            if (window_h - login_top < login_h) {
                if(loginFromBol){
                    $("#login_area_div").css({
                        "bottom":"40px",
                        "top":"inherit"
                    });
                }
            }
        }else if (_layout == "t_b") {//第二套布局只判断top
            if (login_top <= 0) {
                $("#login_area_div").css({
                    "top":"0px",
                    "bottom":"inherit"
                });
            }
        }else if (_layout == "l_r"){//第四套布局只判断left
             if (login_left <= 0) {
                $("#login_area_div").css({
                    "left":"0px",
                    "right":"inherit"
                });
            }
        }
    }
    /**
     * js控制登录页面手机盾二维码样式的显隐，和js文件的加载
     */
    function changeCssAndLoadJsByTrustdo() {
        <c:if test="${ctp:getSystemProperty('trustdo.isStart')=='true'}">
	        var qrcode4Div = document.getElementById("qrcode4");
	        qrcode4Div.style.display = "block";
	        //2.动态加载js文件,并执行脚本
	        getLoginQRCodeForMobileShield();
    	</c:if>
    }

    var pageNum;
    function initQRcodeArea() {
        pageNum = $("#QrcodeArea").find("ul").length;
        if(pageNum > 1) {
            $("#prevBtn,#nextBtn").show();
            var _tempStr = "";
            for (var i = 1; i < pageNum; i++) {
                _tempStr += "<span></span>";
            }
            $("#qrCodeGroupNum").append(_tempStr);
        }else{
            $("#qrCodeGroupNum").empty();
        }
    }

    //新改造的二维码功能
    var currentQRcodePage = 1;
    function showPrevQRPage() {
        if (currentQRcodePage <= 1) {
            return;
        }
        currentQRcodePage --;
        $("#QrcodeArea").find("ul").hide().eq(currentQRcodePage - 1).show();
        $("#qrCodeGroupNum").find("span").removeClass("current").eq(currentQRcodePage - 1).addClass("current");
    }
    function showNextQRPage() {
        if (currentQRcodePage >= pageNum) {
            return;
        }
        currentQRcodePage ++;
        $("#QrcodeArea").find("ul").hide().eq(currentQRcodePage - 1).show();
        $("#qrCodeGroupNum").find("span").removeClass("current").eq(currentQRcodePage - 1).addClass("current");
    }

    var topWin = getCtpTop();
    var isLoginPre = typeof(topWin.vPortal) === "object" && topWin.vPortal.portalType === "3" ? true : false;
    var winName = window.name;
    var winURL = window.location.href;
    var oldPostion = {
        "win": {},
        "loginPreBtnIframeDiv": {},
        "loginSectionDiv": {},
        "loginSectionParentDiv": {},
        "IframeSectionTempleteDiv": {}
    };
    var loginPreBtnIframeDiv,wrapperDiv,loginPreBtnIframe,loginBtnCloseDiv,QrMask4LoginPreDiv,loginSectionDiv,IframeSectionTempleteDiv,winH;
    var cssIsAppend = false;
    var isExportOldTemp;
    //显示二维码
    function showQrcodeArea() {
        //操作css
        if(isLoginPre && !cssIsAppend) {
            var newCss = topWin.document.createElement('link');
            newCss.type = 'text/css';
            newCss.rel = 'stylesheet';
            newCss.href = "/seeyon/main/login/default/css/login4PrePortal.css";
            var topWinHead = topWin.document.getElementsByTagName('head')[0];
            topWinHead.appendChild(newCss);
            cssIsAppend = true;
        }
        //7.1SP1改造二维码时修改了元素的ID并新增id:loginPreBtnIframeDiv，之前版本导出再导入的模板因ID不一样会报错，用isExportOldTemp来兼容一下，-_-!!!
        if (topWin.document.getElementById("loginPreBtnIframeDiv") == null) {  //7.1SP1之前导出的模板
            isExportOldTemp = true;
            loginPreBtnIframeDiv = topWin.document.querySelector(".loginPreBtnIframe");
            loginPreBtnIframe = topWin.document.getElementById("loginPreBtnIframeId");
        }else{
            loginPreBtnIframeDiv = topWin.document.getElementById("loginPreBtnIframeDiv");
            loginPreBtnIframe = topWin.document.getElementById("loginPreBtnIframe");
        }
        wrapperDiv = topWin.document.getElementById("wrapper");
        loginBtnCloseDiv = topWin.document.querySelector(".loginBtnClose");
        QrMask4LoginPreDiv = document.getElementById("QrMask4LoginPre");
        loginSectionDiv = topWin.document.querySelector(".iframeContentAreaBody");
        IframeSectionTempleteDiv = topWin.document.querySelector(".IframeSectionTempleteDiv");
        winW = $(topWin).width();
        winH = $(topWin).height();
        //登录前门户-右上角登录按钮，登录前门户-登录栏目，普通登录页，三种情况各自的逻辑处理
        if((winName === "loginPreBtnIframe" || winName === "") && winURL.indexOf("loginFrom=loginPortal") > -1){
            //登录前门户，右上角登录框弹出的iframe
            topWin.$("body").addClass("loginPreBtnIframePage");
            oldPostion["loginPreBtnIframeDiv"].top = loginPreBtnIframeDiv.style.top;
            oldPostion["loginPreBtnIframeDiv"].right = loginPreBtnIframeDiv.style.right;
            oldPostion["loginPreBtnIframeDiv"].width = loginPreBtnIframeDiv.style.width;
            oldPostion["loginPreBtnIframeDiv"].height = loginPreBtnIframeDiv.style.height;
            loginPreBtnIframeDiv.style.width = "100%";
            loginPreBtnIframeDiv.style.height = "100%";
            loginPreBtnIframe.style.width = "100%";
            loginPreBtnIframe.style.height = winH + "px";
            loginBtnCloseDiv.style.display = "none";
            QrMask4LoginPreDiv.style.display = "block";
            $("#QrcodeArea").show();
        }else if(window.name === "IframeSectionTemplete" && winURL.indexOf("loginFrom=loginPortal") > -1){
            //登录前门户，登录栏目
            oldPostion["loginSectionDiv"].height = loginSectionDiv.offsetHeight;
            oldPostion["loginSectionParentDiv"].height = loginSectionDiv.parentNode.offsetHeight;
            oldPostion["IframeSectionTempleteDiv"].height = IframeSectionTempleteDiv.offsetHeight;
            topWin.$("body").addClass("IframeSectionTemplete");
            $(loginSectionDiv).parents(".col").css("zIndex","100");
            topWin.document.documentElement.style.overflow = "hidden";
            loginSectionDiv.parentNode.style.height = winH + "px";
            loginSectionDiv.style.height = winH + "px";
            IframeSectionTempleteDiv.style.height = winH + "px";
            loginBtnCloseDiv.style.display = "none";
            QrMask4LoginPreDiv.style.display = "block";
            $("#QrcodeArea").show();
        }else{
            $("#QrcodeArea").fadeIn();
        }
        //均分显示
        var QrLi = $("#QrcodeArea").find(".qrCodeGroup ul:first").find("li:visible");
        var QrLen = QrLi.length;
        var eachQrW = Math.floor(100/QrLen);
        QrLi.each(function(){
            $(this).css("width",eachQrW + "%");
        });
        return false;
    }
    //隐藏二维码
    function hideQrcodeArea(_type) {
        var wrapperDiv = topWin.document.getElementById("wrapper");
        var loginBtnCloseDiv = topWin.document.querySelector(".loginBtnClose");
        var winH = topWin.innerWidth;
        if((winName === "loginPreBtnIframe" || winName === "") && winURL.indexOf("loginFrom=loginPortal") > -1){
            //登录前门户，右上角登录框弹出的iframe
            topWin.$("body").removeClass("loginPreBtnIframePage");
            loginPreBtnIframeDiv.style.top = oldPostion["loginPreBtnIframeDiv"].top;
            loginPreBtnIframeDiv.style.right = oldPostion["loginPreBtnIframeDiv"].right;
            loginPreBtnIframeDiv.style.width = oldPostion["loginPreBtnIframeDiv"].width;
            loginPreBtnIframeDiv.style.height = oldPostion["loginPreBtnIframeDiv"].height;
            loginPreBtnIframe.style.width = oldPostion["loginPreBtnIframeDiv"].width;
            loginPreBtnIframe.style.height = oldPostion["loginPreBtnIframeDiv"].height;
            loginBtnCloseDiv.style.display = "block";
            $("#QrcodeArea").hide();
        }else if(window.name === "IframeSectionTemplete" && winURL.indexOf("loginFrom=loginPortal") > -1){
            //登录前门户，登录栏目
            topWin.$("body").removeClass("IframeSectionTemplete");
            topWin.document.documentElement.style.overflow = "";
            loginSectionDiv.parentNode.style.height = oldPostion["loginSectionParentDiv"].height + "px";
            loginSectionDiv.style.height = oldPostion["loginSectionDiv"].height + "px";
            $(loginSectionDiv).parents(".col").css("zIndex","");
            IframeSectionTempleteDiv.style.height = oldPostion["IframeSectionTempleteDiv"].height + "px";
            loginBtnCloseDiv.style.display = "block";
            QrMask4LoginPreDiv.style.display = "none";
            $("#QrcodeArea").hide();
        }else{
            $("#QrcodeArea").fadeOut();
        }
        return false;
    }

</script>
</html>