package site.pushy.landlords.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import site.pushy.landlords.core.CardTips;
import site.pushy.landlords.core.component.RoomComponent;
import site.pushy.landlords.core.enums.CardGradeEnum;
import site.pushy.landlords.core.enums.IdentityEnum;
import site.pushy.landlords.core.enums.TypeEnum;
import site.pushy.landlords.pojo.Card;
import site.pushy.landlords.pojo.DO.User;
import site.pushy.landlords.pojo.Player;
import site.pushy.landlords.pojo.Room;
import site.pushy.landlords.core.enums.RoomStatusEnum;
import site.pushy.landlords.service.impl.GameServiceImpl;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static site.pushy.landlords.core.CardTips.hasHighGradeCards;
import static site.pushy.landlords.core.enums.CardGradeEnum.*;

@SpringBootTest
public class GameServiceTest {

    @Resource
    private GameService gameService;

    @Resource
    private RoomComponent roomComponent;

    /**
     * 模拟三个玩家加入房间并准备，最终游戏自动开始
     */
    @Test
    public void testAllPlayersReadyThenStartGame() {
        // 创建模拟用户
        User user1 = mockUser("u1");
        User user2 = mockUser("u2");
        User user3 = mockUser("u3");
        User user4 = mockUser("u4");
        // 创建房间
        Room room = roomComponent.createRoom(user1, "Test Room", "123");
        Assertions.assertNotNull(room);

        // 玩家2、3加入
        roomComponent.joinRoom(room.getId(), user2, "123");
        roomComponent.joinRoom(room.getId(), user3, "123");

        // 玩家准备
        boolean result1 = gameService.readyGame(user1);
        boolean result2 = gameService.readyGame(user2);
        boolean result3 = gameService.readyGame(user3);


        Assertions.assertFalse(result1);
        Assertions.assertFalse(result2);
        Assertions.assertTrue(result3);

        // 所有玩家都准备好之后，所有player的 状态都设置为flase，为下一句做准备，room.isAllReady()=flase
        // room 的 status 设置为 PLAYING
        Room updatedRoom = roomComponent.getRoom(room.getId());
        Assertions.assertFalse(updatedRoom.isAllReady());

//         可选：验证游戏是否已开始（你需要有这个状态字段）
         Assertions.assertEquals(RoomStatusEnum.PLAYING, updatedRoom.getStatus());
    }

    /**
     * 模拟玩家准备后又取消准备
     */
    @Test
    public void testUnReadyGame() {
        User user = mockUser("test-user");
        Room room = roomComponent.createRoom(user, "Unready Room", "abc");
        gameService.readyGame(user);

        Room roomAfterReady = roomComponent.getRoom(room.getId());
        Assertions.assertTrue(roomAfterReady.getPlayerByUserId(user.getId()).isReady());

        gameService.unReadyGame(user);
        Room roomAfterUnready = roomComponent.getRoom(room.getId());
        Assertions.assertFalse(roomAfterUnready.getPlayerByUserId(user.getId()).isReady());
    }

    //三个人都不叫，地主直接给他
    @Test
    public void testAllPlayersNoWant() {
        // 创建3个玩家并进入房间
        User user1 = mockUser("u1");
        User user2 = mockUser("u2");
        User user3 = mockUser("u3");

        Room room = roomComponent.createRoom(user1, "Room", "123");
        roomComponent.joinRoom(room.getId(), user2, "123");
        roomComponent.joinRoom(room.getId(), user3, "123");

        // 3人准备
        gameService.readyGame(user1);
        gameService.readyGame(user2);
        gameService.readyGame(user3);

        // 当前轮到 user1 叫地主（模拟房间状态为已进入叫地主流程）
        room.setBiddingPlayer(1);
        room.setFirstBiddingPlayerId(1);
        roomComponent.updateRoom(room);

        gameService.noWant(user1);  // user1 不叫
        gameService.noWant(user2);  // user2 不叫
        gameService.noWant(user3);  // user3 不叫


        //地主20张牌，农民17张牌
        Assertions.assertEquals(20,room.getPlayerById(1).getCards().size());
        Assertions.assertEquals(17,room.getPlayerById(2).getCards().size());
        Assertions.assertEquals(17,room.getPlayerById(3).getCards().size());


        // 你可以进一步判断是否房间进入下一状态，或是否重新发牌
    }


