# SipVoice

该 demo 基于[https://github.com/VoiSmart/pjsip-android](https://github.com/VoiSmart/pjsip-android)
项目实现语音对讲对讲功能。    
还有很多关于 pjsip 的功能和 API 请移步 pjsip-android。  
    
正常运行该 Demo ，需要已有 Sip 服务端，Constants 中配置 ip 和端口。 

解决了一个坑，通话2分钟必定断开，在 pjsip-android 提了 issues ,[https://github.com/VoiSmart/pjsip-android/issues/89](https://github.com/VoiSmart/pjsip-android/issues/89)  
估计他们使用的服务器不要客户端发心跳，所以一直没有找到答案，不过还是非常感谢 pjsip-android 团队。

后面通过抓win软件的包，与同事沟通，找资料等方式，得知客户端需要主动发心跳，这才解决。
sip是支持发送自定义数据的。
``
SipCall.sendRequest ()
``
详情看demo中的 CallOutActivity 的 160行  

[https://blog.csdn.net/netnote/article/details/3857191](https://blog.csdn.net/netnote/article/details/3857191)  
这篇文章也给了我启示  
    
demo 中，实现了基本的语音对讲的功能，拨号，接听，静音，外放，计时等...  
  
  
拨号方：      
<img width = "250" height = "450" src="https://github.com/zhanglihow/SipVoice/blob/master/img/callout_1.png?raw=true" />
<img width = "250" height = "450" src="https://github.com/zhanglihow/SipVoice/blob/master/img/callout_2.png?raw=true" />
<img width = "250" height = "450" src="https://github.com/zhanglihow/SipVoice/blob/master/img/callout_3.png?raw=true" />


接听方：    
<img width = "250" height = "450" src="https://github.com/zhanglihow/SipVoice/blob/master/img/callin_1.png?raw=true" />
<img width = "250" height = "450" src="https://github.com/zhanglihow/SipVoice/blob/master/img/callin_2.png?raw=true" />
<img width = "250" height = "450" src="https://github.com/zhanglihow/SipVoice/blob/master/img/callin_3.png?raw=true" />

