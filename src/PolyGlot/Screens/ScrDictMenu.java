/*
 * Copyright (c) 2015-2017, draque
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 *  See LICENSE.TXT included with this code to read the full license agreement.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package PolyGlot.Screens;

import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PFrame;
import PolyGlot.DictCore;
import PolyGlot.ExcelExport;
import PolyGlot.IOHandler;
import PolyGlot.ManagersCollections.OptionsManager;
import PolyGlot.Nodes.ConWord;
import PolyGlot.PGTUtil;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;

/**
 *
 * @author draque
 */
public class ScrDictMenu extends PFrame implements ApplicationListener {
// implementation of ApplicationListener is part of macify

    private String curFileName = "";
    private final Map<String, PFrame> children = new HashMap<>();
    private boolean cleanSave = true;
    private boolean holdFront = false;
    private final List<String> lastFiles;

    /**
     * Creates new form ScrDictMenu
     *
     * @param overridePath Path PolyGlot should treat as home directory (blank
     * if default)
     */
    public ScrDictMenu(String overridePath) {
        initComponents();
        newFile(true);
        setOverrideProgramPath(overridePath);
        lastFiles = core.getOptionsManager().getLastFiles();
        populateRecentOpened();
        checkJavaVersion();
        openLastWindows();

        // activates macify for menu integration...
        if (System.getProperty("os.name").startsWith("Mac")) {
            activateMacify();
        }
    }

    @Override
    public boolean thisOrChildrenFocused() {
        boolean ret = this.isFocusOwner() || holdFront;
        for (PFrame child : children.values()) {
            ret = ret || child.thisOrChildrenFocused();
        }

        return ret;
    }

    @Override
    public void dispose() {
        // only exit if save/cancel test is passed
        if (!saveOrCancelTest()) {
            return;
        }

        super.dispose();

        try {
            saveWindowsOpen();
            core.getOptionsManager().setScreenPosition(getClass().getName(),
                    getLocation());
            core.getOptionsManager().setLastFiles(lastFiles);
            core.getOptionsManager().saveIni();
        } catch (IOException e) {
            localError("Ini Save Error", "Unable to save PolyGlot.ini:\n"
                    + e.getLocalizedMessage());
        }
        System.exit(0);
    }

    /**
     * Records open windows in options manager
     */
    private void saveWindowsOpen() {
        OptionsManager options = core.getOptionsManager();
        for (PFrame child : children.values()) {
            if (!child.isDisposed() && child.isVisible()) {
                options.addScreenUp(child.getClass().getName());
                child.dispose();
            }
        }
    }

    /**
     * Opens windows left open when PolyGlot last run, then clears list
     */
    private void openLastWindows() {
        List<String> lastScreensUp = core.getOptionsManager().getLastScreensUp();
        for (String leftOpen : lastScreensUp) {
            // switch has to be on constants...
            if (leftOpen.equals(PGTUtil.scrNameGrammar)) {
                btnGrammar.setSelected(true);
                grammarHit();
            } else if (leftOpen.equals(PGTUtil.scrNameLexicon)) {
                btnLexicon.setSelected(true);
                lexHit();
            } else if (leftOpen.equals(PGTUtil.scrNameLogo)) {
                btnLogos.setSelected(true);
                logoHit();
            } else if (leftOpen.equals(PGTUtil.scrNameFam)) {
                btnFam.setSelected(true);
                famHit();
            } else if (leftOpen.equals(PGTUtil.scrIPARefChart)) {
                IPAHit();
            } else if (leftOpen.equals(PGTUtil.scrIPARefChart)) {
                IPAHit();
            } else if (leftOpen.equals(PGTUtil.scrIPARefChart)) {
                IPAHit();
            } else if (leftOpen.equals(PGTUtil.scrQuizGenDialog)) {
                quizHit();
            } else {
                InfoBox.error("Unrecognized Window",
                        "Unrecognized window in last session: " + leftOpen, this);
            }

        }
        lastScreensUp.clear();
    }

