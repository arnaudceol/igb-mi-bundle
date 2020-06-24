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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Adapted from https://stackoverflow.com/questions/8581215/jfilechooser-and-checking-for-overwrite/8581229#8581229?newreg=fa70c68b2b9b4ee4b6c329bdbcfa72ef
 * @author arnaudceol
 */
public class CustomFileChooser extends JFileChooser {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private final String extension;
  
  public CustomFileChooser(String directory, String extension, String message) {
    super(directory);
    this.extension = extension;
    addChoosableFileFilter(new FileNameExtensionFilter(
      String.format("%1$s (*.%2$s)", message, extension), extension));
  }

  @Override public File getSelectedFile() {
    File selectedFile = super.getSelectedFile();

    if (selectedFile != null) {
      String name = selectedFile.getName();
      if (false == name.endsWith("." + extension))
        selectedFile = new File(selectedFile.getParentFile(), 
          name + '.' + extension);
    }

    return selectedFile;
  }

  @Override public void approveSelection() {
    if (getDialogType() == SAVE_DIALOG) {
      File selectedFile = getSelectedFile();
      if ((selectedFile != null) && selectedFile.exists()) {
        int response = JOptionPane.showConfirmDialog(this,
          "The file " + selectedFile.getName() + 
          " already exists. Do you want to replace the existing file?",
          "Ovewrite file", JOptionPane.YES_NO_OPTION,
          JOptionPane.WARNING_MESSAGE);
        if (response != JOptionPane.YES_OPTION)
          return;
      }
    }

    super.approveSelection();
  }
}
