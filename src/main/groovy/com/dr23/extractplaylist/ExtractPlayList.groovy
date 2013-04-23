package com.dr23.extractplaylist

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter


class ExtractPlayList {

    @Parameter(names = "-playlist", description = "Playlist path (*.m3u)", required = true)
    String playlist;

    @Parameter(names = "-output", description = "Destination to copy file", required = true)
    String output;

    @Parameter(names = "-debug", description = "Enabled debug mode")
    Boolean debug = false;

    @Parameter(names = "-help", description = "Display this help", help = true)
    Boolean help;

    static main(args) {
        // Initialisation
        def extractPlayList = new ExtractPlayList()
        def jcmd = new JCommander(extractPlayList, args);

        // Help
        if (extractPlayList.help) {
            jcmd.usage();
        } else {

            // Extract playlist
            def mp3s = extractPlayList.getMp3s(extractPlayList.playlist)

            // Copy mp3s
            extractPlayList.copyMp3s(mp3s, extractPlayList.output)
        }
    }

    /**
     * Retourne la liste de mp3 de la play liste
     */
    def getMp3s(path) {
        def mp3s = []
        File file = new File(path)
        if (file.exists() && file.isFile()) {
            file.eachLine {
                line -> mp3s.add(line)
            }
        } else {
            println("file $path doesn't exist or not a file")
        }
        return mp3s
    }

    /**
     * Copy la liste de mp3 dans repertoire de destination
     */
    def copyMp3s(mp3s, dir) {
        mp3s.each {

            // Fichier de source
            File src = new File(it)

            // Repertoire de destination
            new File(dir + '/' + (it - src.name)).mkdirs()

            // Fichier de destination
            File dest = new File(dir + '/' + it)

            if (debug) {
                println("processing $src.absoluteFile to $dest.absoluteFile")
            }

            copy(src, dest)
        }
    }


    def copy = { def src, def dest ->

        def srcStream = src.newDataInputStream()
        def destStream = dest.newDataOutputStream()

        destStream << srcStream

        srcStream.close()
        destStream.close()
    }

}
