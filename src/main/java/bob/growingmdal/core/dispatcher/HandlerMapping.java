package bob.growingmdal.core.dispatcher;

import java.lang.reflect.Method;

public record HandlerMapping(Object handler, Method method, ParameterBinding binding) {
}
