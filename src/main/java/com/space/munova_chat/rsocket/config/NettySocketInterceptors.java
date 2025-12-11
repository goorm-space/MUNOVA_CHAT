package com.space.munova_chat.rsocket.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class NettySocketInterceptors implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {

        long pid = ProcessHandle.current().pid();
        System.out.println("Current PID = " + pid);

        Path fdPath = Path.of("/proc/" + pid + "/fd");

        if (!Files.exists(fdPath)) {
            System.out.println("⚠ /proc 접근 불가 — Linux 컨테이너 또는 VM 필요");
            return;
        }

        Files.list(fdPath).forEach(fd -> {

            try {
                Path link = Files.readSymbolicLink(fd);
                String target = link.toString();

                if (target.startsWith("socket:[")) {
                    System.out.println("Found socket: " + target);
                }

            } catch (Exception ignored) {
            }
        });

        System.out.println("======================================");
        System.out.println("🔥 OS-level backlog = cat /proc/sys/net/core/somaxconn");
        System.out.println("🔥 OS-level tcp_max_syn_backlog = /proc/sys/net/ipv4/tcp_max_syn_backlog");
        System.out.println("🔥 SO_REUSEADDR 은 OS 기본값 사용 중");
        System.out.println("======================================");
    }
}
