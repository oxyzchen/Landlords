package site.pushy.landlords.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.pushy.landlords.core.enums.TypeEnum;
import site.pushy.landlords.pojo.Card;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Pushy
 * @since 2018/12/29 15:09
 */
public class GradeComparison {

    private static final Logger logger = LoggerFactory.getLogger(GradeComparison.class.getSimpleName());

    /**
     * 判断是否可以出牌，也就是当前玩家的牌是否比上家的大
     *
     * @param myCards   当前玩家的牌
     * @param prevCards 上家的牌
     */
    public static boolean canPlayCards(List<Card> myCards, TypeEnum myType,
                                       List<Card> prevCards, TypeEnum prevType) {
        if (myCards == null || prevCards == null) {
            return false;
        }
        if (myType == null || prevType == null) {
            return false;
        }
        /* 判断上家出的牌或者我出的牌是否是王炸 */
        if (prevType == TypeEnum.JOKER_BOMB) {
            return false;
        } else if (myType == TypeEnum.JOKER_BOMB) {
            return true;
        }
        /* 如果上家不是炸弹，而我出炸弹，则可以出牌 */
        if (prevType != TypeEnum.BOMB && myType == TypeEnum.BOMB) {
            return true;
        }

        Collections.sort(myCards);
        Collections.sort(prevCards);

        int mySize = myCards.size();
        int prevSize = prevCards.size();
        Card myCard = myCards.get(0);
        Card prevCard = prevCards.get(0);

        /* 排除玩家出牌和出牌类型不一致的情况 */
        if (prevType != myType) {
            return false;
        }
        //除了炸弹和王炸 可以压不同张数的牌，其他情况都要牌的数量和上次出牌数量一致
        if (mySize != prevSize) {  // 出的顺子牌数不同，无法出牌
            return false;
        }
        /* 3带1、4带2、3带一对，只需要比较第三张牌的等级即可 */
        if (prevType == TypeEnum.THREE_WITH_ONE||prevType == TypeEnum.THREE_WITH_PAIR||prevType == TypeEnum.FOUR_WITH_TWO) {
            myCard = myCards.get(2);
            prevCard = prevCards.get(2);
            return CardUtils.compareGradeTo(myCard, prevCard);
        }

        /* 顺子、连对一样，只需要比较最大的一张牌的大小 */
        else if (prevType == TypeEnum.STRAIGHT || prevType == TypeEnum.STRAIGHT_PAIR) {
            myCard = myCards.get(mySize - 1);
            prevCard = prevCards.get(prevSize - 1);
            return CardUtils.compareGradeTo(myCard, prevCard);
        }
        /* 飞机带翅膀 */
        else if (prevType == TypeEnum.AIRCRAFT_WITH_WINGS) {
            //记录各个牌型的数量 如 3张3 三张4 两张2
            Map<Integer, Integer> myCardcnt = new HashMap<>();
            Map<Integer, Integer> preCardcnt = new HashMap<>();

            for (Card card : myCards) {
                myCardcnt.put(card.getGradeValue(), myCardcnt.getOrDefault(card.getGradeValue(), 0) + 1);
            }
            for (Card card : prevCards) {
                preCardcnt.put(card.getGradeValue(), preCardcnt.getOrDefault(card.getGradeValue(), 0) + 1);
            }
            //把有三个的提出来，组成一个新的list
            List<Integer> myCardaircraft = myCardcnt.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            List<Integer> preCardaircraft = preCardcnt.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if(myCardaircraft.get(0)>preCardaircraft.get(0)){
                return true;
            }
            return false;
        }
        /* 单张、对子、三张、炸弹、飞机，都是只需要判断第一张牌大小即可 */
        else {
            return CardUtils.compareGradeTo(myCard, prevCard);
        }
    }

    /**
     * 判断当前玩家手中是否有牌可以管住上家出的牌
     *
     * @param myCards   当前玩家手中所有的牌
     * @param prevCards 上家出的牌
     * @param prevType  上家出的牌的类型
     */
    public static boolean hasHighGradeCards(List<Card> myCards,
                                            List<Card> prevCards, TypeEnum prevType) {
        if (myCards == null || prevCards == null) {
            return false;
        }
        if (prevType == null) {
            logger.error("上家出的牌不合法，无法出牌");
            return false;
        }

        // Todo 实现 => 判断当前玩家手中是否有牌可以管住上家出的牌

        Collections.sort(myCards);
        Collections.sort(prevCards);
        return false;
    }
}
