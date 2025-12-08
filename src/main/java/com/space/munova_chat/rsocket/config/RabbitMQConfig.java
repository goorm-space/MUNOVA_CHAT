package com.space.munova_chat.rsocket.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class RabbitMQConfig {

    private static final String FANOUT_NAME = "chat.fanout";
    private static final String TOPIC_NAME = "chat.topic";
    private static final String QUEUE_NAME = "chat.queue";
    private static final String ROUTING_KEY = "chat.routing_key";

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Bean(name = "rabbitConnectionFactory")
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin(@Qualifier("rabbitConnectionFactory") ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean(name = "fanoutQueue")
    public Queue fanoutQueue() {
        String queueName = "chat.server." + UUID.randomUUID() + ".fanOut";
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding fanoutBinding(@Qualifier("fanoutQueue") Queue serverQueue, FanoutExchange exchange) {
        return BindingBuilder.bind(serverQueue).to(exchange);
    }

    @Bean
    public FanoutExchange chatExchange() {
        return new FanoutExchange(FANOUT_NAME, true, false);
    }

    @Bean(name = "serverQueue")
    public Queue serverQueue() {
        String queueName = "chat.server." + UUID.randomUUID();
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_NAME, true, false);
    }

    @Bean
    public Binding topicBinding(@Qualifier("serverQueue") Queue serverQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(serverQueue).to(topicExchange).with(ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(@Qualifier("rabbitConnectionFactory") ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

}
