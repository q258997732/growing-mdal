package bob.growingmdal.dto.upload;

import lombok.Data;

/**
 * 文件上传请求 DTO。
 */
@Data
public class FileUploadRequest {

    private String name;
    private String contentBase64;
}
