package com.ncbi.medhub.quartz;

import com.ncbi.medhub.http.HttpApiService;
import com.ncbi.medhub.pojo.Article;
import com.ncbi.medhub.service.ArticleService;
import com.ncbi.medhub.util.MedXmlUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedhubJob extends QuartzJobBean {
    @Autowired
    private ArticleService articleService;
    @Value("${ncbi.medhub.accountKey}")
    private String medHubKey;
    @Autowired
    private HttpApiService apiService;

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("进来了?");
        List<Article> list = articleService.queryByPmidHql();
        List<String> strings = new ArrayList<>();
        list.stream().forEach(l -> {
            strings.add(l.getField0029());
        });
        if(strings.size()>0){
            String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi";
            Map<String, Object> map = new HashMap<>();
            map.put("db", "pubmed");
            map.put("api_key", medHubKey);
            map.put("id", StringUtils.join(strings, ","));
            String xml = apiService.doHttpsGet(url, map);
            List<Article> articleList= MedXmlUtil.readStringXml(xml);
            articleList.stream().forEach(ar -> {
                articleService.updateArticleByPMID(ar.getField0001(),ar.getField0002(),ar.getField0003(),ar.getField0004()
                        ,ar.getField0005(),ar.getField0006(),ar.getField0011(),ar.getField0037(),ar.getField0023(),
                        ar.getField0007(),ar.getField0008(),ar.getField0029());
            });
        }

    }


}
