/**
 * Created by giko on 10/12/2014.
 */
public class LogServerEvent {
    private String type;
    private Object object;

    public LogServerEvent(String type, Object object) {
        this.type = type;
        this.object = object;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
