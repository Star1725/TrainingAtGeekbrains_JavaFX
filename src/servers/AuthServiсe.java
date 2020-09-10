package servers;

public interface AuthServiсe {
    /*
    * @return nickname если user есть
    * @return null если нет
    * */
    String getNickNameByLoginAndPassword(String login, String password);
}
