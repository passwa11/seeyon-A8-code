package com.ncbi.medhub.util;

import com.ncbi.medhub.pojo.Article;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MedXmlUtil {

    public static List<Article> readStringXml(String xml) {
        Document document = null;
        List<Article> articleList = new ArrayList<>();
        try {
            document = DocumentHelper.parseText(xml);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            elements.stream().forEach(n -> {
                String id = n.element("Id").getTextTrim();
                Article article = new Article();
                article.setField0029(id);
                if (1 == 1) {
                    List<Element> list = n.elements();
                    Iterator<Element> it = list.iterator();
                    while (it.hasNext()) {
                        Element e = it.next();
                        String key = e.attributeValue("Name");
                        if (null != key) {
                            if (key.equals("Title")) {
                                article.setField0001(e.getTextTrim());
                            }
                            if (key.equals("FullJournalName")) {
                                article.setField0002(e.getTextTrim());
                            }
                            if (key.equals("EPubDate")) {
                                LocalDate localDate = DateFormater.revokeString(e.getTextTrim());
                                article.setField0003(Long.parseLong(localDate.getYear() + ""));
                                article.setField0004(Long.parseLong(localDate.getMonth().getValue() + ""));
                            }
                            if (key.equals("Volume")) {
                                article.setField0005(Long.parseLong(e.getTextTrim()));
                            }
                            if (key.equals("Issue")) {//期号
                                article.setField0006(Long.parseLong((e.getTextTrim()).equals("") ? "0" : e.getTextTrim()));
                            }
                            if (key.equals("ISSN")) {
                                article.setField0037(e.getTextTrim());
                            }
                            if (key.equals("DOI")) {
                                article.setField0037(e.getTextTrim());
                            }
                            if(key.equals("Pages")){
                                String[] arr=e.getTextTrim().split("-");
                                article.setField0007(Long.parseLong(arr[0]));
                                if(arr.length>1){
                                    article.setField0008(Long.parseLong(arr[0])+Long.parseLong(arr[1]));
                                }else {
                                    article.setField0008(Long.parseLong(arr[0]));
                                }
                            }
                            article.setField0011("医学");
                        }
                    }
                }
                articleList.add(article);
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            System.gc();
        }
        return articleList;
    }

    public static Map<String, Object> readStringXml(String xml, String attrValue, String Id) {
        Document document = null;
        try {
            document = DocumentHelper.parseText(xml);
            Element root = document.getRootElement();
            List<Element> elements = root.elements();
            AtomicReference<String> result = new AtomicReference<>("");
            List<String> resultList = new ArrayList<>();
            AtomicBoolean type = new AtomicBoolean(false);
            elements.stream().forEach(n -> {
                String id = n.element("Id").getTextTrim();
                if (id.equals(Id)) {
                    List<Element> list = n.elements();
                    Iterator<Element> it = list.iterator();
                    while (it.hasNext()) {
                        Element e = it.next();
                        String key = e.attributeValue("Name");
                        if (null != key) {
                            if (key.equals(attrValue)) {
                                List<Element> list1 = e.elements();
                                if (null == list1 || list1.size() == 0) {
                                    result.set(e.getTextTrim());
                                    type.set(true);
                                } else {
                                    list1.stream().forEach(element -> {
                                        resultList.add(element.getTextTrim());
                                    });
                                }
                            }
                        }
                    }
                }
            });
            Map<String, Object> map = new HashMap<>();
            map.put("type", type);
            map.put("result", result);
            map.put("list", resultList);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> get(String url, String attrValue, String Id) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(url));
        Element root = document.getRootElement();
        List<Element> elements = root.elements();
        AtomicReference<String> result = new AtomicReference<>("");
        List<String> resultList = new ArrayList<>();
        AtomicBoolean type = new AtomicBoolean(false);
        elements.stream().forEach(n -> {
            String id = n.element("Id").getTextTrim();
            if (id.equals(Id)) {
                List<Element> list = n.elements();
                Iterator<Element> it = list.iterator();
                while (it.hasNext()) {
                    Element e = it.next();
                    String key = e.attributeValue("Name");
                    if (null != key) {
                        if (key.equals(attrValue)) {
                            List<Element> list1 = e.elements();
                            if (null == list1 || list1.size() == 0) {
//                                System.out.println(e.getTextTrim());
                                result.set(e.getTextTrim());
                                type.set(true);
                            } else {
                                list1.stream().forEach(element -> {
                                    resultList.add(element.getTextTrim());
//                                    System.out.println(element.getTextTrim());
                                });
                            }
                        }
                    }
                }
            }
        });
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("reult", result);
        map.put("list", resultList);
        return map;
    }
}
