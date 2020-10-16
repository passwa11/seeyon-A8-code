package com.ncbi.medhub.service.impl;

import com.ncbi.medhub.dao.ArticleDao;
import com.ncbi.medhub.pojo.Article;
import com.ncbi.medhub.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleDao articleDao;

    @Override
    public List<Article> queryByPmidHql() {
        return articleDao.queryByPmidHql();
    }

    @Override
    @Transactional
    public void updateArticleByPMID(String p1, String p2, long p3, long p4, long p5, long p6, String p11, String p37, String p23, long p7, long p8, String id) {
        articleDao.updateArticleByPMID(p1, p2, p3, p4, p5, p6, p11, p37, p23, p7, p8, id);
    }
}
