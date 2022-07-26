package com.black.router;

import android.os.Bundle;

public interface FragmentRouteHelper {
    void openFragment(Class fragmentClass, int fragmentIndex, Bundle extras);
}
