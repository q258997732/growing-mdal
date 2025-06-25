package bob.growingmdal.entity.baseinfo;

import lombok.Data;

@Data
public class DomesticIDCard {

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
}
