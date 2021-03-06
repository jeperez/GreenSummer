package org.greeneyed.summer.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * #%L
 * Summer
 * %%
 * Copyright (C) 2018 GreenEyed (Daniel Lopez)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.greeneyed.summer.util.ApplicationContextProvider;
import org.greeneyed.summer.util.SummerJoltView;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "summer.jolt")
@Data
@Slf4j
public class JoltViewConfiguration {

    public static final String DEFAULT_SPEC_PREFIX = "/json-spec/";
    public static final String JSON_SOURCE_TAG = "jsonSource";
    public static final String REFRESH_SPEC_FLAG = "refreshSpec";
    public static final String SHOW_JSON_SOURCE_FLAG = "showJsonSource";
    public static final String DEFAULT_SPEC_SUFFIX = ".json";

    private boolean devMode = false;
    private String specPrefix = DEFAULT_SPEC_PREFIX;
    private String specSuffix = DEFAULT_SPEC_SUFFIX;

    private Map<String, Chainr> chainrCache = new ConcurrentHashMap<>();

    public static class JoltModelAndView extends ModelAndView {
        public JoltModelAndView(String viewName, Object modelObject) {
            this(viewName, modelObject, HttpStatus.OK);
        }

        public JoltModelAndView(String viewName, Object modelObject, HttpStatus status) {
            super(new SummerJoltView(viewName, ApplicationContextProvider.getApplicationContext().getBean(JoltViewConfiguration.class)),
                    JSON_SOURCE_TAG, modelObject);
            setStatus(status);
        }
    }

    public synchronized Chainr getChainr(String joltSpecName, boolean refresh) {
        if (refresh) {
            final Chainr chainr = createChainr(joltSpecName);
            chainrCache.put(joltSpecName, chainr);
            return chainr;
        } else {
            return chainrCache.computeIfAbsent(joltSpecName, name -> createChainr(joltSpecName));
        }
    }

    private Chainr createChainr(String joltSpecName) {
        log.debug("Generating Jolt Chainr from spec: {}", joltSpecName);
        return Chainr.fromSpec(JsonUtils.classpathToList(getSpecPrefix() + joltSpecName + getSpecSuffix()));
    }

}
