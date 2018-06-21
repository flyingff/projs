package net.flyingff.douyu.barrage.resolver;

import java.util.Map;

public class AllMessageListener extends AbstractBarrageListener {
	private GiftConfig giftConfig;
	public AllMessageListener(GiftConfig giftConfig) {
		this.giftConfig = giftConfig;
	}
	
	// ---------------
	// 日常类型
	// ---------------
	@Type("mrkl")
	public void onMark() {
		//keep alive message, ignore it.
	}
	@Type("loginres")
	public void onLogin() {
		System.out.println("登录成功");
	}
	@Type("pingreq")
	public void onPingReq() {
		// ignore it
	}
	
	// ---------------
	// 弹幕类型
	// ---------------
	@Type("chatmsg")
	public void onChat(@Param("nn")String userName, @Param("txt")String text) {
		System.out.println(userName + ": " + text);
	}
	@Type("cthn")
	public void onBroadcast(@Param("unk")String uName, @Param("chatmsg")String msg) {
		//Message: {uid=0, gid=0, unk=潇逍鱼, drid=903559, chatmsg=主播电脑绘画现场教学, cl=2, type=cthn, rid=160504, onk=一个老司机丶, nl=0, cid=14, ts=1529322967}
		System.out.println("全站广播:" + msg);
	}
	
	// ---------------
	// 礼物类型
	// ---------------
	
	@Type("onlinegift")
	public void onRecvOnlineGift(@Param("nn")String userName, @Param("sil") int count) {
		System.out.println(userName + "领取了" + count + "个鱼丸");
	}
	@Type("dgb")
	public void onOnlineGiftItem(@Param("nn")String userName,
			@Param("gfid")String item,
			@Param("hits") String hits) {
		System.out.println(userName + "送出了" + giftConfig.getString(item) + (hits == null ? "" : "[连击" + hits + "]"));
	}
	@Type("ggbb")
	public void onRedPacket2() {}
	@Type("tsgs")
	public void onRedPacket(@Param("dnk")String receiver,
			@Param("snk")String sender,
			@Param("silver")String cnt
			) {
		if(cnt != null) {
			System.out.println(receiver + "抢到了" + sender + "派发的" + cnt + "个鱼丸");	
		}
	}
	@Type("gpbc")
	public void onRedPacketItem(@Param("dnk")String receiver,
			@Param("snk")String sender,
			@Param("cnt")String cnt,
			@Param("pnm")String item
			) {
		System.out.println(receiver + "抢到了" + sender + "派发的" + cnt  + "个" + item);
	}
	
	// ---------------
	// 房间相关
	// ---------------
	@Type("uenter")
	public void onEnterRoom(@Param("nn")String userName) {
		System.out.println(userName + " 进入了直播间");
	}
	@Type("bc_buy_deserve")
	public void onDeserve() {
		System.out.println("酬勤？");
	}
	@Type("rss")
	public void onOpenClose(@Param("ss")String ss) {
		System.out.println("0".equals(ss) ? "关直播了" : "开直播了");
	}
	@Type("ranklist")
	public void onRankList() {
		System.out.println("榜单更新");
	}
	@Type("ssd")
	public void onSuperChat(@Param("content") String content) {
		System.out.println("超级弹幕: " + content);
	}
	@Type("spbc")
	public void onGiftEffect(@Param("gn") String giftName) {
		System.out.println("礼物效果: " + giftName);
	}
	@Type("rankup")
	public void onRankUp() {
		System.out.println("Top10 变化");
	}
	@Type("synexp")
	public void onExpChange() {
		System.out.println("主播经验变化");
	}
	@Type("blab")
	public void onBannerChange(@Param("nn")String uName,
			@Param("bnn")String banner,
			@Param("bl")String level
			) {
		System.out.println(uName + "的粉丝等级[" + banner + "] 升级到" + level);
	}
	@Type("frank")
	public void onFansListChange() {
		System.out.println("粉丝列表更新");
	}
	
	@Type("wiru")
	public void onRoomPosChange(@Param("pos") String pos, Map<String, String> packet) {
		System.out.println("当前房间排名变化为" + pos);
	}
	@Type("wirt")
	public void onPersonPosChange(@Param("pos") String pos, @Param("nick")String uName) {
		System.out.println("主播排名变化: " + uName + "排名变为" + pos);
	}
	
	@Type("gbroadcast")
	public void onGBroadcast(Map<String, String> packet) {
		// 比如车队召唤?
		System.out.println("某种广播" + packet);
	}
	@Type("newblackres")
	public void onBlock(@Param("snic")String adminName, @Param("dnic")String target) {
		System.out.println(adminName + "禁言了" + target);
	}
	@Type("upgrade")
	public void onUpgrade(@Param("nn")String uName, @Param("level")String level) {
		System.out.println(uName + "升级到"  + level);
	}
	@Type("lds")
	public void onLotteryStart() {
		//未知消息类型lds: {now_time=1529287017, join_type=2, rebate_switch={"web":0,"ios":0,"android":0,"pc":0}, prize_img=null, prize_num=5, expire_time=1529287317, tuid=0, type=lds, rid=160504, prize_name=皮肤三连包, ftype=0, activity_type=1, join_condition={"gift_id":1027,"gift_num":1,"gift_name":"\u836f\u4e38","expire_time":300,"lottery_range":3}, roomset=null, catelv2=null, activity_id=2624505, catelv1=null}
		System.out.println("抽奖开始了！");
	}
	@Type("lde")
	public void onLotteryEnd() {
		//未知消息类型lde: {join_type=2, end_type=1, rebate_switch={"web":0,"ios":0,"android":0,"pc":0}, prize_img=null, tuid=0, type=lde, rid=160504, gift_count=258, prize_name=游侠, join_time=307, fans_count=2, ftype=0, ad_list={"alp":-1}, follow_count=13, activity_type=1, roomset=null, catelv2=null, activity_id=2624456, catelv1=null, lottery_range=3, gift_name=药丸, join_count=61, win_list=[{"uid":"8987518","nickname":"\u5200\u54e5\u6211\u7231\u4f60\u5200\u54e5","level":"23"}], prize_type=0}
		System.out.println("抽奖结束了！");
	}
	// ---------------
	// 贵族相关
	// ---------------
	@Type("noble_num_info")
	public void onNobelNumberChange() {
		System.out.println("贵族数量变化");
	}
	@Type("online_noble_list")
	public void onNobelListChange() {
		System.out.println("贵族列表更新");
	}
	@Type("rnewbc")
	public void onRenewNobel(@Param("unk")String uName, @Param("donk")String roomOwner, @Param("nl") String level) {
		System.out.println(uName + "在" + roomOwner + "的房间续费了贵族[" + level + "]");
	}
	@Type("anbc")
	public void onNewNobel(@Param("unk")String uName, @Param("donk")String roomOwner, @Param("nl")String level) {
		System.out.println(uName + "在" + roomOwner + "的房间开通了贵族[" + level + "]");
	}
	@Type("tsboxb")
	public void onBox(@Param("snk") String uName) {
		System.out.println(uName + "给大家送了鱼丸箱子");
	}
	
	// ---------------
	// 未知类型
	// ---------------
	@Type("error")
	public void onError(String code) {
		System.out.println("出错了: " + code);
	}
	@Type("rri")
	public void onRRI(Map<String, String> packet) {
		System.out.println("未知消息rri" + packet);
	}
	@Type("drbm")
	public void onDRBM(Map<String, String> packet) {
		// 好像和抽奖有关？
		System.out.println("未知消息drbm" + packet);
	}
}
