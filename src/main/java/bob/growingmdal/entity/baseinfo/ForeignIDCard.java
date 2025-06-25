package bob.growingmdal.entity.baseinfo;

import lombok.Data;

@Data
public class ForeignIDCard {

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
}
