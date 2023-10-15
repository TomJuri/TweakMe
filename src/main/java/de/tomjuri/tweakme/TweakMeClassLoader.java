package de.tomjuri.tweakme;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TweakMeClassLoader extends URLClassLoader {
    private final List<URL> urls;
    private final List<ITransformer> transformers = new ArrayList<>();
    private final List<String> exclusions = new ArrayList<>();

    public TweakMeClassLoader(URL[] urls) {
        super(urls, null);
        this.urls = new ArrayList<>(Arrays.asList(urls));
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if(exclusions.stream().anyMatch(name::contains)) return super.findClass(name);
        byte[] bytes = loadClassBytes(name);
        if (bytes == null) throw new ClassNotFoundException(name);
        bytes = transform(name, bytes);
        return defineClass(name, bytes, 0, bytes.length);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
        urls.add(url);
    }

    public void addTransformers(ITransformer... transformers) {
        this.transformers.addAll(Arrays.asList(transformers));
    }

    public void addExclusions(String... exclusions) {
        this.exclusions.addAll(Arrays.asList(exclusions));
    }

    private byte[] loadClassBytes(String name) {
        String classPath = name.replace(".", "/") + ".class";
        for (URL url : urls) {
            System.out.println(url.getFile());
            if (url.getFile().endsWith(".jar")) {
               byte[] bytes = loadFromJar(classPath, url.getFile());
               if(bytes != null) return bytes;
            } else if(url.getFile().endsWith(".class")) {
              if(url.getFile().contains(classPath)) return loadFromFile(url);
            } else if(!url.getFile().substring(url.getFile().lastIndexOf("/")).contains(".")) {
                try {
                    System.out.println("dir");
                    String s = "";
                    if (!url.getFile().endsWith("/")) s = "/";
                    byte[] bytes = loadFromFile(new URL("file://" + url.getFile() + s + classPath));
                    if (bytes != null) return bytes;
                } catch (MalformedURLException ignored) {}
            }
        }
        return null;
    }

    private byte[] loadFromFile(URL file) {
        try (InputStream is = file.openStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int data;
            while ((data = is.read()) != -1) {
                baos.write(data);
            }
            return baos.toByteArray();
        } catch (IOException ignored) {
        }
        return null;
    }

    private byte[] loadFromJar(String classPath, String file) {
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry entry = jarFile.getJarEntry(classPath);
            if (entry == null) return null;
            try (InputStream is = jarFile.getInputStream(entry); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                int data;
                while ((data = is.read()) != -1) {
                    baos.write(data);
                }
                return baos.toByteArray();
            } catch (IOException ignored) {
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private byte[] transform(String name, byte[] bytes) {
        for (ITransformer transformer : transformers) {
            bytes = transformer.transform(name, bytes);
        }
        return bytes;
    }
}
