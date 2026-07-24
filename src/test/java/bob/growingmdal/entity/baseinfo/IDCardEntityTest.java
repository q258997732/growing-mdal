package bob.growingmdal.entity.baseinfo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IDCardEntityTest {

    @Test
    void domesticCardShouldExposeName() {
        DomesticIDCard card = new DomesticIDCard();
        card.setName("张三");
        assertThat(card.getName()).isEqualTo("张三");
        assertThat(card.toString()).isNotBlank();
    }

    @Test
    void foreignCardShouldExposeChineseName() {
        ForeignIDCard card = new ForeignIDCard();
        card.setChineseName("李四");
        assertThat(card.getName()).isEqualTo("李四");
    }

    @Test
    void foreignCardToStringShouldBeNonBlank() {
        ForeignIDCard card = new ForeignIDCard();
        card.setIdNumber("123456");
        assertThat(card.toString()).isNotBlank();
    }
}
