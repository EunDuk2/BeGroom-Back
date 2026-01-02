package com.example.BeGroom.payment_processor;

import com.example.BeGroom.payment.domain.PaymentMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentProcessorFactory {

    private final Map<PaymentMethod, PaymentProcessor> processorMap;

    @Autowired
    public PaymentProcessorFactory(List<PaymentProcessor> processors) {
        this.processorMap = processors.stream()
                .collect(Collectors.toMap(
                        PaymentProcessor::getMethod,
                        Function.identity()
                ));
    }

    public PaymentProcessor get(PaymentMethod method) {
        PaymentProcessor processor = processorMap.get(method);
        if (processor == null) {
            throw new IllegalArgumentException("지원하지 않는 결제 수단: " + method);
        }
        return processor;
    }
}
