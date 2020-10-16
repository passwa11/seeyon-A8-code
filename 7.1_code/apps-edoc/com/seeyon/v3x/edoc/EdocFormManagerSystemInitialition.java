/**
 * 
 */
package com.seeyon.v3x.edoc;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemInitializer;
import com.seeyon.v3x.edoc.manager.EdocFormManager;

/**
 * @author Administrator
 *
 */
public class EdocFormManagerSystemInitialition implements SystemInitializer {

    @Override
    public void destroy() {
        
    }

    @Override
    public void initialize() {
        EdocFormManager edocFormManager = (EdocFormManager)AppContext.getBean("edocFormManager");
        edocFormManager.initialize();
    }

}
