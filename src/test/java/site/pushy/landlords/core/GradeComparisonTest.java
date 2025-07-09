package site.pushy.landlords.core;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import site.pushy.landlords.core.enums.CardGradeEnum;
import site.pushy.landlords.pojo.Card;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static site.pushy.landlords.core.enums.CardGradeEnum.SECOND;

public class GradeComparisonTest {

    @Test
    public void canPlayCards() {
        Assert.assertTrue(TypeJudgement.isThree(Lists.newArrayList(buildCards(SECOND, SECOND, SECOND))));
    }

    @Test
    public void hasHighGradeCards() {
    }
    private List<Card> buildCards(CardGradeEnum... arr) {
        return Arrays.stream(arr).map(Card::new)
                .collect(Collectors.toList());
    }
}
