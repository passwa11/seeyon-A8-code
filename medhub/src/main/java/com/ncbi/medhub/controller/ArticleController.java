package com.ncbi.medhub.controller;

import com.ncbi.medhub.http.HttpApiService;
import com.ncbi.medhub.pojo.Article;
import com.ncbi.medhub.service.ArticleService;
import com.ncbi.medhub.util.MedXmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@RestController
@RequestMapping("/ar")
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    @Value("${ncbi.medhub.accountKey}")
    private String medHubKey;
    @Autowired
    private HttpApiService apiService;

    @RequestMapping(value = "/index")
    public ModelAndView index(HttpServletRequest request)  {
        ModelAndView modelAndView=new ModelAndView();
        modelAndView.setViewName("view/index");
        try {
            System.out.println("进来了?");
            List<Article> list = articleService.queryByPmidHql();
            List<String> strings = new ArrayList<>();
            list.stream().forEach(l -> {
                strings.add(l.getField0029());
            });
            if(strings.size()>0){
                String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi";
                Map<String, Object> map = new ConcurrentHashMap<>();
                map.put("db", "pubmed");
                map.put("api_key", medHubKey);
                map.put("id", StringUtils.join(strings, ","));
                String xml = apiService.doHttpsGet(url, map);
                List<Article> articleList = MedXmlUtil.readStringXml(xml);
                articleList.stream().forEach(ar -> {
                    articleService.updateArticleByPMID(ar.getField0001(), ar.getField0002(), ar.getField0003(), ar.getField0004()
                            , ar.getField0005(), ar.getField0006(), ar.getField0011(), ar.getField0037(), ar.getField0023(),
                            ar.getField0007(),ar.getField0008(),ar.getField0029());
                });
            }
            request.setAttribute("info","1");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("info","-1");
        }
        return modelAndView;
    }
}
