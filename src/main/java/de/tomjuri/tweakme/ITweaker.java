package de.tomjuri.tweakme;

import java.util.List;

public interface ITweaker {
    void options(String launchTarget, String[] args);
    void inject(ClassLoader classLoader);
    List<ITransformer> transformers();
}
