package com.example.scheduler;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

//@ImportRuntimeHints(SchedulerApplication.Hints.class)
@SpringBootApplication
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
/*
    static class Hints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
            hints.reflection().registerType(org.springframework.ai.mcp.annotation.context.DefaultMetaProvider.class,
                    MemberCategory.values());
        }
    }*/
}

@Service
class Scheduler {

    @McpTool(description = "schedule an appointment to pick up or adopt a dog from a Pooch Palace location")
    Instant schedule(@McpToolParam int dogId) {
        var i = Instant
                .now()
                .plus(3, ChronoUnit.DAYS);
        IO.println("schedulign " + dogId + " for " + i + " on behalf of "+
                SecurityContextHolder
                        .getContext().getAuthentication().getName());
        return i;
    }
}
