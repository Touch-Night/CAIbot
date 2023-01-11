原本是针对[Character.AI](beta.character.ai)写的，但是遇到了我解决不了的问题+=[相同的包只有用burpsuite发才有正常response]=+，就改成现在这样了。
开源地址：https://github.com/Touch-Night/CAIbot  
**安装方法**
1.进入[Ai乌托邦](https://www.ai-topia.com)，登录并挑选一个想做成机器人的角色，和ta聊一两句，保证ta出现在你的聊天列表中。  
![ede112d3-2028-4806-a2d7-cefd0199c9aa-image.png](/assets/uploads/files/1672726006079-ede112d3-2028-4806-a2d7-cefd0199c9aa-image.png)   

2.在登录后右键，检查，  
![917064c3-c399-47f7-8bdc-686c837d866d-image.png](/assets/uploads/files/1672727973040-917064c3-c399-47f7-8bdc-686c837d866d-image.png) 
选中+=[应用程序选单中]=+`存储`一栏下的`本地存储`，在里面可以得到你的`ACCESS_TOKEN`+=[这个可以先不管，后面可以直接扫二维码登录自动获取TOKEN]=+  
![7107dca0-532a-4fdb-80f7-83bce14c68ef-image.png](/assets/uploads/files/1672726250311-7107dca0-532a-4fdb-80f7-83bce14c68ef-image.png)  
 ，之后，选中`网络`一栏以开启网页监听，向角色发送一条消息，在下方记录的日志中找到`名称`一栏，选择其中的`sendChat`一项，在右边的`预览`一栏中的第三行（开头为`data:`），可以看到`roleId: `，它后面的数字就是`角色Id`  
![e2bf108b-d145-440e-b0f2-5ea2b7c3d13f-image.png](/assets/uploads/files/1672726423792-e2bf108b-d145-440e-b0f2-5ea2b7c3d13f-image.png)   

3.在此处下载插件：[release](https://github.com/Touch-Night/CAIbot/releases/v0.2)
4.将它放到plugins文件夹中 
5.运行一遍生成配置文件，配置文件在Config文件夹中，文件名为`CAIconf.properties`  
6.停止运行mirai，编辑好上述配置文件。其中，  
> `Authorization`为你的密钥
>`Id`为对应角色的ID
>`CommandPrefix`为指令前缀
>`InitAdmin`为唯一的初始超级管理员，拥有最高权限，且只能通过修改配置文件来变更
>`Admins`为指定的其他管理员，使用英文半角逗号分隔想要设为管理员的qq号
>`NoPower`为权限不足时的提示词

7.重新运行mirai。
8.建议第一次使用时先`/微信登录` 
**指令**  
`/重启对话`重置聊天，从头开始  
![e30637f2-e13f-4d79-98a3-48116ee69a52-Screenshot_20230103_141442.jpg](/assets/uploads/files/1672727099571-e30637f2-e13f-4d79-98a3-48116ee69a52-screenshot_20230103_141442.jpg)   

`/重设ID newID`将newID对应的角色与机器人连接，当角色不在前述聊天列表中时，机器人将会提醒你  
![05b89d3f-8665-405a-bebe-70ebf6db0ce0-Screenshot_20230103_141704.jpg](/assets/uploads/files/1672727127410-05b89d3f-8665-405a-bebe-70ebf6db0ce0-screenshot_20230103_141704.jpg)   

`/重设密钥 newAuth`旧的密钥过期后，将密钥更新为newAuth+=[自带防刷屏]=+  
![6d537f64-4580-46e2-8819-397b986bac4a-Screenshot_20230103_141825.jpg](/assets/uploads/files/1672727233362-6d537f64-4580-46e2-8819-397b986bac4a-screenshot_20230103_141825.jpg)   

`/重设命令前缀 newPrefix`将指令前缀设为newPrefix+=[之前坏掉了，原来是我变量名写错了，哈哈]=+  
![41dc5664-d4e8-43d8-8e3b-3ab5c09d4f1b-Screenshot_20230103_142201.jpg](/assets/uploads/files/1672727192458-41dc5664-d4e8-43d8-8e3b-3ab5c09d4f1b-screenshot_20230103_142201.jpg)   

`/重发消息`重新生成一条回复  
![7323d120-ce3d-4145-88ab-48555b944636-Screenshot_20230103_142349.jpg](/assets/uploads/files/1672727285313-7323d120-ce3d-4145-88ab-48555b944636-screenshot_20230103_142349.jpg)   

`/微信登录`获得微信登录二维码，用微信通过摄像头扫描后自动获取TOKEN  
![2bb92a16-a018-4cad-ab27-855d02c48524-Screenshot_20230103_142235.jpg](/assets/uploads/files/1672727398118-2bb92a16-a018-4cad-ab27-855d02c48524-screenshot_20230103_142235.jpg)   

`/添加管理员 <member>`将<member>设为管理员，<member>的输入可以用@，也可以用qq号
`/移除管理员 <member>`撤销<member>的管理员身份，<member>的输入可以用@，也可以用qq号+=[似乎有点问题]=+
![c4febaf6-7657-4dc5-9b16-d80ff74b4d20-IMG_20230111_174600.jpg](/assets/uploads/files/1673430406467-c4febaf6-7657-4dc5-9b16-d80ff74b4d20-img_20230111_174600.jpg) 

`/重设权限不够时的回复 <String>`让机器人在权限不足的人使用指令时以<String>回复
![4a762f19-393a-4508-8828-096188e1ed2a-Screenshot_20230111_174847.jpg](/assets/uploads/files/1673430564417-4a762f19-393a-4508-8828-096188e1ed2a-screenshot_20230111_174847.jpg) 

*其中重设ID、重设密钥、重设权限不够时的回复为管理员指令*
*重设命令前缀、添加管理员、移除管理员为超级管理员指令*
**使用方法**
1.在聊天内容中包含提及机器人会使得这条消息被当成消息输入。当@在消息的开头时，它会被删去；当@在消息的中间时，它会被替换成当前角色ID对应的角色名。
2.在聊天内容中包含指令可执行对应指令。
**Tips**  
0.1.0版本的用户在使用0.2.0版本时应删除配置文件，重新生成配置文件并配置。
**未来计划**
1.完全改成使用官方提供的api实现Ai乌托邦的消息获取
