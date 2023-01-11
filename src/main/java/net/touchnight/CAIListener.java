package net.touchnight;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;



public class CAIListener extends SimpleListenerHost {
    public static String Authorization = "";
    public static String Id = "";
    public static String MsgId = "";
    public static String CommandPrefix = "/";
    public static long InitAdmin = 10000;
    public static ArrayList<String> Admins;
    public static String NoPower = "你的权限不够";
    public CAIListener() throws IOException {
        try {
            Properties pro = new Properties();
            pro.load(new FileInputStream("config/CAIconf.properties"));
            Authorization = pro.getProperty("Authorization");
            Id = pro.getProperty("Id");
            CommandPrefix = pro.getProperty("CommandPrefix");
            InitAdmin = Long.parseLong(pro.getProperty("InitAdmin"));
            Admins = new ArrayList<>(Arrays.asList(pro.getProperty("Admins").split(",")));
            NoPower = pro.getProperty("NoPower");
            System.out.println("已加载配置");
        } catch (FileNotFoundException e) {
            Properties pro = new Properties();
            pro.setProperty("Authorization", "");
            pro.setProperty("Id", "");
            pro.setProperty("CommandPrefix", "/");
            pro.setProperty("InitAdmin", "10000");
            pro.setProperty("Admins", "10001");
            pro.setProperty("NoPower", "你的权限不够");
            pro.store(new FileOutputStream("config/CAIconf.properties"),"see https://mirai.mamoe.net/topic/1904/caibot-%E8%BF%9E%E6%8E%A5ai%E4%B9%8C%E6%89%98%E9%82%A6%E4%B8%8Eqq%E8%81%8A%E5%A4%A9%E6%9C%BA%E5%99%A8%E4%BA%BA for instuctions");
            System.out.println("已创建配置文件");
        }
    }

