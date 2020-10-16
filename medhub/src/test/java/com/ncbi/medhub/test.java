package com.ncbi.medhub;

import com.alibaba.fastjson.JSONObject;
import com.ncbi.medhub.util.HttpUtilTool;
import com.ncbi.medhub.util.MedXmlUtil;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class test {

    public static void main(String[] args) {
        String s="25460";
        String[] strings=s.split("-");
        for (int i = 0; i < strings.length; i++) {
            System.out.println(strings[i]);
        }
    }

    public static void main2(String[] args) throws DocumentException {
        String url = "C:\\Users\\Administrator\\Desktop\\1.xml";
        String attr = "DOI";
//        String attr="AuthorList";
//        MedXmlUtil.get(url, attr, "22455949");
        Map<String, Object> s = MedXmlUtil.get(url, attr, "22455949");
        AtomicBoolean b = (AtomicBoolean) s.get("type");
        if (b.get()) {
            System.out.println(s.get("reult"));
        } else {
            List<String> list = (List<String>) s.get("list");
            list.stream().forEach(str->{
                System.out.println(str);
            });
        }
//        main2();
    }

//    public static void main5(String[] args) throws DocumentException {
//        SAXReader saxReader = new SAXReader();
//        Document document = saxReader.read(new File("C:\\Users\\Administrator\\Desktop\\1.xml"));
//        Element root = document.getRootElement();
//        List<Element> elements = root.content();
//        elements.stream().forEach(n -> {
//            List<Element> list = n.content();
//            Iterator<Element> it = list.iterator();
//            while (it.hasNext()) {
//                Element e = it.next();
//                String key = e.attributeValue("Name");
//                if (null != key) {
//                    if (key.equals("AuthorList")) {
//                        List<Element> list1 = e.elements();
//                        for (int i = 0; i < list1.size(); i++) {
//                            Element element = list1.get(i);
//                            System.out.println(element.getTextTrim());
//                        }
//                    }
//                }
//            }
//            System.out.println("==============================================================");
//        });
//
//    }
//
//    public static void main4(String[] args) {
//        SAXReader saxReader = new SAXReader();
//        Document document;
//        try {
//            document = saxReader.read(new File("C:\\Users\\Administrator\\Desktop\\1.xml"));
//            Element root = document.getRootElement();
//            Element node = parse(root, "Name", "Author");
//            System.out.println(node.getTextTrim());
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//
//    public static Element parse(Element node, String type, String val) {
//        Element element = null;
//        for (Iterator iter = node.elementIterator(); iter.hasNext(); ) {
//            element = (Element) iter.next();
//            Attribute name = element.attribute(type);
//            if (name != null) {
//                String value = name.getValue();
//                if (value != null && val.equals(value)) {
//                    System.out.println(value);
//                    return element;
//                } else {
//                    parse(element, type, val);
//
//                }
//            }
//        }
//        return element;
//    }
//
//    public static void main2() throws DocumentException {
//        SAXReader saxReader = new SAXReader();
//        Document document = saxReader.read(new File("C:\\Users\\Administrator\\Desktop\\1.xml"));
//        Element root = document.getRootElement();
////        String path = "//eSummaryResult//DocSum//Item[@Name='Source']";
////        String path = "//eSummaryResult//DocSum//Item//Id";
////        List<Node> node = root.selectNodes(path);
////        for (int i = 0; i < node.size(); i++) {
////            System.out.println(node.get(i).getText());
////        }
//        List<Element> list = root.elements();
//        list.stream().forEach(n -> {
//            String t = n.element("Id").getTextTrim();
//            System.out.println(t);
////            if (t.equals("22455949")) {
////                Iterator<Element> iterator = n.elements().iterator();
////                while (iterator.hasNext()) {
////                    Element e = iterator.next();
////                    String ns = e.attributeValue("Name");
////                    if (ns.equals("AuthorList")) {
////                        Iterator<Element> ie = e.elements().iterator();
////                        while (ie.hasNext()) {
////                            Element et = iterator.next();
////                            System.out.println(et.getTextTrim());
////                        }
////                    }
////                }
////            }
//        });
////        for (Element element:list){
////            List<Node> node=element.selectNodes("//Item[@Name='PubDate']");
////            for (int i = 0; i < node.size(); i++) {
////                System.out.println(node.get(i).getText());
////            }
////        }
//    }
}
