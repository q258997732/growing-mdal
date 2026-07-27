package bob.growingmdal.dto.rpa;

import lombok.Getter;

import java.util.Objects;

/**
 * K-RPA 任务执行状态枚举。
 */
@Getter
public enum ExecStateEnum {

    RUN("-1", "正在运行"),
    STOP("0", "手动停止"),
    SUCCESS("1", "执行成功"),
    TIMEOUT("2", "执行超时"),
    DIST_SUCCESS("3", "发起执行"),
    DIST_FAIL("4", "发起失败"),
    FAIL("99", "执行失败");

    private final String code;
    private final String description;

    ExecStateEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ExecStateEnum fromCode(String code) {
        for (ExecStateEnum execStateEnum : ExecStateEnum.values()) {
            if (Objects.equals(execStateEnum.getCode(), code)) {
                return execStateEnum;
            }
        }
        return null;
    }
}
