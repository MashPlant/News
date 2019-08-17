package com.java.lichenhao;

import android.os.Parcelable;
import android.support.annotation.NonNull;

public class ParcelHelper {
    private ParcelHelper() {
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static final Parcelable.Creator<NewsExt> NEWS_EXT_CREATOR = NewsExt.CREATOR;
}
