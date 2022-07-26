package com.black.router;

import android.os.Bundle;

public interface RouteCheckHelper {
    public void routeCheck(String uri, String beforePath, int requestCode, int flags, Bundle extras);
}
