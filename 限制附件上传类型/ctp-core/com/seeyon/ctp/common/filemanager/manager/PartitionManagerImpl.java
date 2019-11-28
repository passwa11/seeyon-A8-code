/**
 * 
 */
package com.seeyon.ctp.common.filemanager.manager;

import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.cache.CacheAccessable;
import com.seeyon.ctp.common.cache.CacheFactory;
import com.seeyon.ctp.common.cache.CacheMap;
import com.seeyon.ctp.common.constants.SystemProperties;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.Constants;
import com.seeyon.ctp.common.filemanager.NoSuchPartitionException;
import com.seeyon.ctp.common.filemanager.dao.PartitionDAO;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.filemanager.Partition;
import com.seeyon.ctp.datasource.annotation.DataSourceName;
import com.seeyon.ctp.datasource.annotation.ProcessInDataSource;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;

/**
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2006-11-15
 */
@ProcessInDataSource(name = DataSourceName.BASE)
public class PartitionManagerImpl implements PartitionManager {
    private static Log                   log               = CtpLogFactory.getLog(PartitionManagerImpl.class);
    private static final CacheAccessable cacheFactory      = CacheFactory.getInstance(PartitionManagerImpl.class);

    //	private List<Partition> allPartitions = new ArrayList<Partition>();
    private CacheMap<Long, Partition>    allPartitions;

	public int getSortOrder() {
		return -990;
	}
	public void initialize() {
		init();
	}

	//	private Map<Long, String> allPartitionPaths = new HashMap<Long, String>();
    private CacheMap<Long, String>       allPartitionPaths ;

    private PartitionDAO                 partitionDAO;

    public void setPartitionDAO(PartitionDAO partitionDAO) {
        this.partitionDAO = partitionDAO;
    }
    public void initCache(){
        String cacheName = "AllPartitions";
        if(cacheFactory.isExist(cacheName)){
            allPartitions = cacheFactory.getMap(cacheName);
        }else{
            allPartitions = cacheFactory.createLinkedMap(cacheName);
        }
        cacheName = "AllPartitionPaths";
        if(cacheFactory.isExist(cacheName)){
            allPartitionPaths = cacheFactory.getMap(cacheName);
        }else{
            allPartitionPaths = cacheFactory.createMap(cacheName);
        }
    }
    /**
     * 加载所有分区信息
     */
    public void init() {
        long startTime = System.currentTimeMillis();
        initCache();

//        if(!CacheFactory.isSkipFillData()){
//            allPartitions.clear();
//            allPartitionPaths.clear();
            final List<Partition> partitations = partitionDAO.findAll();
            Map<Long, Partition> map = new HashMap<Long, Partition>();
            for (Partition p : partitations) {
                String path = p.getPath();

                path = toCanonicalPath(path);

                // 检查有效分区路径合法性
                if(p.getState().equals(Constants.PARTITION_STATE.used.ordinal())){
                    if(!new File(path).exists()){
                        log.error(path + " 分区不存在，请检查分区配置并确定对应磁盘位置有效！");
                    }
                }

                allPartitionPaths.put(p.getId(), path);

                map.put(p.getId(), p);
            }
            allPartitions.replaceAll(map);
//        }
        putPartitionsToSysProps();

        log.info("加载所有分区信息. 耗时：" + (System.currentTimeMillis() - startTime) + " MS");
    }

