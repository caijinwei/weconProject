/**
 * Created by zengzhipeng on 2017/8/5.
 */
$(function () {
    var params =
    {
        type: T.common.util.getParameter("t"),
        uid: T.common.util.getParameter("u"),
        token: T.common.util.getParameter("token")
    };
    T.common.ajax.request("WeconBox", "user/signupemailactive", params, function (data, code, msg) {
        if (code == 200) {
            if (params.type == 1) {
                alert("激活成功");
                //T.common.user.setSid(data.sid);
                location = "login.html";
            } else {
                $("#title").html("激活成功");
            }
        }
        else {
            alert(code + " " + msg);
            $("#title").html("激活失败，请使用邮件中的激活地址");
        }
    });
})