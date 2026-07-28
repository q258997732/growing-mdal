package bob.growingmdal.service;

import bob.growingmdal.annotation.DeviceOperation;
import bob.growingmdal.config.AdapterProperties;
import bob.growingmdal.connector.RpaHttpConnector;
import bob.growingmdal.core.command.DeviceCommand;
import bob.growingmdal.core.dispatcher.AnnotationDrivenHandler;
import bob.growingmdal.dto.rpa.*;
import bob.growingmdal.hardware.LifecycleManaged;
import bob.growingmdal.util.RpaUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * K-RPA 客户端服务。
 */
@Slf4j
@Service
public class RpaClientService extends AnnotationDrivenHandler implements LifecycleManaged {

    public static final String DEVICE_TYPE = "Rpa";

    private static final String APP_DM_GUID = "{9F8E5ECB-5976-4315-B8F3-43B8502B694D}";
    private static final String FUNC_GUID = "{2881E26D-62CE-4937-B4BB-8998440417C4}";

    private final RpaHttpConnector rpaHttpConnector;
    private final ObjectMapper objectMapper;
    private final AdapterProperties adapterProperties;

    public RpaClientService(RpaHttpConnector rpaHttpConnector,
                            ObjectMapper objectMapper,
                            AdapterProperties adapterProperties) {
        this.rpaHttpConnector = rpaHttpConnector;
        this.objectMapper = objectMapper;
        this.adapterProperties = adapterProperties;
    }

    @Override
    public String getDeviceType() {
        return DEVICE_TYPE;
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "CallComponent")
    public boolean callComponent(DeviceCommand command) {
        JsonNode transferData = parseTransferData(command.getTransferData());
        if (transferData == null) {
            return false;
        }
        JsonNode scriptNode = transferData.get("script");
        JsonNode paramsNode = transferData.get("params");
        JsonNode agentIpNode = transferData.get("agentIp");
        if (scriptNode == null || paramsNode == null || agentIpNode == null) {
            log.error("Rpa CallComponent requires script, params and agentIp");
            return false;
        }
        return callComponent(scriptNode.asText(), paramsNode.asText(), agentIpNode.asText());
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "AddDataQueue")
    public boolean addDataQueue(DeviceCommand command) {
        JsonNode transferData = parseTransferData(command.getTransferData());
        if (transferData == null) {
            return false;
        }
        JsonNode flowTypeNode = transferData.get("flowType");
        JsonNode flowNode = transferData.get("flow");
        JsonNode dataNode = transferData.get("data");
        JsonNode agentIpNode = transferData.get("agentIp");
        JsonNode levelNode = transferData.get("level");
        if (flowTypeNode == null || flowNode == null || dataNode == null
                || agentIpNode == null || levelNode == null) {
            log.error("Rpa AddDataQueue requires flowType, flow, data, agentIp and level");
            return false;
        }
        return addDataQueue(flowTypeNode.asText(), flowNode.asText(), dataNode.asText(),
                agentIpNode.asText(), levelNode.asInt());
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "GetAgentList", readOnly = true)
    public List<KAgentBean> getAgentList(DeviceCommand command) {
        List<RpaRequestBean> request = new ArrayList<>();
        request.add(new RpaRequestBean("TCoreDM", 4, APP_DM_GUID));
        request.add(new RpaRequestBean(adapterProperties.getRpaUser(), 4, "AppName"));
        request.add(new RpaRequestBean(adapterProperties.getRpaPass(), 4, "AppPass"));
        request.add(new RpaRequestBean("GetAgentList", 4, FUNC_GUID));
        List<RpaRequestBean> response = rpaHttpConnector.sendRequest(request);
        return RpaUtil.result2KAgentList(response);
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "GetFlowList", readOnly = true)
    public List<KFlowBean> getFlowList(DeviceCommand command) {
        List<RpaRequestBean> request = new ArrayList<>();
        request.add(new RpaRequestBean("TFlowDM", 4, APP_DM_GUID));
        request.add(new RpaRequestBean(adapterProperties.getRpaPass(), 4, "AppPass"));
        request.add(new RpaRequestBean(adapterProperties.getRpaUser(), 4, "AppName"));
        request.add(new RpaRequestBean("GetFlowList", 4, FUNC_GUID));
        request.add(new RpaRequestBean("", 4, "ConsumerID"));
        List<RpaRequestBean> response = rpaHttpConnector.sendRequest(request);
        return RpaUtil.result2KFlowList(response);
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "GetAgentThreadList", readOnly = true)
    public Map<String, KAgentThreadBean> getAgentThreadList(DeviceCommand command) {
        List<RpaRequestBean> request = new ArrayList<>();
        request.add(new RpaRequestBean("TUserDM", 4, APP_DM_GUID));
        request.add(new RpaRequestBean(adapterProperties.getRpaUser(), 4, "AppName"));
        request.add(new RpaRequestBean(adapterProperties.getRpaPass(), 4, "AppPass"));
        request.add(new RpaRequestBean("GetRobotExecStatus", 4, FUNC_GUID));
        List<RpaRequestBean> response = rpaHttpConnector.sendRequest(request);
        return RpaUtil.result2KAgentThreadList(response);
    }

