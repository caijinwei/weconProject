package com.wecon.box.action;

import com.alibaba.fastjson.JSONObject;
import com.wecon.box.api.FileStorageApi;
import com.wecon.box.constant.ConstKey;
import com.wecon.box.entity.FileStorage;
import com.wecon.box.enums.ErrorCodeOption;
import com.wecon.box.enums.OpTypeOption;
import com.wecon.box.enums.ResTypeOption;
import com.wecon.box.util.BoxWebConfigContext;
import com.wecon.box.util.DbLogUtil;
import com.wecon.common.redis.RedisManager;
import com.wecon.common.util.CommonUtils;
import com.wecon.restful.annotation.WebApi;
import com.wecon.restful.core.AppContext;
import com.wecon.restful.core.BusinessException;
import com.wecon.restful.core.Output;
import com.wecon.restful.core.Session;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zengzhipeng on 2017/9/8.
 */
@RestController
@RequestMapping("/fileact")
public class FileAction {
    private static final Logger logger = LogManager.getLogger(FileAction.class.getName());

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

    @Autowired
    FileStorageApi fileStorageApi;

    @Autowired
    protected DbLogUtil dbLogUtil;

    @Description("上传文件接口")
    @RequestMapping(value = "/fileupload")
    @WebApi(forceAuth = true, master = true, authority = {"0"})
    public Output fileUploadAction(@RequestParam("act") String act) throws IOException, ServletException {
        logger.debug("开始上传");
        final Session session = AppContext.getSession();
        final HttpServletRequest request = session.request;
        final Collection<Part> parts = session.request.getParts();
        if (parts.isEmpty()) {
            throw new BusinessException(ErrorCodeOption.UploadFileParamEmpty.key, ErrorCodeOption.UploadFileParamEmpty.value);
        }
        Part partFile = session.request.getPart("file");

        logger.debug("filename:" + getFileName(partFile));

        FileStorage model = new FileStorage();
        model.file_name = getFileName(partFile);
        model.file_type = model.file_name.substring(model.file_name.lastIndexOf(".") + 1);
        model.file_data = IOUtils.toByteArray(partFile.getInputStream());
        model.file_md5 = DigestUtils.md5Hex(model.file_data);
        model.file_size = Long.valueOf(model.file_data.length);
        JSONObject dataOut = new JSONObject();
        File tempFile = File.createTempFile(model.file_name, "");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(model.file_data);
        fos.close();
        if (act.equals("firm")) {
            //<editor-fold desc="对固件文件特殊处理,获取版本信息">
            FileInputStream fis = new FileInputStream(tempFile);
            fis.getChannel().position(fis.getChannel().size() - 256);
            byte[] a = new byte[256];
            fis.read(a);
            fis.close();
            String verStr = new String(a);
            try {
                String[] strs1 = verStr.split("VER:");
                String[] strs2 = strs1[1].split(" ");
                verStr = strs2[0];
            } catch (Exception e) {
                throw new BusinessException(ErrorCodeOption.FileGetVerError.key, ErrorCodeOption.FileGetVerError.value);
            }
            dataOut.put("version_code", verStr);
            dataOut.put("version_name", model.file_name.substring(0, model.file_name.lastIndexOf(".") - 1));
            //</editor-fold>
        } else if (act.equals("driver")) {
            //<editor-fold desc="对驱动文件特殊处理,获取最后修改时间--文件原始的最后修改没有获取到-后面使用md5">
            /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(tempFile.lastModified());
            model.file_lasttime = sdf.format(cal.getTime());
            dataOut.put("file_lasttime", model.file_lasttime);*/
            //</editor-fold>
            //驱动名
            dataOut.put("driver_name", model.file_name.substring(0, model.file_name.lastIndexOf(".")));
        }

        fileStorageApi.addFileStorage(model);
        dataOut.put("file_id", model.file_id);
        dataOut.put("file_name", model.file_name);
        dataOut.put("file_md5", model.file_md5);
        dataOut.put("file_size", model.file_size);
        String token = UUID.randomUUID().toString().replace("-", "");
        String redisKey = String.format(ConstKey.REDIS_FILE_DOWNLOAD_TOKEN, model.file_id);
        RedisManager.set(ConstKey.REDIS_GROUP_NAME, redisKey, token, ConstKey.FILEDOWNLOAD_EXPIRE_TIME);//保存一小时
        String url = BoxWebConfigContext.boxWebConfig.getFileDownloadUrl() + "?id=" + model.file_id + "&t=" + token;
        dataOut.put("file_url", url);

        //<editor-fold desc="操作日志">
        dbLogUtil.addOperateLog(OpTypeOption.AddFile, ResTypeOption.File, model.file_id, dataOut);
        //</editor-fold>
        logger.debug("上传成功");
        return new Output(dataOut);
    }

    @Description("下载文件接口")
    @RequestMapping(value = "/filedownload", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Resource> fileDownloadAction(@RequestParam("id") Long id, @RequestParam("t") String t) throws IOException {
        String redisKey = String.format(ConstKey.REDIS_FILE_DOWNLOAD_TOKEN, id);
        String token = RedisManager.get(ConstKey.REDIS_GROUP_NAME, redisKey);
        if (CommonUtils.isNullOrEmpty(token) || !token.equals(t)) {
            throw new BusinessException(ErrorCodeOption.DownloadFileParamError.key, ErrorCodeOption.DownloadFileParamError.value);
        }

        FileStorage model = fileStorageApi.getFileStorage(id);
        File tempFile = File.createTempFile(model.file_name, "");
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(model.file_data);
        fos.close();

        String fileName = model.file_name;
        Resource body = new FileSystemResource(tempFile);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        String header = request.getHeader("User-Agent").toUpperCase();
        HttpStatus status = HttpStatus.CREATED;
        try {
            if (header.contains("MSIE") || header.contains("TRIDENT") || header.contains("EDGE")) {
                fileName = URLEncoder.encode(fileName, "UTF-8");
                fileName = fileName.replace("+", "%20");    // IE下载文件名空格变+号问题
                status = HttpStatus.OK;
            } else {
                fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            }
        } catch (UnsupportedEncodingException e) {
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentLength(tempFile.length());

        return new ResponseEntity<Resource>(body, headers, status);
    }

    /**
     * 获取文件名
     *
     * @param part
     * @return
     */
    private String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim()
                        .replace("\"", "");
            }
        }
        return null;
    }
}