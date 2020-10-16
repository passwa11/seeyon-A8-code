package com.seeyon.apps.meetingSeat.dao;

import java.util.List;
import java.util.Map;

import org.apache.geode.internal.cache.FilterProfile.interestType;

import com.seeyon.apps.meetingSeat.po.Formson0256;
import com.seeyon.apps.meetingSeat.util.JDBCUtil;
import com.seeyon.ctp.util.DBAgent;

public class MeetingSeatDaoImpl implements MeetingSeatDao {

	@Override
	public List<Map> selectIdByhybh(String hybh) {

		// 正式环境
		String sql1 = "select t.formmain_id, t.field0034,t.field0035,t.field0025,t.field0026 from formson_0085 t inner join  formmain_0084 f on t.formmain_id=f.id where f.field0030 = '"
				+ hybh + "'and t.field0034 is not null and f.field0038 is null";

		// 测试环境
		String sql = "select t.formmain_id, t.field0034,t.field0035,t.field0025,t.field0026 from formson_0331 t inner join  formmain_0330 f on t.formmain_id=f.id where f.field0030 = '"
				+ hybh + "'and t.field0034 is not null and f.field0038 is null";

		List<Map> list = JDBCUtil.doQuery(sql);
		return list;
	}

	@Override
	public List<Map> selectDepNoList(String meetingId) {

		// 正式环境
		String sql1 = "select field0046,id from formmain_0084 where field0030='" + meetingId
				+ "' and field0018 = '5791838574438402388' and (field0046 not in ('集团党政办','北京办事处','南京办事处','集团督查科','集团秘书科','集团信访科','集团事务科','集团档案科','信息化科','集团领导','集团副总师','集团总助','集团党政办主任','副管理者岗位') and field0046 not like '%集团总助%' )and field0038 is null";
 
		// 测试环境
		String sql = "select field0040,id from formmain_0330 where field0030='" + meetingId
				+ "' and field0018 = '8768734248003453394' and field0040 not in ('集团党政办','北京办事处','南京办事处','集团督查科','集团秘书科','集团信访科','集团事务科','集团档案科','信息化科','集团领导','集团副总师','集团总助','集团党政办主任','副管理者岗位') and field0038 is null";

		List<Map> list = JDBCUtil.doQuery(sql);
		return list;
	}

	@Override
	public int updateMeetingSeatPerson(String meetingId, String name, String dep, String col, String row) {
		// 正式环境
		String sql1 = "update formson_0085 t set t.field0025='" + row + "',t.field0026='" + col
				+ "'where FORMMAIN_ID IN ( SELECT ID FROM formmain_0084 WHERE field0030 = '" + meetingId
				+ "') AND t.field0034 IS NOT NULL AND T.field0034='" + name + "' AND T.FIELD0035='" + dep + "'";
		// 测试环境
		String sql = "update formson_0331 t set t.field0025='" + row + "',t.field0026='" + col
				+ "'where FORMMAIN_ID IN ( SELECT ID FROM formmain_0330 WHERE field0030 = '" + meetingId
				+ "') AND t.field0034 IS NOT NULL AND T.field0034='" + name + "' AND T.FIELD0035='" + dep + "'";

		int result = JDBCUtil.doUpdateOrInsert(sql);
		return result;
	}

	@Override
	public int updateMeetingSeatDepStatus(String meetingId, String name) {

		// 正式环境
		String sql1 = "update formmain_0084 t set t.field0018='-8392862459988524299' where field0030 = '" + meetingId
				+ "' AND t.field0046 ='" + name + "'";

		// 测试环境
		String sql = "update FORMMAIN_0330 t set t.field0018='-5420469062522421038' where field0030 = '" + meetingId
				+ "' AND t.field0040 ='" + name + "'";

		int result = JDBCUtil.doUpdateOrInsert(sql);
		return result;
	}

	@Override
	public void insertMeetingSeatDep(List<Formson0256> formson0256) {
		DBAgent.saveAll(formson0256);
	}

}
