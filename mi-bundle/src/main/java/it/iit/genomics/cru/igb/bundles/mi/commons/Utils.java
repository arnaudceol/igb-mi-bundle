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
package it.iit.genomics.cru.igb.bundles.mi.commons;

import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import it.iit.genomics.cru.structures.model.MIGene;

import java.util.Collection;
import java.util.HashSet;


/**
 *
 * @author Arnaud Ceol
 *
 * Misc utilities
 *
 */
public class Utils {

    public static Collection<String> getGeneIds(Collection<MIGene> syms) {
        HashSet<String> ids = new HashSet<>();

        for (MIGene sym : syms) {
            ids.add(sym.getName());
        }

        return ids;
    }

    public static Collection<String> getSymIds(Collection<SeqSymmetry> syms) {
        HashSet<String> ids = new HashSet<>();

        for (SeqSymmetry sym : syms) {
            ids.add(sym.getID());
        }

        return ids;
    }

    public static String getSymmetrySummary(SeqSymmetry sym) {
        int start = sym.getSpan(0).getStart();
        int end = sym.getSpan(0).getEnd();

        if (start != end) {
            return sym.getSpan(0).getBioSeq().getId() + ":" + sym.getSpan(0).getStart() + "-" + sym.getSpan(0).getEnd()
                    + " (" + sym.getID() + ")";
        } else {
            return sym.getSpan(0).getBioSeq().getId() + ":" + sym.getSpan(0).getStart()
                    + " (" + sym.getID() + ")";
        }
    }

}
