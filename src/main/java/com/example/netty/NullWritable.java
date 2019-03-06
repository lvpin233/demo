package com.example.netty;

import java.io.Serializable;

/**
 * Created by Lvpin on 2019/3/6.
 */
public class NullWritable implements Serializable {
    private static final long serialVersionUID = -8191640400484155111L;
    private static NullWritable instance = new NullWritable();

    private NullWritable() {
    }

    public static NullWritable nullWritable() {
        return instance;
    }
}
