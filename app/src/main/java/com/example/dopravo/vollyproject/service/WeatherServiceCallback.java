package com.example.dopravo.vollyproject.service;

import com.example.dopravo.vollyproject.data.Channel;

/**
 * Created by Dopravo on 7/4/2016.
 */
public interface WeatherServiceCallback {

    void serviceSuccess(Channel channel);

    void serviceFailure(Exception exception);
}
