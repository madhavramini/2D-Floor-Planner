import java.io.Serializable;

public class Fixtures implements Serializable {
    int position;
    String type;
    int size;

    Fixtures(int position, String type, int size){
        this.position = position;
        this.type = type;
        this.size = size;
    }


}
