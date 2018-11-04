import java.util.ArrayList;

/**
 * Created by Anna on 04.11.2018.
 */
public class User extends ArrayList<Double> {
    public Double getMark(int i){
        return get(i);
    }

    public void setMark(int index, Double mark){
       set(index,mark);
    }

    public void addMark(Double mark){
        add(mark);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<size(); i++){
            stringBuilder.append(getMark(i)).append(" ");
        }
        return stringBuilder.toString();
    }
}
