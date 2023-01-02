package net.touchnight;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import okhttp3.*;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;



public class CAIListener extends SimpleListenerHost {
    public static String Authorization = "";
    public static String Id = "";
    public static String MsgId = "";
    public static String CommandPrefix = "/";
    public static long Admin = 10000;
    public CAIListener() throws IOException {
        try {
            Properties pro = new Properties();
            pro.load(new FileInputStream("config/CAIconf.properties"));
            Authorization = pro.getProperty("Authorization");
            Id = pro.getProperty("Id");
            CommandPrefix = pro.getProperty("CommandPrefix");
            Admin = Long.parseLong(pro.getProperty("Admin"));
            System.out.println("已加载配置");
        } catch (FileNotFoundException e) {
            Properties pro = new Properties();
            pro.setProperty("Authorization", "");
            pro.setProperty("Id", "");
            pro.setProperty("CommandPrefix", "/");
            pro.setProperty("Admin", "10000");
            pro.store(new FileOutputStream("config/CAIconf.properties"),"Follow Instruction");
            System.out.println("已创建配置文件");
        }
    }

    @EventHandler
    private ListeningStatus onEvent(MessageEvent event) throws Exception {
        String msg = event.getMessage().serializeToMiraiCode();
        String realMsg = event.getMessage().contentToString();
        String at = "[mirai:at:" + event.getBot().getId() + "]";
        String realAt = "@" + event.getBot().getId();
        if (msg.contains(at)) {
            if (msg.startsWith(at)) {
                msg = msg.substring(at.length());
                realMsg = realMsg.substring(realAt.length());
            }
            msg = msg.replace(at, getName(Id));
            realMsg = realMsg.replace(realAt, getName(Id));
            MessageChain chain = new MessageChainBuilder()
                    .append(new QuoteReply(event.getMessage()))
                    .append(getAns(realMsg).getString("content"))
                    .build();
            event.getSubject().sendMessage(chain);}
        if (msg.startsWith(CommandPrefix)) {
            msg = msg.substring(CommandPrefix.length());
            if (msg.equals("重启对话")){
                Flush(event);
            }
            if (msg.startsWith("重设ID")){
                resetID(msg, event);
            }
            if (msg.startsWith("重设密钥")){
                resetAuth(msg, event);
            }
            if (msg.startsWith("重设命令前缀")){
                resetPrefix(msg, event);
            }
            if (msg.equals("重发消息")){
                reSend(MsgId, event);
            }
            if (msg.equals("微信登录")){
                WeChatLogin(event);
            }
        }
        return ListeningStatus.LISTENING;
    }

    private  void resetID(String msg, MessageEvent event) throws Exception {
        if (event.getSender().getId() == Admin){
            String newID = msg.substring("重设ID".length() + 1);
            Id = newID;
            MessageChain chain = new MessageChainBuilder()
                    .append("已重设ID为" + newID + "，现在我是" + getName(newID))
                    .build();
            event.getSubject().sendMessage(chain);
        }
    }

    private  void resetAuth(String msg, MessageEvent event) throws Exception {
        if (event.getSender().getId() == Admin){
            String newAuth = msg.substring("重设密钥".length() + 1);
            Authorization = newAuth;
            String shortAuth = newAuth;
            if (newAuth.length() > 9) {
                shortAuth = newAuth.substring(0,5) + "..." + newAuth.substring(newAuth.length() - 4);
            }
            MessageChain chain = new MessageChainBuilder()
                    .append("已重设密钥为" + shortAuth)
                    .build();
            event.getSubject().sendMessage(chain);
        }
    }

    private  void resetPrefix(String msg, MessageEvent event) throws Exception {
        if (event.getSender().getId() == Admin) {
            String newPrefix = msg.substring("重设命令前缀" .length() + 1);
            Authorization = newPrefix;
            MessageChain chain = new MessageChainBuilder()
                    .append("已重设命令前缀为" + newPrefix)
                    .build();
            event.getSubject().sendMessage(chain);
        }
    }

