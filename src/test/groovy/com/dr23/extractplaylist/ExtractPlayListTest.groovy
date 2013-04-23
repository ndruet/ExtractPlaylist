package com.dr23.extractplaylist

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException

class ExtractPlayListTest extends GroovyTestCase {

    static String PATH_RESOURCES_DIRECTORY = 'src/test/resources/'
    static String PATH_TEST_DIRECTORY = PATH_RESOURCES_DIRECTORY+'com/dr23/extractplaylist/'

    def main = new ExtractPlayList();

//    void testParameters_avec_parametres_attend_initialisation(){
//        String[] argv = { "-log", "2", "-groups", "unit1,unit2,unit3", "-debug", "-Doption=value", "a", "b", "c" };
//        new JCommander(main, argv);
//
//        Assert.assertEquals(2, jct.verbose.intValue());
//        Assert.assertEquals("unit1,unit2,unit3", jct.groups);
//        Assert.assertEquals(true, jct.debug);
//        Assert.assertEquals("value", jct.dynamicParams.get("option"));
//        Assert.assertEquals(Arrays.asList("a", "b", "c"), jct.parameters);
//    }

    void testParameters_sans_parametre_attend_options_required(){
        // Given
        String[] argv = []

        // When
        try{
            new JCommander(main, argv);
        }
        // Then
        catch (ParameterException e){
            assertTrue( e.getMessage().contains("The following options are required: -output -playlist" ))
        }
    }

    void testParameters_help_attend_help_displayed(){
        // Given
        String[] argv = ["-help"]

        // When
        try{
            new JCommander(main, argv);
        }
        // Then
        catch (ParameterException e){
            assertTrue( e.getMessage().contains("The following options are required: -output -playlist" ))
        }
    }

    void testGetMp3s_playList_existe_attend_mp3s() {
        //Given
        String path = PATH_TEST_DIRECTORY + '#PlayList.m3u'

        //When
        def mp3s = main.getMp3s(path)

        //Then
        assertTrue mp3s.get(0) == "artiste/album/titre.mp3"
    }

    void testGetMp3s_playList_inexistante_attend_mp3s() {
        //Given
        String path = PATH_TEST_DIRECTORY

        //When
        def mp3s = main.getMp3s(path)

        //Then
        assertTrue mp3s.size() == 0
    }

    void testCopyMp3s_liste_non_vide_attend_mp3_copies() {
        //Given
        def String path = PATH_TEST_DIRECTORY;
        def mp3s = ['artiste/album/titre.mp3']

        //When
        main.copyMp3s(mp3s,path)

        //Then
        assertTrue new File(PATH_TEST_DIRECTORY+'/artiste/album/titre.mp3').isFile()
        assertTrue new File(PATH_TEST_DIRECTORY+'/artiste').deleteDir()
    }
}
