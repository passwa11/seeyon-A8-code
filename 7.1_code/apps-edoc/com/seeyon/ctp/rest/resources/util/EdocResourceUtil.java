package com.seeyon.ctp.rest.resources.util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;

public class EdocResourceUtil {
	private static Log LOGGER = CtpLogFactory.getLog(EdocResourceUtil.class);
	private final static EdocResourceUtil INSTANCE = new EdocResourceUtil();
	private final static String DBTYPE = getDbType();

	public final static EdocResourceUtil getInstance() {
		return INSTANCE;
	}

	private static final List<String> mysqlAndSqlServerOrderListForTemplate = new ArrayList<String>() {
		{
			add("delete cpl from ctp_process_log cpl, edoc_summary es where cpl.process_id = es.process_id and es.templete_id = ?");
			add("delete ess from edoc_supervisor ess, edoc_supervise_detail esd, edoc_summary es where ess.supervise_id = esd.id and esd.edoc_id = es.id and es.templete_id = ?");
			add("delete esl from edoc_supervise_log esl, edoc_supervise_detail esd, edoc_summary es where esl.supervise_id = esd.id and esd.edoc_id = es.id and es.templete_id = ?");
			add("delete esr from edoc_supervise_remind esr, edoc_supervise_detail esd, edoc_summary es where esr.supervise_id = esd.id and esd.edoc_id = es.id and es.templete_id = ?");
			add("delete dl from doc_learning dl, doc_resources dr, ctp_affair ca, edoc_summary es where dl.doc_resource_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?");
			add("delete df from doc_favorites df, doc_resources dr, edoc_summary es where df.doc_resource_id = dr.id and dr.source_id = es.id and es.templete_id = ?");
			add("delete df from doc_favorites df, doc_resources dr, ctp_affair ca, edoc_summary es where df.doc_resource_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?");
			add("delete cuhm from ctp_user_history_message cuhm, doc_resources dr, ctp_affair ca, edoc_summary es where cuhm.reference_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?");
			add("delete drm from doc_resources drm, doc_resources dr, ctp_affair ca, edoc_summary es where drm.source_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?");
			add("delete cum from ctp_user_message cum, doc_resources dr, ctp_affair ca, edoc_summary es where cum.reference_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?");
			add("delete cuhm from ctp_user_history_message cuhm, doc_resources dr, edoc_summary es where cuhm.reference_id = dr.id and dr.source_id = es.id and es.templete_id = ?");
			add("delete cum from ctp_user_message cum, doc_resources dr, edoc_summary es where cum.reference_id = dr.id and dr.source_id = es.id and es.templete_id = ?");
			add("delete dl from doc_learning dl, doc_resources dr, edoc_summary es where dl.doc_resource_id = dr.id and dr.source_id = es.id and es.templete_id = ?");
			add("delete drm from doc_resources drm, doc_resources dr, edoc_summary es where drm.source_id = dr.id and dr.source_id = es.id and es.templete_id = ?");
			add("delete csr from ctp_supervise_receiver csr, ctp_supervise_log csl, ctp_supervise_detail csd, edoc_summary es where csr.log_id = csl.id and csl.supervise_id = csd.id and csd.entity_id = es.id and es.templete_id = ?");
			add("delete css from ctp_supervisor css, ctp_supervise_detail csd, edoc_summary es where css.supervise_id = csd.id and csd.entity_id = es.id and es.templete_id = ?");
			add("delete csl from ctp_supervise_log csl, ctp_supervise_detail csd, edoc_summary es where csl.supervise_id = csd.id and csd.entity_id = es.id and es.templete_id = ?");
			add("delete cuhm from ctp_user_history_message cuhm, ctp_supervise_detail csd, edoc_summary es where cuhm.reference_id = csd.id and csd.entity_id = es.id and es.templete_id = ?");
			add("delete cum from ctp_user_message cum, ctp_supervise_detail csd, edoc_summary es where cum.reference_id = csd.id and csd.entity_id = es.id and es.templete_id = ?");
			add("delete wwr from wf_workitem_run wwr, edoc_summary es, ctp_affair ca where wwr.id = ca.sub_object_id and ca.object_id = es.id and es.templete_id = ?");
			add("delete wcr from wf_case_run wcr, edoc_summary es where wcr.id = es.case_id and es.templete_id = ?");
			add("delete wch from wf_case_history wch, edoc_summary es where wch.id = es.case_id and es.templete_id = ?");
			add("delete cuhm from ctp_user_history_message cuhm, ctp_affair ca, edoc_summary es where cuhm.reference_id = ca.id and ca.object_id = es.id and es.templete_id = ?");
			add("delete dr from doc_resources dr, ctp_affair ca, edoc_summary es where dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?");
			add("delete cum from ctp_user_message cum, ctp_affair ca, edoc_summary es where cum.reference_id = ca.id and ca.object_id = es.id and es.templete_id = ?");
			add("delete wpr from wf_process_running wpr, edoc_summary es where wpr.id = es.process_id and es.templete_id = ?");
			add("delete eo from edoc_opinion eo, edoc_summary es where eo.edoc_id = es.id and es.templete_id = ?");
			add("delete est from edoc_stat est, edoc_summary es where est.edoc_id = es.id and es.templete_id = ?");
			add("delete gr from govdoc_register gr, edoc_summary es where gr.summary_id = es.id and es.templete_id = ?");
			add("delete cwt from ctp_workflow_track cwt, edoc_summary es where cwt.module_id = es.id and es.templete_id = ?");
			add("delete gedl from govdoc_exchange_detail_log gedl, govdoc_exchange_detail ged, govdoc_exchange_main gem, edoc_summary es where gedl.detail_id = ged.id and ged.main_id = gem.id and gem.summary_id = es.id and es.templete_id = ?");
			add("delete ged from govdoc_exchange_detail ged, govdoc_exchange_main gem, edoc_summary es where ged.main_id = gem.id and gem.summary_id = es.id and es.templete_id = ?");
			add("delete gem from govdoc_exchange_main gem, edoc_summary es where gem.summary_id = es.id and es.templete_id = ?");
			add("delete em from edoc_mark em, edoc_summary es where em.edoc_id = es.id and es.templete_id = ?");
			add("delete gmr from govdoc_mark_record gmr, edoc_summary es where gmr.summary_id = es.id and es.templete_id = ?");
			add("delete emh from edoc_mark_history emh, edoc_summary es where emh.edoc_id = es.id and es.templete_id = ?");
			add("delete esd from edoc_supervise_detail esd, edoc_summary es where esd.edoc_id = es.id and es.templete_id = ?");
			add("delete esr from edoc_summary_relation esr, edoc_summary es where esr.summary_id = es.id and es.templete_id = ?");
			add("delete dr from doc_resources dr, edoc_summary es where dr.source_id = es.id and es.templete_id = ?");
			add("delete dr from doc_resources dr, edoc_summary es,ctp_affair ca where dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?");
			add("delete elpn from edoc_leader_pishi_no elpn, edoc_summary es where elpn.summary_id = es.id and es.templete_id = ?");
			add("delete ese from edoc_summary_extend ese, edoc_summary es where ese.summary_id = es.id and es.templete_id = ?");
			add("delete csd from ctp_supervise_detail csd, edoc_summary es where csd.entity_id = es.id and es.templete_id = ?");
			add("delete cca from ctp_comment_all cca, edoc_summary es where cca.module_id = es.id and es.templete_id = ?");
			// add("delete cca from ctp_content_all cca, edoc_summary es where cca.module_id
			// = es.id and es.templete_id = ?");
			add("delete gwr from govdoc_wpstrans_record gwr, ctp_affair ca,edoc_summary es where ca.object_id = es.id and gwr.affair_id = ca.id and es.templete_id = ?");
			add("delete ca from ctp_affair ca, edoc_summary es where ca.object_id = es.id and es.templete_id = ?");
			add("delete from edoc_summary where templete_id = ?");
		}
	};

