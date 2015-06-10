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
public class MIExon {

    private final int start;

    private final int end;

    private String sequence;
    
    /**
     * 0-based exclusive
     */
    private int proteinStart;

    /**
     * 0-based exclusive
     */   
    private int proteinEnd;
    
    /**
     * Number of bases at the beginning that encode for 
     * an AA started on previous exon
     * 0,1 or 2
     */
    private int overlappingBases;

    /**
     *
     * @return
     */
    public int getProteinStart() {
        return proteinStart;
    }

    /**
     *
     * @param proteinStart
     */
    public void setProteinStart(int proteinStart) {
        this.proteinStart = proteinStart;
    }

    /**
     *
     * @return
     */
    public int getProteinEnd() {
        return proteinEnd;
    }

    /**
     *
     * @param proteinEnd
     */
    public void setProteinEnd(int proteinEnd) {
        this.proteinEnd = proteinEnd;
    }
    
    /**
     *
     * @return
     */
    public int getNumberOfOverlappingBases() {
        return overlappingBases;
    }

    /**
     *
     * @param frame
     */
    public void setNumberOfOverlappingBases(int frame) {
        this.overlappingBases = frame;
    }
    
    /**
     *
     * @return
     */
    public int getStart() {
        return start;
    }

    /**
     *
     * @return
     */
    public int getEnd() {
        return end;
    }

    /**
     *
     * @return
     */
    public int getMin() {
        return  isForward() ? start : end;
    }

    /**
     *
     * @return
     */
    public int getMax() {
        return  isForward() ? end : start;
    }

    /**
     *
     * @return
     */
    public boolean isForward() {
        return start <= end ;
    }
    
    /**
     *
     * @param start
     * @param end
     */
    public MIExon(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     *
     * @return
     */
    public String getSequence() {
        return sequence;
    }

    /**
     *
     * @param sequence
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + start;
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
        MIExon other = (MIExon) obj;
        if (end != other.end) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        return true;
    }

    /**
     *
     * @return
     */
    public int getLength() {
        return sequence != null ? sequence.length() : getMax() - getMin() +1;
    }
    
}
