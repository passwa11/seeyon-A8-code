package com.seeyon.v3x.edoc.webmodel;

import com.seeyon.v3x.edoc.domain.EdocRegisterCondition;

public class EdocRegisterConditionModel {

    private EdocRegisterCondition register;
    private EdocRegisterCondition relationRegister;
    public EdocRegisterCondition getRegister() {
        return register;
    }
    public void setRegister(EdocRegisterCondition register) {
        this.register = register;
    }
    public EdocRegisterCondition getRelationRegister() {
        return relationRegister;
    }
    public void setRelationRegister(EdocRegisterCondition relationRegister) {
        this.relationRegister = relationRegister;
    }
}
