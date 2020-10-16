/*
 * Created on 2004-11-12
 *
 */
package net.joinwork.bpm.engine.execute;

import java.io.Serializable;

/**
 * @author dinghong
 *
 */
public class SubCaseInfo implements Cloneable, Serializable {
public static final long serialVersionUID = 1;
public long caseId;
public String domain;
public String activityId;
}
