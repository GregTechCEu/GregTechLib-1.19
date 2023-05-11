package com.gregtechceu.gtlib.syncdata;

public interface IContentChangeAware {
    void setOnContentsChanged(Runnable onContentChanged);
    Runnable getOnContentsChanged();
}
