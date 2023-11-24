package state;

import bus.StateObserver;

public interface State {

    StateObserver<String> trigger();

}
