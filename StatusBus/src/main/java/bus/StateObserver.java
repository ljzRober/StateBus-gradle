package bus;

public interface StateObserver<T> {

    void post(T data);

}
