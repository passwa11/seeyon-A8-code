package com.ncbi.medhub.dao;

import com.ncbi.medhub.pojo.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ArticleDao extends JpaRepository<Article, Integer> {

    @Query("from Article where field0001 is null")
    List<Article> queryByPmidHql();

    @Query("update Article set field0001= ?1,field0002= ?2,field0003=?3,field0004=?4, " +
            "field0005=?5, field0006=?6,field0011=?7,field0037=?8,field0023=?9,field0007=?10,field0008=?11 where field0029 = ?12")
    @Modifying
    @Transactional
    void updateArticleByPMID(String p1, String p2, long p3, long p4, long p5, long p6, String p11, String p37, String p23,long p7,long p8, String id);
}
