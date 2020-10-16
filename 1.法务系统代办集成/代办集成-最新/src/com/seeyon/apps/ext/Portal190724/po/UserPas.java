package com.seeyon.apps.ext.Portal190724.po;

public class UserPas {
    private String id;
    private String law_user;
    private String law_pas;
    private String fox_user;
    private String fox_pas;
    private String record_user;
    private String record_pas;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLaw_user() {
        return law_user;
    }

    public void setLaw_user(String lawUser) {
        law_user = lawUser;
    }

    public String getLaw_pas() {
        return law_pas;
    }

    public void setLaw_pas(String lawPas) {
        law_pas = lawPas;
    }

    public String getFox_user() {
        return fox_user;
    }

    public void setFox_user(String foxUser) {
        fox_user = foxUser;
    }

    public String getFox_pas() {
        return fox_pas;
    }

    public void setFox_pas(String foxPas) {
        fox_pas = foxPas;
    }

    public String getRecord_user() {
        return record_user;
    }

    public void setRecord_user(String recordUser) {
        record_user = recordUser;
    }

    public String getRecord_pas() {
        return record_pas;
    }

    public void setRecord_pas(String recordPas) {
        record_pas = recordPas;
    }


    public UserPas() {
        super();
        // TODO Auto-generated constructor stub
    }

    public UserPas(String id, String lawUser, String lawPas, String foxUser,
                   String foxPas, String recordUser, String recordPas) {
        super();
        this.id = id;
        law_user = lawUser;
        law_pas = lawPas;
        fox_user = foxUser;
        fox_pas = foxPas;
        record_user = recordUser;
        record_pas = recordPas;
    }

    @Override
    public String toString() {
        return "UserPas [fox_pas=" + fox_pas + ", fox_user=" + fox_user
                + ", id=" + id + ", law_pas=" + law_pas + ", law_user="
                + law_user + ", record_pas=" + record_pas + ", record_user="
                + record_user + "]";
    }


}
