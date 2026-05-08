package com.logobing.utils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BingoNumberGenerator {
    private static final int MAX = 75;
    private final Set<Integer> called = new HashSet<>();
    private final List<Integer> history = new ArrayList<>();
    
    public int draw() {
        if (called.size() >= MAX) return -1;
        int num;
        do { num = ThreadLocalRandom.current().nextInt(1, MAX + 1); }
        while (called.contains(num));
        called.add(num);
        history.add(num);
        return num;
    }
    
    public void reset() { called.clear(); history.clear(); }
    public List<Integer> getHistory() { return new ArrayList<>(history); }
    public Set<Integer> getCalled() { return new HashSet<>(called); }
}
