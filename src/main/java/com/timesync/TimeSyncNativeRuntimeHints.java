package com.timesync;


import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Arrays;

public class TimeSyncNativeRuntimeHints implements RuntimeHintsRegistrar {

    private static final Logger LOG = LoggerFactory.getLogger(TimeSyncNativeRuntimeHints.class);

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Allow SPEL property injection of Duration
        Method method = ReflectionUtils.findMethod(Duration.class, "parse", CharSequence.class);
        hints.reflection().registerMethod(method, ExecutableMode.INVOKE);

        // Explicitly register jackson constructor
        hints.reflection().registerType(JacksonFeature.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

        // Auotmatically register DTOs by reflection (they must implement marker interface)
        try {
            ClassPath classPath = ClassPath.from(classLoader);
            ImmutableSet<ClassPath.ClassInfo> classes = classPath.getTopLevelClassesRecursive("com.timesync");
            for (ClassPath.ClassInfo classInfo : classes) {
                Class<?> clazz = Class.forName(classInfo.getName());
                Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(clazz);
                if (Arrays.stream(interfaces).anyMatch(i -> i.isAssignableFrom(NativeSerializable.class)) &&
                        !clazz.isInterface() &&
                        !Modifier.isAbstract(clazz.getModifiers())) {
                    LOG.info("Registering native hint for {}", classInfo.getName());
                    hints.reflection().registerType(clazz,
                            MemberCategory.DECLARED_FIELDS,
                            MemberCategory.INVOKE_PUBLIC_METHODS,
                            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
