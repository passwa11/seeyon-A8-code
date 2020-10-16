/*
 * Created on 2005-2-1
 *
 */
package net.joinwork.bpm.engine.execute;

import net.joinwork.bpm.engine.wapi.NodeInfo;

/**
 * @author dinghong
 *
 */
public class BPMNodeInfo extends NodeInfo{
	/**
		 * @param string
		 */
		public void setDesc(String string) {
			desc = string;
		}

		/**
		 * @param string
		 */
		public void setId(String string) {
			id = string;
		}

		/**
		 * @param string
		 */
		public void setName(String string) {
			name = string;
		}
}
