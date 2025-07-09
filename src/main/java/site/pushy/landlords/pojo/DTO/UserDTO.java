package site.pushy.landlords.pojo.DTO;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserDTO {

    @NotNull
    private String username;

    @NotNull
    private String password;

    @NotNull
    private String avatar;

    private String gender;
}
