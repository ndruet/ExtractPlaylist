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
        String[] argv = ["-help"]

        // When
        try {
            new JCommander(main, argv);
        }
        // Then
        catch (ParameterException e) {
            assertTrue(e.getMessage().contains("The following options are required: -output -playlist"))
        }
    }

    void testGetMp3s_avec_liste_attend_mp3s() {
        //Given
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')

        //When
        def mp3s = main.getMp3s(playlist)

        //Then
        assertTrue mp3s[0].absolutePath.contains(/artiste\album\titre.mp3/)
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
        File destination = new File('./')
        def mp3s = main.getMp3s(playlist)

        //When
        main.copyMp3s(mp3s, playlist, destination)

        //Then
        assertTrue(new File("./artiste/album/titre.mp3").isFile())
        assertTrue(new File(destination.canonicalPath + "/artiste").deleteDir())
    }


    void testCopyMp3s_copie_deux_fois_attend_une_copie_effectuee() {
        //Given
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')
        File destination = new File('./')
        def mp3s = main.getMp3s(playlist)

        // When / Then
        assertEquals(1, main.copyMp3s(mp3s, playlist, destination))
        assertEquals(0, main.copyMp3s(mp3s, playlist, destination))
        assertTrue(new File(destination.canonicalPath + "/artiste").deleteDir())
    }

    void testAddAlbum_avec_album_false_attend_liste_identique(){
        // Given
        main.@album = false
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')
        def mp3s = main.getMp3s(playlist)

        // When
        List<String> actual = main.addAlbum(mp3s);

        // Then
        assert actual == mp3s;

    }

    void testAddAlbum_avec_album_true_attend_liste_complete(){
        // Given
        main.@album = true
        File playlist = new File(CURRENT_TEST_PATH + '#PlayList.m3u')
        def mp3s = main.getMp3s(playlist)

        // When
        List<String> actual = main.addAlbum(mp3s);

        // Then
        assert actual.find {it.endsWith("autre.mp3")};
        assert actual.find {it.endsWith("titre.mp3")};
    }

    void testGenerateReport(){
        // Given
        main.@report = true

        // When
        main.generateReport(null,null,null)

        // Then
    }
}
