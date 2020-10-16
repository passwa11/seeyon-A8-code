package com.seeyon.v3x.edoc.manager;

import java.util.Date;
import java.util.List;

import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;

/**
 * 绩效管理接口
 * @author 杨帆
 *
 */
public interface EdocWorkManageManager {
	
	/**
	 * 绩效管理--显示公文列表
	 * @param memberId  查询人员id，如果传递为空，则查询当前登录人员id
	 * @param type 0:指定期限待办；1:指定期限暂存待办；2:本日已办；3:本日已发；4:本周已办； 5:本周已发；6:本月已办；7:本月已发；8:指定期限的已办；9:指定期限的已发；10:归档的
	 * @param beginDate 指定期限的开始时间
	 * @param endDate   指定期限的结束时间
	 * @param coverTime  0：未超期；1:超期；全部时不传值
	 * @param processType 流程分类： 自由流程，模板流程
	 * @param isPage 是否分页，打印不分页
	 * @return
	 * @throws EdocException
	 */
	public List<EdocSummaryModel> queryEdocList(Long memberId, int type, Date beginDate, Date endDate,int coverTime, String processType, boolean isPage) throws EdocException;

}