    @DeviceOperation(DeviceType = DEVICE_TYPE, ProcessCommand = "GetSXFAgentFlowQuery", readOnly = true)
    public List<KSxfAgentBean> getSxfAgentFlowQuery(DeviceCommand command) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        List<RpaRequestBean> request = new ArrayList<>();
        request.add(new RpaRequestBean(date, 4, "EDate"));
        request.add(new RpaRequestBean(date, 4, "BDate"));
        request.add(new RpaRequestBean("THomeDM", 4, APP_DM_GUID));
        request.add(new RpaRequestBean(adapterProperties.getRpaUser(), 4, "AppName"));
        request.add(new RpaRequestBean(adapterProperties.getRpaPass(), 4, "AppPass"));
        request.add(new RpaRequestBean("GetSXFAgentFlowQuery", 4, FUNC_GUID));
        List<RpaRequestBean> response = rpaHttpConnector.sendRequest(request);
        return RpaUtil.result2KSxfAgent(response);
    }

    private boolean callComponent(String script, String params, String agentIp) {
        List<RpaRequestBean> request = new ArrayList<>();
        request.add(new RpaRequestBean(agentIp, 4, "IP"));
        request.add(new RpaRequestBean(script, 4, "VCLName"));
        request.add(new RpaRequestBean(params, 7, "Params"));
        request.add(new RpaRequestBean(adapterProperties.getRpaPass(), 4, "AppPass"));
        request.add(new RpaRequestBean(adapterProperties.getRpaUser(), 4, "AppName"));
        request.add(new RpaRequestBean("TSystemDM", 4, APP_DM_GUID));
        request.add(new RpaRequestBean("CallComponent", 4, FUNC_GUID));
        request.add(new RpaRequestBean(true, 1, "VCLSynchro"));
        request.add(new RpaRequestBean(false, 1, "VCLBlockInput"));
        request.add(new RpaRequestBean(adapterProperties.getRpaCallFunTimeout(), 0, "TimeOut"));
        request.add(new RpaRequestBean(0, 0, "AomScript"));
        List<RpaRequestBean> response = rpaHttpConnector.sendRequest(request);
        return RpaUtil.callFunStatus(response);
    }

    private boolean addDataQueue(String flowType, String flow, String data, String agentIp, int level) {
        List<RpaRequestBean> request = new ArrayList<>();
        request.add(new RpaRequestBean(agentIp, 4, "Robot"));
        request.add(new RpaRequestBean(data, 4, "Data"));
        request.add(new RpaRequestBean(flow, 4, "name".equals(flowType) ? "FlowName" : "FlowID"));
        request.add(new RpaRequestBean(true, 1, "IdleRobot"));
        request.add(new RpaRequestBean(level, 0, "Level"));
        request.add(new RpaRequestBean("TRPADM", 4, APP_DM_GUID));
        request.add(new RpaRequestBean(true, 1, "IsThird"));
        request.add(new RpaRequestBean(true, 1, "IsQueue"));
        request.add(new RpaRequestBean("KTSSTS任务调度", 4, "TaskName"));
        request.add(new RpaRequestBean(adapterProperties.getRpaUser(), 4, "AppName"));
        request.add(new RpaRequestBean(adapterProperties.getRpaPass(), 4, "AppPass"));
        request.add(new RpaRequestBean("AddDataQueue", 4, FUNC_GUID));
        List<RpaRequestBean> response = rpaHttpConnector.sendRequest(request);
        return RpaUtil.callFunStatus(response);
    }

    private JsonNode parseTransferData(String transferData) {
        if (transferData == null || transferData.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(transferData);
        } catch (IOException e) {
            log.error("Failed to parse Rpa transferData: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void initialize() {
        log.info("Rpa client service initialized, host={}, port={}",
                adapterProperties.getRpaHost(), adapterProperties.getRpaPort());
    }

    @Override
    public Health health() {
        boolean reachable = rpaHttpConnector.isReachable();
        if (reachable) {
            return Health.up()
                    .withDetail("host", adapterProperties.getRpaHost())
                    .withDetail("port", adapterProperties.getRpaPort())
                    .build();
        }
        return Health.down()
                .withDetail("host", adapterProperties.getRpaHost())
                .withDetail("port", adapterProperties.getRpaPort())
                .withDetail("reason", "K-RPA host not reachable")
                .build();
    }

    @Override
    public void shutdown() {
        log.info("Rpa client service shutdown");
    }
}
