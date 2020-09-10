package servers;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthServiсe implements AuthServiсe{

    private class UserData{
        String login;
        String password;
        String nickName;

        public UserData(String login, String password, String nickName) {
            this.login = login;
            this.password = password;
            this.nickName = nickName;
        }
    }

    List<UserData> usersDataList;

    public SimpleAuthServiсe() {
        usersDataList = new ArrayList<>();
        for (int i = 1; i < 11 ; i++) {
            usersDataList.add(new UserData("login" + i, "login" + i, "nick" + i));
        }
        usersDataList.add(new UserData("qwe", "qwe", "qwe"));
        usersDataList.add(new UserData("asd", "asd", "asd"));
        usersDataList.add(new UserData("zxc", "zxc", "zxc"));
    }

    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
        for (UserData userData : usersDataList) {
            if (userData.login.equals(login) && userData.password.equals(password)){
                return userData.nickName;
            }
        }
        return null;
    }
}
