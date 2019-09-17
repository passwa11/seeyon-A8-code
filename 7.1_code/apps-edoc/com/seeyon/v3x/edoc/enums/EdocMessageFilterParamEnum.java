package com.seeyon.v3x.edoc.enums;
/**系统消息-标记位枚举*/
public enum EdocMessageFilterParamEnum{
    //发文
    sendPutong(1),
    sendPingji(2),
    sendJiaji(3),
    sendTeji(4),
    sendTeti(5),
    sendQita(18),
    //收文
    recPutong(6),
    recPingji(7),
    recJiaji(8),
    recTeji(9),
    recTeti(10),
    recQita(19),
    //签报
    signPutong(11),
    signPingji(12),
    signJiaji(13),
    signTeji(14),
    signTeti(15),
    signQita(20),
    //公文交换
    exchange(16),
    //公文督办
    supervise(17);
    public Integer key;
    private EdocMessageFilterParamEnum(Integer value) {
        this.key = value;
    }
}
