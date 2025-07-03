package bob.growingmdal.service;

import bob.growingmdal.core.command.NantianCameraAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

@Slf4j
@Service
public class NantianCameraService {

    @Value("${nantian.camera.url}")
    private String cameraUrl;

    NantianCameraAdapter videoAdapter = new NantianCameraAdapter(120 * 1000);
    NantianCameraAdapter operateAdapter = new NantianCameraAdapter(120 * 1000);;

    /**
     * 调用方法
     * @param methodName 方法名
     * @throws Exception 报错
     */
    public void invokeMethod(String methodName) throws Exception {
        // 获取当前类的方法
        Method method = this.getClass().getDeclaredMethod(methodName);
        // 调用方法
        method.invoke(this);
    }

    public int openDevice(int  index){
        try {
            operateAdapter.connect(cameraUrl);
        }catch (Exception e){
            log.error("连接设备失败", e);
            return 0;
        }
        return 1;
    }

    public static void main(String[] args) {



    }

}
