package com.demo.taskmanager.service;

import org.springframework.stereotype.Service;

@Service
public class QualityGateDemoService {

    public String normalizeTitle(String title) {
        return title == null ? "" : title.trim();
    }
}
