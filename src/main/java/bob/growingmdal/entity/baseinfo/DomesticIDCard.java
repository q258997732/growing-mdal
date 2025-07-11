package bob.growingmdal.entity.baseinfo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class DomesticIDCard implements IDCard {

    private String name;             // 姓名
    private String sex;              // 性别 (1:男)
    private String nation;           // 民族
    private String birthDay;         // 出生日期
    private String address;          // 地址
    private String idNumber;         // 身份证号
    private String department;       // 签发机关
    private String expireStartDay;   // 有效期开始日
    private String expireEndDay;     // 有效期截止日
    private String reserved;         // 保留字段
    private String photo;           // 照片

    // 民族代码映射表
    private static final Map<String, String> NATION_MAP = new HashMap<>() {{
        put("01", "汉族");
        put("02", "蒙古族");
        put("03", "回族");
        put("04", "藏族");
        put("05", "维吾尔族");
        put("06", "苗族");
        put("07", "彝族");
        put("08", "壮族");
        put("09", "布依族");
        put("10", "朝鲜族");
        put("11", "满族");
        put("12", "侗族");
        put("13", "瑶族");
        put("14", "白族");
        put("15", "土家族");
        put("16", "哈尼族");
        put("17", "哈萨克族");
        put("18", "傣族");
        put("19", "黎族");
        put("20", "傈僳族");
        put("21", "佤族");
        put("22", "畲族");
        put("23", "高山族");
        put("24", "拉祜族");
        put("25", "水族");
        put("26", "东乡族");
        put("27", "纳西族");
        put("28", "景颇族");
        put("29", "柯尔克孜族");
        put("30", "土族");
        put("31", "达斡尔族");
        put("32", "仫佬族");
        put("33", "羌族");
        put("34", "布朗族");
        put("35", "撒拉族");
        put("36", "毛南族");
        put("37", "仡佬族");
        put("38", "锡伯族");
        put("39", "阿昌族");
        put("40", "普米族");
        put("41", "塔吉克族");
        put("42", "怒族");
        put("43", "乌孜别克族");
        put("44", "俄罗斯族");
        put("45", "鄂温克族");
        put("46", "德昂族");
        put("47", "保安族");
        put("48", "裕固族");
        put("49", "京族");
        put("50", "塔塔尔族");
        put("51", "独龙族");
        put("52", "鄂伦春族");
        put("53", "赫哲族");
        put("54", "门巴族");
        put("55", "珞巴族");
        put("56", "基诺族");
        put("97", "其他");
        put("98", "外国血统");
    }};

    public DomesticIDCard() {
    }

    public DomesticIDCard(String name, String sex, String nation, String birthDay, String address,
                          String idNumber, String department, String expireStartDay,
                          String expireEndDay, String reserved) {
        this.name = name;
        this.sex = "1".equals(sex) ? "男" : "女";
        this.nation = NATION_MAP.getOrDefault(nation, nation); // 如果找不到对应的民族，则保持原值
        this.birthDay = birthDay;
        this.address = address;
        this.idNumber = idNumber;
        this.department = department;
        this.expireStartDay = expireStartDay;
        this.expireEndDay = expireEndDay;
        this.reserved = reserved;
    }

    public String toString() {
        return "{" +
                "\"name\":\"" + name + "\"" +
                ", \"sex\":\"" + sex + "\"" +
                ", \"nation\":\"" + nation + "\"" +
                ", \"birthDay\":\"" + birthDay + "\"" +
                ", \"address\":\"" + address + "\"" +
                ", \"idNumber\":\"" + idNumber + "\"" +
                ", \"department\":\"" + department + "\"" +
                ", \"expireStartDay\":\"" + expireStartDay + "\"" +
                ", \"expireEndDay\":\"" + expireEndDay + "\"" +
                ", \"reserved\":\"" + reserved + "\"" +
                "}";
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("sex", sex);
            json.put("nation", nation);
            json.put("birthDay", birthDay);
            json.put("address", address);
            json.put("idNumber", idNumber);
            json.put("department", department);
            json.put("expireStartDay", expireStartDay);
            json.put("expireEndDay", expireEndDay);
            json.put("reserved", reserved);
        } catch (Exception e) {
            log.error("DomesticIDCard toJson error: {}", e.getMessage());
            return null;
        }
        return json;
    }

    // Getter and Setter methods for sex with conversion
    public String getSex() {
        return "1".equals(sex) ? "男" : "女";
    }

    public void setSex(String sex) {
        this.sex = "1".equals(sex) ? "男" : "女";
    }

    // Getter and Setter methods for nation with conversion
    public String getNation() {
        return NATION_MAP.getOrDefault(nation, nation);
    }

    public void setNation(String nation) {
        this.nation = NATION_MAP.getOrDefault(nation, nation);
    }
}