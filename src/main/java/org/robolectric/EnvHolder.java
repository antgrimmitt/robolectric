package org.robolectric;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class EnvHolder {
    public final Map<File, AndroidManifest> appManifestsByFile = new HashMap<File, AndroidManifest>();
    private final Map<SdkConfig, SoftReference<SdkEnvironment>> sdkToEnvironmentSoft = new HashMap<SdkConfig, SoftReference<SdkEnvironment>>();
    private final Map<SdkConfig, SdkEnvironment> sdkToEnvironment = new HashMap<SdkConfig, SdkEnvironment>();

    synchronized public SdkEnvironment getSdkEnvironment(SdkConfig sdkConfig, SdkEnvironment.Factory factory) {
        SoftReference<SdkEnvironment> reference = sdkToEnvironmentSoft.get(sdkConfig);
        SdkEnvironment sdkEnvironment = reference == null ? null : reference.get();
//            SdkEnvironment sdkEnvironment = envHolder.sdkToEnvironment.get(sdkVersion);
        if (sdkEnvironment == null) {
            if (reference != null) {
                System.out.println("DEBUG: ********************* GC'ed SdkEnvironment reused!");
            }

            sdkEnvironment = factory.create();
            sdkToEnvironmentSoft.put(sdkConfig, new SoftReference<SdkEnvironment>(sdkEnvironment));
            sdkToEnvironment.put(sdkConfig, sdkEnvironment);
//                envHolder.sdkToEnvironment.put(sdkVersion, sdkEnvironment);
        }
        return sdkEnvironment;
    }
//    public final Map<SdkConfig, SdkEnvironment> sdkToEnvironment = new HashMap<SdkConfig, SdkEnvironment>();
}
