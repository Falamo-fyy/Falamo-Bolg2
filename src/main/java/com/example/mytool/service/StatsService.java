package com.example.mytool.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    public Object getStats(String username) {
        // 实现获取用户统计信息的逻辑
        // 这里只是一个示例，实际中需要根据业务需求实现
        // 假设返回一个包含 postCount 和 likeCount 属性的对象
        // 可以是一个自定义的 DTO 类
        @Getter
        class Stats {
            private int postCount;
            private int likeCount;

            public Stats(int postCount, int likeCount) {
                this.postCount = postCount;
                this.likeCount = likeCount;
            }

        }

        return new Stats(10, 20); // 示例数据
    }
}