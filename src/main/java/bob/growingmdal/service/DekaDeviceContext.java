package bob.growingmdal.service;

/**
 * 德卡读卡器单次操作的上下文。
 * <p>
 * 将句柄、状态、卡类型封装为局部对象，避免多请求间的共享可变状态污染。
 */
public class DekaDeviceContext {

    private int handle = -1;
    private int status = -1;
    private int type = 0;

    public int getHandle() {
        return handle;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void reset() {
        this.handle = -1;
        this.status = -1;
        this.type = 0;
    }
}