	private static final List<String> oracleOrderListForTemplate = new ArrayList<String>() {
		{
			add("delete from ctp_process_log WHERE EXISTS (select 1 from ctp_process_log cpl, edoc_summary es where cpl.process_id = es.process_id and es.templete_id = ?)");
			add("delete from edoc_supervisor WHERE EXISTS (select 1 from edoc_supervisor ess, edoc_supervise_detail esd, edoc_summary es where ess.supervise_id = esd.id and esd.edoc_id = es.id and es.templete_id = ?)");
			add("delete from edoc_supervise_log WHERE EXISTS (select 1 from edoc_supervise_log esl, edoc_supervise_detail esd, edoc_summary es where esl.supervise_id = esd.id and esd.edoc_id = es.id and es.templete_id = ?)");
			add("delete from edoc_supervise_remind WHERE EXISTS (select 1 from edoc_supervise_remind esr, edoc_supervise_detail esd, edoc_summary es where esr.supervise_id = esd.id and esd.edoc_id = es.id and es.templete_id = ?)");
			add("delete from doc_learning WHERE EXISTS (select 1 from doc_learning dl, doc_resources dr, ctp_affair ca, edoc_summary es where dl.doc_resource_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?)");
			add("delete from doc_favorites WHERE EXISTS (select 1 from doc_favorites df, doc_resources dr, edoc_summary es where df.doc_resource_id = dr.id and dr.source_id = es.id and es.templete_id = ?)");
			add("delete from ctp_user_history_message WHERE EXISTS (select 1 from ctp_user_history_message cuhm, doc_resources dr, ctp_affair ca, edoc_summary es where cuhm.reference_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?)");
			add("delete from doc_favorites WHERE EXISTS (select 1 from doc_favorites df, doc_resources dr, ctp_affair ca, edoc_summary es where df.doc_resource_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?)");
			add("delete from doc_resources WHERE EXISTS (select 1 from doc_resources drm, doc_resources dr, ctp_affair ca, edoc_summary es where drm.source_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?)");
			add("delete from ctp_user_message WHERE EXISTS (select 1 from ctp_user_message cum, doc_resources dr, ctp_affair ca, edoc_summary es where cum.reference_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?)");
			add("delete from ctp_user_history_message WHERE EXISTS (select 1 from ctp_user_history_message cuhm, doc_resources dr, edoc_summary es where cuhm.reference_id = dr.id and dr.source_id = es.id and es.templete_id = ?)");
			add("delete from ctp_user_message WHERE EXISTS (select 1 from ctp_user_message cum, doc_resources dr, edoc_summary es where cum.reference_id = dr.id and dr.source_id = es.id and es.templete_id = ?)");
			add("delete from doc_learning WHERE EXISTS (select 1 from doc_learning dl, doc_resources dr, edoc_summary es where dl.doc_resource_id = dr.id and dr.source_id = es.id and es.templete_id = ?)");
			add("delete from doc_resources WHERE EXISTS (select 1 from doc_resources drm, doc_resources dr, edoc_summary es where drm.source_id = dr.id and dr.source_id = es.id and es.templete_id = ?)");
			add("delete from ctp_supervise_receiver WHERE EXISTS (select 1 from ctp_supervise_receiver csr, ctp_supervise_log csl, ctp_supervise_detail csd, edoc_summary es where csr.log_id = csl.id and csl.supervise_id = csd.id and csd.entity_id = es.id and es.templete_id = ?)");
			add("delete from ctp_supervisor WHERE EXISTS (select 1 from ctp_supervisor css, ctp_supervise_detail csd, edoc_summary es where css.supervise_id = csd.id and csd.entity_id = es.id and es.templete_id = ?)");
			add("delete from ctp_supervise_log WHERE EXISTS (select 1 from ctp_supervise_log csl, ctp_supervise_detail csd, edoc_summary es where csl.supervise_id = csd.id and csd.entity_id = es.id and es.templete_id = ?)");
			add("delete from ctp_user_history_message WHERE EXISTS (select 1 from ctp_user_history_message cuhm, ctp_supervise_detail csd, edoc_summary es where cuhm.reference_id = csd.id and csd.entity_id = es.id and es.templete_id = ?)");
			add("delete from ctp_user_message WHERE EXISTS (select 1 from ctp_user_message cum, ctp_supervise_detail csd, edoc_summary es where cum.reference_id = csd.id and csd.entity_id = es.id and es.templete_id = ?)");
			add("delete from wf_workitem_run WHERE EXISTS (select 1 from wf_workitem_run wwr, edoc_summary es, ctp_affair ca where wwr.id = ca.sub_object_id and ca.object_id = es.id and es.templete_id = ?)");
			add("delete from wf_case_run WHERE EXISTS (select 1 from wf_case_run wcr, edoc_summary es where wcr.id = es.case_id and es.templete_id = ?)");
			add("delete from wf_case_history WHERE EXISTS (select 1 from wf_case_history wch, edoc_summary es where wch.id = es.case_id and es.templete_id = ?)");
			add("delete from ctp_user_history_message WHERE EXISTS (select 1 from ctp_user_history_message cuhm, ctp_affair ca, edoc_summary es where cuhm.reference_id = ca.id and ca.object_id = es.id and es.templete_id = ?)");
			add("delete from doc_resources WHERE EXISTS (select 1 from doc_resources dr, ctp_affair ca, edoc_summary es where dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?)");
			add("delete from ctp_user_message WHERE EXISTS (select 1 from ctp_user_message cum, ctp_affair ca, edoc_summary es where cum.reference_id = ca.id and ca.object_id = es.id and es.templete_id = ?)");
			add("delete from wf_process_running WHERE EXISTS (select 1 from wf_process_running wpr, edoc_summary es where wpr.id = es.process_id and es.templete_id = ?)");
			add("delete from edoc_opinion WHERE EXISTS (select 1 from edoc_opinion eo, edoc_summary es where eo.edoc_id = es.id and es.templete_id = ?)");
			add("delete from edoc_stat WHERE EXISTS (select 1 from edoc_stat est, edoc_summary es where est.edoc_id = es.id and es.templete_id = ?)");
			add("delete from govdoc_register WHERE EXISTS (select 1 from govdoc_register gr, edoc_summary es where gr.summary_id = es.id and es.templete_id = ?)");
			add("delete from ctp_workflow_track WHERE EXISTS (select 1 from ctp_workflow_track cwt, edoc_summary es where cwt.module_id = es.id and es.templete_id = ?)");
			add("delete from govdoc_exchange_detail_log WHERE EXISTS (select 1 from govdoc_exchange_detail_log gedl, govdoc_exchange_detail ged, govdoc_exchange_main gem, edoc_summary es where gedl.detail_id = ged.id and ged.main_id = gem.id and gem.summary_id = es.id and es.templete_id = ?)");
			add("delete from govdoc_exchange_detail WHERE EXISTS (select 1 from govdoc_exchange_detail ged, govdoc_exchange_main gem, edoc_summary es where ged.main_id = gem.id and gem.summary_id = es.id and es.templete_id = ?)");
			add("delete from govdoc_exchange_main WHERE EXISTS (select 1 from govdoc_exchange_main gem, edoc_summary es where gem.summary_id = es.id and es.templete_id = ?)");
			add("delete from edoc_mark WHERE EXISTS (select 1 from edoc_mark em, edoc_summary es where em.edoc_id = es.id and es.templete_id = ?)");
			add("delete from govdoc_mark_record WHERE EXISTS (select 1 from govdoc_mark_record gmr, edoc_summary es where gmr.summary_id = es.id and es.templete_id = ?)");
			add("delete from edoc_mark_history WHERE EXISTS (select 1 from edoc_mark_history emh, edoc_summary es where emh.edoc_id = es.id and es.templete_id = ?)");
			add("delete from edoc_supervise_detail WHERE EXISTS (select 1 from edoc_supervise_detail esd, edoc_summary es where esd.edoc_id = es.id and es.templete_id = ?)");
			add("delete from edoc_summary_relation WHERE EXISTS (select 1 from edoc_summary_relation esr, edoc_summary es where esr.summary_id = es.id and es.templete_id = ?)");
			add("delete from doc_resources WHERE EXISTS (select 1 from doc_resources dr, edoc_summary es where dr.source_id = es.id and es.templete_id = ?)");
			add("delete from doc_resources WHERE EXISTS (select 1 from doc_resources dr, edoc_summary es,ctp_affair ca where dr.source_id = ca.id and ca.object_id = es.id and es.templete_id = ?)");
			add("delete from edoc_leader_pishi_no WHERE EXISTS (select 1 from edoc_leader_pishi_no elpn, edoc_summary es where elpn.summary_id = es.id and es.templete_id = ?)");
			add("delete from edoc_summary_extend WHERE EXISTS (select 1 from edoc_summary_extend ese, edoc_summary es where ese.summary_id = es.id and es.templete_id = ?)");
			add("delete from ctp_supervise_detail WHERE EXISTS (select 1 from ctp_supervise_detail csd, edoc_summary es where csd.entity_id = es.id and es.templete_id = ?)");
			add("delete from ctp_comment_all WHERE EXISTS (select 1 from ctp_comment_all cca, edoc_summary es where cca.module_id = es.id and es.templete_id = ?)");
			// add("delete from ctp_content_all WHERE EXISTS (select 1 from ctp_content_all
			// cca, edoc_summary es where cca.module_id = es.id and es.templete_id = ?)");
			add("delete from govdoc_wpstrans_record WHERE EXISTS (select 1 from govdoc_wpstrans_record gwr, ctp_affair ca,edoc_summary es where ca.object_id = es.id and gwr.affair_id = ca.id and es.templete_id = ?)");
			add("delete from ctp_affair WHERE EXISTS (select 1 from ctp_affair ca, edoc_summary es where ca.object_id = es.id and es.templete_id = ?)");
			add("delete from edoc_summary where templete_id = ?");
		}
	};

