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
package it.iit.genomics.cru.structures.model.position;

import java.util.Objects;

/**
 *
 * @author aceol
 */
public class PdbPosition {
        
    
    /**
     * pos:chain
     */
    String position;

    /**
     *
     * @param position
     */
    public PdbPosition(String position) {
        this.position = position;
    }

    /**
     *
     * @return
     */
    public String getPosition() {
        return position;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final PdbPosition other = (PdbPosition) obj;
        if (!Objects.equals(this.position, other.position)) {
            return false;
        }
        return true;
    }
    
   
}
