package com.dr23.extractplaylist

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter


class ExtractPlaylist {

    @Parameter(names = "-playlist", description = "Playlist path (*.m3u)", required = true)
    String playlist;

    @Parameter(names = "-output", description = "Destination to copy files", required = true)
    String output;

    @Parameter(names = "-debug", description = "Enabled debug mode")
    Boolean debug = false;

    @Parameter(names = "-album", description = "Copy entire album")
    Boolean album = false;

    @Parameter(names = "-help", description = "Display this help", help = true)
    Boolean help;

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
            List<String> mp3s = main.getMp3s(playlist)

            // Add album mp3
            main.addAlbum(mp3s);

            // Copy mp3s
            main.copyMp3s(mp3s,playlist,output)
        }
    }

    /**
     * Retourne la liste de mp3 de la play liste
     */
    List<String> getMp3s(File playlist) {
        List<String> mp3s = []
        if (playlist.exists() && playlist.isFile()) {
            playlist.eachLine {

                if (!it.empty) {
                    File mp3 = new File(it);
                    // Les chemins doivent être relatif à la playlist
                    if (!mp3.absolute) {
                        mp3s.add(new File(playlist.parent + it).canonicalPath)
                    }
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
    List<String> addAlbum(List<String> mp3s){
        if (album){
            List mp3sWithAlbums = [];

            for (it in mp3s) {
                File mp3 = new File(it)
                if (!mp3sWithAlbums.contains(mp3.getParent())) {
                    mp3sWithAlbums.addAll(mp3.getParentFile().list(
                           {d, f-> f.endsWith(".mp3") } as FilenameFilter).toList())
                }
            }

            mp3sWithAlbums
        }else{
            mp3s
        }
    }


    /**
     * Copie la liste de mp3 dans repertoire de destination
     */
    Integer copyMp3s(List<String> mp3s, File playlist, File destination) {

        int nbFileCopied = 0;

        mp3s.each {
            // Fichier source
            File mp3 = new File(it)

            // Repertoire de destination
            File arborescence = new File(destination.canonicalPath + File.separatorChar +(mp3.canonicalPath - (playlist.canonicalPath - playlist.name))- mp3.name)
            arborescence.mkdirs()

            // Fichier de destination
            File dest = new File(arborescence.canonicalPath + File.separatorChar+ mp3.name)

            if (dest.exists()) {
                println("fichier existant $dest.absoluteFile")
            }else{

                if (debug) {
                    println("processing $mp3.absoluteFile to $dest.absoluteFile")
                }

                copy(mp3, dest)
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
