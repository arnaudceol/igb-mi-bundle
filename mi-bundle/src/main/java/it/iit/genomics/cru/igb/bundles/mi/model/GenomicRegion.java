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

import java.util.Objects;

/**
 *
 * @author aceol
 */
public class GenomicRegion {
    
    private final int start;
    
    private final int end;
    
    private final String sequenceId;

    public GenomicRegion(int start, int end, String sequenceId) {
        this.start = start;
        this.end = end;
        this.sequenceId = sequenceId;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + this.start;
        hash = 61 * hash + this.end;
        hash = 61 * hash + Objects.hashCode(this.sequenceId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GenomicRegion other = (GenomicRegion) obj;
        if (this.start != other.start) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        if (!Objects.equals(this.sequenceId, other.sequenceId)) {
            return false;
        }
        return true;
    }
    
}
