package site.pushy.landlords.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.pushy.landlords.core.enums.TypeEnum;
import site.pushy.landlords.pojo.Card;

import java.util.*;
import java.util.stream.Collectors;

public class CardTips {
    private static final Logger logger = LoggerFactory.getLogger(CardTips.class.getSimpleName());
    /**
     * 判断当前玩家手中是否有牌可以管住上家出的牌
     *
     * 其实应该写在前端的，没必要后端生成，后端只要负责判断牌的合法性
     *
     * @param myCards   当前玩家手中所有的牌
     * @param prevCards 上家出的牌
     * @param prevType  上家出的牌的类型
     */
    public static List<List<Card>> hasHighGradeCards(List<Card> myCards,
                                            List<Card> prevCards, TypeEnum prevType) {
        if (myCards == null || prevCards == null) {
            return null;
        }
        if (prevType == null) {
            logger.error("上家出的牌不合法，无法出牌");
            return null;
        }
        // Todo 实现 => 判断当前玩家手中是否有牌可以管住上家出的牌
        Collections.sort(myCards);
        Collections.sort(prevCards);
        List<List<Card>> result = null;

        switch (prevType) {
            case SINGLE:
                if(prevCards.get(0).getGradeValue()>myCards.get(0).getGradeValue()){
                    break;
                }
                result = findHigherSingleCards(myCards, prevCards.get(0));
                break;
            case PAIR:
                if(prevCards.get(0).getGradeValue()>myCards.get(0).getGradeValue()){
                    break;
                }
                result = findHigherPairOrThreeCards(myCards, prevCards, 2);
                break;
            case THREE:
                if(prevCards.get(0).getGradeValue()>myCards.get(0).getGradeValue()){
                    break;
                }
                result = findHigherPairOrThreeCards(myCards, prevCards, 3);
                break;
            case THREE_WITH_ONE:
            case THREE_WITH_PAIR:
            case FOUR_WITH_TWO:
                result = findThreeOrFourWithAttach(myCards, prevCards, prevType);
                break;
            case STRAIGHT:
                result = findStraight(myCards, prevCards);
                break;
            case STRAIGHT_PAIR:
            case AIRCRAFT:
                result = findStraightPairOrAircraft(myCards, prevCards, prevType);
                break;
            case AIRCRAFT_WITH_WINGS:
                result = findAircraftWithWings(myCards, prevCards);
                break;
            // ✅ 如果上家是炸弹，你只能出更大的炸弹或王炸
            case BOMB:
                result = findBombs(myCards).stream()
                        .filter(bomb -> bomb.get(0).getGradeValue() > prevCards.get(0).getGradeValue())
                        .collect(Collectors.toList());
                if (result.isEmpty()) {
                    List<Card> jokerBomb = findJokerBomb(myCards);
                    if (jokerBomb != null) {
                        result = Collections.singletonList(jokerBomb);
                    }
                }
                break;
            // ✅ 如果上家是王炸，直接返回 null
            case JOKER_BOMB:
                return null;
        }
        // 如果不是炸弹类，尝试炸弹或王炸压制
        if (prevType != TypeEnum.BOMB &&
                prevType != TypeEnum.JOKER_BOMB) {
            List<List<Card>> bombs = findBombs(myCards);
            if (!bombs.isEmpty()){
                if (result == null) result = new ArrayList<>();
                result.addAll(bombs);
            }

            List<Card> jokerBomb = findJokerBomb(myCards);
            if (jokerBomb != null){
                if (result == null) result = new ArrayList<>();
                result.add(jokerBomb);
            }
        }


        return result;
    }

    public static List<List<Card>> findHigherSingleCards(List<Card> myCards, Card prevCard) {
        List<List<Card>> result = new ArrayList<>();
        int prevValue = prevCard.getGradeValue();

        for (Card card : myCards) {
            if (card.getGradeValue() > prevValue) {
                result.add(Collections.singletonList(card));
            }
        }
        return result;
    }

