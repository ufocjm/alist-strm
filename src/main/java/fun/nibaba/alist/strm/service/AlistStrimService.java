package fun.nibaba.alist.strm.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.nibaba.alist.strm.conf.AlistStrmProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

@Slf4j
@AllArgsConstructor
@Service
public class AlistStrimService {

    private final AlistStrmProperties alistStrmProperties;

    private final static String STRM_SUFFIX = ".strm";

    private final static String FILE_LIST = "/api/fs/list";


    /**
     * 执行
     */
    public void execute() {
        this.execute(this.alistStrmProperties.getScanBasicPath());
    }

    private void execute(String scanBasicPath) {
        if (StrUtil.isBlank(scanBasicPath)) {
            scanBasicPath = StrUtil.SLASH;
        }
        HttpRequest httpRequest = this.createPost(FILE_LIST);
        JSONObject requestJson = new JSONObject();
        requestJson.put("path", scanBasicPath);
        httpRequest.body(requestJson.toJSONString());
        HttpResponse httpResponse = httpRequest.execute();
        String result = httpResponse.body();

        JSONObject resultJson = JSONObject.parseObject(result);
        Integer code = resultJson.getInteger("code");
        if (code != null && code == 200) {
            // 成功
            JSONObject dataJson = resultJson.getJSONObject("data");
            if (dataJson == null) {
                return;
            }

            JSONArray contentArray = dataJson.getJSONArray("content");
            if (contentArray == null || contentArray.isEmpty()) {
                return;
            }


            for (int i = 0; i < contentArray.size(); i++) {
                JSONObject itemJson = contentArray.getJSONObject(i);
                String name = itemJson.getString("name");
                Boolean isDir = itemJson.getBoolean("is_dir");
                String alistPath = this.safePath(scanBasicPath, name);
                if (isDir != null && isDir) {
                    // 是文件夹
                    // 创建文件 然后递归下去
                    String currentPath = this.safePath(this.alistStrmProperties.getOutputPath(), scanBasicPath);
                    log.info("创建文件夹:[{}]", currentPath);
                    FileUtil.mkdir(currentPath);

                    this.execute(alistPath);
                } else {
                    // 不是文件夹
                    // 创建strm
                    String alistFile = alistPath;
                    String sign = itemJson.getString("sign");
                    String alist302Url = this.getAlist302Url(alistFile, sign);
                    String suffix = FileUtil.getSuffix(name);
                    if (this.alistStrmProperties.getStrmSuffixList().contains(suffix)) {
                        String strmName = alistFile.substring(0, alistFile.lastIndexOf("."));
                        strmName = strmName + STRM_SUFFIX;
                        log.info("----------开始生成strm文件----------");
                        log.info("alist 302 路径为:[{}]", alist302Url);
                        log.info("strm文件名为:[{}]", strmName);
                        log.info("----------结束生成strm文件----------");
//                        FileUtil.writeString(alist302Url,
//                                this.safePath(this.alistStrmProperties.getOutputPath(), strmName), Charset.defaultCharset());
                    } else {
                        log.info("复制非视频文件:[{}]", alistFile);
//                        HttpUtil.downloadFile(alist302Url, this.safePath(this.alistStrmProperties.getOutputPath(), alistFile));
                    }
                }
            }

        }

    }

    private String safePath(String... paths) {
        for (int i = 0; i < paths.length; i++) {
            if (i != paths.length - 1) {
                if (paths[i].endsWith(StrUtil.SLASH)) {
                    paths[i] = paths[i].substring(0, paths[i].length() - 1);
                }
            }
            if (i != 0) {
                if (paths[i].startsWith(StrUtil.SLASH)) {
                    paths[i] = paths[i].substring(1);
                }
            }

        }
        return ArrayUtil.join(paths, StrUtil.SLASH);
    }

    private String getAlist302Url(String alistFile, String sign) {
        return this.safePath(this.alistStrmProperties.getAlistStrmUrl(), "d", alistFile + "?sign=" + sign);
    }


    private HttpRequest createPost(String url) {
        HttpRequest httpRequest = HttpUtil.createPost(this.alistStrmProperties.getAlistServerUrl() + url);
        httpRequest.header("Authorization", this.alistStrmProperties.getAlistToken());
        return httpRequest;
    }

}