    @Override
    public void setupAccelerators() {
        String OS = System.getProperty("os.name");
        if (OS.startsWith("Mac")) {
            mnuSaveLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuNewLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuOpenLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
            mnuPublish.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.META_DOWN_MASK));
        } else {
            // I'm pretty sure all other OSes just use CTRL+ to do stuff
            mnuSaveLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuNewLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuOpenLocal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
            mnuPublish.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_DOWN_MASK));
        }
    }

    // MACIFY RELATED CODE ->    
    private void activateMacify() {
        Application application = new DefaultApplication();
        application.addApplicationListener(this);
        application.addApplicationListener(this);
        application.addPreferencesMenuItem();
        application.setEnabledPreferencesMenu(true);
    }

    @Override
    public void handleAbout(ApplicationEvent event) {
        viewAbout();
        event.setHandled(true);
    }

    @Override
    public void handleOpenApplication(ApplicationEvent event) {
        // Ok, we know our application started
        // Not much to do about that..
    }

    @Override
    public void handleOpenFile(ApplicationEvent event) {
        //openFileInEditor(new File(event.getFilename()));
    }

    @Override
    public void handlePreferences(ApplicationEvent event) {
        //preferencesAction.actionPerformed(null);
    }

    @Override
    public void handlePrintFile(ApplicationEvent event) {
        localInfo("Printing", "PolyGlot does not currently support printing.");
    }

    @Override
    public void handleQuit(ApplicationEvent event) {
        dispose();
    }

    @Override
    public void handleReOpenApplication(ApplicationEvent event) {
        setVisible(true);
    }
    // <- MACIFY RELATED CODE

    /**
     * Populates recently opened files menu
     */
    private void populateRecentOpened() {
        mnuRecents.removeAll();

        for (int i = lastFiles.size() - 1; i >= 0; i--) {
            final String curFile = lastFiles.get(i);
            Path p = Paths.get(curFile);
            String fileName = p.getFileName().toString();
            JMenuItem lastFile = new JMenuItem();
            lastFile.setText(fileName);
            lastFile.setToolTipText(curFile);
            lastFile.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    // only open if save/cancel test is passed
                    if (!saveOrCancelTest()) {
                        return;
                    }

                    setFile(curFile);
                    pushRecentFile(curFile);
                    populateRecentOpened();
                }
            });
            mnuRecents.add(lastFile);
        }
    }

    /**
     * Pushes a recently opened file (if appropriate) into the recent files list
     *
     * @param file full path of file
     */
    private void pushRecentFile(String file) {
        if (!lastFiles.isEmpty()
                && lastFiles.contains(file)) {
            lastFiles.remove(file);
            lastFiles.add(file);
            return;
        }

        while (lastFiles.size() > PGTUtil.optionsNumLastFiles) {
            lastFiles.remove(0);
        }

        lastFiles.add(file);
    }

    /**
     * Used by saving worker to communicate whether files saved were successful
     *
     * @param _cleanSave whether the save was a success
     */
    public void setCleanSave(boolean _cleanSave) {
        cleanSave = _cleanSave;
    }

    /**
     * Creates totally new file
     *
     * @param performTest whether the UI ask for confirmation
     */
    final public void newFile(boolean performTest) {
        if (performTest && !saveOrCancelTest()) {
            return;
        }

        //core = new DictCore();
        core.setRootWindow(this);
        updateAllValues(core);
        curFileName = "";
    }

    /**
     * opens dictionary file
     */
    public void open() {
        // only open if save/cancel test is passed
        if (!saveOrCancelTest()) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open Dictionary");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Dictionaries", "pgd", "xml");
        chooser.setFileFilter(filter);
        String fileName;
        chooser.setCurrentDirectory(new File("."));

        holdFront = true;
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        holdFront = false;

        //core = new DictCore();
        core.setRootWindow(this);
        setFile(fileName);
        pushRecentFile(fileName);
        populateRecentOpened();
    }

    /**
     * Gives user option to save file, returns continue/don't continue
     *
     * @return true to signal continue, false to signal stop
     */
    private boolean saveOrCancelTest() {
        // if there's a current dictionary loaded, prompt user to save before creating new
        if (core != null
                && !core.getWordCollection().getWordNodes().isEmpty()) {
            Integer saveFirst = localYesNoCancel("Save First?",
                    "Save current dictionary before performing action?");

            if (saveFirst == JOptionPane.YES_OPTION) {
                boolean saved = saveFile();

                // if the file didn't save (usually due to a last minute cancel) don't continue.
                if (!saved) {
                    return false;
                }
            } else if (saveFirst == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }

        return true;
    }

    /**
     * save file, open save as dialog if no file name already
     *
     * @return true if file saved, false otherwise
     */
    public boolean saveFile() {
        if (curFileName.equals("")) {
            saveFileAs();
        }

        // if it still is blank, the user has hit cancel on the save as dialog
        if (curFileName.equals("")) {
            return false;
        }

        pushRecentFile(curFileName);
        populateRecentOpened();
        return doWrite(curFileName);
    }

    /**
     * sends the write command to the core in a new thread
     *
     * @param _fileName path to write to
     * @return returns success
     */
    private boolean doWrite(final String _fileName) {
        final ScrDictMenu parent = this;
        //final CountDownLatch latch = new CountDownLatch(1);
        boolean ret;

        cleanSave = false;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            core.writeFile(_fileName);
            cleanSave = true;
        } catch (IOException | ParserConfigurationException |
                TransformerException e) {
            parent.setCleanSave(false);
            localError("Save Error", "Unable to save to file: "
                    + curFileName + "\n\n" + e.getMessage());
        }

        setCursor(Cursor.getDefaultCursor());

        if (cleanSave) {
            localInfo("Success", "Dictionary saved to: "
                    + curFileName + ".");
            ret = true;
        } else {
            ret = false;
        }
        cleanSave = true;
        return ret;
    }

    /**
     * saves file as particular filename
     *
     * @return true if file saved, false otherwise
     */
    private boolean saveFileAs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Dictionary");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PolyGlot Dictionaries", "pgd", "xml");
        chooser.setFileFilter(filter);
        chooser.setApproveButtonText("Save");
        chooser.setCurrentDirectory(new File("."));

        String fileName;

        holdFront = true;
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return false;
        }
        holdFront = false;

        // if user has not provided an extension, add one
        if (!fileName.contains(".pgd")) {
            fileName += ".pgd";
        }

        File f = new File(fileName);

        if (f.exists()) {
            Integer overWrite = localYesNoCancel("Overwrite Dialog",
                    "Overwrite existing file? " + fileName);

            if (overWrite == JOptionPane.NO_OPTION) {
                return saveFileAs();
            } else if (overWrite == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }

        curFileName = fileName;
        return true;
    }

    @Override
    public void updateAllValues(DictCore _core) {
        core = _core;
        String title = "PolyGlot " + core.getVersion();
        String langName = core.getPropertiesManager().getLangName().trim();
        if (!langName.isEmpty()) {
            title += (" : " + langName);
        }

        for (PFrame child : children.values()) {
            if (!child.isDisposed()) {
                child.updateAllValues(_core);
            }
        }

        this.setTitle(title);
    }

    @Override
    public void addBindingToComponent(JComponent c) {
        // no bindings necessary for this window
    }

    /**
     * Checks to make certain Java is a high enough version. Informs user and
     * quits otherwise.
     */
    private void checkJavaVersion() {
        String javaVersion = System.getProperty("java.version");

        if (javaVersion.startsWith("1.0")
                || javaVersion.startsWith("1.1")
                || javaVersion.startsWith("1.2")
                || javaVersion.startsWith("1.3")
                || javaVersion.startsWith("1.4")
                || javaVersion.startsWith("1.5")
                || javaVersion.startsWith("1.6")) {
            localError("Please Upgrade Java", "Java " + javaVersion
                    + " must be upgraded to run PolyGlot. Version 1.7 or higher is required.\n\n"
                    + "Please upgrade at https://java.com/en/download/.");
            System.exit(0);
        }
    }

    private void setFile(String fileName) {
        // some wrappers communicate empty files like this
        if (fileName.equals(PGTUtil.emptyFile)
                || fileName.isEmpty()) {
            return;
        }

        //core = new DictCore();
        core.setRootWindow(this);

        try {
            core.readFile(fileName);
            curFileName = fileName;
        } catch (IOException e) {
            //core = new DictCore(); // don't allow partial loads
            localError("File Read Error", "Could not read file: " + fileName
                    + "\n\n " + e.getMessage());
        } catch (IllegalStateException e) {
            InfoBox.warning("File Read Problems", "Problems reading file:\n"
                    + e.getLocalizedMessage(), this);
        }

        updateAllValues(core);
    }

    /**
     * checks web for updates to PolyGlot
     *
     * @param verbose Set this to have messages post to user.
     */
    private void checkForUpdates(final boolean verbose) {
        final Window parent = this;

        Thread check = new Thread() {
            @Override
            public void run() {
                try {
                    ScrUpdateAlert.run(verbose, core);
                } catch (Exception e) {
                    if (verbose) {
                        PolyGlot.CustomControls.InfoBox.error("Update Problem",
                                "Unable to check for update:\n"
                                + e.getLocalizedMessage(), parent);
                    }
                }
            }
        };

        check.start();
    }

    private void bindButtonToWindow(Window w, final JToggleButton b) {
        w.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        b.setSelected(false);
                    }
                };
                SwingUtilities.invokeLater(runnable);
            }
        });
    }

    /**
     * Provided for cases where the java is run from an odd source folder (such
     * as under an app file in OSX)
     *
     * @param override directory for base PolyGlot directory
     */
    private void setOverrideProgramPath(String override) {
        core.getPropertiesManager().setOverrideProgramPath(override);
        try {
            core.getOptionsManager().loadIni();
        } catch (Exception e) {
            localError("Options Load Error", "Unable to load or create options file:\n"
                    + e.getLocalizedMessage());
        }
    }

    /**
     * Retrieves currently selected word (if any) from ScrLexicon
     *
     * @return current word selected in scrLexicon, null otherwise (or if
     * lexicon is not visible)
     */
    public ConWord getCurrentWord() {
        ConWord ret = null;

        if (children.containsKey(ScrLexicon.class.getName())) {
            ScrLexicon scrLexicon = (ScrLexicon) children.get(ScrLexicon.class.getName());

            if (scrLexicon.isVisible() && !scrLexicon.isDisposed()) {
                ret = scrLexicon.getCurrentWord();
            }
        }

        return ret;
    }

    /**
     * Sets selection on lexicon by word id
     *
     * @param id
     */
    public void selectWordById(int id) {
        if (children.containsKey(ScrLexicon.class.getName())) {
            ScrLexicon scrLexicon = (ScrLexicon) children.get(ScrLexicon.class.getName());

            if (scrLexicon.isVisible() && !scrLexicon.isDisposed()) {
                scrLexicon.selectWordById(id);
            }
        }
    }

    @Override
    protected void setupKeyStrokes() {
        addBindingsToPanelComponents(this.getRootPane());
        super.setupKeyStrokes();
    }

    private void viewAbout() {
        ScrAbout.run(core);
    }

    private void lexHit() {
        if (btnLexicon.isSelected()) {
            try {
                if (children.containsKey(ScrLexicon.class.getName())) {
                    ScrLexicon scrLexicon = (ScrLexicon) children.get(ScrLexicon.class.getName());

                    if (scrLexicon.isDisposed()) {
                        //scrLexicon = ScrLexicon.run(core);
                        //bindButtonToWindow(scrLexicon, btnLexicon);
                        //children.replace(ScrLexicon.class.getName(), scrLexicon);
                    }

                    scrLexicon.setVisible(true);
                } else {
                    //ScrLexicon scrLexicon = ScrLexicon.run(core);
                    //bindButtonToWindow(scrLexicon, btnLexicon);
                    //children.put(ScrLexicon.class.getName(), scrLexicon);
                    //scrLexicon.setVisible(true);
                }
            } catch (Exception e) {
                localError("Open Window Error", "Error Opening Lexicon: "
                        + e.getLocalizedMessage());
            }
        } else {
            try {
                children.get(ScrLexicon.class.getName()).setVisible(false);
            } catch (Exception e) {
                localError("Close Window Error", "Error Closing Lexicon: "
                        + e.getLocalizedMessage());
            }
        }
    }

    public void quickEntryHit() {
        ScrQuickWordEntry s = ((ScrLexicon) children.get(ScrLexicon.class.getName())).openQuickEntry();
        bindButtonToWindow(s, btnQuickEntry);
        s.setVisible(true);
    }

    public void grammarHit() {
        if (btnGrammar.isSelected()) {
            try {
                ScrGrammarGuide guide = ScrGrammarGuide.run(core);
                bindButtonToWindow(guide, btnGrammar);

                if (!children.containsKey(ScrGrammarGuide.class.getName())) {
                    children.put(ScrGrammarGuide.class.getName(), guide);
                } else if (children.get(ScrGrammarGuide.class.getName()).isDisposed()) {
                    children.replace(ScrGrammarGuide.class.getName(), guide);
                }
                guide.setVisible(true);
            } catch (Exception e) {
                localError("Open Window Error", "Error Opening Grammar Guide: "
                        + e.getLocalizedMessage());
            }
        } else {
            try {
                children.get(ScrGrammarGuide.class.getName()).setVisible(false);
            } catch (Exception e) {
                localError("Close Window Error", "Error Closing Grammar Guide: "
                        + e.getLocalizedMessage());
            }
        }
    }

    private void logoHit() {
        if (btnLogos.isSelected()) {
            try {
                PFrame logos;
                if (!children.containsKey(ScrLogoDetails.class.getName())) {
                    logos = ScrLogoDetails.run(core);
                    children.put(ScrLogoDetails.class.getName(), logos);
                    bindButtonToWindow(logos, btnLogos);
                } else {
                    logos = children.get(ScrLogoDetails.class.getName());
                    if (logos.isDisposed()) {
                        logos = ScrLogoDetails.run(core);
                        children.replace(ScrLogoDetails.class.getName(), logos);
                        bindButtonToWindow(logos, btnLogos);
                    }
                }

                logos.setVisible(true);
            } catch (Exception e) {
                localError("Open Window Error", "Error Opening Logograph Guide: "
                        + e.getLocalizedMessage());
            }
        } else {
            try {
                children.get(ScrLogoDetails.class.getName()).setVisible(false);
            } catch (Exception e) {
                localError("Close Window Error", "Error Closing Grammar Guide: "
                        + e.getLocalizedMessage());
            }
        }
    }

    private void IPAHit() {
        PFrame ipa;
        String className = ScrIPARefChart.class.getName();
        if (children.containsKey(className)) {
            ipa = children.get(className);

            if (ipa.isDisposed()) {
                ipa = new ScrIPARefChart(core);
                children.replace(className, ipa);
            }
        } else {
            ipa = new ScrIPARefChart(core);
            children.put(className, ipa);
        }

        ipa.setVisible(true);
        ipa.toFront();
    }

    private void famHit() {
        String className = ScrFamilies.class.getName();
        
        if (btnFam.isSelected()) {
            PFrame fam;
            if (children.containsKey(className)) {
                fam = children.get(className);

                if (fam.isDisposed()) {
                    //fam = new ScrFamilies(core, this);
                    //children.replace(className, fam);
                    //bindButtonToWindow(fam, btnFam);
                }
            } else {
                //fam = new ScrFamilies(core, this);
                //children.put(className, fam);
                //bindButtonToWindow(fam, btnFam);
            }

            //fam.setVisible(true);
            //fam.toFront();
        } else {
            try {
                children.get(className).setVisible(false);
            } catch (Exception e) {
                localInfo("Close Window Error", "Error Closing Grammar Guide: "
                        + e.getLocalizedMessage());
            }
        }
    }
    
    private void quizHit() {
        String className = ScrQuizGenDialog.class.getName();
        if (children.containsKey(className) && !children.get(className).isDisposed()) {
            PFrame p = children.get(className);
            p.setVisible(true);
            p.toFront();
        } else {
            PFrame p = ScrQuizGenDialog.run(core);
            if (children.containsKey(className)) {
                children.replace(className, p);
            } else {
                children.put(className, p);
            }
        }
    }

    /**
     * Export dictionary to excel file
     */
    private void exportToExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Dictionary to Excel");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xls");
        chooser.setFileFilter(filter);
        chooser.setApproveButtonText("Save");
        chooser.setCurrentDirectory(new File("."));

        String fileName;

        holdFront = true;
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        holdFront = false;

        if (!fileName.contains(".xls")) {
            fileName += ".xls";
        }

        try {
            ExcelExport.exportExcelDict(fileName, core);
            localInfo("Export Status", "Dictionary exported to " + fileName + ".");
        } catch (Exception e) {
            localError("Export Problem", e.getLocalizedMessage());
        }
    }

    /**
     * Prompts user for a location and exports font within PGD to given path
     */
    public void exportFont() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Font");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Font Files", "ttf");
        chooser.setFileFilter(filter);
        String fileName;
        chooser.setCurrentDirectory(new File("."));
        chooser.setApproveButtonText("Save");

        holdFront = true;
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getAbsolutePath();
        } else {
            return;
        }
        holdFront = false;

        try {
            IOHandler.exportFont(fileName, curFileName);
        } catch (IOException e) {
            localError("Export Error", "Unable to export font: " + e.getMessage());
        }
    }

    private void openHelp() {
        URI uri;
        try {
            String OS = System.getProperty("os.name");
            String overridePath = core.getPropertiesManager().getOverrideProgramPath();
            if (OS.startsWith("Windows")) {
                String relLocation = new File(".").getAbsolutePath();
                relLocation = relLocation.substring(0, relLocation.length() - 1);
                relLocation = "file:///" + relLocation + "readme.html";
                relLocation = relLocation.replaceAll(" ", "%20");
                relLocation = relLocation.replaceAll("\\\\", "/");
                uri = new URI(relLocation);
                uri.normalize();
                java.awt.Desktop.getDesktop().browse(uri);
            } else if (OS.startsWith("Mac")) {
                String relLocation;
                if (overridePath.equals("")) {
                    relLocation = new File(".").getAbsolutePath();
                    relLocation = relLocation.substring(0, relLocation.length() - 1);
                    relLocation = "file://" + relLocation + "readme.html";
                } else {
                    relLocation = core.getPropertiesManager().getOverrideProgramPath();
                    relLocation = "file://" + relLocation + "/Contents/Resources/readme.html";
                }
                relLocation = relLocation.replaceAll(" ", "%20");
                uri = new URI(relLocation);
                uri.normalize();
                java.awt.Desktop.getDesktop().browse(uri);
            } else {
                // TODO: Implement this for Linux
                localError("Help", "This is not yet implemented for OS: " + OS
                        + ". Please open readme.html in the application directory");
            }
        } catch (URISyntaxException | IOException e) {
            localError("Missing File", "Unable to open readme.html.");
        }
    }

    /**
     * Wrapped locally to ensure front position of menu not disturbed
     *
     * @param infoHead title text
     * @param infoText message text
     */
    private void localInfo(String infoHead, String infoText) {
        holdFront = true;
        PolyGlot.CustomControls.InfoBox.info(infoHead, infoText, this);
        holdFront = false;
    }

    /**
     * Wrapped locally to ensure front position of menu not disturbed
     *
     * @param infoHead title text
     * @param infoText message text
     */
    private void localError(String infoHead, String infoText) {
        holdFront = true;
        PolyGlot.CustomControls.InfoBox.error(infoHead, infoText, this);
        holdFront = false;
    }

    /**
     * Wrapped locally to ensure front position of menu not disturbed
     *
     * @param infoHead title text
     * @param infoText message text
     */
    private int localYesNoCancel(String infoHead, String infoText) {
        holdFront = true;
        int ret = PolyGlot.CustomControls.InfoBox.yesNoCancel(infoHead, infoText, this);
        holdFront = false;
        return ret;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnLexicon = new javax.swing.JToggleButton();
        btnGrammar = new javax.swing.JToggleButton();
        btnTypes = new javax.swing.JToggleButton();
        btnLangProp = new javax.swing.JToggleButton();
        btnClasses = new javax.swing.JToggleButton();
        btnQuickEntry = new javax.swing.JToggleButton();
        btnLogos = new javax.swing.JToggleButton();
        btnFam = new javax.swing.JToggleButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        mnuNewLocal = new javax.swing.JMenuItem();
        mnuSaveLocal = new javax.swing.JMenuItem();
        mnuSaveAs = new javax.swing.JMenuItem();
        mnuOpenLocal = new javax.swing.JMenuItem();
        mnuRecents = new javax.swing.JMenu();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        mnuPublish = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mnuExit = new javax.swing.JMenuItem();
        mnuTools = new javax.swing.JMenu();
        mnuImportFile = new javax.swing.JMenuItem();
        mnuExportToExcel = new javax.swing.JMenuItem();
        mnuExportFont = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuLangStats = new javax.swing.JMenuItem();
        mnuQuiz = new javax.swing.JMenuItem();
        mnuTransWindow = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        mnuIPAChart = new javax.swing.JMenuItem();
        mnuHelp = new javax.swing.JMenu();
        mnuAbout = new javax.swing.JMenuItem();
        mnuChkUpdate = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem8 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        btnLexicon.setText("Lexicon");
        btnLexicon.setToolTipText("Store all of your language's words here.");
        btnLexicon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLexiconActionPerformed(evt);
            }
        });

        btnGrammar.setText("Grammar");
        btnGrammar.setToolTipText("A complex chapter book of grammar can be written here. Supports spoken/replayable messages.");
        btnGrammar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGrammarActionPerformed(evt);
            }
        });

        btnTypes.setText("Parts of Speech");
        btnTypes.setToolTipText("Define parts of speech and their rules here.");
        btnTypes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTypesActionPerformed(evt);
            }
        });

        btnLangProp.setText("Lang Properties");
        btnLangProp.setToolTipText("General properties of your language such as alphabetical order, orthography, and language name are stored here.");
        btnLangProp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLangPropActionPerformed(evt);
            }
        });

        btnClasses.setText("Lexical Classes");
        btnClasses.setToolTipText("Class properties parts of speech may have, such as gender");
        btnClasses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClassesActionPerformed(evt);
            }
        });

        btnQuickEntry.setText("Quickentry");
        btnQuickEntry.setToolTipText("This allows you to very quickly add in new words, hitting Enter when done.");
        btnQuickEntry.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickEntryActionPerformed(evt);
            }
        });

        btnLogos.setText("Logographs");
        btnLogos.setToolTipText("If your language uses a logographic writing system, you can store images of the logographs here.");
        btnLogos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogosActionPerformed(evt);
            }
        });

        btnFam.setText("Lexical Families");
        btnFam.setToolTipText("This is a place to store families of related words in a nested, searchable tre structure.");
        btnFam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFamActionPerformed(evt);
            }
        });

        jMenu1.setText("File");

        mnuNewLocal.setText("New");
        mnuNewLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuNewLocalActionPerformed(evt);
            }
        });
        jMenu1.add(mnuNewLocal);

        mnuSaveLocal.setText("Save");
        mnuSaveLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveLocalActionPerformed(evt);
            }
        });
        jMenu1.add(mnuSaveLocal);

        mnuSaveAs.setText("Save As");
        mnuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSaveAsActionPerformed(evt);
            }
        });
        jMenu1.add(mnuSaveAs);

        mnuOpenLocal.setText("Open");
        mnuOpenLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuOpenLocalActionPerformed(evt);
            }
        });
        jMenu1.add(mnuOpenLocal);

        mnuRecents.setText("Recent");
        jMenu1.add(mnuRecents);
        jMenu1.add(jSeparator5);

        mnuPublish.setText("Publish to PDF");
        mnuPublish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuPublishActionPerformed(evt);
            }
        });
        jMenu1.add(mnuPublish);
        jMenu1.add(jSeparator2);

        mnuExit.setText("Exit");
        mnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExitActionPerformed(evt);
            }
        });
        jMenu1.add(mnuExit);

        jMenuBar1.add(jMenu1);

        mnuTools.setText("Tools");

        mnuImportFile.setText("Import from File");
        mnuImportFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuImportFileActionPerformed(evt);
            }
        });
        mnuTools.add(mnuImportFile);

        mnuExportToExcel.setText("Export to Excel");
        mnuExportToExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportToExcelActionPerformed(evt);
            }
        });
        mnuTools.add(mnuExportToExcel);

        mnuExportFont.setText("Export Font");
        mnuExportFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuExportFontActionPerformed(evt);
            }
        });
        mnuTools.add(mnuExportFont);
        mnuTools.add(jSeparator1);

        mnuLangStats.setText("Language Statistics");
        mnuLangStats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuLangStatsActionPerformed(evt);
            }
        });
        mnuTools.add(mnuLangStats);

        mnuQuiz.setText("Quiz Generator");
        mnuQuiz.setToolTipText("Generate customized flashcard quizzes to help increase fluency.");
        mnuQuiz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuQuizActionPerformed(evt);
            }
        });
        mnuTools.add(mnuQuiz);

        mnuTransWindow.setText("Translation Window");
        mnuTransWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuTransWindowActionPerformed(evt);
            }
        });
        mnuTools.add(mnuTransWindow);
        mnuTools.add(jSeparator4);

        mnuIPAChart.setText("Interactive IPA Chart");
        mnuIPAChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuIPAChartActionPerformed(evt);
            }
        });
        mnuTools.add(mnuIPAChart);

        jMenuBar1.add(mnuTools);

        mnuHelp.setText("Help");

        mnuAbout.setText("Help");
        mnuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAboutActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuAbout);

        mnuChkUpdate.setText("About");
        mnuChkUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuChkUpdateActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuChkUpdate);
        mnuHelp.add(jSeparator3);

        jMenuItem8.setText("Check for Updates");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        mnuHelp.add(jMenuItem8);

        jMenuBar1.add(mnuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnLexicon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnQuickEntry, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnTypes, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
            .addComponent(btnGrammar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnLangProp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnLogos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnFam, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnClasses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(btnLexicon)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnQuickEntry)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnTypes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClasses)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnGrammar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLangProp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLogos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnFam)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLexiconActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLexiconActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lexHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnLexiconActionPerformed

    private void mnuSaveLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveLocalActionPerformed
        saveFile();
    }//GEN-LAST:event_mnuSaveLocalActionPerformed

    private void btnQuickEntryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickEntryActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (!btnLexicon.isSelected()) {
            btnLexicon.setSelected(true);
            lexHit();
        }
        quickEntryHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnQuickEntryActionPerformed

    private void btnGrammarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGrammarActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        grammarHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnGrammarActionPerformed

    private void btnTypesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTypesActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrTypes s = ScrTypes.run(core);
        bindButtonToWindow(s, btnTypes);
        s.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnTypesActionPerformed

    private void btnClassesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClassesActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrWordClasses s = new ScrWordClasses(core);
        bindButtonToWindow(s, btnClasses);
        s.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnClassesActionPerformed

    private void btnLangPropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLangPropActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScrLangProps s = ScrLangProps.run(core);
        bindButtonToWindow(s, btnLangProp);
        s.setVisible(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnLangPropActionPerformed

    private void btnLogosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogosActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        logoHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnLogosActionPerformed

    private void btnFamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFamActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        famHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_btnFamActionPerformed

    private void mnuNewLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuNewLocalActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        newFile(true);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuNewLocalActionPerformed

    private void mnuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSaveAsActionPerformed
        if (saveFileAs()) {
            saveFile();
        }
    }//GEN-LAST:event_mnuSaveAsActionPerformed

    private void mnuOpenLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuOpenLocalActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        open();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuOpenLocalActionPerformed

    private void mnuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExitActionPerformed
        dispose();
    }//GEN-LAST:event_mnuExitActionPerformed

    private void mnuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAboutActionPerformed
        openHelp();
    }//GEN-LAST:event_mnuAboutActionPerformed

    private void mnuChkUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuChkUpdateActionPerformed
        ScrAbout.run(core);
    }//GEN-LAST:event_mnuChkUpdateActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        checkForUpdates(true);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void mnuPublishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuPublishActionPerformed
        ScrPrintToPDF.run(core);
    }//GEN-LAST:event_mnuPublishActionPerformed

    private void mnuIPAChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuIPAChartActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        IPAHit();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuIPAChartActionPerformed

    private void mnuTransWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuTransWindowActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //ScrTranslationWindow.run(core, this);
        InfoBox.warning("Deprecated", "Does nothing.", this);
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuTransWindowActionPerformed

    private void mnuLangStatsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuLangStatsActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (localYesNoCancel("Continue Operation?", "The statistics report can"
            + " take a long time to complete, depending on the complexity\n"
            + "of your conlang. Continue?") == JOptionPane.YES_OPTION) {
        ScrLangStats.run(core);
        }
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuLangStatsActionPerformed

    private void mnuExportFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExportFontActionPerformed
        exportFont();
    }//GEN-LAST:event_mnuExportFontActionPerformed

    private void mnuExportToExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuExportToExcelActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        exportToExcel();
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuExportToExcelActionPerformed

    private void mnuImportFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuImportFileActionPerformed
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        //ScrExcelImport.run(core, );
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_mnuImportFileActionPerformed

    private void mnuQuizActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuQuizActionPerformed
        quizHit();
    }//GEN-LAST:event_mnuQuizActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void mainOLD(final String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            // This is the only form that should have the traditional logger.
            java.util.logging.Logger.getLogger(ScrDictMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String overridePath = args.length > 1 ? args[1] : "";
                ScrDictMenu s = new ScrDictMenu(overridePath);

                // open file if one is provided via arguments
                if (args.length > 0) {
                    s.setFile(args[0]);
                }

                s.checkForUpdates(false);
                s.setupKeyStrokes();
                s.setVisible(true);

                String problems = "";
                // Test for JavaFX and inform user that it is not present, they cannot run PolyGlot
                // Test for minimum version of Java (8)
                String jVer = System.getProperty("java.version");
                if (jVer.startsWith("1.5") || jVer.startsWith("1.6") || jVer.startsWith("1.7")) {
                    problems += "Unable to start PolyGlot without Java 8.";
                }
                try {
                    this.getClass().getClassLoader().loadClass("javafx.embed.swing.JFXPanel");
                } catch (ClassNotFoundException e) {
                    problems += "\nUnable to load Java FX. Download and install to use PolyGlot.";
                }

                if (!problems.equals("")) {
                    InfoBox.error("Unable to start", problems + "\nPlease upgrade and restart to continue.", s);
                    s.dispose();
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnClasses;
    private javax.swing.JToggleButton btnFam;
    private javax.swing.JToggleButton btnGrammar;
    private javax.swing.JToggleButton btnLangProp;
    private javax.swing.JToggleButton btnLexicon;
    private javax.swing.JToggleButton btnLogos;
    private javax.swing.JToggleButton btnQuickEntry;
    private javax.swing.JToggleButton btnTypes;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JMenuItem mnuAbout;
    private javax.swing.JMenuItem mnuChkUpdate;
    private javax.swing.JMenuItem mnuExit;
    private javax.swing.JMenuItem mnuExportFont;
    private javax.swing.JMenuItem mnuExportToExcel;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenuItem mnuIPAChart;
    private javax.swing.JMenuItem mnuImportFile;
    private javax.swing.JMenuItem mnuLangStats;
    private javax.swing.JMenuItem mnuNewLocal;
    private javax.swing.JMenuItem mnuOpenLocal;
    private javax.swing.JMenuItem mnuPublish;
    private javax.swing.JMenuItem mnuQuiz;
    private javax.swing.JMenu mnuRecents;
    private javax.swing.JMenuItem mnuSaveAs;
    private javax.swing.JMenuItem mnuSaveLocal;
    private javax.swing.JMenu mnuTools;
    private javax.swing.JMenuItem mnuTransWindow;
    // End of variables declaration//GEN-END:variables

    @Override
    public Component getWindow() {
        // this is going away. No need to implement anything new on the window.
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean canClose() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
