package com.seeyon.ctp.common.barCode.manager;

import com.seeyon.ctp.common.barCode.vo.BarCodeParamVo;
import com.seeyon.ctp.common.barCode.vo.ResultVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.annotation.AjaxAccess;

import java.util.Map;

public interface BarCodeManager {
	/**
	 *保存或更新二维码信息
	 *@param objectId  对象id，比如：公文id
	 *@param fileName  服务器保存的物理文件名
	 *@param categoryId 应用分类 
	*/
    void saveBarCode(Long objectId, Long fileName, String fileExt, Integer categoryId);

    /**
     * 根据参数生成二维码文件并返回相应的文件对象的id
     * @param codeParam 二维码参数
     * @param customParam 获取二维码正文的自定义参数
     * @return 文件id
     * @throws BusinessException
     */
    @AjaxAccess
    Long getBarCode(Map<String, Object> codeParam, Map<String, Object> customParam) throws BusinessException;

    /**
     * 根据参数生成二维码文件并返回相应的文件对象
     * @param codeParam 二维码参数
     * @param customParam 获取二维码正文的自定义参数
     * @return 文件对象
     * @throws BusinessException
     */
    @AjaxAccess
    ResultVO getBarCodeFile(Map<String, Object> codeParam, Map<String, Object> customParam) throws BusinessException;

    /**
     * 根据参数生成二维码文件并返回相应的文件对象
     * @param paramVo 二维码参数
     * @param customParam 获取二维码正文的自定义参数
     * @return 文件对象
     * @throws BusinessException
     */
    ResultVO getBarCodeFile(BarCodeParamVo paramVo, Map<String, Object> customParam) throws BusinessException;

    /**
     * 根据参数生成二维码文件并返回相应的附件对象
     * @param codeParam 二维码生成参数
     * @param customParam 自定义参数
     * @return 附件对象
     * @throws BusinessException
     */
    @AjaxAccess
    ResultVO getBarCodeAttachment(Map<String, Object> codeParam, Map<String, Object> customParam) throws BusinessException;

    /**
     * 根据参数生成二维码文件并返回相应的附件对象
     * @param paramVo 二维码生成参数
     * @param customParam 自定义参数
     * @return 附件对象
     * @throws BusinessException
     */
    ResultVO getBarCodeAttachment(BarCodeParamVo paramVo, Map<String, Object> customParam) throws BusinessException;

    /**
     * 解密二维码数据
     * @param codeType 对二维码数据格式化的类型
     * @param codeStr 二维码数据，扫描枪中获取的数据
     * @return 返回的对象
     * @throws BusinessException
     */
    @AjaxAccess
    Object decodeBarCode(String codeType, String codeStr, Map<String, Object> customParam) throws Exception;

    /**
     * 删除已经存在的二维码记录
     * @param reference 一级应用
     * @param subReference 二级应用
     * @throws BusinessException
     */
    @AjaxAccess
    void deleteBarCode(Long reference, Long subReference) throws BusinessException;
}
