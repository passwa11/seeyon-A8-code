/**
 * 
 */
package com.seeyon.v3x.exchange.manager;

import java.util.Date;
import java.util.List;

import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.selectpeople.manager.AbstractSelectPeoplePanel;
import com.seeyon.v3x.exchange.domain.ExchangeAccount;

/**
 * @author <a href="tanmf@seeyon.com">Tanmf</a>
 * @date 2012-11-15 
 */
public class SelectPeoplePanel4ExchangeAccountImpl extends AbstractSelectPeoplePanel {
    
    private ExchangeAccountManager exchangeAccountManager;
    
    public void setExchangeAccountManager(ExchangeAccountManager exchangeAccountManager) {
        this.exchangeAccountManager = exchangeAccountManager;
    }

//    @Override
//    public String getJsonString(long memberId, long loginAccountId) throws BusinessException {
//        StringBuilder a = new StringBuilder();
//        a.append("[");
//        int i = 0;
//        List<ExchangeAccount> eas = this.exchangeAccountManager.getExternalAccounts(loginAccountId);
//        for (ExchangeAccount t : eas) {
//            if(i++ != 0){
//                a.append(",");
//            }
//            t.toJsonString(a);
//        }
//        a.append("]");
//        
//        return a.toString();
//    }
    
    public Object[] getName(String id, Long accountId) {
        ExchangeAccount ea = exchangeAccountManager.getExchangeAccount(Long.parseLong(id));
        if(ea == null){
            return null;
        }

        return new Object[]{ea.getName(), ea.getDomainId()};
    }

    @Override
    public Date getLastModifyTimestamp(Long accountId) throws BusinessException {
        return exchangeAccountManager.getLastModifyTimestamp();
    }

    @Override
    public String getType() {
        return "ExchangeAccount";
    }

	@Override
	public String getJsonString(long memberId, long accountId,
			String extParameters) throws BusinessException {
		StringBuilder a = new StringBuilder();
        a.append("[");
        int i = 0;
        List<ExchangeAccount> eas = this.exchangeAccountManager.getExternalAccounts(accountId);
        for (ExchangeAccount t : eas) {
            if(i++ != 0){
                a.append(",");
            }
            t.toJsonString(a);
        }
        a.append("]");
        
        return a.toString();
	}

}
