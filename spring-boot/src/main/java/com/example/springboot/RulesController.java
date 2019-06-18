package com.example.springboot;

import com.example.drools.Measurement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

@RestController
public class RulesController {

    @RequestMapping("/rules")
    public String rules(
        @RequestParam(value="id", defaultValue="") String id,
        @RequestParam(value="val", defaultValue="") String val
    ) {
        Measurement measurement = new Measurement(id, val);

        // See https://docs.jboss.org/drools/release/7.18.0.Final/kie-api-javadoc/
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.insert(measurement);
        kieSession.fireAllRules();

        return measurement.toString();
    }
}

//<context:component-scan base-package="com.example.yourapp"/>
