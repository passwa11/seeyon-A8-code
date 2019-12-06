package com.seeyon.ctp.rest.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.meetingSeat.manager.MeetingSeatManager;
import com.seeyon.apps.meetingSeat.manager.MeetingSeatManagerImpl;
import com.seeyon.apps.meetingSeat.po.Formson0256;
import com.seeyon.apps.meetingSeat.po.MeetingSeatPerson;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.AttachmentManager;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.Base64;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.UUIDLong;


@Path("cap4/meetingSeat")
@Consumes({"application/json"})
@Produces({"application/json"})
public class MeetingSeatResources extends BaseResource {

    private static final Log log = LogFactory.getLog(MeetingSeatResources.class);

    private static CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");

    private static CAP4FormDataManager cap4FormDataManager = (CAP4FormDataManager) AppContext
            .getBean("cap4FormDataManager");

    private MeetingSeatManager meetingSeatManager = new MeetingSeatManagerImpl();


    private static Random rand = new Random();

    @GET
    @Produces({"application/json"})
    @Path("getMeetingSeatPeople")
    public Response getMeetingSeatPeople(@QueryParam("meetingId") String meetingId) throws BusinessException {
        // meetingId = "HYBH201905004";
        if (null == meetingId || "".equals(meetingId)) {
            return this.fail("会议编号错误");
        }

        Map<String, Object> result = new HashMap<String, Object>();
        // 获取人员列表
        List<Map> personList = meetingSeatManager.getMeetingSeatPersonList(meetingId);
        // 获取未报送单位列表
        List<Map> depList = meetingSeatManager.getMeetingSeatDepNoList(meetingId);
        // 定义返回列表
        List<MeetingSeatPerson> list = new ArrayList<MeetingSeatPerson>();
        for (int i = 0; i < personList.size(); i++) {
            list.add(new MeetingSeatPerson(personList.get(i).get("field0034").toString(),
                    personList.get(i).get("field0035").toString(),
                    (null == personList.get(i).get("field0026")) ? null : personList.get(i).get("field0026").toString(),
                    (null == personList.get(i).get("field0025")) ? null
                            : personList.get(i).get("field0025").toString()));
        }
        List<MeetingSeatPerson> list1 = new ArrayList<MeetingSeatPerson>();
        for (int i = 0; i < depList.size(); i++) {
            //测试环境
            if (null != depList.get(i).get("field0040")) {
                list1.add(new MeetingSeatPerson(depList.get(i).get("field0040").toString(), "", null, null));
            }
            //正式环境
            if (null != depList.get(i).get("field0046")) {
                list1.add(new MeetingSeatPerson(depList.get(i).get("field0046").toString(), "", null, null));
            }

        }

        result.put("users", list);
        result.put("users1", list1);
        result.put("meetingId", meetingId);
        result.put("meetingRoomId", "002");
        result.put("rows", "16");
        result.put("cols", "16");
        return success(result);
    }

