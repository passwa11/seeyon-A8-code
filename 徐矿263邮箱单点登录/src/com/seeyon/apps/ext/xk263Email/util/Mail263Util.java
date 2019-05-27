package com.seeyon.apps.ext.xk263Email.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

//import net._263.macom.axis.xmapi.XmapiImpl;
//import net._263.macom.axis.xmapi.XmapiImplServiceLocator;
//
//import org.apache.commons.mail.EmailException;
//
//import com.cnofe.mail.entity.DepartmentList;
//import com.cnofe.mail.entity.User;
//import com.cnofe.mail.jaxb.JAXBUtil;
//import com.cnofe.portal.utils.Base64;
//import com.cnofe.portal.utils.Mail;
//import com.cnofe.sso.utils.SSOMD5;
//import com.cnofe.sys.entity.SysOrgEntity;
//import com.cnofe.sys.entity.SysUserEntity;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

public class Mail263Util {

    public static ExecutorService pool = Executors.newCachedThreadPool();
//	public static XmapiImplServiceLocator service = new XmapiImplServiceLocator();

    /**
     * 263授权单点登录sid和key
     */
    public static String SSO_CID = "ff80808150fbc5b2015124367cc103ba";
    public static String SSO_KEY = "4fW3H8my2cBeE";

    /**
     * 263授权人员组织account和key
     */
    public static String API_ACCOUNT = "xkjt.net";
    public static String API_KEY = "R5hy7M2dcv4AK";

    public static String MAIL_ADMIN = "admin";
    public static String defaultMailFix = "@xkjt.net";
    public static String MAIL_DOMAIN = "xkjt.net";
    public static String MAIL_DOMAIN_ID = "284581";

    // 用户启用禁用操作标志
    public static int MAIL_USER_ENABLE = 1;
    public static int MAIL_USER_DISABLE = 0;

    // 用户邮箱参数
    // 未分配部门ID -1
    public static int DEPT_ID_NO = -1;
    // 默认密码
    public static String USER_PWD = "123";
    // 密码类型 密码的明文:0 密码的32位MD5小写加密串:4
    public static int USER_CRYPTTYPE_0 = 0;
    public static int USER_CRYPTTYPE_4 = 4;
    // 组ID（空间大小ID），263G-5万封:33 263G-10万封:43
    public static int USER_GID_33 = 33;
    public static int USER_GID_43 = 43;
    // 角色ID，默认0
    public static int USER_ROLE_ID = 0;
    // 首次登录是否需要修改密码，不能为空，0为不需修改，1为需要修改
    public static int USER_CHANGEPWD_OFF = 0;
    public static int USER_CHANGEPWD_ON = 1;

    // 操作标志（用户，部门通用）
    public static int MAIL_PARTY_DEL = 1;
    public static int MAIL_PARTY_ADD = 2;
    public static int MAIL_PARTY_EDIT = 3;
//	public static int MAIL_PARTY_BACK = 4;

    /** 邮件接口调用 ===↓↓↓S↓↓↓======================== */

    /**
     * 获取单点登录URL
     * 单点登录接口为HTTP链接形式，根据接口要求提供正确参数，
     * 访问如下链接可以直接登录263 Web Mail，显示指定用户的邮箱
     * http://pcc.263.net/PCC/263mail.do?cid=单点登录接口账号&domain=邮箱域名&uid=用户ID&sign=加密标识
     * sign = 32位MD5 （ cid=单点登录接口账号&domain=邮箱域名&uid=用户ID&key=单点登录接口密钥 ）
     *
     * @return
     */
    public static String getSSOUrl(String alias) throws Exception {
        StringBuffer sb = new StringBuffer("http://pcc.263.net/PCC/263mail.do?");
        sb.append("cid=");
        sb.append(SSO_CID);
        sb.append("&domain=");
        sb.append(MAIL_DOMAIN);
        sb.append("&uid=");
        sb.append(alias);
        sb.append("&sign=");
        sb.append(sign("cid=" + SSO_CID, "&domain=" + MAIL_DOMAIN, "&uid=" + alias, "&key=" + SSO_KEY));
        return sb.toString();
    }

    /**
     * 生成调用API时的sign
     * 参数根据sign不同会有不同的意义，此处不说明
     *
     * @param args
     * @return
     */
    public static String sign(String... args) {
//        if (args != null && args.length > 0) {
//            StringBuffer signsb = new StringBuffer("");
//            for (String arg : args) {
//                signsb.append(arg);
//            }
//            String sign = SSOMD5.createEncrypPassword(signsb.toString());
//            return sign.toLowerCase();
//        }
        return null;
    }



