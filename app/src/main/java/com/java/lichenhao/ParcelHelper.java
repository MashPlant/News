package com.java.lichenhao;

import android.os.Parcelable;
import android.support.annotation.NonNull;

public class ParcelHelper {
    private ParcelHelper() {
    }


    // https://stackoverflow.com/questions/51799353/how-to-use-parcel-readtypedlist-along-with-parcelize-from-kotlin-android-exte
    // 看起来还是没有更好的方法?
    @NonNull
    @SuppressWarnings("unchecked")
    public static final Parcelable.Creator<NewsExt> CREATOR = NewsExt.CREATOR;
}
