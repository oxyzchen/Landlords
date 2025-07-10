package site.pushy.landlords.core;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import site.pushy.landlords.core.enums.CardGradeEnum;
import site.pushy.landlords.core.GradeComparison;
import site.pushy.landlords.core.enums.TypeEnum;
import site.pushy.landlords.pojo.Card;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static site.pushy.landlords.core.enums.CardGradeEnum.*;
import static site.pushy.landlords.core.enums.CardGradeEnum.EIGHTH;

public class GradeComparisonTest {

    @Test
    public void canPlayCards() {
        //单张
        //4 vs 3
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SECOND),TypeEnum.SINGLE,buildCards(FIRST),TypeEnum.SINGLE));
        Assert.assertFalse(GradeComparison.canPlayCards(buildCards(FIRST),TypeEnum.SINGLE,buildCards(SECOND),TypeEnum.SINGLE));
        //k vs 3
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(ELEVENTH),TypeEnum.SINGLE,buildCards(FIRST),TypeEnum.SINGLE));
        Assert.assertFalse(GradeComparison.canPlayCards(buildCards(FIRST),TypeEnum.SINGLE,buildCards(FIRST),TypeEnum.SINGLE));
        Assert.assertFalse(GradeComparison.canPlayCards(buildCards(FIRST),TypeEnum.SINGLE,buildCards(ELEVENTH),TypeEnum.SINGLE));
        //2 vs A
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(THIRTEENTH),TypeEnum.SINGLE,buildCards(TWELFTH),TypeEnum.SINGLE));
        Assert.assertFalse(GradeComparison.canPlayCards(buildCards(TWELFTH),TypeEnum.SINGLE,buildCards(THIRTEENTH),TypeEnum.SINGLE));
        // 大王 vs 9
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(FIFTEENTH),TypeEnum.SINGLE,buildCards(SEVENTH),TypeEnum.SINGLE));

        //对子 vs 单张
        // AA vs 9
        Assert.assertFalse(GradeComparison.canPlayCards(buildCards(TWELFTH,TWELFTH),TypeEnum.PAIR,buildCards(SEVENTH),TypeEnum.SINGLE));
        //炸弹 vs 单张
        //AAAA vs 9
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(TWELFTH,TWELFTH,TWELFTH,TWELFTH),TypeEnum.BOMB,buildCards(SEVENTH),TypeEnum.SINGLE));
        //AAAA vs 大王
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(TWELFTH,TWELFTH,TWELFTH,TWELFTH),TypeEnum.BOMB,buildCards(FOURTEENTH),TypeEnum.SINGLE));
        // 王炸 vs 9
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(FOURTEENTH,FIFTEENTH),TypeEnum.JOKER_BOMB,buildCards(THIRTEENTH),TypeEnum.SINGLE));

        //对子 vs 对子
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(TWELFTH,TWELFTH),TypeEnum.PAIR,buildCards(SEVENTH,SEVENTH),TypeEnum.PAIR));
        Assert.assertFalse(GradeComparison.canPlayCards(buildCards(SEVENTH,SEVENTH),TypeEnum.PAIR,buildCards(TWELFTH,TWELFTH),TypeEnum.PAIR));
        //AAAA vs 对子
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(TWELFTH,TWELFTH,TWELFTH,TWELFTH),TypeEnum.BOMB,buildCards(SEVENTH,SEVENTH),TypeEnum.PAIR));
        //AAAA vs 对子
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(TWELFTH,TWELFTH,TWELFTH,TWELFTH),TypeEnum.BOMB,buildCards(SEVENTH,SEVENTH),TypeEnum.PAIR));
        // 王炸 vs 对子
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(FOURTEENTH,FIFTEENTH),TypeEnum.JOKER_BOMB,buildCards(THIRTEENTH,THIRTEENTH),TypeEnum.SINGLE));

        //三张 vs 三张

        //三带一
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, EIGHTH),TypeEnum.THREE_WITH_ONE, buildCards(FIRST, FIRST, FIRST, SECOND), TypeEnum.THREE_WITH_ONE));
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, SECOND),TypeEnum.THREE_WITH_ONE, buildCards(FIRST, FIRST, FIRST, SECOND), TypeEnum.THREE_WITH_ONE));
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, FIRST),TypeEnum.THREE_WITH_ONE, buildCards(FIRST, FIRST, FIRST, FIFTEENTH), TypeEnum.THREE_WITH_ONE));
        Assert.assertFalse(GradeComparison.canPlayCards(buildCards(FIRST, FIRST, FIRST, SECOND),TypeEnum.THREE_WITH_ONE, buildCards(SEVENTH, SEVENTH, SEVENTH, EIGHTH), TypeEnum.THREE_WITH_ONE));
        //三带二
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, EIGHTH,EIGHTH),TypeEnum.THREE_WITH_PAIR, buildCards(FIRST, FIRST, FIRST, SECOND,SECOND), TypeEnum.THREE_WITH_PAIR));
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, SECOND, SECOND),TypeEnum.THREE_WITH_PAIR, buildCards(FIRST, FIRST, FIRST, SECOND, SECOND), TypeEnum.THREE_WITH_PAIR));
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, FIRST,FIRST),TypeEnum.THREE_WITH_PAIR, buildCards(FIRST, FIRST, FIRST, FIFTEENTH,FIRST), TypeEnum.THREE_WITH_PAIR));
        Assert.assertFalse(GradeComparison.canPlayCards(buildCards(FIRST, FIRST, FIRST, SECOND,SECOND),TypeEnum.THREE_WITH_PAIR, buildCards(SEVENTH, SEVENTH, SEVENTH, EIGHTH,EIGHTH), TypeEnum.THREE_WITH_PAIR));
        //四带二
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, SEVENTH, EIGHTH,EIGHTH),TypeEnum.FOUR_WITH_TWO, buildCards(FIRST, FIRST, FIRST, FIRST, SECOND,SECOND), TypeEnum.FOUR_WITH_TWO));
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, SEVENTH, SECOND, SECOND),TypeEnum.FOUR_WITH_TWO, buildCards(FIRST, FIRST, FIRST, FIRST, SECOND, SECOND), TypeEnum.FOUR_WITH_TWO));
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, SEVENTH, FIRST,FIRST),TypeEnum.FOUR_WITH_TWO, buildCards(FIRST, FIRST, FIRST, FIRST, FIFTEENTH,FIRST), TypeEnum.FOUR_WITH_TWO));
        Assert.assertFalse(GradeComparison.canPlayCards(buildCards(FIRST, FIRST, FIRST, FIRST, SECOND,SECOND),TypeEnum.FOUR_WITH_TWO, buildCards(SEVENTH, SEVENTH, SEVENTH, SEVENTH, EIGHTH,EIGHTH), TypeEnum.FOUR_WITH_TWO));


        //飞机
        //999 101010 vs 333444
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, EIGHTH, EIGHTH, EIGHTH),TypeEnum.AIRCRAFT, buildCards(FIRST, FIRST, FIRST, SECOND, SECOND, SECOND), TypeEnum.AIRCRAFT));


        //飞机带翅膀
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, EIGHTH, EIGHTH, EIGHTH, FIRST, SECOND),TypeEnum.AIRCRAFT_WITH_WINGS, buildCards(FIRST, FIRST, FIRST, SECOND, SECOND, SECOND, SIXTH, EIGHTH), TypeEnum.AIRCRAFT_WITH_WINGS));
        Assert.assertFalse(GradeComparison.canPlayCards(buildCards(FIRST, FIRST, FIRST, SECOND, SECOND, SECOND, SIXTH, EIGHTH),TypeEnum.AIRCRAFT_WITH_WINGS, buildCards(SEVENTH, SEVENTH, SEVENTH, EIGHTH, EIGHTH, EIGHTH, FIRST, SECOND), TypeEnum.AIRCRAFT_WITH_WINGS));
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(SEVENTH, SEVENTH, SEVENTH, EIGHTH, EIGHTH, EIGHTH, FIRST, EIGHTH),TypeEnum.AIRCRAFT_WITH_WINGS, buildCards(FIRST, FIRST, FIRST, SECOND, SECOND, SECOND, SIXTH, EIGHTH), TypeEnum.AIRCRAFT_WITH_WINGS));
        Assert.assertTrue(GradeComparison.canPlayCards(buildCards(FIFTH, FIFTH, FIFTH, SIXTH, SIXTH, SIXTH, SEVENTH, SEVENTH,SEVENTH,THIRD,NINTH,TENTH),TypeEnum.AIRCRAFT_WITH_WINGS, buildCards(FIRST, FIRST, FIRST, SECOND, SECOND, SECOND,THIRD,THIRD,THIRD, SIXTH,SEVENTH, EIGHTH), TypeEnum.AIRCRAFT_WITH_WINGS));
    }

    @Test
    public void hasHighGradeCards() {
    }
    private List<Card> buildCards(CardGradeEnum... arr) {
        return Arrays.stream(arr).map(Card::new)
                .collect(Collectors.toList());
    }
}
