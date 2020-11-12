package com.monkeyk.sos.util;

import com.monkeyk.sos.domain.user.User;
import com.monkeyk.sos.domain.user.UserRepository;
import com.seeyon.ctp.common.security.MessageEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.NoSuchAlgorithmException;

public class OaPasswordEncode implements PasswordEncoder {
    @Autowired
    private UserRepository userRepository;

    @Override
    public String encode(CharSequence charSequence) {
        MessageEncoder encoder = null;
        String pwd = "";
        try {
            encoder = new MessageEncoder();
            pwd = encoder.encode("admin", charSequence.toString());
            if (!(charSequence.equals("userNotFoundPassword")) && null != charSequence) {
                pwd = encoder.encode("admin", charSequence.toString());
            }
            System.out.println("========pwd=======" + pwd);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return pwd;
    }


    public String encode(CharSequence charSequence, String useranme) {
        MessageEncoder encoder = null;
        String pwd = "";
        try {

            encoder = new MessageEncoder();
            pwd = encoder.encode(useranme, charSequence.toString());
            if (!(charSequence.equals("userNotFoundPassword")) && null != charSequence) {
                pwd = encoder.encode(useranme, charSequence.toString());
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return pwd;
    }

    @Override
    public boolean matches(CharSequence charSequence, String secret) {
        String re = "";
        String re1 = "";
        boolean flag = false;
        User user = userRepository.findBySecret(secret);
        if (null != user) {
            System.out.println("================");
            System.out.println(charSequence.toString());
            //secret="8SLV1OmYxVcZPihpR40Utk8CYDU=";
            System.out.println("data_secret:" + secret);
            re = encode(charSequence, user.username());
            if (null != user.getRealsecret()) {
                re1 = encode(user.getRealsecret(), user.username());
            }
            System.out.println("new_secret:" + re);
        }
        if (secret.equals(re) || secret.equals(re1)) {
            flag = true;
        }
        return flag;
    }

    public static void main(String[] args) {
        OaPasswordEncode passwordEncoder = new OaPasswordEncode();
        System.out.println(passwordEncoder.encode("111111", "zhang"));
    }
}
