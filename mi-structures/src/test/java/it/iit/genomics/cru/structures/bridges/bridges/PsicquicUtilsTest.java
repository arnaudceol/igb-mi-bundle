/**
 * *****************************************************************************
 * Copyright 2014 Fondazione Istituto Italiano di Tecnologia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *****************************************************************************
 */
package it.iit.genomics.cru.structures.bridges.bridges;

import it.iit.genomics.cru.structures.bridges.psicquic.PsicquicService;
import it.iit.genomics.cru.structures.bridges.psicquic.PsicquicUtils;

import java.io.IOException;
import java.util.Collection;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Arnaud Ceol
 *
 */
public class PsicquicUtilsTest {

    static final Logger logger = LoggerFactory.getLogger(PsicquicUtilsTest.class);

    
    public void test() throws Exception {
        PsicquicUtils.getInstance().getActiveServerFromRegistry();

        PsicquicUtils psiquic = PsicquicUtils.getInstance();

        Collection<PsicquicService> activeServices = psiquic
                .getActiveServerFromRegistry();

        for (PsicquicService service : activeServices) {

            if (service.isActive()) {
                try {
                    if (service.getClient().countByQuery(
                            "idA:uniprotkb AND idB:uniprotkb") > 0) {
                        psiquic.addUrl(service.getName(), service.getRestUrl());
                    } else {

                    }
                } catch (IOException ioe) {
                    logger.error(
                            "Cannot access psicquic service "
                            + service.getName(), ioe);
                }
            } else {
                logger.info( "Psicquic service {0} is not active.", service.getName());
            }
        }
        Assert.assertTrue(PsicquicUtils.getInstance().getProviderNames().length > 0);

    }

}
