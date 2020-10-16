package com.ncbi.medhub.service;

import com.ncbi.medhub.pojo.Article;

import java.util.List;

public interface ArticleService {

    List<Article> queryByPmidHql();

    void updateArticleByPMID(String p1, String p2, long p3, long p4, long p5, long p6, String p11, String p37, String p23,long p7,long p8, String id);

}
