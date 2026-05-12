package com.sunyard.ecm.config;

import com.sunyard.ecm.constant.IcmsConstants;
import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Queue;
import org.springframework.util.StringUtils;

/**
 * @author lw
 * @Date: 2023/8/28
 * @Desc MQ配置类
 */
@Component
@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class MqConfig {
    /**
     * 队列名称
     */
    private String queueName;
    /**
     * 交换机名称
     */
    private String exchangeName;
    /**
     * 环境
     */
    private String environment;

    // 生成带环境的名称（带环境前缀或不带）
    public String getName(String baseName) {
        if (StringUtils.hasText(environment)) {
            return environment + "_" + baseName;
        }
        return baseName;
    }

    //获取智能化队列交换机名称
    public String getExchangeEcmIntelligent(){
        return getName(IcmsConstants.EXCHANGE_ECM_INTELLIGENT);
    }

    @Bean
    public Queue docOcr() {
        return new Queue(getName(IcmsConstants.QUEUE_DOC_OCR), true);
    }

    @Bean
    public Queue afm() {
        return new Queue(getName(IcmsConstants.QUEUE_AFM), true);
    }

    @Bean
    public Queue obscure() {
        return new Queue(getName(IcmsConstants.QUEUE_OBSCURE), true);
    }

    @Bean
    public Queue regularize() {
        return new Queue(getName(IcmsConstants.QUEUE_REGULARIZE), true);
    }

    @Bean
    public Queue remake() {
        return new Queue(getName(IcmsConstants.QUEUE_REMAKE), true);
    }

    @Bean
    public Queue esContext() {
        return new Queue(getName(IcmsConstants.QUEUE_ES_CONTEXT), true);
    }

    @Bean
    public Queue reflective() {
        return new Queue(getName(IcmsConstants.QUEUE_REFLECTIVE), true);
    }

    @Bean
    public Queue missCorner() {
        return new Queue(getName(IcmsConstants.QUEUE_MISS_CORNER), true);
    }

    @Bean
    public Queue special() {
        return new Queue(getName(IcmsConstants.QUEUE_SPECIAL), true);
    }

    // 交换器
    @Bean
    public DirectExchange exchange() {
        // 创建一个持久化的direct类型交换器
        return new DirectExchange(getExchangeEcmIntelligent(), true, false);
    }

    // 绑定队列和交换器
    @Bean
    public Binding bindingDocOcr(Queue docOcr, DirectExchange exchange) {
        return BindingBuilder.bind(docOcr).to(exchange).with("docOcr");
    }

    @Bean
    public Binding bindingAfm(Queue afm, DirectExchange exchange) {
        return BindingBuilder.bind(afm).to(exchange).with("afm");
    }

    @Bean
    public Binding bindingObscure(Queue obscure, DirectExchange exchange) {
        return BindingBuilder.bind(obscure).to(exchange).with("obscure");
    }

    @Bean
    public Binding bindingRegularize(Queue regularize, DirectExchange exchange) {
        return BindingBuilder.bind(regularize).to(exchange).with("regularize");
    }

    @Bean
    public Binding bindingRemake(Queue remake, DirectExchange exchange) {
        return BindingBuilder.bind(remake).to(exchange).with("remake");
    }

    @Bean
    public Binding bindingEsContext(Queue esContext, DirectExchange exchange) {
        return BindingBuilder.bind(esContext).to(exchange).with("esContext");
    }

    @Bean
    public Binding bindingReflective(Queue reflective, DirectExchange exchange) {
        return BindingBuilder.bind(reflective).to(exchange).with("reflective");
    }

    @Bean
    public Binding bindingMissCorner(Queue missCorner, DirectExchange exchange) {
        return BindingBuilder.bind(missCorner).to(exchange).with("missCorner");
    }

    @Bean
    public Binding bindingSpecial(Queue special, DirectExchange exchange) {
        return BindingBuilder.bind(special).to(exchange).with("special");
    }
}