    private JSONObject getAns(String msg) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"content\":\"" + msg + "\",\"roleId\":" + Id + "}");
        Request request = new Request.Builder()
                .url("https://www.ai-topia.com/mr/chat/sendChat")
                .method("POST", body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0")
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Referer", "https://www.ai-topia.com/")
                .addHeader("Origin", "https://www.ai-topia.com")
                .addHeader("Te", "trailers")
                .addHeader("Authorization", Authorization)
                .build();
        Response response = client.newCall(request).execute();
        String stringAns1 = response.body().string();
        JSONObject jsonAns = JSONObject.parseObject(stringAns1);
        String stringAns2 = jsonAns.getString("data");
        JSONObject jsonAns2 = JSONObject.parseObject(stringAns2);
        MsgId = jsonAns2.getString("id");
        System.out.println(msg);
        response.close();
        return jsonAns2;
    }

    private String reSend(String id, MessageEvent event) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(50, TimeUnit.SECONDS)
                    .readTimeout(50, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url("https://www.ai-topia.com/mr/chat/newChat?id=" + id)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0")
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                    .addHeader("Referer", "https://www.ai-topia.com/")
                    .addHeader("Te", "trailers")
                    .addHeader("Authorization", Authorization)
                    .build();
            Response response = client.newCall(request).execute();
            String stringAns1 = response.body().string();
            JSONObject jsonAns = JSONObject.parseObject(stringAns1);
            String stringAns2 = jsonAns.getString("data");
            JSONObject jsonAns2 = JSONObject.parseObject(stringAns2);
            String reSendAns = jsonAns2.getString("content");
            MessageChain chain = new MessageChainBuilder()
                    .append(new QuoteReply(event.getMessage()))
                    .append(reSendAns)
                    .build();
            event.getSubject().sendMessage(chain);
            return reSendAns;
        } catch (Exception e) {
            String error = "还没有消息，无法重发";
            MessageChain chaine = new MessageChainBuilder()
                    .append(error)
                    .build();
            event.getSubject().sendMessage(chaine);
            return error;
        }
    }

    private String getHello(String id) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url("https://www.ai-topia.com/mr/role/get/" + id)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0")
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Referer", "https://www.ai-topia.com/")
                .addHeader("Origin", "https://www.ai-topia.com")
                .addHeader("Te", "trailers")
                .addHeader("Authorization", Authorization)
                .build();
        Response response = client.newCall(request).execute();
        String stringAns1 = response.body().string();
        JSONObject jsonAns = JSONObject.parseObject(stringAns1);
        String stringAns2 = jsonAns.getString("data");
        JSONObject jsonAns2 = JSONObject.parseObject(stringAns2);
        String Hello = jsonAns2.getString("greeting");
        response.close();
        return Hello;
    }

    private void Flush(MessageEvent event) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://www.ai-topia.com/mr/chat/clearContext?roleId=" + Id)
                .method("POST", body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0")
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Referer", "https://www.ai-topia.com/")
                .addHeader("Origin", "https://www.ai-topia.com")
                .addHeader("Te", "trailers")
                .addHeader("Authorization", Authorization)
                .build();
        Response response = client.newCall(request).execute();
        MessageChain reply = new MessageChainBuilder()
                .append("已重启对话")
                .build();
        MessageChain Hello = new MessageChainBuilder()
                .append(new QuoteReply(event.getMessage()))
                .append(getHello(Id))
                .build();
        event.getSubject().sendMessage(reply);
        event.getSubject().sendMessage(Hello);
        response.close();
    }

    private String getName(String id) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"pageNum\":0,\"pageSize\":20}");
        Request request = new Request.Builder()
                .url("https://www.ai-topia.com/mr/statistics/useRoleList")
                .method("POST", body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0")
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Referer", "https://www.ai-topia.com/")
                .addHeader("Origin", "https://www.ai-topia.com")
                .addHeader("Te", "trailers")
                .addHeader("Authorization", Authorization)
                .build();
        Response response = client.newCall(request).execute();
        String stringAns1 = response.body().string();
        JSONObject jsonAns = JSONObject.parseObject(stringAns1);
        String stringAns2 = jsonAns.getString("data");
        JSONObject jsonAns2 = JSONObject.parseObject(stringAns2);
        JSONArray contentArray = jsonAns2.getJSONArray("content");
        String name = "";
        for (int i = 0; i < contentArray.size(); i++) {
            String RoleId = contentArray.getJSONObject(i).getString("roleId");
            String RoleName = contentArray.getJSONObject(i).getString("roleName");
            if (RoleId.equals(id)) {
                name = RoleName;
            }
        }
        response.close();
        return name;
    }
    private void WeChatLogin(MessageEvent event) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"redirect_uri\":\"https://www.ai-topia.com/#/authentication/login\"}");
        Request request = new Request.Builder()
                .url("https://www.ai-topia.com/mr/wechat/loginUrl")
                .method("POST", body)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0")
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Referer", "https://www.ai-topia.com/")
                .addHeader("Origin", "https://www.ai-topia.com")
                .addHeader("Te", "trailers")
                .build();
        Response response = client.newCall(request).execute();
        String stringAns1 = response.body().string();
        JSONObject jsonAns = JSONObject.parseObject(stringAns1);
        String stringAns2 = jsonAns.getString("data");
        response.close();


        OkHttpClient client2 = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        Request request2 = new Request.Builder()
                .url(stringAns2)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Referer", "https://www.ai-topia.com/")
                .addHeader("Te", "trailers")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .build();
        Response response2 = client2.newCall(request2).execute();
        String QRhtml = response2.body().string();
        String QRsrc = QRhtml.substring(QRhtml.indexOf("<img class=\"web_qrcode_img\" src=\"") + "<img class=\"web_qrcode_img\" src=\"".length(), QRhtml.indexOf("<img class=\"web_qrcode_img\" src=\"") + "<img class=\"web_qrcode_img\" src=\"".length() + 32);
        Image QRcode = Contact.uploadImage(event.getSender(), new URL("https://open.weixin.qq.com" + QRsrc).openConnection().getInputStream());
        MessageChain chain = new MessageChainBuilder()
                .append(new QuoteReply(event.getMessage()))
                .append("请使用微信摄像头扫描")
                .append(QRcode)
                .build();
        event.getSubject().sendMessage(chain);
        response2.close();


        Req3(QRsrc, stringAns2, event);
    }
    private void Req3(String QRsrc, String ans2, MessageEvent event) throws IOException {
        OkHttpClient client3 = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        Request request3 = new Request.Builder()
                .url("https://lp.open.weixin.qq.com/connect/l/qrconnect?uuid=" + QRsrc.substring("/connect/qrcode/".length()) + "&_=1671970491092")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0")
                .addHeader("Accept", "*/*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Referer", "https://open.weixin.qq.com/")
                .addHeader("Te", "trailers")
                .build();
        Response response3 = client3.newCall(request3).execute();
        String wxresponse = response3.body().string();
        response3.close();
        if (wxresponse.startsWith("window.wx_errcode=408;")){
            Req3(QRsrc, ans2, event);
        } else {
            if (wxresponse.startsWith("window.wx_errcode=404;")){
                Req4(QRsrc, ans2, event);
            }
        }
    }

    private void Req4(String QRsrc, String ans2, MessageEvent event) throws IOException {
        OkHttpClient client4 = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        Request request4 = new Request.Builder()
                .url("https://lp.open.weixin.qq.com/connect/l/qrconnect?uuid=" + QRsrc.substring("/connect/qrcode/" .length()) + "&last=404&_=1671970491093")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0")
                .addHeader("Accept", "*/*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Referer", "https://open.weixin.qq.com/")
                .addHeader("Te", "trailers")
                .build();
        Response response4 = client4.newCall(request4).execute();
        String wxresponse4 = response4.body().string();
        response4.close();
        if (wxresponse4.startsWith("window.wx_errcode=408;")) {
            Req4(QRsrc, ans2, event);
        } else {
            if (wxresponse4.startsWith("window.wx_errcode=405;")) {
                String wxcode = wxresponse4.substring("window.wx_errcode=405;window.wx_code='" .length(), "window.wx_errcode=405;window.wx_code='" .length() + 32);
                Req5(wxcode, ans2, event);
            }
        }
    }

    private void Req5(String wxcode, String ans2, MessageEvent event) throws IOException {
        OkHttpClient client5 = new OkHttpClient.Builder()
                .connectTimeout(50, TimeUnit.SECONDS)
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        Request request5 = new Request.Builder()
                .url("https://www.ai-topia.com/mr/wechat/callBack?code=" + wxcode + "&state=" + ans2.substring(ans2.indexOf("&state=") + "&state=".length(), ans2.indexOf("&state=") + "&state=".length() + 36))
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0")
                .addHeader("Accept", "*/*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Referer", "https://open.weixin.qq.com/")
                .addHeader("Te", "trailers")
                .build();
        Response response5 = client5.newCall(request5).execute();
        String token = response5.body().string();
        JSONObject jsonToken = JSONObject.parseObject(token);
        String stringToken2 = jsonToken.getString("data");
        JSONObject jsonToken2 = JSONObject.parseObject(stringToken2);
        String AccessToken = jsonToken2.getString("access_token");
        response5.close();
        Authorization = AccessToken;
        MessageChain chain = new MessageChainBuilder()
                .append(new QuoteReply(event.getMessage()))
                .append("已成功登录")
                .build();
        event.getSubject().sendMessage(chain);
    }
}
