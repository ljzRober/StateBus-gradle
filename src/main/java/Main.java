import annotation.StateSubscribe;
import bus.InvokeTest;
import bus.StateBus;
import state.State;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Main {

    private static final String TAG = "com.ljz.bus.Main";
    private TestModel testModel;

    public Main() {
        testModel = new TestModel();
        System.out.println("main constructor");

        testInvoke();
        testInvoke();
    }

    public static void main(String[] args) {
//        com.ljz.bus.Main main = new com.ljz.bus.Main();
//        System.out.println("main,com.ljz.bus.TestModel: " + main.testModel);
//        com.ljz.bus.TestModel testModel1 = main.testModel;
//        System.out.println("main,copy,com.ljz.bus.TestModel: " + testModel1);
//        main.giveValue(main.testModel);

//        JDKProxy.getInstance().doProxy();

        StateBus.getDefault().register(Main.class);
        StateBus.getDefault().by(State.class)
                .trigger()
                .post("state trigger");

    }

    private void testInvoke() {
        try {
            InvokeTest main = InvokeTest.class.newInstance();
            Method method = InvokeTest.class.getMethod("invoke");
            method.invoke(main);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void giveValue(TestModel testModel) {
        int a = 1 + 2;
        System.out.println("giveValue,com.ljz.bus.TestModel: " + testModel);
    }


    @StateSubscribe(classPath = State.class, methodName = "trigger")
    public void StateListener(String state) {
        System.out.println(state);
    }

}