    @EventHandler
    private ListeningStatus onEvent(MessageEvent event) throws Exception {
        String msg = event.getMessage().serializeToMiraiCode();
        String realMsg = event.getMessage().contentToString();
        String at = "[mirai:at:" + event.getBot().getId() + "]";
        String realAt = "@" + event.getBot().getId();
        if (msg.contains(at) && !msg.startsWith(CommandPrefix)) {
            if (msg.startsWith(at)) {
                msg = msg.substring(at.length());
                realMsg = realMsg.substring(realAt.length());
            }
            msg = msg.replace(at, getName(Id));
            realMsg = realMsg.replace(realAt, getName(Id));
            if (getName(Id) == ""){
                if (Id == "") {
                    MessageChain chain = new MessageChainBuilder()
                            .append(new QuoteReply(event.getMessage()))
                            .append(getAns(realMsg, event).getString("我还不知道我是谁，请先用 " + CommandPrefix + "重设ID <newID> 指令来让我知道我的身份"))
                            .build();
                    event.getSubject().sendMessage(chain);
                } else {
                    MessageChain chain = new MessageChainBuilder()
                            .append(new QuoteReply(event.getMessage()))
                            .append(getAns(realMsg, event).getString("我不认识" + Id + "这个ID对应的角色，它似乎没有出现在你AI乌托邦的聊天列表中"))
                            .build();
                    event.getSubject().sendMessage(chain);
                }
            } else {
                MessageChain chain = new MessageChainBuilder()
                        .append(new QuoteReply(event.getMessage()))
                        .append(getAns(realMsg, event).getString("content"))
                        .build();
                event.getSubject().sendMessage(chain);
            }
        }
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
            if (msg.startsWith("添加管理员")){
                op(msg, event);
            }
            if (msg.startsWith("移除管理员")){
                deop(msg, event);
            }
            if (msg.startsWith("重设权限不够时的回复")){
                noPower(msg, event);
            }
        }
        return ListeningStatus.LISTENING;
    }

    private void resetID(String msg, MessageEvent event) {
        if (event.getSender().getId() == InitAdmin || Admins.contains(Long.toString(event.getSender().getId()))){
            String newID = "";
            try {
                newID = msg.substring("重设ID".length() + 1);
            } catch (StringIndexOutOfBoundsException e) {
                newID = "";
            }
            if (getName(newID) == ""){
                if (newID == "") {
                    MessageChain chain = new MessageChainBuilder()
                            .append("我的ID在哪？我怎么没看见？")
                            .build();
                    event.getSubject().sendMessage(chain);
                } else {
                    MessageChain chain = new MessageChainBuilder()
                            .append("我不认识" + newID + "这个ID对应的角色，它应该没有出现在你在AI乌托邦的聊天列表里吧，也有可能你还没有微信登录。ID应该是一串不长的数字")
                            .build();
                    event.getSubject().sendMessage(chain);
                }
            } else {
                Id = newID;
                try {
                    Properties pro = new Properties();
                    pro.load(new FileInputStream("config/CAIconf.properties"));
                    OutputStream ops = new FileOutputStream("config/CAIconf.properties");
                    pro.setProperty("Id", newID);
                    pro.store(ops, "更改了角色ID");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("更新newID出错");
                }
                MessageChain chain = new MessageChainBuilder()
                        .append("已重设ID为" + newID + "，现在我是" + getName(newID))
                        .build();
                event.getSubject().sendMessage(chain);
            }
        } else {
            if (NoPower != ""){
                MessageChain chain = new MessageChainBuilder()
                        .append(NoPower)
                        .build();
                event.getSubject().sendMessage(chain);
            }
        }
    }

    private void resetAuth(String msg, MessageEvent event) {
        if (event.getSender().getId() == InitAdmin || Admins.contains(Long.toString(event.getSender().getId()))){
            String newAuth = "";
            try {
                newAuth = msg.substring("重设密钥".length() + 1);
            } catch (StringIndexOutOfBoundsException e) {
                newAuth = "";
            }
            Authorization = newAuth;
            try {
                Properties pro = new Properties();
                pro.load(new FileInputStream("config/CAIconf.properties"));
                OutputStream ops = new FileOutputStream("config/CAIconf.properties");
                pro.setProperty("Authorization", newAuth);
                pro.store(ops, "更改了登录密钥");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("更新newAuth出错");
            }
            String shortAuth = newAuth;
            if (newAuth.length() > 9) {
                shortAuth = newAuth.substring(0,5) + "..." + newAuth.substring(newAuth.length() - 4);
            }
            if (newAuth.length() <= 0){
                shortAuth = "...等下，你在指令后面什么都没写呢";
            }
            MessageChain chain = new MessageChainBuilder()
                    .append("已重设密钥为" + shortAuth)
                    .build();
            event.getSubject().sendMessage(chain);
        } else {
            if (NoPower != ""){
                MessageChain chain = new MessageChainBuilder()
                        .append(NoPower)
                        .build();
                event.getSubject().sendMessage(chain);
            }
        }
    }

    private void resetPrefix(String msg, MessageEvent event) {
        if (event.getSender().getId() == InitAdmin) {
            String newPrefix = "";
            try{
                newPrefix = msg.substring("重设命令前缀" .length() + 1);
            } catch (StringIndexOutOfBoundsException e) {
                newPrefix = "";
            }
            if (newPrefix.length() <=0 ) {
                MessageChain chain = new MessageChainBuilder()
                        .append("我的命令前缀呢？")
                        .build();
                event.getSubject().sendMessage(chain);
            } else {
                CommandPrefix = newPrefix;
                try {
                    Properties pro = new Properties();
                    pro.load(new FileInputStream("config/CAIconf.properties"));
                    OutputStream ops = new FileOutputStream("config/CAIconf.properties");
                    pro.setProperty("CommandPrefix", newPrefix);
                    pro.store(ops, "更改了命令前缀");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("更新newPrefix出错");
                }
                MessageChain chain = new MessageChainBuilder()
                        .append("已重设命令前缀为" + newPrefix)
                        .build();
                event.getSubject().sendMessage(chain);
            }
        } else {
            if (NoPower != ""){
                MessageChain chain = new MessageChainBuilder()
                        .append(NoPower)
                        .build();
                event.getSubject().sendMessage(chain);
            }
        }
    }

    private JSONObject getAns(String msg, MessageEvent event) {
        try{
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
        } catch (Exception e) {
            MessageChain chain = new MessageChainBuilder()
                    .append("似乎密钥未填写或已经过期，使用 " + CommandPrefix + "微信登录 来重新获取密钥吧")
                    .build();
            event.getSubject().sendMessage(chain);
            return null;
        }
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

    private String getHello(String id) {
        try{
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
        } catch (Exception e) {
            return "似乎密钥未填写或已经过期，使用 " + CommandPrefix + "微信登录 来重新获取密钥吧";
        }
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

    private String getName(String id) {
        try {
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
        } catch (Exception e) {
            return "";
        }
    }
    private void op(String msg, MessageEvent event) {
        if (event.getSender().getId() == InitAdmin) {
            String newAdmin = "";
            try {
                newAdmin = msg.substring("添加管理员" .length() + 1);
            } catch (StringIndexOutOfBoundsException ex) {
                MessageChain chain = new MessageChainBuilder()
                        .append("要添加谁为管理员？")
                        .build();
                event.getSubject().sendMessage(chain);
            }
            String atStart = "[mirai:at:";
            if (newAdmin.startsWith(atStart) && newAdmin != "") {
                newAdmin = newAdmin.substring(atStart.length());
                newAdmin = newAdmin.substring(0, newAdmin.indexOf("]"));
            }
            if (newAdmin == Long.toString(InitAdmin) && newAdmin != "") {
                MessageChain chain = new MessageChainBuilder()
                        .append(new At(InitAdmin))
                        .append("已经是超级管理员了")
                        .build();
                event.getSubject().sendMessage(chain);
            } else {
                if (Admins.contains(newAdmin)){
                    MessageChain chain = new MessageChainBuilder()
                            .append(new At(Long.parseLong(newAdmin)))
                            .append("早就是管理员了")
                            .build();
                    event.getSubject().sendMessage(chain);
                } else {
                    if (Long.parseLong(newAdmin) == event.getBot().getId()) {
                        MessageChain chain = new MessageChainBuilder()
                                .append("我是" + getName(Id) + "，不想当管理员")
                                .build();
                        event.getSubject().sendMessage(chain);
                    } else {
                        Admins.add(newAdmin);
                        try {
                            Properties pro = new Properties();
                            pro.load(new FileInputStream("config/CAIconf.properties"));
                            OutputStream ops = new FileOutputStream("config/CAIconf.properties");
                            pro.setProperty("Admins", String.join(",", Admins));
                            pro.store(ops, "新增了" + newAdmin + "为管理员");
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("更新管理员列表出错");
                        }
                        MessageChain chain = new MessageChainBuilder()
                                .append("已将")
                                .append(new At(Long.parseLong(newAdmin)))
                                .append("设为管理员")
                                .build();
                        event.getSubject().sendMessage(chain);
                    }
                }
            }
        } else {
            if (NoPower != ""){
                MessageChain chain = new MessageChainBuilder()
                        .append(NoPower)
                        .build();
                event.getSubject().sendMessage(chain);
            }
        }
    }

    private void deop(String msg, MessageEvent event) {
        if (event.getSender().getId() == InitAdmin) {
            String delAdmin = "";
            try {
                delAdmin = msg.substring("移除管理员" .length() + 1);
            } catch (StringIndexOutOfBoundsException ex) {
                MessageChain chain = new MessageChainBuilder()
                        .append("要将谁从管理员列表中移除？")
                        .build();
                event.getSubject().sendMessage(chain);
            }
            String atStart = "[mirai:at:";
            if (delAdmin.startsWith(atStart) && delAdmin != "") {
                delAdmin = delAdmin.substring(atStart.length());
                delAdmin = delAdmin.substring(0, delAdmin.indexOf("]"));
            }
            if (delAdmin == Long.toString(InitAdmin) && delAdmin != "") {
                MessageChain chain = new MessageChainBuilder()
                        .append(new At(InitAdmin))
                        .append("不能移除超级管理员")
                        .build();
                event.getSubject().sendMessage(chain);
            } else {
                if (!Admins.contains(delAdmin)){
                    if (Long.parseLong(delAdmin) == event.getBot().getId()) {
                        MessageChain chain = new MessageChainBuilder()
                                .append("我是" + getName(Id) + "，不是管理员")
                                .build();
                        event.getSubject().sendMessage(chain);
                    } else {
                        MessageChain chain = new MessageChainBuilder()
                                .append(new At(Long.parseLong(delAdmin)))
                                .append("本就不是管理员")
                                .build();
                        event.getSubject().sendMessage(chain);
                    }
                } else {
                    Admins.remove(delAdmin);
                    try {
                        Properties pro = new Properties();
                        pro.load(new FileInputStream("config/CAIconf.properties"));
                        OutputStream ops = new FileOutputStream("config/CAIconf.properties");
                        pro.setProperty("Admins", String.join(",", Admins));
                        pro.store(ops, "将" + delAdmin + "从管理员列表中移除了");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("更新管理员列表出错");
                    }
                    MessageChain chain = new MessageChainBuilder()
                            .append("已撤销")
                            .append(new At(Long.parseLong(delAdmin)))
                            .append("的管理员身份")
                            .build();
                    event.getSubject().sendMessage(chain);
                }
            }
        } else {
            if (NoPower != ""){
                MessageChain chain = new MessageChainBuilder()
                        .append(NoPower)
                        .build();
                event.getSubject().sendMessage(chain);
            }
        }
    }
    private void noPower(String msg, MessageEvent event){
        if (event.getSender().getId() == InitAdmin || Admins.contains(Long.toString(event.getSender().getId()))) {
            String newNoPower = "";
            try {
                newNoPower = msg.substring("重设权限不够时的回复" .length() + 1);
            } catch(StringIndexOutOfBoundsException ex) {
                MessageChain chain = new MessageChainBuilder()
                        .append(new At(InitAdmin))
                        .append("已关闭权限不够时的提示信息")
                        .build();
                event.getSubject().sendMessage(chain);
            }
            NoPower = newNoPower;
            try {
                Properties pro = new Properties();
                pro.load(new FileInputStream("config/CAIconf.properties"));
                OutputStream ops = new FileOutputStream("config/CAIconf.properties");
                pro.setProperty("NoPower", newNoPower);
                pro.store(ops, "更改了权限不够时的回复");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("更新newNoPower出错");
            }
            MessageChain chain = new MessageChainBuilder()
                    .append("已重设权限不够时的回复为" + newNoPower)
                    .build();
            event.getSubject().sendMessage(chain);
        } else {
            MessageChain chain = new MessageChainBuilder()
                    .append(NoPower)
                    .build();
            event.getSubject().sendMessage(chain);
        }
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
        try {
            Properties pro = new Properties();
            pro.load(new FileInputStream("config/CAIconf.properties"));
            OutputStream ops = new FileOutputStream("config/CAIconf.properties");
            pro.setProperty("Authorization", AccessToken);
            pro.store(ops, "通过微信登录自动更新了密钥");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("微信登录出错");
        }
        MessageChain chain = new MessageChainBuilder()
                .append(new QuoteReply(event.getMessage()))
                .append("已成功登录")
                .build();
        event.getSubject().sendMessage(chain);
    }
}
