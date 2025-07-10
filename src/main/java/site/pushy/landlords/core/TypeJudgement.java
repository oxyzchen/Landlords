package site.pushy.landlords.core;

import site.pushy.landlords.core.enums.CardGradeEnum;
import site.pushy.landlords.pojo.Card;

import java.util.*;
import java.util.stream.Collectors;

import static site.pushy.landlords.core.enums.CardGradeEnum.*;

/**
 * @author Pushy
 * @since 2018/12/28 21:51
 */
public class TypeJudgement {

    private static final List<CardGradeEnum> ILLEGAL_GRADES_OF_STRAIGHT =
            Arrays.asList(THIRTEENTH, FOURTEENTH, FIFTEENTH);

    /**
     * 判断是否出的牌是单牌
     */
    public static boolean isSingle(List<Card> cards) {
        return !isEmpty(cards) && cards.size() == 1;
    }

    /**
     * 判断是否出的牌是对子
     */
    public static boolean isPair(List<Card> cards) {
        if (isEmpty(cards) || cards.size() != 2) {
            return false;
        }
        return isAllGradeEqual(cards);
    }

    /**
     * 判断是否出的牌是三张不带牌
     */
    public static boolean isThree(List<Card> cards) {
        if (isEmpty(cards) || cards.size() != 3) {
            return false;
        }
        return isAllGradeEqual(cards);
    }

    /**
     * 判断是否出的牌是炸弹
     */
    public static boolean isBomb(List<Card> cards) {
        if (isEmpty(cards) || cards.size() != 4) {
            return false;
        }
        return isAllGradeEqual(cards);
    }

    /**
     * 判断是否出的牌是王炸
     */
    public static boolean isJokerBomb(List<Card> cards) {
        if (isEmpty(cards) || cards.size() != 2) {
            return false;
        }
        CardGradeEnum cg1 = cards.get(0).getGrade(), cg2 = cards.get(1).getGrade();
        return (cg1 == FOURTEENTH && cg2 == FIFTEENTH)
                || (cg1 == FIFTEENTH && cg2 == FOURTEENTH);
    }

