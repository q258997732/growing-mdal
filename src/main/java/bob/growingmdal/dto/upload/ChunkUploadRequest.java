package bob.growingmdal.dto.upload;

import lombok.Data;

/**
 * 文件分块上传请求 DTO。
 */
@Data
public class ChunkUploadRequest {

    private String md5;
    private String name;
    private Long size;
    private Integer chunks;
    private Integer chunk;
    private Integer chunkSize;
    private String contentBase64;
}
