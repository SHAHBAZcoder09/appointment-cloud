package com.appointment.order.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initFlowRules() {
        FlowRule rule = new FlowRule();
        rule.setResource("bookAppointment");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(2); // Allow max 2 bookings per second; excess triggers 429
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }
}