    /**
     * 判断是否出的牌是三带一
     */
    public static boolean isThreeWithOne(List<Card> cards) {
        if (isEmpty(cards) || cards.size() != 4) {
            return false;
        }
        // 防止该算法将炸弹判定为三带一
        if (isAllGradeEqual(cards)) {
            return false;
        }

        CardUtils.sortCards(cards);
        // 是 3 带 1，并且被带的牌在牌头
        // 是 3 带 1，并且被带的牌在牌尾
        if(isAllGradeEqual(cards,0,2)||isAllGradeEqual(cards,1,3)) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否是三带一对
     */
    public static boolean isThreeWithPair(List<Card> cards) {
        if (isEmpty(cards) || cards.size() != 5) {
            return false;
        }
        CardUtils.sortCards(cards);
        if(isAllGradeEqual(cards,0,2)&&isAllGradeEqual(cards,3,4) ||isAllGradeEqual(cards,0,1)&&isAllGradeEqual(cards,2,4)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否出的牌是四带二
     */
    public static boolean isFourWithTwo(List<Card> cards) {
        if (isEmpty(cards) || cards.size() != 6) {
            return false;
        }
        CardUtils.sortCards(cards);


        for (int i = 0; i < 3; i++) {
            if(isAllGradeEqual(cards,i,i+3)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否出的牌是顺子
     */
    public static boolean isStraight(List<Card> cards) {
        if (isEmpty(cards) || cards.size() < 5) {  // 顺子不能小于5个
            return false;
        }
        CardUtils.sortCards(cards);
        Card last = cards.get(cards.size() - 1);
        if (ILLEGAL_GRADES_OF_STRAIGHT.contains(last.getGrade())) { // 顺子最大的数只能是A
            return false;
        }
        // 判断卡片数组是不是递增的，如果是递增的，说明是顺子
        for (int i = 0; i < cards.size() - 1; i++) {
            // 将每一张牌和它的后一张牌对比，是否相差 1
            if ((cards.get(i).getGradeValue() + 1) != cards.get(i + 1).getGradeValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否出的牌是连对
     */
    public static boolean isStraightPair(List<Card> cards) {
        // 连对的牌必须满足大于 6 张牌，而且必须是双数
        if (isEmpty(cards) || cards.size() < 6 || cards.size() % 2 != 0) {
            return false;
        }
        CardUtils.sortCards(cards);
        Card last = cards.get(cards.size() - 1);
        if (ILLEGAL_GRADES_OF_STRAIGHT.contains(last.getGrade())) { // 连对最大的数只能是 A
            return false;
        }
        for (int i = 0; i < cards.size(); i += 2) {
            Card current = cards.get(i);   // 当前牌
            Card next = cards.get(i + 1);  // 下一张牌

            // 判断牌尾的两张牌是否相等
            if (i == cards.size() - 2) {
                if (!current.equalsByGrade(next)) {
                    return false;
                }
                // 判断完是否相等可以跳出循环
                // 因为不需要和下一个连对数（下一张牌的下一张牌）进行比较
                break;
            }
            Card nextNext = cards.get(i + 2);  // 下一张牌的下一张牌
            // 判断是否和下一牌的牌数相同
            if (!current.equalsByGrade(next)) {
                return false;
            }
            // 判断当前的连对数是否和下一个连对数递增1，如果是，则满足连对的出牌规则
            if ((current.getGradeValue() + 1) != nextNext.getGradeValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否出的牌是飞机
     */
    public static boolean isAircraft(List<Card> cards) {
        if (isEmpty(cards) || cards.size() < 3 || cards.size() % 3 != 0) {
            return false;
        }
        CardUtils.sortCards(cards);

        for (int i = 0; i < cards.size(); i += 3) {
            if (i == cards.size() - 3) {
                // 比较最后一个飞机的三张牌是否相等
                //如果最后一个飞机是222 不算飞机
                if (cards.get(i).getGrade() == THIRTEENTH){
                    return false;
                }
                return isAllGradeEqual(cards, i, cards.size() - 1);
            }
            // 如果当前这张牌不和下边的两张牌相同，则不符合飞机的出牌规则
            if (!isAllGradeEqual(cards, i, i + 2)) {
                return false;
            }

            // 判断当前飞机等级是否和下一个飞机的等级递增 1
            // 如果是，则符合飞机的出牌规则
            if ((cards.get(i).getGradeValue() + 1) != cards.get(i + 3).getGradeValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否出的牌是飞机带翅膀
     */
    public static boolean isAircraftWithWing(List<Card> cards) {

        //先判断牌的数量
        if (isEmpty(cards) || cards.size() < 6 || (cards.size() % 4 != 0 && cards.size() % 5 != 0) ) {
            return false;
        }
        //记录各个牌型的数量 如 3张3 三张4 两张2
        Map<Integer, Integer> counter = new HashMap<>();
        for (Card card : cards) {
            counter.put(card.getGradeValue(), counter.getOrDefault(card.getGradeValue(), 0) + 1);
        }
        //把有三个的提出来，组成一个新的list
        List<Integer> aircraftList = counter.entrySet().stream()
                .filter(entry -> entry.getValue() >= 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        // 飞机要保证递增, 且不包含2
        if (aircraftList.isEmpty() || aircraftList.size() < 2 || !isIncremental(aircraftList)||aircraftList.contains(13)) {
            return false;
        }
        // 判断翅膀是否合法, 即:
        // 1. 要么都是双
        // 2. 要么都是单
        // 首先判断翅膀的数量，要么是3张的数量，要么是3张的两倍
        Map<Integer, Integer> wings = counter.entrySet().stream()
                .filter(entry -> entry.getValue() != 3) // 筛选值不为 3 的
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        // 计算所有 values 的和
        int sum = wings.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        //如果是三带一，则数量对即可
        if(sum == aircraftList.size()){
            return true;
        }
        //如果是三带一对，判断剩下的排是否是对子
        if(sum == aircraftList.size()*2){
            for(Integer num : wings.values()){
                if(num!=2){
                    return false;
                }
            }
            return true;
        }


        return false;
    }

    private static boolean isEmpty(List<Card> cards) {
        return cards == null || cards.size() == 0;
    }

    private static boolean isIncremental(List<Integer> cards) {
        if (cards == null || cards.isEmpty()) {
            return false;
        }
        Collections.sort(cards);
        for (int i = 0; i < cards.size() - 1; i++) {
            if (cards.get(i) + 1 != cards.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断传入的卡牌数组是否全部是相同等级的牌，如 4444，返回true
     */
    public static boolean isAllGradeEqual(List<Card> cards) {
        return isAllGradeEqual(cards, 0, cards.size() - 1);
    }

    public static boolean isAllGradeEqual(List<Card> cards, int start, int end) {
        if (start > end || end >= cards.size()) {
            throw new IllegalArgumentException("start or end is illegal");
        }
        if (start == end) {
            return true;
        }
        Card first = cards.get(start);
        for (int i = start + 1; i < end + 1; i++) {
            if (!first.equalsByGrade(cards.get(i))) {
                return false;
            }
        }
        return true;
    }
}
