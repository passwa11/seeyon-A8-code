/**
 * $Author: $
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.dao;

import java.util.List;

import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.v3x.isearch.model.ConditionModel;

/**
 * @author mujun
 *
 */
public interface ISearchInterfaceColDao {
    public List<CtpAffair> transSearch(ConditionModel cModel);
}
