package site.pushy.landlords.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import site.pushy.landlords.core.component.RoomComponent;
import site.pushy.landlords.core.enums.IdentityEnum;
import site.pushy.landlords.pojo.DO.User;
import site.pushy.landlords.pojo.Player;
import site.pushy.landlords.pojo.Room;
import site.pushy.landlords.core.enums.RoomStatusEnum;
import site.pushy.landlords.service.impl.GameServiceImpl;

import javax.annotation.Resource;

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