	private static final List<String> mysqlAndSqlServerOrderListForSummary = new ArrayList<String>() {
		{
			add("delete cpl from ctp_process_log cpl, edoc_summary es where cpl.process_id = es.process_id and es.id in ");
			add("delete ess from edoc_supervisor ess, edoc_supervise_detail esd, edoc_summary es where ess.supervise_id = esd.id and esd.edoc_id = es.id and es.id in ");
			add("delete esl from edoc_supervise_log esl, edoc_supervise_detail esd, edoc_summary es where esl.supervise_id = esd.id and esd.edoc_id = es.id and es.id in ");
			add("delete esr from edoc_supervise_remind esr, edoc_supervise_detail esd, edoc_summary es where esr.supervise_id = esd.id and esd.edoc_id = es.id and es.id in ");
			add("delete dl from doc_learning dl, doc_resources dr, ctp_affair ca, edoc_summary es where dl.doc_resource_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete df from doc_favorites df, doc_resources dr, edoc_summary es where df.doc_resource_id = dr.id and dr.source_id = es.id and es.id in ");
			add("delete df from doc_favorites df, doc_resources dr, ctp_affair ca, edoc_summary es where df.doc_resource_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete cuhm from ctp_user_history_message cuhm, doc_resources dr, ctp_affair ca, edoc_summary es where cuhm.reference_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete drm from doc_resources drm, doc_resources dr, ctp_affair ca, edoc_summary es where drm.source_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete cum from ctp_user_message cum, doc_resources dr, ctp_affair ca, edoc_summary es where cum.reference_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete cuhm from ctp_user_history_message cuhm, doc_resources dr, edoc_summary es where cuhm.reference_id = dr.id and dr.source_id = es.id and es.id in ");
			add("delete cum from ctp_user_message cum, doc_resources dr, edoc_summary es where cum.reference_id = dr.id and dr.source_id = es.id and es.id in ");
			add("delete dl from doc_learning dl, doc_resources dr, edoc_summary es where dl.doc_resource_id = dr.id and dr.source_id = es.id and es.id in ");
			add("delete drm from doc_resources drm, doc_resources dr, edoc_summary es where drm.source_id = dr.id and dr.source_id = es.id and es.id in ");
			add("delete csr from ctp_supervise_receiver csr, ctp_supervise_log csl, ctp_supervise_detail csd, edoc_summary es where csr.log_id = csl.id and csl.supervise_id = csd.id and csd.entity_id = es.id and es.id in ");
			add("delete css from ctp_supervisor css, ctp_supervise_detail csd, edoc_summary es where css.supervise_id = csd.id and csd.entity_id = es.id and es.id in ");
			add("delete csl from ctp_supervise_log csl, ctp_supervise_detail csd, edoc_summary es where csl.supervise_id = csd.id and csd.entity_id = es.id and es.id in ");
			add("delete cuhm from ctp_user_history_message cuhm, ctp_supervise_detail csd, edoc_summary es where cuhm.reference_id = csd.id and csd.entity_id = es.id and es.id in ");
			add("delete cum from ctp_user_message cum, ctp_supervise_detail csd, edoc_summary es where cum.reference_id = csd.id and csd.entity_id = es.id and es.id in ");
			add("delete wwr from wf_workitem_run wwr, edoc_summary es, ctp_affair ca where wwr.id = ca.sub_object_id and ca.object_id = es.id and es.id in ");
			add("delete wcr from wf_case_run wcr, edoc_summary es where wcr.id = es.case_id and es.id in ");
			add("delete wch from wf_case_history wch, edoc_summary es where wch.id = es.case_id and es.id in ");
			add("delete cuhm from ctp_user_history_message cuhm, ctp_affair ca, edoc_summary es where cuhm.reference_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete dr from doc_resources dr, ctp_affair ca, edoc_summary es where dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete cum from ctp_user_message cum, ctp_affair ca, edoc_summary es where cum.reference_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete wpr from wf_process_running wpr, edoc_summary es where wpr.id = es.process_id and es.id in ");
			add("delete eo from edoc_opinion eo, edoc_summary es where eo.edoc_id = es.id and es.id in ");
			add("delete est from edoc_stat est, edoc_summary es where est.edoc_id = es.id and es.id in ");
			add("delete gr from govdoc_register gr, edoc_summary es where gr.summary_id = es.id and es.id in ");
			add("delete cwt from ctp_workflow_track cwt, edoc_summary es where cwt.module_id = es.id and es.id in ");
			add("delete gedl from govdoc_exchange_detail_log gedl, govdoc_exchange_detail ged, govdoc_exchange_main gem, edoc_summary es where gedl.detail_id = ged.id and ged.main_id = gem.id and gem.summary_id = es.id and es.id in ");
			add("delete ged from govdoc_exchange_detail ged, govdoc_exchange_main gem, edoc_summary es where ged.main_id = gem.id and gem.summary_id = es.id and es.id in ");
			add("delete gem from govdoc_exchange_main gem, edoc_summary es where gem.summary_id = es.id and es.id in ");
			add("delete em from edoc_mark em, edoc_summary es where em.edoc_id = es.id and es.id in ");
			add("delete gmr from govdoc_mark_record gmr, edoc_summary es where gmr.summary_id = es.id and es.id in ");
			add("delete emh from edoc_mark_history emh, edoc_summary es where emh.edoc_id = es.id and es.id in ");
			add("delete esd from edoc_supervise_detail esd, edoc_summary es where esd.edoc_id = es.id and es.id in ");
			add("delete esr from edoc_summary_relation esr, edoc_summary es where esr.summary_id = es.id and es.id in ");
			add("delete dr from doc_resources dr, edoc_summary es where dr.source_id = es.id and es.id in ");
			add("delete dr from doc_resources dr, edoc_summary es,ctp_affair ca where dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete elpn from edoc_leader_pishi_no elpn, edoc_summary es where elpn.summary_id = es.id and es.id in ");
			add("delete ese from edoc_summary_extend ese, edoc_summary es where ese.summary_id = es.id and es.id in ");
			add("delete csd from ctp_supervise_detail csd, edoc_summary es where csd.entity_id = es.id and es.id in ");
			add("delete cca from ctp_comment_all cca, edoc_summary es where cca.module_id = es.id and es.id in ");
			// add("delete cca from ctp_content_all cca, edoc_summary es where cca.module_id
			// = es.id and es.id = ?");
			add("delete gwr from govdoc_wpstrans_record gwr, ctp_affair ca,edoc_summary es where ca.object_id = es.id and gwr.affair_id = ca.id and es.id in ");
			add("delete ca from ctp_affair ca, edoc_summary es where ca.object_id = es.id and es.id in ");
			add("delete from edoc_summary where id in ");
		}
	};

