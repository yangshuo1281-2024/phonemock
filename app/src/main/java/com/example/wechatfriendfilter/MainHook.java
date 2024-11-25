package com.example.wechatfriendfilter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private static final String WECHAT_PACKAGE = "com.tencent.mm";
    private static final String TARGET_WXID = "wxid_2f39dtrtkvyq22"; // 需要替换为目标好友的微信ID

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(WECHAT_PACKAGE)) {
            return;
        }

        // 获取好友 wxid
        XposedHelpers.findAndHookMethod(
            "com.tencent.mm.ui.contact.ContactInfoUI",
            lpparam.classLoader,
            "onCreate",
            Bundle.class,
            new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    Intent intent = activity.getIntent();
                    String username = intent.getStringExtra("Contact_User");
                    
                    // 显示一个 Toast 消息，展示好友的 wxid
                    if (username != null) {
                        Toast.makeText(activity, "好友ID: " + username, Toast.LENGTH_LONG).show();
                    }
                }
            }
        );

        // hook 会话列表
        XposedHelpers.findAndHookMethod(
            "com.tencent.mm.ui.conversation.ConversationAdapter",
            lpparam.classLoader,
            "getItem",
            int.class,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Object item = param.getResult();
                    if (item != null) {
                        String field_username = (String) XposedHelpers.getObjectField(item, "field_username");
                        if (TARGET_WXID.equals(field_username)) {
                            param.setResult(null);
                        }
                    }
                }
            }
        );

        // hook 消息接收
        XposedHelpers.findAndHookMethod(
            "com.tencent.mm.model.aj",
            lpparam.classLoader,
            "handleMessage",
            "com.tencent.mm.protocal.protobuf.Message",
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Object message = param.args[0];
                    String fromUser = (String) XposedHelpers.getObjectField(message, "fromUser");
                    if (TARGET_WXID.equals(fromUser)) {
                        param.setResult(null);
                    }
                }
            }
        );
    }
} 