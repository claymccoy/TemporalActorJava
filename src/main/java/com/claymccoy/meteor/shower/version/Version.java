package com.claymccoy.meteor.shower.version;

import static java.util.Objects.requireNonNullElse;

public class Version
{
    private Version() {}

    public static String getVersion()
    {
        return requireNonNullElse(Version.class.getPackage().getImplementationVersion(), "unknown");
    }
}
