package com.ncbi.medhub.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "formmain_0334")
public class Article implements Serializable {

    @Id
    @Column(name = "ID")
    private BigDecimal id;
    @Column(name = "FIELD0041")
    private Long field0041;
    @Column(name = "FIELD0001")
    private String field0001;//论文题目
    @Column(name = "FIELD0002")
    private String field0002;//期刊名称
    @Column(name = "FIELD0003")
    private Long field0003;//出版年
    @Column(name = "FIELD0004")
    private Long field0004;//出版月
    @Column(name = "FIELD0005")
    private Long field0005;//卷号
    @Column(name = "FIELD0006")
    private Long field0006;//期号
    @Column(name = "FIELD0007")
    private Long field0007;//起始页码
    @Column(name = "FIELD0008")
    private Long field0008;//截止页码
    @Column(name = "FIELD0009")
    private Long field0009;
    @Column(name = "FIELD0029")
    private String field0029;//PMID
    @Column(name = "FIELD0010")
    private String field0010;
    @Column(name = "FIELD0011")
    private String field0011;//JCR分区	 医学
    @Column(name = "FIELD0037")
    private String field0037;//ISSN
    @Column(name = "FIELD0023")
    private String field0023;//doi
    @Column(name = "FIELD0024")
    private Long field0024;
    @Column(name = "FIELD0025")
    private Long field0025;
    @Column(name = "FIELD0019")
    private String field0019;
    @Column(name = "FIELD0043")
    private Long field0043;

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", field0041=" + field0041 +
                ", field0001='" + field0001 + '\'' +
                ", field0002='" + field0002 + '\'' +
                ", field0003=" + field0003 +
                ", field0004=" + field0004 +
                ", field0005=" + field0005 +
                ", field0006=" + field0006 +
                ", field0007=" + field0007 +
                ", field0008=" + field0008 +
                ", field0009=" + field0009 +
                ", field0029='" + field0029 + '\'' +
                ", field0010='" + field0010 + '\'' +
                ", field0011='" + field0011 + '\'' +
                ", field0037='" + field0037 + '\'' +
                ", field0023='" + field0023 + '\'' +
                ", field0024=" + field0024 +
                ", field0025=" + field0025 +
                ", field0019='" + field0019 + '\'' +
                ", field0043=" + field0043 +
                '}';
    }
}
