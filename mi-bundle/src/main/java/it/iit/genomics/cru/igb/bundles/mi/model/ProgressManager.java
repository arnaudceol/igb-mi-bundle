/* 
 * Copyright 2015 Fondazione Istituto Italiano di Tecnologia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.iit.genomics.cru.igb.bundles.mi.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressManager {

	private static final Logger logger = LoggerFactory.getLogger(ProgressManager.class);
	
    private final int numberOfSteps;

    private int numMinorSteps;

    private int progress = 0;
    private int currentStep = 0;

    private int currentMajorStep = 0;

    private int nextMax = 0;

    public ProgressManager(int numberOfSteps) {
        super();
        this.numberOfSteps = numberOfSteps;
    }

    public void nextMajorStep(int numMinorSteps) {
        currentMajorStep++;
        this.numMinorSteps = numMinorSteps;
        currentStep = 0;
        nextMax = nextMax + (100 / numberOfSteps) * numMinorSteps;
    }

    public void nextStep() {
        currentStep++;
        if (Math.floor((currentMajorStep - 1) * 100 / numberOfSteps
                + (float) currentStep * 100 / (numberOfSteps * numMinorSteps))
                > progress) {
            progress++;
        }
    }

    public int getProgress() {
        if (progress > nextMax) {
            logger.error("progress  > " + nextMax + ": " + progress);
            return nextMax;
        }

        if (progress > 100) {
            logger.error("progress  > 100 (strange): " + progress);
            return 100;
        }
        return progress;
    }

}