    /**
     * 获取个人定制未读邮件（非系统邮箱）
     * @param user
     * @param password
     * @param imapStr
     * @return
     * @throws Exception
     */
//	public static List<Mail> getOutNewMailListByUserMail(String user, String password, String imapStr) throws Exception {
//		List<Mail> newMailList = new ArrayList<Mail>();
//
//		Properties prop = System.getProperties();
//		prop.put("mail.store.protocol", "imap");
//		prop.put("mail.imap.host", imapStr);
//		Session session = Session.getInstance(prop);
//		IMAPStore store = (IMAPStore) session.getStore("imap"); // 使用imap会话机制，连接服务器
//		store.connect(user, password);
//		IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX"); // 收件箱
//		folder.open(Folder.READ_WRITE);
//
//		// 得到收件箱文件夹信息，获取邮件列表
//		// System.out.println("未读邮件数：" + folder.getUnreadMessageCount());
//		Message[] messages = folder.getMessages();
//
//		for (int i = 0; i < messages.length; i++) {
//			Message message = messages[i];
//			Flags flags = message.getFlags();
//			if (flags.contains(Flags.Flag.SEEN)) {
//				// System.out.println("这是一封已读邮件");
//			} else {
//				String url = "http://mail" + imapStr.substring(imapStr.indexOf("."));
//				Mail mail = new Mail();
//				mail.setSubject(message.getSubject());
//				mail.setUrl(url);
//				newMailList.add(mail);
//			}
//		}
//		List<Mail> newMailList1 = new ArrayList<Mail>();
//
//		// 取后5条未读邮件，因为没有排序功能，故人工排序list
//		int size = newMailList.size();
//		int start = 0;
//		if (size > 5) {
//			start = size - 5;
//		}
//		for (int i = 0; size > i; size--) {
//			if (start == size) {
//				break;
//			}
//			newMailList1.add(newMailList.get(size - 1));
//		}
//		// 释放资源
//		if (folder != null) {
//			folder.close(true);
//		}
//		if (store != null) {
//			store.close();
//		}
//
//		return newMailList1;
//	}

