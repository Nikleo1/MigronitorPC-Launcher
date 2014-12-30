package de.migronitor.pc.launcher;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import jupar.Downloader;
import jupar.Updater;
import jupar.objects.Modes;
import jupar.objects.Release;
import jupar.parsers.ReleaseXMLParser;
import org.xml.sax.SAXException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Programmieren
 */
public class AutoUpdater {

    private String pfad;
    static String updatePath = "http://pc.migronitor.de/";
    private RandomAccessFile raf;
    private String version;
    private String release;

    public AutoUpdater() {

        String OS = (System.getProperty("os.name")).toUpperCase();
        //if it is some version of Windows
        if (OS.contains("WIN")) {
            //it is simply the location of the "AppData" folder
            pfad = System.getenv("AppData");
        }
        //Otherwise, we assume Linux or Mac
        else {
            //in either case, we would start in the user's home directory
            pfad = System.getProperty("user.home");
            //if we are on a Mac, we are not done, we look for "Application Support"
            if(OS.contains("MAC"))
                pfad += "/Library/Application Support";
        }

        try {
            File f = new File(pfad + "\\.MigronitorPC\\" + "version");
            if (!f.exists()) {
                File p = new File(pfad + "\\.MigronitorPC");
                p.mkdirs();
                raf = new RandomAccessFile(f, "rw");
                raf.seek(0);
                raf.writeDouble(0.0);
                raf.writeInt(0);
                this.version = "0.0";
                this.release = "0";
                this.updatecheck();
            } else {
                raf = new RandomAccessFile(f, "rw");
                raf.seek(0);
                this.version = "" + raf.readDouble();
                this.release = "" + raf.readInt();
                this.updatecheck();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(AutoUpdater.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AutoUpdater.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Runtime.getRuntime().exec("java -jar " + pfad + "\\.MigronitorPC\\MigronitorPC.jar");
            System.out.println("java -jar " + pfad + "\\.MigronitorPC\\MigronitorPC.jar");

        } catch (IOException ex) {
            Logger.getLogger(AutoUpdater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updatecheck() {
        Release cRelease = new Release();
        cRelease.setpkgver(this.version);
        cRelease.setPkgrel(this.release);

        ReleaseXMLParser parser = new ReleaseXMLParser();
        try {
            Release current = parser.parse(updatePath + "latest.xml", Modes.URL);
            if (current.compareTo(cRelease) > 0) {

                /**
                 * Download needed files
                 */
                Downloader dl = new Downloader();
                dl.download(updatePath + "files.xml", pfad + "\\.MigronitorPC" + "\\tmp\\", Modes.URL);
                System.out.println("Neue version");
                Updater update = new Updater();
                update.update("update.xml", pfad + "\\.MigronitorPC" + "\\tmp\\", Modes.FILE, pfad + "\\.MigronitorPC" );
                raf.setLength(0);
                raf.writeDouble(Double.parseDouble(current.getpkgver()));
                raf.writeInt(Integer.parseInt(current.getPkgrel()));

                JOptionPane.showMessageDialog(null, "MigronitorPC wurde erfolgreich auf Version " + current.getpkgver() +  " aktualisiert.");
                /**
                 * Delete tmp directory
                 */
                File tmp = new File(pfad + "\\.MigronitorPC" + "\\tmp\\");
                if (tmp.exists()) {
                    for (File file : tmp.listFiles()) {
                        file.delete();
                    }
                    tmp.delete();
                }


            }
        } catch (SAXException ex) {
            JOptionPane.showMessageDialog(null, "Die Versionsdatei konte nicht geladen werden!\n",
                    "Fehler!", JOptionPane.WARNING_MESSAGE);
        } catch (FileNotFoundException ex) {
            
            File application = new File(pfad + "\\.MigronitorPC\\MigronitorPC.jar");
            if(!application.exists()){
                JOptionPane.showMessageDialog(null, "Dateien konnten nicht gelesen werden!\n"
                            + "Bitte überprüfen sie ihre Internetverbindung!",
                    "Fehler!", JOptionPane.WARNING_MESSAGE);
            }else{
                try {
                     Runtime.getRuntime().exec("java -jar " + pfad + "\\.MigronitorPC\\MigronitorPC.jar");
                } catch (IOException ex1) {
                    Logger.getLogger(AutoUpdater.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "IOEXception!",
                    "Fehler!", JOptionPane.WARNING_MESSAGE);
        } catch (InterruptedException ex) {
            JOptionPane.showMessageDialog(null, "Die Verbindung wurde Abgebrochen!\n"
                            + "Bitte überprüfen sie ihre Internetverbindung!",
                    "Verbindungsfehler", JOptionPane.WARNING_MESSAGE);
        }
    }

}