    @POST
    @Produces({"application/json"})
    @Path("saveMeetingSeatPeople")
    public Response saveMeetingSeatPeople(Map<String, Object> postMap) throws BusinessException {
        Map<String, Object> dataMap = (Map<String, Object>) postMap.get("data");
        String meetingId = (String) dataMap.get("meetingId");
        List<Map<String, Object>> personList = (List<Map<String, Object>>) dataMap.get("users");

        List<Map> depList = meetingSeatManager.getMeetingSeatDepNoList(meetingId);

        List<Map> countList = meetingSeatManager.getMeetingSeatPersonList(meetingId);
        long sort = countList.size();
        //long formmainId = Long.valueOf(countList.get(0).get("formmain_id").toString());
        Map<String, Object> result = new HashMap<String, Object>();

        try {

            List<Formson0256> formSonList = new ArrayList<>();
            for (int i = 0; i < personList.size(); i++) {
                //dep为空，说明是未报送单位排座，需要对未报送单位进行新增操作
                if ("" == personList.get(i).get("dep")) {

                    Formson0256 f0256 = new Formson0256();
                    f0256.setId(UUIDLong.longUUID());
                    //f0256.setFormmainId(formmainId);
                    f0256.setSort(++sort);
                    f0256.setField0008(sort);
                    f0256.setField0025(personList.get(i).get("row") == null ? null : personList.get(i).get("row").toString());
                    f0256.setField0026(personList.get(i).get("col") == null ? null : personList.get(i).get("col").toString());
                    f0256.setField0034(personList.get(i).get("name").toString());


                    //.f0256.setField0035(personList.get(i).get("name").toString());  //单位名称和人员名称一致


                    for (int j = 0; j < depList.size(); j++) {
                        //测试环境
                        if (null != depList.get(j).get("field0040")) {
                            if (personList.get(i).get("name").equals(depList.get(j).get("field0040"))) {
                                f0256.setFormmainId(Long.valueOf(depList.get(j).get("id").toString()));
                            }
                        }
                        //正式环境
                        if (null != depList.get(j).get("field0046")) {
                            if (personList.get(i).get("name").equals(depList.get(j).get("field0046"))) {
                                //int temp = (int) depList.get(j).get("id")+1;
                                f0256.setFormmainId(100000L);
                            }
                        }

                    }

                    formSonList.add(f0256);
                    //将单位未报送改为已报送
                    meetingSeatManager.modMeetingSeatDepStatus(meetingId, personList.get(i).get("name").toString());
                } else {
                    meetingSeatManager.SetMeetingSeatPerson(meetingId, personList.get(i).get("name").toString(),
                            personList.get(i).get("dep").toString(),
                            personList.get(i).get("col") == null ? null : personList.get(i).get("col").toString(),
                            personList.get(i).get("row") == null ? null : personList.get(i).get("row").toString());
                }
            }
            meetingSeatManager.SetMeetingSeatDep(formSonList);

            //personList如果为空，则清空所有排座信息
            if (0 == personList.size()) {

            }


            //显示图片
            Attachment tt = new Attachment();
            AttachmentManager nsdasd = (AttachmentManager) AppContext.getBean("attachmentManager");
            FileManager nan = (FileManager) AppContext.getBean("fileManager");
            dataMap.get("base64_pdf");
            V3XFile sdsd = saveBase64Img(dataMap.get("base64_png").toString(), null, null);
            Long[] fieldsLongs = {sdsd.getId()};
            String idStrings = nsdasd.create(fieldsLongs, ApplicationCategoryEnum.addressbook, -1l, -1l);

            tt = nsdasd.getAttachmentByFileURL(sdsd.getId());
            System.out.println("返回结果：" + tt.toString());
            result.put("state", "ok");
            result.put("createdate", Datetimes.format(tt.getCreatedate(), "yyyy-MM-dd HH:mm:ss"));
            result.put("fileUrl", String.valueOf(tt.getFileUrl()));
            result.put("id", String.valueOf(tt.getId()));
            result.put("filename", tt.getFilename());
            result.put("vcold", tt.getV());
            result.put("mimeType", tt.getMimeType());
            result.put("attachment", tt);
            return success(result);
        } catch (Exception e) {

            result.put("state", "error");
            return success(result);
        }
        // JsonKit.toJson(savePeople);

    }


    public V3XFile saveBase64Img(String base64Str, String fileName, Map<String, Object> param) throws BusinessException {
        User user = AppContext.getCurrentUser();
        Date createDate = new Date();
        String base64Body = null;
        String[] base64;
        String suffix;
        if (base64Str.startsWith("data:image/png")) {
            base64 = base64Str.split(",");
            suffix = base64[0];
            base64Body = base64[1];
            //给PDF文件起一个可爱的名字
            if (Strings.isBlank(fileName)) {
                String imgSuffix = suffix.replace("data:image/png;filename=", "").replace(";base64", "");
                fileName = Datetimes.format(createDate, "yyyyMMddHHmmss") + "_" + imgSuffix;
            }
        } else {
            base64Body = base64Str;
        }

        base64 = null;

        ByteArrayInputStream inputStream;
        try {
            byte[] b = Base64.decode(base64Body);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] = (byte) (b[i] + 256);
                }
            }

            inputStream = new ByteArrayInputStream(b);
        } catch (Exception var29) {
            log.error(var29.getLocalizedMessage(), var29);
            throw new BusinessException(var29);
        }


        FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
        V3XFile file = fileManager.save(inputStream, ApplicationCategoryEnum.addressbook, System.currentTimeMillis() + ".png", createDate, true);
        try {
            inputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return file;
    }


    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0L;

        int n;
        for (boolean var5 = false; -1 != (n = input.read(buffer)); count += (long) n) {
            output.write(buffer, 0, n);
        }

        return count;
    }


    public String getFolder(Date createDate, boolean createWhenNoExist) throws BusinessException {
        //return this.partitionManager.getFolder(createDate, createWhenNoExist);
        return null;
    }


}
