package bob.growingmdal.util;

import bob.growingmdal.dto.rpa.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * K-RPA 响应解析工具。
 */
@Slf4j
public final class RpaUtil {

    private static final String APP_DM_GUID = "{9F8E5ECB-5976-4315-B8F3-43B8502B694D}";
    private static final String FUNC_GUID = "{2881E26D-62CE-4937-B4BB-8998440417C4}";
    private static final String ERROR_LEVEL_GUID = "{50043442-8A69-4A6B-A8B5-61F882EDE4F3}";

    private RpaUtil() {
    }

    public static Object getRpaResponseValue(String target, List<RpaRequestBean> rpaRequestBeanList) {
        if (rpaRequestBeanList == null) {
            return null;
        }
        for (RpaRequestBean rpaRequestBean : rpaRequestBeanList) {
            if (target.equals(rpaRequestBean.getName())) {
                return rpaRequestBean.getValue();
            }
        }
        return null;
    }

    public static boolean callFunStatus(List<RpaRequestBean> rpaRequestBeanList) {
        if (rpaRequestBeanList == null || rpaRequestBeanList.isEmpty()) {
            return false;
        }
        Object level = getRpaResponseValue(ERROR_LEVEL_GUID, rpaRequestBeanList);
        return "".equals(level);
    }

    public static List<RpaRequestBean> result2KRpaRequestBean(String json, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<RpaRequestBean>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("Parse Rpa response to json fail : {}", json);
            return null;
        }
    }

    public static List<KSxfAgentBean> result2KSxfAgent(List<RpaRequestBean> rpaRequestBeanList) {
        if (rpaRequestBeanList == null || rpaRequestBeanList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, String> kAgentInvMap = new HashMap<>();
        List<KSxfAgentBean> result = new ArrayList<>();

        for (RpaRequestBean rpaRequestBean : rpaRequestBeanList) {
            if ("k_agent_object".equals(rpaRequestBean.getName())) {
                List<List<Map<String, String>>> kAgentList = castListListMap(rpaRequestBean.getValue());
                if (kAgentList == null) {
                    continue;
                }
                for (int i = 0; i < kAgentList.size(); i++) {
                    if (i == 0) {
                        for (int j = 0; j < kAgentList.get(i).size(); j++) {
                            String name = kAgentList.get(i).get(j).get("Name");
                            if (name != null && !name.isEmpty()) {
                                kAgentInvMap.put(j, name);
                            }
                        }
                    } else {
                        Map<String, Object> beanParamMap = new HashMap<>();
                        for (int j = 0; j < kAgentList.get(i).size(); j++) {
                            beanParamMap.put(kAgentInvMap.get(j), kAgentList.get(i).get(j).get("Value"));
                        }
                        result.add(new KSxfAgentBean(beanParamMap));
                    }
                }
                break;
            }
        }
        return result;
    }

    public static Map<String, KAgentThreadBean> result2KAgentThreadList(List<RpaRequestBean> rpaRequestBeanList) {
        if (rpaRequestBeanList == null || rpaRequestBeanList.isEmpty()) {
            return new HashMap<>();
        }
        Map<Integer, String> kAgentInvMap = new HashMap<>();
        Map<String, KAgentThreadBean> result = new HashMap<>();

        for (RpaRequestBean rpaRequestBean : rpaRequestBeanList) {
            if ("k_agent".equals(rpaRequestBean.getName())) {
                List<List<Map<String, String>>> kAgentThreadList = castListListMap(rpaRequestBean.getValue());
                if (kAgentThreadList == null) {
                    continue;
                }
                for (int i = 0; i < kAgentThreadList.size(); i++) {
                    if (i == 0) {
                        for (int j = 0; j < kAgentThreadList.get(i).size(); j++) {
                            String name = kAgentThreadList.get(i).get(j).get("Name");
                            if (name != null && !name.isEmpty()) {
                                kAgentInvMap.put(j, name);
                            }
                        }
                    } else {
                        Map<String, Object> beanParamMap = new HashMap<>();
                        for (int j = 0; j < kAgentThreadList.get(i).size(); j++) {
                            beanParamMap.put(kAgentInvMap.get(j), kAgentThreadList.get(i).get(j).get("Value"));
                        }
                        KAgentThreadBean bean = new KAgentThreadBean(beanParamMap);
                        result.put(bean.getID(), bean);
                    }
                }
                break;
            }
        }
        return result;
    }

    public static List<KAgentBean> result2KAgentList(List<RpaRequestBean> rpaRequestBeanList) {
        if (rpaRequestBeanList == null || rpaRequestBeanList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, String> kAgentInvMap = new HashMap<>();
        List<KAgentBean> result = new ArrayList<>();

        for (RpaRequestBean rpaRequestBean : rpaRequestBeanList) {
            if ("k_agent".equals(rpaRequestBean.getName())) {
                List<List<Map<String, String>>> kAgentList = castListListMap(rpaRequestBean.getValue());
                if (kAgentList == null) {
                    continue;
                }
                for (int i = 0; i < kAgentList.size(); i++) {
                    if (i == 0) {
                        for (int j = 0; j < kAgentList.get(i).size(); j++) {
                            String name = kAgentList.get(i).get(j).get("Name");
                            if (name != null && !name.isEmpty()) {
                                kAgentInvMap.put(j, name);
                            }
                        }
                    } else {
                        Map<String, Object> beanParamMap = new HashMap<>();
                        for (int j = 0; j < kAgentList.get(i).size(); j++) {
                            beanParamMap.put(kAgentInvMap.get(j), kAgentList.get(i).get(j).get("Value"));
                        }
                        result.add(new KAgentBean(beanParamMap));
                    }
                }
                break;
            }
        }
        return result;
    }

    public static List<KFlowBean> result2KFlowList(List<RpaRequestBean> rpaRequestBeanList) {
        if (rpaRequestBeanList == null || rpaRequestBeanList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, String> kFlowInvMap = new HashMap<>();
        List<KFlowBean> result = new ArrayList<>();

        for (RpaRequestBean rpaRequestBean : rpaRequestBeanList) {
            if ("k_flow_info".equals(rpaRequestBean.getName())) {
                List<List<Map<String, String>>> kFlowList = castListListMap(rpaRequestBean.getValue());
                if (kFlowList == null) {
                    continue;
                }
                for (int i = 0; i < kFlowList.size(); i++) {
                    if (i == 0) {
                        for (int j = 0; j < kFlowList.get(i).size(); j++) {
                            String name = kFlowList.get(i).get(j).get("Name");
                            if (name != null && !name.isEmpty()) {
                                kFlowInvMap.put(j, name);
                            }
                        }
                    } else {
                        Map<String, Object> beanParamMap = new HashMap<>();
                        for (int j = 0; j < kFlowList.get(i).size(); j++) {
                            beanParamMap.put(kFlowInvMap.get(j), kFlowList.get(i).get(j).get("Value"));
                        }
                        result.add(new KFlowBean(beanParamMap));
                    }
                }
                break;
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<List<Map<String, String>>> castListListMap(Object value) {
        if (value instanceof List<?> list) {
            return (List<List<Map<String, String>>>) list;
        }
        return null;
    }
}