    /**
     * 获取个人定制未读邮件数量（非系统邮箱）
     *
     * @param user
     * @param password
     * @param imapStr
     * @return
     * @throws Exception
     */
    public static String getOutMailCountByUserMail(
            String user, String password, String imapStr) throws Exception {
        String count = "0";
        try {
            Properties prop = System.getProperties();
            prop.put("mail.store.protocol", "imap");
            prop.put("mail.imap.host", imapStr);
            prop.setProperty("mail.imap.port", "143");
            Session session = Session.getInstance(prop);
            IMAPStore store = (IMAPStore) session.getStore("imap"); // 使用imap会话机制，连接服务器
            store.connect(user, password);
            IMAPFolder folder = (IMAPFolder) store.getFolder("INBOX"); // 收件箱
            folder.open(Folder.READ_WRITE);

            count = String.valueOf(folder.getUnreadMessageCount());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * 获取未读邮件数量
     *
     * @param loginName
     * @return
     * @throws Exception
     */
//    public static String getUnReadCount(String loginName) throws Exception {
//        XmapiImpl apiImpl = service.getxmapi();
//        /**
//         * userid string 用户ID（不包含@和域名），不能为空，最大长度为20字节
//         * domain string 域名，不能为空，最大长度为40字节
//         * passwd string 用户密码，为兼容旧版本而保留的参数，可传递任意值
//         * crypttype int 密码类型，为兼容旧版本而保留的参数，可传递任意值
//         * account string 接口账号
//         * sign string 加密标识	 sign = 32位MD5小写 （ userid + domain + 接口密钥 ）   /* 成功返回值+ 成功返回值符串连接 * /
//         */
//        String sign = sign(loginName, MAIL_DOMAIN, API_KEY);
//        String count = apiImpl.getDirInfo_New(loginName, MAIL_DOMAIN, "", 0, API_ACCOUNT, sign);
//        try {
//            int code = Integer.parseInt(count);
//            if (code < 0) {
//                count = "0";
//            }
//        } catch (Exception e) {
//            count = "0";
//        }
//
//        return count;
//    }
/** 邮件接口调用 ===↑↑↑E↑↑↑======================== */

/** 用户相关接口调用 ===↓↓↓S↓↓↓======================== */
    /**
     * 获取成员资料
     *
     * @param userid
     * @return
     */
//    public static User getUser(String userid) throws Exception {
//        XmapiImpl apiImpl = service.getxmapi();
//
//        /**
//         * userid 用户ID（不包含@和域名），不能为空，最大长度为20字节
//         * domain 域名，不能为空，最大长度为40字节
//         * account 接口账号
//         * sign 加密标识 sign = 32位MD5小写 （userid + domain + 接口账号 + 接口密钥 ）   /* 等式中的“+”号表示字符串连接 * /
//         */
//        String sign = sign(userid, MAIL_DOMAIN, API_ACCOUNT, API_KEY);
//        String userInfo = apiImpl.getUserInfo_New(userid, MAIL_DOMAIN, API_ACCOUNT, sign);
//
//        // 获取不到应该就是系统错误
//        if ("-5".equals(userInfo) || "-7".equals(userInfo)) {
//            return null;
//        }
//        return JAXBUtil.xmlStrToObject(userInfo, User.class);
//    }

    /**
     * 禁用用户帐户
     *
     * @param userLoginName
     * @return
     * @throws Exception
     */
//    public static synchronized void userDisabled(String userid) throws Exception {
//        pool.execute(new UserDisabedThread(userid));
//    }

    /**
     * 启用用户帐号
     *
     * @param userLoginName
     * @return
     * @throws Exception
     */
//    public static synchronized void userEnabled(String userLoginName) throws Exception {
//        pool.execute(new UserEnabledThread(userLoginName));
//    }




    /**
     * 获取单点登录URL(手机端不需要https)
     *
     * @return
     */
//    public static String getSSOUrlForMobile(String alias) throws Exception {
//        return getSSOUrl(alias);
//    }

    /**
     * 同步用户
     *
     * @param user
     * @return
     * @throws Exception
     */
//    public static String synUser(int action, SysUserEntity user) throws Exception {
//        pool.execute(new UserSynThread(action, user));
//        return user.getUserLoginName() + defaultMailFix;
//    }

/** 用户相关接口调用 ===↑↑↑E↑↑↑======================== */

/** 部门相关接口调用 ===↓↓↓S↓↓↓======================== */
    /**
     * 同步组织
     *
     * @param action 1:删除2：添加3：编辑
     * @param org
     * @throws Exception
     */
//    public static void synOrg(int action, SysOrgEntity org) {
//        pool.execute(new OrgSynThread(action, org));
//    }

    /**
     * 获取所有部门列表
     *
     * @return
     * @throws Exception
     */
//    public static DepartmentList getDeptList(String userid) throws Exception {
//        XmapiImpl apiImpl = service.getxmapi();
//
//        /**
//         * userid 用户ID（不包含@和域名），不能为空，最大长度为20字节
//         * domain 域名，不能为空，最大长度为40字节
//         * account 接口账号
//         * sign 加密标识 sign = 32位MD5小写 （userid + domain + 接口账号 + 接口密钥 ）   /* 等式中的“+”号表示字符串连接 * /
//         */
//        String sign = sign(userid, MAIL_DOMAIN, API_ACCOUNT, API_KEY);
//        String depts = apiImpl.getDepartment(userid, MAIL_DOMAIN, API_ACCOUNT, sign);
//
//        return JAXBUtil.xmlStrToObject(depts, DepartmentList.class);
//    }
/** 部门相关接口调用 ===↑↑↑E↑↑↑======================== */

/** 通用方法 ===↓↓↓S↓↓↓======================== */
    /**
     * 验证api返回内容是否包含错误信息,如果是错误返回项的一种，抛出错误
     *
     * @param result
     * @throws Exception
     */
//    public void checkResult(String result) throws Exception {
//        /**
//         * 返回值	返回值类型 	说明
//         * -5	int	系统错误
//         * -7	int	用户不存在
//         * -8	int	域名不存在
//         * -267	int	用户名或密码错误
//         * -303	int	用户已存在（创建用户失败时）
//         * -401	int	用户不存在
//         * -501	int	由于当前用户数或邮箱空间已超出限制，不能添加！
//         * -502	int	验证身份错误
//         * -503	int	部门已存在（创建部门）
//         * -1025	int	权限错误
//         * -3500	int	参数错误
//         * -4602	int	超出域总空间限制（创建用户失败时）
//         */
//        try {
//            int code = Integer.parseInt(result);
//            String msg = "";
//            switch (code) {
//                case -5:
//                    msg = "系统错误 ";
//                    break;
//                case -7:
//                    msg = "用户不存在 ";
//                    break;
//                case -8:
//                    msg = "域名不存在 ";
//                    break;
//                case -267:
//                    msg = "用户名或密码错误 ";
//                    break;
//                case -303:
//                    msg = "用户已存在（创建用户失败时） ";
//                    break;
//                case -401:
//                    msg = "用户不存在 ";
//                    break;
//                case -501:
//                    msg = "由于当前用户数或邮箱空间已超出限制，不能添加！ ";
//                    break;
//                case -502:
//                    msg = "验证身份错误 ";
//                    break;
//                case -503:
//                    msg = "部门已存在（创建部门）";
//                    break;
//                case -1025:
//                    msg = "权限错误 ";
//                    break;
//                case -3500:
//                    msg = "参数错误 ";
//                    break;
//                case -4602:
//                    msg = "超出域总空间限制（创建用户失败时） ";
//                    break;
//                default:
//                    break;
//            }
//            if (!"".equals(msg)) {
//                throw new Exception(msg);
//            }
//
//        } catch (Exception e) {
//            // 转换失败表示不是错误消息，忽略
//        }
//    }

    /**
     * 发送邮件
     *
     * @param subject
     * @param message
     */
//    public static void sendMail(String subject, String message) {
//        SendMailUtil email = new SendMailUtil("smtp.263.net", 25, "songxy@xkjt.net", "xkjt2015");
//        try {
//            email.sendSimpleMail("songxy@xkjt.net", "admin@xkjt.net", subject, message);
//        } catch (EmailException e) {
//            e.printStackTrace();
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * 将字符串转换成gbk编码后用Base64编码
     *
     * @param s
     * @return
     * @throws Exception
     */
//    public static String GBKToBase64(String s) throws Exception {
//        if (s == null) {
//            return "";
//        }
//        String base64 = Base64.encode(s.getBytes("GBK"));
//        return base64;
//    }



/** 通用方法 ===↑↑↑E↑↑↑======================== */
}
