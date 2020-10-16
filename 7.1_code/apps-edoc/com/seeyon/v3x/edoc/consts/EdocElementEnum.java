/**
 * Author : xuqw
 *   Date : 2017年4月21日 下午2:51:04
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.v3x.edoc.consts;

import com.seeyon.v3x.edoc.domain.EdocElement;

/**
 * <p>Title       : 应用模块名称</p>
 * <p>Description : 代码描述</p>
 * <p>Copyright   : Copyright (c) 2012</p>
 * <p>Company     : seeyon.com</p>
 * <p>@Since A8-V5 6.1</p>
 */
public enum EdocElementEnum {

    subject(1L, "001", "edoc.element.subject", "subject", 0, null, 1, 1, 0L, ""),
    doc_type(2L, "002", "edoc.element.doctype", "doc_type", 5, 401L, 1, 1, 0L, ""),
    send_type(3L, "003", "edoc.element.sendtype", "send_type", 5, 402L, 1, 1, 0L, ""),
    doc_mark(4L, "004", "edoc.element.wordno.label", "doc_mark", 0, null, 1, 1, 0L, ""),
    serial_no(5L, "005", "edoc.element.wordinno.label", "serial_no", 0, null, 1, 1, 0L, ""),
    secret_level(6L, "006", "edoc.element.secretlevel", "secret_level", 5, 403L, 1, 1, 0L, ""),
    urgent_level(7L, "007", "edoc.element.urgentlevel", "urgent_level", 5, 404L, 1, 1, 0L, ""),
    keep_period(8L, "008", "edoc.element.keepperiod", "keep_period", 5, 405L, 1, 1, 0L, ""),
    create_person(9L, "009", "edoc.element.author", "create_person", 0, null, 1, 1, 0L, ""),
    send_unit(10L, "010", "edoc.element.sendunit", "send_unit", 0, null, 1, 1, 0L, ""),
    issuer(11L, "011", "edoc.element.issuer", "issuer", 0, null, 1, 1, 0L, ""),
    signing_date(12L, "012", "edoc.element.sendingdate", "signing_date", 4, null, 1, 1, 0L, ""),
    send_to(13L, "013", "edoc.element.sendtounit", "send_to", 0, null, 1, 1, 0L, ""),
    copy_to(14L, "014", "edoc.element.copytounit", "copy_to", 0, null, 1, 1, 0L, ""),
    report_to(15L, "015", "edoc.element.copy.reportunit", "report_to", 0, null, 1, 1, 0L, ""),
    keyword(16L, "016", "edoc.element.keyword", "keyword", 0, null, 1, 1, 0L, ""),
    print_unit(17L, "017", "edoc.element.printedunit", "print_unit", 0, null, 1, 1, 0L, ""),
    copies(18L, "018", "edoc.element.copies", "copies", 2, null, 1, 1, 0L, ""),
    printer(19L, "019", "edoc.element.printer", "printer", 0, null, 1, 1, 0L, ""),
    doc_mark2(21L, "021", "edoc.element.wordno2.label", "doc_mark2", 0, null, 1, 0, 0L, ""),
    copies2(22L, "022", "edoc.element.copies2", "copies2", 2, null, 1, 0, 0L, ""),
    send_to2(23L, "023", "edoc.element.sendtounit2", "send_to2", 0, null, 1, 0, 0L, ""),
    copy_to2(24L, "024", "edoc.element.copytounit2", "copy_to2", 0, null, 1, 0, 0L, ""),
    report_to2(25L, "025", "edoc.element.copy.reportunit2", "report_to2", 0, null, 1, 0, 0L, ""),
    send_unit2(26L, "026", "edoc.element.sendunit2", "send_unit2", 0, null, 1, 0, 0L, ""),
    filesm(320L, "320", "edoc.element.filesm", "filesm", 0, null, 1, 1, 0L, ""),
    filefz(321L, "321", "edoc.element.filefz", "filefz", 0, null, 1, 1, 0L, ""),
    string1(51L, "051", "单行文本1", "string1", 0, null, 0, 0, 0L, ""),
    string2(52L, "052", "单行文本2", "string2", 0, null, 0, 0, 0L, ""),
    string3(53L, "053", "单行文本3", "string3", 0, null, 0, 0, 0L, ""),
    string4(54L, "054", "单行文本4", "string4", 0, null, 0, 0, 0L, ""),
    string5(55L, "055", "单行文本5", "string5", 0, null, 0, 0, 0L, ""),
    string6(56L, "056", "单行文本6", "string6", 0, null, 0, 0, 0L, ""),
    string7(57L, "057", "单行文本7", "string7", 0, null, 0, 0, 0L, ""),
    string8(58L, "058", "单行文本8", "string8", 0, null, 0, 0, 0L, ""),
    string9(59L, "059", "单行文本9", "string9", 0, null, 0, 0, 0L, ""),
    string10(60L, "060", "单行文本10", "string10", 0, null, 0, 0, 0L, ""),
    text1(61L, "061", "多行文本1", "text1", 1, null, 0, 0, 0L, ""),
    text2(62L, "062", "多行文本2", "text2", 1, null, 0, 0, 0L, ""),
    text3(63L, "063", "多行文本3", "text3", 1, null, 0, 0, 0L, ""),
    text4(64L, "064", "多行文本4", "text4", 1, null, 0, 0, 0L, ""),
    text5(65L, "065", "多行文本5", "text5", 1, null, 0, 0, 0L, ""),
    text6(66L, "066", "多行文本6", "text6", 1, null, 0, 0, 0L, ""),
    text7(67L, "067", "多行文本7", "text7", 1, null, 0, 0, 0L, ""),
    text8(68L, "068", "多行文本8", "text8", 1, null, 0, 0, 0L, ""),
    text9(69L, "069", "多行文本9", "text9", 1, null, 0, 0, 0L, ""),
    text10(70L, "070", "多行文本10", "text10", 1, null, 0, 0, 0L, ""),
    integer1(71L, "071", "整数类型1", "integer1", 2, null, 0, 0, 0L, ""),
    integer2(72L, "072", "整数类型2", "integer2", 2, null, 0, 0, 0L, ""),
    integer3(73L, "073", "整数类型3", "integer3", 2, null, 0, 0, 0L, ""),
    integer4(74L, "074", "整数类型4", "integer4", 2, null, 0, 0, 0L, ""),
    integer5(75L, "075", "整数类型5", "integer5", 2, null, 0, 0, 0L, ""),
    integer6(76L, "076", "整数类型6", "integer6", 2, null, 0, 0, 0L, ""),
    integer7(77L, "077", "整数类型7", "integer7", 2, null, 0, 0, 0L, ""),
    integer8(78L, "078", "整数类型8", "integer8", 2, null, 0, 0, 0L, ""),
    integer9(79L, "079", "整数类型9", "integer9", 2, null, 0, 0, 0L, ""),
    integer10(80L, "080", "整数类型10", "integer10", 2, null, 0, 0, 0L, ""),
    decimal1(81L, "081", "小数类型1", "decimal1", 3, null, 0, 0, 0L, ""),
    decimal2(82L, "082", "小数类型2", "decimal2", 3, null, 0, 0, 0L, ""),
    decimal3(83L, "083", "小数类型3", "decimal3", 3, null, 0, 0, 0L, ""),
    decimal4(84L, "084", "小数类型4", "decimal4", 3, null, 0, 0, 0L, ""),
    decimal5(85L, "085", "小数类型5", "decimal5", 3, null, 0, 0, 0L, ""),
    decimal6(86L, "086", "小数类型6", "decimal6", 3, null, 0, 0, 0L, ""),
    decimal7(87L, "087", "小数类型7", "decimal7", 3, null, 0, 0, 0L, ""),
    decimal8(88L, "088", "小数类型8", "decimal8", 3, null, 0, 0, 0L, ""),
    decimal9(89L, "089", "小数类型9", "decimal9", 3, null, 0, 0, 0L, ""),
    decimal10(90L, "090", "小数类型10", "decimal10", 3, null, 0, 0, 0L, ""),
    date1(91L, "091", "日期类型1", "date1", 4, null, 0, 0, 0L, ""),
    date2(92L, "092", "日期类型2", "date2", 4, null, 0, 0, 0L, ""),
    date3(93L, "093", "日期类型3", "date3", 4, null, 0, 0, 0L, ""),
    date4(94L, "094", "日期类型4", "date4", 4, null, 0, 0, 0L, ""),
    date5(95L, "095", "日期类型5", "date5", 4, null, 0, 0, 0L, ""),
    date6(96L, "096", "日期类型6", "date6", 4, null, 0, 0, 0L, ""),
    date7(97L, "097", "日期类型7", "date7", 4, null, 0, 0, 0L, ""),
    date8(98L, "098", "日期类型8", "date8", 4, null, 0, 0, 0L, ""),
    date9(99L, "099", "日期类型9", "date9", 4, null, 0, 0, 0L, ""),
    date10(100L, "100", "日期类型10", "date10", 4, null, 0, 0, 0L, ""),
    list1(101L, "101", "列表类型1", "list1", 5, null, 0, 0, 0L, ""),
    list2(102L, "102", "列表类型2", "list2", 5, null, 0, 0, 0L, ""),
    list3(103L, "103", "列表类型3", "list3", 5, null, 0, 0, 0L, ""),
    list4(104L, "104", "列表类型4", "list4", 5, null, 0, 0, 0L, ""),
    list5(105L, "105", "列表类型5", "list5", 5, null, 0, 0, 0L, ""),
    list6(106L, "106", "列表类型6", "list6", 5, null, 0, 0, 0L, ""),
    list7(107L, "107", "列表类型7", "list7", 5, null, 0, 0, 0L, ""),
    list8(108L, "108", "列表类型8", "list8", 5, null, 0, 0, 0L, ""),
    list9(109L, "109", "列表类型9", "list9", 5, null, 0, 0, 0L, ""),
    list10(110L, "110", "列表类型10", "list10", 5, null, 0, 0, 0L, ""),
    createdate(201L, "201", "edoc.element.createdate", "createdate", 4, null, 1, 1, 0L, ""),
    packdate(202L, "202", "edoc.element.packdate", "packdate", 4, null, 1, 1, 0L, ""),
    niwen(203L, "203", "edoc.element.niwen", "niwen", 6, null, 1, 1, 0L, ""),
    shenpi(204L, "204", "edoc.element.shenpi", "shenpi", 6, null, 1, 1, 0L, ""),
    shenhe(205L, "205", "edoc.element.shenhe", "shenhe", 6, null, 1, 1, 0L, ""),
    fuhe(206L, "206", "edoc.element.fuhe", "fuhe", 6, null, 1, 1, 0L, ""),
    fengfa(207L, "207", "edoc.element.fengfa", "fengfa", 6, null, 1, 1, 0L, ""),
    huiqian(208L, "208", "edoc.element.huiqian", "huiqian", 6, null, 1, 1, 0L, ""),
    qianfa(209L, "209", "edoc.element.qianfa", "qianfa", 6, null, 1, 1, 0L, ""),
    zhihui(210L, "210", "edoc.element.zhihui", "zhihui", 6, null, 1, 1, 0L, ""),
    yuedu(211L, "211", "edoc.element.yuedu", "yuedu", 6, null, 1, 1, 0L, ""),
    banli(212L, "212", "edoc.element.banli", "banli", 6, null, 1, 1, 0L, ""),
    dengji(213L, "213", "edoc.element.dengji", "dengji", 6, null, 1, 1, 0L, ""),
    niban(214L, "214", "edoc.element.niban", "niban", 6, null, 1, 1, 0L, ""),
    pishi(215L, "215", "edoc.element.pishi", "pishi", 6, null, 1, 1, 0L, ""),
    chengban(216L, "216", "edoc.element.chengban", "chengban", 6, null, 1, 1, 0L, ""),
    logoimg(217L, "217", "edoc.element.logoimg", "logoimg", 7, null, 1, 1, 0L, ""),
    otherOpinion(218L, "218", "edoc.element.otherOpinion", "otherOpinion", 6, null, 1, 1, 0L, ""),
    wenshuguanli(219L, "219", "edoc.element.wenshuguanli", "wenshuguanli", 6, null, 1, 1, 0L, ""),
    integer11(231L, "231", "整数类型11", "integer11", 2, null, 0, 0, 0L, ""),
    integer12(232L, "232", "整数类型12", "integer12", 2, null, 0, 0, 0L, ""),
    integer13(233L, "233", "整数类型13", "integer13", 2, null, 0, 0, 0L, ""),
    integer14(234L, "234", "整数类型14", "integer14", 2, null, 0, 0, 0L, ""),
    integer15(235L, "235", "整数类型15", "integer15", 2, null, 0, 0, 0L, ""),
    integer16(236L, "236", "整数类型16", "integer16", 2, null, 0, 0, 0L, ""),
    integer17(237L, "237", "整数类型17", "integer17", 2, null, 0, 0, 0L, ""),
    integer18(238L, "238", "整数类型18", "integer18", 2, null, 0, 0, 0L, ""),
    integer19(239L, "239", "整数类型19", "integer19", 2, null, 0, 0, 0L, ""),
    integer20(240L, "240", "整数类型20", "integer20", 2, null, 0, 0, 0L, ""),
    string11(241L, "241", "单行文本11", "string11", 0, null, 0, 0, 0L, ""),
    string12(242L, "242", "单行文本12", "string12", 0, null, 0, 0, 0L, ""),
    string13(243L, "243", "单行文本13", "string13", 0, null, 0, 0, 0L, ""),
    string14(244L, "244", "单行文本14", "string14", 0, null, 0, 0, 0L, ""),
    string15(245L, "245", "单行文本15", "string15", 0, null, 0, 0, 0L, ""),
    string16(246L, "246", "单行文本16", "string16", 0, null, 0, 0, 0L, ""),
    string17(247L, "247", "单行文本17", "string17", 0, null, 0, 0, 0L, ""),
    string18(248L, "248", "单行文本18", "string18", 0, null, 0, 0, 0L, ""),
    string19(249L, "249", "单行文本19", "string19", 0, null, 0, 0, 0L, ""),
    string20(250L, "250", "单行文本20", "string20", 0, null, 0, 0, 0L, ""),
    decimal11(251L, "251", "小数类型11", "decimal11", 3, null, 0, 0, 0L, ""),
    decimal12(252L, "252", "小数类型12", "decimal12", 3, null, 0, 0, 0L, ""),
    decimal13(253L, "253", "小数类型13", "decimal13", 3, null, 0, 0, 0L, ""),
    decimal14(254L, "254", "小数类型14", "decimal14", 3, null, 0, 0, 0L, ""),
    decimal15(255L, "255", "小数类型15", "decimal15", 3, null, 0, 0, 0L, ""),
    decimal16(256L, "256", "小数类型16", "decimal16", 3, null, 0, 0, 0L, ""),
    decimal17(257L, "257", "小数类型17", "decimal17", 3, null, 0, 0, 0L, ""),
    decimal18(258L, "258", "小数类型18", "decimal18", 3, null, 0, 0, 0L, ""),
    decimal19(259L, "259", "小数类型19", "decimal19", 3, null, 0, 0, 0L, ""),
    decimal20(260L, "260", "小数类型20", "decimal20", 3, null, 0, 0, 0L, ""),
    list11(261L, "261", "列表类型11", "list11", 5, null, 0, 0, 0L, ""),
    list12(262L, "262", "列表类型12", "list12", 5, null, 0, 0, 0L, ""),
    list13(263L, "263", "列表类型13", "list13", 5, null, 0, 0, 0L, ""),
    list14(264L, "264", "列表类型14", "list14", 5, null, 0, 0, 0L, ""),
    list15(265L, "265", "列表类型15", "list15", 5, null, 0, 0, 0L, ""),
    list16(266L, "266", "列表类型16", "list16", 5, null, 0, 0, 0L, ""),
    list17(267L, "267", "列表类型17", "list17", 5, null, 0, 0, 0L, ""),
    list18(268L, "268", "列表类型18", "list18", 5, null, 0, 0, 0L, ""),
    list19(269L, "269", "列表类型19", "list19", 5, null, 0, 0, 0L, ""),
    list20(270L, "270", "列表类型20", "list20", 5, null, 0, 0, 0L, ""),
    date11(271L, "271", "日期类型11", "date11", 4, null, 0, 0, 0L, ""),
    date12(272L, "272", "日期类型12", "date12", 4, null, 0, 0, 0L, ""),
    date13(273L, "273", "日期类型13", "date13", 4, null, 0, 0, 0L, ""),
    date14(274L, "274", "日期类型14", "date14", 4, null, 0, 0, 0L, ""),
    date15(275L, "275", "日期类型15", "date15", 4, null, 0, 0, 0L, ""),
    date16(276L, "276", "日期类型16", "date16", 4, null, 0, 0, 0L, ""),
    date17(277L, "277", "日期类型17", "date17", 4, null, 0, 0, 0L, ""),
    date18(278L, "278", "日期类型18", "date18", 4, null, 0, 0, 0L, ""),
    date19(279L, "279", "日期类型19", "date19", 4, null, 0, 0, 0L, ""),
    date20(280L, "280", "日期类型20", "date20", 4, null, 0, 0, 0L, ""),
    opinion1(281L, "281", "自定义意见1", "opinion1", 6, null, 0, 0, 0L, ""),
    opinion2(282L, "282", "自定义意见2", "opinion2", 6, null, 0, 0, 0L, ""),
    opinion3(283L, "283", "自定义意见3", "opinion3", 6, null, 0, 0, 0L, ""),
    opinion4(284L, "284", "自定义意见4", "opinion4", 6, null, 0, 0, 0L, ""),
    opinion5(285L, "285", "自定义意见5", "opinion5", 6, null, 0, 0, 0L, ""),
    opinion6(286L, "286", "自定义意见6", "opinion6", 6, null, 0, 0, 0L, ""),
    opinion7(287L, "287", "自定义意见7", "opinion7", 6, null, 0, 0, 0L, ""),
    opinion8(288L, "288", "自定义意见8", "opinion8", 6, null, 0, 0, 0L, ""),
    opinion9(289L, "289", "自定义意见9", "opinion9", 6, null, 0, 0, 0L, ""),
    opinion10(290L, "290", "自定义意见10", "opinion10", 6, null, 0, 0, 0L, ""),
    string21(291L, "291", "单行文本21", "string21", 0, null, 0, 0, 0L, ""),
    string22(292L, "292", "单行文本22", "string22", 0, null, 0, 0, 0L, ""),
    string23(293L, "293", "单行文本23", "string23", 0, null, 0, 0, 0L, ""),
    string24(294L, "294", "单行文本24", "string24", 0, null, 0, 0, 0L, ""),
    string25(295L, "295", "单行文本25", "string25", 0, null, 0, 0, 0L, ""),
    string26(296L, "296", "单行文本26", "string26", 0, null, 0, 0, 0L, ""),
    string27(297L, "297", "单行文本27", "string27", 0, null, 0, 0, 0L, ""),
    string28(298L, "298", "单行文本28", "string28", 0, null, 0, 0, 0L, ""),
    string29(299L, "299", "单行文本29", "string29", 0, null, 0, 0, 0L, ""),
    string30(300L, "300", "单行文本30", "string30", 0, null, 0, 0, 0L, ""),
    text11(301L, "301", "多行文本11", "text11", 1, null, 0, 0, 0L, ""),
    text12(302L, "302", "多行文本12", "text12", 1, null, 0, 0, 0L, ""),
    text13(303L, "303", "多行文本13", "text13", 1, null, 0, 0, 0L, ""),
    text14(304L, "304", "多行文本14", "text14", 1, null, 0, 0, 0L, ""),
    text15(305L, "305", "多行文本15", "text15", 1, null, 0, 0, 0L, ""),
    opinion11(306L, "306", "自定义意见11", "opinion11", 6, null, 0, 0, 0L, ""),
    opinion12(307L, "307", "自定义意见12", "opinion12", 6, null, 0, 0, 0L, ""),
    opinion13(308L, "308", "自定义意见13", "opinion13", 6, null, 0, 0, 0L, ""),
    opinion14(309L, "309", "自定义意见14", "opinion14", 6, null, 0, 0, 0L, ""),
    opinion15(310L, "310", "自定义意见15", "opinion15", 6, null, 0, 0, 0L, ""),
    attachments(311L, "311", "edoc.element.attachments", "attachments", 1, null, 1, 1, 0L, ""),
    send_department(312L, "312", "edoc.element.senddepartment", "send_department", 0, null, 1, 1, 0L, ""),
    send_department2(313L, "313", "edoc.element.senddepartment2", "send_department2", 0, null, 1, 0, 0L, ""),
    opinion16(334L, "334", "自定义意见16", "opinion16", 6, null, 0, 0, 0L, ""),
    opinion17(335L, "335", "自定义意见17", "opinion17", 6, null, 0, 0, 0L, ""),
    opinion18(336L, "336", "自定义意见18", "opinion18", 6, null, 0, 0, 0L, ""),
    opinion19(337L, "337", "自定义意见19", "opinion19", 6, null, 0, 0, 0L, ""),
    opinion20(338L, "338", "自定义意见20", "opinion20", 6, null, 0, 0, 0L, ""),
    opinion21(339L, "339", "自定义意见21", "opinion21", 6, null, 0, 0, 0L, ""),
    opinion22(340L, "340", "自定义意见22", "opinion22", 6, null, 0, 0, 0L, ""),
    opinion23(341L, "341", "自定义意见23", "opinion23", 6, null, 0, 0, 0L, ""),
    opinion24(342L, "342", "自定义意见24", "opinion24", 6, null, 0, 0, 0L, ""),
    opinion25(343L, "343", "自定义意见25", "opinion25", 6, null, 0, 0, 0L, ""),
    opinion26(344L, "344", "自定义意见26", "opinion26", 6, null, 0, 0, 0L, ""),
    opinion27(345L, "345", "自定义意见27", "opinion27", 6, null, 0, 0, 0L, ""),
    opinion28(346L, "346", "自定义意见28", "opinion28", 6, null, 0, 0, 0L, ""),
    opinion29(347L, "347", "自定义意见29", "opinion29", 6, null, 0, 0, 0L, ""),
    opinion30(348L, "348", "自定义意见30", "opinion30", 6, null, 0, 0, 0L, ""),
    report(225L, "225", "sup.unit.opinion", "report", 6, null, 1, 1, 0L, ""),
    feedback(226L, "226", "sub.unit.feedback", "feedback", 6, null, 1, 1, 0L, ""),
    phone(322L, "322", "edoc.element.phone", "phone", 0, null, 1, 1, 0L, ""),
    receipt_date(329L, "329", "edoc.element.receipt_date", "receipt_date", 4, null, 1, 1, 0L, ""),
    registration_date(330L, "330", "edoc.element.registration_date", "registration_date", 4, null, 1, 1, 0L, ""),
    auditor(331L, "331", "edoc.element.auditor", "auditor", 0, null, 1, 1, 0L, ""),
    review(332L, "332", "edoc.element.review", "review", 0, null, 1, 1, 0L, ""),
    undertaker(333L, "333", "edoc.element.undertaker", "undertaker", 0, null, 1, 1, 0L, ""),
    undertakenoffice(349L, "349", "edoc.element.undertakeUnit", "undertakenoffice", 0, null, 1, 1, 0L, ""),
    unit_level(350L, "350", "edoc.element.unitLevel", "unit_level", 5, 406L, 1, 1, 0L, "");
    
