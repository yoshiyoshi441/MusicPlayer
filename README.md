# MusicPlayer

Create to Bullshit

##Use Lib:  
mp3spi1.9.5.jar  
jl1.0.1.jar  
tritonus_share-0.3.6.jar  
log4j-core-2.2.jar  
log4j-api-2.2.jar  

### Sample code:  

```java
MusicPlayer musicPlayer = new MusicPlayer();
musicPlayer.open(new File("path"));

// if you want to be able to loop playback, you use code below 
musicPlayer.setLoop(true);

musicPlayer.play();

System.out.println(musicPlayer.getStatus());

//if played music supports a control you can use Three code
System.out.println("MinGain : " + musicPlayer.getMinGain());
System.out.println("MaxGain : " + musicPlayer.getMaxGain());
System.out.println("Value : " + musicPlayer.getValue());

//if played music extension is mp3, you can use code below
System.out.println(musicPlayer.getProperties("title"));
System.out.println(musicPlayer.getProperties("author"));
System.out.println(musicPlayer.getProperties("album"));
System.out.println(musicPlayer.getProperties("date"));
System.out.println(musicPlayer.getProperties("copyright"));
System.out.println(musicPlayer.getProperties("comment"));
```

Reference:  
https://docs.oracle.com/javase/8/docs/api/javax/sound/sampled/package-summary.html  
http://www.javazoom.net/mp3spi/documents.html  
