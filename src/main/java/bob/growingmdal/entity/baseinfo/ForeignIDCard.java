package bob.growingmdal.entity.baseinfo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONObject;

@Slf4j
@Data
public class ForeignIDCard implements IDCard {

    private String englishName;      // 英文姓名
    private String sex;              // 性别
    private String idNumber;         // 证件号码
    private String citizenship;      // 国籍
    private String chineseName;      // 中文姓名
    private String expireStartDay;   // 有效期开始日
    private String expireEndDay;     // 有效期截止日
    private String birthDay;         // 出生日期
    private String versionNumber;    // 版本号
    private String departmentCode;   // 签发机关代码
    private String typeSign;         // 证件类型标识
    private String reserved;         // 保留字段

    public ForeignIDCard() {
    }

    public ForeignIDCard(String englishName, String sex, String idNumber, String citizenship, String chineseName, String expireStartDay, String expireEndDay, String birthDay, String versionNumber, String departmentCode, String typeSign, String reserved) {
        this.englishName = englishName;
        this.sex = sex;
        this.idNumber = idNumber;
        this.citizenship = citizenship;
        this.chineseName = chineseName;
        this.expireStartDay = expireStartDay;
        this.expireEndDay = expireEndDay;
        this.birthDay = birthDay;
        this.versionNumber = versionNumber;
        this.departmentCode = departmentCode;
        this.typeSign = typeSign;
    }

    @Override
    public String getName() {
        return this.chineseName;
    }

    public String toString() {
        return "{" +
                "englishName='" + englishName + '\'' +
                ", sex='" + sex + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", citizenship='" + citizenship + '\'' +
                ", chineseName='" + chineseName + '\'' +
                ", expireStartDay='" + expireStartDay + '\'' +
                ", expireEndDay='" + expireEndDay + '\'' +
                ", birthDay='" + birthDay + '\'' +
                ", versionNumber='" + versionNumber + '\'' +
                ", departmentCode='" + departmentCode + '\'' + '}';
     }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("englishName", this.englishName);
            json.put("sex", this.sex);
            json.put("idNumber", this.idNumber);
            json.put("citizenship", this.citizenship);
            json.put("chineseName", this.chineseName);
            json.put("expireStartDay", this.expireStartDay);
            json.put("expireEndDay", this.expireEndDay);
            json.put("birthDay", this.birthDay);
            json.put("versionNumber", this.versionNumber);
            json.put("departmentCode", this.departmentCode);
            json.put("typeSign", this.typeSign);
        }catch (Exception e) {
            log.error("ForeignIDCard toJson error: {}", e.getMessage());
            return null;
        }
        return json;
    }
}
