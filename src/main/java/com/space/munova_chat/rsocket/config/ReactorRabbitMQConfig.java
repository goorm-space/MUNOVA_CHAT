package com.space.munova_chat.rsocket.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.*;

@Configuration
public class ReactorRabbitMQConfig {

    ///  RabbitMQ TCP 연결 객체
    @Bean(name = "reactorConnectionFactory")
    public ConnectionFactory getConnectionFactory(
            @Value("${spring.rabbitmq.host}") String host,
            @Value("${spring.rabbitmq.port}") int port,
            @Value("${spring.rabbitmq.username}") String username,
            @Value("${spring.rabbitmq.password}") String password
    ) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    ///  SENDER BEAN
    @Bean
    public Sender rabbitSender(ConnectionFactory connectionFactory) {
        SenderOptions senderOptions = new SenderOptions()
                .connectionFactory(connectionFactory)
                .resourceManagementScheduler(Schedulers.boundedElastic());  // Sender 내부 리소스 관리 작업 수행 시
        return RabbitFlux.createSender(senderOptions);
    }

    ///  RECEIVER BEAN -> SENDER 보다 블로킹 위험이 낮음
    @Bean
    public Receiver rabbitReceiver(ConnectionFactory connectionFactory) {
        ReceiverOptions receiverOptions = new ReceiverOptions()
                .connectionFactory(connectionFactory);
        return RabbitFlux.createReceiver(receiverOptions);
    }

}
