package com.seeyon.v3x.edoc.webmodel;

import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.system.signet.domain.V3xHtmDocumentSignature;

/**
 * 文单上的一个区域的意见，包括签章数据。
 * @author mujun
 *
 */
public class EdocOpinionModel {

	private List<EdocOpinion> opinions;
    private List<V3xHtmDocumentSignature> v3xHtmDocumentSignature;
	
	public List<EdocOpinion> getOpinions() {
		return opinions;
	}
	public void setOpinions(List<EdocOpinion> opinions) {
		this.opinions = opinions;
	}
    public List<V3xHtmDocumentSignature> getV3xHtmDocumentSignature() {
		return v3xHtmDocumentSignature;
	}
	public void setV3xHtmDocumentSignature(
			List<V3xHtmDocumentSignature> v3xHtmDocumentSignature) {
		this.v3xHtmDocumentSignature = v3xHtmDocumentSignature;
	}
}
