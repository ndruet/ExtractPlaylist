package com.dr23.extractplaylist
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import groovy.xml.MarkupBuilder

class ExtractPlaylist {

    @Parameter(names = "-playlist", description = "Playlist path (*.m3u)", required = true)
    private String playlist;

    @Parameter(names = "-output", description = "Destination to copy files", required = true)
    private String output;

    @Parameter(names = "-report", description = "Generate report XML")
    private  Boolean report = false;

    @Parameter(names = "-album", description = "Copy entire album")
    private Boolean album = false;

    @Parameter(names = "-help", description = "Display this help", help = true)
    private Boolean help;

    static main(args) {
        // Initialisation
        ExtractPlaylist main = new ExtractPlaylist()
        JCommander jcmd = new JCommander(main, args);

        // Help
        if (main.help) {
            jcmd.usage();
        } else {

            def playlist = new File(main.playlist)
            def output = new File(main.output)

            // Extract playlist
            List<File> mp3s = main.getMp3s(playlist)

            // Add album
            mp3s = main.addAlbum(mp3s)

            // Copy mp3s
            main.copyMp3s(mp3s, playlist, output)

            // Generate report
            main.generateReport(mp3s, playlist, output)
        }
    }

    /**
     * Génération du rapport
     */
    String generateReport(List<File> mp3s, File playlist, File output) {
       if (report){
           def writer = new StringWriter()
           def xml = new MarkupBuilder(writer)
           xml.report(from:playlist.getName(),to:output){
               mp3s.each {
                   musique(titre:it.getName())
               }
           }

           writer.toString()
       }else{
           null
       }
    }

    /**
     * Retourne la liste de mp3 de la Playlist
     */
    List<File> getMp3s(File playlist) {
        List<File> mp3s = []
        if (playlist.exists() && playlist.isFile()) {
            playlist.eachLine {
                if (!it.empty) {
                    mp3s += new File(playlist.parent + it)
                }
            }
        } else {
            println("file $playlist doesn't exist or not a file")
        }

        mp3s
    }

    /**
     * Ajout les albums complets dont sont issus les mp3 de la playlist
     */
    List<String> addAlbum(List<File> mp3s) {
        if (album) {
            List albums = [];

            for (mp3 in mp3s) {
                if (!(mp3.getParent() in albums)) {
                    albums += filterMp3sFromDirectory(mp3.getParentFile())
                }
            }

            albums
        } else {
            mp3s
        }
    }

    /**
     * Filtre les fichiers mp3 d'un album
     */
    List<String> filterMp3sFromDirectory(File directory){
        List mp3s = directory.list({ d, f -> f.endsWith(".mp3") } as FilenameFilter).toList()
        mp3s.collect ({
            directory.getPath() + File.separatorChar + it
        })
    }

    /**
     * Copie la liste de mp3 dans repertoire de destination
     */
    Integer copyMp3s(List<File> mp3s, File playlist, File destination) {

        int nbFileCopied = 0;

        mp3s.each {
            // Repertoire de destination
            File arborescence = new File(destination.canonicalPath + File.separatorChar + (it.canonicalPath - (playlist.canonicalPath - playlist.name)) - it.name)
            arborescence.mkdirs()

            // Fichier de destination
            File dest = new File(arborescence.canonicalPath + File.separatorChar + it.name)

            if (!dest.exists()) {
                copy(it, dest)
                nbFileCopied++;
            }
        }

        nbFileCopied
    }


    def copy(File src, File dest) {

        def srcStream = src.newDataInputStream()
        def destStream = dest.newDataOutputStream()

        destStream << srcStream

        srcStream.close()
        destStream.close()
    }

}
