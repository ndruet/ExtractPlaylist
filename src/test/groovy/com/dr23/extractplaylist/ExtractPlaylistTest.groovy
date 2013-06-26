package com.dr23.extractplaylist

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException

class ExtractPlaylistTest extends GroovyTestCase {

    static String CURRENT_TEST_PATH = 'src/test/resources/com/dr23/extractplaylist/'

    def main = new ExtractPlaylist();


    void testParameters_sans_parametre_attend_options_required() {
        // Given
        String[] argv = []

        // When
        try {
            new JCommander(main, argv);
        }
        // Then
        catch (ParameterException e) {
            assertTrue(e.getMessage().contains("The following options are required: -output -playlist"))
        }
    }

    void testParameters_avec_commande_help_attend_affichage_aide() {
        // Given
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // Create a stream to hold the output
        System.setOut(new PrintStream(baos)); // Tell Java to use your special stream
        String[] argv = ["-help"]

        // When
        ExtractPlaylist.main(argv);

        // Then
        System.out.flush();
        assert baos.toString().contains('Usage: <main class> [options]')
    }

    void testMain_attend_chargement_playlist_gestion_album_copie_puis_rapport() {
        // Given
        boolean chargementPlayList= false
        boolean gestionAlbum= false
        boolean copie= false
        boolean rapport= false

        ExtractPlaylist.metaClass.getMp3s = {
            File playlist ->
                chargementPlayList = true
                return []
        }
        ExtractPlaylist.metaClass.addAlbum =  {
            List<File> mp3s, File playlist ->
                gestionAlbum = true
                return []
        }
        ExtractPlaylist.metaClass.copyMp3s =  {
            List<File> mp3s, File playlist, File destination ->
                copie = true
                return 0
        }
        ExtractPlaylist.metaClass.generateReport =  {
            List<File> mp3s, File playlist, File output ->
                rapport = true
                return ""
        }

        // When
        ExtractPlaylist.main('-playlist','playlist.m3u','-output','./','-report');

        // Then
        assertTrue(chargementPlayList)
        assertTrue(gestionAlbum)
        assertTrue(copie)
        assertTrue(rapport)
    }

    void testGetMp3s_avec_liste_attend_mp3s() {
        //Given
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')

        //When
        def mp3s = main.getMp3s(playlist)

        //Then
        assertTrue mp3s[0].absolutePath.contains(/artiste\album1\titre.mp3/)
        assertTrue mp3s[1].absolutePath.contains(/artiste\album1\autre1.mp3/)
        assertTrue mp3s[2].absolutePath.contains(/nonclasse1.mp3/)

    }

    void testGetMp3s_avec_liste_inexistante_attend_aucun_mp3() {
        //Given
        File playlist = new File(CURRENT_TEST_PATH)

        //When
        def mp3s = main.getMp3s(playlist)

        //Then
        assertTrue mp3s.size() == 0
    }

    void testCopyMp3s_avec_liste_attend_mp3() {
        //Given
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')
        File destination = new File('./copy/')
        def mp3s = main.getMp3s(playlist)

        //When
        Integer count = main.copyMp3s(mp3s, playlist, destination)

        //Then
        assertEquals 3 , count
        assertTrue new File(destination.canonicalPath+"/artiste/album1/titre.mp3").isFile()
        assertTrue new File(destination.canonicalPath+"/artiste/album1/autre1.mp3").isFile()
        assertTrue new File(destination.canonicalPath+"/nonclasse1.mp3").isFile()
        assertTrue new File(destination.canonicalPath ).deleteDir()
    }


    void testCopyMp3s_copie_deux_fois_attend_une_copie_effectuee() {
        //Given
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')
        File destination = new File('./copy/')
        def mp3s = main.getMp3s(playlist)

        // When / Then
        assertEquals(3, main.copyMp3s(mp3s, playlist, destination))
        assertEquals(0, main.copyMp3s(mp3s, playlist, destination))
        assertTrue(new File(destination.canonicalPath).deleteDir())
    }

    void testAddAlbum_avec_album_false_attend_liste_identique(){
        // Given
        main.@album = false
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')
        def mp3s = main.getMp3s(playlist)

        // When
        List<File> actual = main.addAlbum(mp3s,playlist)

        // Then
        assert actual == mp3s;

    }

    void testAddAlbum_avec_album_true_attend_liste_complete(){
        // Given
        main.@album = true
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')
        def mp3s = main.getMp3s(playlist)

        // When
        List<File> actual = main.addAlbum(mp3s,playlist);

        // Then
        assert actual.size() == 4
        assert actual.find({it.name.endsWith("autre1.mp3")})
        assert actual.find({it.name.endsWith("autre2.mp3")})
        assert actual.find({it.name.endsWith("titre.mp3")})
        assert actual.find({it.name.endsWith("nonclasse1.mp3")})

    }

    void testGenerateReport_attend_xml(){
        // Given
        main.@report = true
        File output = new File('./');
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')
        def mp3s = main.getMp3s(playlist)

        // When
        String xml = main.generateReport(mp3s,playlist,output)

        // Then
        assert xml == new File(CURRENT_TEST_PATH+'report-expected.xml').text.replaceAll('\r','')
    }

    void testGenerateReport_attend_null(){
        // Given
        main.@report = false
        File output = new File('./');
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')
        def mp3s = main.getMp3s(playlist)

        // When
        String xml = main.generateReport(mp3s,playlist,output)

        // Then
        assertNull xml;
    }
}
