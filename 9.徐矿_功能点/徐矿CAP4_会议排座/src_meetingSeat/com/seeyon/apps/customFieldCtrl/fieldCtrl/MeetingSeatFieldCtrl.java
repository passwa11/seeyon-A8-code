package com.seeyon.apps.customFieldCtrl.fieldCtrl;

import java.util.List;
import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCustomCtrl;
import www.seeyon.com.utils.UUIDUtil;
/**
 * <pre>
 * 自定义控件:会议排座
 * </pre>
 */
public class MeetingSeatFieldCtrl extends FormFieldCustomCtrl{

	public static void main(String[] args) {
		System.out.println("UUID:" + UUIDUtil.getUUIDLong());
	}
	
	
	@Override
	public String getKey() {
		//www.seeyon.com.utils.UUIDUtil.getUUIDString()接口	生成一个uuid，将此Id作为控件的key
		return "5583959520879353687";
	}

	//重写canUse()方法， 使控件可以在客户端使用  ，具体能不能生效  我也不晓得
	@Override
	public boolean canUse() {
		return true;
	}

	@Override
	public String getFieldLength() {
		return "255";
	}

	@Override
	public void init() {
		setPluginId("meetingSeat");
	}

	@Override
	public String getMBInjectionInfo() {
		return null;
	}
	
	
	
	/**
	 * 定义PC端自定义控件运行态资源注入信息
	 * path:文件夹路径
	 * jsUri:定义PC端表单运行态加载第三方JavaScript的路径
	 * cssUri：定义PC端表单运行态加载第三方CSS的路径
	 * initMethod:定义PC端表单运行态第三方js入口方法名称
	 * nameSpace：此自定义控件前端运行时的命名空间，可以参照一下写法来定义命名空间
	 * @return
	 */
	@Override
	public String getPCInjectionInfo() {
		String path="{path:'apps_res/cap/customCtrlResources/meetingSeatResources/',jsUri:'js/meetingSeat.js',initMethod:'init',nameSpace:'field_" + getKey() + "'}";
		return path;
	}



	@Override
	public boolean canBathUpdate() {
		return false;
	}

	@Override
	public String[] getDefaultVal(String arg0) {
		return new String[0];
	}

	@Override
	public List<String[]> getListShowDefaultVal(Integer arg0) {
		return null;
	}

	@Override
	public String getText() {
		return "会议排座";
	}
	
	

}
