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
package it.iit.genomics.cru.igb.bundles.mi.business;

import it.iit.genomics.cru.structures.model.Range;
import it.iit.genomics.cru.utils.maps.MapOfMap;
import java.util.ArrayList;
import java.util.Collection;


public class RangeMerger {
    private final MapOfMap<String, Range> ranges;
    
    public RangeMerger() {
        ranges = new MapOfMap<>();
    }
    public Collection<String> getSequences() {
        return ranges.keySet();
    }

    public Collection<Range> getRanges(String seq) {
        return ranges.get(seq);
    }

    public void addRange(String seq, Range newRange) {
        if (false == ranges.containsKey(seq)) {
            ranges.add(seq, newRange);
            return;
        }
        ArrayList<Range> updatedRanges = new ArrayList<>();
        for (Range range : ranges.get(seq)) {
            if (range.intersects(newRange)) {
                newRange.setMin(Math.min(range.getMin(), newRange.getMin()));
                newRange.setMax(Math.max(range.getMax(), newRange.getMax()));
            } else {
                updatedRanges.add(range);
            }
        }
        updatedRanges.add(newRange);
        ranges.get(seq).clear();
        ranges.addAll(seq, updatedRanges);
    }
    
    /**
     * Import all range from a second merger
     * @param merger 
     */
    public void merge(RangeMerger merger) {
        for (String seq: merger.getSequences()) {
            for (Range range: merger.getRanges(seq)) {
                this.addRange(seq, new Range(range.getMin(), range.getMax()));
            }
        }
    }
    
}
