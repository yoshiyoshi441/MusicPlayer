package com.setuga.musicplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

public class SamplePlayer {
    public static final Logger LOG = LogManager.getLogger(SamplePlayer.class);
    
    public static void main(final String[] args) {
        if (args.length < 1) {
            LOG.error("Please specify the music file.");
            return;
        }

        MusicPlayer player = new MusicPlayer();
        player.open(Paths.get(args[0]).toFile());

        // if you want to be able to loop playback, you use code below
        player.setLoop(true);

        player.play();

        LOG.info(player.getStatus());

        //if played music supports a control you can use Three code
        LOG.info("MinGain : {}", player.getMaxGain());
        LOG.info("MaxGain : {}", player.getMaxGain());
        LOG.info("Value : {}", player.getValue());

        //if played music extension is mp3, you can use code below
        LOG.info("title: {}", player.getProperties("title"));
        LOG.info("author: {}", player.getProperties("author"));
        LOG.info("album: {}", player.getProperties("album"));
        LOG.info("date: {}", player.getProperties("date"));
        LOG.info("copyright: {}", player.getProperties("copyright"));
        LOG.info("comment: {}", player.getProperties("comment"));
    }
}
