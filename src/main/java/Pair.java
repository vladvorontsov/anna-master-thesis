/**
 * Created by Anna on 19.11.2018.
 */
public class Pair {
    Double valueOfF;
    Integer integerOfX;

    public Pair(Double valueOfF, Integer integerOfX){
        this.valueOfF = valueOfF;
        this.integerOfX = integerOfX;
    }

    public void setIntegerOfX(Integer integerOfX) {
        this.integerOfX = integerOfX;
    }

    public void setValueOfF(Double valueOfF) {
        this.valueOfF = valueOfF;
    }

    public Integer getIntegerOfX() {
        return integerOfX;
    }

    public Double getValueOfF() {
        return valueOfF;
    }
}
