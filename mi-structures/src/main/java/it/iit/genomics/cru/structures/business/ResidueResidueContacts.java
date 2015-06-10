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
package it.iit.genomics.cru.structures.business;

import it.iit.genomics.cru.structures.model.Contact;
import it.iit.genomics.cru.structures.model.Contact.ContactType;
import it.iit.genomics.cru.structures.sources.StructureSource;
import it.iit.genomics.cru.structures.sources.USERStructureSource;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.align.gui.jmol.JmolPanel;


/**
 * Get residues in contact
 *
 * @author aceol
 *
 */
public class ResidueResidueContacts {

    private final static String SULFUR_NAME = "S";
    private final static String CYSTEINE_NAME = "C";
    private final static String NITROGEN_NAME = "N"; //StructureTools.nAtomName;
    private final static String OXYGEN_NAME = "O"; //StructureTools.oAtomName;
    private final static String CARBON_NAME = "C";

    private ContactType getContactType(Atom atomA, Atom atomB, String residueA, String residueB) {
        String typeA = atomA.getElement().name();
        String typeB = atomB.getElement().name();

        if (CYSTEINE_NAME.equals(residueA) && CYSTEINE_NAME.equals(residueB) && SULFUR_NAME.equals(typeA) && SULFUR_NAME.equals(typeB)) {
            return ContactType.DISULFITE_BRIDGE;
        }

        if ((OXYGEN_NAME.equals(typeA) && NITROGEN_NAME.equals(typeB))
                || (NITROGEN_NAME.equals(typeA) && OXYGEN_NAME.equals(typeB))) {
            return ContactType.SALT_BRIDGE;
        }

        if (CARBON_NAME.equals(typeA) && CARBON_NAME.equals(typeB)) {
            return ContactType.VAN_DER_WAALS;
        }

        return ContactType.OTHER;
    }

    private boolean isContact(ContactType type, double distance) {

        if (ContactType.DISULFITE_BRIDGE.equals(type) && distance <= 2.56) {
            return true;
        }

        if (ContactType.SALT_BRIDGE.equals(type) && distance <= 5.5) {
            return true;
        }

        if (ContactType.VAN_DER_WAALS.equals(type) && distance <= 5.5) {
            return true;
        }

        return false;
    }

    private final StructureSource structureSource;

    /**
     *
     * @param pdbFileName
     * @return
     * @throws Exception
     */
    public Collection<Contact> getContacts(String pdbFileName) throws Exception {

        Structure structure = structureSource.getStructure(pdbFileName);

        ArrayList<Contact> contacts = new ArrayList<>();

        for (Chain chainA : structure.getChains()) {
            for (Chain chainB : structure.getChains()) {

                // calculate only once
                if (chainA.getChainID().compareTo(chainB.getChainID()) >= 0) {
                    continue;
                }

                for (Group groupA : chainA.getAtomGroups()) {
                    String residueA = ((AminoAcid) groupA).getAminoType() + "";

                    for (Group groupB : chainB.getAtomGroups()) {
                        boolean inContact = false;
                        String residueB = ((AminoAcid) groupB).getAminoType() + "";

                        for (Atom atomA : groupA.getAtoms()) {

                            if (inContact) {
                                break;
                            }

                            for (Atom atomB : groupB.getAtoms()) {

                                ContactType type = getContactType(atomA, atomB, residueA, residueB);
                                if (ContactType.OTHER.equals(type)) {
                                    continue;
                                }

                                double distance = Calc.getDistance(atomA, atomB);

                                if (isContact(type, distance)) {

                                    Contact contact = new Contact(
                                            chainA.getChainID(), chainB.getChainID(), type, distance, groupA.getResidueNumber().toString(),
                                            groupB.getResidueNumber().toString(), residueA, residueB, groupA.getResidueNumber().getSeqNum(), groupB.getResidueNumber().getSeqNum());

                                    contacts.add(contact);
//									Math.round(distance *100 ) /100 
//									System.out.println(chainA.getChainID() + " " + chainB.getChainID() + " " + Math.round(distance *100 ) /100 + " " +type);

                                    inContact = true;
                                    break;
                                }

                            }
                        }

                    }
                }

            }
        }
        return contacts;

    }

    /**
     *
     * @param structureSource
     */
    public ResidueResidueContacts(StructureSource structureSource) {
        this.structureSource = structureSource;
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        String pdbFile = "A0A585-K7N5N2-EXP-4mnq.pdb1-E-0-D-0";

        String i3ddir = "/home/aceol/Workspace/workdir/interfaces/data/i3d_2014_06/";

        StructureSource structureSource = new USERStructureSource(i3ddir);

        ResidueResidueContacts c = new ResidueResidueContacts(structureSource);
        Collection<Contact> contacts = c.getContacts(pdbFile);

//		PDBFileReader pdbr = new PDBFileReader();
//		pdbr.setAutoFetch(true);
//		pdbr.downloadPDB(pdbCode);
        Structure structure = structureSource.getStructure(pdbFile);

        JFrame jmolFrame;
        JmolPanel jmolPanel;

        jmolFrame = new JFrame();

        jmolPanel = new JmolPanel();

        jmolPanel.setPreferredSize(new Dimension(500, 500));
        jmolFrame.add(jmolPanel);

        jmolFrame.pack();
        jmolFrame.setVisible(false);
        jmolPanel.setStructure(structure);

        jmolFrame.setVisible(true);
        jmolFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jmolPanel.getViewer().evalString(
                "select *; spacefill off; wireframe off; backbone 0.4;  ");
//		jmolPanel.getViewer().evalString("select *; cartoon;  ");
        jmolPanel.getViewer().evalString("select *:A; color green;  ");
        jmolPanel.getViewer().evalString("select *:B; color blue;  ");

        for (Contact contact : contacts) {
            System.out.println(contact);
            jmolPanel.getViewer().evalString(
                    "select " + contact.getAtomA() + ":" + contact.getChainA()
                    + "; cartoon off; backbone 0.0; color yellow ");
            jmolPanel.getViewer().evalString(
                    "select " + contact.getAtomB() + ":" + contact.getChainB()
                    + "; cartoon off; backbone 0.0; color orange ");

        }

        System.out.println("num contacts: " + contacts.size());

    }

}
