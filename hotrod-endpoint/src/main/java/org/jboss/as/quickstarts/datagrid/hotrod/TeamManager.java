/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.datagrid.hotrod;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.SaslQop;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.configuration.BasicConfiguration;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Martin Gencur
 */
@Component
public class TeamManager {

    //@Autowired
    //@Value("${jdg.host}")
    private String JDG_HOST = "rhdgroute-datagrid.apps.35.211.184.5.nip.io";//"cache-service-hotrod-route-rhdg.apps.35.211.184.5.nip.io";
    //@Value("${jdg.hotrod.port}")
    private Integer HOTROD_PORT = 443;
    private static final String teamsKey = "teams";
    private final String cacheName = "custom";

    private RemoteCacheManager cacheManager;
    private RemoteCache<String, Object> cache;
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();

    @Autowired
    public TeamManager() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
              .host("datagrid-service.rhdg.svc").port(11222)
                //.clientIntelligence(ClientIntelligence.BASIC)
                .security()
                .authentication().enable()
                .username("datagrid")
                .password("datagrid") //   IfwaxrQkNRf6O0Xs
                .serverName("datagrid-service")
                //.saslMechanism("DIGEST-MD5")
                .saslQop(SaslQop.AUTH)
                .ssl()
                //.sniHostName(JDG_HOST)
                //.trustStoreFileName(tccl.getResource("truststore.jks").getPath())
                //.trustStorePassword("changeit".toCharArray());
                .trustStorePath("/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt"); ///src/main/resources


        cacheManager = new RemoteCacheManager(builder.build());
        /*cacheManager.administration()
                .withFlags(CacheContainerAdmin.AdminFlag.PERMANENT)
                .createCache(cacheName, null);*/

        cache = cacheManager.getCache("teams");
        if(!cache.containsKey(teamsKey)) {
            List<String> teams = new ArrayList<String>();
            String team = "Barcelona";
            teams.add(team);
            cache.put(teamsKey, teams);
        }
    }

    public void addTeam(String teamName) {
        cache.put(teamsKey, teamName);
    }

    public void removeTeam(String teamName) {
        String team = (String) cache.get(teamName);
        if (team != null) {
            @SuppressWarnings("unchecked")
            List<String> teams = (List<String>) cache.get(teamsKey);
            if (teams != null) {
                teams.remove(teamName);
            }
            cache.put(teamsKey, teams);
        } else {
            System.out.println("msgTeamMissing :  " + teamName);
        }
    }

    public String printTeams() {
        return (String) cache.get(teamsKey);

    }

    public void stop() {
        cacheManager.stop();
    }

}
