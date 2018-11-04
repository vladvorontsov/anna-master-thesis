import java.util.ArrayList;

/**
 * Created by Anna on 04.11.2018.
 */
public class ListOfMarks extends ArrayList<User> {
    public User getUser(int i){
        return get(i);
    }

    public void addUser(User user){
        add(user);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<size(); i++){
            stringBuilder.append(getUser(i)).append("\n");
        }
        return stringBuilder.toString();
    }
}
