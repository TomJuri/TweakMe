package de.tomjuri.tweakme;

public interface ITransformer {
    byte[] transform(String name, byte[] bytes);
}
