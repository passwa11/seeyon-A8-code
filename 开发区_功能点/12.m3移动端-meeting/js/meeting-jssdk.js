(function( global ) {
	var api = {
        Meeting : {
            //zhou
            updateMeetingRoom :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/updateMeetingRoom',_params,_body,cmp.extend({},options))},

            getVideoMeetingZPK :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/getVideoMeetingZPK',_params,_body,cmp.extend({},options))},
            
        	videoMeetingParams :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/videoMeetingParams',_params,_body,cmp.extend({},options))}, 

            findPendingMeetings :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/findPendingMeetings',_params,_body,cmp.extend({},options))}, 

            getWaitsendMeetings :  function(personid,_params,_body,options){return SeeyonApi.Rest.post('meeting/waitsends/'+personid+'',_params,_body,cmp.extend({},options))}, 

            getMeetingRoomApp :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/getMeetingRoomApp',_params,_body,cmp.extend({},options))}, 

            execApp :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/execApp',_params,_body,cmp.extend({},options))}, 

            getMeetingComments :  function(meetingid,_params,_body,options){return SeeyonApi.Rest.post('meeting/comments/'+meetingid+'',_params,_body,cmp.extend({},options))}, 

            cancelMeetRoomApp :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/cancelMeetRoomApp',_params,_body,cmp.extend({},options))}, 

            getPendingMeetingCount :  function(_params,options){return SeeyonApi.Rest.get('meeting/pendingCount',_params,'',cmp.extend({},options))}, 

            checkMeetingRoomConflict :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/checkMeetingRoomConflict',_params,_body,cmp.extend({},options))}, 

            meetingUserPeivMenu :  function(_params,options){return SeeyonApi.Rest.get('meeting/meeting/user/privMenu',_params,'',cmp.extend({},options))}, 

            findSentMeetings :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/findSentMeetings',_params,_body,cmp.extend({},options))}, 

            showMeetingSummaryMembers :  function(_params,options){return SeeyonApi.Rest.get('meeting/showMeetingSummaryMembers',_params,'',cmp.extend({},options))}, 

            transInviteConferees :  function(_params,options){return SeeyonApi.Rest.get('meeting/transInviteConferees',_params,'',cmp.extend({},options))}, 

            getMeetingRoomAppDetail :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/getMeetingRoomAppDetail',_params,_body,cmp.extend({},options))}, 

            create :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/create',_params,_body,cmp.extend({},options))}, 

            reply :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/reply',_params,_body,cmp.extend({},options))}, 

            checkConfereesConflict :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/checkConfereesConflict',_params,_body,cmp.extend({},options))}, 

            finishAuditMeetingRoom :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/finishAuditMeetingRoom',_params,_body,cmp.extend({},options))}, 

            getMeetingRooms :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/getMeetingRooms',_params,_body,cmp.extend({},options))}, 

            getMeetingModifyElement :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/getMeetingModifyElement',_params,_body,cmp.extend({},options))}, 

            advanceMeeting :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/advanceMeeting',_params,_body,cmp.extend({},options))}, 


            cancelMeeting :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/cancelMeeting',_params,_body,cmp.extend({},options))}, 

            finishMeetingRoom :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/finishMeetingRoom',_params,_body,cmp.extend({},options))}, 

            getOrderDate :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/getOrderDate',_params,_body,cmp.extend({},options))}, 

            sendRemindersMeetingReceiptMessage :  function(_params,options){return SeeyonApi.Rest.get('meeting/detail/sendMessage',_params,'',cmp.extend({},options))}, 

            findWaitSentMeetings :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/findWaitSentMeetings',_params,_body,cmp.extend({},options))}, 

            getMeetingRoom :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/getMeetingRoom',_params,_body,cmp.extend({},options))}, 

            getPendingMeetings :  function(_params,options){return SeeyonApi.Rest.get('meeting/pendings',_params,'',cmp.extend({},options))}, 

            findDoneMeetings :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/findDoneMeetings',_params,_body,cmp.extend({},options))}, 
            
            getMeetingSummarys :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/summary/getMeetingSummarys',_params,_body,cmp.extend({},options))}, 
            
            getMeetingSummary :  function(recordId,_params,options){return SeeyonApi.Rest.get('meeting/summary/'+recordId+'',_params,'',cmp.extend({},options))}, 

            getMeetingRoomApps :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/getMeetingRoomApps',_params,_body,cmp.extend({},options))}, 

            showMeetingMembers :  function(_params,options){return SeeyonApi.Rest.get('meeting/meetingMembers',_params,'',cmp.extend({},options))}, 

            getMeeting :  function(id,_params,options){return SeeyonApi.Rest.get('meeting/'+id+'',_params,'',cmp.extend({},options))}, 

            getSendMeetings :  function(personid,_params,_body,options){return SeeyonApi.Rest.post('meeting/sends/'+personid+'',_params,_body,cmp.extend({},options))}, 

            getMeetingRoomAudits :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/getMeetingRoomAudits',_params,_body,cmp.extend({},options))}, 

            comment :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/comment',_params,_body,cmp.extend({},options))}, 

            detail :  function(_params,options){return SeeyonApi.Rest.get('meeting/detail',_params,'',cmp.extend({},options))}, 

            removeInvitePer :  function(_params,options){return SeeyonApi.Rest.get('meeting/removeInvitePer',_params,'',cmp.extend({},options))}, 

            getApplyMeemtingRooms :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/getApplyMeemtingRooms',_params,_body,cmp.extend({},options))}, 

            send :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/send',_params,_body,cmp.extend({},options))}, 

            removeMeeting :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/removeMeeting',_params,_body,cmp.extend({},options))}, 
            
            getMeetingSummaryTypeList :  function(_params,options){return SeeyonApi.Rest.get('meeting/summary/getTypeList',_params,'',cmp.extend({},options))},
            
            meetingSignCondition :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/meetingSignCondition',_params,_body,cmp.extend({},options))},
            
            meetingSign :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/meetingSign',_params,_body,cmp.extend({},options))},
            
            meetingRoomState :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/meetingRoomState',_params,_body,cmp.extend({},options))},
            
            meetingRoomApplyCondition :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/meetingRoomApplyCondition',_params,_body,cmp.extend({},options))},
            
            meetingInviteDetail :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/meetingInviteDetail',_params,_body,cmp.extend({},options))},
          
        	attendMeeting :  function(_params,_body,options){return SeeyonApi.Rest.post('meeting/attendMeeting',_params,_body,cmp.extend({},options))},
        	
        	meetingInviteCard :  function(_params,options){return SeeyonApi.Rest.get('meeting/meetingInviteCard',_params,'',cmp.extend({},options))},
        
        	screenSlot: function (_params, _body, options) { return SeeyonApi.Rest.post('meeting/screenSlot', _params, _body, cmp.extend({}, options)) }
        	
        }
    }
	global.SeeyonApi = global.SeeyonApi || {};
    for(var key in api){
        global.SeeyonApi[key] = api[key];
    }
})(this);