package com.agora.facerecovery.model;

import java.io.File;

public class FaceResult {

    private final File original;
    private final File withGlasses;
    private final File cleaned;

    public FaceResult(File original, File withGlasses, File cleaned) {
        this.original = original;
        this.withGlasses = withGlasses;
        this.cleaned = cleaned;
    }

    public File getOriginal() {
        return original;
    }

    public File getWithGlasses() {
        return withGlasses;
    }

    public File getCleaned() {
        return cleaned;
    }

}
