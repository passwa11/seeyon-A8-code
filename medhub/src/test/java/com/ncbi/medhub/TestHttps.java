package com.ncbi.medhub;

import com.ncbi.medhub.http.HttpApiService;
import com.ncbi.medhub.pojo.Article;
import com.ncbi.medhub.service.ArticleService;
import com.ncbi.medhub.service.impl.ArticleServiceImpl;
import com.ncbi.medhub.util.DateFormater;
import com.ncbi.medhub.util.MedXmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import javax.swing.text.Document;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestHttps {
    public static void main3(String[] args) {
        DateFormater.MonthEnum m = DateFormater.MonthEnum.Apr;
//        System.out.println(m);
        String s=null;
        switch (m){
            case Apr:
                System.out.println("4");
                s="4";
                break;
        }
        System.out.println(s);
    }

    public static void main(String[] args) {
        String pdate="2012 Jun 15";
        LocalDate localDate=DateFormater.revokeString(pdate);
        System.out.println(localDate.getMonth().getValue());
        System.out.println(localDate.getYear());
    }


    public static void mainw(String[] args) throws Exception {
        HttpApiService service = new HttpApiService();
        String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=31708269,22455949,22704907,22761125&api_key=027815c5eb8d38e87b63f9b305aebc748a09";
        String result = service.doHttpsGet(url);
        System.out.println(result);
//        String attr = "DOI";
//        String attr="AuthorList";
//        Map<String, Object> s = MedXmlUtil.readStringXml(result, attr, "22455949");
//        AtomicBoolean b = (AtomicBoolean) s.get("type");
//        if (b.get()) {
//            System.out.println(s.get("result"));
//        } else {
//            List<String> list = (List<String>) s.get("list");
//            list.stream().forEach(str->{
//                System.out.println(str);
//            });
//        }
        List<Article> list = MedXmlUtil.readStringXml(result);
        list.stream().forEach(n->{
            String title=n.getField0001();
            System.out.println(title);
        });
    }
}
