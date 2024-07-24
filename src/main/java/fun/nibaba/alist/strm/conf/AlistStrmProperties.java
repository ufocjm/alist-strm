package fun.nibaba.alist.strm.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "alist.strm")
public class AlistStrmProperties {

    /**
     * alist 请求 url
     */
    private String alistServerUrl;

    /**
     * alist strm 里面的 url
     */
    private String alistStrmUrl;

    /**
     * alist token
     */
    private String alistToken;

    /**
     * 扫描频率 cron
     */
    private String cron;

    /**
     * 扫描的基础路径
     */
    private String scanBasicPath;

    /**
     * 生成strm的路径
     */
    private String outputPath;

    /**
     * 生成strm的后缀
     */
    private List<String> strmSuffixList;


}