    private Long id;
    private String elementId;
    private String name;
    private String fieldName;
    private int type;
    private Long metadataId;
    private int isSystem;
    private int status;
    private Long domainId;
    private String inputMode;
    
    
    private EdocElementEnum(Long id,
        String elementId,
        String name,
        String fieldName,
        int type,
        Long metadataId,
        int isSystem,
        int status,
        Long domainId,
        String inputMode) {
        
        this.id = id;
        this.elementId = elementId;
        this.name = name;
        this.fieldName = fieldName;
        this.type = type;
        this.metadataId = metadataId;
        this.isSystem = isSystem;
        this.status = status;
        this.domainId = domainId;
        this.inputMode = inputMode;
    }
    
    
    /**
     * 
     * 获取元素名称
     * 
     * @return
     *
     * @Since A8-V5 6.1
     * @Author      : xuqw
     * @Date        : 2017年4月21日下午3:37:09
     *
     */
    public static int size(){
        return EdocElementEnum.values().length;
    }
    
    public EdocElement trans2Element(){
        EdocElement ret = new EdocElement();
        ret.setId(id);
        ret.setDomainId(domainId);
        ret.setElementId(this.elementId);
        ret.setFieldName(this.fieldName);
        ret.setInputMode(this.inputMode);
        ret.setIsSystem(this.isSystem == 1);
        ret.setMetadataId(this.metadataId);
        ret.setName(this.name);
        ret.setStatus(this.status);
        ret.setType(this.type);
        return ret;
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getElementId() {
        return elementId;
    }
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public Long getMetadataId() {
        return metadataId;
    }
    public void setMetadataId(Long metadataId) {
        this.metadataId = metadataId;
    }
    public int getIsSystem() {
        return isSystem;
    }
    public void setIsSystem(int isSystem) {
        this.isSystem = isSystem;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public Long getDomainId() {
        return domainId;
    }
    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }
    public String getInputMode() {
        return inputMode;
    }
    public void setInputMode(String inputMode) {
        this.inputMode = inputMode;
    }
}