	private static final List<String> oracleOrderListForSummary = new ArrayList<String>() {
		{
			add("delete from ctp_process_log WHERE EXISTS (select 1 from ctp_process_log cpl, edoc_summary es where cpl.process_id = es.process_id and es.id in )");
			add("delete from edoc_supervisor WHERE EXISTS (select 1 from edoc_supervisor ess, edoc_supervise_detail esd, edoc_summary es where ess.supervise_id = esd.id and esd.edoc_id = es.id and es.id in ");
			add("delete from edoc_supervise_log WHERE EXISTS (select 1 from edoc_supervise_log esl, edoc_supervise_detail esd, edoc_summary es where esl.supervise_id = esd.id and esd.edoc_id = es.id and es.id in ");
			add("delete from edoc_supervise_remind WHERE EXISTS (select 1 from edoc_supervise_remind esr, edoc_supervise_detail esd, edoc_summary es where esr.supervise_id = esd.id and esd.edoc_id = es.id and es.id in ");
			add("delete from doc_learning WHERE EXISTS (select 1 from doc_learning dl, doc_resources dr, ctp_affair ca, edoc_summary es where dl.doc_resource_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete from doc_favorites WHERE EXISTS (select 1 from doc_favorites df, doc_resources dr, edoc_summary es where df.doc_resource_id = dr.id and dr.source_id = es.id and es.id in ");
			add("delete from ctp_user_history_message WHERE EXISTS (select 1 from ctp_user_history_message cuhm, doc_resources dr, ctp_affair ca, edoc_summary es where cuhm.reference_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete from doc_favorites WHERE EXISTS (select 1 from doc_favorites df, doc_resources dr, ctp_affair ca, edoc_summary es where df.doc_resource_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete from doc_resources WHERE EXISTS (select 1 from doc_resources drm, doc_resources dr, ctp_affair ca, edoc_summary es where drm.source_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete from ctp_user_message WHERE EXISTS (select 1 from ctp_user_message cum, doc_resources dr, ctp_affair ca, edoc_summary es where cum.reference_id = dr.id and dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete from ctp_user_history_message WHERE EXISTS (select 1 from ctp_user_history_message cuhm, doc_resources dr, edoc_summary es where cuhm.reference_id = dr.id and dr.source_id = es.id and es.id in ");
			add("delete from ctp_user_message WHERE EXISTS (select 1 from ctp_user_message cum, doc_resources dr, edoc_summary es where cum.reference_id = dr.id and dr.source_id = es.id and es.id in ");
			add("delete from doc_learning WHERE EXISTS (select 1 from doc_learning dl, doc_resources dr, edoc_summary es where dl.doc_resource_id = dr.id and dr.source_id = es.id and es.id in ");
			add("delete from doc_resources WHERE EXISTS (select 1 from doc_resources drm, doc_resources dr, edoc_summary es where drm.source_id = dr.id and dr.source_id = es.id and es.id in ");
			add("delete from ctp_supervise_receiver WHERE EXISTS (select 1 from ctp_supervise_receiver csr, ctp_supervise_log csl, ctp_supervise_detail csd, edoc_summary es where csr.log_id = csl.id and csl.supervise_id = csd.id and csd.entity_id = es.id and es.id in ");
			add("delete from ctp_supervisor WHERE EXISTS (select 1 from ctp_supervisor css, ctp_supervise_detail csd, edoc_summary es where css.supervise_id = csd.id and csd.entity_id = es.id and es.id in ");
			add("delete from ctp_supervise_log WHERE EXISTS (select 1 from ctp_supervise_log csl, ctp_supervise_detail csd, edoc_summary es where csl.supervise_id = csd.id and csd.entity_id = es.id and es.id in ");
			add("delete from ctp_user_history_message WHERE EXISTS (select 1 from ctp_user_history_message cuhm, ctp_supervise_detail csd, edoc_summary es where cuhm.reference_id = csd.id and csd.entity_id = es.id and es.id in ");
			add("delete from ctp_user_message WHERE EXISTS (select 1 from ctp_user_message cum, ctp_supervise_detail csd, edoc_summary es where cum.reference_id = csd.id and csd.entity_id = es.id and es.id in ");
			add("delete from wf_workitem_run WHERE EXISTS (select 1 from wf_workitem_run wwr, edoc_summary es, ctp_affair ca where wwr.id = ca.sub_object_id and ca.object_id = es.id and es.id in ");
			add("delete from wf_case_run WHERE EXISTS (select 1 from wf_case_run wcr, edoc_summary es where wcr.id = es.case_id and es.templete_id in ");
			add("delete from wf_case_history WHERE EXISTS (select 1 from wf_case_history wch, edoc_summary es where wch.id = es.case_id and es.templete_id in ");
			add("delete from ctp_user_history_message WHERE EXISTS (select 1 from ctp_user_history_message cuhm, ctp_affair ca, edoc_summary es where cuhm.reference_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete from doc_resources WHERE EXISTS (select 1 from doc_resources dr, ctp_affair ca, edoc_summary es where dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete from ctp_user_message WHERE EXISTS (select 1 from ctp_user_message cum, ctp_affair ca, edoc_summary es where cum.reference_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete from wf_process_running WHERE EXISTS (select 1 from wf_process_running wpr, edoc_summary es where wpr.id = es.process_id and es.id in ");
			add("delete from edoc_opinion WHERE EXISTS (select 1 from edoc_opinion eo, edoc_summary es where eo.edoc_id = es.id and es.id in ");
			add("delete from edoc_stat WHERE EXISTS (select 1 from edoc_stat est, edoc_summary es where est.edoc_id = es.id and es.id in ");
			add("delete from govdoc_register WHERE EXISTS (select 1 from govdoc_register gr, edoc_summary es where gr.summary_id = es.id and es.id in ");
			add("delete from ctp_workflow_track WHERE EXISTS (select 1 from ctp_workflow_track cwt, edoc_summary es where cwt.module_id = es.id and es.id in ");
			add("delete from govdoc_exchange_detail_log WHERE EXISTS (select 1 from govdoc_exchange_detail_log gedl, govdoc_exchange_detail ged, govdoc_exchange_main gem, edoc_summary es where gedl.detail_id = ged.id and ged.main_id = gem.id and gem.summary_id = es.id and es.id in ");
			add("delete from govdoc_exchange_detail WHERE EXISTS (select 1 from govdoc_exchange_detail ged, govdoc_exchange_main gem, edoc_summary es where ged.main_id = gem.id and gem.summary_id = es.id and es.id in ");
			add("delete from govdoc_exchange_main WHERE EXISTS (select 1 from govdoc_exchange_main gem, edoc_summary es where gem.summary_id = es.id and es.id in ");
			add("delete from edoc_mark WHERE EXISTS (select 1 from edoc_mark em, edoc_summary es where em.edoc_id = es.id and es.id in ");
			add("delete from govdoc_mark_record WHERE EXISTS (select 1 from govdoc_mark_record gmr, edoc_summary es where gmr.summary_id = es.id and es.id in ");
			add("delete from edoc_mark_history WHERE EXISTS (select 1 from edoc_mark_history emh, edoc_summary es where emh.edoc_id = es.id and es.id in ");
			add("delete from edoc_supervise_detail WHERE EXISTS (select 1 from edoc_supervise_detail esd, edoc_summary es where esd.edoc_id = es.id and es.id in ");
			add("delete from edoc_summary_relation WHERE EXISTS (select 1 from edoc_summary_relation esr, edoc_summary es where esr.summary_id = es.id and es.id in ");
			add("delete from doc_resources WHERE EXISTS (select 1 from doc_resources dr, edoc_summary es where dr.source_id = es.id and es.id in ");
			add("delete from doc_resources WHERE EXISTS (select 1 from doc_resources dr, edoc_summary es,ctp_affair ca where dr.source_id = ca.id and ca.object_id = es.id and es.id in ");
			add("delete from edoc_leader_pishi_no WHERE EXISTS (select 1 from edoc_leader_pishi_no elpn, edoc_summary es where elpn.summary_id = es.id and es.id in ");
			add("delete from edoc_summary_extend WHERE EXISTS (select 1 from edoc_summary_extend ese, edoc_summary es where ese.summary_id = es.id and es.id in ");
			add("delete from ctp_supervise_detail WHERE EXISTS (select 1 from ctp_supervise_detail csd, edoc_summary es where csd.entity_id = es.id and es.id in ");
			add("delete from ctp_comment_all WHERE EXISTS (select 1 from ctp_comment_all cca, edoc_summary es where cca.module_id = es.id and es.id in ");
			// add("delete from ctp_content_all WHERE EXISTS (select 1 from ctp_content_all
			// cca, edoc_summary es where cca.module_id = es.id and es.id in (?))");
			add("delete from govdoc_wpstrans_record WHERE EXISTS (select 1 from govdoc_wpstrans_record gwr, ctp_affair ca,edoc_summary es where ca.object_id = es.id and gwr.affair_id = ca.id and es.id in ");
			add("delete from ctp_affair WHERE EXISTS (select 1 from ctp_affair ca, edoc_summary es where ca.object_id = es.id and es.id in ");
			//add("delete from edoc_summary where id in ");
		}
	};

