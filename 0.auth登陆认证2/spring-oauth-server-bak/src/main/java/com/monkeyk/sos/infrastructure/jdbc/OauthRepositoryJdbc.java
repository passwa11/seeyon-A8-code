/*
 * Copyright (c) 2015 MONKEYK Information Technology Co. Ltd
 * www.monkeyk.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * MONKEYK Information Technology Co. Ltd ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you
 * entered into with MONKEYK Information Technology Co. Ltd.
 */
package com.monkeyk.sos.infrastructure.jdbc;

import com.monkeyk.sos.domain.oauth.OauthClientDetails;
import com.monkeyk.sos.domain.oauth.OauthRepository;
import com.monkeyk.sos.util.OaPasswordEncode;
import com.seeyon.ctp.common.security.MessageEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * 2015/11/16
 *
 * @author Shengzhao Li
 */
@Repository("oauthRepositoryJdbc")
public class OauthRepositoryJdbc implements OauthRepository {


    private static OauthClientDetailsRowMapper oauthClientDetailsRowMapper = new OauthClientDetailsRowMapper();


    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public OauthClientDetails findOauthClientDetails(String clientId) {
        final String sql = " select * from oauth_client_details where  client_id = ? ";
        final List<OauthClientDetails> list = this.jdbcTemplate.query(sql, new Object[]{clientId}, oauthClientDetailsRowMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<OauthClientDetails> findAllOauthClientDetails() {
        final String sql = " select * from oauth_client_details where archived = 0  order by create_time desc ";
        return this.jdbcTemplate.query(sql, oauthClientDetailsRowMapper);
    }

    @Override
    public void updateOauthClientDetailsArchive(String clientId, boolean archive) {
//        final String sql = " update oauth_client_details set archived = ? where client_id = ? ";
        final String sql = " delete from oauth_client_details  where client_id = ? ";
        this.jdbcTemplate.update(sql, clientId);
    }

    @Override
    public void updateClientDetails(OauthClientDetails clientDetails) {
        final String sql = " update oauth_client_details set client_name=?,web_server_redirect_uri=?,access_token_validity=?,refresh_token_validity=?  where client_id = ? ";
        this.jdbcTemplate.update(sql, clientDetails.getClientName(), clientDetails.getWebServerRedirectUri(),clientDetails.getAccessTokenValidity(),clientDetails.getRefreshTokenValidity(), clientDetails.getClientId());
    }

    @Override
    public void deleteClient(String clientId) {
        final String sql = "delete from oauth_client_details where client_id=?";
        this.jdbcTemplate.update(sql, clientId);
    }

    @Override
    public void saveOauthClientDetails(final OauthClientDetails clientDetails) {
        final String sql = " insert into oauth_client_details(client_id,resource_ids,client_secret,scope,authorized_grant_types,web_server_redirect_uri," +
                " authorities,access_token_validity,refresh_token_validity,additional_information,trusted,autoapprove,realsecret,client_name) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        this.jdbcTemplate.update(sql, ps -> {
            ps.setString(1, clientDetails.clientId());
            ps.setString(2, clientDetails.resourceIds());

            MessageEncoder encoder = null;
            String password = "";
            try {
                encoder = new MessageEncoder();
                password = encoder.encode(clientDetails.clientId(), clientDetails.clientSecret());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }//密码加密
            ps.setString(3, password);

            ps.setString(4, clientDetails.scope());

            ps.setString(5, clientDetails.authorizedGrantTypes());
            ps.setString(6, clientDetails.webServerRedirectUri());

            ps.setString(7, clientDetails.authorities());
            ps.setObject(8, clientDetails.accessTokenValidity());

            ps.setObject(9, clientDetails.refreshTokenValidity());
            ps.setString(10, clientDetails.additionalInformation());

            ps.setBoolean(11, clientDetails.trusted());
            ps.setString(12, clientDetails.autoApprove());
            ps.setString(13, clientDetails.clientSecret());
            ps.setString(14, clientDetails.clientName());

        });
    }
}