    /**
     *
     * @param myCards   当前玩家的牌
     * @param prevCards 上家出的牌
     * @param num       要求的牌数量（2 为对子，3 为三张）
     * @return 所有符合条件的牌组合列表
     */
    public static List<List<Card>> findHigherPairOrThreeCards(List<Card> myCards, List<Card> prevCards,int num) {
        List<List<Card>> result = new ArrayList<>();
        Map<Integer, List<Card>> map = new HashMap<>();
        for (Card card : myCards) {
            map.computeIfAbsent(card.getGradeValue(), k -> new ArrayList<>()).add(card);
        }
        int prevGrade = prevCards.get(0).getGradeValue();  // 上家主牌的牌值
        for (Map.Entry<Integer, List<Card>> entry : map.entrySet()) {
            int grade = entry.getKey();
            List<Card> group = entry.getValue();

            // 如果手牌中这个牌值的数量达到要求，并且比上家牌大，加入结果
            if (group.size() >= num && grade > prevGrade) {
                result.add(group.subList(0, num));
            }
        }

        return result;
    }
    public static List<List<Card>> findThreeOrFourWithAttach(List<Card> myCards, List<Card> prevCards, TypeEnum type) {
        List<List<Card>> result = new ArrayList<>();

        // 分组手牌
        Map<Integer, List<Card>> map = new HashMap<>();
        for (Card card : myCards) {
            map.computeIfAbsent(card.getGradeValue(), k -> new ArrayList<>()).add(card);
        }

        // 获取上家主牌的牌值（从 prevCards 中找出出现3次或4次的牌）
        Map<Integer, Integer> prevMap = new HashMap<>();
        for (Card card : prevCards) {
            prevMap.put(card.getGradeValue(), prevMap.getOrDefault(card.getGradeValue(), 0) + 1);
        }
        int prevGrade = -1;
        int mainCount = (type == TypeEnum.THREE_WITH_ONE || type == TypeEnum.THREE_WITH_PAIR) ? 3 : 4;
        for (Map.Entry<Integer, Integer> entry : prevMap.entrySet()) {
            if (entry.getValue() == mainCount) {
                prevGrade = entry.getKey();
                break;
            }
        }
        if (prevGrade == -1) {
            return result; // 无法识别上家的主牌
        }

        // 遍历所有可用主牌组合
        for (Map.Entry<Integer, List<Card>> entry : map.entrySet()) {
            int grade = entry.getKey();
            List<Card> group = entry.getValue();

            if (group.size() >= mainCount && grade > prevGrade) {
                List<Card> main = group.subList(0, mainCount);

                // 准备副牌：从 myCards 中去除主牌部分
                List<Card> rest = new ArrayList<>(myCards);
                rest.removeAll(main);

                if (type == TypeEnum.THREE_WITH_ONE) {
                    for (Card card : rest) {
                        List<Card> combo = new ArrayList<>(main);
                        combo.add(card);
                        result.add(combo);
                    }

                } else if (type == TypeEnum.THREE_WITH_PAIR) {
                    Map<Integer, List<Card>> pairMap = new HashMap<>();
                    for (Card card : rest) {
                        pairMap.computeIfAbsent(card.getGradeValue(), k -> new ArrayList<>()).add(card);
                    }
                    for (List<Card> pair : pairMap.values()) {
                        if (pair.size() >= 2) {
                            List<Card> combo = new ArrayList<>(main);
                            combo.addAll(pair.subList(0, 2));
                            result.add(combo);
                        }
                    }

                } else if (prevCards.size() == 8) {
                    // 四带两个对子
                    Map<Integer, List<Card>> pairMap = new HashMap<>();
                    for (Card card : rest) {
                        pairMap.computeIfAbsent(card.getGradeValue(), k -> new ArrayList<>()).add(card);
                    }

                    // 收集所有对子（数量 >= 2）
                    List<List<Card>> pairs = new ArrayList<>();
                    for (List<Card> group2 : pairMap.values()) {
                        if (group2.size() >= 2) {
                            pairs.add(group2.subList(0, 2));
                        }
                    }

                    // 🔺 加一个必要判断：必须有两个及以上对子才可能出“四带两对”
                    if (pairs.size() < 2) {
                        return null; // 没有足够对子，不能构成有效组合
                    }

                    // 从所有对子中选两组不同的
                    for (int i = 0; i < pairs.size(); i++) {
                        for (int j = i + 1; j < pairs.size(); j++) {
                            List<Card> combo = new ArrayList<>(main);
                            combo.addAll(pairs.get(i));
                            combo.addAll(pairs.get(j));
                            result.add(combo);
                        }
                    }
                }


            }

        }

        return result;
    }
    public static List<List<Card>> findStraight(List<Card> myCards, List<Card> prevCards) {
        List<List<Card>> result = new ArrayList<>();

        // 上家顺子的最大牌不能是 A（TWELFTH），否则顺子不能再大
        int prevMaxGrade = prevCards.stream()
                .mapToInt(Card::getGradeValue)
                .max()
                .orElse(-1);

        if (prevMaxGrade >= 12) {
            return null;  // 顺子最大不能超过A
        }

        int straightLength = prevCards.size(); // 顺子的长度必须一样

        // 只考虑 3 ~ A 的牌
        Map<Integer, Card> cardMap = new HashMap<>();
        for (Card card : myCards) {
            int grade = card.getGradeValue();
            if (grade >= 1 && grade <= 12) {// 只考虑3-A
                // 只保留一张（顺子不能重复值）
                cardMap.putIfAbsent(grade, card);
            }
        }

        // 提取所有可用 grade 值，并排序
        List<Integer> sortedGrades = new ArrayList<>(cardMap.keySet());
        Collections.sort(sortedGrades);

        // 滑动窗口找长度为 straightLength 的连续序列
        for (int i = 0; i <= sortedGrades.size() - straightLength; i++) {
            boolean isStraight = true;
            for (int j = 1; j < straightLength; j++) {
                if (sortedGrades.get(i + j) != sortedGrades.get(i) + j) {
                    isStraight = false;
                    break;
                }
            }
            if (isStraight && sortedGrades.get(i + straightLength - 1) > prevMaxGrade) {
                List<Card> straight = new ArrayList<>();
                for (int j = 0; j < straightLength; j++) {
                    straight.add(cardMap.get(sortedGrades.get(i + j)));
                }
                result.add(straight);
            }
        }

        return result;
    }
    public static List<List<Card>> findStraightPairOrAircraft(List<Card> myCards, List<Card> prevCards, TypeEnum type) {
        List<List<Card>> result = new ArrayList<>();

        if (myCards == null || prevCards == null || type == null) return result;

        // 设置规则参数
        int groupSize;
        switch (type) {
            case STRAIGHT_PAIR:
                groupSize = 2;
                break;
            case AIRCRAFT:
                groupSize = 3;
                break;
            default:
                return result; // 非法类型
        }

        int totalSize = prevCards.size();
        if (totalSize % groupSize != 0) return result;

        int groupCount = totalSize / groupSize;

        // 找出上家牌中连续组的最大牌值
        Map<Integer, Integer> prevMap = new HashMap<>();
        for (Card card : prevCards) {
            prevMap.put(card.getGradeValue(), prevMap.getOrDefault(card.getGradeValue(), 0) + 1);
        }
        List<Integer> prevGroupGrades = prevMap.entrySet().stream()
                .filter(e -> e.getValue() >= groupSize)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
        if (prevGroupGrades.isEmpty()) return result;

        int prevMax = Collections.max(prevGroupGrades);
        if (prevMax == 12) {
            return null;  // A 不能被超越
        }

        // 分组当前手牌
        Map<Integer, List<Card>> groupMap = new HashMap<>();
        for (Card card : myCards) {
            int grade = card.getGradeValue();
            if (grade <= 12) {
                groupMap.computeIfAbsent(grade, k -> new ArrayList<>()).add(card);
            }
        }

        List<Integer> validGrades = groupMap.entrySet().stream()
                .filter(e -> e.getValue().size() >= groupSize)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());