	public void deleteEdocDataByTemplate(Object obj, Integer needDelRec) throws Exception {
		LOGGER.info("进入删除公文数据逻辑");
		// 如果传入的是模板对象，根据模板删除相关数据
		if (obj instanceof CtpTemplate) {
			CtpTemplate template = (CtpTemplate) obj;
			int tempModuleType = template.getModuleType();
			if (tempModuleType != 401 && tempModuleType != 402 && tempModuleType != 403 && tempModuleType != 404) {
				LOGGER.warn("传入的模板ID不属于公文，清除任务取消");
				return;
			}
			LOGGER.info(
					"根据模板删除公文数据，开始-------------------，模板id=" + template.getId() + ", 模板名称:" + template.getSubject());
			List<Object> tempalteParam = new ArrayList<Object>();
			tempalteParam.add(template.getId());

			// 查询出交换相关的mainId
			String exchangeMainIdSql = "select gem.id from edoc_summary es,govdoc_exchange_main gem where es.id = gem.summary_id and es.templete_id = ?";
			List<Map<String, Object>> exchangeMainIds = exeQuerySource(exchangeMainIdSql, tempalteParam);
			String mainIds = installIdToString(exchangeMainIds, "id");
			List<Object> mainIdsParam = new ArrayList<Object>();
			mainIdsParam.add(mainIds.toString());
			if (!Strings.isBlank(mainIds)) {
				StringBuilder needDelSummaryIds = new StringBuilder();
				// 处理生成的签收数据ID
				String signSummaryIdsSql = "select summary_id from govdoc_exchange_detail where main_id in (?)";
				List<Map<String, Object>> signSummaryIds = exeQuerySource(signSummaryIdsSql, mainIdsParam);
				String signIds = installIdToString(signSummaryIds, "summary_id");
				needDelSummaryIds.append(signIds);

				if (needDelRec == 1) {
					// 处理生成的收文数据ID
					String recSummaryIdsSql = "select rec_summary_id from govdoc_exchange_detail where main_id in (?)";
					List<Map<String, Object>> recSummaryIds = exeQuerySource(recSummaryIdsSql, mainIdsParam);
					String recIds = installIdToString(recSummaryIds, "rec_summary_id");
					needDelSummaryIds.append(",").append(recIds);
				}
				if (!Strings.isBlank(needDelSummaryIds.toString())) {
					List<Map<String, Object>> formIdAndSummaryId = getSummaryFormIdAndSummaryId(
							needDelSummaryIds.toString());
					deleteEdocDataBySummary(formIdAndSummaryId);
				}

			}

			//删除主表记录
			delFormmainByFormIdAndObjectId(template.getFormAppId(),template.getId(),1);
			
			//删除设置的超期任务 
			delJKTask(template.getId(),1);
			
			//删除公文相关的其他数据 
			if(DBTYPE.indexOf("oracle") > -1){
				exeBulkDelQuery(oracleOrderListForTemplate,tempalteParam); 
			}else if(DBTYPE.indexOf("mysql") > -1 || DBTYPE.indexOf("sqlserver") > -1){
				exeBulkDelQuery(mysqlAndSqlServerOrderListForTemplate,tempalteParam);
			}else{//兼容 达梦 等其他数据库 据说和sqlserver相同
				exeBulkDelQuery(mysqlAndSqlServerOrderListForTemplate,tempalteParam); 
			}
			
			LOGGER.info("根据模板删除公文数据，结束-------------------");
			
		}
	}

