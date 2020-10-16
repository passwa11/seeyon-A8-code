package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.common.metadata.MetadataNameEnum;

/**
 * @author maokai 本接口支持协同事项和协同流程节点权限控制的管理
 *         在元数据表中定义了所有的控制类型，这里需要利用这些控制类型，来判断用户是否具有某一个操作的选线。
 *         注：这里的操作，使通过协同事项和节点策略定义，而不是与用户相关的权限定义。
 */
public interface EdocPermissionControlManager {

	/**
	 * 通过这个方法，可以知道当前的流程或节点上，某一个操作是否被支持
	 * 
	 * @param action --
	 *            操作的名字
	 * @return
	 */
	boolean isActionAllowed(EdocSummary summary,MetadataNameEnum configCategory, String nodePolicy, String action);

	/**
	 * 这个方法返回当前流程和节点上支持的所有操作
	 * 注意：这里列出的操作，使我们需要检查的（或已知的）操作。如果不在要检查的范围内，返回的列表中不会包含这个操作。但这时并不能说明该操作不被支持。
	 * 
	 * @return
	 */
	List<String> getAllowedActions(EdocSummary summary, MetadataNameEnum configCategory,String nodePolicy);
}