    //有人叫牌
    @Test
    public void testPlayerWantToBeLandlord() {
        User user1 = mockUser("u1");
        User user2 = mockUser("u2");
        User user3 = mockUser("u3");

        Room room = roomComponent.createRoom(user1, "Room", "123");
        roomComponent.joinRoom(room.getId(), user2, "123");
        roomComponent.joinRoom(room.getId(), user3, "123");

        gameService.readyGame(user1);
        gameService.readyGame(user2);
        gameService.readyGame(user3);

        // 设置状态为叫地主轮到 user1
        room.setBiddingPlayer(2);
        room.setFirstBiddingPlayerId(2);
        roomComponent.updateRoom(room);

        gameService.want(user2, 3); // user1 叫地主成功

        Room updated = roomComponent.getRoom(room.getId());

        // 校验结果
        Player player2 = updated.getPlayerByUserId(user2.getId());
        Assertions.assertEquals(IdentityEnum.LANDLORD, player2.getIdentity());
        Assertions.assertEquals(3, updated.getMultiple()); // 分数为3
        Assertions.assertEquals(RoomStatusEnum.PLAYING, updated.getStatus());

        // 地主牌应该已加到手牌中
        Assertions.assertTrue(player2.getCards().containsAll(updated.getDistribution().getTopCards()));

        //地主20张牌，农民17张牌
        Assertions.assertEquals(20,room.getPlayerById(2).getCards().size());
        Assertions.assertEquals(17,room.getPlayerById(1).getCards().size());
        Assertions.assertEquals(17,room.getPlayerById(3).getCards().size());

        //假设上家出了单牌
        System.out.println("====单张====");
        System.out.println("手牌："+player2.getCards());
        List<List<Card>> res = hasHighGradeCards(player2.getCards(),buildCards(THIRD), TypeEnum.SINGLE);
        System.out.println("可出的牌："+res);
        System.out.println("====对子====");
        System.out.println("手牌："+player2.getCards());
        res = hasHighGradeCards(player2.getCards(),buildCards(THIRD,THIRD), TypeEnum.PAIR);
        System.out.println("可出的牌："+res);
        System.out.println("====三张====");
        System.out.println("手牌："+player2.getCards());
        res = hasHighGradeCards(player2.getCards(),buildCards(THIRD,THIRD,THIRD), TypeEnum.THREE);
        System.out.println("可出的牌："+res);
        System.out.println("====三带一====");
        System.out.println("手牌："+player2.getCards());
        res = hasHighGradeCards(player2.getCards(),buildCards(THIRD,THIRD,THIRD,FIRST), TypeEnum.THREE_WITH_ONE);
        System.out.println("可出的牌："+res);
        System.out.println("====三带二====");
        System.out.println("手牌："+player2.getCards());
        res = hasHighGradeCards(player2.getCards(),buildCards(THIRD,THIRD,THIRD,FIRST,FIRST), TypeEnum.THREE_WITH_PAIR);
        System.out.println("可出的牌："+res);
        System.out.println("====四带二====");
        List<Card> cards4 = buildCards(FOURTH,FOURTH,FOURTH,FOURTH,FIRST,FIRST,THIRD,NINTH,NINTH,TENTH);
        System.out.println("手牌："+cards4);
        res = hasHighGradeCards(cards4,buildCards(THIRD,THIRD,THIRD,THIRD,FIRST,FIRST), TypeEnum.FOUR_WITH_TWO);
        System.out.println("可出的牌："+res);
        System.out.println("====四带二,对子====");
        System.out.println("手牌："+cards4);
        res = hasHighGradeCards(cards4,buildCards(THIRD,THIRD,THIRD,THIRD,FIRST,FIRST,SECOND,SECOND), TypeEnum.FOUR_WITH_TWO);
        System.out.println("可出的牌："+res);
        System.out.println("====四带二,对子====");
        List<Card> cards42 = buildCards(FOURTH,FOURTH,FOURTH,FOURTH,FIRST,FIRST,THIRD,NINTH,TENTH);
        System.out.println("手牌："+cards4);
        res = hasHighGradeCards(cards42,buildCards(THIRD,THIRD,THIRD,THIRD,FIRST,FIRST,SECOND,SECOND), TypeEnum.FOUR_WITH_TWO);
        System.out.println("可出的牌："+res);
        System.out.println("====顺子====");
        List<Card> cardsS = buildCards(SECOND,THIRD,FOURTH,FIFTH,SIXTH,SEVENTH,EIGHTH,NINTH,FIFTH);
        System.out.println("手牌："+cardsS);
        res = hasHighGradeCards(cardsS,buildCards(FIRST,SECOND,THIRD,FOURTH,FIFTH,SIXTH,SEVENTH), TypeEnum.STRAIGHT);
        System.out.println("可出的牌："+res);
        System.out.println("====顺子====");
        List<Card> cardsS2 = buildCards(THIRD,FOURTH,FIFTH,SIXTH,SEVENTH,EIGHTH,NINTH,TENTH);
        System.out.println("手牌："+cardsS2);
        res = hasHighGradeCards(cardsS2,buildCards(FIRST,SECOND,THIRD,FOURTH,FIFTH,SIXTH,SEVENTH), TypeEnum.STRAIGHT);
        System.out.println("可出的牌："+res);
        System.out.println("====连对====");
        List<Card> cardsP2 = buildCards(FIFTH,SIXTH,SEVENTH,FIFTH,SIXTH,SEVENTH,EIGHTH,EIGHTH);
        System.out.println("手牌："+cardsP2);
        res = hasHighGradeCards(cardsP2,buildCards(FIRST,SECOND,THIRD,FIRST,SECOND,THIRD), TypeEnum.STRAIGHT_PAIR);
        System.out.println("可出的牌："+res);
        System.out.println("====飞机====");
        List<Card> cardsA = buildCards(FIFTH,SIXTH,SEVENTH,FIFTH,SIXTH,SEVENTH,EIGHTH,EIGHTH,SIXTH,SEVENTH,EIGHTH,EIGHTH);
        System.out.println("手牌："+cardsA);
        res = hasHighGradeCards(cardsA,buildCards(FIRST,SECOND,FIRST,SECOND,FIRST,SECOND), TypeEnum.AIRCRAFT);
        System.out.println("可出的牌："+res);
        System.out.println("====飞机带两单张====");
        List<Card> cardsA1 = buildCards(FIFTH,FIFTH,FIFTH,SIXTH,SIXTH,SIXTH,SEVENTH,TENTH,TWELFTH);
        System.out.println("手牌："+cardsA1);
        res = hasHighGradeCards(cardsA1,buildCards(FIRST,SECOND,FIRST,SECOND,FIRST,SECOND,TENTH,THIRD), TypeEnum.AIRCRAFT_WITH_WINGS);
        System.out.println("可出的牌："+res);
        System.out.println("====飞机带两对子====");
        List<Card> cardsA2 = buildCards(FIFTH,FIFTH,FIFTH,SIXTH,SIXTH,SIXTH,SEVENTH,SEVENTH,TENTH,TENTH,NINTH,SIXTH);
        System.out.println("手牌："+cardsA2);
        res = hasHighGradeCards(cardsA2,buildCards(FIRST,SECOND,FIRST,SECOND,FIRST,SECOND,TENTH,THIRD,TENTH,THIRD), TypeEnum.AIRCRAFT_WITH_WINGS);
        System.out.println("可出的牌："+res);

    }

    private List<Card> buildCards(CardGradeEnum... arr) {
        return Arrays.stream(arr).map(Card::new)
                .collect(Collectors.toList());
    }
    /**
     * 工具方法：构造一个假用户
     */
    private User mockUser(String id) {
        User user = new User();
        user.setId(id);
        user.setUsername(id);
        return user;
    }
}
