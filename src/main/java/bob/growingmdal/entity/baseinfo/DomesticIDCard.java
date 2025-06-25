package bob.growingmdal.entity.baseinfo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONObject;

@Slf4j
@Data
public class DomesticIDCard implements IDCard {

    private String name;             // 姓名
    private String sex;              // 性别
    private String nation;           // 民族
    private String birthDay;         // 出生日期
    private String address;          // 地址
    private String idNumber;         // 身份证号
    private String department;       // 签发机关
    private String expireStartDay;   // 有效期开始日
    private String expireEndDay;     // 有效期截止日
    private String reserved;         // 保留字段

    public DomesticIDCard() {
    }

    public DomesticIDCard(String name, String sex, String nation, String birthDay, String address, String idNumber, String department, String expireStartDay, String expireEndDay, String reserved) {
    }

    public String toString(){
        return "{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", nation='" + nation + '\'' +
                ", birthDay='" + birthDay + '\'' +
                ", address='" + address + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", department='" + department + '\'' +
                ", expireStartDay='" + expireStartDay + '\'' +
                ", expireEndDay='" + expireEndDay + '\'' +
                ", reserved='" + reserved + '\'' +
                '}';
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
        }catch (Exception e) {
            log.error("DomesticIDCard toJson error: {}", e.getMessage());
            return null;
        }
        return json;
    }
}