	public void deleteEdocDataBySummary(List<Map<String, Object>> objs) throws Exception {
		LOGGER.info("根据功能我给你ID删除公文数据，开始-------------------");
		if (objs.isEmpty()) {
			return;
		}
		StringBuilder summaryIds = new StringBuilder();
		for (int i = 0; i < objs.size(); i++) {

			// 删除主表记录
			delFormmainByFormIdAndObjectId((Long) objs.get(i).get("form_app_id"), (Long) objs.get(i).get("id"), 2);

			// 删除设置的超期任务
			delJKTask((Long) objs.get(i).get("id"), 2);

			summaryIds.append((Long) objs.get(i).get("id"));
			if (i + 1 < objs.size()) {
				summaryIds.append(",");
			}
		}

		// 删除公文相关的其他数据
		if (DBTYPE.indexOf("oracle") > -1) {
			exeBulkDelQueryForIn(oracleOrderListForSummary, summaryIds.toString());
		} else if (DBTYPE.indexOf("mysql") > -1 || DBTYPE.indexOf("sqlserver") > -1) {
			exeBulkDelQueryForIn(mysqlAndSqlServerOrderListForSummary, summaryIds.toString());
		} else {// 兼容 达梦 等其他数据库 据说和sqlserver相同
			exeBulkDelQueryForIn(mysqlAndSqlServerOrderListForSummary, summaryIds.toString());
		}

		LOGGER.info("根据公文ID删除公文数据，结束-------------------");

	}

	/**
	 * 删除动态表数据
	 * 
	 * @param formId 表单ID
	 * @param objId  需要删除的数据ID
	 * @param type   删除的类型1 根据模板ID删除，2 根据SummaryId删除
	 * @return
	 * @throws Exception
	 */
	private void delFormmainByFormIdAndObjectId(Long formId, Long objId, int type) throws Exception {
		// 动态表名称
		String tableName = "";
		String formSql = "select field_info from form_definition where id = ?";
		List<Object> formParams = new ArrayList<Object>();
		formParams.add(formId);
		List<Map<String, Object>> fieldInfos = exeQuerySource(formSql, formParams);
		for (Map<String, Object> fieldInfo : fieldInfos) {
			String info = (String) fieldInfo.get("field_info");
			Element root = getRootElementByString(info);
			Element tableElement = root.element("Table");
			tableName = tableElement.attributeValue("name");
		}

		StringBuilder delFmMysqlSqlServerSql = new StringBuilder();
		StringBuilder delFmOracleSql = new StringBuilder();

		delFmMysqlSqlServerSql.append("delete fm from ").append(tableName)
				.append(" fm, edoc_summary es where fm.id = es.form_recordid ");
		delFmOracleSql.append("delete fm from ").append(tableName).append(" where exists (select 1 from ")
				.append(tableName).append(" fm, edoc_summary es where fm.id = es.form_recordid ");

		if (type == 1) {
			delFmMysqlSqlServerSql.append("and es.templete_id = ?");
			delFmOracleSql.append(" and es.templete_id = ?");
		} else {
			delFmMysqlSqlServerSql.append("and es.id = ?");
			delFmOracleSql.append("and es.id = ?");
		}

		List<Object> param = new ArrayList<Object>();
		param.add(objId);
		// 删除主表记录
		if (DBTYPE.indexOf("oracle") > -1) {
			exeDelQuery(delFmOracleSql.toString(), param);
		} else if (DBTYPE.indexOf("mysql") > -1 || DBTYPE.indexOf("sqlserver") > -1) {
			exeDelQuery(delFmMysqlSqlServerSql.toString(), param);
		} else {// 兼容 达梦 等其他数据库 据说和sqlserver相同
			exeDelQuery(delFmMysqlSqlServerSql.toString(), param);
		}
	}

