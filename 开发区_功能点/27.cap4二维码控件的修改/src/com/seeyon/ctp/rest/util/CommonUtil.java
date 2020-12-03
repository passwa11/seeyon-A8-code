package com.seeyon.ctp.rest.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.songjian.utils.json.JSON;
import org.songjian.utils.json.JSONArray;
import org.songjian.utils.json.JSONObject;

import com.seeyon.cap4.template.util.HttpClientUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.FileManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.V3XFile;
import com.seeyon.ctp.util.DateUtil;

public class CommonUtil {
    private static final Log logger = CtpLogFactory.getLog(CommonUtil.class);
    public static String comName;

    public static Long id;

    public static Date date;

    public static String v;


    public static String post(String url, org.songjian.utils.json.JSONObject jsonObject) {
    	 Protocol myhttps = new Protocol("https", new MySSLSocketFactory(), 443);
         Protocol.registerProtocol("https", myhttps);
        @SuppressWarnings("resource")
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        String result = "";
        logger.info(jsonObject.toString());
        try {
            StringEntity s = new StringEntity(jsonObject.toString(), "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(s);
            // 发送请求
            HttpResponse httpResponse = client.execute(post);
            HttpEntity resEntity = httpResponse.getEntity();
            Header[] headers = httpResponse.getHeaders("content-disposition");
            if (headers.length > 0) {
                comName = URLDecoder.decode(((headers[0].getValue().split(";"))[1].split("="))[1], "UTF-8");
                date = DateUtil.currentDate();
                id = updateFile(MD5(comName + ".gif"), resEntity.getContent(), date);
                EntityUtils.consume(resEntity);
                return null;
            }
            // 获得返回来的信息，转化为字符串string
            String resString = EntityUtils.toString(resEntity);
            logger.info(resString);
            return resString;
        } catch (Exception e) {
            logger.error("请求异常" + e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            client.close();
        }
    }

    public static String uploadFileWithHttpMime(String uri, String data, File file) {
        // 1:创建一个httpclient对象
        org.apache.http.client.HttpClient httpclient = new DefaultHttpClient();
        Charset charset = Charset.forName("UTF-8");// 设置编码
        try {
            // 2：创建http的发送方式对象，是GET还是post
            HttpPost httppost = new HttpPost(uri);
            // 3：创建要发送的实体，就是key-value的这种结构，借助于这个类，可以实现文件和参数同时上传，很简单的。
            MultipartEntity reqEntity = new MultipartEntity();
            if (file != null) {
                FileBody bin = new FileBody(file);
                reqEntity.addPart("file", bin);
            }
            StringBody comment = new StringBody(data, charset);
            reqEntity.addPart("data", comment);
            httppost.setEntity(reqEntity);
            // 4：执行httppost对象，从而获得信息
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity resEntity = response.getEntity();
            Header[] headers = response.getHeaders("content-disposition");
            if (headers.length > 0) {
                comName = URLDecoder.decode(((headers[0].getValue().split(";"))[1].split("="))[1], "UTF-8");
                date = DateUtil.currentDate();
                if (comName.indexOf(".") <= 0) {
                    comName = comName + ".jpg";
                }
                id = updateFile(comName, resEntity.getContent(), date);
                EntityUtils.consume(resEntity);
                return null;
            }
            // 获得返回来的信息，转化为字符串string
            return EntityUtils.toString(resEntity);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalStateException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (UnsupportedOperationException e) {
            logger.error(e.getMessage(), e);
        } catch (BusinessException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
            } catch (Exception ignore) {
                logger.error(ignore.getMessage(), ignore);
            }
        }
        return "";
    }

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F'};

    /**
     * map参数计算签名：按照字典顺序对value拼装字符串，计算签名 需要对value进行URLEncoder处理
     *
     * @param retMap 返回Map
     * @param bizKey 应用系统secretKey
     */
    public static String getMd5B2bSign(Map<String, Object> retMap, String bizKey) {
        try {
            TreeMap<String, Object> treeMap = toTreeMap(retMap);
            StringBuilder sb = new StringBuilder();
            Set<Entry<String, Object>> entrySet = treeMap.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                if (entry.getValue() != null && !StringUtils.equals(entry.getKey(), "sign")) {
                    sb.append(URLEncoder.encode(entry.getValue().toString(), "utf-8"));
                }
            }
            String toSignData = sb.append(bizKey).toString();
            return MD5(toSignData);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * map转成TreeMap
     */
    private static TreeMap<String, Object> toTreeMap(Map<String, Object> map) {
        TreeMap<String, Object> newTreeMap = new TreeMap<String, Object>();
        Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> next = iterator.next();
            newTreeMap.put(next.getKey(), next.getValue());
        }
        return newTreeMap;
    }

    /**
     * 对字符串进行MD5签名
     *
     */
    public final static String MD5(String s) {
        try {
            byte[] btInput = s.getBytes("utf-8");
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            return getFormattedText(mdInst.digest());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        // 把密文转换成十六进制的字符串形式
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

    /**
     * json转换map. <br>
     * 详细说明
     *
     * @param jsonStr json字符串
     * @return Map 集合
     */
    public static Map<String, Object> parseJSON2Map(String jsonStr) {
        Map<String, Object> params = new HashMap<String, Object>();
        // 最外层解析
        JSONObject json = JSON.parseObject(jsonStr);
        for (String k : json.keySet()) {
            Object v = json.get(k);
            // 如果内层还是数组的话，继续解析
            if (v instanceof JSONArray) {
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                Iterator<Object> it = ((JSONArray) v).iterator();
                while (it.hasNext()) {
                    JSONObject json2 = JSON.parseObject(it.next().toString());
                    list.add(parseJSON2Map(json2.toString()));
                }
                params.put(k, list);
            } else {
                params.put(k, v);
            }
        }
        return params;
    }


    private static Long updateFile(String fileName, InputStream is, Date date) throws BusinessException {
        FileManager fileManager = (FileManager) AppContext.getBean("fileManager");
        V3XFile v3xfile = fileManager.save(is, ApplicationCategoryEnum.cap4Form, fileName, date, true);
        v = v3xfile.getV();
        return v3xfile.getId();
    }


    public static String doGet(String httpurl) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        String result = null;// 返回结果字符串
        try {
            // 创建远程url连接对象
            URL url = new URL(httpurl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            // 发送请求
            connection.connect();
            // 通过connection连接，获取输入流
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 封装输入流is，并指定字符集
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                // 存放数据
                StringBuilder sbf = new StringBuilder();
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }

            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            if (connection != null) {
                connection.disconnect();// 关闭远程连接
            }
        }

        return result;
    }

    public static Map<String, String> dozip(File zfile) throws IOException, BusinessException {
        ZipFile zip = null;
        Map<String, String> map = new HashMap<String, String>();
        try {
            zip = new ZipFile(zfile);
            ZipEntry entry;
            Enumeration<ZipEntry> enums = (Enumeration<ZipEntry>) zip.entries();
            while (enums.hasMoreElements()) {
                entry = enums.nextElement();
                date = DateUtil.currentDate();
                // 获取条目流

                Long id = updateFile(entry.getName(), zip.getInputStream(entry), date);
                map.put(id.toString(), entry.getName());
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
        return map;
    }

    public static String formUpload(String urlStr, File file, String contentType) throws IOException {
        JSONObject ret = HttpClientUtil.doFilePost(urlStr, file, null);
        return ret.toString();
    }

    /**
     * 将pdf中的maxPage页，转换成一张图片
     *
     * @param pdfFile pdf的路径
     */
    public static void pdf2multiImage(File pdfFile) {
        try {
            InputStream is = new FileInputStream(pdfFile);
            PDDocument pdf = PDDocument.load(is);
//            List<PDPage> pages = pdf.getDocumentCatalog().getAllPages();
//            List<BufferedImage> piclist = new ArrayList<BufferedImage>();
//            int actSize = pages.size(); // pdf中实际的页数
//            for (int i = 0; i < actSize; i++) {
//                piclist.add(pages.get(i).convertToImage());
//            }

            List<BufferedImage> piclist = new ArrayList<BufferedImage>();
            PDFRenderer pdfRenderer = new PDFRenderer(pdf);
            for (int page = 0; page < pdf.getNumberOfPages(); ++page)
            { 
            	piclist.add(pdfRenderer.renderImageWithDPI(page, 800, ImageType.RGB));
            }
            yPic(piclist);
            pdf.close();
            is.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 将宽度相同的图片，竖向追加在一起 ##注意：宽度必须相同
     *
     * @param piclist 文件流数组
     */
    public static void yPic(List<BufferedImage> piclist) {// 纵向处理图片
        if (piclist == null || piclist.size() <= 0) {
            logger.info("图片数组为空!");
            return;
        }
        try {
            int height = 0, // 总高度
                    width = 0, // 总宽度
                    _height = 0, // 临时的高度 , 或保存偏移高度
                    __height = 0, // 临时的高度，主要保存每个高度
                    picNum = piclist.size();// 图片的数量
            int[] heightArray = new int[picNum]; // 保存每个文件的高度
            BufferedImage buffer = null; // 保存图片流
            List<int[]> imgRGB = new ArrayList<int[]>(); // 保存所有的图片的RGB
            int[] _imgRGB; // 保存一张图片中的RGB数据
            for (int i = 0; i < picNum; i++) {
                buffer = piclist.get(i);
                heightArray[i] = _height = buffer.getHeight();// 图片高度
                if (i == 0) {
                    width = buffer.getWidth();// 图片宽度
                }
                height += _height; // 获取总高度
                _imgRGB = new int[width * _height];// 从图片中读取RGB
                _imgRGB = buffer.getRGB(0, 0, width, _height, _imgRGB, 0, width);
                imgRGB.add(_imgRGB);
            }
            _height = 0; // 设置偏移高度为0
            // 生成新图片
            BufferedImage imageResult = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < picNum; i++) {
                __height = heightArray[i];
                if (i != 0)
                    _height += __height; // 计算偏移高度
                imageResult.setRGB(0, _height, width, __height, imgRGB.get(i), 0, width); // 写入流中
            }
            comName = UUID.randomUUID().toString() + ".jpg";
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
            ImageIO.write(imageResult, "jpg", imOut);
            date = DateUtil.currentDate();
            id = updateFile(comName, new ByteArrayInputStream(bs.toByteArray()), date);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
