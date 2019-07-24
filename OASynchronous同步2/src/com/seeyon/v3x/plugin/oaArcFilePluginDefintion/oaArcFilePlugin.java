package com.seeyon.v3x.plugin.oaArcFilePluginDefintion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import www.seeyon.com.utils.FileUtil;

import com.seeyon.v3x.services.V3XLocator;
import com.seeyon.v3x.services.document.DocumentFactory;

public class oaArcFilePlugin {
	private String para;
	private ReadConfigTool rft = new ReadConfigTool();
	private String driverClassName;
	private String oaurl;
	private String username;
	private String password;

	public String getPara() {
		return this.para;
	}

	public void setPara(String para) {
		this.para = para;
	}

	// 静态类
	private static class oaArcFilePluginInstance {
		private static final oaArcFilePlugin instance = new oaArcFilePlugin();
	}

	public static oaArcFilePlugin getInstance() {
		return oaArcFilePluginInstance.instance;
	}

	public void run() throws SQLException {
		System.out.println("开始执行档案接口数据同步操作！>>>>>>>>>");

		onIniParams();

		Connection conn = getConnection();
		PreparedStatement pst = null;

		conn.setAutoCommit(false);

		if (!onSynOrg(conn, pst).booleanValue()) {
			System.out.println("数据同步---->组织机构失败！");
			return;
		}
		if (!onSynUser(conn, pst).booleanValue()) {
			System.out.println("数据同步---->同步OA用户失败！");
			return;
		}
		if (!onSynEdoc(conn).booleanValue()) {
			System.out.println("数据同步---->公文信息失败！");
			return;
		}

		// copy 正文和附件带上后缀名
		copyEdoc(conn);

		copyAttachment(conn);

		System.out.println("结束执行档案接口数据同步操作！>>>>>>>>>");

		conn.commit();

		try {

			if (!conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void onIniParams() {
		this.driverClassName = this.rft.getString("driverClassName");
		this.oaurl = this.rft.getString("url");
		this.username = this.rft.getString("username");
		this.password = this.rft.getString("password");

		System.out.println("oaurl-------->:" + this.oaurl);
	}

	public Connection getConnection() {
		Connection conn = null; // 创建用于连接数据库的Connection对象
		try {
			Class.forName(this.driverClassName);// 加载Mysql数据驱动

			conn = DriverManager.getConnection(this.oaurl, this.username, this.password);// 创建数据连接

		} catch (Exception e) {

			e.printStackTrace();
		}
		return conn; // 返回所建立的数据库连接
	}

	private Boolean onSynOrg(Connection conn, PreparedStatement pst) {
		System.out.println("开始同步OA机构>>>>>>>>>>>>");
		boolean bresult = true;
		String strsql = "";

		try {
			strsql = "insert into s_midorg@dblink_oa_for_arc (ID, FLDSSGSID, FLDSSGSMC, C_ORGNAME, C_ORGID, C_PARENTID, C_DATE, I_TAG, I_STATE) "
					+ "select d.id, o.id, o.name, d.name, d.id, d.path, d.create_time, 0, 0"
					+ "  from v3x_org_account o, v3x_org_department d" + " where o.id = d.org_account_id"
					+ "   and d.is_deleted = 0" + "   and d.status = 1 "
					+ "   and d.id not in (select id from s_midorg@dblink_oa_for_arc)" + " order by d.sort_id";
			pst = conn.prepareStatement(strsql);
			pst.execute();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			bresult = false;
			e.printStackTrace();
		}
		/*
		 * finally { try { if (!pst.isClosed()) { pst.close(); } } catch
		 * (SQLException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } }
		 */

		System.out.println("同步OA机构结束>>>>>>>>>>>>");
		return Boolean.valueOf(bresult);
	}

	private Boolean onSynUser(Connection conn, PreparedStatement pst) {
		System.out.println("开始同步OA用户>>>>>>>>>>>>");
		boolean bresult = true;
		String strsql = "";
		try {
			strsql = "insert into S_MIDUSERS@DBLINK_OA_FOR_ARC(id,fldssgsid,fldssgsmc,c_username,c_logname,"
					+ " c_orgid,c_password,c_telphone, c_email, i_leave, i_tag, i_state, i_date, c_pde1, c_pde2, c_pde3) "
					+ " select m.id,m.org_account_id,a.name accountname,m.name,substr(p.full_path,7,length(p.full_path)) pw,"
					+ " d.name dname,c.column_value,m.tel_number,m.email_address,"
					+ " case m.state when 1 then 0 else 1 end state,0,0,sysdate,'','','' " + " from v3x_org_member m "
					+ " left join v3x_org_account a on m.org_account_id=a.id "
					+ " left join security_principal p on m.id = p.entityinternalid "
					+ " left join v3x_org_department d  on m.org_department_id=d.id "
					+ " left join security_credential c on p.principal_id = c.principal_id "
					+ " where m.id not in (select id from s_midusers@DBLINK_OA_FOR_ARC)";

			pst = conn.prepareStatement(strsql);
			pst.execute();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			bresult = false;
		}
		/*
		 * finally { try { if (!pst.isClosed()) { pst.close(); } } catch
		 * (SQLException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } }
		 */

		System.out.println("同步OA用户结束>>>>>>>>>>>>");
		return Boolean.valueOf(bresult);
	}

	private Boolean onSynEdoc(Connection conn) {
		System.out.println("开始同步OA公文>>>>>>>>>>>>");
		boolean bresult = true;
		String strsql = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Statement st = null;
		ResultSet rs = null;
		try {
			System.out.println(sdf.format(new Date()) + " 开始执行 pro_number1   >>>>>>>>>>>>>>>>");

			// 1.--临时记录此次同步的公文信息表主键
			CallableStatement edoc_key = conn.prepareCall("{call pro_number1(?)}");
			edoc_key.setInt(1, 1);
			edoc_key.execute();

			// 2.--同步公文信息主表
			CallableStatement edoc_record = conn.prepareCall("{call pro_number1(?)}");
			edoc_record.setInt(1, 2);
			edoc_record.execute();

			// 3.同步公文信息正文表
			CallableStatement edoc_content = conn.prepareCall("{call pro_number1(?)}");
			edoc_content.setInt(1, 3);
			edoc_content.execute();

			// 4.同步公文附件
			CallableStatement edoc_attach = conn.prepareCall("{call pro_number1(?)}");
			edoc_attach.setInt(1, 4);
			edoc_attach.execute();

			/*
			 * strsql =
			 * "select a.id as id, b.id as ebodyid, a.subject as subject, c.Filename as filename, "
			 * + " substr(to_char(a.create_time, 'yyyy-mm-dd'), 0, 4) year, " +
			 * " substr(to_char(a.create_time, 'yyyy-mm-dd'), 6, 2) month, " +
			 * " substr(to_char(a.create_time, 'yyyy-mm-dd'), 9, 2) day" +
			 * " from edoc_summary a, edoc_body b, v3x_file C " +
			 * " where a.has_archive = 1 " + " and a.id = b.edoc_id" +
			 * " and to_char(b.content) = C.id" +
			 * " and a.id in (select id from TEMP_NUMBER1)";
			 */

			// 2015-06-11 modify
			strsql = "select a.id as id, a.subject as subject,"
					+ " substr(to_char(a.create_time, 'yyyy-mm-dd'), 0, 4) year, "
					+ " substr(to_char(a.create_time, 'yyyy-mm-dd'), 6, 2) month, "
					+ " substr(to_char(a.create_time, 'yyyy-mm-dd'), 9, 2) day" + " from edoc_summary a, edoc_body b"
					+ " where a.has_archive = 1" + " and a.id = b.edoc_id"
					+ " and a.id in (select id from TEMP_NUMBER1)";

			st = conn.createStatement();
			rs = st.executeQuery(strsql);

			DocumentFactory df = V3XLocator.getInstance().lookup(DocumentFactory.class);

			TransformerFactory tFactory = TransformerFactory.newInstance();

			String[] htmlContent = null;

			String sPath = "";

			while (rs.next()) {
				htmlContent = df.exportOfflineEdocModel(Long.parseLong(rs.getString("id")));

				// 创建一个xsl文件
				Transformer transformer = tFactory.newTransformer(new StreamSource(new StringReader(htmlContent[1])));

				// HTML文件输出路径
				sPath = "/upload/" + rs.getString("year") + File.separator + rs.getString("month") + File.separator
						+ rs.getString("day") + File.separator + rs.getString("id") + ".html";

				// 年文件夹创建
				if (!new File("/upload" + File.separator + rs.getString("year")).exists()
						&& !new File("/upload" + File.separator + rs.getString("year")).isDirectory()) {
					new File("/upload" + File.separator + rs.getString("year")).mkdir();
				}
				// 月文件夹创建
				if (!new File(
						"/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month"))
								.exists()
						&& !new File("/upload" + File.separator + rs.getString("year") + File.separator
								+ rs.getString("month")).isDirectory()) {
					new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month"))
							.mkdir();
				}
				// 日文件夹创建
				if (!new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month")
						+ File.separator + rs.getString("day")).exists()
						&& !new File("/upload" + File.separator + rs.getString("year") + File.separator
								+ rs.getString("month") + File.separator + rs.getString("day")).isDirectory()) {
					new File("/upload" + File.separator + rs.getString("year") + File.separator + rs.getString("month")
							+ File.separator + rs.getString("day")).mkdir();
				}

				if (!new File(sPath).exists()) {
					new File(sPath).createNewFile();
				}

				System.out.println("上传HTML文件路径>>>>>>" + sPath);

				// 将xml文件与xsl文件进行合并，然后输出为html文件
				transformer.transform(new StreamSource(new StringReader(htmlContent[0])),
						new StreamResult(new OutputStreamWriter(new FileOutputStream(sPath), "GBK")));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			bresult = false;
		}
		/*
		 * finally { try { if (!rs.isClosed()) { rs.close(); } if
		 * (!st.isClosed()) { st.close(); } } catch (SQLException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } }
		 */

		System.out.println("同步OA公文结束>>>>>>>>>>>>");
		return Boolean.valueOf(bresult);
	}

	// copy正文
	private void copyEdoc(Connection conn) {
		System.out.println("开始复制OA正文>>>>>>>>>>>>");
		String str = "";

		Statement st = null;
		ResultSet rs = null;

		try {
			str = " select '/upload/' || substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 0, 4) || '/' || "
					+ " substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 6, 2) || '/' || "
					+ " substr(to_char(C.Create_Date, 'yyyy-mm-dd'), 9, 2) || '/' || C.Filename || '.doc' as C_FTPFILEPATH "
					+ " from edoc_summary A left join (select * from edoc_body where content_type <> 'HTML') B on B.Edoc_Id = A.Id"
					+ " left join v3x_file C on to_char(B.content) = C.Id "
					+ " where a.has_archive = 1 and B.Id is not null " + " and a.id in (select id from TEMP_NUMBER1)";

			st = conn.createStatement();
			rs = st.executeQuery(str);

			String sPath = "";

			String sFilePath = "";

			while (rs.next()) {
				sPath = rs.getString("C_FTPFILEPATH");

				sFilePath = sPath.substring(0, sPath.lastIndexOf("."));

				if (new File(sFilePath).exists()) {
					FileUtil.copyFile(new File(sFilePath), new File(sPath));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		/*
		 * finally { try { if (!rs.isClosed()) { rs.close(); } if
		 * (!st.isClosed()) { st.close(); } } catch(Exception ex) {
		 * ex.printStackTrace(); } }
		 */

		System.out.println("开始复制OA正文结束>>>>>>>>>>>>");
	}

	// copy附件
	private void copyAttachment(Connection conn) {

		System.out.println("开始复制OA附件>>>>>>>>>>>>");
		String str = "";

		Statement st = null;
		ResultSet rs = null;

		try {
			str = " select '/upload/' || substr(to_char(C.createdate, 'yyyy-mm-dd'), 0, 4) || '/' || "
					+ " substr(to_char(C.createdate, 'yyyy-mm-dd'), 6, 2) || '/' || "
					+ " substr(to_char(C.createdate, 'yyyy-mm-dd'), 9, 2) || '/' || "
					+ " C.file_url || substr(C.Filename, instr(C.Filename, '.', -1, 1)) C_FTPFILEPATH  "
					+ " from edoc_summary A left join edoc_body  B on B.Edoc_Id = A.Id"
					+ " left join v3x_attachment C on b.Edoc_Id = c.reference "
					+ " where a.has_archive = 1 and C.id is not null " + " and a.id in (select id from TEMP_NUMBER1)";

			st = conn.createStatement();
			rs = st.executeQuery(str);

			String sPath = "";

			String sFilePath = "";

			while (rs.next()) {
				sPath = rs.getString("C_FTPFILEPATH");

				sFilePath = sPath.substring(0, sPath.lastIndexOf("."));

				if (new File(sFilePath).exists()) {
					FileUtil.copyFile(new File(sFilePath), new File(sPath));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		/*
		 * finally { try { if (!rs.isClosed()) { rs.close(); } if
		 * (!st.isClosed()) { st.close(); } } catch(Exception ex) {
		 * ex.printStackTrace(); } }
		 */

		System.out.println("开始复制OA附件结束>>>>>>>>>>>>");
	}

}