	/**
	 * 删除超期定时的任务
	 * 
	 * @param objId
	 * @param type
	 * @throws Exception
	 */
	private void delJKTask(Long obj, int type) throws Exception {
		StringBuilder selectSummaryIdSql = new StringBuilder();
		StringBuilder selectAffairIdSql = new StringBuilder();
		selectSummaryIdSql.append("select id,subject from edoc_summary es where ");
		selectAffairIdSql.append("select ca.id from ctp_affair ca,edoc_summary es where ca.object_id = es.id ");
		List<Object> param = new ArrayList<Object>();
		param.add(obj);

		if (type == 1) {
			selectSummaryIdSql.append(" es.templete_id = ?");
			selectAffairIdSql.append(" and es.templete_id = ?");
		} else {
			selectSummaryIdSql.append(" es.id = ?");
			selectAffairIdSql.append(" and es.id = ?");
		}

		List<Map<String, Object>> sIds = exeQuerySource(selectSummaryIdSql.toString(), param);
		List<Map<String, Object>> aIds = exeQuerySource(selectAffairIdSql.toString(), param);
		sIds.addAll(aIds);
		for (Map<String, Object> objId : sIds) {
			Long id = (Long) objId.get("id");
			String delJkJobsql = "delete from jk_job_details where job_name like '%" + id + "%'";
			String delJkSimpleTriggersql = "delete from jk_simple_triggers where trigger_name like '%" + id + "%'";
			String delJkTriggersql = "delete from jk_triggers where trigger_name like '%" + id + "%'";
			exeDelQuery(delJkJobsql, null);
			exeDelQuery(delJkSimpleTriggersql, null);
			exeDelQuery(delJkTriggersql, null);

			String subject = (String) objId.get("subject");
			if (!Strings.isBlank(subject)) {
				LOGGER.info("清除公文《" + subject + "》相关数据！");
			}
		}
	}

	/**
	 * 根据人员姓名模糊查询人员ID
	 * 
	 * @param memberName
	 * @return
	 * @throws Exception
	 */
	public Map<Long, String> getMemberIdsByName(String memberName) throws Exception {
		Map<Long, String> memberIdAndName = new HashMap<Long, String>();
		String sql = "select id,name from org_member where name like '%" + memberName + "%'";
		List<Map<String, Object>> result = exeQuerySource(sql, null);
		if (!result.isEmpty()) {
			for (Map<String, Object> map : result) {
				memberIdAndName.put((Long) map.get("id"), (String) map.get("name"));
			}
		}

		return memberIdAndName;
	}

	/**
	 * 根据人员姓名模糊查询人员ID
	 * 
	 * @param memberName
	 * @return
	 * @throws Exception
	 */
	public Map<Long, String> getTemplateNameAndIdByName(String templateName) throws Exception {
		Map<Long, String> templateIdAndName = new HashMap<Long, String>();
		String sql = "select id,subject from ctp_template where module_type in (401,402,403,404) and subject like '%"
				+ templateName + "%'";
		List<Map<String, Object>> result = exeQuerySource(sql, null);
		if (!result.isEmpty()) {
			for (Map<String, Object> map : result) {
				templateIdAndName.put((Long) map.get("id"), (String) map.get("subject"));
			}
		}

		return templateIdAndName;
	}

	public FlipInfo getEdocTemplateByTemplateName(String templateName, FlipInfo flipInfo) throws Exception {
		if (!Strings.isBlank(templateName)) {
			StringBuilder sql = new StringBuilder();
			sql.append(
					"select id,module_type,subject,org_account_id from ctp_template where module_type in (401,402,403,404) and is_system = 1 ");
			sql.append(" and subject like '%").append(templateName).append("%'");
			flipInfo = exeQueryByPage(sql.toString(), null, flipInfo.getPage(), flipInfo.getSize(), -1);
		}
		return flipInfo;
	}

	/**
	    * 根据条件筛选summary的数据
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public FlipInfo getSummaryByCondition(Map<String, Object> param, FlipInfo flipInfo) throws Exception {
		if (param != null) {
			List<Object> queryParam = new ArrayList<Object>();
			StringBuilder sql = new StringBuilder();
			sql.append(
					"select id,doc_mark,serial_no,subject,govdoc_type,templete_id,start_user_id,start_time from edoc_summary where govdoc_type in (1,2,3,4) ");
			if (param.get("templateIds") != null) {
				sql.append(" and templete_id in (").append(param.get("templateIds")).append(") ");
			}
			if (param.get("memberIds") != null) {
				sql.append(" and start_user_id in (").append(param.get("memberIds")).append(")");
			}
			if (param.get("subject") != null) {
				sql.append(" and subject like '%").append(param.get("subject")).append("%'");
			}
			if (param.get("docMark") != null) {
				sql.append(" and doc_mark = ?");
				queryParam.add(param.get("docMark"));
			}
			if (param.get("innerMark") != null) {
				sql.append(" and serial_no = ?");
				queryParam.add(param.get("innerMark"));
			}
			if (param.get("sDate") != null) {
				sql.append(" and start_time >= ?");
				queryParam.add(param.get("sDate"));
			}
			if (param.get("eDate") != null) {
				sql.append(" and start_time <= ?");
				queryParam.add(param.get("eDate"));
			}
			flipInfo = exeQueryByPage(sql.toString(), queryParam, flipInfo.getPage(), flipInfo.getSize(), -1);

		}
		return flipInfo;
	}
	
	/**
	 *根据条件 查询待办
	 * @param param
	 * @param flipInfo
	 * @return
	 * @throws Exception
	 */
	public FlipInfo getPendingAffairByCondition(Map<String, Object> param, FlipInfo flipInfo) throws Exception {
		if (param != null) {
			List<Object> queryParam = new ArrayList<Object>();
			StringBuilder sql = new StringBuilder();
			sql.append(
					"select ca.id,ca.subject,ca.create_date,om.name from ctp_affair ca,org_member om where ca.member_id = om.id and ca.app = 4 and ca.state = 3 ");
			if (param.get("memberIds") != null) {
				sql.append(" and ca.sender_id in (").append(param.get("memberIds")).append(")");
			}
			if (param.get("subject") != null) {
				sql.append(" and ca.subject like '%").append(param.get("subject")).append("%'");
			}
			if (param.get("sDate") != null) {
				sql.append(" and ca.create_date >= ?");
				queryParam.add(param.get("sDate"));
			}
			if (param.get("eDate") != null) {
				sql.append(" and ca.create_date <= ?");
				queryParam.add(param.get("eDate"));
			}
			flipInfo = exeQueryByPage(sql.toString(), queryParam, flipInfo.getPage(), flipInfo.getSize(), -1);

		}
		return flipInfo;
		
	}

	public static List<Map<String, Object>> getSummaryFormIdAndSummaryId(String summaryIds) throws Exception {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if (!Strings.isBlank(summaryIds)) {
			List<Object> queryParam = new ArrayList<Object>();
			queryParam.add(summaryIds);
			StringBuilder sql = new StringBuilder("select id,form_app_id from edoc_summary where id in (");
			sql.append(summaryIds).append(")");
			result = exeQuerySource(sql.toString(),null);
		}
		return result;
	}
	
	public static Integer countEdocSummaryByTemplateId(Long templateId) throws Exception {
		Integer result = 0;
		if(templateId != null) {
			List<Object> queryParam = new ArrayList<Object>();
			queryParam.add(templateId);
			String countSql = "select count(id) from edoc_summary where templete_id = ?";
			List<Map<String, Object>> queryRes = exeQuerySource(countSql,queryParam);
			return Integer.valueOf(String.valueOf(queryRes.get(0).get("count(id)")));
		}

		return result;
	}
	
