package com.seeyon.apps.ext;

import com.seeyon.apps.ext.copyFile.manager.KDocBackupManager;
import com.seeyon.apps.ext.copyFile.util.UtilString;

public class Test {

    public static void main(String[] args) throws Exception {
        long docLibID = Long.parseLong("-3989647408744217611");
        String ids = "469709112804133127|1887971124812992251";
        System.out.println(docLibID);
        System.out.println(ids);
        String[] newIds = ids.split("\\|");
        for (String id : newIds) {
            String[] folderIDs = UtilString.tokenize(id, "|");
            KDocBackupManager temp = new KDocBackupManager(docLibID, folderIDs);
            temp.download();
        }


    }
}
