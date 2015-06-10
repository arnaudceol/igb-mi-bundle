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
package it.iit.genomics.cru.structures.model;

/**
 *
 * @author Arnaud Ceol
 */
public class Range {
    
    /**
     *
     */
    protected int min;

    /**
     *
     */
    protected int max;

    /**
     *
     * @param start
     * @param end
     */
    public Range(int start, int end) {
        this.min = Math.min(start, end);
        this.max = Math.max(start, end);
    }
    
    /**
     *
     * @param range
     * @return
     */
    public boolean intersects(Range range) { 
        return Math.max(min, range.getMin()) <= Math.min(max, range.getMax());
    }

    /**
     *
     * @param range
     * @return
     */
    public boolean continousTo(Range range) {
        return max +1 == range.getMin() || range.getMax() +1 == max;
    }

    /**
     *
     * @return
     */
    public int getMin() {
        return min;
    }

    /**
     *
     * @param min
     */
    public void setMin(int min) {
        this.min = min;
    }

    /**
     *
     * @return
     */
    public int getMax() {
        return max;
    }

    /**
     *
     * @param max
     */
    public void setMax(int max) {
        this.max = max;
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
        final Range other = (Range) obj;
        if (this.min != other.min) {
            return false;
        }
        if (this.max != other.max) {
            return false;
        }
        return true;
    }
    
    
    @Override
    public String toString() {
        return "Range: min=" + this.min + " - max=" + this.max;
    }
    
}