    private void putPartitionsToSysProps() {
        StringBuffer stringBuffer = new StringBuffer();
        for (Partition partition : allPartitions.values()) {
            String startDate = Datetimes.formatDatetime(partition.getStartDate());
            String endDate = Datetimes.formatDatetime(partition.getEndDate());
            stringBuffer.append(";");
            stringBuffer.append(startDate);
            stringBuffer.append("_");
            stringBuffer.append(endDate);
            stringBuffer.append(",");
            stringBuffer.append(partition.getPath());
        }
        System.setProperty("all_partition_str", stringBuffer.substring(1));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.seeyon.v3x.common.filemanager.manager.PartitionManager#getAllPartitions()
     */
    @Override
    public List<Partition> getAllPartitions() {
        return new ArrayList<Partition>(this.allPartitions.values());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.seeyon.v3x.common.filemanager.manager.PartitionManager#getPartitions(java.lang.Long)
     */
    @Override
    public Partition getPartition(Long id) {
        return allPartitions.get(id);
        /*		for (Partition partition : allPartitions.toList()) {
        			if (partition.getId().equals(id)) {
        				return partition;
        			}

        		return null;
        		}*/
    }

    @Override
    public Partition getPartition(Date createDate, boolean isOnlyEnable) {
    	Date cd = clearTime(createDate);

        for (Partition partition : allPartitions.values()) {
            Date startDate = partition.getStartDate();
            Date endDate = partition.getEndDate();

            if ((startDate.before(cd) && endDate.after(cd)) || cd.equals(startDate)
                    || cd.equals(endDate)) { // 符合时间
                if (!isOnlyEnable) {
                    return partition;
                }

                if (partition.getState().equals(Constants.PARTITION_STATE.used.ordinal())) {
                    return partition;
                }
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.seeyon.v3x.common.filemanager.manager.PartitionManager#create(com.seeyon.v3x.common.filemanager.Partition)
     */
    @Override
    public void create(Partition partition) {
        partition.setIdIfNew();

        partitionDAO.save(partition);

        this.init();
        /**
         * 发送通知。
         */
        //		NotificationManager.getInstance().send(NotificationType.PartitaionReload, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.seeyon.v3x.common.filemanager.manager.PartitionManager#delete(long)
     */
    @Override
    public void delete(long id) {
        partitionDAO.delete(id);
        this.init();
        /**
         * 发送通知。
         */
        //		NotificationManager.getInstance().send(NotificationType.PartitaionReload, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.seeyon.v3x.common.filemanager.manager.PartitionManager#update(com.seeyon.v3x.common.filemanager.Partition)
     */
    @Override
    public void update(Partition partition) throws BusinessException {
        List<Partition> partitions = getPartition(partition.getStartDate(), partition.getEndDate(), true);
        if (partitions.size() > 1) {
            partitions.remove(partition);
            throw new BusinessException("时间段与" + partitions + "重叠.");
        }

        if (partitions.size() == 1 && !partitions.get(0).equals(partition)) {
            throw new BusinessException("时间段与" + partitions + "重叠.");
        }

        Partition originPartition = this.getPartition(partition.getId());
        String originPath = originPartition.getPath();
        String newPath = partition.getPath();

        partitionDAO.update(partition);
        this.init();
        /**
         * 发送通知。
         */
        //		NotificationManager.getInstance().send(NotificationType.PartitaionReload, null);

        //路径有变化，把文件拷贝到新路径加
        moveFile(partition.getStartDate(), partition.getEndDate(), originPath, newPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.seeyon.v3x.common.filemanager.manager.PartitionManager#splitPartition(java.lang.Long,
     *      java.lang.String, java.lang.String, java.util.Date,
     *      java.lang.String)
     */
    @Override
    public void splitPartition(Long originPartitionId, String newPartitionName, String newPartitionPath,
                               Date splitDate, String newPartitionDescription) throws BusinessException {
        Partition originPartition = this.getPartition(originPartitionId);

        if (originPartition == null) {
            throw new BusinessException("分区id为[" + originPartitionId + "]的分区不存在.");
        }

        GregorianCalendar splitCalendar = new GregorianCalendar();
        splitCalendar.setTime(splitDate);
        splitCalendar.set(Calendar.HOUR_OF_DAY, 0);
        splitCalendar.set(Calendar.MINUTE, 0);
        splitCalendar.set(Calendar.SECOND, 0);
        splitCalendar.set(Calendar.MILLISECOND, 0);

        /**
         * 前一天
         */
        splitCalendar.add(GregorianCalendar.DAY_OF_YEAR, -1);

        Date sd = splitCalendar.getTime();

        Date endDate = originPartition.getEndDate();
		if (!Datetimes.between(sd, originPartition.getStartDate(), endDate, true)) {
            throw new BusinessException("拆分时间点" + DateFormatUtils.ISO_DATE_FORMAT.format(sd) + "不在原分区的起止时间之内。");
        }

        originPartition.setEndDate(sd);

        partitionDAO.update(originPartition);
        allPartitions.notifyUpdate(originPartition.getId());
        /**
         * 下一天
         */
        splitCalendar.add(GregorianCalendar.DAY_OF_YEAR, 1);

        Partition newPartition = new Partition();
        newPartition.setIdIfNew();
        newPartition.setName(newPartitionName);
        newPartition.setPath(newPartitionPath);
        newPartition.setCreateTime(new Date());
        newPartition.setStartDate(splitCalendar.getTime());
        newPartition.setEndDate(endDate);
        newPartition.setState(Constants.PARTITION_STATE.used.ordinal());
        newPartition.setDescription(newPartitionDescription);

        this.create(newPartition);

        Date today = Datetimes.getTodayFirstTime();

        //分区时间点在今天之前，需要把以前的附件复制到新的分区下
        if (!sd.after(today)) {
            moveFile(newPartition.getStartDate(), today, originPartition.getPath(), newPartitionPath);
        }
    }

    @Override
    public List<Partition> getPartition(Date startDate, Date endDate, boolean isOnlyEnable) {
    	Date sd = clearTime(startDate);
    	Date ed = clearTime(endDate);

        List<Partition> result = new ArrayList<Partition>();

        for (Partition partition : allPartitions.values()) {
            if (Datetimes.checkOverup(sd, ed, partition.getStartDate(), partition.getEndDate(), false)) {
                if (!isOnlyEnable) {
                    result.add(partition);
                } else {
                    if (partition.getState().equals(Constants.PARTITION_STATE.used.ordinal())) {
                        result.add(partition);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 把Date对象的时、分、秒置为0
     * 
     * @param date
     */
    private static Date clearTime(Date date) {
    	GregorianCalendar startCalendar = new GregorianCalendar();
        startCalendar.setTime((date==null) ? new Date() : date);
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        return startCalendar.getTime();
    }

    @Override
    public String getFolder(Date createDate, boolean createWhenNoExist) throws BusinessException {
        String partitionPath = getPartitionPath(createDate, true);

        if (partitionPath == null) {
            throw new NoSuchPartitionException(createDate);
        }
        if(createDate == null) {
        	log.error("create date is null");
        	return null;
        }
        
        if(separator.equals(partitionPath.substring((partitionPath.length()-1)))){
        	partitionPath = partitionPath.substring(0, partitionPath.length()-1);
        }
        
        String folder = DateFormatUtils.format(createDate, Constants.DATE_TO_FOLDER_STYLE);
        folder = partitionPath + separator + folder;

        if (createWhenNoExist) {
            File file = new File(folder);
            if (!file.exists()) {
                file.mkdirs();
            }
        }

        return folder;
    }

    private static String toCanonicalPath(String path) {
    	String p = path;
        p = SystemProperties.interpolateHelper(p, SystemProperties.getInstance().getAllProperties());
        p = Strings.getCanonicalPath(p);

        return p;
    }

    @Override
    public boolean validatePath(String path) {
    	String p = path;
        if (Strings.isBlank(p)) {
            return false;
        }

        p = toCanonicalPath(p);

        File file = new File(p);
        if (file.isDirectory() && file.exists()) {
            return true;
        }

        return false;
    }

    @Override
    public String getPartitionPath(Date createDate, boolean isOnlyEnable) {
        Partition partition = this.getPartition(createDate, isOnlyEnable);
        if (partition != null) {
            return allPartitionPaths.get(partition.getId());
        }

        return null;
    }

    private void moveFile(Date from, Date to, String originPath, String newPath) {
        /*
        from = clearTime(from);
        to = clearTime(to);
        
        Date today = Datetimes.getTodayFirstTime();
        
        to = to.after(today) ? today : to;
        
        if(to.before(from)){
        	return;
        }
        
        originPath = toCanonicalPath(originPath);
        newPath = toCanonicalPath(newPath);
        
        if(new File(originPath).equals(new File(newPath))){
        	return;
        }
        
        log.info("移动附件从[" + originPath + "]到[" + newPath + "], 时间段：[" + Datetimes.formatDate(from) + "]到[" + Datetimes.formatDate(to) + "]");
        
        long len = (to.getTime() - from.getTime()) / 1000 / 3600 / 24;
        
        GregorianCalendar baseCalendar = new GregorianCalendar();
        baseCalendar.setTime(from);
        
        for (int i = 0; i < len + 1; i++) {
        	Date date = Datetimes.addDate(from, i);
        	String folder = DateFormatUtils.format(date, Constants.DATE_TO_FOLDER_STYLE);
        	try {
        		File srcDir = new File(originPath + separator + folder);
        		File destDir = new File(newPath + separator + folder);
        		
        		if(!srcDir.exists()){
        			continue;
        		}
        		
        		FileUtils.copyDirectory(srcDir, destDir);
        		FileUtils.deleteDirectory(srcDir);
        		
        		boolean isDeleteMonthDir = false;
        		{ //删除月份文件夹
        		File p = srcDir.getParentFile();
        		File[] fs = p.listFiles();
        		if(fs == null || fs.length == 0){
        			isDeleteMonthDir = p.delete();
        		}
        		}
        		if(isDeleteMonthDir){//删除年份文件夹
        			File yp = srcDir.getParentFile().getParentFile();
        			File[] yfs = yp.listFiles();
        			if(yfs == null || yfs.length == 0){
        				yp.delete();
        			}
        		}
        	}
        	catch (Exception e) {
        		log.error("", e);
        	}
        }
        */
    }

    /* (non-Javadoc)
     * @see com.seeyon.v3x.common.filemanager.manager.PartitionManager#isPartitionNameDuple(java.lang.String)
     */
    @Override
    public boolean isPartitionNameDuple(String name) {
        if (allPartitions != null) {
            for (Iterator iter = allPartitions.values().iterator(); iter.hasNext();) {
                Partition element = (Partition) iter.next();
                if (element.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getFolderForUC(Date createDate, boolean createWhenNoExist) throws BusinessException {
        {
            String partitionPath = getPartitionPath(createDate, true);

            if (partitionPath == null) {
                throw new NoSuchPartitionException(createDate);
            }
            if(createDate == null) {
                log.error("create date is null");
                return null;
            }

            if(separator.equals(partitionPath.substring((partitionPath.length()-1)))){
                partitionPath = partitionPath.substring(0, partitionPath.length()-1);
            }

            String folder = DateFormatUtils.format(createDate, Constants.DATE_TO_FOLDER_STYLE);
            folder = partitionPath + separator + "zx" + separator + folder;

            if (createWhenNoExist) {
                File file = new File(folder);
                if (!file.exists()) {
                    file.mkdirs();
                }
            }

            return folder;
        }
    }

}
