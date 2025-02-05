package com.connectivity.networkstats;

public interface IModifyAbleNbtAccounter
{
    void setQuota(long newQuota);

    long getOriginalQuota();
}
