package com.example.baseactivity;

public class InjectHelper {
    public static int injectObject(Object handler) throws Exception {

        Class<?> handlerType = handler.getClass();

        // inject ContentView
        AhView contentView = handlerType.getAnnotation(AhView.class);
        if (contentView != null) {
            try {
                return contentView.value();
            } catch (Throwable e) {
                throw new Exception("No injection layout");
            }
        }else{
            throw new Exception("No injection layout");
        }
    }
}
