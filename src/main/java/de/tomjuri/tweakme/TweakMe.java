package de.tomjuri.tweakme;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TweakMe {
    private final TweakMeClassLoader classLoader;

    public static void main(String[] args) {
        new TweakMe().launch(new String[]{"--launchTarget", "com.someone.Test", "--tweaker", "yay.yoy.YaTweaker"});
    }

    private TweakMe() {
        classLoader = new TweakMeClassLoader(((URLClassLoader) getClass().getClassLoader()).getURLs());
        classLoader.addExclusions("de.tomjuri.tweakme");
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    public void launch(String[] args) {
        String launchTarget = "";
        List<String> tweakers = new ArrayList<>();
        List<ITransformer> transformers = new ArrayList<>();

        for (String arg : args) {
            if (arg.equals("--launchTarget"))
                launchTarget = args[Arrays.asList(args).indexOf(arg) + 1];
            if (arg.equals("--tweaker"))
                tweakers.add(args[Arrays.asList(args).indexOf(arg) + 1]);
        }

        System.out.println("Launch target: " + launchTarget);
        System.out.println("Tweakers: " + tweakers);

        for (String tweaker : tweakers) {
            classLoader.addExclusions(tweaker);
            try {
                ITweaker iTweaker = (ITweaker) Class.forName(tweaker).getDeclaredConstructor().newInstance();
                iTweaker.options(launchTarget, args);
                iTweaker.inject(classLoader);
                transformers.addAll(iTweaker.transformers());
            } catch (Exception ignored) {}
        }

        classLoader.addTransformers(transformers.toArray(new ITransformer[0]));

        try {
            Class.forName(launchTarget, false, classLoader).getDeclaredMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Exception ignored) {}
    }
}