package com.seeyon.apps.wpstrans.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class WpsTransFileUtil {

	 /* 
	* 使用文件通道的方式复制文件 
	*/  
	public static void fileChannelCopy(String srcDirName, String destDirName) {  
		FileInputStream fi = null;  
		FileOutputStream fo = null;  
		FileChannel in = null;  
		FileChannel out = null;  
		try {  
			fi = new FileInputStream(new File(srcDirName));  
			fo = new FileOutputStream(new File(destDirName));  
			in = fi.getChannel();//得到对应的文件通道  
			out = fo.getChannel();//得到对应的文件通道  
			in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道中  
		} catch (FileNotFoundException e) {          
	 
		} catch (IOException e) {  
	  
		} finally {  
			try {  
				if(fi != null){					
					fi.close();  
				}
				if(in != null){					
					in.close();  
				}
				if(fo != null){					
					fo.close();  
				}
				if(out != null){					
					out.close();  
				}
			} catch (IOException e) {
				
			}
		}
	}
}
