package com.seeyon.ctp.portal.section.templete;

import com.seeyon.ctp.portal.section.util.SectionUtils;
import com.seeyon.ctp.util.ObjectToXMLBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 成倍行,不定列 模板<br/>
 * 适用于　三或四列标准列表模板满足不了需要的情况下<br/>
 * 可以自定义列数、宽度、单元格样式、链接地址<br/>
 * 
 * @author <a href="mailto:fishsoul@126.com">Mazc</a>
 * 
 */
public class MultiRowVariableColumnColTemplete extends BaseSectionTemplete {
    private static final long                        serialVersionUID = -2910452785360815515L;

    private List<Row> rows;

    public static final String RESOLVE_FUNCTION= "multiRowVariableColumnColTemplete";

    @Override
    public String getResolveFunction() {
        return RESOLVE_FUNCTION;
    }

    public MultiRowVariableColumnColTemplete.Row addRow() {
        if (this.rows == null) {
            this.rows = new ArrayList<Row>();
        }

        MultiRowVariableColumnColTemplete.Row row = new MultiRowVariableColumnColTemplete.Row();
        this.rows.add(row);

        return row;
    }

    public List<Row> getRows() {
        return this.rows;
    }

    /**
     * 行对象
     */
    public class Row extends ObjectToXMLBase implements Serializable {

        private static final long                 serialVersionUID = 6799252093506685897L;

        List<Cell> cells;

        public MultiRowVariableColumnColTemplete.Cell addCell() {
            if (cells == null) {
                cells = new ArrayList<Cell>();
            }
            MultiRowVariableColumnColTemplete.Cell c = new MultiRowVariableColumnColTemplete.Cell();

            cells.add(c);

            return c;
        }

        public List<Cell> getCells() {
            return cells;
        }

    }

    /**
     * 列单元格 对象
     */
    public class Cell extends ObjectToXMLBase implements Serializable {
        private static final long serialVersionUID = -3786227930814279877L;
        
        private long			  id;

        private String            cellContent;                             // 单元格内容

        private String            cellContentHTML;

        private String            alt;

        private Boolean           hasAttachments;                          // 是否有附件

        private Boolean           fiexed;                                  // 固定文字，不裁减，默认false

        private int               cellWidth;                               // 单元格宽度 百分比　（用int值，不需要'%'）

        private int               cellContentWidth;                        // 单元格内容 百分比 (用int值，不需要'%')

        private String            linkURL;                                 // 链接地址

        private String            className;                               // 样式名称

        private int               openType;

        private List<String>      extIcons;

        private List<String>      extClasses;

        private List<String>      extPreClasses;

        private String            bodyType;
        
        private Map<String, Map<String, String>>       handler;		//事件参数
        
        private boolean       	  isTop;		//是否置顶
        
        private Integer 		  app;

		private Integer 		  subApp;
		
		private List<String>      extClassesAlt;//加签会签图标class浮动显示信息
        
        public Integer getApp() {
			return app;
		}

        private String receiveTimeAll;//周刘成  ：文件接收时间

        public String getReceiveTimeAll() {
            return receiveTimeAll;
        }

        public void setReceiveTimeAll(String receiveTimeAll) {
            this.receiveTimeAll = receiveTimeAll;
        }

        public void setApp(Integer app) {
			this.app = app;
		}

		public Integer getSubApp() {
			return subApp;
		}

		public void setSubApp(Integer subApp) {
			this.subApp = subApp;
		}

		public void setOpenType(int openType) {
			this.openType = openType;
		}
        
        public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}
        
		public boolean isTop() {
			return isTop;
		}

		public void setTop(boolean isTop) {
			this.isTop = isTop;
		}

		public Map<String, Map<String, String>> getHandler() {
			return handler;
		}

		public void setHandler(Map<String, Map<String, String>> handler) {
			this.handler = handler;
		}

		public String getCellContent() {
            return cellContent;
        }

        public void setCellContent(String cellContent) {
            this.cellContent = cellContent;
        }

        public String getCellContentHTML() {
            return cellContentHTML;
        }

        /**
         * 标题的HTML代码，设置这个属性后，其它参数{hasAttachments, extIcons}仍然不起作用
         * 
         * @see SectionUtils.mergeSubject(String subject, int maxLength, Integer
         *      importantLevel, Boolean hasAttachments, String bodyType,
         *      List<String> extIcons)
         * 
         * @param cellContentHTML
         */
        public void setCellContentHTML(String cellContentHTML) {
            this.cellContentHTML = cellContentHTML;
        }

        public String getAlt() {
            return alt;
        }

        public void setAlt(String alt) {
            this.alt = alt;
        }

        public Boolean getHasAttachments() {
            return hasAttachments;
        }

        public void setHasAttachments(Boolean hasAttachments) {
            this.hasAttachments = hasAttachments;
        }

        public Boolean getFiexed() {
            return fiexed;
        }

        public void setFiexed() {
            this.fiexed = true;
        }

        public int getCellWidth() {
            return cellWidth;
        }

        /**
         * 单元格宽度 百分比　（用int值，不需要'%'）
         * 
         * @param cellWidth
         */
        public void setCellWidth(int cellWidth) {
            this.cellWidth = cellWidth;
        }

        public int getCellContentWidth() {
            return cellContentWidth;
        }

        public void setCellContentWidth(int cellContentWidth) {
            this.cellContentWidth = cellContentWidth;
        }

        public String getLinkURL() {
            return linkURL;
        }

        public void setLinkURL(String linkURL) {
            this.linkURL = linkURL;
        }

        public void setLinkURL(String linkURL, OPEN_TYPE openType) {
            this.linkURL = linkURL;
            this.openType = openType.ordinal();
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public int getOpenType() {
            return openType;
        }

        public void setOpenType(OPEN_TYPE openType) {
            this.openType = openType.ordinal();
        }

        public List<String> getExtIcons() {
            return extIcons;
        }

        public void addExtIcon(String extIcon) {
            if (this.extIcons == null) {
                this.extIcons = new ArrayList<String>();
            }

            this.extIcons.add(extIcon);
        }

        public List<String> getExtClasses() {
            return extClasses;
        }

        public void setExtClasses(List<String> extClasses) {
            this.extClasses = extClasses;
        }

        public void addExtClasses(String extClasses) {
            if (this.extClasses == null) {
                this.extClasses = new ArrayList<String>();
            }

            this.extClasses.add(extClasses);
        }

        public List<String> getExtPreClasses() {
            return extPreClasses;
        }

        public void setExtPreClasses(List<String> extPreClasses) {
            this.extPreClasses = extPreClasses;
        }

        public void addExtPreClasses(String extPreClasses) {
            if (this.extPreClasses == null) {
                this.extPreClasses = new ArrayList<String>();
            }

            this.extPreClasses.add(extPreClasses);
        }

        public String getBodyType() {
            return bodyType;
        }

        public void setBodyType(String bodyType) {
            this.bodyType = bodyType;
        }
        
        public List<String> getExtClassesAlt() {
			return extClassesAlt;
		}

		public void setExtClassesAlt(List<String> extClassesAlt) {
			this.extClassesAlt = extClassesAlt;
		}
		
		public void addExtClassesAlt(String ExtClassesAltStr) {
            if (this.extClassesAlt == null) {
                this.extClassesAlt = new ArrayList<String>();
            }

            this.extClassesAlt.add(ExtClassesAltStr);
        }
    }

    @Override
    public int[] getPageSize(Map<String, String> preference) {
        return SectionUtils.getMultiRowPageSize(preference);
    }

}
