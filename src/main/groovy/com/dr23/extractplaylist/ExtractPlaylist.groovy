package com.dr23.extractplaylist
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

@Slf4j
class ExtractPlaylist {

    @Parameter(names = "-playlist", description = "Playlist path (*.m3u)", required = true)
    private String playlist;

    @Parameter(names = "-output", description = "Destination to copy files", required = true)
    private String output;

    @Parameter(names = "-report", description = "Generate report XML")
    private  Boolean report = false;

    @Parameter(names = "-album", description = "Copy entire album1")
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

            log.info "Playlist: ${playlist.getCanonicalPath()}"
            log.info "Output: ${output.getCanonicalPath()}"

            // Extract playlist

            List<File> mp3s = main.getMp3s(playlist)

            // Add album
            mp3s = main.addAlbum(mp3s,playlist)

            // Copy mp3s
            main.copyMp3s(mp3s, playlist, output)

            // Generate report
            println main.generateReport(mp3s, playlist, output)
        }
    }

    /**
     * Génération du rapport
     */
    String generateReport(List<File> mp3s, File playlist, File output) {
       if (report){
           log.info 'Génération du rapport...'

           def writer = new StringWriter()
           def xml = new MarkupBuilder(writer)
           xml.report(from:playlist.getName(),to:output){
               mp3s.each {
                   musique(titre:it.getName())
               }
           }

           writer.toString()
       }else{
           log.info 'Aucune génération du rapport...'
           null
       }
    }

    /**
     * Retourne la liste de mp3 de la Playlist
     */
    List<File> getMp3s(File playlist) {
        List<File> mp3s = []
        if (playlist.exists() && playlist.isFile()) {

            log.info 'Extraction de la playlist en cours...'

            playlist.eachLine {
                if (!it.empty) {
                    mp3s += new File(playlist.parent + it)
                }
            }

            log.info 'Extraction de la playlist terminée'

        } else {
            log.info "$playlist n'existe pas ou n'est pas un fichier"
        }

        mp3s
    }

    /**
     * Ajout les albums complets dont sont issus les mp3 de la playlist
     */
    List<File> addAlbum(List<File> mp3s, File playlist) {
        if (album) {
            List albums = [];// Les albums traités
            List<File> playlistEtAlbum = [];

            log.info "Le contenu des albums est en cours d'ajout..."

            for (mp3 in mp3s) {
                // Evite de copier les mp3s non triés à la racine de la playlist
                if (mp3.getCanonicalFile().getParent() == playlist.getCanonicalFile().getParent()){
                    playlistEtAlbum += mp3
                }
                // Evite de parcourir les albums deux fois de suite
                else if (!(mp3.getParent() in albums)) {
                    playlistEtAlbum += filterMp3sFromDirectory(mp3.getParentFile())
                    albums +=mp3.getParent()
                }
            }

            log.info "Le contenu des albums a été ajouté"

            playlistEtAlbum
        } else {
            log.info "Le contenu des albums n'est pas pris en compte"
            mp3s
        }
    }

    /**
     * Filtre les fichiers mp3 d'un album1
     */
    List<File> filterMp3sFromDirectory(File directory){
        directory.listFiles({ d, f -> f.endsWith(".mp3") } as FilenameFilter)
    }

    /**
     * Copie la liste de mp3 dans repertoire de destination
     */
    Integer copyMp3s(List<File> mp3s, File playlist, File destination) {

        Integer count = 0

        log.info 'Copie des mp3s en cours...'

        mp3s.each {
            // Repertoire de destination
            File arborescence = new File(destination.canonicalPath+ File.separatorChar + (it.canonicalPath - (playlist.canonicalPath- playlist.name)) - it.name)
            arborescence.mkdirs()

            // Fichier de destination
            File dest = new File(arborescence.canonicalPath+ File.separatorChar + it.name)
            if (!dest.exists()) {
                log.debug "$it copié"
                count++
                copy(it, dest)
            }else{
                log.debug "$it existe déjà"
            }
        }

        log.info  "$count mp3s ont été copiés"

        count
    }


    def copy(File src, File dest) {

        def srcStream = src.newDataInputStream()
        def destStream = dest.newDataOutputStream()

        destStream << srcStream

        srcStream.close()
        destStream.close()
    }

}