        // 滑动窗口查找连续 groupCount 个组合
        for (int i = 0; i <= validGrades.size() - groupCount; i++) {
            boolean isConsecutive = true;
            for (int j = 1; j < groupCount; j++) {
                if (validGrades.get(i + j) != validGrades.get(i) + j) {
                    isConsecutive = false;
                    break;
                }
            }

            int highestGrade = validGrades.get(i + groupCount - 1);
            if (isConsecutive && highestGrade > prevMax) {
                List<Card> combo = new ArrayList<>();
                for (int j = 0; j < groupCount; j++) {
                    combo.addAll(groupMap.get(validGrades.get(i + j)).subList(0, groupSize));
                }
                result.add(combo);
            }
        }

        return result;
    }
    private static List<List<Card>> findAircraftWithWings(List<Card> myCards, List<Card> prevCards) {
        List<List<Card>> result = new ArrayList<>();
        if (myCards == null || prevCards == null || prevCards.size() < 8) return result;

        // 1. 解析上家的飞机主体（三张连续）
        Map<Integer, List<Card>> prevMap = new HashMap<>();
        for (Card card : prevCards) {
            prevMap.computeIfAbsent(card.getGradeValue(), k -> new ArrayList<>()).add(card);
        }

        List<Integer> prevTriples = prevMap.entrySet().stream()
                .filter(e -> e.getValue().size() >= 3)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());

        if (prevTriples.isEmpty()) return result;
        int groupCount = prevTriples.size();  // 飞机长度
        int totalSize = prevCards.size();
        boolean isWingPair = totalSize % 5 == 0;

        int prevMax = Collections.max(prevTriples);
        if (prevMax >= 12) return null;

        // 2. 当前牌按牌值归类
        Map<Integer, List<Card>> myMap = new HashMap<>();
        for (Card card : myCards) {
            myMap.computeIfAbsent(card.getGradeValue(), k -> new ArrayList<>()).add(card);
        }

        List<Integer> myTriples = myMap.entrySet().stream()
                .filter(e -> e.getValue().size() >= 3)
                .map(Map.Entry::getKey)
                .filter(v -> v <= 12)
                .sorted()
                .collect(Collectors.toList());

        // 3. 构造所有合法飞机主体
        for (int i = 0; i <= myTriples.size() - groupCount; i++) {
            boolean isConsecutive = true;
            for (int j = 1; j < groupCount; j++) {
                if (myTriples.get(i + j) != myTriples.get(i) + j) {
                    isConsecutive = false;
                    break;
                }
            }

            int maxTriple = myTriples.get(i + groupCount - 1);
            if (isConsecutive && maxTriple > prevMax) {
                // 构建主体
                List<Card> mainPart = new ArrayList<>();
                Set<Integer> used = new HashSet<>();
                for (int j = 0; j < groupCount; j++) {
                    int grade = myTriples.get(i + j);
                    mainPart.addAll(myMap.get(grade).subList(0, 3));
                    used.add(grade);
                }

                // 剩余牌
                List<Card> rest = myCards.stream()
                        .filter(c -> !used.contains(c.getGradeValue()))
                        .collect(Collectors.toList());

                if (isWingPair) {
                    // 飞机带对：构造所有组合对子的组合
                    Map<Integer, List<Card>> pairMap = new HashMap<>();
                    for (Card c : rest) {
                        pairMap.computeIfAbsent(c.getGradeValue(), k -> new ArrayList<>()).add(c);
                    }

                    List<List<Card>> allPairs = pairMap.values().stream()
                            .filter(list -> list.size() >= 2)
                            .map(list -> list.subList(0, 2))  // 保证每对包含2张牌
                            .collect(Collectors.toList());

                    if (allPairs.size() >= groupCount) {
                        List<List<List<Card>>> pairCombos = combinePairs(allPairs, groupCount);
                        for (List<List<Card>> comboPairs : pairCombos) {
                            List<Card> combo = new ArrayList<>(mainPart);
                            for (List<Card> pair : comboPairs) {
                                combo.addAll(pair);  // ✅ 一定要 addAll，不能 add(pair.get(0))
                            }
                            result.add(combo);
                        }
                    }
                }

                else {
                    // 飞机带单
                    List<List<Card>> wingSingles = combineSingles(rest, groupCount);
                    for (List<Card> wings : wingSingles) {
                        List<Card> fullCombo = new ArrayList<>(mainPart);
                        fullCombo.addAll(wings);
                        result.add(fullCombo);
                    }
                }
            }
        }

        return result;
    }
    // 单张组合
    private static List<List<Card>> combineSingles(List<Card> cards, int count) {
        List<List<Card>> result = new ArrayList<>();
        int n = cards.size();
        if (n < count) return result;

        backtrackSingle(cards, new ArrayList<>(), 0, count, result);
        return result;
    }

    private static void backtrackSingle(List<Card> cards, List<Card> path, int start, int count, List<List<Card>> result) {
        if (path.size() == count) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < cards.size(); i++) {
            path.add(cards.get(i));
            backtrackSingle(cards, path, i + 1, count, result);
            path.remove(path.size() - 1);
        }
    }

    // 对子组合
    private static List<List<List<Card>>> combinePairs(List<List<Card>> pairs, int count) {
        List<List<List<Card>>> result = new ArrayList<>();
        combinePairBacktrack(pairs, new ArrayList<>(), 0, count, result);
        return result;
    }

    private static void combinePairBacktrack(List<List<Card>> pairs, List<List<Card>> path, int start, int count, List<List<List<Card>>> result) {
        if (path.size() == count) {
            result.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < pairs.size(); i++) {
            path.add(pairs.get(i));
            combinePairBacktrack(pairs, path, i + 1, count, result);
            path.remove(path.size() - 1);
        }
    }


    // 查找普通炸弹（4张相同）
    public static List<List<Card>> findBombs(List<Card> cards) {
        Map<Integer, List<Card>> map = new HashMap<>();
        for (Card card : cards) {
            map.computeIfAbsent(card.getGradeValue(), k -> new ArrayList<>()).add(card);
        }

        List<List<Card>> result = new ArrayList<>();
        for (List<Card> group : map.values()) {
            if (group.size() == 4) {
                result.add(new ArrayList<>(group));
            }
        }
        return result;
    }

    // 查找王炸（大王+小王）
    public static List<Card> findJokerBomb(List<Card> cards) {
        boolean hasSmall = false, hasBig = false;
        Card smallJoker = null, bigJoker = null;
        for (Card c : cards) {
            if (c.getGradeValue() == 14) { // 小王
                hasSmall = true;
                smallJoker = c;
            } else if (c.getGradeValue() == 15) { // 大王
                hasBig = true;
                bigJoker = c;
            }
        }
        if (hasSmall && hasBig) {
            return Arrays.asList(smallJoker, bigJoker);
        }
        return null;
    }




}
