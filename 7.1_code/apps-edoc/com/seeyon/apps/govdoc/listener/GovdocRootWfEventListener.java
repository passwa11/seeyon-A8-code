package com.seeyon.apps.govdoc.listener;

import java.util.ArrayList;
import java.util.List;

import com.seeyon.apps.govdoc.service.GovdocApplicationHandler;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.event.AbstractEventListener;
import com.seeyon.ctp.workflow.event.EventDataContext;

public class GovdocRootWfEventListener extends AbstractEventListener  {
	
	public  String getModuleName() {
		return "edoc";
	}

	@Override
	public boolean onProcessStarted(EventDataContext context) {
		return true;
	}
	
	@Override
	public boolean onProcessFinished(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onProcessFinished(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onProcessCanceled(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onProcessCanceled(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onWorkitemAssigned(EventDataContext context) {
		List<EventDataContext> contextList = new ArrayList<EventDataContext>();
        contextList.add(context);
        return onWorkflowAssigned(contextList);
	}
	
	@Override
	public boolean onWorkflowAssigned(List<EventDataContext> contextList) {
		boolean isNotEmptyContextList = Strings.isNotEmpty(contextList);
	    if(!isNotEmptyContextList) {
	    	return true;
	    }
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) contextList.get(0).getBusinessData("subAppName"));
			return listener.onWorkflowAssigned(contextList);
		} catch(Exception e) {
			return false;
		}
    }

	@Override
	public boolean onWorkitemFinished(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onWorkitemFinished(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onWorkitemCanceled(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onWorkitemCanceled(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onWorkitemTakeBack(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onWorkitemTakeBack(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onWorkitemStoped(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onWorkitemStoped(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onWorkitemWaitToLastTimeStatus(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onWorkitemWaitToLastTimeStatus(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onWorkitemWaitToReady(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onWorkitemWaitToReady(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onWorkitemReadyToWait(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onWorkitemReadyToWait(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onWorkitemDoneToReady(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onWorkitemDoneToReady(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean onSubProcessStarted(EventDataContext context) {
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onSubProcessStarted(context);
		} catch(Exception e) {
			return false;
		}
	}
	
	public boolean onSubProcessCanceled(EventDataContext context){
		GovdocApplicationHandler handler = (GovdocApplicationHandler)AppContext.getBean("govdocApplicationHandler");
		try {
			GovdocAbWorkflowEventListener listener = handler.getWorkflowListenerHandler((String) context.getBusinessData("subAppName"));
			return listener.onSubProcessCanceled(context);
		} catch(Exception e) {
			return false;
		}
	}

}