	public void replaceEdocMarkRecords(Long summaryId,String docMark) throws Exception{
		List<Object> queryParam = new ArrayList<Object>();
		queryParam.add(docMark);
		queryParam.add(summaryId);
		String reSummaryMarkSql = "update edoc_summary set doc_mark = ? where id = ?";
		String reEdocMarkSql = "update edoc_mark set mark_definition_id = -1 , doc_mark_no = null ,category_id = null ,select_type = 1, doc_mark = ? where edoc_id = ?";
		String reGovdocMarkRecordsSql = "update govdoc_mark_record set select_type = 1, category_id = null, mark_def_id = null, call_id = null, mark_number = null, markstr = ? where summary_id = ?";
		List<String> sqlList = new ArrayList<String>();
		sqlList.add(reSummaryMarkSql);
		sqlList.add(reEdocMarkSql);
		sqlList.add(reGovdocMarkRecordsSql);
		exeBulkDelQuery(sqlList,queryParam);	
	}
	
	public void delRecData(Long summaryId) throws Exception{
		List<Object> queryParam = new ArrayList<Object>();
		queryParam.add(summaryId);
		String exchangeMainIdSql = "select gem.id from edoc_summary es,govdoc_exchange_main gem where es.id = gem.summary_id and es.id = ?";
		List<Map<String, Object>> exchangeMainIds = exeQuerySource(exchangeMainIdSql, queryParam);
		String mainIds = installIdToString(exchangeMainIds, "id");
		List<Object> mainIdsParam = new ArrayList<Object>();
		mainIdsParam.add(mainIds.toString());
		if (!Strings.isBlank(mainIds)) {
			StringBuilder needDelSummaryIds = new StringBuilder();

			// 处理生成的收文数据ID
			String recSummaryIdsSql = "select rec_summary_id from govdoc_exchange_detail where main_id = ?";
			List<Map<String, Object>> recSummaryIds = exeQuerySource(recSummaryIdsSql, mainIdsParam);
			String recIds = installIdToString(recSummaryIds, "rec_summary_id");
			needDelSummaryIds.append(recIds);
			
			if (!Strings.isBlank(needDelSummaryIds.toString())) {
				List<Map<String, Object>> formIdAndSummaryId = getSummaryFormIdAndSummaryId(
						needDelSummaryIds.toString());
				deleteEdocDataBySummary(formIdAndSummaryId);
			}

		}
	}
	
	/**
	 * 待办置已办
	 * @param affairId
	 * @throws Exception
	 */
	public void exAffairToDone(String affairId) throws Exception{
		StringBuilder sql  = new StringBuilder();
		sql.append("update ctp_affair set state = 4 where id in (").append(affairId).append(")");
		
		exeDelQuery(sql.toString(),null);
	}
	
	/**
	 * 根据summaryId更新所有相关affair的正文类型
	 * @param summaryId
	 * @param bodyType
	 * @throws Exception
	 */
	public void updateAffairBodyType(Long summaryId,String bodyType) throws Exception{
		List<Object> param = new ArrayList<Object>();
		String sql = "update ctp_affair set body_type = ? where object_id = ? ";
		param.add(bodyType);
		param.add(summaryId);
		
		exeDelQuery(sql,param);
		
	}

	private String installIdToString(List<Map<String, Object>> data, String symbol) {
		StringBuilder ids = new StringBuilder();
		if (!data.isEmpty()) {
			for (int i = 0; i < data.size(); i++) {
				ids.append(data.get(i).get(symbol));
				if (i + 1 < data.size()) {
					ids.append(",");
				}
			}
		}

		return ids.toString();
	}

	private static FlipInfo exeQueryByPage(String sql, List params, int page, int pageSize, int total) {
		JDBCAgent dba = new JDBCAgent();
		FlipInfo fpi = new FlipInfo();
		if (total < 0) {// 只取一次总数
			fpi.setNeedTotal(true);
		} else {
			fpi.setTotal(total);
			fpi.setNeedTotal(false);
		}
		fpi.setPage(page);
		fpi.setSize(pageSize);
		try {
			if (params == null) {
				dba.findByPaging(sql, fpi);
			} else {
				dba.findByPaging(sql, params, fpi);
			}
		} catch (Throwable e) {
			LOGGER.error("执行分页查询失败...", e);
		} finally {
			dba.close();
		}
		return fpi;
	}

	private void exeBulkDelQuery(List<String> sqlLIst, List params) throws Exception {
		for (String sql : sqlLIst) {
			exeDelQuery(sql, params);
		}
	}
	
	private void exeBulkDelQueryForIn(List<String> sqlLIst, String param) throws Exception {
		for (String sql : sqlLIst) {
			StringBuilder tempSql = new StringBuilder(sql);
			if (DBTYPE.indexOf("oracle") > -1) {
				tempSql.append("(").append(param).append("))");
			}else {
				tempSql.append("(").append(param).append(")");
			}
			exeDelQuery(tempSql.toString(), null);
		}
		if (DBTYPE.indexOf("oracle") > -1) {
			StringBuilder delSsql = new StringBuilder("delete from edoc_summary where id in (");
			delSsql.append(param).append(")");
			exeDelQuery(delSsql.toString(), null);
		}
	}

	private void exeDelQuery(String sql, List params) throws Exception {
		JDBCAgent jdbcAgent = new JDBCAgent();
		try {
			if (Strings.isNotEmpty(params)) {
				jdbcAgent.execute(sql, params);
			} else {
				jdbcAgent.execute(sql);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			jdbcAgent.close();
		}

	}

	private static List<Map<String, Object>> exeQuerySource(String sql, List params) throws Exception {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		JDBCAgent jdbcAgent = new JDBCAgent();
		try {
			if (Strings.isNotEmpty(params)) {
				jdbcAgent.execute(sql, params);
			} else {
				jdbcAgent.execute(sql);
			}
			result = (List<Map<String, Object>>) jdbcAgent.resultSetToList();
		} catch (Exception e) {
			throw e;
		} finally {
			jdbcAgent.close();
		}
		return result;
	}

	/**
	 * 获取当前数据库类型
	 * 
	 * @return
	 */
	private static String getDbType() {
		JDBCAgent agent = new JDBCAgent(true);
		String dbType = agent.getDBType().toLowerCase();
		agent.close();
		return dbType;
	}

	/**
	 * 获取xml的根节点
	 * 
	 * @param xml
	 * @return
	 */
	public static Element getRootElementByString(String xml) {
		if (Strings.isBlank(xml))
			return null;
		try {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(new InputSource(new ByteArrayInputStream(xml.trim().getBytes("utf-8"))));
			return doc.getRootElement();
		} catch (Exception e) {

		}
		return null;
	}
}
