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
package it.iit.genomics.cru.igb.bundles.mi.view;

import it.iit.genomics.cru.igb.bundles.mi.view.StructuresPanel.ResiduesType;
import it.iit.genomics.cru.structures.sources.StructureManager.StructureSourceType;

public class StructureItem implements Comparable<StructureItem> {

    private final String name;
    private final StructureSourceType sourceType;
    private final ResiduesType residuesType;

    public ResiduesType getResiduesType() {
        return residuesType;
    }

    public String getName() {
        return name;
    }

    public StructureSourceType getSourceType() {
        return sourceType;
    }
    
    public StructureItem(String name, StructureSourceType sourceType, ResiduesType residuesType) {
        super();
        this.name = name;
        this.sourceType = sourceType;
        this.residuesType = residuesType;
    }

    @Override
    public int compareTo(StructureItem o) {
        // first by type

        if (this.residuesType.compareTo(o.getResiduesType()) != 0) {
            return residuesType.compareTo(o.getResiduesType());
        }

        return name.compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((sourceType == null) ? 0 : sourceType.hashCode());
        result = prime * result
                + ((residuesType == null) ? 0 : residuesType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StructureItem other = (StructureItem) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }

        if (sourceType == null) {
            if (other.sourceType != null) {
                return false;
            }
        } else if (!sourceType.equals(other.sourceType)) {
            return false;
        }
        return residuesType == other.residuesType;
    }

}
