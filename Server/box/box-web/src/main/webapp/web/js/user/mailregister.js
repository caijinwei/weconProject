/**
 * Created by zengzhipeng on 2017/8/2.
 */
$(function () {
    $('#signup').bind('click', function () {
        $("#loadingModal").modal("show");
        var params =
        {
            username: $('#username').val().trim(),
            email: $('#email').val().trim(),
            password: $('#password').val().trim()
        };
        if (params.username == '' || params.password == '' || params.email == '') {
            alert("请填写完整");
            $("#loadingModal").modal("hide");
            return;
        }
        if (params.password != $('#confirmpwd').val().trim()) {
            alert("输入两次的密码不一致");
            $("#loadingModal").modal("hide");
            return;
        }
        params.password = T.common.util.md5(params.password);
        T.common.ajax.request("WeconBox", "user/signupemail", params, function (data, code, msg) {
            $("#loadingModal").modal("hide");
            if (code == 200) {
                alert("注册成功");
            }
            else {
                alert(code + "-" + msg);
            }
        });
    })

})